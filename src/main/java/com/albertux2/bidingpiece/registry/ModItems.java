package com.albertux2.bidingpiece.registry;

import com.albertux2.bidingpiece.item.AuctionPaddle;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static com.albertux2.bidingpiece.BidingPiece.MOD_ID;
import static com.albertux2.bidingpiece.registry.ModBlocks.CHAIR;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<Item> AUCTION_PADDLE = ITEMS.register("auction_paddle", AuctionPaddle::new);

    public static final RegistryObject<Item> BIDING_CHAIR = ITEMS.register("biding_chair",
        () -> new BlockItem(CHAIR.get(), new Item.Properties().tab(ItemGroup.TAB_DECORATIONS)));

    public static final RegistryObject<Item> AUCTION_EXHIBITOR = ITEMS.register("auction_exhibitor",
        () -> new BlockItem(ModBlocks.AUCTION_EXHIBITOR.get(), new Item.Properties().tab(ItemGroup.TAB_DECORATIONS)));
}
