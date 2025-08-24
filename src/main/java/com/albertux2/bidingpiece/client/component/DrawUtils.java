package com.albertux2.bidingpiece.client.component;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;

public class DrawUtils {

    public static void drawTextWithBorder(MatrixStack matrixStack, FontRenderer font, String text, int x, int y, int mainColor,
        int borderColor) {
        font.draw(matrixStack, text, x - 1, y, borderColor);
        font.draw(matrixStack, text, x + 1, y, borderColor);
        font.draw(matrixStack, text, x, y - 1, borderColor);
        font.draw(matrixStack, text, x, y + 1, borderColor);

        font.draw(matrixStack, text, x, y, mainColor);
    }
}
