package com.albertux2.bidingpiece;

import com.albertux2.bidingpiece.network.NetworkHandler;
import com.albertux2.bidingpiece.registry.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("biding-piece")
public class BidingPiece {

    public static final String MOD_ID = "biding-piece";

    private static final Logger LOGGER = LogManager.getLogger();

    public BidingPiece() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModTileEntities.TILE_ENTITIES.register(bus);
        ModContainers.CONTAINERS.register(bus);

        NetworkHandler.init();
    }
}
