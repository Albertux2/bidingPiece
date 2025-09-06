package com.albertux2.bidingpiece.network;

import com.albertux2.bidingpiece.block.blockentity.AuctionPodiumTileEntity;
import com.albertux2.bidingpiece.client.component.screen.BetItem;
import com.albertux2.bidingpiece.entity.BidingSeat;
import com.albertux2.bidingpiece.messaging.Messager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SubmitBetPacket {
    private static final int SEARCH_RANGE = 20;
    private final List<BetItem> items;

    public SubmitBetPacket(List<BetItem> items) {
        this.items = items;
    }

    public static void encode(SubmitBetPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.items.size());
        msg.items.forEach(betItem -> {
            ItemStack copy = betItem.stack.copy();
            copy.setCount(1);
            CompoundNBT tag = copy.serializeNBT();
            buf.writeNbt(tag);
            buf.writeInt(betItem.quantity);
        });
    }

    public static SubmitBetPacket decode(PacketBuffer buf) {
        int size = buf.readInt();
        List<BetItem> items = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ItemStack stack = ItemStack.of(buf.readNbt());
            int quantity = buf.readInt();
            items.add(new BetItem(stack, quantity));
        }
        return new SubmitBetPacket(items);
    }

    public static void handle(SubmitBetPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null || !(player.getVehicle() instanceof BidingSeat)) {
                return;
            }

            ServerWorld world = player.getLevel();
            Entity vehicle = player.getVehicle();
            BlockPos seatPos = vehicle.blockPosition();

            final AtomicBoolean foundPodium = new AtomicBoolean(false);
            BlockPos.betweenClosed(
                seatPos.offset(-SEARCH_RANGE, -SEARCH_RANGE, -SEARCH_RANGE),
                seatPos.offset(SEARCH_RANGE, SEARCH_RANGE, SEARCH_RANGE)
            ).forEach(pos -> {
                if (foundPodium.get()) return;
                TileEntity te = world.getBlockEntity(pos);
                if (te instanceof AuctionPodiumTileEntity) {
                    AuctionPodiumTileEntity podium = (AuctionPodiumTileEntity) te;
                    foundPodium.set(true);
                    if (podium.isAuctionActive()) {
                        if (hasAllItems(player, msg.items)) {
                            List<BetItem> betItems = msg.items.stream()
                                .map(item -> {
                                    ItemStack newStack = item.stack.copy();
                                    return new BetItem(newStack, item.quantity);
                                })
                                .collect(Collectors.toList());

                            podium.getCurrentAuction().getBids()
                                .put(player.getUUID(), betItems);

                            podium.setChanged();

                            NetworkHandler.INSTANCE.send(
                                PacketDistributor.NEAR.with(() ->
                                    new PacketDistributor.TargetPoint(
                                        pos.getX(), pos.getY(), pos.getZ(),
                                        32.0,
                                        world.dimension()
                                    )
                                ),
                                new UpdateAuctionStatePacket(podium.getCurrentAuction())
                            );
                            betBroadcast(world, player, betItems);
                        }
                    } else {
                        Messager.messageToPlayer(player, "There's no active auction.");
                    }
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

    private static void betBroadcast(ServerWorld world, ServerPlayerEntity player, List<BetItem> items) {
        IFormattableTextComponent itemsMessage = items.stream()
            .map(item -> new StringTextComponent(String.format("  - %s x%d\n",
                item.getStack().getHoverName().getString(), item.getQuantity()))
                .withStyle(style -> style
                    .withColor(Color.fromRgb(0xFFD700))  // Dorado para los items
                    .withBold(true)))
            .reduce(new StringTextComponent("\nBet items: \n")
                .withStyle(style -> style
                    .withColor(Color.fromRgb(0xC0C0C0))  // Plateado para el encabezado
                    .withBold(true)),
                IFormattableTextComponent::append,
                IFormattableTextComponent::append);

        IFormattableTextComponent playerMessage = new StringTextComponent(player.getName().getString())
            .withStyle(style -> style
                .withColor(Color.fromRgb(0x00FF00))  // Verde para el nombre del jugador
                .withBold(true))
            .append(new StringTextComponent(" has placed a bet!")
                .withStyle(style -> style
                    .withColor(Color.fromRgb(0xFFFFFF))  // Blanco para el texto principal
                    .withBold(true)));

        Messager.broadcastMessage(world, playerMessage.append(itemsMessage));
    }

    private static boolean hasAllItems(ServerPlayerEntity player, List<BetItem> items) {
        final Map<String, Integer> playerItems = player.inventory.items.stream()
            .filter(stack -> !stack.isEmpty())
            .collect(Collectors.groupingBy(
                stack -> stack.getItem().getRegistryName().toString(),
                Collectors.summingInt(ItemStack::getCount)
            ));

        return items.stream().allMatch(betItem -> {
            String itemId = betItem.stack.getItem().getRegistryName().toString();
            return playerItems.containsKey(itemId) && playerItems.get(itemId) >= betItem.quantity;
        });
    }
}
