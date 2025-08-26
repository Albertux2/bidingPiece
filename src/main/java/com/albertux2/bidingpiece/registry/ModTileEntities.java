package com.albertux2.bidingpiece.registry;

import com.albertux2.bidingpiece.block.blockentity.AuctionExhibitorTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.RegistryObject;

import static com.albertux2.bidingpiece.BidingPiece.MOD_ID;
import static com.albertux2.bidingpiece.registry.ModBlocks.AUCTION_EXHIBITOR;

public class ModTileEntities {
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES =
        DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);

    public static final RegistryObject<TileEntityType<AuctionExhibitorTileEntity>> AUCTION_EXHIBITOR =
        TILE_ENTITIES.register("auction_exhibitor", () ->
            TileEntityType.Builder.of(AuctionExhibitorTileEntity::new, ModBlocks.AUCTION_EXHIBITOR.get()).build(null));
}
