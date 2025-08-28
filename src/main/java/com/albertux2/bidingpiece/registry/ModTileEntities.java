package com.albertux2.bidingpiece.registry;

import com.albertux2.bidingpiece.block.blockentity.AuctionExhibitorTileEntity;
import com.albertux2.bidingpiece.block.blockentity.AuctionPodiumTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static com.albertux2.bidingpiece.BidingPiece.MOD_ID;

public class ModTileEntities {
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES =
        DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);

    public static final RegistryObject<TileEntityType<AuctionExhibitorTileEntity>> AUCTION_EXHIBITOR =
        TILE_ENTITIES.register("auction_exhibitor", () ->
            TileEntityType.Builder.of(AuctionExhibitorTileEntity::new, ModBlocks.AUCTION_EXHIBITOR.get()).build(null));

    public static final RegistryObject<TileEntityType<AuctionPodiumTileEntity>> AUCTION_PODIUM =
        TILE_ENTITIES.register("auction_podium", () ->
            TileEntityType.Builder.of(AuctionPodiumTileEntity::new, ModBlocks.AUCTION_PODIUM.get()).build(null));
}
