package com.albertux2.bidingpiece.block;

import com.albertux2.bidingpiece.block.blockentity.AuctionPodiumTileEntity;
import com.albertux2.bidingpiece.container.AuctionContainer;
import com.albertux2.bidingpiece.messaging.Messager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class AuctionPodium extends HorizontalBlock {
    private static final Logger LOGGER = LogManager.getLogger();

    public AuctionPodium() {
        super(Properties.of(net.minecraft.block.material.Material.METAL)
            .strength(2.0f)
            .noOcclusion());
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
        BlockRayTraceResult hit) {
        if (!world.isClientSide) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof AuctionPodiumTileEntity && player instanceof ServerPlayerEntity) {
                AuctionPodiumTileEntity podiumTE = (AuctionPodiumTileEntity) te;
                if (podiumTE.getCurrentAuction() != null && !player.getUUID().equals(podiumTE.getCurrentAuction().getOwner())) {
                    Messager.messageToPlayer(player,
                        String.format("There is an active auction. Only the auctioneer (%s) can manage it.",
                            world.getPlayerByUUID(podiumTE.getCurrentAuction().getOwner()).getName().getString()));
                    return ActionResultType.SUCCESS;
                }
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

                podiumTE.scanForExhibitors(state);
                List<ItemStack> exhibitedItems = podiumTE.getExhibitedItems();
                if (exhibitedItems.isEmpty()) {
                    Messager.messageToPlayer(player, "There are no items to auction in the nearby exhibitors.");
                    return ActionResultType.SUCCESS;
                }

                NetworkHooks.openGui(serverPlayer, new INamedContainerProvider() {
                    @Override
                    public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
                        AuctionContainer container = new AuctionContainer(windowId, inv, podiumTE);
                        container.updateClientItems(exhibitedItems);
                        return container;
                    }

                    @Override
                    public ITextComponent getDisplayName() {
                        return new StringTextComponent("");
                    }
                }, buf -> {
                    buf.writeBlockPos(pos);
                    buf.writeInt(exhibitedItems.size());
                    for (ItemStack stack : exhibitedItems) {
                        buf.writeItemStack(stack, false);
                    }
                });
            }
        }
        return ActionResultType.sidedSuccess(world.isClientSide);
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

    @Override
    public void playerWillDestroy(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClientSide) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof AuctionPodiumTileEntity) {
                AuctionPodiumTileEntity podium = (AuctionPodiumTileEntity) te;
                if (podium.getCurrentAuction() != null && podium.getCurrentAuction().isActive()) {
                    world.setBlock(pos, state, 3);
                    Messager.messageToPlayer(player, "No puedes destruir el podium mientras hay una subasta activa");
                    return;
                }
                super.playerWillDestroy(world, pos, state, player);
            }
        }
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player,
        boolean willHarvest, FluidState fluid) {
        if (!world.isClientSide) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof AuctionPodiumTileEntity) {
                AuctionPodiumTileEntity podium = (AuctionPodiumTileEntity) te;
                if (podium.getCurrentAuction() != null && podium.getCurrentAuction().isActive()) {
                    Messager.messageToPlayer(player, "No puedes destruir un podium mientras hay una subasta activa");
                    return false;
                }
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public float getExplosionResistance(BlockState state, net.minecraft.world.IBlockReader world, BlockPos pos,
        net.minecraft.world.Explosion explosion) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof AuctionPodiumTileEntity) {
            AuctionPodiumTileEntity podium = (AuctionPodiumTileEntity) te;
            if (podium.getCurrentAuction() != null && podium.getCurrentAuction().isActive()) {
                return 3600000.0F;
            }
        }
        return super.getExplosionResistance(state, world, pos, explosion);
    }
}
