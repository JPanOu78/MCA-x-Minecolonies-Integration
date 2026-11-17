package com.mcaminecolonies.mixin.colony;

import com.mcaminecolonies.data.CitizenMCAData;
import com.mcaminecolonies.data.CitizenMCAManager;
import com.mcaminecolonies.entity.CitizenRelationshipHelper;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenSkillHandler;
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

import java.util.HashSet;
import java.util.Set;

/**
 * Mixin on ReproductionManager enhancing colony child birth with MCA Reborn birth mechanics,
 * FamilyTree registration, parent genetic inheritance, and stat synergies.
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

                        // Inherit genetics and stats from parents to newly born children
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
                                    if (childMca != null && parentMca != null && !childMca.isInherited()) {
                                        // 1. Appearance & Physical Genetics Blending
                                        float blendedMelanin = (parentMca.getMelanin() + (partnerMca != null ? partnerMca.getMelanin() : parentMca.getMelanin())) * 0.5F;
                                        float blendedHemoglobin = (parentMca.getHemoglobin() + (partnerMca != null ? partnerMca.getHemoglobin() : parentMca.getHemoglobin())) * 0.5F;
                                        childMca.setMelanin(blendedMelanin);
                                        childMca.setHemoglobin(blendedHemoglobin);
                                        childMca.setHairColor(serverLevel.random.nextBoolean() || partnerMca == null ? parentMca.getHairColor() : partnerMca.getHairColor());
                                        childMca.setSkinIndex(serverLevel.random.nextBoolean() || partnerMca == null ? parentMca.getSkinIndex() : partnerMca.getSkinIndex());

                                        // Height/Size & Body Width
                                        float avgSize = (parentMca.getSize() + (partnerMca != null ? partnerMca.getSize() : parentMca.getSize())) * 0.5F;
                                        float avgWidth = (parentMca.getWidth() + (partnerMca != null ? partnerMca.getWidth() : parentMca.getWidth())) * 0.5F;
                                        avgSize += (serverLevel.random.nextFloat() - 0.5F) * 0.08F;
                                        avgWidth += (serverLevel.random.nextFloat() - 0.5F) * 0.08F;
                                        childMca.setSize(Math.max(0.2F, Math.min(0.8F, avgSize)));
                                        childMca.setWidth(Math.max(0.2F, Math.min(0.8F, avgWidth)));

                                        // Eye Color & Brightness
                                        int eye = serverLevel.random.nextBoolean() || partnerMca == null ? parentMca.getEyeColor() : parentMca.getEyeColor();
                                        childMca.setEyeColor(eye);
                                        childMca.setEyeColorLeft(eye);
                                        float avgBright = (parentMca.getEyeBrightness() + (partnerMca != null ? partnerMca.getEyeBrightness() : parentMca.getEyeBrightness())) * 0.5F;
                                        childMca.setEyeBrightness(avgBright);

                                        // 2. Inherit MCA Traits
                                        Set<String> childTraits = new HashSet<>(childMca.getTraits());
                                        if (parentMca.getTraits() != null) {
                                            for (String trait : parentMca.getTraits()) {
                                                if (serverLevel.random.nextFloat() < 0.60F) childTraits.add(trait);
                                            }
                                        }
                                        if (partnerMca != null && partnerMca.getTraits() != null) {
                                            for (String trait : partnerMca.getTraits()) {
                                                if (serverLevel.random.nextFloat() < 0.60F) childTraits.add(trait);
                                            }
                                        }
                                        childMca.setTraits(childTraits);

                                        // 3. Inherit MineColonies Skills & Apply Trait Synergies
                                        ICitizenSkillHandler cSkills = childData.getCitizenSkillHandler();
                                        ICitizenSkillHandler p1Skills = cData.getCitizenSkillHandler();
                                        if (cSkills != null && p1Skills != null) {
                                            ICitizenSkillHandler p2Skills = (partnerData != null && partnerData.getCitizenSkillHandler() != null)
                                                    ? partnerData.getCitizenSkillHandler()
                                                    : p1Skills;

                                            for (Skill skill : Skill.values()) {
                                                int p1Level = p1Skills.getLevel(skill);
                                                int p2Level = p2Skills.getLevel(skill);
                                                // 15% of parents' average skill level passed as inherited bonus (capped at +5)
                                                int inheritedBonus = (int) Math.round(((p1Level + p2Level) / 2.0) * 0.15);
                                                if (inheritedBonus > 0) {
                                                    cSkills.incrementLevel(skill, Math.min(inheritedBonus, 5));
                                                }
                                            }

                                            // Apply Trait synergies to skills
                                            if (childTraits.contains("mca:athletic") || childTraits.contains("athletic")) {
                                                cSkills.incrementLevel(Skill.Athletics, 2);
                                                cSkills.incrementLevel(Skill.Agility, 1);
                                                cSkills.incrementLevel(Skill.Stamina, 1);
                                            }
                                            if (childTraits.contains("mca:tough") || childTraits.contains("tough")) {
                                                cSkills.incrementLevel(Skill.Strength, 2);
                                                cSkills.incrementLevel(Skill.Stamina, 1);
                                            }
                                            if (childTraits.contains("mca:weak") || childTraits.contains("weak")) {
                                                if (cSkills.getLevel(Skill.Strength) > 1) {
                                                    cSkills.incrementLevel(Skill.Strength, -1);
                                                }
                                            }
                                        }

                                        childMca.setInherited(true);
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

