package com.albertux2.bidingpiece.network;

import com.albertux2.bidingpiece.auction.Auction;
import com.albertux2.bidingpiece.block.blockentity.AuctionPodiumTileEntity;
import com.albertux2.bidingpiece.client.component.screen.AuctionScreen;
import com.albertux2.bidingpiece.container.AuctionContainer;
import com.albertux2.bidingpiece.messaging.Messager;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UpdateAuctionStatePacket {
    private final Auction auction;

    public UpdateAuctionStatePacket(Auction auction) {
        this.auction = auction;
    }

    public static void encode(UpdateAuctionStatePacket msg, PacketBuffer buf) {
        buf.writeBoolean(msg.auction != null);
        if (msg.auction != null) {
            CompoundNBT tag = msg.auction.serializeNBT();
            buf.writeNbt(tag);
        }
    }

    public static UpdateAuctionStatePacket decode(PacketBuffer buf) {
        boolean hasAuction = buf.readBoolean();
        if (!hasAuction) {
            return new UpdateAuctionStatePacket(null);
        }
        CompoundNBT tag = buf.readNbt();
        return new UpdateAuctionStatePacket(Auction.fromNBT(tag));
    }

    public static void handle(UpdateAuctionStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                handleClient(msg);
            } else {
                handleServer(msg, ctx.get());
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleClient(UpdateAuctionStatePacket msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AuctionScreen) {
            AuctionScreen screen = (AuctionScreen) mc.screen;
            screen.updateAuctionState(msg.auction);
        }
    }

    private static void handleServer(UpdateAuctionStatePacket msg, NetworkEvent.Context ctx) {
        if (ctx.getSender() != null && ctx.getSender().containerMenu instanceof AuctionContainer) {
            AuctionContainer container = (AuctionContainer) ctx.getSender().containerMenu;
            AuctionPodiumTileEntity podium = container.getTileEntity();

            if (podium != null) {
                if (podium.getCurrentAuction() == null) {
                    sendAuctionStartedMessage(ctx, podium.getExhibitedItems());
                }
                podium.updateAuctionState(msg.auction);

                NetworkHandler.INSTANCE.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(podium.getBlockPos().getX(), podium.getBlockPos().getY(), podium.getBlockPos().getZ(), 32.0, ctx.getSender().level.dimension())), new UpdateAuctionStatePacket(podium.getCurrentAuction()));
            }
        }
    }

    private static void sendAuctionStartedMessage(NetworkEvent.Context ctx, List<ItemStack> items) {
        IFormattableTextComponent auctionedItems = items
            .stream()
            .filter(item -> !item.isEmpty())
            .collect(Collectors.groupingBy(i -> i.getHoverName().getString(), Collectors.summingInt(ItemStack::getCount)))
            .entrySet()
            .stream()
            .map(item -> String.format("  - %s x%d\n", item.getKey(), item.getValue()
            ))
            .map(itemDesc -> new StringTextComponent(itemDesc)
                .withStyle(style -> style
                    .withColor(Color.fromRgb(0xFFD700))
                    .withBold(true)))
            .reduce(new StringTextComponent("\nAuctioned items: \n")
                .withStyle(style -> style
                    .withColor(Color.fromRgb(0xC0C0C0))
                    .withBold(true)), IFormattableTextComponent::append, IFormattableTextComponent::append);

        Messager.broadcastMessage(ctx.getSender().level, new StringTextComponent("Player " + ctx.getSender().getName().getString() + " started a new auction")
            .withStyle(style -> style.withBold(true))
            .append(auctionedItems));
    }
}
