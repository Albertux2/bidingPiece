package com.albertux2.bidingpiece.network;

import com.albertux2.bidingpiece.block.blockentity.AuctionPodiumTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class RequestAuctionStatePacket {
    private final BlockPos podiumPos;

    public RequestAuctionStatePacket(BlockPos podiumPos) {
        this.podiumPos = podiumPos;
    }

    public static void encode(RequestAuctionStatePacket msg, PacketBuffer buf) {
        buf.writeBlockPos(msg.podiumPos);
    }

    public static RequestAuctionStatePacket decode(PacketBuffer buf) {
        return new RequestAuctionStatePacket(buf.readBlockPos());
    }

    public static void handle(RequestAuctionStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isServer()) {
                if (ctx.get().getSender() != null) {
                    TileEntity te = ctx.get().getSender().level.getBlockEntity(msg.podiumPos);
                    if (te instanceof AuctionPodiumTileEntity) {
                        AuctionPodiumTileEntity podium = (AuctionPodiumTileEntity) te;
                        // Enviamos el estado actual solo al jugador que lo solicitó
                        NetworkHandler.INSTANCE.send(
                            PacketDistributor.PLAYER.with(() -> ctx.get().getSender()),
                            new UpdateAuctionStatePacket(podium.getCurrentAuction())
                        );
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
