package com.mcaminecolonies.client.model;

import com.google.common.collect.ImmutableList;
import com.mcaminecolonies.client.appearance.CitizenAgeState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;

/**
 * Humanoid model for MineColonies Citizens using standard 64x64 Player / MCA villager geometry.
 */
public class CitizenMCAModel extends PlayerModel<AbstractEntityCitizen> {

    public CitizenMCAModel(ModelPart root, boolean slim) {
        super(root, slim);
    }

    public static LayerDefinition createLayerDefinition(boolean slim) {
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, slim);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(
                this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat,
                this.jacket, this.rightSleeve, this.leftSleeve, this.rightPants, this.leftPants
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
    }

    @Override
    public void copyPropertiesTo(HumanoidModel<AbstractEntityCitizen> target) {
        super.copyPropertiesTo(target);
    }
}
