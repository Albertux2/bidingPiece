package com.albertux2.bidingpiece.client.component.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;

import java.util.List;

import static net.minecraft.client.gui.AbstractGui.fill;

public class SelectedListComponent {
    private final List<BetItem> items;
    private final FontRenderer font;

    private int xLeft, xRight, startY, itemH, visibleSlots;
    private double uiScale;
    private int scroll = 0;

    private boolean visibleDeleteButton;

    public SelectedListComponent(List<BetItem> items, FontRenderer font, boolean visibleDeleteButton) {
        this.items = items;
        this.font = font;
        this.visibleDeleteButton = visibleDeleteButton;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    public void setLayout(int screenWidth, int startY, int itemH, int padL, int padR, double uiScale,
        int visibleSlots) {
        this.xLeft = padL;
        this.xRight = screenWidth - padR;
        this.startY = startY;
        this.itemH = itemH;
        this.uiScale = uiScale;
        this.visibleSlots = Math.max(1, visibleSlots);
        clampScroll();
    }

    public void render(MatrixStack ms) {
        int h = itemH * visibleSlots;
        fill(ms, xLeft, startY, xRight, startY + h, 0xAA202020);

        int x = xLeft + (int) Math.round(4 * uiScale);

        int end = Math.min(items.size(), scroll + visibleSlots);
        renderItem(ms, end, x);

        if (items.size() > visibleSlots) {
            renderScrollBar(ms, h);
        }
    }

    private void renderScrollBar(MatrixStack ms, int h) {
        int scrollBarWidth = 6;
        int scrollBarX = xRight + 6;
        int scrollBarY = startY + 2;
        int scrollBarHeigth = h - 4;
        fill(ms, scrollBarX, scrollBarY, scrollBarX + scrollBarWidth, scrollBarY + scrollBarHeigth, 0xFF202020);
        int thumbH = Math.max(20, (int) (scrollBarHeigth * (visibleSlots / (float) items.size())));
        int thumbY = scrollBarY + (int) ((scrollBarHeigth - thumbH) * (scroll / (float) (items.size() - visibleSlots)));
        fill(ms, scrollBarX, thumbY, scrollBarX + scrollBarWidth, thumbY + thumbH, 0xFFAAAAAA);
    }

    private void renderItem(MatrixStack ms, int end, int x) {
        for (int i = scroll; i < end; i++) {
            BetItem bi = items.get(i);
            ItemStack stack = bi.stack;

            int ry = startY + (i - scroll) * itemH;
            fill(ms, xLeft + 2, ry + 2, xRight - 2, ry + itemH - 2, 0xFF2A2A2A);

            Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, x, ry + 4);

            String name = stack.getHoverName().getString();
            String qty = "x" + bi.quantity;
            font.draw(ms, name, x + 20, ry + (itemH - font.lineHeight) / 2, 0xFFFFFF);
            font.draw(ms, qty, xRight - 50, ry + (itemH - font.lineHeight) / 2, 0xFFFFFF);

            if (visibleDeleteButton) {
                int buttonX = xRight - 18;
                int buttonY = ry + (itemH - 12) / 2;
                fill(ms, buttonX, buttonY - 2, buttonX + 14, buttonY + 14, 0xFF883333);
                font.draw(ms, "X", buttonX + 4, buttonY + 2, 0xFFFFFF);
            }
        }
    }

    public boolean clicked(double mouseX, double mouseY) {
        return visibleDeleteButton && deleteButtonClicked(mouseX, mouseY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX < xLeft || mouseX > xRight) return false;
        if (mouseY < startY || mouseY > startY + itemH * visibleSlots) return false;
        if (items.size() <= visibleSlots) return false;
        scroll = clamp(scroll - (int) Math.signum(delta), 0, items.size() - visibleSlots);
        return true;
    }

    private void clampScroll() {
        if (items.size() <= visibleSlots) scroll = 0;
        else scroll = Math.max(0, Math.min(scroll, items.size() - visibleSlots));
    }

    private boolean deleteButtonClicked(double mouseX, double mouseY) {
        if (mouseX < xLeft || mouseX > xRight) return false;
        if (mouseY < startY || mouseY > startY + itemH * visibleSlots) return false;

        int idx = (int) ((mouseY - startY) / itemH) + scroll;
        if (idx >= 0 && idx < items.size()) {
            int bx = xRight - 18;
            int by = startY + (idx - scroll) * itemH + (itemH - 12) / 2;
            if (mouseX >= bx && mouseX <= bx + 12 && mouseY >= by && mouseY <= by + 12) {
                items.remove(idx);
                clampScroll();
                return true;
            }
        }
        return false;
    }
}

