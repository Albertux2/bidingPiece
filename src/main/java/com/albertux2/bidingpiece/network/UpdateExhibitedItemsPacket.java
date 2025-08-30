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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class UpdateExhibitedItemsPacket {
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<ItemStack> items;
    private final boolean isRequest;

    public UpdateExhibitedItemsPacket() {
        this.items = new ArrayList<>();
        this.isRequest = true;
    }

    public UpdateExhibitedItemsPacket(List<ItemStack> items) {
        this.items = new ArrayList<>(items);
        this.isRequest = false;
    }

    public static void encode(UpdateExhibitedItemsPacket msg, PacketBuffer buf) {
        buf.writeBoolean(msg.isRequest);
        if (!msg.isRequest) {
            buf.writeInt(msg.items.size());
            for (ItemStack stack : msg.items) {
                buf.writeItemStack(stack, false);
            }
        }
    }

    public static UpdateExhibitedItemsPacket decode(PacketBuffer buf) {
        boolean isRequest = buf.readBoolean();
        if (isRequest) {
            return new UpdateExhibitedItemsPacket();
        }

        int size = buf.readInt();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            items.add(buf.readItem());
        }
        return new UpdateExhibitedItemsPacket(items);
    }

    public static void handle(UpdateExhibitedItemsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            NetworkEvent.Context context = ctx.get();
            if (context.getDirection().getReceptionSide().isClient()) {
                if (!msg.isRequest) {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.screen instanceof AuctionScreen) {
                        ((AuctionScreen) mc.screen).updateExhibitedItems(msg.items);
                    }
                }
            } else {
                if (msg.isRequest) {
                    PlayerEntity player = context.getSender();
                    if (player instanceof ServerPlayerEntity &&
                        player.containerMenu instanceof AuctionContainer) {
                        AuctionContainer container = (AuctionContainer) player.containerMenu;
                        List<ItemStack> items = container.getExhibitedItems();

                        NetworkHandler.INSTANCE.send(
                            PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                            new UpdateExhibitedItemsPacket(items)
                        );
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
