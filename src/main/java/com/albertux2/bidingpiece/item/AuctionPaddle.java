package com.albertux2.bidingpiece.item;

import com.albertux2.bidingpiece.client.component.screen.BettingScreen;
import net.minecraft.client.Minecraft;
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

public class AuctionPaddle extends Item {
    public AuctionPaddle() {
        super(new Item.Properties().tab(ItemGroup.TAB_MISC).stacksTo(1));
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
        Minecraft.getInstance().setScreen(new BettingScreen());
    }


    @Override
    public boolean onDroppedByPlayer(ItemStack item, PlayerEntity player) {
        item.shrink(item.getCount());
        return false;
    }
}
