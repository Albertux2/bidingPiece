package com.albertux2.bidingpiece.block.blockentity;

import com.albertux2.bidingpiece.registry.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class AuctionExhibitorTileEntity extends TileEntity {

    private ItemStack stack = ItemStack.EMPTY;

    public AuctionExhibitorTileEntity() {
        super(ModTileEntities.AUCTION_EXHIBITOR.get());
    }

    public ItemStack getDisplayedItem() {
        return stack;
    }

    public void setDisplayedItem(ItemStack stack) {
        this.stack = stack;
        setChanged(); // marca como cambiado
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        stack = ItemStack.of(nbt.getCompound("Item"));
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        if (!stack.isEmpty()) {
            nbt.put("Item", stack.save(new CompoundNBT()));
        }
        return nbt;
    }
}
