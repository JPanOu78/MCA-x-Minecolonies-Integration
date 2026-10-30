package com.mcaminecolonies.client.render;

import com.mcaminecolonies.client.appearance.CitizenAgeState;
import com.mcaminecolonies.client.appearance.CitizenAppearance;
import com.mcaminecolonies.client.model.CitizenMCAModel;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.client.render.CitizenArmorLayer;
import com.minecolonies.core.client.render.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.conczin.mca.Config;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.resources.HairStyleList;
import net.conczin.mca.resources.data.skin.HairStyle;
import net.conczin.mca.resources.data.skin.LayeredHair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles rendering MineColonies Citizens with MCA-styled humanoid models, textures, layers,
 * armor, held items, growth scaling, female breasts geometry, and arm swing animations.
 */
public class CitizenMCARenderer {

    private static CitizenMCAModel defaultModel;
    private static CitizenMCAModel slimModel;

    private static CitizenArmorLayer<AbstractEntityCitizen, CitizenMCAModel, HumanoidModel<AbstractEntityCitizen>> defaultArmorLayer;
    private static CitizenArmorLayer<AbstractEntityCitizen, CitizenMCAModel, HumanoidModel<AbstractEntityCitizen>> slimArmorLayer;

    private static ItemInHandLayer<AbstractEntityCitizen, CitizenMCAModel> defaultItemLayer;
    private static ItemInHandLayer<AbstractEntityCitizen, CitizenMCAModel> slimItemLayer;

    private static CitizenFaceLayer defaultFaceLayer;
    private static CitizenFaceLayer slimFaceLayer;
    private static CitizenMCAModel defaultFaceModel;
    private static CitizenMCAModel slimFaceModel;

    private static final Map<ResourceLocation, Boolean> TEXTURE_EXISTS_CACHE = new ConcurrentHashMap<>();

    public static boolean textureExists(ResourceLocation loc) {
        if (loc == null) return false;
        return TEXTURE_EXISTS_CACHE.computeIfAbsent(loc, key -> {
            try {
                return Minecraft.getInstance().getResourceManager().getResource(key).isPresent();
            } catch (Exception e) {
                return false;
            }
        });
    }

    public static synchronized void ensureModelsInitialized() {
        if (defaultModel == null) {
            EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
            ModelManager modelManager = Minecraft.getInstance().getModelManager();
            ItemInHandRenderer itemInHandRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();

            if (modelSet != null && modelManager != null && itemInHandRenderer != null) {
                // Initialize models using custom layer definitions including breasts & breastsWear
                defaultModel = new CitizenMCAModel(CitizenMCAModel.createLayerDefinition(false).bakeRoot(), false);
                slimModel = new CitizenMCAModel(CitizenMCAModel.createLayerDefinition(true).bakeRoot(), true);

                HumanoidModel<AbstractEntityCitizen> innerModel = new HumanoidModel<>(modelSet.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
                HumanoidModel<AbstractEntityCitizen> outerModel = new HumanoidModel<>(modelSet.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));

                RenderLayerParent<AbstractEntityCitizen, CitizenMCAModel> defaultParent = createParent(defaultModel);
                RenderLayerParent<AbstractEntityCitizen, CitizenMCAModel> slimParent = createParent(slimModel);

                defaultArmorLayer = new CitizenArmorLayer<>(defaultParent, innerModel, outerModel, modelManager, modelSet);
                slimArmorLayer = new CitizenArmorLayer<>(slimParent, innerModel, outerModel, modelManager, modelSet);

                defaultItemLayer = new ItemInHandLayer<>(defaultParent, itemInHandRenderer);
                slimItemLayer = new ItemInHandLayer<>(slimParent, itemInHandRenderer);

                defaultFaceModel = new CitizenMCAModel(CitizenMCAModel.createLayerDefinition(false).bakeRoot(), false);
                slimFaceModel = new CitizenMCAModel(CitizenMCAModel.createLayerDefinition(true).bakeRoot(), true);

                defaultFaceLayer = new CitizenFaceLayer(defaultParent, defaultFaceModel, "normal");
                slimFaceLayer = new CitizenFaceLayer(slimParent, slimFaceModel, "normal");
            }
        }
    }

    private static RenderLayerParent<AbstractEntityCitizen, CitizenMCAModel> createParent(CitizenMCAModel model) {
        return new RenderLayerParent<>() {
            @Override
            public CitizenMCAModel getModel() {
                return model;
            }

            @Override
            public ResourceLocation getTextureLocation(AbstractEntityCitizen entity) {
                return CitizenAppearance.getAppearance(entity).getBaseSkinTexture();
            }
        };
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
        CitizenArmorLayer<AbstractEntityCitizen, CitizenMCAModel, HumanoidModel<AbstractEntityCitizen>> armorLayer =
                appearance.isSlimModel() ? slimArmorLayer : defaultArmorLayer;
        ItemInHandLayer<AbstractEntityCitizen, CitizenMCAModel> itemLayer =
                appearance.isSlimModel() ? slimItemLayer : defaultItemLayer;
        CitizenFaceLayer faceLayer =
                appearance.isSlimModel() ? slimFaceLayer : defaultFaceLayer;
        CitizenMCAModel faceModel =
                appearance.isSlimModel() ? slimFaceModel : defaultFaceModel;
        CitizenAgeState ageState = appearance.getAgeState();

        if (faceModel != null) {
            faceModel.applyAgeProportions(ageState);
        }

        poseStack.pushPose();

        // 1. Orient body rotation matching Minecraft LivingEntity standard
        float bodyRot = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyRot));

        // 2. Invert Y-axis, scale by MCA growth stage, and shift down to ground level
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        float scale = 0.9375F * ageState.getBodyScale();
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        // 3. Compute relative head yaw and pitch
        float headRot = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float netHeadYaw = headRot - bodyRot;
        float headPitch = Mth.rotLerp(partialTicks, entity.xRotO, entity.getXRot());

        float limbSwing = entity.walkAnimation.position(partialTicks);
        float limbSwingAmount = entity.walkAnimation.speed(partialTicks);
        float ageInTicks = entity.tickCount + partialTicks;

        // Resolve Job and Guard status safely on client
        String jobName = "none";
        boolean isGuard = false;
        try {
            String synchedJob = entity.getEntityData().get(AbstractEntityCitizen.DATA_JOB);
            if (synchedJob != null && !synchedJob.isEmpty()) {
                jobName = synchedJob;
            }
            if (jobName.equals("none") || jobName.isEmpty()) {
                var view = entity.getCitizenDataView();
                if (view != null && view.getJob() != null && !view.getJob().isEmpty()) {
                    jobName = view.getJob();
                }
            }
            if (jobName.equals("none") || jobName.isEmpty()) {
                ICitizenData citizenData = entity.getCitizenData();
                if (citizenData != null) {
                    IJob<?> job = citizenData.getJob();
                    if (job != null && job.getJobRegistryEntry() != null && job.getJobRegistryEntry().getKey() != null) {
                        jobName = job.getJobRegistryEntry().getKey().getPath();
                    }
                }
            }
        } catch (Exception ignored) {
        }

        String lowerJob = jobName.toLowerCase();
        isGuard = lowerJob.contains("guard") || lowerJob.contains("knight") || lowerJob.contains("archer")
                || lowerJob.contains("ranger") || lowerJob.contains("druid") || lowerJob.contains("mage")
                || lowerJob.contains("combat") || lowerJob.contains("patrol") || lowerJob.contains("soldier")
                || lowerJob.contains("mercenary");

        // Fallback guard detection via held weapons / shields
        if (!isGuard) {
            net.minecraft.world.item.ItemStack mainItem = entity.getMainHandItem();
            net.minecraft.world.item.ItemStack offItem = entity.getOffhandItem();
            String mainItemName = mainItem.getItem().toString().toLowerCase();
            String offItemName = offItem.getItem().toString().toLowerCase();
            if (mainItem.getItem() instanceof net.minecraft.world.item.SwordItem
                    || mainItem.getItem() instanceof net.minecraft.world.item.BowItem
                    || mainItem.getItem() instanceof net.minecraft.world.item.CrossbowItem
                    || mainItem.getItem() instanceof net.minecraft.world.item.ShieldItem
                    || offItem.getItem() instanceof net.minecraft.world.item.ShieldItem
                    || mainItemName.contains("wand") || mainItemName.contains("staff") || mainItemName.contains("spell")
                    || offItemName.contains("spell") || offItemName.contains("shield")) {
                isGuard = true;
            }
        }

        // Arm swing & poses: guards ONLY swing when attacking in combat. Non-guard workers swing while working.
        float swingProgress = entity.getAttackAnim(partialTicks);
        if (!isGuard && swingProgress == 0.0F && entity.getRenderMetadata() != null && entity.getRenderMetadata().contains("working")) {
            float workSwing = (float) Math.sin((entity.tickCount + partialTicks) * 0.4F);
            if (workSwing > 0.0F) {
                swingProgress = workSwing;
            }
        }

        model.attackTime = swingProgress;
        model.crouching = entity.isCrouching();
        model.riding = entity.isPassenger();
        model.swimAmount = entity.getSwimAmount(partialTicks);
        model.rightArmPose = RenderUtils.getArmPose(entity, InteractionHand.MAIN_HAND);
        model.leftArmPose = RenderUtils.getArmPose(entity, InteractionHand.OFF_HAND);

        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        model.applyAgeProportions(ageState);

        // 1. Render Base Human Skin Layer (Body and limbs; hide outer clothing layers)
        model.setAllVisible(true);
        model.hat.visible = false;
        model.jacket.visible = false;
        model.leftSleeve.visible = false;
        model.rightSleeve.visible = false;
        model.leftPants.visible = false;
        model.rightPants.visible = false;

        ResourceLocation skinTexture = appearance.getBaseSkinTexture();
        if (textureExists(skinTexture)) {
            VertexConsumer skinConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(skinTexture));
            model.renderToBuffer(poseStack, skinConsumer, packedLight, OverlayTexture.NO_OVERLAY, appearance.getSkinColor());
        }

        // 2. Render Face / Expression Layer
        if (faceLayer != null) {
            faceLayer.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }

        // 3. Render Job/Clothing Layer (Body and limbs; hide head/hat)
        model.setAllVisible(true);
        model.head.visible = false;
        model.hat.visible = false;

        ResourceLocation clothingTexture = appearance.getClothingTexture(jobName);
        if (textureExists(clothingTexture)) {
            VertexConsumer clothingConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(clothingTexture));
            model.renderToBuffer(poseStack, clothingConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        }

        // 4. Render Hair Layer with Hair Color Tinting and Layered Hair Support
        model.setAllVisible(false);
        model.head.visible = true;
        model.hat.visible = true;
        model.head.xScale *= 1.03F;
        model.head.yScale *= 1.03F;
        model.head.zScale *= 1.03F;

        int hairColor = appearance.getHairColor();
        boolean renderedHair = false;

        if (entity instanceof VillagerLike<?> vl) {
            String styleId = vl.getHairStyleId();
            HairStyle hairStyle = null;
            if (styleId != null && !styleId.isBlank()) {
                try {
                    hairStyle = HairStyleList.getInstance().get(styleId);
                } catch (Exception ignored) {
                }
            }

            for (LayeredHair.Category category : LayeredHair.Category.RENDER_ORDER) {
                String layerStr = vl.getLayeredHair(category);
                if ((layerStr == null || layerStr.isBlank()) && hairStyle != null) {
                    layerStr = hairStyle.layer(category);
                }
                if (layerStr != null && !layerStr.isBlank()) {
                    ResourceLocation tex = resolveHairResource(layerStr);
                    if (textureExists(tex)) {
                        renderedHair = true;
                        VertexConsumer hairConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(tex));
                        model.renderToBuffer(poseStack, hairConsumer, packedLight, OverlayTexture.NO_OVERLAY, hairColor);
                    }
                }
            }
        }

        if (!renderedHair) {
            ResourceLocation classicHair = appearance.getHairTexture();
            if (textureExists(classicHair)) {
                VertexConsumer hairConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(classicHair));
                model.renderToBuffer(poseStack, hairConsumer, packedLight, OverlayTexture.NO_OVERLAY, hairColor);
            }
        }

        model.head.xScale /= 1.03F;
        model.head.yScale /= 1.03F;
        model.head.zScale /= 1.03F;

        // Reset all parts visible for armor and held items
        model.setAllVisible(true);

        // 5. Render Citizen Armor Layer (Helmet, Chestplate, Leggings, Boots)
        if (armorLayer != null) {
            armorLayer.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }

        // 6. Render Held Items Layer (Main hand & Offhand items/blocks/tools)
        if (itemLayer != null) {
            itemLayer.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }

        poseStack.popPose();
        return true;
    }

    private static ResourceLocation resolveHairResource(String path) {
        if (path == null || path.isBlank() || path.equals("none")) return null;
        try {
            if (path.contains(":")) {
                return ResourceLocation.parse(path);
            }
            if (path.startsWith("/")) path = path.substring(1);
            if (!path.startsWith("skins/")) path = "skins/" + path;
            return ResourceLocation.fromNamespaceAndPath("mca", path);
        } catch (Exception e) {
            return null;
        }
    }
}
