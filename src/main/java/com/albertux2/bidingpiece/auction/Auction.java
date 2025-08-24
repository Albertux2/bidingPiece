package com.albertux2.bidingpiece.auction;

import net.minecraft.item.Item;

import java.util.*;

public class Auction {

    private final List<Item> prize = new ArrayList<>();

    private final UUID auctioneer;

    private final Map<UUID, List<Item>> bids = new HashMap<>();

    public Auction(UUID auctioneer) {
        this.auctioneer = auctioneer;
    }
}
