package com.albertux2.bidingpiece.network;

import com.albertux2.bidingpiece.auction.Auction;
import com.albertux2.bidingpiece.client.component.screen.AuctionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

public class UpdateAuctionStatePacket {
    private final Auction auction;

    public UpdateAuctionStatePacket(Auction auction) {
        this.auction = auction;
    }

    public static void encode(UpdateAuctionStatePacket msg, PacketBuffer buf) {
        // Escribir estado básico
        buf.writeBoolean(msg.auction.isActive());
        buf.writeUUID(msg.auction.getAuctioneer());

        // Escribir items subastados
        List<ItemStack> items = msg.auction.getAuctionedItems();
        buf.writeInt(items.size());
        items.forEach(buf::writeItem);

        // Escribir ofertas
        Map<UUID, List<ItemStack>> bids = msg.auction.getBids();
        buf.writeInt(bids.size());
        bids.forEach((uuid, bidItems) -> {
            buf.writeUUID(uuid);
            buf.writeInt(bidItems.size());
            bidItems.forEach(buf::writeItem);
        });
    }

    public static UpdateAuctionStatePacket decode(PacketBuffer buf) {
        boolean isActive = buf.readBoolean();
        UUID auctioneer = buf.readUUID();

        // Leer items subastados
        int itemCount = buf.readInt();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            items.add(buf.readItem());
        }

        // Leer ofertas
        Map<UUID, List<ItemStack>> bids = new HashMap<>();
        int bidderCount = buf.readInt();
        for (int i = 0; i < bidderCount; i++) {
            UUID bidder = buf.readUUID();
            int bidItemCount = buf.readInt();
            List<ItemStack> bidItems = new ArrayList<>();
            for (int j = 0; j < bidItemCount; j++) {
                bidItems.add(buf.readItem());
            }
            bids.put(bidder, bidItems);
        }

        Auction auction = new Auction(auctioneer, items, isActive);
        auction.getBids().putAll(bids);
        return new UpdateAuctionStatePacket(auction);
    }

    public static void handle(UpdateAuctionStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof AuctionScreen) {
                ((AuctionScreen) Minecraft.getInstance().screen).updateAuction(msg.auction);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
