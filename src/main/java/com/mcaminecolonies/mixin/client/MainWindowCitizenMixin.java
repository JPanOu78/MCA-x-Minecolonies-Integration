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
 * opening MCA's native InteractScreen directly.
 */
@Mixin(value = MainWindowCitizen.class, remap = false)
public abstract class MainWindowCitizenMixin extends AbstractWindowCitizen {

    public MainWindowCitizenMixin(ICitizenDataView citizen, ResourceLocation xml) {
        super(citizen, xml);
    }

    @Inject(method = "onOpened", at = @At("TAIL"))
    private void mcaminecolonies$addTalkButton(CallbackInfo ci) {
        if (findPaneByID("mcaTalkButton") != null) return;

        ButtonImage talkButton = new ButtonImage();
        talkButton.setID("mcaTalkButton");
        talkButton.setSize(18, 18);
        talkButton.setPosition(186, 6); // Placed at top-right corner inside the window frame
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
