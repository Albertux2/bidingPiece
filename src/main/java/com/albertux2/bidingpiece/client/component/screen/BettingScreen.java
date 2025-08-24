package com.albertux2.bidingpiece.client.component.screen;

import com.albertux2.bidingpiece.client.component.button.FancyButton;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class BettingScreen extends Screen {
    private static final ResourceLocation BACKGROUND = new ResourceLocation("minecraft", "textures/gui/demo_background.png");
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 25;

    public BettingScreen() {
        super(new StringTextComponent("Betting System"));
    }

    @Override
    protected void init() {
        super.init();

        // Botón ITEM
        this.addButton(new FancyButton(
            this.width / 2 - 65,
            this.height / 2 - 15,
            BUTTON_WIDTH,
            BUTTON_HEIGHT,
            new StringTextComponent("ITEM"),
            (button) -> {
                Minecraft.getInstance().setScreen(new ItemBettingScreen(this));
            }
        ));

        this.addButton(new FancyButton(
            this.width / 2 - 65,
            this.height / 2 + 25,
            BUTTON_WIDTH,
            BUTTON_HEIGHT,
            new StringTextComponent("SYSTEM"),
            (button) -> {
                Minecraft.getInstance().setScreen(new SystemBettingScreen(this));
            }
        ));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.fillGradient(matrixStack, 0, 0, this.width, this.height, 0x80000000, 0x80202020);

        int panelWidth = 300;
        int panelHeight = 200;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = (this.height - panelHeight) / 2;

        fill(matrixStack, panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF2A2A2A);
        fill(matrixStack, panelX + 2, panelY + 2, panelX + panelWidth - 2, panelY + panelHeight - 2, 0xFF1E1E1E);
        fill(matrixStack, panelX, panelY, panelX + panelWidth, panelY + 3, 0xFF4A90E2);

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        String title = "Select your bid";
        int titleWidth = this.font.width(title);
        this.font.draw(matrixStack, title, (this.width - titleWidth) / 2f, this.height / 2f - 60, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}