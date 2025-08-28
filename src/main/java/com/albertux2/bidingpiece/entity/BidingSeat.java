package com.albertux2.bidingpiece.entity;

import com.albertux2.bidingpiece.item.AuctionPaddle;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.stream.IntStream;

public class BidingSeat extends SeatableEntity {

    public BidingSeat(World world, BlockPos anchor, double yOffset) {
        super(world, anchor, yOffset);
    }

    public BidingSeat(EntityType<?> type, World world) {
        super(type, world);
    }
}
