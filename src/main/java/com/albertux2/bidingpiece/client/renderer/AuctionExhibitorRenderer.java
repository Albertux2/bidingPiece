package com.albertux2.bidingpiece.client.renderer;


import com.albertux2.bidingpiece.block.blockentity.AuctionExhibitorTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;

public class AuctionExhibitorRenderer extends TileEntityRenderer<AuctionExhibitorTileEntity> {

    public AuctionExhibitorRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(AuctionExhibitorTileEntity te, float partialTicks, MatrixStack ms,
        IRenderTypeBuffer buffer, int light, int overlay) {
        ItemStack stack = te.getDisplayedItem();
        if (stack.isEmpty()) return;

        ms.pushPose();
        ms.translate(0.5D, 1.1D, 0.5D); // posición centrada sobre el bloque

        ItemEntity fake = new ItemEntity(te.getLevel(), 0, 0, 0, stack);
        fake.setNoPickUpDelay();
        fake.setItem(stack);

        Minecraft.getInstance().getEntityRenderDispatcher().render(
            fake, 0.0D, 0.0D, 0.0D,
            0.0F, partialTicks,
            ms, buffer, light
        );

        ms.popPose();
    }
}
