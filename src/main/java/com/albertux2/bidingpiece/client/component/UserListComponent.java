package com.albertux2.bidingpiece.client.component;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserListComponent extends AbstractGui {
    private static final ResourceLocation STEVE_FACE = new ResourceLocation("textures/entity/steve.png");
    private static final int ROW_SPACING = 10;
    private static final int COLUMN_SPACING = 10;
    private static final int SCROLLBAR_WIDTH = 6;
    private final List<UUID> users;
    private final FontRenderer font;
    private int width;
    private int startY;
    private int itemHeight;
    private int selected = -1;
    private int scrollOffset = 0;
    private int maxItemsPerRow = 3;
    private int visibleRows = 2;

    public UserListComponent(List<UUID> users, FontRenderer font) {
        this.users = new ArrayList<>(users);
        this.font = font;
    }

    public void setLayout(int width, int startY, int itemHeight) {
        this.width = width;
        this.startY = startY;
        this.itemHeight = itemHeight;
    }

    public void render(MatrixStack matrixStack) {
        // Calcula el ancho del item basado en el ancho de la pantalla
        int itemWidth = Math.min(200, (width - 100 - (maxItemsPerRow - 1) * COLUMN_SPACING) / maxItemsPerRow);
        int totalWidth = maxItemsPerRow * itemWidth + (maxItemsPerRow - 1) * COLUMN_SPACING;
        int startX = (width - totalWidth) / 2;

        // Calcula las dimensiones totales del área de scroll
        int totalRows = (int) Math.ceil((double) users.size() / maxItemsPerRow);
        int displayRows = Math.min(visibleRows, Math.max(totalRows, visibleRows));
        int contentHeight = displayRows * itemHeight + (displayRows - 1) * ROW_SPACING;

        // Renderiza los items
        for (int row = 0; row < displayRows; row++) {
            for (int col = 0; col < maxItemsPerRow; col++) {
                int index = (row + scrollOffset) * maxItemsPerRow + col;
                if (index >= users.size()) break;

                UUID uuid = users.get(index);
                String username = getUsernameFromUUID(uuid);
                boolean isSelected = index == selected;

                int x = startX + col * (itemWidth + COLUMN_SPACING);
                int y = startY + row * (itemHeight + ROW_SPACING);

                // Render background
                fill(matrixStack, x, y, x + itemWidth, y + itemHeight,
                    isSelected ? 0x80808080 : 0x80000000);

                // Render player face
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                Minecraft.getInstance().getTextureManager().bind(STEVE_FACE);
                blit(matrixStack, x + 5, y + (itemHeight - 32) / 2,
                    32, 32, 8, 8, 8, 8, 64, 64);

                // Render username con texto ajustado
                String truncatedName = font.plainSubstrByWidth(username, itemWidth - 50);
                font.draw(matrixStack, truncatedName,
                    x + 42, y + itemHeight / 2f - 4, 0xFFFFFF);
            }
        }

        // Renderiza la scrollbar si es necesaria
        int maxScroll = Math.max(0, totalRows - visibleRows);
        if (maxScroll > 0) {
            int scrollBarHeight = visibleRows * itemHeight + (visibleRows - 1) * ROW_SPACING;
            int scrollBarX = startX + totalWidth + 10;
            int scrollBarY = startY;

            // Fondo de la scrollbar
            fill(matrixStack, scrollBarX, scrollBarY,
                scrollBarX + SCROLLBAR_WIDTH, scrollBarY + scrollBarHeight,
                0x40FFFFFF);

            // Thumb de la scrollbar
            float thumbHeight = ((float) visibleRows / totalRows) * scrollBarHeight;
            float thumbY = scrollBarY + ((float) scrollOffset / maxScroll) * (scrollBarHeight - thumbHeight);
            fill(matrixStack, scrollBarX, (int) thumbY,
                scrollBarX + SCROLLBAR_WIDTH, (int) (thumbY + thumbHeight),
                0xFFFFFFFF);
        }
    }

    private String getUsernameFromUUID(UUID uuid) {
        PlayerEntity player = Minecraft.getInstance().level.getPlayerByUUID(uuid);
        if (player != null) {
            return player.getGameProfile().getName();
        }
        return uuid.toString().substring(0, 8);
    }

    public boolean clicked(double mouseX, double mouseY) {
        int itemWidth = Math.min(200, (width - 100 - (maxItemsPerRow - 1) * COLUMN_SPACING) / maxItemsPerRow);
        int totalWidth = maxItemsPerRow * itemWidth + (maxItemsPerRow - 1) * COLUMN_SPACING;
        int startX = (width - totalWidth) / 2;

        // Verifica click en scrollbar
        int maxScroll = (int) Math.ceil((double) users.size() / maxItemsPerRow) - visibleRows;
        if (maxScroll > 0) {
            int scrollBarX = startX + totalWidth + 10;
            int scrollBarHeight = visibleRows * itemHeight + (visibleRows - 1) * ROW_SPACING;
            if (mouseX >= scrollBarX && mouseX <= scrollBarX + SCROLLBAR_WIDTH &&
                mouseY >= startY && mouseY <= startY + scrollBarHeight) {
                float clickPosition = (float) (mouseY - startY) / scrollBarHeight;
                scrollOffset = Math.max(0, Math.min(maxScroll,
                    Math.round(clickPosition * maxScroll)));
                return true;
            }
        }

        // Verifica click en items
        int totalHeight = visibleRows * itemHeight + (visibleRows - 1) * ROW_SPACING;
        if (mouseY >= startY && mouseY < startY + totalHeight) {
            int row = (int) ((mouseY - startY) / (itemHeight + ROW_SPACING));
            int col = -1;

            for (int i = 0; i < maxItemsPerRow; i++) {
                int x = startX + i * (itemWidth + COLUMN_SPACING);
                if (mouseX >= x && mouseX < x + itemWidth) {
                    col = i;
                    break;
                }
            }

            if (col != -1) {
                int index = (row + scrollOffset) * maxItemsPerRow + col;
                if (index >= 0 && index < users.size()) {
                    selected = index;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int maxRows = (int) Math.ceil((double) users.size() / maxItemsPerRow);
        int maxScroll = Math.max(0, maxRows - visibleRows);

        if (maxScroll > 0) {
            scrollOffset = Math.max(0, Math.min(maxScroll,
                scrollOffset - (delta > 0 ? 1 : -1)));
            return true;
        }
        return false;
    }

    public void setVisibleRows(int rows) {
        this.visibleRows = Math.max(2, Math.min(4, rows));
    }

    public UUID getSelectedUser() {
        return selected >= 0 && selected < users.size() ? users.get(selected) : null;
    }
}
