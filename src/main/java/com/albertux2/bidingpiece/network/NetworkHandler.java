package com.albertux2.bidingpiece.network;

import com.albertux2.bidingpiece.BidingPiece;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(BidingPiece.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public static void init() {
        int id = 0;
        INSTANCE.registerMessage(
            id++,
            UpdateExhibitedItemsPacket.class,
            UpdateExhibitedItemsPacket::encode,
            UpdateExhibitedItemsPacket::decode,
            UpdateExhibitedItemsPacket::handle
        );

        INSTANCE.registerMessage(
            id++,
            UpdateAuctionStatePacket.class,
            UpdateAuctionStatePacket::encode,
            UpdateAuctionStatePacket::decode,
            UpdateAuctionStatePacket::handle
        );

        INSTANCE.registerMessage(
            id++,
            RequestAuctionStatePacket.class,
            RequestAuctionStatePacket::encode,
            RequestAuctionStatePacket::decode,
            RequestAuctionStatePacket::handle
        );
    }
}
