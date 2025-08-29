package com.albertux2.bidingpiece.auction;

import net.minecraft.item.ItemStack;

import java.util.*;

public class Auction {
    private final List<ItemStack> auctionedItems;
    private final Map<UUID, List<ItemStack>> bids = new HashMap<>();
    private UUID auctioneer;
    private boolean isActive = false;

    public Auction(UUID auctioneer, List<ItemStack> auctionedItems, boolean isActive) {
        this.isActive = isActive;
        this.auctioneer = auctioneer;
        // Hacer copia profunda de los items
        this.auctionedItems = new ArrayList<>();
        for (ItemStack stack : auctionedItems) {
            this.auctionedItems.add(stack.copy());
        }
    }

    // Constructor de copia
    public Auction(Auction other) {
        this.isActive = other.isActive;
        this.auctioneer = other.auctioneer;

        // Copia profunda de los items subastados
        this.auctionedItems = new ArrayList<>();
        for (ItemStack stack : other.auctionedItems) {
            this.auctionedItems.add(stack.copy());
        }

        // Copia profunda de las ofertas
        for (Map.Entry<UUID, List<ItemStack>> entry : other.bids.entrySet()) {
            List<ItemStack> bidCopy = new ArrayList<>();
            for (ItemStack stack : entry.getValue()) {
                bidCopy.add(stack.copy());
            }
            this.bids.put(entry.getKey(), bidCopy);
        }
    }

    public List<ItemStack> getAuctionedItems() {
        return new ArrayList<>(auctionedItems);
    }

    public UUID getAuctioneer() {
        return auctioneer;
    }

    public void setAuctioneer(UUID auctioneer) {
        this.auctioneer = auctioneer;
    }

    public Map<UUID, List<ItemStack>> getBids() {
        return bids;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
