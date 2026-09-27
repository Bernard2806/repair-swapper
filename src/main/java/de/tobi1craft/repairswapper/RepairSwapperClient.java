package de.tobi1craft.repairswapper;

import com.mojang.blaze3d.platform.InputConstants;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

public class RepairSwapperClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("repair-swapper");

    private static KeyMapping keyBinding;

    private static boolean enabled;
    private static int swappedSlot = -1; // -1 for nothing swapped currently
    private static int swappedSlotTo;
    private static int tickCounter = 0;

    public static void doSwapping() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (!canUseInventory(client, player)) return;

        int slot = getLeastDurabilitySlot(player);

        if (!needsSwap(player, slot)) return;

        if (swappedSlot != -1) swapBack(client, player);

        swappedSlot = slot;
        swappedSlotTo = 45;

        if (swappedSlot < 9) {
            player.getInventory().setSelectedSlot(swappedSlot);
            swappedSlot = -1;
            return;
        }
        if (RepairSwapperConfig.hand == RepairSwapperConfig.Hand.MAINHAND)
            swappedSlotTo = player.getInventory().getSelectedSlot() + 36;

        if (!player.getInventory().getItem(swappedSlotTo).isEmpty())
            client.gameMode.handleContainerInput(player.inventoryMenu.containerId, swappedSlotTo, 0, ContainerInput.PICKUP, player);
        client.gameMode.handleContainerInput(player.inventoryMenu.containerId, swappedSlot, 0, ContainerInput.PICKUP, player);
        client.gameMode.handleContainerInput(player.inventoryMenu.containerId, swappedSlotTo, 0, ContainerInput.PICKUP, player);
    }

    private static void tick(Minecraft client) {
        while (keyBinding.consumeClick()) {
            if (enabled) disable(client);
            else enable(client, false);
        }
        if (!enabled) {
            if (swappedSlot != -1 && canUseInventory(client, client.player))
                swapBack(client, client.player);
            return;
        }
        if (RepairSwapperConfig.delayToReset != 0) {
            if (tickCounter >= RepairSwapperConfig.delayToReset) {
                disable(client);
                tickCounter = 0;
                return;
            }
            tickCounter++;
        }
        doSwapping();
    }

    public static void enable(Minecraft client, boolean autoTrigger) {
        tickCounter = 0;
        if (enabled || (autoTrigger && !RepairSwapperConfig.auto)) return;
        if (swappedSlot != -1) {
            if (!canUseInventory(client, client.player)) return;
            swapBack(client, client.player);
        }
        if (client.player != null && getRepairableSlots(client.player).isEmpty()) {
            if (!autoTrigger)
                client.gui.hud.setOverlayMessage(Component.translatable("hud.repair-swapper.noRepairable"), false);
            return;
        }
        enabled = true;
        client.gui.hud.setOverlayMessage(Component.translatable("hud.repair-swapper.enabled"), false);
    }

    public static void disable(Minecraft client) {
        client.gui.hud.setOverlayMessage(Component.translatable("hud.repair-swapper.disabled"), false);
        enabled = false;
        if (swappedSlot == -1 || !canUseInventory(client, client.player)) return;
        swapBack(client, client.player);
    }

    @Unique
    private static int getLeastDurabilitySlot(LocalPlayer player) {
        List<Integer> repairable = getRepairableSlots(player);
        if (repairable.isEmpty()) return -1;
        int leastDurability = Integer.MAX_VALUE;
        int leastDurabilitySlot = -1;
        for (int slot : repairable) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getMaxDamage() - stack.getDamageValue() < leastDurability) {
                leastDurability = stack.getMaxDamage() - stack.getDamageValue();
                leastDurabilitySlot = slot;
            }
        }
        return leastDurabilitySlot;
    }

    private static void swapBack(Minecraft client, LocalPlayer player) {
        if (!player.getInventory().getItem(swappedSlot).isEmpty())
            client.gameMode.handleContainerInput(player.inventoryMenu.containerId, swappedSlot, 0, ContainerInput.PICKUP, player);
        client.gameMode.handleContainerInput(player.inventoryMenu.containerId, swappedSlotTo, 0, ContainerInput.PICKUP, player);
        client.gameMode.handleContainerInput(player.inventoryMenu.containerId, swappedSlot, 0, ContainerInput.PICKUP, player);
        swappedSlot = -1;
    }

    @Unique
    private static boolean needsSwap(LocalPlayer player, int slot) {
        return slot != -1 && slot <= 35 && player.getInventory().getSelectedSlot() != slot;
    }

    @Unique
    private static List<Integer> getRepairableSlots(LocalPlayer player) {
        Inventory inventory = player.getInventory();
        Holder<Enchantment> mending = player.level().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.MENDING);
        List<Integer> repairable = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isDamaged() && EnchantmentHelper.getItemEnchantmentLevel(mending, stack) > 0)
                repairable.add(i);
        }
        return repairable;
    }

    @Unique
    private static boolean canUseInventory(Minecraft client, LocalPlayer player) {
        return player != null
                && client.gameMode != null
                && client.gui.screen() == null
                && player.containerMenu == player.inventoryMenu
                && player.containerMenu.getCarried().isEmpty();
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Repair Swapper initializing");
        MidnightConfig.init("repair-swapper", RepairSwapperConfig.class);
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("repair-swapper", "main")
        );
        keyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.repair-swapper.toggle",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_R,
                category
        ));
        ClientTickEvents.END_CLIENT_TICK.register(RepairSwapperClient::tick);
    }
}
