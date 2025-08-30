package com.albertux2.bidingpiece.auction;

import com.albertux2.bidingpiece.client.component.screen.BetItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class Auction {
    private final List<ItemStack> auctionedItems;
    private final Map<UUID, List<BetItem>> bids;
    private final UUID auctioneer;
    private boolean isActive;

    public Auction(UUID auctioneer, List<ItemStack> items, boolean isActive, Map<UUID, List<BetItem>> bids) {
        this.auctioneer = auctioneer;
        this.isActive = isActive;
        this.auctionedItems = new ArrayList<>();
        this.bids = bids;

        // Copiar items
        for (ItemStack stack : items) {
            this.auctionedItems.add(stack.copy());
        }
    }

    public Auction(UUID auctioneer, List<ItemStack> items, boolean isActive) {
        this(auctioneer, items, isActive, new HashMap<>());
    }

    public static Auction fromNBT(CompoundNBT tag) {
        boolean active = tag.getBoolean("Active");
        UUID auctioneer = tag.getUUID("Auctioneer");

        List<ItemStack> items = new ArrayList<>();
        ListNBT itemsList = tag.getList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < itemsList.size(); i++) {
            items.add(ItemStack.of(itemsList.getCompound(i)));
        }

        Map<UUID, List<BetItem>> bids = new HashMap<>();
        CompoundNBT bidsTag = tag.getCompound("Bids");
        for (String key : bidsTag.getAllKeys()) {
            UUID playerUUID = UUID.fromString(key);
            List<BetItem> playerBids = new ArrayList<>();
            ListNBT bidList = bidsTag.getList(key, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < bidList.size(); i++) {
                CompoundNBT betTag = bidList.getCompound(i);
                ItemStack stack = ItemStack.of(betTag);
                int quantity = betTag.getInt("Quantity");
                playerBids.add(new BetItem(stack, quantity));
            }
            bids.put(playerUUID, playerBids);
        }

        return new Auction(auctioneer, items, active, bids);
    }

    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();

        tag.putUUID("Auctioneer", auctioneer);
        tag.putBoolean("Active", isActive);

        ListNBT itemsList = new ListNBT();
        for (ItemStack stack : auctionedItems) {
            CompoundNBT itemTag = new CompoundNBT();
            stack.save(itemTag);
            itemsList.add(itemTag);
        }
        tag.put("Items", itemsList);

        CompoundNBT bidsTag = new CompoundNBT();
        for (Map.Entry<UUID, List<BetItem>> entry : bids.entrySet()) {
            ListNBT bidList = new ListNBT();
            for (BetItem betItem : entry.getValue()) {
                CompoundNBT betTag = new CompoundNBT();
                betItem.stack.save(betTag);
                betTag.putInt("Quantity", betItem.quantity);
                bidList.add(betTag);
            }
            bidsTag.put(entry.getKey().toString(), bidList);
        }
        tag.put("Bids", bidsTag);

        return tag;
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

    public Map<UUID, List<BetItem>> getBids() {
        return bids;
    }
}
