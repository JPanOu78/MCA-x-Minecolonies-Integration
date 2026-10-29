package com.mcaminecolonies.client.render;

import com.mcaminecolonies.client.appearance.CitizenAppearance;
import com.mcaminecolonies.client.model.CitizenMCAModel;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.core.entity.citizen.AbstractEntityCitizen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Handles rendering MineColonies Citizens with MCA-styled humanoid models, textures, and layers.
 */
public class CitizenMCARenderer {

    private static CitizenMCAModel defaultModel;
    private static CitizenMCAModel slimModel;

    public static void init(EntityRendererProvider.Context context) {
        if (defaultModel == null) {
            defaultModel = new CitizenMCAModel(context.bakeLayer(ModelLayers.PLAYER), false);
            slimModel = new CitizenMCAModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
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
        if (defaultModel == null) {
            EntityRendererProvider.Context ctx = new EntityRendererProvider.Context(
                    Minecraft.getInstance().getEntityRenderDispatcher(),
                    Minecraft.getInstance().getItemRenderer(),
                    Minecraft.getInstance().getBlockRenderer(),
                    Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer(),
                    Minecraft.getInstance().getResourceManager(),
                    Minecraft.getInstance().getEntityModels(),
                    Minecraft.getInstance().font
            );
            init(ctx);
        }

        CitizenAppearance appearance = CitizenAppearance.getAppearance(entity);
        CitizenMCAModel model = appearance.isSlimModel() ? slimModel : defaultModel;

        poseStack.pushPose();

        // Standard living entity scale
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);

        float limbSwing = entity.walkAnimation.position(partialTicks);
        float limbSwingAmount = entity.walkAnimation.speed(partialTicks);
        float ageInTicks = entity.tickCount + partialTicks;
        float netHeadYaw = entity.getViewYRot(partialTicks) - entity.yBodyRot;
        float headPitch = entity.getViewXRot(partialTicks);

        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // 1. Render Base Human Skin
        ResourceLocation skinTexture = appearance.getBaseSkinTexture();
        VertexConsumer skinConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(skinTexture));
        model.renderToBuffer(poseStack, skinConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        // 2. Render Eyes Layer
        ResourceLocation eyesTexture = appearance.getEyesTexture();
        VertexConsumer eyesConsumer = buffer.getBuffer(RenderType.entityDecal(eyesTexture));
        model.renderToBuffer(poseStack, eyesConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

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
                if (job != null && job.getName() != null) {
                    jobName = job.getName();
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
