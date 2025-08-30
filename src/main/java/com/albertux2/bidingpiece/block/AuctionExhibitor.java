package com.albertux2.bidingpiece.block;

import com.albertux2.bidingpiece.block.blockentity.AuctionExhibitorTileEntity;
import com.albertux2.bidingpiece.block.blockentity.AuctionPodiumTileEntity;
import com.albertux2.bidingpiece.messaging.Messager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
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
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class AuctionExhibitor extends Block {

    private static final VoxelShape SHAPE = VoxelShapes.box(0.0D, 0.0D, 0.0D, 1.0D, 1.5D, 1.0D);

    public AuctionExhibitor() {
        super(Block.Properties
            .of(Material.METAL)
            .strength(2.0f)
            .noOcclusion());
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
        return displayed.isEmpty() || (held.getDescriptionId().equals(displayed.getDescriptionId()) && displayed.getMaxStackSize() > 1 && displayed.getCount() < displayed.getMaxStackSize());
    }

    private static ActionResultType removeItem(PlayerEntity player, AuctionExhibitorTileEntity exhibitor) {
        player.addItem(exhibitor.getDisplayedItem());
        exhibitor.setDisplayedItem(ItemStack.EMPTY);
        return ActionResultType.SUCCESS;
    }

    private static ActionResultType addItem(PlayerEntity player, AuctionExhibitorTileEntity exhibitor, ItemStack held) {
        ItemStack copy = held.copy();
        int amount = held.getCount();

        int total = amount + exhibitor.getDisplayedItem().getCount();
        if (total > held.getMaxStackSize()) {
            copy.setCount(held.getMaxStackSize());
            exhibitor.setDisplayedItem(copy);
            if (!player.isCreative()) {
                held.shrink(Math.abs(total - held.getMaxStackSize() - amount));
            }

            return ActionResultType.SUCCESS;
        }

        copy.setCount(total);
        exhibitor.setDisplayedItem(copy);
        if (!player.isCreative()) {
            held.shrink(amount);
        }
        return ActionResultType.SUCCESS;
    }

    private static void unableActionMessage(PlayerEntity player) {
        Messager.messageToPlayer(player, "No puedes interactuar con un exhibidor mientras hay una subasta activa");
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
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
        BlockRayTraceResult hit) {
        if (!world.isClientSide) {
            TileEntity te = world.getBlockEntity(pos);
            PodiumResult nearbyPodium = findNearbyPodium(world, pos);
            if (nearbyPodium != null) {
                TileEntity podiumTE = world.getBlockEntity(nearbyPodium.pos);
                if (podiumTE instanceof AuctionPodiumTileEntity) {
                    AuctionPodiumTileEntity podium = (AuctionPodiumTileEntity) podiumTE;
                    if (podium.getCurrentAuction() != null && podium.getCurrentAuction().isActive()) {
                        unableActionMessage(player);
                        return ActionResultType.CONSUME;
                    }
                }
            }
            if (te instanceof AuctionExhibitorTileEntity) {
                AuctionExhibitorTileEntity exhibitor = (AuctionExhibitorTileEntity) te;
                ActionResultType result = handleExhibitorItem(player, exhibitor, player.getItemInHand(hand));
                if (result != null) return result;
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void tick(BlockState p_225534_1_, ServerWorld p_225534_2_, BlockPos p_225534_3_, Random p_225534_4_) {
        TileEntity te = p_225534_2_.getBlockEntity(p_225534_3_);
        if (te instanceof AuctionExhibitorTileEntity) {
            AuctionExhibitorTileEntity exhibitor = (AuctionExhibitorTileEntity) te;
            if (!exhibitor.getDisplayedItem().isEmpty()) {
                Messager.messageToPlayer(p_225534_2_.getRandomPlayer().inventory.player, "Count of displayed item: " + exhibitor.getDisplayedItem().getCount());
            }
        }
        super.tick(p_225534_1_, p_225534_2_, p_225534_3_, p_225534_4_);
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (world.isClientSide) {
            super.onRemove(state, world, pos, newState, isMoving);
            return;
        }

        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof AuctionExhibitorTileEntity) {
            AuctionExhibitorTileEntity exhibitor = (AuctionExhibitorTileEntity) te;
            ItemStack displayed = exhibitor.getDisplayedItem();
            if (!displayed.isEmpty()) {
                Block.popResource(world, pos, displayed);
                exhibitor.setDisplayedItem(ItemStack.EMPTY);
            }
            world.updateNeighbourForOutputSignal(pos, this);
        }

        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public void playerWillDestroy(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClientSide) {
            PodiumResult nearbyPodium = findNearbyPodium(world, pos);
            if (nearbyPodium != null) {
                TileEntity podiumTE = world.getBlockEntity(nearbyPodium.getPos());
                if (podiumTE instanceof AuctionPodiumTileEntity) {
                    AuctionPodiumTileEntity podium = (AuctionPodiumTileEntity) podiumTE;
                    if (podium.getCurrentAuction() != null && podium.getCurrentAuction().isActive()) {
                        world.setBlock(pos, state, 3);
                        unableActionMessage(player);
                        return;
                    }
                    super.playerWillDestroy(world, pos, state, player);
                }
            }
        }
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player,
        boolean willHarvest, FluidState fluid) {
        if (!world.isClientSide) {
            PodiumResult nearbyPodium = findNearbyPodium(world, pos);
            if (nearbyPodium != null) {
                TileEntity podiumTE = world.getBlockEntity(nearbyPodium.getPos());
                if (podiumTE instanceof AuctionPodiumTileEntity) {
                    AuctionPodiumTileEntity podium = (AuctionPodiumTileEntity) podiumTE;
                    if (podium.getCurrentAuction() != null && podium.getCurrentAuction().isActive()) {
                        return false;
                    }
                }
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    private PodiumResult findNearbyPodium(World world, BlockPos exhibitorPos) {
        int range = 10;
        return BlockPos.betweenClosedStream(exhibitorPos.offset(-range, -range, -range), exhibitorPos.offset(range, range, range))
            .filter(pos -> world.getBlockState(pos).getBlock() instanceof AuctionPodium)
            .map(pos -> new PodiumResult((AuctionPodium) world.getBlockState(pos).getBlock(), pos.immutable()))
            .findFirst()
            .orElse(null);
    }

    @Override
    public float getExplosionResistance(BlockState state, net.minecraft.world.IBlockReader world, BlockPos pos,
        net.minecraft.world.Explosion explosion) {
        if (world instanceof World) {
            PodiumResult nearbyPodium = findNearbyPodium((World) world, pos);
            if (nearbyPodium != null) {
                TileEntity te = world.getBlockEntity(nearbyPodium.getPos());
                if (te instanceof AuctionPodiumTileEntity) {
                    AuctionPodiumTileEntity podium = (AuctionPodiumTileEntity) te;
                    if (podium.getCurrentAuction() != null && podium.getCurrentAuction().isActive()) {
                        return 3600000.0F;
                    }
                }
            }
        }
        return super.getExplosionResistance(state, world, pos, explosion);
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, net.minecraft.world.IBlockReader world, BlockPos pos,
        net.minecraft.world.Explosion explosion) {
        if (world instanceof World) {
            PodiumResult nearbyPodium = findNearbyPodium((World) world, pos);
            if (nearbyPodium != null) {
                TileEntity te = world.getBlockEntity(nearbyPodium.getPos());
                if (te instanceof AuctionPodiumTileEntity) {
                    AuctionPodiumTileEntity podium = (AuctionPodiumTileEntity) te;
                    return podium.getCurrentAuction() == null || !podium.getCurrentAuction().isActive();
                }
            }
        }
        return super.canDropFromExplosion(state, world, pos, explosion);
    }

    private static class PodiumResult {
        private final AuctionPodium podium;
        private final BlockPos pos;

        public PodiumResult(AuctionPodium podium, BlockPos pos) {
            this.podium = podium;
            this.pos = pos;
        }

        public AuctionPodium getPodium() {
            return podium;
        }

        public BlockPos getPos() {
            return pos;
        }
    }
}
