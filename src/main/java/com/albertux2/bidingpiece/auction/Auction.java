package com.albertux2.bidingpiece.auction;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import java.util.*;

public class Auction {
    private final List<ItemStack> auctionedItems;
    private final Map<UUID, List<ItemStack>> bids;
    private final UUID auctioneer;
    private boolean isActive;

    public Auction(UUID auctioneer, List<ItemStack> items, boolean isActive) {
        this.auctioneer = auctioneer;
        this.isActive = isActive;
        this.auctionedItems = new ArrayList<>();
        this.bids = new HashMap<>();

        // Copiar items
        for (ItemStack stack : items) {
            this.auctionedItems.add(stack.copy());
        }
    }

    // Deserialización desde NBT
    public static Auction fromNBT(CompoundNBT tag) {
        boolean active = tag.getBoolean("Active");
        UUID auctioneer = tag.getUUID("Auctioneer");

        List<ItemStack> items = new ArrayList<>();
        ListNBT itemsList = tag.getList("Items", 10);
        for (int i = 0; i < itemsList.size(); i++) {
            items.add(ItemStack.of(itemsList.getCompound(i)));
        }

        return new Auction(auctioneer, items, active);
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public List<ItemStack> getAuctionedItems() {
        return Collections.unmodifiableList(auctionedItems);
    }

    public UUID getAuctioneer() {
        return auctioneer;
    }

    public Map<UUID, List<ItemStack>> getBids() {
        return Collections.unmodifiableMap(bids);
    }

    public void addBid(UUID player, List<ItemStack> items) {
        if (!isActive) return;
        List<ItemStack> playerBid = new ArrayList<>();
        for (ItemStack stack : items) {
            playerBid.add(stack.copy());
        }
        bids.put(player, playerBid);
    }

    // Serialización para NBT
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putBoolean("Active", isActive);
        tag.putUUID("Auctioneer", auctioneer);

        ListNBT itemsList = new ListNBT();
        for (ItemStack stack : auctionedItems) {
            CompoundNBT itemTag = new CompoundNBT();
            stack.save(itemTag);
            itemsList.add(itemTag);
        }
        tag.put("Items", itemsList);

        return tag;
    }
}
