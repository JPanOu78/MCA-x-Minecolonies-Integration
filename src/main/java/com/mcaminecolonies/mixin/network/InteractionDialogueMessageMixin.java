package com.mcaminecolonies.mixin.network;

import com.mcaminecolonies.entity.CitizenDialogueHandler;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.conczin.mca.network.c2s.InteractionDialogueMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts InteractionDialogueMessage on the server when a player selects a dialogue answer with a MineColonies citizen,
 * dispatching through MCA's native dialogue tree and advancing conversation.
 */
@Mixin(value = InteractionDialogueMessage.class, remap = false)
public class InteractionDialogueMessageMixin {

    @Inject(method = "handleServer", at = @At("HEAD"), cancellable = true)
    private void mcaminecolonies$handleServer(ServerPlayer player, CallbackInfo ci) {
        InteractionDialogueMessage msg = (InteractionDialogueMessage) (Object) this;
        Entity entity = player.serverLevel().getEntity(msg.villagerUUID());

        if (entity instanceof AbstractEntityCitizen citizen) {
            ci.cancel();
            CitizenDialogueHandler.handleDialogue(citizen, player, msg.question(), msg.answer());
        }
    }
}
