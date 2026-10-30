package com.mcaminecolonies.entity;

import com.mcaminecolonies.data.CitizenMCAData;
import com.mcaminecolonies.data.CitizenMCAManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.Memories;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.network.Network;
import net.conczin.mca.network.s2c.InteractionDialogueQuestionResponse;
import net.conczin.mca.network.s2c.InteractionDialogueResponse;
import net.conczin.mca.registry.EntitiesMCA;
import net.conczin.mca.resources.Dialogues;
import net.conczin.mca.resources.data.dialogue.Answer;
import net.conczin.mca.resources.data.dialogue.Question;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bridges MineColonies citizens with MCA Reborn's native dialogue system (Dialogues.java).
 * Manages an in-memory delegate VillagerEntityMCA for packet handling, relationship progression,
 * fatigue management, and multi-turn conversational dialogue trees.
 */
public class CitizenDialogueHandler {

    private static final Map<UUID, CitizenVillagerDelegate> DELEGATES = new HashMap<>();

    /**
     * Subclass of VillagerEntityMCA that overrides messaging to synchronize terminal responses
     * (jokes, chat success/fail, refusals) directly with the player's client GUI dialogue bubble.
     */
    public static class CitizenVillagerDelegate extends VillagerEntityMCA {
        private final AbstractEntityCitizen citizen;

        public CitizenVillagerDelegate(Level level, Gender gender, AbstractEntityCitizen citizen) {
            super(gender == Gender.FEMALE ? EntitiesMCA.FEMALE_VILLAGER : EntitiesMCA.MALE_VILLAGER, level, gender);
            this.citizen = citizen;
            this.setUUID(citizen.getUUID());
        }

        @Override
        public Component getName() {
            return this.citizen != null ? this.citizen.getName() : super.getName();
        }

        @Override
        public boolean isSpeechImpaired() {
            if (this.citizen instanceof VillagerLike<?> vl) {
                return vl.isSpeechImpaired();
            }
            return super.isSpeechImpaired();
        }

        @Override
        public boolean isToYoungToSpeak() {
            if (this.citizen instanceof VillagerLike<?> vl) {
                return vl.isToYoungToSpeak();
            }
            return super.isToYoungToSpeak();
        }

        @Override
        public Component sendChatMessage(Component component, Entity entity) {
            Component transformed = super.sendChatMessage(component, entity);
            if (entity instanceof ServerPlayer sp) {
                Network.sendToPlayer(new InteractionDialogueQuestionResponse(transformed, isSpeechImpaired()), sp);
                Network.sendToPlayer(new InteractionDialogueResponse("", Collections.emptyList()), sp);
            }
            return transformed;
        }

        @Override
        public void sendChatMessage(Player player, String translationKey, Object... args) {
            sendChatMessage(getTranslatable(player, translationKey, args), player);
        }
    }

    public static CitizenVillagerDelegate getOrCreateDelegate(AbstractEntityCitizen citizen, ServerPlayer player) {
        CitizenVillagerDelegate delegate = DELEGATES.get(citizen.getUUID());
        if (delegate == null || delegate.level() != citizen.level()) {
            delegate = new CitizenVillagerDelegate(
                    citizen.level(),
                    citizen.isFemale() ? Gender.FEMALE : Gender.MALE,
                    citizen
            );
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
                mem.setInteractionFatigue(data.getFatigue());
            }
            if (citizen instanceof VillagerLike<?> vl) {
                Memories citMem = vl.getVillagerBrain().getMemoriesForPlayer(player);
                if (citMem != null) {
                    citMem.setHearts(data.getHearts());
                    citMem.setInteractionFatigue(data.getFatigue());
                }
            }
        }

        return delegate;
    }

    public static void handleInit(AbstractEntityCitizen citizen, ServerPlayer player) {
        // Do not allow visitors to open MCA dialogue
        if (citizen instanceof com.minecolonies.core.entity.visitor.VisitorCitizen
                || citizen.getClass().getName().contains("Visitor")) {
            return;
        }

        CitizenMCAData data = CitizenMCAManager.getData(citizen);
        if (data != null && data.getFatigue() >= 4) {
            // Dialogue limit: citizen is exhausted from talking! Reject interaction
            citizen.level().playSound(null, citizen.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
            if (citizen.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, citizen.getX(), citizen.getEyeY() + 0.5, citizen.getZ(), 5, 0.3, 0.3, 0.3, 0.1);
            }
            MutableComponent reply = Component.translatable("dialogue.chat.fail");
            player.sendSystemMessage(Component.literal("<" + citizen.getDisplayName().getString() + "> ").append(reply));
            // Send empty dialogue options to clear any previous answers, and show rejection phrase
            Network.sendToPlayer(new InteractionDialogueResponse("", Collections.emptyList()), player);
            Network.sendToPlayer(new InteractionDialogueQuestionResponse(reply, false), player);
            return;
        }

        CitizenVillagerDelegate delegate = getOrCreateDelegate(citizen, player);
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
        // Do not allow visitors to open MCA dialogue
        if (citizen instanceof com.minecolonies.core.entity.visitor.VisitorCitizen
                || citizen.getClass().getName().contains("Visitor")) {
            return;
        }

        CitizenMCAData data = CitizenMCAManager.getData(citizen);
        if (data != null && data.getFatigue() >= 4) {
            citizen.level().playSound(null, citizen.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
            if (citizen.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, citizen.getX(), citizen.getEyeY() + 0.5, citizen.getZ(), 5, 0.3, 0.3, 0.3, 0.1);
            }
            data.addHearts(-2);
            data.modFatigue(1);
            CitizenMCAManager.saveData(citizen, data);
            if (citizen instanceof VillagerLike<?> vl) {
                Memories mem = vl.getVillagerBrain().getMemoriesForPlayer(player);
                if (mem != null) {
                    mem.modHearts(-2);
                    mem.modInteractionFatigue(1);
                }
            }
            MutableComponent reply = Component.translatable("dialogue.chat.fail");
            player.sendSystemMessage(Component.literal("<" + citizen.getDisplayName().getString() + "> ").append(reply));
            Network.sendToPlayer(new InteractionDialogueResponse("", Collections.emptyList()), player);
            Network.sendToPlayer(new InteractionDialogueQuestionResponse(reply, false), player);
            return;
        }

        CitizenVillagerDelegate delegate = getOrCreateDelegate(citizen, player);

        // Forward non-dialogue movement commands to citizen directly (stay, follow, move, etc.)
        if (citizen instanceof VillagerLike<?> vl && !isDialogueTopic(answer)) {
            vl.getInteractions().handle(player, answer);
        }

        if (data != null && isDialogueTopic(answer)) {
            data.modFatigue(1);
            CitizenMCAManager.saveData(citizen, data);
        }

        Dialogues.getInstance().selectAnswer(delegate, player, question, answer);
        syncAfterDialogue(citizen, delegate, player);
    }

    private static boolean isDialogueTopic(String cmd) {
        if (cmd == null) return false;
        return switch (cmd) {
            case "chat", "joke", "story", "hug", "flirt", "kiss", "procreate", "procreate_engaged",
                 "adopt", "divorce", "divorceInitiate", "divorcePapers", "rock_paper_scissor",
                 "rumors", "apologize", "hire" -> true;
            default -> false;
        };
    }

    private static void syncAfterDialogue(AbstractEntityCitizen citizen, VillagerEntityMCA delegate, ServerPlayer player) {
        CitizenMCAData data = CitizenMCAManager.getData(citizen);
        if (data != null) {
            Memories mem = delegate.getVillagerBrain().getMemoriesForPlayer(player);
            if (mem != null) {
                data.setHearts(mem.getHearts());
                data.setFatigue(Math.max(data.getFatigue(), mem.getInteractionFatigue()));
                CitizenMCAManager.saveData(citizen, data);
                if (citizen instanceof VillagerLike<?> vl) {
                    Memories citMem = vl.getVillagerBrain().getMemoriesForPlayer(player);
                    if (citMem != null) {
                        citMem.setHearts(data.getHearts());
                        citMem.setInteractionFatigue(data.getFatigue());
                    }
                }
            }
        }
    }
}
