package com.albertux2.bidingpiece.block;

import com.albertux2.bidingpiece.entity.BidingSeat;
import com.albertux2.bidingpiece.entity.SeatableEntity;
import com.albertux2.bidingpiece.item.AuctionPaddle;
import com.albertux2.bidingpiece.messaging.Messages;
import com.albertux2.bidingpiece.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import java.util.List;

import static com.albertux2.bidingpiece.messaging.Messager.messageToPlayer;

public class BidingChair extends Block {
    public static final DirectionProperty FACING = HorizontalBlock.FACING;

    private static final VoxelShape SHAPE_OUTLINE = Block.box(0.5, 0, 0.5, 15.5, 24, 15.5);
    private static final VoxelShape SHAPE_COLLISION = Block.box(0.5, 0, 0.5, 15.5, 24, 15.5);

    public BidingChair() {
        super(Properties.of(Material.WOOD)
            .harvestTool(ToolType.AXE)
            .strength(1.5f)
            .noOcclusion()
            .sound(SoundType.WOOL));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState s, IBlockReader w, BlockPos p, ISelectionContext c) {
        return SHAPE_OUTLINE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState s, IBlockReader w, BlockPos p, ISelectionContext c) {
        return SHAPE_COLLISION;
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos,
        PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ActionResultType pass = handleUsing(state, world, pos, player);
        if (pass != null) return pass;

        givePaddle(player);
        return ActionResultType.SUCCESS;
    }

    private ActionResultType handleUsing(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (player.isShiftKeyDown()) return ActionResultType.PASS;
        if (world.isClientSide) return ActionResultType.SUCCESS;

        if (player.inventory.getFreeSlot() < 0) {
            messageToPlayer(player, Messages.AUCTION_FREE_SLOT_REQUIRED);
            return ActionResultType.SUCCESS;
        }

        if (!handleSitting(state, world, pos, player)) {
            return ActionResultType.SUCCESS;
        }
        return null;
    }

    private void givePaddle(PlayerEntity player) {
        if (player.inventory.items.stream()
            .filter(stack -> !stack.isEmpty())
            .map(ItemStack::getItem)
            .anyMatch(item -> item instanceof AuctionPaddle)) {
            return;
        }
        player.inventory.setItem(player.inventory.getFreeSlot(), new ItemStack(ModItems.AUCTION_PADDLE.get(), 1));
    }

    private boolean handleSitting(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        List<SeatableEntity> list = world.getEntitiesOfClass(SeatableEntity.class, new AxisAlignedBB(pos).inflate(0.1D));
        SeatableEntity seat = list.isEmpty() ? null : list.get(0);

        double yOff = 0.43D;
        if (seat != null) {
            // already in use by other player
            return false;
        }

        seat = new BidingSeat(world, pos, yOff);
        world.addFreshEntity(seat);

        Direction f = state.getValue(FACING);
        seat.yRot = f.toYRot();
        seat.sit(player);
        return true;
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!world.isClientSide && state.getBlock() != newState.getBlock()) {
            List<SeatableEntity> seats = world.getEntitiesOfClass(SeatableEntity.class, new AxisAlignedBB(pos).inflate(0.25D));
            for (SeatableEntity s : seats) s.remove();
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }
}
