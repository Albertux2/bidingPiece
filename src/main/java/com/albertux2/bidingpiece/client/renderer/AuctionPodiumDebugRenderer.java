package com.albertux2.bidingpiece.client.renderer;

import com.albertux2.bidingpiece.block.blockentity.AuctionPodiumTileEntity;
import com.albertux2.bidingpiece.util.DebugState;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

public class AuctionPodiumDebugRenderer {

    public static void renderDebugCube(MatrixStack matrixStack, IRenderTypeBuffer buffer,
        BlockPos min, BlockPos max, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        double camX = mc.gameRenderer.getMainCamera().getPosition().x;
        double camY = mc.gameRenderer.getMainCamera().getPosition().y;
        double camZ = mc.gameRenderer.getMainCamera().getPosition().z;

        matrixStack.pushPose();
        matrixStack.translate(min.getX() - camX, min.getY() - camY, min.getZ() - camZ);

        float sizeX = max.getX() - min.getX() + 1;
        float sizeY = max.getY() - min.getY() + 1;
        float sizeZ = max.getZ() - min.getZ() + 1;

        WorldRenderer.renderLineBox(matrixStack, buffer.getBuffer(RenderType.lines()),
            0, 0, 0,
            sizeX, sizeY, sizeZ,
            1.0f, 1.0f, 1.0f, 1.0f);

        matrixStack.popPose();
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        String playerName = mc.player != null ? mc.player.getName().getString() : "";
        if (!DebugState.isDebugEnabled(playerName)) return;
        World world = mc.level;
        if (world == null) return;

        IRenderTypeBuffer.Impl buffer = mc.renderBuffers().bufferSource();

        world.blockEntityList.stream()
            .filter(te -> te instanceof AuctionPodiumTileEntity)
            .map(te -> (AuctionPodiumTileEntity) te)
            .forEach(podium -> {
                BlockPos origin = podium.getBlockPos();
                Direction facing = world.getBlockState(origin).getValue(net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING);
                Pair<BlockPos, BlockPos> area = AuctionPodiumTileEntity.getExhibitionArea(origin, facing);
                renderDebugCube(event.getMatrixStack(), buffer, area.getLeft(), area.getRight(), event.getPartialTicks());
            });

        buffer.endBatch();
    }
}
