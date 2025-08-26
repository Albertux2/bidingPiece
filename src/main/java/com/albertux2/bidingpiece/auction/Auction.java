package com.albertux2.bidingpiece.auction;

import net.minecraft.item.ItemStack;

import java.util.*;

public class Auction {

    private final List<ItemStack> auctionedItems;

    private final UUID auctioneer;

    private final Map<UUID, List<ItemStack>> bids = new HashMap<>();

    public Auction(UUID auctioneer, List<ItemStack> auctionedItems) {
        this.auctioneer = auctioneer;
        this.auctionedItems = auctionedItems;
    }
}
