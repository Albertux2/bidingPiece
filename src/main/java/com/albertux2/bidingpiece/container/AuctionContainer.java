package com.albertux2.bidingpiece.container;

import com.albertux2.bidingpiece.block.blockentity.AuctionPodiumTileEntity;
import com.albertux2.bidingpiece.network.NetworkHandler;
import com.albertux2.bidingpiece.network.UpdateAuctionStatePacket;
import com.albertux2.bidingpiece.registry.ModContainers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class AuctionContainer extends Container {
    private final PlayerEntity player;
    private List<ItemStack> exhibitedItems;
    private AuctionPodiumTileEntity tileEntity;
    private BlockPos podiumPos;

    public AuctionContainer(int windowId, PlayerInventory playerInventory, PacketBuffer data) {
        super(ModContainers.AUCTION_CONTAINER.get(), windowId);
        this.player = playerInventory.player;
        this.exhibitedItems = new ArrayList<>();

        // Leer la posición del podio
        this.podiumPos = data.readBlockPos();

        if (!player.level.isClientSide) {
            TileEntity te = player.level.getBlockEntity(podiumPos);
            if (te instanceof AuctionPodiumTileEntity) {
                this.tileEntity = (AuctionPodiumTileEntity) te;
                this.exhibitedItems = this.tileEntity.getExhibitedItems();

                // Enviar estado actual de la subasta al cliente
                if (this.tileEntity.getCurrentAuction() != null) {
                    NetworkHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                        new UpdateAuctionStatePacket(this.tileEntity.getCurrentAuction())
                    );
                }
            }
        } else {
            // En el cliente, leer los items del buffer
            int size = data.readInt();
            for (int i = 0; i < size; i++) {
                ItemStack stack = data.readItem();
                if (!stack.isEmpty()) {
                    this.exhibitedItems.add(stack);
                }
            }
        }
    }

    public AuctionContainer(int windowId, PlayerInventory playerInventory, AuctionPodiumTileEntity tileEntity) {
        super(ModContainers.AUCTION_CONTAINER.get(), windowId);
        this.player = playerInventory.player;
        this.tileEntity = tileEntity;
        this.podiumPos = tileEntity.getBlockPos();
        this.exhibitedItems = new ArrayList<>();

        if (!player.level.isClientSide) {
            this.exhibitedItems = tileEntity.getExhibitedItems();

            // Enviar estado actual de la subasta al cliente
            if (this.tileEntity.getCurrentAuction() != null) {
                NetworkHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                    new UpdateAuctionStatePacket(this.tileEntity.getCurrentAuction())
                );
            }
        }
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        // Verificar que el jugador esté cerca del podio correcto
        if (podiumPos == null) return false;
        if (player.level.getBlockEntity(podiumPos) != tileEntity) return false;
        return player.distanceToSqr(podiumPos.getX() + 0.5D, podiumPos.getY() + 0.5D, podiumPos.getZ() + 0.5D) <= 64.0D;
    }

    public void updateClientItems(List<ItemStack> items) {
        this.exhibitedItems = new ArrayList<>(items);
    }

    public List<ItemStack> getExhibitedItems() {
        if (!player.level.isClientSide && tileEntity != null) {
            return tileEntity.getExhibitedItems();
        }
        return new ArrayList<>(exhibitedItems);
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }

    public BlockPos getPodiumPos() {
        return podiumPos;
    }
}
