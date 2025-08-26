package com.albertux2.bidingpiece.registry;

import com.albertux2.bidingpiece.block.AuctionExhibitor;
import com.albertux2.bidingpiece.block.BidingChair;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static com.albertux2.bidingpiece.BidingPiece.MOD_ID;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

    public static final RegistryObject<Block> CHAIR = BLOCKS.register("biding_chair", BidingChair::new);

    public static final RegistryObject<Block> AUCTION_EXHIBITOR = BLOCKS.register("auction_exhibitor", AuctionExhibitor::new);

    public static void renderLayer() {
        RenderTypeLookup.setRenderLayer(ModBlocks.AUCTION_EXHIBITOR.get(), RenderType.solid());
    }
}
