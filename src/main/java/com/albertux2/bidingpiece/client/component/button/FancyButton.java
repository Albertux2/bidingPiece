package com.albertux2.bidingpiece.client.component.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

public class FancyButton extends Button {
    public FancyButton(int x, int y, int w, int h, ITextComponent msg, IPressable onPress) {
        super(x, y, w, h, msg, onPress);
    }
    @Override
    public void renderButton(MatrixStack ms, int mx, int my, float pt) {
        float a = this.isHovered() ? 1.0f : 0.85f;
        int bg1 = ((int)(a*255) << 24) | 0x1A1A1A;
        int bg2 = ((int)(a*255) << 24) | 0x2A2A2A;
        int hl  = 0x66FFFFFF;
        int sh  = 0x66000000;
        fill(ms, x-2, y+height, x+width+2, y+height+2, sh);
        fill(ms, x-2, y-2, x+width+2, y, hl);
        fill(ms, x-2, y, x, y+height, hl);
        fill(ms, x+width, y, x+width+2, y+height, sh);
        fillGradient(ms, x, y, x+width, y+height, bg1, bg2);
        int c = this.isHovered() ? 0xFFFFFFFF : 0xFFEAEAEA;
        drawCenteredString(ms, Minecraft.getInstance().font, getMessage().getString(), x + width/2, y + (height-8)/2, c);
    }
}