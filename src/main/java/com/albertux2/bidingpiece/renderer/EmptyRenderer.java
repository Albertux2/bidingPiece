package com.albertux2.bidingpiece.renderer;

import com.albertux2.bidingpiece.entity.SeatableEntity;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class EmptyRenderer extends EntityRenderer<SeatableEntity> {
    public EmptyRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(SeatableEntity p_110775_1_) {
        return null;
    }

    @Override
    public boolean shouldRender(SeatableEntity p_225626_1_, ClippingHelper p_225626_2_, double p_225626_3_,
        double p_225626_5_, double p_225626_7_) {
        return true;
    }
}
