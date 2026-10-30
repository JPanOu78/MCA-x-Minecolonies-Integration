package com.mcaminecolonies.client.render;

import com.mcaminecolonies.client.model.CitizenMCAModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.conczin.mca.client.render.layer.FaceLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;

/**
 * Custom FaceLayer for MineColonies citizens.
 * Overrides isTranslucent() to return false so that the eye layer (sclera, details, iris)
 * renders into the entityCutoutNoCull buffer instead of itemEntityTranslucentCull.
 * This guarantees proper depth-buffer writing, prevents translucent buffer flushing delays,
 * and ensures that hair bangs, hats, and armor properly occlude the eyes in front without
 * eyes rendering on top of hair or clipping out of order.
 */
public class CitizenFaceLayer extends FaceLayer<AbstractEntityCitizen, CitizenMCAModel> {

    public CitizenFaceLayer(RenderLayerParent<AbstractEntityCitizen, CitizenMCAModel> parent, CitizenMCAModel model, String variant) {
        super(parent, model, variant);
    }

    @Override
    protected boolean isTranslucent() {
        return false;
    }
}
