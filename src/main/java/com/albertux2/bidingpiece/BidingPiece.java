package com.albertux2.bidingpiece;

import com.albertux2.bidingpiece.client.BidingPieceClient;
import com.albertux2.bidingpiece.network.NetworkHandler;
import com.albertux2.bidingpiece.registry.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("biding-piece")
public class BidingPiece {

    public static final String MOD_ID = "biding-piece";

    private static final Logger LOGGER = LogManager.getLogger();

    public BidingPiece() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        // Registrar el mod en el bus de eventos
        forgeBus.register(this);

        // Registrar todos los elementos del mod
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModTileEntities.TILE_ENTITIES.register(modBus);
        ModContainers.CONTAINERS.register(modBus);

        // Inicializar la red
        NetworkHandler.init();

        // Configuración específica del cliente usando DistExecutor
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modBus.register(BidingPieceClient.class);
        });
    }
}
