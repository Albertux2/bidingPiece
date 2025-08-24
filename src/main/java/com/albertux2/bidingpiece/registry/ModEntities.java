package com.albertux2.bidingpiece.registry;

import com.albertux2.bidingpiece.BidingPiece;
import com.albertux2.bidingpiece.entity.SeatableEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, BidingPiece.MOD_ID);

    public static final RegistryObject<EntityType<SeatableEntity>> SEAT = ENTITIES.register("seat",
        () -> EntityType.Builder.<SeatableEntity>of(SeatableEntity::new, EntityClassification.MISC)
            .sized(0.001F, 0.001F)
            .noSave()
            .noSummon()
            .build(new ResourceLocation(BidingPiece.MOD_ID, "seat").toString()));
}
