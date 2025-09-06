package com.albertux2.bidingpiece.client.renderer;

import com.albertux2.bidingpiece.block.blockentity.AuctionExhibitorTileEntity;
import com.albertux2.bidingpiece.client.BidingPieceClient;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SodiumCompatibleRenderer {

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!BidingPieceClient.isSodiumRendererEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getEntityRenderDispatcher() == null) return;

        MatrixStack matrixStack = event.getMatrixStack();

        double camX = mc.gameRenderer.getMainCamera().getPosition().x;
        double camY = mc.gameRenderer.getMainCamera().getPosition().y;
        double camZ = mc.gameRenderer.getMainCamera().getPosition().z;

        World world = mc.level;
        int renderDistance = 32;

        int playerChunkX = (int) mc.player.getX() >> 4;
        int playerChunkZ = (int) mc.player.getZ() >> 4;

        for (int chunkX = playerChunkX - 2; chunkX <= playerChunkX + 2; chunkX++) {
            for (int chunkZ = playerChunkZ - 2; chunkZ <= playerChunkZ + 2; chunkZ++) {
                for (int x = (chunkX << 4); x < (chunkX << 4) + 16; x++) {
                    for (int z = (chunkZ << 4); z < (chunkZ << 4) + 16; z++) {
                        for (int y = 0; y < world.getHeight(); y++) {
                            BlockPos pos = new BlockPos(x, y, z);

                            double dx = pos.getX() + 0.5 - camX;
                            double dy = pos.getY() + 0.5 - camY;
                            double dz = pos.getZ() + 0.5 - camZ;
                            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                            if (distance > renderDistance || !world.isLoaded(pos)) continue;

                            TileEntity te = world.getBlockEntity(pos);
                            if (te instanceof AuctionExhibitorTileEntity) {
                                renderAuctionExhibitor((AuctionExhibitorTileEntity) te, matrixStack, camX, camY, camZ);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void renderAuctionExhibitor(AuctionExhibitorTileEntity te, MatrixStack matrixStack,
        double camX, double camY, double camZ) {
        if (te == null || te.isRemoved() || te.getLevel() == null) return;

        ItemStack stack = te.getDisplayedItem().copy();
        if (stack.isEmpty()) return;
        stack.setCount(1);

        BlockPos pos = te.getBlockPos();

        matrixStack.pushPose();
        matrixStack.translate(
            pos.getX() + 0.5 - camX,
            pos.getY() + 1.0 - camY,
            pos.getZ() + 0.5 - camZ
        );

        try {
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.color4f(1f, 1f, 1f, 1f);

            renderItemFixed(te, matrixStack, stack);
            renderTextFixed(stack, te, matrixStack);
        } catch (Exception ignored) {
        }finally {
            RenderSystem.disableBlend();
        }

        matrixStack.popPose();
    }

    private static void renderItemFixed(AuctionExhibitorTileEntity te, MatrixStack ms, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getItemRenderer() == null) return;

        ms.pushPose();
        ms.translate(0.0D, -0.2D, 0.0D);
        ms.scale(1, 1, 1);

        // Rotación suave
        long gameTime = te.getLevel().getGameTime();
        float angle = (gameTime + mc.getFrameTime()) * 2.0f;
        ms.mulPose(Vector3f.YP.rotationDegrees(angle));

        try {
            IRenderTypeBuffer.Impl buffer = mc.renderBuffers().bufferSource();
            int light = WorldRenderer.getLightColor(te.getLevel(), te.getBlockPos().above());
            int overlay = OverlayTexture.NO_OVERLAY;

            mc.getItemRenderer().renderStatic(
                stack,
                ItemCameraTransforms.TransformType.GROUND,
                light,
                overlay,
                ms,
                buffer
            );

            buffer.endBatch();
        } catch (Exception ignored) {
        }

        ms.popPose();
    }

    private static void renderTextFixed(ItemStack stack, AuctionExhibitorTileEntity te, MatrixStack ms) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.font == null || mc.getEntityRenderDispatcher() == null) return;

        String text = stack.getHoverName().getString() + " x" + te.getDisplayedItem().getCount();

        ms.pushPose();
        ms.translate(0.0D, 0.6D, 0.0D);
        ms.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        float scale = 0.02F;
        ms.scale(-scale, -scale, scale);

        IRenderTypeBuffer.Impl buffer = mc.renderBuffers().bufferSource();
        RenderSystem.enableDepthTest();

        int color = 0xFFFFFF;
        float x = -mc.font.width(text) / 2f;
        float y = 0f;

        mc.font.drawInBatch(
            text,
            x,
            y,
            color,
            false,
            ms.last().pose(),
            buffer,
            false,
            0,
            0x00F000F0
        );

        buffer.endBatch();

        ms.popPose();
    }
}