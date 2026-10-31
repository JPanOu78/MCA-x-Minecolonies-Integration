package com.mcaminecolonies.mixin.client;

import com.ldtteam.blockui.controls.Button;
import com.mcaminecolonies.client.gui.VisitorInteractionState;
import com.minecolonies.core.client.gui.WindowInteraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tracks whether the player explicitly clicked "Show citizen stats" in WindowInteraction
 * so that MainWindowCitizen only opens when stats are requested, and not on cancel or "maybe later".
 */
@Mixin(value = WindowInteraction.class, remap = false)
public abstract class WindowInteractionMixin {

    @Inject(method = "onButtonClicked", at = @At("HEAD"))
    private void mcaminecolonies$onButtonClicked(Button button, CallbackInfo ci) {
        if (button != null) {
            String id = button.getID();
            // "response_1" corresponds to response 0 in RecruitmentInteraction ("Show citizen stats")
            VisitorInteractionState.setShowStatsRequested("response_1".equals(id));
        }
    }
}
