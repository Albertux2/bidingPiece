package com.albertux2.bidingpiece.client.component.screen;

import com.albertux2.bidingpiece.client.component.button.FancyButton;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

public class SystemBettingScreen extends Screen {
    private final Screen parent;

    public SystemBettingScreen(Screen parent) {
        super(new StringTextComponent("System Betting"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        // Botón Back
        this.addButton(new FancyButton(
            10,
            10,
            60,
            24,
            new StringTextComponent("← Back"),
            (button) -> Minecraft.getInstance().setScreen(parent)
        ));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Fondo moderno
        this.fillGradient(matrixStack, 0, 0, this.width, this.height, 0x80000000, 0x80202020);

        // Panel central
        int panelWidth = 400;
        int panelHeight = 300;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = (this.height - panelHeight) / 2;

        fill(matrixStack, panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF2A2A2A);
        fill(matrixStack, panelX + 2, panelY + 2, panelX + panelWidth - 2, panelY + panelHeight - 2, 0xFF1E1E1E);
        fill(matrixStack, panelX, panelY, panelX + panelWidth, panelY + 3, 0xFF4A90E2);

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        // Título
        String title = "System Betting";
        int titleWidth = this.font.width(title);
        this.font.draw(matrixStack, title, (this.width - titleWidth) / 2f, panelY + 20, 0xFFFFFF);

        // Mensaje dummy
        String message = "This feature is coming soon...";
        int messageWidth = this.font.width(message);
        this.font.draw(matrixStack, message, (this.width - messageWidth) / 2f, this.height / 2f, 0xAAAAAAAA);

        // Decoración
        fill(matrixStack, panelX + 50, this.height / 2 + 20, panelX + panelWidth - 50, this.height / 2 + 22, 0xFF4A90E2);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}