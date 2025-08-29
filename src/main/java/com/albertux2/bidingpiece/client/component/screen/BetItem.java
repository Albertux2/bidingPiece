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
}

