package com.mcaminecolonies.data;

import com.mcaminecolonies.client.appearance.CitizenAgeState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.UUID;

/**
 * Manages MCA data lifecycle, ticking, growth progression, and state transitions for citizens.
 */
public class CitizenMCAManager {

    public static CitizenMCAData getData(AbstractEntityCitizen citizen) {
        if (citizen instanceof IMCAEntityCitizen duck) {
            CompoundTag tag = duck.mcaminecolonies$getMCAData();
            if (tag != null && !tag.isEmpty()) {
                return CitizenMCAData.fromNbt(tag, citizen.getUUID(), citizen.isFemale(), citizen.isBaby());
            }
        }
        // Fallback default
        return CitizenMCAData.createDefault(citizen.getUUID(), citizen.isFemale(), citizen.isBaby());
    }

    public static void saveData(AbstractEntityCitizen citizen, CitizenMCAData data) {
        if (citizen instanceof IMCAEntityCitizen duck) {
            duck.mcaminecolonies$setMCAData(data.toNbt());
        }
    }

    /**
     * Ticks growth and age on server side.
     */
    public static void tickCitizen(AbstractEntityCitizen citizen) {
        if (citizen.level().isClientSide()) return;

        // Run growth update every 20 ticks (1 second)
        if (citizen.tickCount % 20 != 0) return;

        CitizenMCAData data = getData(citizen);
        boolean isChild = citizen.isBaby();

        if (isChild) {
            CitizenAgeState oldState = data.getAgeState(true);
            data.addAge(20);
            CitizenAgeState newState = data.getAgeState(true);

            // Check if grown into adult
            if (data.getAge() >= CitizenAgeState.ADULT.getMinAge()) {
                citizen.setIsChild(false);
                data.setAge(CitizenAgeState.ADULT.getMinAge());
                saveData(citizen, data);

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

            // If age stage changed (e.g. Baby -> Toddler, Toddler -> Child, Child -> Teen)
            if (oldState != newState) {
                saveData(citizen, data);
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
        }
    }
}
