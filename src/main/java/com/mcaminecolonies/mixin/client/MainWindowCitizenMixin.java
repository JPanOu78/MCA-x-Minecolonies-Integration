package com.mcaminecolonies.mixin.client;

import com.ldtteam.blockui.controls.ButtonImage;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.core.client.gui.citizen.AbstractWindowCitizen;
import com.minecolonies.core.client.gui.citizen.MainWindowCitizen;
import net.conczin.mca.client.gui.InteractScreen;
import net.conczin.mca.entity.VillagerLike;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects an MCA Dialogue button at the top-right corner of MineColonies MainWindowCitizen GUI,
 * opening MCA's native InteractScreen directly, and ensures MainWindowCitizen never opens for visitors.
 */
@Mixin(value = MainWindowCitizen.class, remap = false)
public abstract class MainWindowCitizenMixin extends AbstractWindowCitizen {

    public MainWindowCitizenMixin(ICitizenDataView citizen, ResourceLocation xml) {
        super(citizen, xml);
    }

    @Inject(method = "onOpened", at = @At("HEAD"), cancellable = true)
    private void mcaminecolonies$preventVisitorMainWindow(CallbackInfo ci) {
        // Do not display MainWindowCitizen for visitors (tavern visitors, wandering recruits, etc.)
        if (this.citizen instanceof com.minecolonies.api.colony.IVisitorViewData
                || (this.citizen != null && this.citizen.getClass().getName().contains("Visitor"))) {
            Minecraft.getInstance().setScreen(null);
            ci.cancel();
            return;
        }

        if (Minecraft.getInstance().level != null && this.citizen != null) {
            Entity entity = Minecraft.getInstance().level.getEntity(this.citizen.getEntityId());
            if (entity != null && (entity instanceof com.minecolonies.core.entity.visitor.VisitorCitizen
                    || entity.getClass().getName().contains("Visitor"))) {
                Minecraft.getInstance().setScreen(null);
                ci.cancel();
            }
        }
    }

    @Inject(method = "onOpened", at = @At("TAIL"))
    private void mcaminecolonies$addTalkButton(CallbackInfo ci) {
        if (findPaneByID("mcaTalkButton") != null) return;

        // Do not add MCA dialogue button for visitors
        if (this.citizen instanceof com.minecolonies.api.colony.IVisitorViewData) {
            return;
        }

        if (Minecraft.getInstance().level != null && this.citizen != null) {
            Entity entity = Minecraft.getInstance().level.getEntity(this.citizen.getEntityId());
            if (entity != null && (entity instanceof com.minecolonies.core.entity.visitor.VisitorCitizen
                    || entity.getClass().getName().contains("Visitor"))) {
                return;
            }
        }

        ButtonImage talkButton = new ButtonImage();
        talkButton.setID("mcaTalkButton");
        talkButton.setSize(18, 18);
        talkButton.setPosition(180, 8); // Top-right position
        talkButton.setImage(ResourceLocation.fromNamespaceAndPath("mcaminecolonies", "textures/gui/talk_icon.png"));
        talkButton.setHandler(btn -> {
            this.close();
            if (Minecraft.getInstance().level != null && this.citizen != null) {
                Entity entity = Minecraft.getInstance().level.getEntity(this.citizen.getEntityId());
                if (entity instanceof VillagerLike<?> villager) {
                    Minecraft.getInstance().setScreen(new InteractScreen(villager));
                }
            }
        });
        addChild(talkButton);
    }
}
