package com.albertux2.bidingpiece.block.blockentity;

import com.albertux2.bidingpiece.auction.Auction;
import com.albertux2.bidingpiece.container.AuctionContainer;
import com.albertux2.bidingpiece.network.NetworkHandler;
import com.albertux2.bidingpiece.network.UpdateAuctionStatePacket;
import com.albertux2.bidingpiece.network.UpdateExhibitedItemsPacket;
import com.albertux2.bidingpiece.registry.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class AuctionPodiumTileEntity extends TileEntity {
    private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();
    private List<BlockPos> nearbyExhibitors = new ArrayList<>();
    private Auction currentAuction;

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
                nearbyExhibitors.add(pos.immutable());
            }
        });

        return nearbyExhibitors;
    }

    public List<ItemStack> getExhibitedItems() {
        if (level == null) {
            LOGGER.warn("TileEntity no tiene world!");
            return new ArrayList<>();
        }

        List<ItemStack> items = getNearbyExhibitors()
            .stream()
            .map(level::getBlockEntity)
            .filter(e -> e instanceof AuctionExhibitorTileEntity)
            .map(e -> ((AuctionExhibitorTileEntity) e).getDisplayedItem())
            .filter(i -> Objects.nonNull(i) && !i.isEmpty())
            .collect(Collectors.toList());

        return items;
    }

    public void sendItemsToClient(ServerPlayerEntity player) {
        if (level != null && !level.isClientSide) {
            List<ItemStack> items = getExhibitedItems();
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
            level.players().forEach(player -> {
                if (player instanceof ServerPlayerEntity &&
                    player.containerMenu instanceof AuctionContainer) {
                    sendItemsToClient((ServerPlayerEntity) player);
                }
            });
        }
    }

    public void toggleAuction(UUID auctioneer) {
        if (currentAuction == null || !currentAuction.isActive()) {
            // Crear una nueva subasta con una copia de los items actuales
            List<ItemStack> items = getExhibitedItems();
            currentAuction = new Auction(auctioneer, items, true);
        } else {
            currentAuction = null;
        }
        setChanged();
        if (level != null && !level.isClientSide) {
            level.getChunkAt(getBlockPos()).getLevel().players()
                .forEach(p -> NetworkHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) p),
                    new UpdateAuctionStatePacket(currentAuction)
                ));
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isAuctionActive() {
        return currentAuction != null && currentAuction.isActive();
    }

    public Auction getCurrentAuction() {
        return currentAuction;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        // Cargar exhibitors
        nearbyExhibitors.clear();
        ListNBT exhibitorList = tag.getList("Exhibitors", 10);
        for (int i = 0; i < exhibitorList.size(); i++) {
            CompoundNBT posTag = exhibitorList.getCompound(i);
            BlockPos pos = new BlockPos(
                posTag.getInt("X"),
                posTag.getInt("Y"),
                posTag.getInt("Z")
            );
            nearbyExhibitors.add(pos);
        }

        // Cargar auction
        if (tag.contains("Auction")) {
            CompoundNBT auctionTag = tag.getCompound("Auction");

            // Cargar items
            List<ItemStack> items = new ArrayList<>();
            ListNBT itemsList = auctionTag.getList("Items", 10);
            for (int i = 0; i < itemsList.size(); i++) {
                items.add(ItemStack.of(itemsList.getCompound(i)));
            }

            // Crear auction
            UUID auctioneer = auctionTag.getUUID("Auctioneer");
            boolean isActive = auctionTag.getBoolean("IsActive");

            currentAuction = new Auction(auctioneer, items, isActive);

            // Cargar ofertas
            CompoundNBT bidsTag = auctionTag.getCompound("Bids");
            for (String key : bidsTag.getAllKeys()) {
                UUID bidder = UUID.fromString(key);
                ListNBT bidItems = bidsTag.getList(key, 10);
                List<ItemStack> bidItemsList = new ArrayList<>();
                for (int i = 0; i < bidItems.size(); i++) {
                    bidItemsList.add(ItemStack.of(bidItems.getCompound(i)));
                }
                currentAuction.getBids().put(bidder, bidItemsList);
            }
        } else {
            currentAuction = null;
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        tag = super.save(tag);

        // Guardar exhibitors
        ListNBT exhibitorList = new ListNBT();
        for (BlockPos pos : nearbyExhibitors) {
            CompoundNBT posTag = new CompoundNBT();
            posTag.putInt("X", pos.getX());
            posTag.putInt("Y", pos.getY());
            posTag.putInt("Z", pos.getZ());
            exhibitorList.add(posTag);
        }
        tag.put("Exhibitors", exhibitorList);

        // Guardar auction si existe
        if (currentAuction != null) {
            CompoundNBT auctionTag = new CompoundNBT();
            auctionTag.putBoolean("IsActive", currentAuction.isActive());
            auctionTag.putUUID("Auctioneer", currentAuction.getAuctioneer());

            // Guardar items
            ListNBT itemsList = new ListNBT();
            for (ItemStack stack : currentAuction.getAuctionedItems()) {
                itemsList.add(stack.save(new CompoundNBT()));
            }
            auctionTag.put("Items", itemsList);

            // Guardar ofertas
            CompoundNBT bidsTag = new CompoundNBT();
            for (Map.Entry<UUID, List<ItemStack>> entry : currentAuction.getBids().entrySet()) {
                ListNBT bidItems = new ListNBT();
                for (ItemStack stack : entry.getValue()) {
                    bidItems.add(stack.save(new CompoundNBT()));
                }
                bidsTag.put(entry.getKey().toString(), bidItems);
            }
            auctionTag.put("Bids", bidsTag);

            tag.put("Auction", auctionTag);
        }

        return tag;
    }
}
