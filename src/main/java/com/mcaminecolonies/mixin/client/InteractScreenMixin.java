package com.mcaminecolonies.mixin.client;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.client.gui.citizen.MainWindowCitizen;
import net.conczin.mca.client.gui.AbstractDynamicScreen;
import net.conczin.mca.client.gui.InteractScreen;
import net.conczin.mca.entity.VillagerLike;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

/**
 * Fixes dialogue looping, endless question state, null popups, and click-to-dismiss behavior in MCA's InteractScreen,
 * and adds a prominent button to open MineColonies Citizen Stats (MainWindowCitizen) natively.
 */
@Mixin(value = InteractScreen.class, remap = false)
public abstract class InteractScreenMixin extends AbstractDynamicScreen {

    @Shadow
    private VillagerLike<?> villager;

    @Shadow
    private List<String> dialogAnswers;

    @Shadow
    private String dialogAnswerHover;

    @Shadow
    private List<FormattedCharSequence> dialogQuestionText;

    @Shadow
    private String dialogQuestionId;

    @Unique
    private Button mcaminecolonies$statsButton;

    protected InteractScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void mcaminecolonies$addStatsButtonOnInit(CallbackInfo ci) {
        mcaminecolonies$ensureStatsButton();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void mcaminecolonies$ensureStatsButtonOnRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        mcaminecolonies$ensureStatsButton();
    }

    @Unique
    private void mcaminecolonies$ensureStatsButton() {
        if (this.villager == null) {
            return;
        }

        Entity entity = this.villager.asEntity();
        if (!(entity instanceof AbstractEntityCitizen citizen)) {
            return;
        }

        // Do not add stats button for visitors
        if (citizen instanceof com.minecolonies.core.entity.visitor.VisitorCitizen
                || citizen.getClass().getName().contains("Visitor")) {
            return;
        }

        // Check if button is already registered in children
        if (this.mcaminecolonies$statsButton != null && this.children().contains(this.mcaminecolonies$statsButton)) {
            return;
        }

        int btnWidth = 72;
        int btnHeight = 20;
        int btnX = this.width - btnWidth - 10;
        int btnY = 10;

        this.mcaminecolonies$statsButton = Button.builder(Component.literal("📊 Stats"), b -> {
            ICitizenDataView view = citizen.getCitizenDataView();
            if (view == null && Minecraft.getInstance().level != null) {
                int colonyId = citizen.getEntityData().get(AbstractEntityCitizen.DATA_COLONY_ID);
                int citizenId = citizen.getEntityData().get(AbstractEntityCitizen.DATA_CITIZEN_ID);
                if (colonyId > 0 && citizenId > 0) {
                    IColonyView colony = IColonyManager.getInstance().getColonyView(colonyId, Minecraft.getInstance().level.dimension());
                    if (colony != null) {
                        view = colony.getCitizen(citizenId);
                    }
                }
            }
            if (view != null) {
                Minecraft.getInstance().setScreen(null);
                new MainWindowCitizen(view).open();
            }
        })
        .bounds(btnX, btnY, btnWidth, btnHeight)
        .tooltip(Tooltip.create(Component.literal("View MineColonies Citizen Stats & Skills")))
        .build();

        this.addRenderableWidget(this.mcaminecolonies$statsButton);
    }

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
            // If the dialogue bubble is displayed and there are no choices (terminal reply or rejection):
            // Dismiss the dialogue bubble and restore the base interact menu buttons without needing ESC.
            if (this.dialogQuestionText != null && (this.dialogAnswers == null || this.dialogAnswers.isEmpty())) {
                this.dialogQuestionText = null;
                this.dialogQuestionId = null;
                this.dialogAnswers = Collections.emptyList();
                this.dialogAnswerHover = null;
                this.setLayout("interact");
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("TAIL"))
    private void mcaminecolonies$clearAnswersOnChoice(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button == 0 && this.dialogAnswerHover != null && this.dialogQuestionText != null) {
            // Once a question answer has been selected, clear choices immediately to prevent repeat clicks
            this.dialogAnswers = Collections.emptyList();
            this.dialogAnswerHover = null;
        }
    }
}
