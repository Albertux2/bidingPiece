package com.albertux2.bidingpiece.network;

import com.albertux2.bidingpiece.auction.Auction;
import com.albertux2.bidingpiece.block.blockentity.AuctionPodiumTileEntity;
import com.albertux2.bidingpiece.client.component.screen.BetItem;
import com.albertux2.bidingpiece.client.component.screen.BidsScreen;
import com.albertux2.bidingpiece.messaging.Messager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SelectWinnerMessage {
    private final BlockPos podiumPos;
    private final UUID winner;

    public SelectWinnerMessage(BlockPos podiumPos, UUID winner) {
        this.podiumPos = podiumPos;
        this.winner = winner;
    }

    public static void encode(SelectWinnerMessage msg, PacketBuffer buf) {
        buf.writeBlockPos(msg.podiumPos);
        buf.writeUUID(msg.winner);
    }

    public static SelectWinnerMessage decode(PacketBuffer buf) {
        return new SelectWinnerMessage(buf.readBlockPos(), buf.readUUID());
    }

    public static void handle(SelectWinnerMessage msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context event = ctx.get();
        event.enqueueWork(() -> {
            if (event.getSender().level.isClientSide) return;
            ServerWorld world = event.getSender().getLevel();
            TileEntity te = world.getBlockEntity(msg.podiumPos);
            if (te instanceof AuctionPodiumTileEntity) {
                ServerPlayerEntity winner = (ServerPlayerEntity) world.getPlayerByUUID(msg.winner);
                if (winner == null || !hasAllItems(winner, ((AuctionPodiumTileEntity) te).getCurrentAuction().getBids().get(msg.winner))) {
                    Screen screen = net.minecraft.client.Minecraft.getInstance().screen;
                    if (screen instanceof BidsScreen) {
                        ((com.albertux2.bidingpiece.client.component.screen.BidsScreen) screen).setErrorMessage("Selected winner does not have the required items in their inventory.");
                    }
                    return;
                }
                AuctionPodiumTileEntity podium = (AuctionPodiumTileEntity) te;
                podium.setWinner(msg.winner);
                podium.finishAuction();
                NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                    new UpdateAuctionStatePacket(podium.getCurrentAuction()));
                sendUserWinnerNotification(event, podium.getCurrentAuction().getBids().get(podium.getCurrentAuction().getWinner()), podium.getCurrentAuction());
                Minecraft.getInstance().setScreen(null);
            }
        });
        event.setPacketHandled(true);
    }

    private static void sendUserWinnerNotification(NetworkEvent.Context ctx, List<BetItem> items, Auction auction) {
        IFormattableTextComponent auctionedItems = items
            .stream()
            .collect(Collectors.groupingBy(i -> i.getStack().getHoverName().getString(), Collectors.summingInt(BetItem::getQuantity)))
            .entrySet()
            .stream()
            .map(item -> String.format("  - %s x%d\n", item.getKey(), item.getValue()
            ))
            .map(itemDesc -> new StringTextComponent(itemDesc)
                .withStyle(style -> style
                    .withColor(Color.fromRgb(0xFFD700))
                    .withBold(true)))
            .reduce(new StringTextComponent("\nItems from bid: \n")
                .withStyle(style -> style
                    .withColor(Color.fromRgb(0xC0C0C0))
                    .withBold(true)), IFormattableTextComponent::append, IFormattableTextComponent::append);

        Messager.broadcastMessage(ctx.getSender().level, new StringTextComponent(getUsernameByUUID(auction.getOwner(), ctx.getSender().level) + " won the auction from " + getUsernameByUUID(auction.getOwner(), ctx.getSender().level))
            .withStyle(style -> style.withBold(true))
            .append(auctionedItems));
    }

    private static String getUsernameByUUID(UUID uuid, World world) {
        return world.getPlayerByUUID(uuid) != null ? world.getPlayerByUUID(uuid).getName().getString() : "Unknown";
    }

    private static boolean hasAllItems(ServerPlayerEntity player, List<BetItem> items) {
        final Map<String, Integer> playerItems = player.inventory.items.stream()
            .filter(stack -> !stack.isEmpty())
            .collect(Collectors.groupingBy(
                stack -> stack.getItem().getRegistryName().toString(),
                Collectors.summingInt(ItemStack::getCount)
            ));

        return items.stream().allMatch(betItem -> {
            String itemId = betItem.stack.getItem().getRegistryName().toString();
            return playerItems.containsKey(itemId) && playerItems.get(itemId) >= betItem.quantity;
        });
    }
}
