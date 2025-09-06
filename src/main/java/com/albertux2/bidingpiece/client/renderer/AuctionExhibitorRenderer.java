package com.albertux2.bidingpiece.client.renderer;

import com.albertux2.bidingpiece.block.blockentity.AuctionExhibitorTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;

public class AuctionExhibitorRenderer extends TileEntityRenderer<AuctionExhibitorTileEntity> {

    public AuctionExhibitorRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    private static void renderItem(AuctionExhibitorTileEntity te, float partialTicks, MatrixStack ms,
        IRenderTypeBuffer buffer, int light, int overlay, ItemStack stack) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.getItemRenderer() == null) {
            return;
        }

        ms.pushPose();
        ms.translate(0.5D, 0.75, 0.5D);
        ms.scale(1.5f, 1.5f, 1.5f);

        // Rotación más suave para mejor compatibilidad
        long gameTime = te.getLevel().getGameTime();
        float angle = (gameTime % 360) + partialTicks;
        ms.mulPose(Vector3f.YP.rotationDegrees(angle * 1.5f));

        // Forzar el uso del renderizado estándar para compatibilidad con Sodium
        try {
            mc.getItemRenderer().renderStatic(
                stack,
                ItemCameraTransforms.TransformType.GROUND,
                light,
                overlay,
                ms,
                buffer
            );
        } catch (Exception e) {
            // Fallback rendering si el método principal falla
            try {
                mc.getItemRenderer().renderStatic(
                    stack,
                    ItemCameraTransforms.TransformType.FIXED,
                    light,
                    overlay,
                    ms,
                    buffer
                );
            } catch (Exception fallbackException) {
                // Último recurso - renderizado básico
            }
        }

        ms.popPose();
    }

    @Override
    public void render(AuctionExhibitorTileEntity te, float partialTicks, MatrixStack ms,
        IRenderTypeBuffer buffer, int light, int overlay) {

        // Verificación de seguridad adicional
        if (te == null || te.getLevel() == null || te.isRemoved()) {
            return;
        }

        ItemStack stack = te.getDisplayedItem().copy();
        if (stack.isEmpty()) {
            return;
        }

        stack.setCount(1);

        try {
            // Configurar estados de OpenGL para compatibilidad con Sodium
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);

            renderItem(te, partialTicks, ms, buffer, light, overlay, stack);
            renderText(stack, te, ms, buffer, light);

            // Restaurar estados
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        } catch (Exception e) {
            // Manejo de errores silencioso para evitar crashes en multiplayer
            e.printStackTrace(); // Solo para debug
        }
    }

    private void renderText(ItemStack stack, AuctionExhibitorTileEntity te, MatrixStack ms,
        IRenderTypeBuffer buffer, int light) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.font == null || mc.getEntityRenderDispatcher() == null) {
            return;
        }

        // Verificar si el buffer está disponible para texto
        if (buffer == null) {
            return;
        }

        String text = stack.getHoverName().getString() + " x" + te.getDisplayedItem().getCount();

        ms.pushPose();
        ms.translate(0.5D, 2.2D, 0.5D);
        ms.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        ms.scale(-0.025F, -0.025F, 0.025F);

        try {
            // Renderizado de texto compatible con Sodium
            mc.font.drawInBatch(
                text,
                -mc.font.width(text) / 2f,
                0,
                0xFFFFFF,
                false,
                ms.last().pose(),
                buffer,
                false,
                0,
                light
            );
        } catch (Exception e) {
            try {
                RenderSystem.pushMatrix();
                mc.font.draw(ms, text, -mc.font.width(text) / 2f, 0, 0xFFFFFF);
                RenderSystem.popMatrix();
            } catch (Exception fallbackException) {
            }
        }

        ms.popPose();
    }
}
