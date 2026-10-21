package de.tobi1craft.repairswapper;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

/**
 * Shows the durability of the item being repaired above the offhand slot.
 */
public final class RepairSwapperHud {

    private static final int HOTBAR_HALF_WIDTH = 91;
    private static final int OFFHAND_ITEM_OFFSET = 18; // from the hotbar edge to the offhand item center

    private RepairSwapperHud() {
    }

    public static void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!RepairSwapperConfig.showHud || !RepairSwapperClient.isEnabled()) return;
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || player.isSpectator()) return;
        ItemStack stack = RepairSwapperClient.getRepairingStack(player);
        if (stack.isEmpty() || !stack.isDamageableItem()) return;

        int percent = (stack.getMaxDamage() - stack.getDamageValue()) * 100 / stack.getMaxDamage();
        int center = graphics.guiWidth() / 2;
        int x = player.getMainArm().getOpposite() == HumanoidArm.LEFT
                ? center - HOTBAR_HALF_WIDTH - OFFHAND_ITEM_OFFSET
                : center + HOTBAR_HALF_WIDTH + OFFHAND_ITEM_OFFSET;
        int y = graphics.guiHeight() - 22 - client.font.lineHeight - 1;
        graphics.centeredText(client.font, percent + "%", x, y, 0xFF000000 | stack.getBarColor());
    }
}
