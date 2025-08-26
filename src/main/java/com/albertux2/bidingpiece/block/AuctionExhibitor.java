package com.albertux2.bidingpiece.block;

import com.albertux2.bidingpiece.block.blockentity.AuctionExhibitorTileEntity;
import com.albertux2.bidingpiece.messaging.Messager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Objects;

public class AuctionExhibitor extends Block {

    private static final VoxelShape SHAPE = VoxelShapes.box(0.0D, 0.0D, 0.0D, 1.0D, 1.5D, 1.0D);

    public AuctionExhibitor() {
        super(Block.Properties
            .of(Material.PISTON)
            .strength(2.0f)
            .noOcclusion());
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

    @Override
    public boolean canBeReplaced(BlockState state, BlockItemUseContext context) {
        return false;
    }


    @Override
    public boolean useShapeForLightOcclusion(BlockState p_220074_1_) {
        return true;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new AuctionExhibitorTileEntity();
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos,
        PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isClientSide) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof AuctionExhibitorTileEntity) {
                AuctionExhibitorTileEntity exhibitor = (AuctionExhibitorTileEntity) te;
                ActionResultType result = handleExhibitorItem(player, exhibitor, player.getItemInHand(hand));
                if (result != null) return result;
            }
        }
        return ActionResultType.CONSUME;
    }


    private static ActionResultType handleExhibitorItem(PlayerEntity player, AuctionExhibitorTileEntity exhibitor,
        ItemStack held) {
        if (!held.isEmpty() && canAddItem(exhibitor, held)) {
            return addItem(player, exhibitor, held);
        } else if (!exhibitor.getDisplayedItem().isEmpty() && held.isEmpty()) {
            return removeItem(player, exhibitor);
        }
        return null;
    }

    private static boolean canAddItem(AuctionExhibitorTileEntity exhibitor, ItemStack held) {
        ItemStack displayed = exhibitor.getDisplayedItem();
        return displayed.isEmpty()
            || (held.getDescriptionId().equals(displayed.getDescriptionId())
            && displayed.getMaxStackSize() > 1
            && displayed.getCount() < displayed.getMaxStackSize());
    }

    private static ActionResultType removeItem(PlayerEntity player, AuctionExhibitorTileEntity exhibitor) {
        player.addItem(exhibitor.getDisplayedItem());
        exhibitor.setDisplayedItem(ItemStack.EMPTY);
        return ActionResultType.SUCCESS;
    }

    private static ActionResultType addItem(PlayerEntity player, AuctionExhibitorTileEntity exhibitor,
        ItemStack held) {
        ItemStack copy = held.copy();
        int amount = player.isShiftKeyDown() ? held.getCount() : 1;

        int total = amount + exhibitor.getDisplayedItem().getCount();
        if (total > held.getMaxStackSize()) {
            copy.setCount(amount);
            exhibitor.setDisplayedItem(copy);
            if (!player.isCreative()) {
                held.shrink(total - held.getMaxStackSize());
            }

            Messager.messageToPlayer(player, "Removed items from player : " + (total - held.getMaxStackSize()) + " to fit max stack size.");
            return ActionResultType.SUCCESS;
        }

        copy.setCount(total);
        exhibitor.setDisplayedItem(copy);
        if (!player.isCreative()) {
            held.shrink(amount);
        }
        Messager.messageToPlayer(player, "Current amount in exhibitor: " + total);
        return ActionResultType.SUCCESS;
    }
}
