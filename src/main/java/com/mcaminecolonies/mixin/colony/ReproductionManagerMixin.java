package com.mcaminecolonies.mixin.colony;

import com.mcaminecolonies.data.CitizenMCAData;
import com.mcaminecolonies.data.CitizenMCAManager;
import com.mcaminecolonies.entity.CitizenRelationshipHelper;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.colony.managers.ReproductionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin on ReproductionManager enhancing colony child birth with MCA Reborn birth mechanics,
 * FamilyTree registration, and parent genetic inheritance.
 */
@Mixin(value = ReproductionManager.class, remap = false)
public abstract class ReproductionManagerMixin {

    @Shadow
    @Final
    private Colony colony;

    @Inject(method = "trySpawnChild", at = @At("TAIL"))
    private void mcaminecolonies$onChildBirth(CallbackInfo ci) {
        if (this.colony != null && this.colony.getWorld() instanceof ServerLevel serverLevel) {
            try {
                if (this.colony.getCitizenManager() != null) {
                    for (ICitizenData cData : this.colony.getCitizenManager().getCitizens()) {
                        if (cData == null || cData.getEntity().isEmpty()) continue;
                        AbstractEntityCitizen parentEntity = cData.getEntity().get();
                        CitizenRelationshipHelper.syncColonyFamilyTree(parentEntity, serverLevel);

                        // Inherit genetics from parents to newly born children
                        if (cData.getChildren() != null && !cData.getChildren().isEmpty()) {
                            CitizenMCAData parentMca = CitizenMCAManager.getData(parentEntity);
                            ICitizenData partnerData = cData.getPartner();
                            CitizenMCAData partnerMca = (partnerData != null && partnerData.getEntity().isPresent())
                                    ? CitizenMCAManager.getData(partnerData.getEntity().get())
                                    : parentMca;

                            for (Integer childId : cData.getChildren()) {
                                if (childId == null) continue;
                                ICitizenData childData = this.colony.getCitizenManager().getCivilian(childId);
                                if (childData != null && childData.isChild() && childData.getEntity().isPresent()) {
                                    AbstractEntityCitizen childEntity = childData.getEntity().get();
                                    CitizenMCAData childMca = CitizenMCAManager.getData(childEntity);
                                    if (childMca != null && parentMca != null && childMca.getAge() > -23900) {
                                        float blendedMelanin = (parentMca.getMelanin() + (partnerMca != null ? partnerMca.getMelanin() : parentMca.getMelanin())) * 0.5F;
                                        float blendedHemoglobin = (parentMca.getHemoglobin() + (partnerMca != null ? partnerMca.getHemoglobin() : parentMca.getHemoglobin())) * 0.5F;
                                        childMca.setMelanin(blendedMelanin);
                                        childMca.setHemoglobin(blendedHemoglobin);
                                        childMca.setHairColor(serverLevel.random.nextBoolean() || partnerMca == null ? parentMca.getHairColor() : partnerMca.getHairColor());
                                        childMca.setSkinIndex(serverLevel.random.nextBoolean() || partnerMca == null ? parentMca.getSkinIndex() : partnerMca.getSkinIndex());
                                        CitizenMCAManager.saveData(childEntity, childMca);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            // Play MCA-style baby birth chime at colony center
            this.colony.getWorld().playSound(
                    null,
                    this.colony.getCenter().getX(),
                    this.colony.getCenter().getY(),
                    this.colony.getCenter().getZ(),
                    SoundEvents.PLAYER_LEVELUP,
                    SoundSource.NEUTRAL,
                    0.7F,
                    1.4F
            );
        }
    }
}

