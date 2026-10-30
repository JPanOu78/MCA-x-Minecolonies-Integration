package com.mcaminecolonies.entity;

import com.mcaminecolonies.data.CitizenMCAData;
import com.mcaminecolonies.data.CitizenMCAManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.Memories;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.network.Network;
import net.conczin.mca.network.s2c.InteractionDialogueResponse;
import net.conczin.mca.registry.EntitiesMCA;
import net.conczin.mca.resources.Dialogues;
import net.conczin.mca.resources.data.dialogue.Answer;
import net.conczin.mca.resources.data.dialogue.Question;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bridges MineColonies citizens with MCA Reborn's native dialogue system (Dialogues.java).
 * Manages an in-memory delegate VillagerEntityMCA for packet handling, relationship progression,
 * fatigue management, and multi-turn conversational dialogue trees.
 */
public class CitizenDialogueHandler {

    private static final Map<UUID, VillagerEntityMCA> DELEGATES = new HashMap<>();

    public static VillagerEntityMCA getOrCreateDelegate(AbstractEntityCitizen citizen, ServerPlayer player) {
        VillagerEntityMCA delegate = DELEGATES.get(citizen.getUUID());
        if (delegate == null || delegate.level() != citizen.level()) {
            delegate = new VillagerEntityMCA(
                    EntitiesMCA.MALE_VILLAGER,
                    citizen.level(),
                    citizen.isFemale() ? Gender.FEMALE : Gender.MALE
            );
            delegate.setUUID(citizen.getUUID());
            DELEGATES.put(citizen.getUUID(), delegate);
        }

        delegate.setPos(citizen.getX(), citizen.getY(), citizen.getZ());
        delegate.setCustomName(citizen.getCustomName());

        // Sync genetics, gender, and traits from citizen
        if (citizen instanceof VillagerLike<?> vl) {
            delegate.copyVillagerAttributesFrom(vl);
            if (vl.getGenetics() != null) {
                delegate.getGenetics().setGender(vl.getGenetics().getGender());
            }
        }

        // Sync hearts & memories
        CitizenMCAData data = CitizenMCAManager.getData(citizen);
        if (data != null) {
            Memories mem = delegate.getVillagerBrain().getMemoriesForPlayer(player);
            if (mem != null) {
                mem.setHearts(data.getHearts());
            }
        }

        return delegate;
    }

    public static void handleInit(AbstractEntityCitizen citizen, ServerPlayer player) {
        VillagerEntityMCA delegate = getOrCreateDelegate(citizen, player);
        Question question = Dialogues.getInstance().getQuestion("root");
        if (question != null) {
            if (question.isAuto()) {
                Answer randomAnswer = question.getRandomAnswer();
                if (randomAnswer != null) {
                    Dialogues.getInstance().selectAnswer(delegate, player, question.getName(), randomAnswer.getName());
                }
            } else {
                InteractionDialogueResponse response = new InteractionDialogueResponse(question, player, delegate);
                Network.sendToPlayer(response, player);
            }
        }
        syncAfterDialogue(citizen, delegate, player);
    }

    public static void handleDialogue(AbstractEntityCitizen citizen, ServerPlayer player, String question, String answer) {
        VillagerEntityMCA delegate = getOrCreateDelegate(citizen, player);

        // Also forward interaction command to citizen directly (for stay, follow, etc.)
        if (citizen instanceof VillagerLike<?> vl) {
            vl.getInteractions().handle(player, answer);
        }

        Dialogues.getInstance().selectAnswer(delegate, player, question, answer);
        syncAfterDialogue(citizen, delegate, player);
    }

    private static void syncAfterDialogue(AbstractEntityCitizen citizen, VillagerEntityMCA delegate, ServerPlayer player) {
        CitizenMCAData data = CitizenMCAManager.getData(citizen);
        if (data != null) {
            Memories mem = delegate.getVillagerBrain().getMemoriesForPlayer(player);
            if (mem != null) {
                data.setHearts(mem.getHearts());
                CitizenMCAManager.saveData(citizen, data);
            }
        }
    }
}

