package com.albertux2.bidingpiece.network;

import com.albertux2.bidingpiece.block.blockentity.AuctionPodiumTileEntity;
import com.albertux2.bidingpiece.client.component.screen.BetItem;
import com.albertux2.bidingpiece.entity.BidingSeat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RequestCurrentBetsPacket {

    private static final int SEARCH_RANGE = 20;

    public static void encode(RequestCurrentBetsPacket msg, PacketBuffer buf) {
    }

    public static RequestCurrentBetsPacket decode(PacketBuffer buf) {
        return new RequestCurrentBetsPacket();
    }

    public static void handle(RequestCurrentBetsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) return;

            Entity vehicle = player.getVehicle();
            if (!(vehicle instanceof BidingSeat)) return;

            ServerWorld world = player.getLevel();
            BlockPos seatPos = vehicle.blockPosition();

            BlockPos.betweenClosed(
                seatPos.offset(-SEARCH_RANGE, -SEARCH_RANGE, -SEARCH_RANGE),
                seatPos.offset(SEARCH_RANGE, SEARCH_RANGE, SEARCH_RANGE)
            ).forEach(pos -> {
                TileEntity te = world.getBlockEntity(pos);
                if (te instanceof AuctionPodiumTileEntity) {
                    AuctionPodiumTileEntity podium = (AuctionPodiumTileEntity) te;
                    if (podium.isAuctionActive()) {
                        List<BetItem> currentBets = podium.getCurrentAuction().getBids()
                            .getOrDefault(player.getUUID(), new ArrayList<>());

                        NetworkHandler.INSTANCE.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new UpdateCurrentBetsPacket(currentBets)
                        );
                    }
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
