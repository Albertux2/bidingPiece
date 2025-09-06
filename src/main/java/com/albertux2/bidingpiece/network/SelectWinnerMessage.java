package com.albertux2.bidingpiece.network;

import com.albertux2.bidingpiece.auction.Auction;
import com.albertux2.bidingpiece.block.blockentity.AuctionExhibitorTileEntity;
import com.albertux2.bidingpiece.block.blockentity.AuctionPodiumTileEntity;
import com.albertux2.bidingpiece.client.component.screen.BetItem;
import com.albertux2.bidingpiece.client.component.screen.BidsScreen;
import com.albertux2.bidingpiece.messaging.Messager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SelectWinnerMessage {
    private final BlockPos podiumPos;
    private final UUID winner;

    public SelectWinnerMessage(BlockPos podiumPos, UUID winner) {
        this.podiumPos = podiumPos;
        this.winner = winner;
    }

    public static void encode(SelectWinnerMessage msg, PacketBuffer buf) {
        buf.writeBlockPos(msg.podiumPos);
        buf.writeUUID(msg.winner);
    }

    public static SelectWinnerMessage decode(PacketBuffer buf) {
        return new SelectWinnerMessage(buf.readBlockPos(), buf.readUUID());
    }

    public static void handle(SelectWinnerMessage msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context event = ctx.get();
        if (event.getDirection().getReceptionSide().isClient()) {
            event.enqueueWork(() -> {
                if (Minecraft.getInstance().screen instanceof BidsScreen) {
                    Minecraft.getInstance().setScreen(null);
                }
            });
            event.setPacketHandled(true);
            return;
        }

        ServerWorld world = event.getSender().getLevel();
        world.getServer().execute(() -> {
            TileEntity te = world.getBlockEntity(msg.podiumPos);
            if (!(te instanceof AuctionPodiumTileEntity)) return;

            AuctionPodiumTileEntity podium = (AuctionPodiumTileEntity) te;
            ServerPlayerEntity winner = (ServerPlayerEntity) world.getPlayerByUUID(msg.winner);
            ServerPlayerEntity sender = event.getSender();

            if (winner == null || !hasAllItems(winner, podium.getCurrentAuction().getBids().get(msg.winner))) {
                sender.sendMessage(new StringTextComponent("The winner does not have all the items for the bet!")
                    .withStyle(style -> style.withColor(Color.fromRgb(0xFF4500))), sender.getUUID());
                return;
            }

            List<BetItem> winnerBids = podium.getCurrentAuction().getBids().get(msg.winner);
            for (BetItem bid : winnerBids) {
                removeItemFromInventory(winner, bid.getStack(), bid.getQuantity());
            }

            boolean someItemDropped = false;
            List<AuctionExhibitorTileEntity> nearbyExhibitors = getNearbyExhibitors(world, podium);

            for (AuctionExhibitorTileEntity exhibitor : nearbyExhibitors) {
                ItemStack displayedItem = exhibitor.getDisplayedItem();
                if (!displayedItem.isEmpty()) {
                    someItemDropped |= giveItemsToPlayer(winner, displayedItem.copy(), displayedItem.getCount());
                    exhibitor.setDisplayedItem(ItemStack.EMPTY);
                    exhibitor.setChanged();
                }
            }

            // Sincronizar todo el inventario del jugador con el cliente
            syncPlayerInventory(winner);

            if (someItemDropped) {
                winner.sendMessage(new StringTextComponent("Alert: Some items were dropped on the ground due to lack of inventory space.")
                    .withStyle(style -> style.withColor(Color.fromRgb(0xFF4500)).withBold(true)), winner.getUUID());
            }

            // Finalizar la subasta y notificar
            podium.setWinner(msg.winner);
            podium.finishAuction();
            podium.setChanged();

            NetworkHandler.INSTANCE.send(
                PacketDistributor.ALL.noArg(),
                new UpdateAuctionStatePacket(podium.getCurrentAuction()));

            sendUserWinnerNotification(event, winnerBids, podium.getCurrentAuction());

            // Forzar sincronización de los cambios
            world.markAndNotifyBlock(podium.getBlockPos(), null, world.getBlockState(podium.getBlockPos()),
                world.getBlockState(podium.getBlockPos()), 3, 512);

            for (AuctionExhibitorTileEntity exhibitor : nearbyExhibitors) {
                world.markAndNotifyBlock(exhibitor.getBlockPos(), null, world.getBlockState(exhibitor.getBlockPos()),
                    world.getBlockState(exhibitor.getBlockPos()), 3, 512);
            }

            // Enviar mensaje para cerrar la pantalla a todos los clientes
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
        });

        event.setPacketHandled(true);
    }

    private static void sendUserWinnerNotification(NetworkEvent.Context ctx, List<BetItem> items, Auction auction) {
        IFormattableTextComponent auctionedItems = items
            .stream()
            .collect(Collectors.groupingBy(i -> i.getStack().getHoverName().getString(), Collectors.summingInt(BetItem::getQuantity)))
            .entrySet()
            .stream()
            .map(item -> String.format("  - %s x%d\n", item.getKey(), item.getValue()
            ))
            .map(itemDesc -> new StringTextComponent(itemDesc)
                .withStyle(style -> style
                    .withColor(Color.fromRgb(0xFFD700))
                    .withBold(true)))
            .reduce(new StringTextComponent("\nItems from bid: \n")
                .withStyle(style -> style
                    .withColor(Color.fromRgb(0xC0C0C0))
                    .withBold(true)), IFormattableTextComponent::append, IFormattableTextComponent::append);

        Messager.broadcastMessage(ctx.getSender().level, new StringTextComponent(getUsernameByUUID(auction.getOwner(), ctx.getSender().level) + " won the auction from " + getUsernameByUUID(auction.getOwner(), ctx.getSender().level))
            .withStyle(style -> style.withBold(true))
            .append(auctionedItems));
    }

    private static String getUsernameByUUID(UUID uuid, World world) {
        return world.getPlayerByUUID(uuid) != null ? world.getPlayerByUUID(uuid).getName().getString() : "Unknown";
    }

    private static boolean hasAllItems(ServerPlayerEntity player, List<BetItem> items) {
        if (items == null) return false;

        Map<String, Integer> playerItems = new HashMap<>();
        for (ItemStack stack : player.inventory.items) {
            if (!stack.isEmpty()) {
                String itemId = stack.getItem().getRegistryName().toString();
                playerItems.merge(itemId, stack.getCount(), Integer::sum);
            }
        }

        for (BetItem bidItem : items) {
            String itemId = bidItem.getStack().getItem().getRegistryName().toString();
            Integer count = playerItems.get(itemId);
            if (count == null || count < bidItem.getQuantity()) {
                return false;
            }
        }
        return true;
    }

    private static void removeItemFromInventory(ServerPlayerEntity player, ItemStack stack, int count) {
        int remaining = count;
        String targetItemId = stack.getItem().getRegistryName().toString();

        for (int i = 0; i < player.inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack invStack = player.inventory.getItem(i);
            if (!invStack.isEmpty() && invStack.getItem().getRegistryName().toString().equals(targetItemId)) {
                int toRemove = Math.min(remaining, invStack.getCount());
                invStack.shrink(toRemove);
                remaining -= toRemove;

                if (invStack.isEmpty()) {
                    player.inventory.setItem(i, ItemStack.EMPTY);
                }
                // Sincronizar el slot específico modificado
                player.connection.send(new SSetSlotPacket(-2, i, player.inventory.getItem(i)));
            }
        }
    }

    private static boolean giveItemsToPlayer(ServerPlayerEntity player, ItemStack stack, int count) {
        if (count <= 0) return false;

        int maxStackSize = stack.getMaxStackSize();
        int remaining = count;
        boolean someItemDropped = false;

        while (remaining > 0) {
            int stackSize = Math.min(remaining, maxStackSize);
            ItemStack newStack = stack.copy();
            newStack.setCount(stackSize);

            if (!player.inventory.add(newStack)) {
                player.drop(newStack, false);
                someItemDropped = true;
            }

            remaining -= stackSize;
        }

        return someItemDropped;
    }

    /**
     * Sincroniza todo el inventario del jugador con el cliente
     */
    private static void syncPlayerInventory(ServerPlayerEntity player) {
        for (int i = 0; i < player.inventory.getContainerSize(); i++) {
            player.connection.send(new SSetSlotPacket(-2, i, player.inventory.getItem(i)));
        }

        for (int i = 0; i < 9; i++) {
            player.connection.send(new SSetSlotPacket(-2, i, player.inventory.getItem(i)));
        }

        // Sincronizar el contenedor actual si está abierto
        player.containerMenu.broadcastChanges();
    }

    private static List<AuctionExhibitorTileEntity> getNearbyExhibitors(World world, AuctionPodiumTileEntity podium) {
        List<AuctionExhibitorTileEntity> exhibitors = new ArrayList<>();
        List<BlockPos> positions = podium.getNearbyExhibitors();

        for (BlockPos pos : positions) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof AuctionExhibitorTileEntity) {
                exhibitors.add((AuctionExhibitorTileEntity) te);
            }
        }

        return exhibitors;
    }
}
