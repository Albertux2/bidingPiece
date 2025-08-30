package com.albertux2.bidingpiece.messaging;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Messager {

    private static final Logger LOG = LoggerFactory.getLogger(Messager.class);

    private static final String PREFIX = "[DEBUG] [BidingPiece] ";

    private Messager() {
        // utility class
    }

    public static void messageToPlayer(PlayerEntity player, String message) {
        player.displayClientMessage(new StringTextComponent(message), true);
    }

    public static void broadcastMessage(World world, String message) {
        world.players().forEach(p -> p.sendMessage(new StringTextComponent(message), p.getUUID()));
    }

    public static void broadcastMessage(World world, ITextComponent message) {
        world.players().forEach(p -> p.sendMessage(message, p.getUUID()));
    }

    public static void debugMessage(World world, String message) {
        if (world.isClientSide) {
            ClientPlayerEntity player = Minecraft.getInstance().player;
            if (player == null) return;
            player.sendMessage(new StringTextComponent("[DEBUG] " + message), player.getUUID());
        }
    }

    public static void debugLog(String message) {
        if (!LOG.isDebugEnabled()) return;

        LOG.debug(PREFIX + message);
    }
}
