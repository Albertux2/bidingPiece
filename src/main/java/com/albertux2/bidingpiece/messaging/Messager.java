package com.albertux2.bidingpiece.messaging;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public final class Messager {

    private Messager() {
        // utility class
    }

    public static void messageToPlayer(PlayerEntity player, String message) {
        player.displayClientMessage(new StringTextComponent(message), true);
    }

    public static void debugMessage(World world, String message) {
        if (world.isClientSide) {
            ClientPlayerEntity player = Minecraft.getInstance().player;
            if (player == null) return;
            player.sendMessage(new StringTextComponent("[DEBUG] " + message), player.getUUID());
        }
    }
}
