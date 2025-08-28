package com.albertux2.bidingpiece.block;

import com.albertux2.bidingpiece.block.blockentity.AuctionPodiumTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class AuctionPodium extends HorizontalBlock {
    public AuctionPodium() {
        super(Properties.of(net.minecraft.block.material.Material.WOOD).strength(2.0f).harvestTool(ToolType.AXE).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, net.minecraft.world.IBlockReader world) {
        return new AuctionPodiumTileEntity();
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
        BlockRayTraceResult result) {
        if (!world.isClientSide) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof AuctionPodiumTileEntity) {
                ((AuctionPodiumTileEntity) te).scanForExhibitors(state);
            }
        }
        return ActionResultType.CONSUME;
    }

    @Override
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!state.is(oldState.getBlock())) {
            if (!world.isClientSide) {
                TileEntity te = world.getBlockEntity(pos);
                if (te instanceof AuctionPodiumTileEntity) {
                    ((AuctionPodiumTileEntity) te).scanForExhibitors(state);
                }
            }
        }
        super.onPlace(state, world, pos, oldState, isMoving);
    }
}
