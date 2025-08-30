package com.albertux2.bidingpiece.container;

import com.albertux2.bidingpiece.block.blockentity.AuctionPodiumTileEntity;
import com.albertux2.bidingpiece.client.component.screen.AuctionScreen;
import com.albertux2.bidingpiece.registry.ModContainers;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class AuctionContainer extends Container {
    private final PlayerEntity player;
    private final BlockPos podiumPos;
    private AuctionPodiumTileEntity tileEntity;

    public AuctionContainer(int windowId, PlayerInventory playerInventory, PacketBuffer data) {
        super(ModContainers.AUCTION_CONTAINER.get(), windowId);
        this.player = playerInventory.player;
        this.podiumPos = data.readBlockPos();

        if (!player.level.isClientSide) {
            TileEntity te = player.level.getBlockEntity(podiumPos);
            if (te instanceof AuctionPodiumTileEntity) {
                this.tileEntity = (AuctionPodiumTileEntity) te;
                // Forzar un broadcast del estado actual al abrir el container
                this.tileEntity.broadcastCurrentState();
            }
        }
    }

    public AuctionContainer(int windowId, PlayerInventory playerInventory, AuctionPodiumTileEntity tileEntity) {
        super(ModContainers.AUCTION_CONTAINER.get(), windowId);
        this.player = playerInventory.player;
        this.tileEntity = tileEntity;
        this.podiumPos = tileEntity.getBlockPos();

        if (!player.level.isClientSide) {
            // Forzar un broadcast del estado actual al abrir el container
            this.tileEntity.broadcastCurrentState();
        }
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return podiumPos != null &&
            player.level.getBlockEntity(podiumPos) instanceof AuctionPodiumTileEntity &&
            player.distanceToSqr(podiumPos.getX() + 0.5D, podiumPos.getY() + 0.5D, podiumPos.getZ() + 0.5D) <= 64.0D;
    }

    public void updateClientItems(List<ItemStack> items) {
        if (player.level.isClientSide && player.containerMenu instanceof AuctionContainer) {
            ((AuctionScreen) Minecraft.getInstance().screen).updateExhibitedItems(items);
        }
    }

    public List<ItemStack> getExhibitedItems() {
        if (!player.level.isClientSide && tileEntity != null) {
            return tileEntity.getExhibitedItems();
        }
        return new ArrayList<>();
    }

    public BlockPos getPodiumPos() {
        return podiumPos;
    }

    public AuctionPodiumTileEntity getTileEntity() {
        return tileEntity;
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }
}
