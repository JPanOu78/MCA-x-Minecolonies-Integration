package com.mcaminecolonies.mixin.client;

import com.ldtteam.blockui.controls.ButtonImage;
import com.mcaminecolonies.client.gui.CitizenDialogueScreen;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.core.client.gui.citizen.AbstractWindowCitizen;
import com.minecolonies.core.client.gui.citizen.MainWindowCitizen;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects an MCA Dialogue button at the top-right corner of MineColonies MainWindowCitizen GUI.
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
        talkButton.setPosition(180, 8);
        talkButton.setImage(ResourceLocation.fromNamespaceAndPath("mcaminecolonies", "textures/gui/talk_icon.png"));
        talkButton.setHandler(btn -> {
            this.close();
            Minecraft.getInstance().setScreen(new CitizenDialogueScreen(this.citizen));
        });
        addChild(talkButton);
    }
}

