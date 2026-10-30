package com.mcaminecolonies.mixin.client;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.IVisitorViewData;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.client.gui.WindowInteraction;
import com.minecolonies.core.client.gui.citizen.MainWindowCitizen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents WindowInteraction for MineColonies Visitors from falling back to open MainWindowCitizen
 * upon closing or canceling the recruit window.
 */
@Mixin(value = AbstractWindowSkeleton.class, remap = false)
public abstract class AbstractWindowSkeletonMixin {

    @Shadow
    @Mutable
    private BOWindow parent;

    @Inject(method = "close", at = @At("HEAD"))
    private void mcaminecolonies$preventVisitorParentWindowOnClose(CallbackInfo ci) {
        if ((Object) this instanceof WindowInteraction && this.parent instanceof MainWindowCitizen mwc) {
            if (mwc.getCitizen() instanceof IVisitorViewData
                    || (mwc.getCitizen() != null && mwc.getCitizen().getClass().getName().contains("Visitor"))) {
                this.parent = null;
            }
        }
    }
}

