package com.mcaminecolonies.mixin.network;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.network.Network;
import net.conczin.mca.network.c2s.InteractionDialogueInitMessage;
import net.conczin.mca.network.s2c.InteractionDialogueQuestionResponse;
import net.conczin.mca.network.s2c.InteractionDialogueResponse;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Intercepts InteractionDialogueInitMessage on the server when interacting with MineColonies citizens,
 * preventing vanilla MCA from dropping the packet and providing valid dialogue options.
 */
@Mixin(value = InteractionDialogueInitMessage.class, remap = false)
public class InteractionDialogueInitMixin {

    @Inject(method = "handleServer", at = @At("HEAD"), cancellable = true)
    private void mcaminecolonies$handleServer(ServerPlayer player, CallbackInfo ci) {
        InteractionDialogueInitMessage msg = (InteractionDialogueInitMessage) (Object) this;
        Entity entity = player.serverLevel().getEntity(msg.villagerUUID());

        if (entity instanceof AbstractEntityCitizen citizen && citizen instanceof VillagerLike<?> vl) {
            ci.cancel();

            // Send greeting phrase at the top of MCA InteractScreen
            Component greeting = Component.translatable("dialogue.root.main.say");
            Network.sendToPlayer(new InteractionDialogueQuestionResponse(greeting, false), player);

            // Populate clickable dialogue options based on citizen age stage
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
