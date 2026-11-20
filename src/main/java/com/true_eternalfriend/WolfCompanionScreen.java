package com.true_eternalfriend;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Inventory;

/** Client view only: slots and numbers are synchronized by the server menu. */
public final class WolfCompanionScreen extends AbstractContainerScreen<WolfCompanionMenu> {
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(TrueEternalFriend.MOD_ID, "textures/gui/wolf_companion.png");
    private static final int TEXT_COLOR = 0xFF404040;

    public WolfCompanionScreen(WolfCompanionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 230, 220);
        inventoryLabelX = 8;
        inventoryLabelY = 108;
        titleLabelX = 8;
        titleLabelY = 6;
    }

    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        Wolf wolf = menu.wolf();
        if (wolf != null && wolf.isAlive()) {
            InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, leftPos + 58, topPos + 17,
                leftPos + 112, topPos + 101, 27, 0.15f, mouseX, mouseY, wolf);
        }
    }

    @Override protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, TEXT_COLOR, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT_COLOR, false);
        graphics.text(font, Component.translatable("screen.true_eternal_friend.slot_armor"), 3, 21, TEXT_COLOR, false);
        graphics.text(font, Component.translatable("screen.true_eternal_friend.slot_food"), 3, 45, TEXT_COLOR, false);
        graphics.text(font, Component.translatable("screen.true_eternal_friend.slot_claw"), 3, 69, TEXT_COLOR, false);
        graphics.text(font, Component.translatable("screen.true_eternal_friend.health", menu.value(0) / 10f, menu.value(1) / 10f), 113, 17, TEXT_COLOR, false);
        graphics.text(font, Component.translatable("screen.true_eternal_friend.defense", menu.value(8) / 10f), 113, 29, TEXT_COLOR, false);
        graphics.text(font, Component.translatable("screen.true_eternal_friend.attack_total", menu.value(9) / 10f), 113, 41, TEXT_COLOR, false);
        graphics.text(font, Component.translatable("screen.true_eternal_friend.damage", menu.value(6), menu.value(7)), 113, 53, TEXT_COLOR, false);
        graphics.text(font, Component.translatable("screen.true_eternal_friend.armor", menu.value(4), menu.value(5)), 113, 65, TEXT_COLOR, false);
        graphics.text(font, Component.translatable("screen.true_eternal_friend.cooldown", menu.value(2) / 20), 113, 77, TEXT_COLOR, false);
        graphics.text(font, Component.translatable("screen.true_eternal_friend.heal", menu.value(3)), 113, 89, TEXT_COLOR, false);
    }
}
