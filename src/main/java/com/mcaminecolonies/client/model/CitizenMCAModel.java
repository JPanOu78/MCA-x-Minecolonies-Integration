package com.mcaminecolonies.client.model;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Humanoid model for MineColonies Citizens using standard 64x64 Player / MCA villager geometry.
 */
public class CitizenMCAModel extends PlayerModel<AbstractEntityCitizen> {

    public CitizenMCAModel(ModelPart root, boolean slim) {
        super(root, slim);
    }

    @Override
    public void setupAnim(AbstractEntityCitizen entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (entity.isSleeping()) {
            this.head.xRot = 0.0F;
            this.head.yRot = 0.0F;
        }
    }
}
