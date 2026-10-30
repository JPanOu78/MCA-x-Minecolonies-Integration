package com.mcaminecolonies.mixin.client;

import net.conczin.mca.client.gui.AbstractDynamicScreen;
import net.conczin.mca.client.gui.InteractScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

/**
 * Fixes dialogue looping, endless question state, null popups, and click-to-dismiss behavior in MCA's InteractScreen.
 */
@Mixin(value = InteractScreen.class, remap = false)
public abstract class InteractScreenMixin {

    @Shadow
    private List<String> dialogAnswers;

    @Shadow
    private String dialogAnswerHover;

    @Shadow
    private List<FormattedCharSequence> dialogQuestionText;

    @Shadow
    private String dialogQuestionId;

    @Inject(method = "setLastPhrase", at = @At("HEAD"))
    private void mcaminecolonies$fixNullDialogAnswersOnLastPhrase(Component phrase, boolean playSound, CallbackInfo ci) {
        if (this.dialogAnswers == null) {
            this.dialogAnswers = Collections.emptyList();
        }
    }

    @Inject(method = "drawTextPopups", at = @At("HEAD"))
    private void mcaminecolonies$ensureDialogAnswersNonNull(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (this.dialogAnswers == null) {
            this.dialogAnswers = Collections.emptyList();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mcaminecolonies$handleDialogueClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button == 0) { // Left-click
            // If the dialogue bubble is displayed and there are no choices (or clicking outside options):
            // Dismiss the dialogue bubble and restore the base interact menu buttons without needing ESC.
            if (this.dialogQuestionText != null && (this.dialogAnswers == null || this.dialogAnswers.isEmpty() || this.dialogAnswerHover == null)) {
                this.dialogQuestionText = null;
                this.dialogQuestionId = null;
                this.dialogAnswers = Collections.emptyList();
                this.dialogAnswerHover = null;
                ((AbstractDynamicScreen) (Object) this).setLayout("interact");
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("TAIL"))
    private void mcaminecolonies$clearAnswersOnChoice(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button == 0 && this.dialogQuestionText != null) {
            // Once a question answer has been selected, clear choices immediately to prevent repeat clicks
            this.dialogAnswers = Collections.emptyList();
            this.dialogAnswerHover = null;
        }
    }
}
