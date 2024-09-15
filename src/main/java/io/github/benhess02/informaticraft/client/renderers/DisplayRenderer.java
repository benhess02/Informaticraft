package io.github.benhess02.informaticraft.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.benhess02.informaticraft.entities.DisplayBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class DisplayRenderer implements BlockEntityRenderer<DisplayBlockEntity> {

    public DisplayRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DisplayBlockEntity displayBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
    }
}
