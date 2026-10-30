package com.mcaminecolonies.mixin.client;

import net.conczin.mca.client.gui.VillagerEditorScreen;
import net.conczin.mca.entity.VillagerEntityMCA;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ensures the preview entity (villagerVisualization) mirrors all color dye changes made
 * in the Villager Editor sliders immediately.
 */
@Mixin(value = VillagerEditorScreen.class, remap = false)
public abstract class VillagerEditorScreenMixin {

    @Shadow
    protected VillagerEntityMCA villager;

    @Shadow
    protected VillagerEntityMCA villagerVisualization;

    @Inject(method = {"refreshHairColor", "refreshSkinColor", "refreshEyeColor", "refreshEyeLeftColor"}, at = @At("TAIL"))
    private void mcaminecolonies$syncVisualizationColors(CallbackInfo ci) {
        if (this.villager != null && this.villagerVisualization != null) {
            this.villagerVisualization.setHairDye(this.villager.getHairDye());
            this.villagerVisualization.setSkinDye(this.villager.getSkinDye());
            this.villagerVisualization.setEyeDye(this.villager.getEyeDye());
            this.villagerVisualization.setEyeLeftDye(this.villager.getEyeLeftDye());
        }
    }
}

