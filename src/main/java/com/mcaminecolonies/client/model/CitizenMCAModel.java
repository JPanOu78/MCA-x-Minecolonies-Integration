package com.mcaminecolonies.client.model;

import com.google.common.collect.ImmutableList;
import com.mcaminecolonies.client.appearance.CitizenAgeState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.conczin.mca.Config;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import java.util.List;
import java.util.Map;

/**
 * Humanoid model for MineColonies Citizens using standard 64x64 Player / MCA villager geometry,
 * complete with MCA-compliant female breasts geometry, positioning, and animation.
 */
public class CitizenMCAModel extends PlayerModel<AbstractEntityCitizen> {

    public final ModelPart breasts;
    public final ModelPart breastsWear;
    private float breastSize = 0.5F;

    public CitizenMCAModel(ModelPart root, boolean slim) {
        super(root, slim);
        this.breasts = root.hasChild("breasts") ? root.getChild("breasts") : new ModelPart(List.of(), Map.of());
        this.breastsWear = root.hasChild("breasts_wear") ? root.getChild("breasts_wear") : new ModelPart(List.of(), Map.of());
    }

    public static LayerDefinition createLayerDefinition(boolean slim) {
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, slim);
        PartDefinition root = mesh.getRoot();

        // MCA female breasts base skin: texOffs(18, 21), box (-3.25, -1.25, -1.5, 6, 3, 3)
        root.addOrReplaceChild("breasts",
                CubeListBuilder.create().texOffs(18, 21).addBox(-3.25F, -1.25F, -1.5F, 6.0F, 3.0F, 3.0F, CubeDeformation.NONE),
                PartPose.ZERO);

        // MCA female breasts clothing overlay: texOffs(18, 37), box (-3.25, -1.25, -1.5, 6, 3, 3) with +0.1F dilation
        root.addOrReplaceChild("breasts_wear",
                CubeListBuilder.create().texOffs(18, 37).addBox(-3.25F, -1.25F, -1.5F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.1F)),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(
                this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat,
                this.jacket, this.rightSleeve, this.leftSleeve, this.rightPants, this.leftPants,
                this.breasts, this.breastsWear
        );
    }

    public void applyAgeProportions(CitizenAgeState ageState) {
        // Suppress default AgeableListModel deformations so we can control uniform proportions
        this.young = false;

        float headScale = ageState.getHeadScale();
        this.head.xScale = headScale;
        this.head.yScale = headScale;
        this.head.zScale = headScale;
        this.hat.xScale = headScale;
        this.hat.yScale = headScale;
        this.hat.zScale = headScale;
    }

    @Override
    public void setupAnim(AbstractEntityCitizen entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (entity.isSleeping()) {
            this.head.xRot = 0.0F;
            this.head.yRot = 0.0F;
        }

        // Breast geometry setup & animation matching MCA standards
        boolean isFemale = entity.isFemale();
        boolean isAdult = !entity.isBaby();

        if (entity instanceof VillagerLike<?> vl) {
            isFemale = vl.getGenetics().getGender() == Gender.FEMALE;
            this.breastSize = vl.getGenetics().getBreastSize();
        }

        try {
            if (Config.getInstance() != null && !Config.getInstance().enableBoobs) {
                isFemale = false;
            }
        } catch (Throwable ignored) {
        }

        if (isFemale && isAdult) {
            this.breasts.visible = true;
            this.breastsWear.visible = true;

            float y = (float) (5.0 - Math.pow(this.breastSize, 0.5) * 2.5);
            float z = -1.5F + this.breastSize * 0.25F;

            // Pitch tilt 0.9424779F (~54 degrees) matching MCA's VillagerEntityBaseModelMCA
            this.breasts.setRotation(0.9424779F + this.body.xRot, 0.0F, 0.0F);
            this.breasts.setPos(0.25F, y, z);
            this.breastsWear.copyFrom(this.breasts);
        } else {
            this.breasts.visible = false;
            this.breastsWear.visible = false;
        }
    }

    @Override
    public void copyPropertiesTo(HumanoidModel<AbstractEntityCitizen> target) {
        super.copyPropertiesTo(target);
        if (target instanceof CitizenMCAModel mcaTarget) {
            mcaTarget.breasts.copyFrom(this.breasts);
            mcaTarget.breastsWear.copyFrom(this.breastsWear);
            mcaTarget.breasts.visible = this.breasts.visible;
            mcaTarget.breastsWear.visible = this.breastsWear.visible;
        }
    }
}
