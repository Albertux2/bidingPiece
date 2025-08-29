package com.albertux2.bidingpiece.client.component.screen;

import net.minecraft.item.ItemStack;

public class BetItem {
    public final ItemStack stack;
    public int quantity;

    public BetItem(ItemStack stack, int quantity) {
        this.stack = stack;
        this.quantity = quantity;
    }

    public BetItem sumQuantity(int plus) {
        this.quantity += plus;
        return this;
    }

    public int getQuantity() {
        return quantity;
    }

    public ItemStack getStack() {
        return stack;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BetItem)) return false;
        BetItem other = (BetItem) obj;
        return ItemStack.isSame(this.stack, other.stack) && this.quantity == other.quantity;
    }
}

