package com.mcaminecolonies.client.render;

import com.mcaminecolonies.client.appearance.CitizenAppearance;
import com.mcaminecolonies.client.model.CitizenMCAModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.conczin.mca.client.render.layer.FaceLayer;
import net.conczin.mca.client.resources.EyeTextureLayers;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.Traits;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Method;

/**
 * Custom FaceLayer for MineColonies citizens.
 * 1. Overrides getSkin() to guarantee a valid open-eye texture (0.png - 10.png) from CitizenAppearance.
 * 2. Overrides renderFinal() to draw eye layers in correct physical order:
 *    - Pass 1: SCLERA (Eyeball white background)
 *    - Pass 2: IRIS (Colored eye circle, derived from genetics or custom eye dye)
 *    - Pass 3: DETAILS (Pupils & highlights layered cleanly on top of the iris)
 * 3. Overrides renderModel() to apply +0.004F draw-time scaling to model.head, eliminating Z-fighting with base skin.
 * 4. Overrides isTranslucent() to false to write depth, ensuring hair bangs occlude eyes properly.
 */
public class CitizenFaceLayer extends FaceLayer<AbstractEntityCitizen, CitizenMCAModel> {

    private static final Method GET_OR_GENERATE_EYE_LAYER;

    static {
        Method m = null;
        try {
            m = FaceLayer.class.getDeclaredMethod("getOrGenerateEyeLayer", ResourceLocation.class, EyeTextureLayers.Layer.class, EyeTextureLayers.Side.class);
            m.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        GET_OR_GENERATE_EYE_LAYER = m;
    }

    public CitizenFaceLayer(RenderLayerParent<AbstractEntityCitizen, CitizenMCAModel> parent, CitizenMCAModel model, String variant) {
        super(parent, model, variant);
    }

    @Override
    protected boolean isTranslucent() {
        return false;
    }

    @Override
    public ResourceLocation getSkin(AbstractEntityCitizen entity) {
        // Guaranteed valid face texture from CitizenAppearance (mca:skins/face/normal/0.png ... 10.png or blink.png)
        return CitizenAppearance.getAppearance(entity).getFaceTexture(entity);
    }

    @Override
    public void renderFinal(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractEntityCitizen entity,
            float partialTicks,
            boolean isVisible,
            boolean isGlowing
    ) {
        int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);
        ResourceLocation faceSkin = this.getSkin(entity);

        // Blinking state check
        if (entity.isSleeping() || entity.isDeadOrDying() || faceSkin.getPath().contains("blink")) {
            ResourceLocation blinkSkin = ResourceLocation.fromNamespaceAndPath("mca", "skins/face/normal/blink.png");
            if (this.canUse(blinkSkin)) {
                this.renderModel(poseStack, buffer, packedLight, this.model, 0xFFFFFFFF, blinkSkin, overlay, isVisible, isGlowing);
                return;
            }
        }

        if (!(entity instanceof VillagerLike<?> villager) || !this.canUse(faceSkin)) {
            return;
        }

        try {
            ResourceLocation scleraLoc = resolveEyeLayer(faceSkin, EyeTextureLayers.Layer.SCLERA, EyeTextureLayers.Side.FULL);
            ResourceLocation detailsLoc = resolveEyeLayer(faceSkin, EyeTextureLayers.Layer.DETAILS, EyeTextureLayers.Side.FULL);

            // 1. PASS 1: SCLERA (Eyeball white background) - color 0xFFFFFFFF
            if (scleraLoc != null) {
                this.renderModel(poseStack, buffer, packedLight, this.model, 0xFFFFFFFF, scleraLoc, overlay, isVisible, isGlowing);
            }

            // 2. PASS 2: IRIS (Colored iris drawn directly on top of sclera)
            if (villager.getTraits() != null && villager.getTraits().hasTrait(Traits.HETEROCHROMIA)) {
                int leftColor = EyeTextureLayers.getStaticEyeColor(villager, true);
                int rightColor = EyeTextureLayers.getStaticEyeColor(villager, false);
                ResourceLocation leftIris = resolveEyeLayer(faceSkin, EyeTextureLayers.Layer.IRIS, EyeTextureLayers.Side.LEFT);
                ResourceLocation rightIris = resolveEyeLayer(faceSkin, EyeTextureLayers.Layer.IRIS, EyeTextureLayers.Side.RIGHT);
                if (leftIris != null) {
                    this.renderModel(poseStack, buffer, packedLight, this.model, leftColor, leftIris, overlay, isVisible, isGlowing);
                }
                if (rightIris != null) {
                    this.renderModel(poseStack, buffer, packedLight, this.model, rightColor, rightIris, overlay, isVisible, isGlowing);
                }
            } else {
                int eyeColor = EyeTextureLayers.getStaticEyeColor(villager, false);
                ResourceLocation irisLoc = resolveEyeLayer(faceSkin, EyeTextureLayers.Layer.IRIS, EyeTextureLayers.Side.FULL);
                if (irisLoc != null) {
                    this.renderModel(poseStack, buffer, packedLight, this.model, eyeColor, irisLoc, overlay, isVisible, isGlowing);
                }
            }

            // 3. PASS 3: DETAILS (Pupils, highlights & shadows rendered cleanly ON TOP of the iris!) - tint 0xFF808080 (-8355712)
            if (detailsLoc != null) {
                this.renderModel(poseStack, buffer, packedLight, this.model, -8355712, detailsLoc, overlay, isVisible, isGlowing);
            }
        } catch (Exception e) {
            // Fallback: render face texture directly if dynamic generation fails
            this.renderModel(poseStack, buffer, packedLight, this.model, 0xFFFFFFFF, faceSkin, overlay, isVisible, isGlowing);
        }
    }

    private ResourceLocation resolveEyeLayer(ResourceLocation texture, EyeTextureLayers.Layer layer, EyeTextureLayers.Side side) {
        if (GET_OR_GENERATE_EYE_LAYER != null) {
            try {
                return (ResourceLocation) GET_OR_GENERATE_EYE_LAYER.invoke(this, texture, layer, side);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    @Override
    protected void renderModel(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CitizenMCAModel model,
            int color,
            ResourceLocation textureLocation,
            int overlay,
            boolean isVisible,
            boolean isGlowing
    ) {
        poseStack.pushPose();
        // Scale head by 1.004F at draw time so the eye layer sits cleanly 0.004 units in front of base skin,
        // avoiding Z-fighting while maintaining precise depth testing against hair bangs and helmets.
        model.head.xScale *= 1.004F;
        model.head.yScale *= 1.004F;
        model.head.zScale *= 1.004F;

        super.renderModel(poseStack, buffer, packedLight, model, color, textureLocation, overlay, isVisible, isGlowing);

        model.head.xScale /= 1.004F;
        model.head.yScale /= 1.004F;
        model.head.zScale /= 1.004F;
        poseStack.popPose();
    }
}
