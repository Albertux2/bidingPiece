package com.albertux2.bidingpiece.network;

import com.albertux2.bidingpiece.client.component.screen.BetItem;
import com.albertux2.bidingpiece.client.component.screen.ItemBettingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class UpdateCurrentBetsPacket {
    private final List<BetItem> items;

    public UpdateCurrentBetsPacket(List<BetItem> items) {
        this.items = items;
    }

    public static void encode(UpdateCurrentBetsPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.items.size());
        msg.items.forEach(betItem -> {
            buf.writeItem(betItem.stack);
            buf.writeInt(betItem.quantity);
        });
    }

    public static UpdateCurrentBetsPacket decode(PacketBuffer buf) {
        int size = buf.readInt();
        List<BetItem> items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ItemStack stack = buf.readItem();
            int quantity = buf.readInt();
            items.add(new BetItem(stack, quantity));
        }
        return new UpdateCurrentBetsPacket(items);
    }

    public static void handle(UpdateCurrentBetsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                if (Minecraft.getInstance().screen instanceof ItemBettingScreen) {
                    ItemBettingScreen screen = (ItemBettingScreen) Minecraft.getInstance().screen;
                    screen.loadCurrentBets(msg.items);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
