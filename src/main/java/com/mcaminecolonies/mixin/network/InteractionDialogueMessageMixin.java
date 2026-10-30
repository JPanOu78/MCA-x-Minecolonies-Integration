package com.mcaminecolonies.mixin.network;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.network.Network;
import net.conczin.mca.network.c2s.InteractionDialogueMessage;
import net.conczin.mca.network.s2c.InteractionDialogueResponse;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Intercepts InteractionDialogueMessage on the server when a player selects a dialogue answer with a MineColonies citizen,
 * triggering command handling and refreshing dialogue choices.
 */
@Mixin(value = InteractionDialogueMessage.class, remap = false)
public class InteractionDialogueMessageMixin {

    @Inject(method = "handleServer", at = @At("HEAD"), cancellable = true)
    private void mcaminecolonies$handleServer(ServerPlayer player, CallbackInfo ci) {
        InteractionDialogueMessage msg = (InteractionDialogueMessage) (Object) this;
        Entity entity = player.serverLevel().getEntity(msg.villagerUUID());

        if (entity instanceof AbstractEntityCitizen citizen && citizen instanceof VillagerLike<?> vl) {
            ci.cancel();

            // Dispatch command to CitizenCommandHandler
            String answer = msg.answer();
            vl.getInteractions().handle(player, answer);

            // Re-send dialogue answers so the dialogue UI stays responsive
            List<String> answers = new ArrayList<>();
            if (vl.getAgeState() == net.conczin.mca.entity.ai.relationship.AgeState.ADULT) {
                answers.add("chat");
                answers.add("joke");
                answers.add("story");
                answers.add("hug");
                answers.add("flirt");
                answers.add("kiss");
            } else {
                answers.add("rock_paper_scissor");
                answers.add("chat");
                answers.add("joke");
                answers.add("story");
                answers.add("hug");
            }

            Network.sendToPlayer(new InteractionDialogueResponse("main", answers), player);
        }
    }
}
