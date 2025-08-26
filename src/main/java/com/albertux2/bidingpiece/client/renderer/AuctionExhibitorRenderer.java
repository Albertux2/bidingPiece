package com.albertux2.bidingpiece.client.renderer;


import com.albertux2.bidingpiece.block.blockentity.AuctionExhibitorTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;

public class AuctionExhibitorRenderer extends TileEntityRenderer<AuctionExhibitorTileEntity> {

    public AuctionExhibitorRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(AuctionExhibitorTileEntity te, float partialTicks, MatrixStack ms,
        IRenderTypeBuffer buffer, int light, int overlay) {
        ItemStack stack = te.getDisplayedItem().copy();
        if (stack.isEmpty()) return;
        stack.setCount(1);

        renderItem(te, partialTicks, ms, buffer, light, overlay, stack);
        renderText(stack, te, ms, buffer, light);
    }

    private static void renderItem(AuctionExhibitorTileEntity te, float partialTicks, MatrixStack ms,
        IRenderTypeBuffer buffer, int light, int overlay, ItemStack stack) {
        ms.pushPose();
        ms.translate(0.5D, 0.85, 0.5D);

        float angle = (te.getLevel().getGameTime() % 360) + partialTicks;
        ms.mulPose(Vector3f.YP.rotationDegrees(angle * 40 / 20f));

        Minecraft.getInstance().getItemRenderer().renderStatic(
            stack,
            net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.GROUND,
            light,
            overlay,
            ms,
            buffer
        );

        ms.popPose();
    }

    private void renderText(ItemStack stack, AuctionExhibitorTileEntity te, MatrixStack ms,
        IRenderTypeBuffer buffer, int light) {
        String text = stack.getHoverName().getString() + " x" + te.getDisplayedItem().getCount();

        ms.pushPose();
        ms.translate(0.5D, 2.2D, 0.5D);
        ms.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        ms.scale(-0.025F, -0.025F, 0.025F);

        Minecraft.getInstance().font.drawInBatch(
            text,
            -Minecraft.getInstance().font.width(text) / 2f,
            0,
            0xFFFFFF,
            false,
            ms.last().pose(),
            buffer,
            false,
            0,
            light
        );

        ms.popPose();
    }

}
