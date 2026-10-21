package de.tobi1craft.repairswapper;

import com.mojang.blaze3d.platform.InputConstants;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepairSwapperClient implements ClientModInitializer {

    public static final String MOD_ID = "repair-swapper";

    private static final int ENABLED_MESSAGE_COLOR = 0x55FF55;
    private static final int DISABLED_MESSAGE_COLOR = 0xFF5555;
    private static final int WARNING_MESSAGE_COLOR = 0xFFFF55;
    private static final int REPAIRED_MESSAGE_COLOR = 0x55FFFF;

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyMapping keyBinding;

    private static boolean enabled;
    private static int swappedSlot = -1; // -1 for nothing swapped currently
    private static ItemStack swappedStack = ItemStack.EMPTY; // item moved into the offhand
    private static ItemStack previousOffhand = ItemStack.EMPTY; // item moved out of the offhand
    private static int tickCounter = 0;
    private static boolean autoSuppressed; // set after a manual disable until the orbs are gone

    private static void tick(Minecraft client) {
        while (keyBinding.consumeClick()) {
            if (client.gui.screen() != null) continue;
            if (enabled) {
                disable(client);
                autoSuppressed = true;
            } else enable(client, false);
        }
        LocalPlayer player = client.player;
        if (player == null) return;
        boolean orbsNearby = hasOrbsNearby(player);
        if (!orbsNearby) autoSuppressed = false;

        // Restore the offhand while paused, so leaving the world never keeps the swapped item there
        if (client.gui.screen() instanceof PauseScreen) {
            if (swappedSlot != -1 && canUseInventory(client, player, true)) swapBack(client, player);
            return;
        }

        if (enabled && !meetsMainHandRequirement(player))
            disable(client);
        if (!enabled) {
            if (swappedSlot != -1 && canUseInventory(client, player, false))
                swapBack(client, player);
            if (orbsNearby)
                enable(client, true);
            return;
        }
        if (orbsNearby)
            tickCounter = 0;
        int delay = getResetDelayTicks();
        if (delay != 0 && !player.isCrouching()) {
            if (tickCounter >= delay) {
                disable(client);
                tickCounter = 0;
                return;
            }
            tickCounter++;
        }
        doSwapping(client, player);
    }

    private static void doSwapping(Minecraft client, LocalPlayer player) {
        if (!canUseInventory(client, player, false)) return;
        ItemStack offhand = player.getOffhandItem();

        if (swappedSlot != -1) {
            if (!isSwappedItem(offhand)) {
                abandonSwap(client, offhand);
                return;
            }
            // Keep the current item until it is fully repaired to avoid swapping back and forth
            if (offhand.isDamaged()) return;
        } else if (isRepairable(offhand) || isProtectedOffhand(offhand)) {
            return;
        }

        int slot = findRepairTarget(player);
        if (slot == -1) {
            if (swappedSlot != -1) swapBack(client, player);
            if (RepairSwapperConfig.disableWhenRepaired)
                disable(client, "hud.repair-swapper.allRepaired", REPAIRED_MESSAGE_COLOR, true);
            return;
        }

        // The next item is swapped in on the following tick, once the offhand holds its original item again
        if (swappedSlot != -1) {
            swapBack(client, player);
            return;
        }

        previousOffhand = offhand.copy();
        swappedStack = player.getInventory().getItem(slot).copy();
        swappedSlot = slot;
        swapWithOffhand(client, player, slot);
    }

    public static void enable(Minecraft client, boolean autoTrigger) {
        tickCounter = 0;
        if (enabled || (autoTrigger && (!RepairSwapperConfig.auto || autoSuppressed))) return;
        LocalPlayer player = client.player;
        if (player == null) return;
        if (!meetsMainHandRequirement(player)) {
            if (!autoTrigger)
                showOverlayMessage(client, getMainHandRequirementKey(), WARNING_MESSAGE_COLOR);
            return;
        }
        if (findRepairTarget(player) == -1) {
            if (!autoTrigger)
                showOverlayMessage(client, "hud.repair-swapper.noRepairable", WARNING_MESSAGE_COLOR);
            return;
        }
        if (swappedSlot == -1 && isProtectedOffhand(player.getOffhandItem())) {
            if (!autoTrigger)
                showOverlayMessage(client, "hud.repair-swapper.totemProtected", WARNING_MESSAGE_COLOR);
            return;
        }
        enabled = true;
        autoSuppressed = false;
        showOverlayMessage(client, "hud.repair-swapper.enabled", ENABLED_MESSAGE_COLOR);
        playSound(client, 1.4F);
    }

    public static void disable(Minecraft client) {
        disable(client, "hud.repair-swapper.disabled", DISABLED_MESSAGE_COLOR, false);
    }

    private static void disable(Minecraft client, String translationKey, int color, boolean repaired) {
        showOverlayMessage(client, translationKey, color);
        if (repaired) playSound(client, SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.value(), 2.0F, 0.5F));
        else playSound(client, 0.8F);
        enabled = false;
        if (swappedSlot == -1 || !canUseInventory(client, client.player, false)) return;
        swapBack(client, client.player);
    }

    private static void reset() {
        enabled = false;
        autoSuppressed = false;
        tickCounter = 0;
        clearSwap();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * @return the item currently being repaired in the offhand, or {@link ItemStack#EMPTY}
     */
    public static ItemStack getRepairingStack(LocalPlayer player) {
        ItemStack offhand = player.getOffhandItem();
        return swappedSlot != -1 && isSwappedItem(offhand) ? offhand : ItemStack.EMPTY;
    }

    private static void swapBack(Minecraft client, LocalPlayer player) {
        ItemStack offhand = player.getOffhandItem();
        if (!isSwappedItem(offhand)) {
            abandonSwap(client, offhand);
            return;
        }
        int restoreSlot = getRestoreSlot(player);
        if (restoreSlot == -1) {
            abandonSwap(client, offhand);
            return;
        }
        swapWithOffhand(client, player, restoreSlot);
        clearSwap();
    }

    /**
     * Finds the slot to put the swapped item back to without moving anything else.
     *
     * @return the inventory slot, or -1 if the original slot was taken by something else
     */
    private static int getRestoreSlot(LocalPlayer player) {
        Inventory inventory = player.getInventory();
        ItemStack current = inventory.getItem(swappedSlot);
        if (!previousOffhand.isEmpty())
            return ItemStack.isSameItemSameComponents(current, previousOffhand) ? swappedSlot : -1;
        if (current.isEmpty()) return swappedSlot;
        // The offhand was empty and the original slot got filled (e.g. by a picked up item), any free slot works
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            if (i != inventory.getSelectedSlot() && inventory.getItem(i).isEmpty()) return i;
        }
        return -1;
    }

    /**
     * Forgets the swap without touching the inventory, used when the player moved the items themselves.
     */
    private static void abandonSwap(Minecraft client, ItemStack offhand) {
        LOGGER.info("Offhand changed unexpectedly, not restoring slot {}", swappedSlot);
        if (!offhand.isEmpty())
            showOverlayMessage(client, "hud.repair-swapper.restoreFailed", WARNING_MESSAGE_COLOR);
        clearSwap();
    }

    private static void clearSwap() {
        swappedSlot = -1;
        swappedStack = ItemStack.EMPTY;
        previousOffhand = ItemStack.EMPTY;
    }

    private static void swapWithOffhand(Minecraft client, LocalPlayer player, int inventorySlot) {
        client.gameMode.handleContainerInput(player.inventoryMenu.containerId, getMenuSlotForInventorySlot(inventorySlot),
                Inventory.SLOT_OFFHAND, ContainerInput.SWAP, player);
    }

    /**
     * Compares the item in the offhand with the one swapped in, ignoring the durability it gained meanwhile.
     */
    private static boolean isSwappedItem(ItemStack stack) {
        if (swappedStack.isEmpty() || !ItemStack.isSameItem(stack, swappedStack)) return false;
        ItemStack current = stack.copy();
        ItemStack original = swappedStack.copy();
        current.setDamageValue(0);
        original.setDamageValue(0);
        return ItemStack.isSameItemSameComponents(current, original);
    }

    /**
     * @return the main inventory slot holding the best item to repair, or -1 if there is none
     */
    private static int findRepairTarget(LocalPlayer player) {
        Inventory inventory = player.getInventory();
        int bestSlot = -1;
        double bestValue = Double.MAX_VALUE;
        // Armor, offhand and the selected slot are already repaired by vanilla Mending
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (slot == inventory.getSelectedSlot()) continue;
            ItemStack stack = inventory.getItem(slot);
            if (!isRepairable(stack)) continue;
            double value = getPriorityValue(stack);
            if (value < bestValue) {
                bestValue = value;
                bestSlot = slot;
            }
        }
        return bestSlot;
    }

    private static double getPriorityValue(ItemStack stack) {
        int remaining = stack.getMaxDamage() - stack.getDamageValue();
        return switch (RepairSwapperConfig.priority) {
            case LOWEST_DURABILITY -> remaining;
            case LOWEST_PERCENTAGE -> (double) remaining / stack.getMaxDamage();
        };
    }

    private static boolean isRepairable(ItemStack stack) {
        return stack.isDamaged()
                && EnchantmentHelper.has(stack, EnchantmentEffectComponents.REPAIR_WITH_XP)
                && !RepairSwapperConfig.excludedItems.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    }

    private static boolean isProtectedOffhand(ItemStack offhand) {
        return RepairSwapperConfig.protectTotem && offhand.has(DataComponents.DEATH_PROTECTION);
    }

    private static boolean meetsMainHandRequirement(LocalPlayer player) {
        ItemStack stack = player.getMainHandItem();
        return switch (RepairSwapperConfig.mainHandRequirement) {
            case SWORDS -> stack.is(ItemTags.SWORDS);
            case MELEE_WEAPONS -> stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.is(ItemTags.SPEARS)
                    || stack.is(Items.TRIDENT) || stack.is(Items.MACE);
            case ANY -> true;
        };
    }

    private static String getMainHandRequirementKey() {
        return RepairSwapperConfig.mainHandRequirement == RepairSwapperConfig.MainHandRequirement.SWORDS
                ? "hud.repair-swapper.requiresSword"
                : "hud.repair-swapper.requiresWeapon";
    }

    private static boolean hasOrbsNearby(LocalPlayer player) {
        int range = RepairSwapperConfig.orbDetectionRange;
        return range > 0 && !player.level().getEntitiesOfClass(ExperienceOrb.class, player.getBoundingBox().inflate(range)).isEmpty();
    }

    private static int getResetDelayTicks() {
        return (int) Math.round(RepairSwapperConfig.resetDelaySeconds * 20);
    }

    private static int getMenuSlotForInventorySlot(int inventorySlot) {
        return inventorySlot < Inventory.SELECTION_SIZE
                ? InventoryMenu.USE_ROW_SLOT_START + inventorySlot
                : inventorySlot;
    }

    private static boolean canUseInventory(Minecraft client, LocalPlayer player, boolean allowPauseScreen) {
        return player != null
                && client.gameMode != null
                && (client.gui.screen() == null || (allowPauseScreen && client.gui.screen() instanceof PauseScreen))
                && player.containerMenu == player.inventoryMenu
                && player.containerMenu.getCarried().isEmpty();
    }

    private static void showOverlayMessage(Minecraft client, String translationKey, int color) {
        client.gui.hud.setOverlayMessage(Component.translatable(translationKey).withColor(color), false);
    }

    private static void playSound(Minecraft client, float pitch) {
        playSound(client, SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
    }

    private static void playSound(Minecraft client, SimpleSoundInstance sound) {
        if (RepairSwapperConfig.playSounds) client.getSoundManager().play(sound);
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Repair Swapper initializing");
        MidnightConfig.init(MOD_ID, RepairSwapperConfig.class);
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "main")
        );
        keyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.repair-swapper.toggle",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_R,
                category
        ));
        ClientTickEvents.END_CLIENT_TICK.register(RepairSwapperClient::tick);
        // The swap state belongs to one world, it must never be applied to another inventory
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR,
                Identifier.fromNamespaceAndPath(MOD_ID, "repair_progress"), RepairSwapperHud::extractRenderState);
    }
}
