package com.albertux2.bidingpiece;

import com.albertux2.bidingpiece.block.BidingChair;
import com.albertux2.bidingpiece.registry.ModEntities;
import com.albertux2.bidingpiece.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("biding-piece")
public class BidingPiece {

    public static final String MOD_ID = "biding-piece";

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

    public static final RegistryObject<Block> CHAIR = BLOCKS.register("biding_chair", BidingChair::new);

    private static final Logger LOGGER = LogManager.getLogger();

    public BidingPiece() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModEntities.ENTITIES.register(bus);

    }
}
