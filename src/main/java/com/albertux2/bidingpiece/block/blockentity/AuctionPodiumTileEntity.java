package com.albertux2.bidingpiece.block.blockentity;

import com.albertux2.bidingpiece.auction.Auction;
import com.albertux2.bidingpiece.network.NetworkHandler;
import com.albertux2.bidingpiece.network.UpdateAuctionStatePacket;
import com.albertux2.bidingpiece.registry.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuctionPodiumTileEntity extends TileEntity {
    private List<BlockPos> nearbyExhibitors = new ArrayList<>();
    private Auction currentAuction;

    public AuctionPodiumTileEntity() {
        super(ModTileEntities.AUCTION_PODIUM.get());
    }

    public static Pair<BlockPos, BlockPos> getExhibitionArea(BlockPos origin, Direction facing) {
        int range = 20;
        int half = range / 2;

        BlockPos min, max;
        switch (facing) {
            case NORTH:
                min = origin.offset(-half, -half, 1);
                max = origin.offset(half, half, range);
                break;
            case WEST:
                min = origin.offset(-range, -half, -half);
                max = origin.offset(-1, half, half);
                break;
            case EAST:
                min = origin.offset(1, -half, -half);
                max = origin.offset(range, half, half);
                break;
            default: // SOUTH
                min = origin.offset(-half, -half, -range);
                max = origin.offset(half, half, -1);
        }
        return Pair.of(min, max);
    }

    public void broadcastCurrentState() {
        if (level != null && !level.isClientSide) {
            NetworkHandler.INSTANCE.send(
                PacketDistributor.TRACKING_CHUNK.with(() ->
                    level.getChunkAt(worldPosition)),
                new UpdateAuctionStatePacket(currentAuction)
            );
        }
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        if (tag.contains("auction")) {
            currentAuction = Auction.fromNBT(tag.getCompound("auction"));
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        tag = super.save(tag);
        if (currentAuction != null) {
            tag.put("auction", currentAuction.serializeNBT());
        }
        return tag;
    }

    public void updateAuctionState(Auction newAuction) {
        if (level != null && !level.isClientSide) {
            boolean changed = (currentAuction == null && newAuction != null) ||
                (currentAuction != null && !currentAuction.equals(newAuction));

            currentAuction = newAuction;
            if (changed) {
                setChanged();
                broadcastCurrentState(); // Aseguramos que se envía el estado actualizado
            }
        }
    }

    private void broadcastAuctionState() {
        if (level != null && !level.isClientSide) {
            NetworkHandler.INSTANCE.send(
                PacketDistributor.TRACKING_CHUNK.with(() ->
                    level.getChunkAt(worldPosition)),
                new UpdateAuctionStatePacket(currentAuction)
            );
        }
    }

    public List<ItemStack> getExhibitedItems() {
        return getNearbyExhibitors().stream()
            .map(pos -> level != null ? level.getBlockEntity(pos) : null)
            .filter(te -> te instanceof AuctionExhibitorTileEntity)
            .map(te -> ((AuctionExhibitorTileEntity) te).getDisplayedItem())
            .filter(item -> item != null && !item.isEmpty())
            .collect(Collectors.toList());
    }

    public List<BlockPos> getNearbyExhibitors() {
        return nearbyExhibitors;
    }

    public List<BlockPos> scanForExhibitors(BlockState state) {
        nearbyExhibitors.clear();
        if (level == null) return nearbyExhibitors;

        Direction facing = state.getValue(net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING);
        Pair<BlockPos, BlockPos> area = getExhibitionArea(worldPosition, facing);

        BlockPos.betweenClosedStream(area.getLeft(), area.getRight())
            .forEach(pos -> {
                TileEntity te = level.getBlockEntity(pos);
                if (te instanceof AuctionExhibitorTileEntity) {
                    nearbyExhibitors.add(pos.immutable());
                }
            });

        setChanged();
        return nearbyExhibitors;
    }

    @Override
    public void setChanged() {
        if (level != null && !level.isClientSide) {
            super.setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public Auction getCurrentAuction() {
        return currentAuction;
    }
}
