package com.albertux2.bidingpiece.network;

import com.albertux2.bidingpiece.block.blockentity.AuctionPodiumTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class ToggleAuctionMessage {
    private final BlockPos podiumPos;

    public ToggleAuctionMessage(BlockPos pos) {
        this.podiumPos = pos;
    }

    public static void encode(ToggleAuctionMessage msg, PacketBuffer buf) {
        buf.writeBlockPos(msg.podiumPos);
    }

    public static ToggleAuctionMessage decode(PacketBuffer buf) {
        return new ToggleAuctionMessage(buf.readBlockPos());
    }

    public static void handle(ToggleAuctionMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null && ctx.get().getSender().level instanceof ServerWorld) {
                ServerWorld world = (ServerWorld) ctx.get().getSender().level;
                TileEntity te = world.getBlockEntity(msg.podiumPos);

                if (te instanceof AuctionPodiumTileEntity) {
                    AuctionPodiumTileEntity podium = (AuctionPodiumTileEntity) te;
                    // Usar el nuevo método con UUID
                    podium.toggleAuction(ctx.get().getSender().getUUID());

                    // Notificar a todos los clientes cercanos del nuevo estado
                    NetworkHandler.INSTANCE.send(
                        PacketDistributor.TRACKING_CHUNK.with(() ->
                            world.getChunkAt(msg.podiumPos)),
                        new UpdateAuctionStatePacket(podium.getCurrentAuction())
                    );
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
