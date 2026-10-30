package com.mcaminecolonies.entity;

import com.mcaminecolonies.data.CitizenMCAData;
import com.mcaminecolonies.data.CitizenMCAManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.conczin.mca.Config;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.Memories;
import net.conczin.mca.entity.ai.Relationship;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.conczin.mca.entity.ai.relationship.CompassionateEntity;
import net.conczin.mca.entity.ai.relationship.EntityRelationship;
import net.conczin.mca.entity.ai.relationship.RelationshipState;
import net.conczin.mca.item.BouquetItem;
import net.conczin.mca.item.EngagementRingItem;
import net.conczin.mca.item.RelationshipItem;
import net.conczin.mca.item.WeddingRingItem;
import net.conczin.mca.server.world.data.FamilyTree;
import net.conczin.mca.server.world.data.FamilyTreeNode;
import net.conczin.mca.server.world.data.PlayerSaveData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

/**
 * Enforces native MCA Reborn relationship conditions, prerequisites, hearts requirements,
 * family validation, and proposal/divorce ceremony handling for MineColonies citizens.
 */
public class CitizenRelationshipHelper {

    public static boolean isRelationshipItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getItem() instanceof RelationshipItem) return true;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return id.contains("ring") || id.contains("bouquet") || id.contains("divorce");
    }

    public static boolean handleRelationshipItem(ServerPlayer player, ItemStack held, AbstractEntityCitizen citizen) {
        if (held == null || held.isEmpty() || !(citizen instanceof VillagerLike<?> vl)) {
            return false;
        }

        String id = BuiltInRegistries.ITEM.getKey(held.getItem()).getPath();
        boolean isBouquet = held.getItem() instanceof BouquetItem || id.contains("bouquet");
        boolean isEngagement = held.getItem() instanceof EngagementRingItem || id.contains("engagement_ring");
        boolean isWedding = held.getItem() instanceof WeddingRingItem || id.contains("wedding_ring");
        boolean isDivorce = id.contains("divorce");

        if (!isBouquet && !isEngagement && !isWedding && !isDivorce) {
            return false;
        }

        ServerLevel level = player.serverLevel();
        PlayerSaveData playerData = PlayerSaveData.get(player);
        Memories memories = vl.getVillagerBrain().getMemoriesForPlayer(player);
        int currentHearts = memories.getHearts();

        CompassionateEntity<?> comp = (citizen instanceof CompassionateEntity<?> c) ? c : null;
        java.util.Optional<EntityRelationship> relOpt = EntityRelationship.of(citizen);
        boolean isCitizenMarried = relOpt.map(EntityRelationship::isMarried).orElse(false);
        boolean isCitizenEngaged = relOpt.map(EntityRelationship::isEngaged).orElse(false);
        boolean isEngagedToPlayer = relOpt.map(r -> r.isEngagedWith(player.getUUID())).orElse(false)
                || (comp != null && Relationship.IS_ENGAGED.test(comp, player))
                || playerData.isEngagedWith(citizen.getUUID());
        boolean isMarriedToPlayer = relOpt.map(r -> r.isMarriedTo(player.getUUID())).orElse(false)
                || (comp != null && Relationship.IS_MARRIED.test(comp, player))
                || playerData.isMarriedTo(citizen.getUUID());
        boolean isPromisedToPlayer = relOpt.map(r -> r.isPromisedTo(player.getUUID())).orElse(false)
                || (comp != null && Relationship.IS_PROMISED.test(comp, player))
                || playerData.isPromisedTo(citizen.getUUID());

        // 1. Handle Divorce Papers
        if (isDivorce) {
            if (!isMarriedToPlayer && !isEngagedToPlayer && !isPromisedToPlayer) {
                reject(player, vl, citizen, "interaction.relationship.fail.marriedtogiver");
                return true;
            }

            if (!player.isCreative()) {
                held.shrink(1);
            }

            FamilyTree tree = FamilyTree.get(level);
            FamilyTreeNode citizenNode = tree.getOrCreate(citizen);
            FamilyTreeNode playerNode = tree.getOrCreate(player);
            citizenNode.updatePartner(null, RelationshipState.SINGLE);
            playerNode.updatePartner(null, RelationshipState.SINGLE);

            playerData.endRelationShip(RelationshipState.SINGLE);
            relOpt.ifPresent(r -> r.endRelationShip(RelationshipState.SINGLE));
            memories.modHearts(-50);

            CitizenMCAData data = CitizenMCAManager.getData(citizen);
            data.setHearts(memories.getHearts());
            CitizenMCAManager.saveData(citizen, data);

            level.playSound(null, citizen.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, citizen.getX(), citizen.getEyeY() + 0.5, citizen.getZ(), 8, 0.3, 0.3, 0.3, 0.1);
            vl.sendChatMessage(player, "interaction.divorce.success");
            return true;
        }

        // 2. Validate Proposal Conditions (Matching MCA RelationshipItem.validate)
        // a. Child / Baby check: must be Adult
        if (citizen.isBaby() || vl.getAgeState() != AgeState.ADULT) {
            reject(player, vl, citizen, "interaction.relationship.fail.isbaby");
            return true;
        }

        // b. Relative check: incest is strictly disallowed in MCA
        if (comp != null && Relationship.IS_RELATIVE.test(comp, player)) {
            reject(player, vl, citizen, "interaction.relationship.fail.isparent");
            return true;
        }

        // c. Already married to this player
        if (isMarriedToPlayer) {
            reject(player, vl, citizen, "interaction.relationship.fail.marriedtogiver");
            return true;
        }

        // d. Citizen is already married to someone else
        if (isCitizenMarried) {
            reject(player, vl, citizen, "interaction.relationship.fail.married");
            return true;
        }

        // e. Citizen is already engaged to someone else
        if (isCitizenEngaged && !isEngagedToPlayer) {
            reject(player, vl, citizen, "interaction.relationship.fail.engaged");
            return true;
        }

        // f. Player is already married to someone else
        if (playerData.isMarried()) {
            reject(player, vl, citizen, "interaction.relationship.fail.playermarried");
            return true;
        }

        // g. Gender orientation / attraction check
        if (!vl.canBeAttractedTo(playerData)) {
            reject(player, vl, citizen, "interaction.relationship.fail.incompatible");
            return true;
        }

        // h. Hearts requirements and stage prerequisites
        Config config = Config.getInstance();
        if (isBouquet) {
            int requiredHearts = config.bouquetHeartsRequirement;
            if (currentHearts < requiredHearts) {
                reject(player, vl, citizen, "interaction.relationship.fail.lowhearts");
                return true;
            }
        } else if (isEngagement) {
            if (isEngagedToPlayer) {
                reject(player, vl, citizen, "interaction.engage.fail.engaged");
                return true;
            }
            int requiredHearts = config.engagementHeartsRequirement;
            if (currentHearts < requiredHearts) {
                reject(player, vl, citizen, "interaction.relationship.fail.lowhearts");
                return true;
            }
        } else if (isWedding) {
            // In MCA, player and villager MUST be engaged before wedding ceremony!
            if (!isEngagedToPlayer) {
                reject(player, vl, citizen, "interaction.relationship.fail.engaged");
                player.sendSystemMessage(Component.literal("§cYou must propose with an Engagement Ring and become engaged first!"));
                return true;
            }
            int requiredHearts = config.marriageHeartsRequirement;
            if (currentHearts < requiredHearts) {
                reject(player, vl, citizen, "interaction.relationship.fail.lowhearts");
                return true;
            }
        }

        // 3. All conditions passed! Execute proposal
        if (!player.isCreative()) {
            held.shrink(1);
        }

        FamilyTree tree = FamilyTree.get(level);
        FamilyTreeNode citizenNode = tree.getOrCreate(citizen);
        FamilyTreeNode playerNode = tree.getOrCreate(player);

        if (isWedding) {
            playerData.marry(citizen);
            relOpt.ifPresent(r -> r.marry(player));
            citizenNode.updatePartner(player, RelationshipState.MARRIED_TO_PLAYER);
            playerNode.updatePartner(citizen, RelationshipState.MARRIED_TO_PLAYER);
            vl.getVillagerBrain().modifyMoodValue(15);
            memories.modHearts(20);

            level.playSound(null, citizen.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.NEUTRAL, 1.0F, 1.0F);
            level.sendParticles(ParticleTypes.HEART, citizen.getX(), citizen.getEyeY() + 0.5, citizen.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
            vl.sendChatMessage(player, "interaction.marry.success");
        } else if (isEngagement) {
            playerData.engage(citizen);
            relOpt.ifPresent(r -> r.engage(player));
            citizenNode.updatePartner(player, RelationshipState.ENGAGED);
            playerNode.updatePartner(citizen, RelationshipState.ENGAGED);
            vl.getVillagerBrain().modifyMoodValue(10);
            memories.modHearts(15);

            level.playSound(null, citizen.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0F, 1.0F);
            level.sendParticles(ParticleTypes.HEART, citizen.getX(), citizen.getEyeY() + 0.5, citizen.getZ(), 10, 0.4, 0.4, 0.4, 0.1);
            vl.sendChatMessage(player, "interaction.engage.success");
        } else {
            playerData.promise(citizen);
            relOpt.ifPresent(r -> r.promise(player));
            citizenNode.updatePartner(player, RelationshipState.PROMISED);
            playerNode.updatePartner(citizen, RelationshipState.PROMISED);
            vl.getVillagerBrain().modifyMoodValue(5);
            memories.modHearts(10);

            level.playSound(null, citizen.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0F, 1.2F);
            level.sendParticles(ParticleTypes.HEART, citizen.getX(), citizen.getEyeY() + 0.5, citizen.getZ(), 8, 0.3, 0.3, 0.3, 0.1);
            vl.sendChatMessage(player, "interaction.promise.success");
        }

        // Sync hearts back to CitizenMCAData
        CitizenMCAData data = CitizenMCAManager.getData(citizen);
        data.setHearts(memories.getHearts());
        CitizenMCAManager.saveData(citizen, data);

        return true;
    }

    private static void reject(ServerPlayer player, VillagerLike<?> vl, AbstractEntityCitizen citizen, String translationKey) {
        player.serverLevel().playSound(null, citizen.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
        player.serverLevel().sendParticles(ParticleTypes.ANGRY_VILLAGER, citizen.getX(), citizen.getEyeY() + 0.5, citizen.getZ(), 5, 0.3, 0.3, 0.3, 0.1);
        vl.sendChatMessage(player, translationKey);
    }
}
