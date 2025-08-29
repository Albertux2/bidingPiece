package com.albertux2.bidingpiece.block.blockentity;

import com.albertux2.bidingpiece.network.NetworkHandler;
import com.albertux2.bidingpiece.network.UpdateExhibitedItemsPacket;
import com.albertux2.bidingpiece.registry.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AuctionPodiumTileEntity extends TileEntity {
    private List<BlockPos> nearbyExhibitors = new ArrayList<>();
    private int podiumState = 0;

    public AuctionPodiumTileEntity(TileEntityType<?> type) {
        super(type);
    }

    public AuctionPodiumTileEntity() {
        super(ModTileEntities.AUCTION_PODIUM.get());
    }

    public static Pair<BlockPos, BlockPos> getExhibitionArea(BlockPos origin, Direction podiumPos) {
        int range = 20;
        int half = range / 2;
        BlockPos min, max;
        switch (podiumPos) {
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
            default:
                min = origin.offset(-half, -half, -range);
                max = origin.offset(half, half, -1);
        }
        return Pair.of(min, max);
    }

    public List<BlockPos> getNearbyExhibitors() {
        return nearbyExhibitors;
    }

    public List<BlockPos> scanForExhibitors(BlockState state) {
        nearbyExhibitors.clear();
        if (level == null) return new ArrayList<>();
        BlockPos origin = this.getBlockPos();
        Direction facing = state.getValue(net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING);
        Pair<BlockPos, BlockPos> area = getExhibitionArea(origin, facing);
        BlockPos min = area.getLeft();
        BlockPos max = area.getRight();
        BlockPos.betweenClosedStream(min, max).forEach(pos -> {
            TileEntity te = level.getBlockEntity(pos);
            if (te instanceof AuctionExhibitorTileEntity) {
                nearbyExhibitors.add(pos);
            }
        });

        return nearbyExhibitors;
    }

    public List<ItemStack> getExhibitedItems(BlockState state) {
        if (level == null) return new ArrayList<>();
        return scanForExhibitors(state)
            .stream()
            .map(level::getBlockEntity)
            .filter(e -> e instanceof AuctionExhibitorTileEntity)
            .map(e -> ((AuctionExhibitorTileEntity) e).getDisplayedItem())
            .filter(i -> Objects.nonNull(i) && !i.isEmpty())
            .collect(Collectors.toList());
    }

    public void sendItemsToClient(ServerPlayerEntity player) {
        if (level != null && !level.isClientSide) {
            List<ItemStack> items = getExhibitedItems(getBlockState());
            NetworkHandler.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new UpdateExhibitedItemsPacket(items)
            );
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            // Actualizar a todos los jugadores que tengan abierto el contenedor
            level.players().forEach(player -> {
                if (player instanceof ServerPlayerEntity && player.containerMenu instanceof com.albertux2.bidingpiece.container.AuctionContainer) {
                    sendItemsToClient((ServerPlayerEntity) player);
                }
            });
        }
    }
}
