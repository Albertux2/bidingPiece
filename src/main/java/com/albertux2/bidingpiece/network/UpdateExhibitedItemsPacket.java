package com.albertux2.bidingpiece.network;

import com.albertux2.bidingpiece.client.component.screen.AuctionScreen;
import com.albertux2.bidingpiece.container.AuctionContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class UpdateExhibitedItemsPacket {
    private final List<ItemStack> items;

    public UpdateExhibitedItemsPacket(List<ItemStack> items) {
        this.items = items;
    }

    public UpdateExhibitedItemsPacket() {
        this.items = new ArrayList<>();
    }

    public static void encode(UpdateExhibitedItemsPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.items.size());
        for (ItemStack stack : msg.items) {
            buf.writeItemStack(stack, false);
        }
    }

    public static UpdateExhibitedItemsPacket decode(PacketBuffer buf) {
        int size = buf.readInt();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            items.add(buf.readItem());
        }
        return new UpdateExhibitedItemsPacket(items);
    }

    public static void handle(UpdateExhibitedItemsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                // Estamos en el cliente, actualizar la GUI
                Minecraft mc = Minecraft.getInstance();
                if (mc.screen instanceof AuctionScreen) {
                    ((AuctionScreen) mc.screen).updateExhibitedItems(msg.items);
                }
            } else {
                // Estamos en el servidor, enviar items al cliente
                PlayerEntity player = ctx.get().getSender();
                if (player != null && player.containerMenu instanceof AuctionContainer) {
                    AuctionContainer container = (AuctionContainer) player.containerMenu;
                    List<ItemStack> items = container.getExhibitedItems();
                    NetworkHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                        new UpdateExhibitedItemsPacket(items)
                    );
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
