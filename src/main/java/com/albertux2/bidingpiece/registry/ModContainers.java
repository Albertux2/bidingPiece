package com.albertux2.bidingpiece.registry;

import com.albertux2.bidingpiece.BidingPiece;
import com.albertux2.bidingpiece.container.AuctionContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModContainers {
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, BidingPiece.MOD_ID);

    public static final RegistryObject<ContainerType<AuctionContainer>> AUCTION_CONTAINER =
        CONTAINERS.register("auction",
            () -> IForgeContainerType.create(AuctionContainer::new));

    public static void register(IEventBus eventBus) {
        CONTAINERS.register(eventBus);
    }
}
