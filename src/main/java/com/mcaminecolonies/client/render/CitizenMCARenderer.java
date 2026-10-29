package com.mcaminecolonies.client.render;

import com.mcaminecolonies.client.appearance.CitizenAppearance;
import com.mcaminecolonies.client.model.CitizenMCAModel;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Handles rendering MineColonies Citizens with MCA-styled humanoid models, textures, and layers.
 */
public class CitizenMCARenderer {

    private static CitizenMCAModel defaultModel;
    private static CitizenMCAModel slimModel;

    public static synchronized void ensureModelsInitialized() {
        if (defaultModel == null) {
            EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
            if (modelSet != null) {
                defaultModel = new CitizenMCAModel(modelSet.bakeLayer(ModelLayers.PLAYER), false);
                slimModel = new CitizenMCAModel(modelSet.bakeLayer(ModelLayers.PLAYER_SLIM), true);
            }
        }
    }

    public static boolean renderCitizen(
            AbstractEntityCitizen entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        ensureModelsInitialized();
        if (defaultModel == null || slimModel == null) {
            return false;
        }

        CitizenAppearance appearance = CitizenAppearance.getAppearance(entity);
        CitizenMCAModel model = appearance.isSlimModel() ? slimModel : defaultModel;

        poseStack.pushPose();

        // 1. Orient body rotation matching Minecraft LivingEntity standard
        float bodyRot = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyRot));

        // 2. Invert Y-axis and shift down to ground level (Minecraft biped model standard)
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        // 3. Compute relative head yaw and pitch
        float headRot = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float netHeadYaw = headRot - bodyRot;
        float headPitch = Mth.rotLerp(partialTicks, entity.xRotO, entity.getXRot());

        float limbSwing = entity.walkAnimation.position(partialTicks);
        float limbSwingAmount = entity.walkAnimation.speed(partialTicks);
        float ageInTicks = entity.tickCount + partialTicks;

        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // 1. Render Base Human Skin Layer
        ResourceLocation skinTexture = appearance.getBaseSkinTexture();
        VertexConsumer skinConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(skinTexture));
        model.renderToBuffer(poseStack, skinConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        // 2. Render Face / Expression Layer
        ResourceLocation faceTexture = appearance.getFaceTexture();
        VertexConsumer faceConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(faceTexture));
        model.renderToBuffer(poseStack, faceConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        // 3. Render Hair Layer
        ResourceLocation hairTexture = appearance.getHairTexture();
        VertexConsumer hairConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(hairTexture));
        model.renderToBuffer(poseStack, hairConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        // 4. Render Job/Clothing Layer
        String jobName = "citizen";
        try {
            ICitizenData citizenData = entity.getCitizenData();
            if (citizenData != null) {
                IJob<?> job = citizenData.getJob();
                if (job != null && job.getJobRegistryEntry() != null && job.getJobRegistryEntry().getKey() != null) {
                    jobName = job.getJobRegistryEntry().getKey().getPath();
                }
            }
        } catch (Exception ignored) {
        }

        ResourceLocation clothingTexture = appearance.getClothingTexture(jobName);
        VertexConsumer clothingConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(clothingTexture));
        model.renderToBuffer(poseStack, clothingConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();
        return true;
    }
}
