package com.albertux2.bidingpiece.client;

import com.albertux2.bidingpiece.BidingPiece;
import com.albertux2.bidingpiece.client.component.screen.AuctionScreen;
import com.albertux2.bidingpiece.client.renderer.AuctionExhibitorRenderer;
import com.albertux2.bidingpiece.client.renderer.AuctionPodiumDebugRenderer;
import com.albertux2.bidingpiece.client.renderer.SodiumCompatibleRenderer;
import com.albertux2.bidingpiece.registry.ModBlocks;
import com.albertux2.bidingpiece.registry.ModContainers;
import com.albertux2.bidingpiece.registry.ModEntities;
import com.albertux2.bidingpiece.registry.ModTileEntities;
import com.albertux2.bidingpiece.renderer.EmptyRenderer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = BidingPiece.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BidingPieceClient {

    private static boolean sodiumRendererEnabled = false;

    static {
        MinecraftForge.EVENT_BUS.register(AuctionPodiumDebugRenderer.class);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Ejecutar en el hilo principal del cliente para evitar problemas de concurrencia
        event.enqueueWork(() -> {
            // Registrar entity renderers
            RenderingRegistry.registerEntityRenderingHandler(ModEntities.SEAT.get(), EmptyRenderer::new);

            // Verificar si Sodium está presente
            boolean hasSodium = ModList.get().isLoaded("sodium") ||
                ModList.get().isLoaded("rubidium") ||
                ModList.get().isLoaded("embeddium");

            if (hasSodium) {
                MinecraftForge.EVENT_BUS.register(SodiumCompatibleRenderer.class);
                sodiumRendererEnabled = true;
            } else {
                ClientRegistry.bindTileEntityRenderer(ModTileEntities.AUCTION_EXHIBITOR.get(), AuctionExhibitorRenderer::new);
            }

            // Registrar screen containers
            ScreenManager.register(ModContainers.AUCTION_CONTAINER.get(), AuctionScreen::new);

            // Configurar render layers
            setupRenderLayers();
        });
    }

    private static void setupRenderLayers() {
        RenderTypeLookup.setRenderLayer(ModBlocks.AUCTION_EXHIBITOR.get(), RenderType.cutout());
    }

    public static boolean isSodiumRendererEnabled() {
        return sodiumRendererEnabled;
    }
}
