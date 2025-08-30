package com.albertux2.bidingpiece.item;

import com.albertux2.bidingpiece.client.component.screen.ItemBettingScreen;
import com.albertux2.bidingpiece.entity.BidingSeat;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.stream.IntStream;

public class AuctionPaddle extends Item {
    public AuctionPaddle() {
        super(new Item.Properties().tab(ItemGroup.TAB_MISC).stacksTo(1));
    }

    public static void removeFromPlayer(PlayerEntity player) {
        int paddleIndex = IntStream.range(0, player.inventory.items.size())
            .filter(i -> {
                ItemStack itemStack = player.inventory.items.get(i);
                return !itemStack.isEmpty() && itemStack.getItem() instanceof AuctionPaddle;
            })
            .findFirst()
            .orElse(-1);

        if (paddleIndex >= 0) {
            player.inventory.setItem(paddleIndex, ItemStack.EMPTY);
        }
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (world.isClientSide) {
            openBettingGUI();
        }
        return ActionResult.sidedSuccess(player.getItemInHand(hand), world.isClientSide);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        if (context.getLevel().isClientSide) {
            openBettingGUI();
        }
        return ActionResultType.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    private void openBettingGUI() {
        Minecraft.getInstance().setScreen(new ItemBettingScreen(null));
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, PlayerEntity player) {
        item.shrink(item.getCount());
        return false;
    }

    @Override
    public void inventoryTick(ItemStack p_77663_1_, World p_77663_2_, Entity p_77663_3_, int p_77663_4_,
        boolean p_77663_5_) {
        PlayerEntity player = (PlayerEntity) p_77663_3_;
        if (player.getVehicle() == null || !(player.getVehicle() instanceof BidingSeat)) {
            removeFromPlayer(player);
        }
        super.inventoryTick(p_77663_1_, p_77663_2_, p_77663_3_, p_77663_4_, p_77663_5_);
    }
}
