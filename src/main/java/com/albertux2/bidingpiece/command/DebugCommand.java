package com.albertux2.bidingpiece.command;

import com.albertux2.bidingpiece.util.DebugState;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

public class DebugCommand {
    private static final SuggestionProvider<CommandSource> TARGET_SUGGESTIONS = (context, builder) -> {
        builder.suggest("server");
        MinecraftServer server = context.getSource().getServer();
        if (server != null) {
            for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
                builder.suggest(player.getName().getString());
            }
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("bpdebug")
            .then(Commands.argument("enabled", BoolArgumentType.bool())
                .then(Commands.argument("target", StringArgumentType.string())
                    .suggests(TARGET_SUGGESTIONS)
                    .executes(ctx -> {
                        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
                        String target = StringArgumentType.getString(ctx, "target");
                        DebugState.setDebugEnabled(target, enabled);
                        ctx.getSource().sendSuccess(new StringTextComponent("Debug mode para '" + target + "': " + enabled), true);
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
        );
    }
}
