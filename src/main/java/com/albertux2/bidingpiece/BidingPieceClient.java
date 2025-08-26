package com.albertux2.bidingpiece;

import com.albertux2.bidingpiece.registry.ModBlocks;
import com.albertux2.bidingpiece.registry.ModEntities;
import com.albertux2.bidingpiece.renderer.EmptyRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = BidingPiece.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BidingPieceClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent e) {
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.SEAT.get(), EmptyRenderer::new);
        ModBlocks.renderLayer();
    }
}

