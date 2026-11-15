package com.mcaminecolonies.mixin.client;

import com.mcaminecolonies.client.gui.CitizenCustomizationScreen;
import com.mcaminecolonies.client.gui.CitizenDialogueScreen;
import com.mcaminecolonies.client.render.CitizenMCARenderer;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.client.render.RenderBipedCitizen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.conczin.mca.Config;
import net.conczin.mca.client.gui.InteractScreen;
import net.conczin.mca.client.gui.LimitedVillagerEditorScreen;
import net.conczin.mca.client.gui.VillagerEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderBipedCitizen.class, remap = false)
public abstract class CitizenRendererMixin {

    @Shadow(remap = false)
    protected abstract void renderNameTag(
            AbstractEntityCitizen entity,
            Component displayName,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            float partialTicks
    );

    @Inject(method = "render(Lcom/minecolonies/api/entity/citizen/AbstractEntityCitizen;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void mcaminecolonies$overrideCitizenRender(
            AbstractEntityCitizen entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci) {
        boolean rendered = CitizenMCARenderer.renderCitizen(entity, entityYaw, partialTicks, poseStack, buffer,
                packedLight);
        if (rendered) {
            Minecraft mc = Minecraft.getInstance();
            Screen screen = mc.screen;
            boolean inAvatarGui = screen instanceof InteractScreen
                    || screen instanceof VillagerEditorScreen
                    || screen instanceof LimitedVillagerEditorScreen
                    || screen instanceof CitizenDialogueScreen
                    || screen instanceof CitizenCustomizationScreen;

            if (!inAvatarGui && Minecraft.renderNames() && mc.player != null && !entity.isInvisibleTo(mc.player)) {
                double distSqr = mc.player.distanceToSqr(entity);
                double maxDist = Config.getInstance().nameTagDistance;
                boolean showMcaName = Config.getInstance().showNameTags && distSqr < (maxDist * maxDist);
                boolean showHoverName = entity == mc.crosshairPickEntity || entity.isCustomNameVisible();
                boolean hasStatusIcon = entity.getCitizenDataView() != null
                        && entity.getCitizenDataView().hasVisibleStatus()
                        && distSqr <= 4096.0D;

                if (showMcaName || showHoverName || hasStatusIcon) {
                    Component displayName = entity.getDisplayName();
                    if (!entity.hasCustomName() && entity.getCitizenDataView() != null
                            && entity.getCitizenDataView().getName() != null
                            && !entity.getCitizenDataView().getName().isBlank()) {
                        displayName = Component.literal(entity.getCitizenDataView().getName());
                    }
                    this.renderNameTag(entity, displayName, poseStack, buffer, packedLight, partialTicks);
                }
            }
            ci.cancel();
        }
    }
}
