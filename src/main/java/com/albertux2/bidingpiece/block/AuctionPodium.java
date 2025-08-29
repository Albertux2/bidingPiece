package com.albertux2.bidingpiece.block;

import com.albertux2.bidingpiece.block.blockentity.AuctionPodiumTileEntity;
import com.albertux2.bidingpiece.container.AuctionContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
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
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class AuctionPodium extends HorizontalBlock {
    private static final Logger LOGGER = LogManager.getLogger();

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
        BlockRayTraceResult hit) {
        if (!world.isClientSide) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof AuctionPodiumTileEntity && player instanceof ServerPlayerEntity) {
                AuctionPodiumTileEntity podiumTE = (AuctionPodiumTileEntity) te;
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

                // Escanear exhibidores y obtener sus items
                List<BlockPos> foundExhibitors = podiumTE.scanForExhibitors(state);

                List<ItemStack> exhibitedItems = podiumTE.getExhibitedItems();

                NetworkHooks.openGui(serverPlayer, new INamedContainerProvider() {
                    @Override
                    public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
                        AuctionContainer container = new AuctionContainer(windowId, inv, podiumTE);
                        // Asegurarnos de que el contenedor tenga los items actualizados
                        container.updateClientItems(exhibitedItems);
                        return container;
                    }

                    @Override
                    public ITextComponent getDisplayName() {
                        return new StringTextComponent("Podium Items");
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
}
