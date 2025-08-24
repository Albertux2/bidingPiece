package com.albertux2.bidingpiece.messaging;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public final class Messager {

    private Messager() {
        // utility class
    }

    public static void messageToPlayer(PlayerEntity player, String message) {
        player.displayClientMessage(new StringTextComponent(message), true);
    }
}
