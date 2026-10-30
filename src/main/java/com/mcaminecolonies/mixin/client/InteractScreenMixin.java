package com.mcaminecolonies.mixin.client;

import net.conczin.mca.client.gui.InteractScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

/**
 * Prevents NullPointerException in InteractScreen.drawTextPopups when dialogAnswers is null.
 */
@Mixin(value = InteractScreen.class, remap = false)
public abstract class InteractScreenMixin {

    @Shadow
    private List<String> dialogAnswers;

    @Inject(method = "setLastPhrase", at = @At("HEAD"))
    private void mcaminecolonies$fixNullDialogAnswersOnLastPhrase(Component phrase, boolean playSound, CallbackInfo ci) {
        if (this.dialogAnswers == null) {
            this.dialogAnswers = Collections.emptyList();
        }
    }

    @Inject(method = "drawTextPopups", at = @At("HEAD"))
    private void mcaminecolonies$ensureDialogAnswersNonNull(CallbackInfo ci) {
        if (this.dialogAnswers == null) {
            this.dialogAnswers = Collections.emptyList();
        }
    }
}
