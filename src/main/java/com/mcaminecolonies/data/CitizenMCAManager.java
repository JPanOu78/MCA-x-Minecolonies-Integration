package com.mcaminecolonies.data;

import com.mcaminecolonies.client.appearance.CitizenAgeState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.Memories;
import net.conczin.mca.entity.ai.MoveState;
import net.conczin.mca.entity.ai.Relationship;
import net.conczin.mca.entity.ai.relationship.CompassionateEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages MCA data lifecycle, ticking, growth progression, and state transitions for citizens.
 */
public class CitizenMCAManager {

    /**
     * Backup cache keyed by "colonyId:citizenId" so that if MineColonies despawns and respawns a fresh
     * EntityCitizen instance (e.g. resurrection, recall, or unstuck), MCA appearance and Memories are preserved.
     */
    private static final Map<String, CompoundTag> COLONY_CITIZEN_BACKUP = new ConcurrentHashMap<>();
    private static final Map<String, CompoundTag> COLONY_TRACKED_BACKUP = new ConcurrentHashMap<>();

    private static String getColonyCitizenKey(AbstractEntityCitizen citizen) {
        try {
            int colonyId = citizen.getEntityData().get(AbstractEntityCitizen.DATA_COLONY_ID);
            int citizenId = citizen.getEntityData().get(AbstractEntityCitizen.DATA_CITIZEN_ID);
            if (colonyId == 0 && citizen.getCitizenData() != null && citizen.getCitizenData().getColony() != null) {
                colonyId = citizen.getCitizenData().getColony().getID();
                citizenId = citizen.getCitizenData().getId();
            }
            if (colonyId != 0 && citizenId != 0) {
                return colonyId + ":" + citizenId;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static CitizenMCAData getData(AbstractEntityCitizen citizen) {
        if (citizen instanceof IMCAEntityCitizen duck) {
            CompoundTag tag = duck.mcaminecolonies$getMCAData();
            if (tag != null && !tag.isEmpty()) {
                return CitizenMCAData.fromNbt(tag, citizen.getUUID(), citizen.isFemale(), citizen.isBaby());
            }

            // Check persistent colony citizen data if entity was recreated or reloaded
            if (citizen.getCitizenData() instanceof IMCACitizenData cdDuck && cdDuck.mcaminecolonies$hasMCAData()) {
                CompoundTag colTag = cdDuck.mcaminecolonies$getMCAData().copy();
                duck.mcaminecolonies$setMCAData(colTag);
                return CitizenMCAData.fromNbt(colTag, citizen.getUUID(), citizen.isFemale(), citizen.isBaby());
            }

            // Check backup cache if citizen entity was recreated by MineColonies
            String key = getColonyCitizenKey(citizen);
            if (key != null && COLONY_CITIZEN_BACKUP.containsKey(key)) {
                CompoundTag backup = COLONY_CITIZEN_BACKUP.get(key).copy();
                duck.mcaminecolonies$setMCAData(backup);
                if (!citizen.level().isClientSide() && citizen instanceof VillagerLike vl && COLONY_TRACKED_BACKUP.containsKey(key)) {
                    try {
                        vl.getTypeDataManager().load(citizen, COLONY_TRACKED_BACKUP.get(key).copy());
                    } catch (Exception ignored) {
                    }
                }
                return CitizenMCAData.fromNbt(backup, citizen.getUUID(), citizen.isFemale(), citizen.isBaby());
            }
        }

        // Deterministic fallback default using colonyId and citizenId if known
        int colonyId = 0;
        int citizenId = 0;
        try {
            if (citizen.getCitizenData() != null) {
                citizenId = citizen.getCitizenData().getId();
                if (citizen.getCitizenData().getColony() != null) {
                    colonyId = citizen.getCitizenData().getColony().getID();
                }
            }
            if (citizenId == 0) {
                citizenId = citizen.getEntityData().get(AbstractEntityCitizen.DATA_CITIZEN_ID);
                colonyId = citizen.getEntityData().get(AbstractEntityCitizen.DATA_COLONY_ID);
            }
        } catch (Exception ignored) {
        }
        return CitizenMCAData.createDefault(citizen.getUUID(), colonyId, citizenId, citizen.isFemale(), citizen.isBaby());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void saveData(AbstractEntityCitizen citizen, CitizenMCAData data) {
        CompoundTag nbt = data.toNbt();
        if (citizen instanceof IMCAEntityCitizen duck) {
            duck.mcaminecolonies$setMCAData(nbt);
        }
        if (citizen.getCitizenData() instanceof IMCACitizenData cdDuck) {
            cdDuck.mcaminecolonies$setMCAData(nbt.copy());
            if (citizen.getCitizenData().getColony() != null) {
                citizen.getCitizenData().getColony().markDirty();
            }
        }
        String key = getColonyCitizenKey(citizen);
        if (key != null) {
            COLONY_CITIZEN_BACKUP.put(key, nbt.copy());
            if (!citizen.level().isClientSide() && citizen instanceof VillagerLike vl && vl.getTypeDataManager() != null) {
                try {
                    CompoundTag tracked = new CompoundTag();
                    vl.getTypeDataManager().save(citizen, tracked);
                    COLONY_TRACKED_BACKUP.put(key, tracked);
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Ticks movement, growth, and age on server side.
     */
    public static void tickCitizen(AbstractEntityCitizen citizen) {
        if (citizen.level().isClientSide()) return;

        // 1. High-frequency Follow and Stay movement handling to prevent MineColonies AI tug-of-war
        if (citizen instanceof VillagerLike<?> vl) {
            MoveState moveState = vl.getVillagerBrain().getMoveState();
            if (moveState == MoveState.STAY) {
                citizen.getNavigation().stop();
                if (citizen.getCitizenData() != null && !citizen.getCitizenData().isPaused()) {
                    citizen.getCitizenData().setPaused(true);
                }
            } else if (moveState == MoveState.FOLLOW) {
                if (citizen.getCitizenData() != null && !citizen.getCitizenData().isPaused()) {
                    citizen.getCitizenData().setPaused(true);
                }
                if (citizen.tickCount % 5 == 0) {
                    CitizenMCAData data = getData(citizen);
                    ServerPlayer followPlayer = null;
                    if (data.getFollowTarget() != null) {
                        followPlayer = (ServerPlayer) citizen.level().getPlayerByUUID(data.getFollowTarget());
                    }
                    if (followPlayer == null) {
                        List<ServerPlayer> nearby = citizen.level().getEntitiesOfClass(ServerPlayer.class, citizen.getBoundingBox().inflate(16.0));
                        if (!nearby.isEmpty()) {
                            followPlayer = nearby.get(0);
                        }
                    }
                    if (followPlayer != null && followPlayer.isAlive()) {
                        double distSq = citizen.distanceToSqr(followPlayer);
                        if (distSq > 400.0) { // > 20 blocks: teleport
                            citizen.teleportTo(followPlayer.getX(), followPlayer.getY(), followPlayer.getZ());
                        } else if (distSq > 6.25) { // > 2.5 blocks: pathfind
                            citizen.getNavigation().moveTo(followPlayer, 1.15);
                        } else {
                            citizen.getNavigation().stop();
                            citizen.getLookControl().setLookAt(followPlayer);
                        }
                    }
                }
            }
        }

        // Run growth & periodic updates every 20 ticks (1 second)
        if (citizen.tickCount % 20 != 0) return;

        CitizenMCAData data = getData(citizen);

        if (citizen instanceof VillagerLike<?> vl) {
            // Restore persisted MoveState if entity reloaded while in FOLLOW or STAY
            MoveState currentBrainMove = vl.getVillagerBrain().getMoveState();
            String savedMove = data.getMoveState();
            if ("FOLLOW".equals(savedMove) && currentBrainMove == MoveState.MOVE && data.getFollowTarget() != null) {
                ServerPlayer target = (ServerPlayer) citizen.level().getPlayerByUUID(data.getFollowTarget());
                if (target != null) {
                    vl.getVillagerBrain().setMoveState(MoveState.FOLLOW, target);
                }
            } else if ("STAY".equals(savedMove) && currentBrainMove == MoveState.MOVE) {
                vl.getVillagerBrain().setMoveState(MoveState.STAY, null);
            }

            // Fatigue recovery: reduce interaction fatigue by 1 every 1200 ticks (60 seconds) per MCA standard
            if (citizen.tickCount % 1200 == 0) {
                if (data.getFatigue() > 0) {
                    data.modFatigue(-1);
                    saveData(citizen, data);
                }
                for (ServerPlayer player : citizen.level().getEntitiesOfClass(ServerPlayer.class, citizen.getBoundingBox().inflate(32.0))) {
                    Memories mem = vl.getVillagerBrain().getMemoriesForPlayer(player);
                    if (mem != null && mem.getInteractionFatigue() > 0) {
                        mem.modInteractionFatigue(-1);
                    }
                }
            }

            // Gift desaturation reset: clear oldest item from gift saturation queue every 24000 ticks (1 day) per MCA standard
            int resetPeriod = net.conczin.mca.Config.getInstance().giftDesaturationReset;
            if (resetPeriod > 0 && citizen.tickCount % resetPeriod == 0) {
                if (citizen instanceof CompassionateEntity<?> comp
                        && comp.getRelationships() instanceof Relationship<?> rel) {
                    rel.getGiftSaturation().pop();
                }
            }
        }

        boolean isChild = citizen.isBaby();
        if (!isChild && data.getAge() < CitizenAgeState.ADULT.getMinAge()) {
            // MineColonies grew the child into an adult -> sync MCA age and AgeState
            data.setAge(CitizenAgeState.ADULT.getMinAge());
            if (citizen instanceof VillagerLike<?> vl) {
                vl.setTrackedValue(VillagerLike.AGE_STATE, net.conczin.mca.entity.ai.relationship.AgeState.ADULT);
                vl.updateAttributes();
            }
            saveData(citizen, data);
            return;
        }

        if (isChild) {
            CitizenAgeState oldState = data.getAgeState(true);
            data.addAge(20);
            CitizenAgeState newState = data.getAgeState(true);

            // Check if grown into adult
            if (data.getAge() >= CitizenAgeState.ADULT.getMinAge()) {
                citizen.setIsChild(false);
                citizen.getEntityData().set(AbstractEntityCitizen.DATA_IS_CHILD, false);
                if (citizen.getCitizenData() != null) {
                    citizen.getCitizenData().setIsChild(false);
                }
                data.setAge(CitizenAgeState.ADULT.getMinAge());
                saveData(citizen, data);

                if (citizen instanceof VillagerLike<?> vl) {
                    vl.setTrackedValue(VillagerLike.AGE_STATE, net.conczin.mca.entity.ai.relationship.AgeState.ADULT);
                    vl.updateAttributes();
                }
                citizen.refreshDimensions();

                if (citizen.level() instanceof ServerLevel serverLevel) {
                    serverLevel.playSound(null, citizen.getX(), citizen.getY(), citizen.getZ(),
                            SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            citizen.getX(), citizen.getY() + 1.2, citizen.getZ(),
                            15, 0.4, 0.4, 0.4, 0.05);
                }

                Component message = Component.translatable("chat.mcaminecolonies.grown_up", citizen.getDisplayName());
                citizen.level().players().forEach(p -> {
                    if (p.distanceToSqr(citizen) < 64 * 64) {
                        p.displayClientMessage(message, false);
                    }
                });
                return;
            }

            // If age stage changed (e.g. Toddler -> Child, Child -> Teen)
            if (oldState != newState) {
                saveData(citizen, data);

                if (citizen instanceof VillagerLike<?> vl) {
                    vl.setTrackedValue(VillagerLike.AGE_STATE, vl.getAgeState());
                    vl.updateAttributes();
                }
                citizen.refreshDimensions();

                if (citizen.level() instanceof ServerLevel serverLevel) {
                    serverLevel.playSound(null, citizen.getX(), citizen.getY(), citizen.getZ(),
                            SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.2F);
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            citizen.getX(), citizen.getY() + 0.8, citizen.getZ(),
                            10, 0.3, 0.3, 0.3, 0.05);
                }
            } else if (citizen.tickCount % 100 == 0) {
                // Periodic save to sync age ticks smoothly
                saveData(citizen, data);
            }
        } else if (citizen.tickCount % 200 == 0) {
            // Periodic backup sync for adults
            saveData(citizen, data);
        }
    }
}

