package com.albertux2.bidingpiece.entity;

import com.albertux2.bidingpiece.block.BidingChair;
import com.albertux2.bidingpiece.registry.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public abstract class SeatableEntity extends Entity {

    private BlockPos anchor;

    protected PlayerEntity player;

    public SeatableEntity(EntityType<?> type, World world) {
        super(type, world);
        this.anchor = this.blockPosition();
        this.setNoGravity(true);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public SeatableEntity(World world, BlockPos anchor, double yOffset) {
        this(ModEntities.SEAT.get(), world);
        this.anchor = anchor;
        this.setPos(anchor.getX() + 0.5D, anchor.getY() + yOffset, anchor.getZ() + 0.5D);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.0D;
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide) return;
        if (!(level.getBlockState(anchor).getBlock() instanceof BidingChair)) {
            this.remove();
        }
    }

    public void sit(PlayerEntity player) {
        player.startRiding(this, false);
        this.player = player;
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!level.isClientSide && getPassengers().isEmpty()) remove();
    }
}
