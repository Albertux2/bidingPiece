package com.albertux2.bidingpiece.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class AuctionExhibitor extends Block {

    private static final VoxelShape SHAPE = VoxelShapes.box(0.0D, 0.0D, 0.0D, 1.0D, 1.5D, 1.0D);

    public AuctionExhibitor() {
        super(Block.Properties.of(Material.PISTON).strength(2.0f));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, IBlockReader world, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader world, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos,
        boolean isMoving) {
        super.neighborChanged(state, world, pos, blockIn, fromPos, isMoving);

        if (fromPos.equals(pos.above())) {
            world.destroyBlock(fromPos, true);
        }
    }
}
