package com.mcaminecolonies.mixin.network;

import com.mcaminecolonies.entity.CitizenDialogueHandler;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.conczin.mca.network.c2s.InteractionDialogueInitMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts InteractionDialogueInitMessage on the server when interacting with MineColonies citizens,
 * bridging with native MCA Reborn dialogue tree.
 */
@Mixin(value = InteractionDialogueInitMessage.class, remap = false)
public class InteractionDialogueInitMixin {

    @Inject(method = "handleServer", at = @At("HEAD"), cancellable = true)
    private void mcaminecolonies$handleServer(ServerPlayer player, CallbackInfo ci) {
        InteractionDialogueInitMessage msg = (InteractionDialogueInitMessage) (Object) this;
        Entity entity = player.serverLevel().getEntity(msg.villagerUUID());

        if (entity instanceof AbstractEntityCitizen citizen) {
            ci.cancel();
            CitizenDialogueHandler.handleInit(citizen, player);
        }
    }
}
