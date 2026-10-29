package com.mcaminecolonies.mixin.client;

import com.mcaminecolonies.client.render.CitizenMCARenderer;
import com.minecolonies.core.client.render.CitizenRenderer;
import com.minecolonies.core.entity.citizen.AbstractEntityCitizen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CitizenRenderer.class, remap = false)
public class CitizenRendererMixin {

    @Inject(
        method = "render(Lcom/minecolonies/core/entity/citizen/AbstractEntityCitizen;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = true
    )
    private void mcaminecolonies$overrideCitizenRender(
        AbstractEntityCitizen entity,
        float entityYaw,
        float partialTicks,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        CallbackInfo ci
    ) {
        boolean rendered = CitizenMCARenderer.renderCitizen(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        if (rendered) {
            ci.cancel();
        }
    }
}
