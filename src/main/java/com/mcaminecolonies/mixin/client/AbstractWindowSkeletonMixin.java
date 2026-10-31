package com.mcaminecolonies.mixin.client;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.IVisitorViewData;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.client.gui.WindowInteraction;
import com.minecolonies.core.client.gui.citizen.MainWindowCitizen;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents WindowInteraction for MineColonies Visitors from falling back to open MainWindowCitizen
 * upon closing or canceling the recruit window, unless the player explicitly clicked "Show citizen stats".
 */
@Mixin(value = AbstractWindowSkeleton.class, remap = false)
public abstract class AbstractWindowSkeletonMixin {

    @Shadow
    private BOWindow parent;

    @Inject(method = "close", at = @At("HEAD"), cancellable = true)
    private void mcaminecolonies$preventVisitorParentWindowOnClose(CallbackInfo ci) {
        if ((Object) this instanceof WindowInteraction && this.parent instanceof MainWindowCitizen mwc) {
            if (mwc.getCitizen() instanceof IVisitorViewData
                    || (mwc.getCitizen() != null && mwc.getCitizen().getClass().getName().contains("Visitor"))) {
                if (!WindowInteractionMixin.isShowStatsRequested()) {
                    // Safely close WindowInteraction without opening the citizen window for visitors on cancel / not now / ESC
                    Minecraft.getInstance().setScreen(null);
                    ci.cancel();
                } else {
                    // Reset flag so subsequent closes won't reuse it
                    WindowInteractionMixin.setShowStatsRequested(false);
                }
            }
        }
    }
}
