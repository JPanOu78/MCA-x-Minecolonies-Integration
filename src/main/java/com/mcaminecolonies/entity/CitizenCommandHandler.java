package com.mcaminecolonies.entity;

import com.mcaminecolonies.data.CitizenMCAData;
import com.mcaminecolonies.data.CitizenMCAManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.Memories;
import net.conczin.mca.entity.ai.MoveState;
import net.conczin.mca.entity.ai.brain.VillagerBrain;
import net.conczin.mca.entity.ai.relationship.RelationshipState;
import net.conczin.mca.entity.interaction.EntityCommandHandler;
import net.conczin.mca.item.RelationshipItem;
import net.conczin.mca.network.Network;
import net.conczin.mca.network.s2c.InteractionDialogueQuestionResponse;
import net.conczin.mca.registry.ItemsMCA;
import net.conczin.mca.server.world.data.FamilyTree;
import net.conczin.mca.server.world.data.FamilyTreeNode;
import net.conczin.mca.server.world.data.PlayerSaveData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

/**
 * EntityCommandHandler implementation for MineColonies citizens interacting with MCA.
 * Handles commands such as dialogue (chat, joke, flirt, hug, kiss, play), movement (follow, stay, move),
 * gifts, rides, and marriage relationships.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CitizenCommandHandler extends EntityCommandHandler {

    public CitizenCommandHandler(Mob entity) {
        super(entity);
    }

    @Override
    public boolean handle(ServerPlayer player, String command) {
        if (command == null || command.isEmpty()) {
            return false;
        }

        // 1. Move commands (follow, stay, move)
        Optional<MoveState> moveOpt = MoveState.byCommand(command);
        if (moveOpt.isPresent() && this.entity instanceof VillagerLike<?> vl) {
            MoveState state = moveOpt.get();
            vl.getVillagerBrain().setMoveState(state, player);
            this.entity.level().playSound(null, this.entity.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
            if (state == MoveState.FOLLOW) {
                if (this.entity instanceof AbstractEntityCitizen citizen) {
                    CitizenMCAData mcaData = CitizenMCAManager.getData(citizen);
                    mcaData.setFollowTarget(player.getUUID());
                    CitizenMCAManager.saveData(citizen, mcaData);
                }
                player.sendSystemMessage(Component.translatable("mca.interaction.follow.success", this.entity.getDisplayName()));
            } else if (state == MoveState.STAY) {
                if (this.entity instanceof AbstractEntityCitizen citizen) {
                    citizen.getNavigation().stop();
                }
                player.sendSystemMessage(Component.translatable("mca.interaction.stay.success", this.entity.getDisplayName()));
            } else {
                player.sendSystemMessage(Component.translatable("mca.interaction.move.success", this.entity.getDisplayName()));
            }
            return true;
        }

        // 1b. Home commands (gohome, sethome) based on MineColonies Resident Hut
        if (command.equals("gohome")) {
            if (this.entity instanceof AbstractEntityCitizen citizen) {
                net.minecraft.core.BlockPos homePos = null;
                if (citizen.getCitizenData() != null) {
                    homePos = citizen.getCitizenData().getBedPos();
                    if (homePos == null || homePos.equals(net.minecraft.core.BlockPos.ZERO)) {
                        homePos = citizen.getCitizenData().getHomePosition();
                    }
                }
                if (homePos == null || homePos.equals(net.minecraft.core.BlockPos.ZERO)) {
                    homePos = citizen.getEntityData().get(AbstractEntityCitizen.DATA_BED_POS);
                }

                if (homePos != null && !homePos.equals(net.minecraft.core.BlockPos.ZERO)) {
                    citizen.getNavigation().moveTo(homePos.getX() + 0.5, homePos.getY() + 0.5, homePos.getZ() + 0.5, 1.0);
                    this.entity.level().playSound(null, this.entity.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    if (this.entity.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.entity.getX(), this.entity.getY() + 1.0, this.entity.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
                    }
                    player.sendSystemMessage(Component.literal("<" + this.entity.getDisplayName().getString() + "> I am heading to my Resident Hut bed!"));
                } else {
                    this.entity.level().playSound(null, this.entity.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    player.sendSystemMessage(Component.literal("<" + this.entity.getDisplayName().getString() + "> I don't have an assigned Resident Hut bed yet!"));
                }
                return true;
            }
        }

        if (command.equals("sethome")) {
            if (this.entity instanceof AbstractEntityCitizen citizen) {
                net.minecraft.core.BlockPos homePos = citizen.getCitizenData() != null ? citizen.getCitizenData().getBedPos() : null;
                if (homePos == null || homePos.equals(net.minecraft.core.BlockPos.ZERO)) {
                    homePos = citizen.getEntityData().get(AbstractEntityCitizen.DATA_BED_POS);
                }
                if (homePos != null && !homePos.equals(net.minecraft.core.BlockPos.ZERO)) {
                    this.entity.level().playSound(null, this.entity.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    player.sendSystemMessage(Component.literal("<" + this.entity.getDisplayName().getString() + "> My home is my assigned Resident Hut at " + homePos.getX() + ", " + homePos.getY() + ", " + homePos.getZ() + "."));
                } else {
                    this.entity.level().playSound(null, this.entity.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    player.sendSystemMessage(Component.literal("<" + this.entity.getDisplayName().getString() + "> I don't have an assigned Resident Hut yet!"));
                }
                return true;
            }
        }

        // 2. Pick up child / baby
        if (command.equals("pick_up")) {
            if ((this.entity instanceof net.minecraft.world.entity.LivingEntity le && le.isBaby())
                    || (this.entity instanceof VillagerLike<?> vl && vl.getAgeState() != net.conczin.mca.entity.ai.relationship.AgeState.ADULT)) {
                this.entity.startRiding(player, true);
                player.connection.send(new ClientboundSetPassengersPacket(player));
                return true;
            }
        }

        // 3. Mount or dismount horse
        if (command.equals("ridehorse")) {
            if (this.entity.isPassenger()) {
                this.entity.stopRiding();
                return true;
            } else {
                AABB box = this.entity.getBoundingBox().inflate(10.0);
                List<Mob> mounts = this.entity.level().getEntitiesOfClass(Mob.class, box, e -> e instanceof Saddleable s && s.isSaddled() && !e.isVehicle());
                if (!mounts.isEmpty()) {
                    this.entity.startRiding(mounts.getFirst(), true);
                    return true;
                }
            }
        }

        // 4. Armor & equipment management
        if (command.equals("armor")) {
            if (this.entity instanceof AbstractEntityCitizen citizen) {
                player.openMenu(citizen);
                return true;
            }
        }

        // 5. Gift giving (item held in hand or specific gift action)
        if (command.equals("gift") || command.startsWith("gift.")) {
            ItemStack held = player.getMainHandItem();
            if (!held.isEmpty()) {
                // Check if gift is an MCA Engagement or Wedding Ring
                if (held.getItem() instanceof RelationshipItem && this.entity.level() instanceof ServerLevel sl) {
                    FamilyTree tree = FamilyTree.get(sl);
                    FamilyTreeNode node = tree.getOrCreate(this.entity);
                    FamilyTreeNode playerNode = tree.getOrCreate(player);
                    boolean isWedding = held.getItem() == ItemsMCA.WEDDING_RING;
                    RelationshipState targetState = isWedding ? RelationshipState.MARRIED_TO_PLAYER : RelationshipState.ENGAGED;

                    node.updatePartner(player, targetState);
                    playerNode.updatePartner(this.entity, targetState);
                    PlayerSaveData pData = PlayerSaveData.get(player);
                    if (isWedding) {
                        pData.marry(this.entity);
                    }
                    held.shrink(1);

                    sl.sendParticles(ParticleTypes.HEART, this.entity.getX(), this.entity.getEyeY() + 0.5, this.entity.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
                    this.entity.level().playSound(null, this.entity.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    player.sendSystemMessage(Component.translatable(isWedding ? "interaction.weddingRing.success" : "interaction.engagementRing.success", this.entity.getDisplayName()));
                    return true;
                }

                ItemStack gifted = held.split(1);
                if (this.entity instanceof VillagerLike<?> vl) {
                    Memories memories = vl.getVillagerBrain().getMemoriesForPlayer(player);
                    memories.modHearts(10);
                    vl.getVillagerBrain().rewardHearts(player, 10);
                }
                this.entity.level().playSound(null, this.entity.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.5F, 1.2F);
                if (this.entity.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HEART, this.entity.getX(), this.entity.getEyeY() + 0.5, this.entity.getZ(), 6, 0.3, 0.3, 0.3, 0.1);
                }
                player.sendSystemMessage(Component.translatable("mca.interaction.gift.success", this.entity.getDisplayName(), gifted.getHoverName()));
                return true;
            }
        }

        // 6. Dialogue interactions (chat, joke, flirt, hug, kiss, story, play)
        return handleDialogueAction(player, command);
    }

    private boolean handleDialogueAction(ServerPlayer player, String command) {
        if (!(this.entity instanceof VillagerLike<?> vl)) {
            return false;
        }

        VillagerBrain<?> brain = vl.getVillagerBrain();
        Memories memories = brain.getMemoriesForPlayer(player);
        int currentHearts = memories.getHearts();
        int fatigue = memories.getInteractionFatigue();

        // 1. Fatigue limit check (aligned with MCA Reborn dialogue fatigue)
        if (fatigue >= 4) {
            memories.modHearts(-2);
            memories.modInteractionFatigue(1);
            this.entity.level().playSound(null, this.entity.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
            if (this.entity.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.entity.getX(), this.entity.getEyeY() + 0.5, this.entity.getZ(), 5, 0.3, 0.3, 0.3, 0.1);
            }
            MutableComponent reply = Component.translatable("dialogue.chat.fail");
            player.sendSystemMessage(Component.literal("<" + this.entity.getDisplayName().getString() + "> ").append(reply));
            Network.sendToPlayer(new InteractionDialogueQuestionResponse(reply, false), player);
            return true;
        }

        // Increment fatigue on interaction attempt
        memories.modInteractionFatigue(1);

        boolean isFlirtOrKiss = command.equals("flirt") || command.equals("kiss");
        float baseChance = isFlirtOrKiss ? 0.75F : 0.85F;
        float fatiguePenalty = fatigue * 0.12F;
        float finalChance = Math.max(0.15F, baseChance - fatiguePenalty);

        boolean success;
        if (isFlirtOrKiss) {
            int minHearts = command.equals("kiss") ? 50 : 25;
            success = currentHearts >= minHearts && this.entity.getRandom().nextFloat() < finalChance;
        } else {
            success = this.entity.getRandom().nextFloat() < finalChance;
        }

        if (success) {
            int heartGain = switch (command) {
                case "kiss" -> 10;
                case "flirt" -> 5;
                case "hug" -> 4;
                case "joke" -> 3;
                default -> 2;
            };
            memories.modHearts(heartGain);
            brain.rewardHearts(player, heartGain);
            this.entity.level().playSound(null, this.entity.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
            if (this.entity.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HEART, this.entity.getX(), this.entity.getEyeY() + 0.5, this.entity.getZ(), 4, 0.3, 0.3, 0.3, 0.1);
            }
            MutableComponent reply = Component.translatable("dialogue." + command + ".success");
            player.sendSystemMessage(Component.literal("<" + this.entity.getDisplayName().getString() + "> ").append(reply));
            Network.sendToPlayer(new InteractionDialogueQuestionResponse(reply, false), player);
        } else {
            int heartLoss = isFlirtOrKiss ? -3 : -1;
            memories.modHearts(heartLoss);
            this.entity.level().playSound(null, this.entity.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
            if (this.entity.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.entity.getX(), this.entity.getEyeY() + 0.5, this.entity.getZ(), 3, 0.3, 0.3, 0.3, 0.1);
            }
            MutableComponent reply = Component.translatable("dialogue." + command + ".fail");
            player.sendSystemMessage(Component.literal("<" + this.entity.getDisplayName().getString() + "> ").append(reply));
            Network.sendToPlayer(new InteractionDialogueQuestionResponse(reply, false), player);
        }
        return true;
    }
}
