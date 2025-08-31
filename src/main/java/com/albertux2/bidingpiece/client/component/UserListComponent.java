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
    private final List<UUID> users;
    private final FontRenderer font;
    private int width;
    private int startY;
    private int itemHeight;
    private int selected = -1;

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
        int y = startY;
        int centerX = width / 2;

        for (int i = 0; i < users.size(); i++) {
            UUID uuid = users.get(i);
            String username = getUsernameFromUUID(uuid);

            boolean isSelected = i == selected;
            int backgroundColor = isSelected ? 0x80808080 : 0x80000000;

            fill(matrixStack, centerX - 100, y, centerX + 100, y + itemHeight, backgroundColor);

            // Render player face
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getInstance().getTextureManager().bind(STEVE_FACE);
            blit(matrixStack, centerX - 90, y + 2, 32, 32, 8, 8, 8, 8, 64, 64);

            // Render username
            font.draw(matrixStack, username, centerX - 50, y + itemHeight / 2f - 4, 0xFFFFFF);

            y += itemHeight;
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
        int relativeY = (int) (mouseY - startY);
        int index = relativeY / itemHeight;

        if (index >= 0 && index < users.size() &&
            mouseX >= width / 2f - 100 && mouseX <= width / 2f + 100) {
            selected = index;
            return true;
        }
        return false;
    }

    public UUID getSelectedUser() {
        return selected >= 0 && selected < users.size() ? users.get(selected) : null;
    }
}
