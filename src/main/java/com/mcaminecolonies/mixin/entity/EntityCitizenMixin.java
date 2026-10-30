package com.mcaminecolonies.mixin.entity;

import com.mcaminecolonies.data.CitizenMCAData;
import com.mcaminecolonies.data.CitizenMCAManager;
import com.mcaminecolonies.data.IMCAEntityCitizen;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.Genetics;
import net.conczin.mca.resources.data.skin.LayeredHair;
import net.conczin.mca.util.network.datasync.CDataManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin on EntityCitizen to persist MCA data across world saves and serialize
 * root tags for MCA VillagerEditor and network synchronization.
 */
@Mixin(value = EntityCitizen.class, remap = false)
public abstract class EntityCitizenMixin {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void mcaminecolonies$saveMCAData(CompoundTag tag, CallbackInfo ci) {
        if ((Object) this instanceof IMCAEntityCitizen duck) {
            CompoundTag mcaTag = duck.mcaminecolonies$getMCAData();
            if (mcaTag != null && !mcaTag.isEmpty()) {
                tag.put("MCAData", mcaTag);
            }
        }
        if ((Object) this instanceof VillagerLike vl && (Object) this instanceof Entity ent) {
            CDataManager dataManager = vl.getTypeDataManager();
            if (dataManager != null) {
                // Save directly to root tag so GetVillagerRequest and VillagerEditorScreen load Clothes, Hair, Genetics
                dataManager.save(ent, tag);
            }
            if (vl.getGenetics() != null && vl.getGenetics().getGender() != null) {
                tag.putString("Gender", vl.getGenetics().getGender().name());
            }
            if (vl.getAgeState() != null) {
                tag.putInt("AgeState", vl.getAgeState().ordinal());
            }
            if (ent instanceof AbstractEntityCitizen cit && cit.getCustomName() != null) {
                try {
                    // Modern MCA syncFamilyTree parses CustomName as JSON Component
                    tag.putString("CustomName", Component.Serializer.toJson(cit.getCustomName(), ent.registryAccess()));
                } catch (Exception e) {
                    tag.putString("CustomName", cit.getCustomName().getString());
                }
            }

            // Explicitly sync root visual tags for MCA GetVillagerResponse
            if (vl.getSkin() != null && !vl.getSkin().isEmpty()) {
                tag.putString("Skin", vl.getSkin());
            }
            if (vl.getClothes() != null && !vl.getClothes().isEmpty()) {
                tag.putString("Clothes", vl.getClothes());
            }
            if (vl.getHair() != null && !vl.getHair().isEmpty()) {
                tag.putString("Hair", vl.getHair());
            }
            if (vl.getHairStyleId() != null && !vl.getHairStyleId().isEmpty()) {
                tag.putString("HairStyle", vl.getHairStyleId());
            }
            if (vl.getSkinDye() != -16777216) {
                tag.putInt("SkinColor", vl.getSkinDye());
                tag.putInt("SkinDye", vl.getSkinDye());
            }
            if (vl.getHairDye() != -16777216) {
                tag.putInt("HairColor", vl.getHairDye());
                tag.putInt("HairDye", vl.getHairDye());
            }
            if (vl.getEyeDye() != -1) {
                tag.putInt("EyeColor", vl.getEyeDye());
                tag.putInt("EyeDye", vl.getEyeDye());
            }
            if (vl.getEyeLeftDye() != -1) {
                tag.putInt("EyeColorLeft", vl.getEyeLeftDye());
                tag.putInt("EyeLeftDye", vl.getEyeLeftDye());
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void mcaminecolonies$loadMCAData(CompoundTag tag, CallbackInfo ci) {
        if ((Object) this instanceof IMCAEntityCitizen duck && tag.contains("MCAData")) {
            duck.mcaminecolonies$setMCAData(tag.getCompound("MCAData"));
        }
        if ((Object) this instanceof VillagerLike vl && (Object) this instanceof Entity ent) {
            CDataManager dataManager = vl.getTypeDataManager();
            if (dataManager != null) {
                if (tag.contains("MCATrackedData")) {
                    dataManager.load(ent, tag.getCompound("MCATrackedData"));
                }
                dataManager.load(ent, tag);
            }
            // Keep CitizenMCAData in sync with any changes from VillagerEditor
            if ((Object) this instanceof AbstractEntityCitizen citizen) {
                try {
                    CitizenMCAData data = CitizenMCAManager.getData(citizen);

                    if (tag.contains("Skin", 8)) {
                        String skin = tag.getString("Skin");
                        data.setCustomSkin(skin);
                        vl.setSkin(skin);
                    }
                    if (tag.contains("Clothes", 8)) {
                        String clothes = tag.getString("Clothes");
                        data.setCustomClothes(clothes);
                        vl.setClothes(clothes);
                    } else if (vl.getClothes() != null && !vl.getClothes().isEmpty()) {
                        data.setCustomClothes(vl.getClothes());
                    }

                    if (tag.contains("Hair", 8)) {
                        String hair = tag.getString("Hair");
                        data.setCustomHair(hair);
                        vl.setHair(hair);
                    } else if (vl.getHair() != null && !vl.getHair().isEmpty()) {
                        data.setCustomHair(vl.getHair());
                    }

                    if (tag.contains("SkinDye", 99)) {
                        int sc = tag.getInt("SkinDye");
                        data.setSkinDye(sc);
                        vl.setSkinDye(sc);
                    } else if (tag.contains("SkinColor", 99)) {
                        int sc = tag.getInt("SkinColor");
                        data.setSkinDye(sc);
                        vl.setSkinDye(sc);
                    }

                    if (tag.contains("HairDye", 99)) {
                        int hc = tag.getInt("HairDye");
                        data.setHairColor(hc);
                        vl.setHairDye(hc);
                    } else if (tag.contains("HairColor", 99)) {
                        int hc = tag.getInt("HairColor");
                        data.setHairColor(hc);
                        vl.setHairDye(hc);
                    }

                    if (tag.contains("EyeColor", 99)) {
                        vl.setEyeDye(tag.getInt("EyeColor"));
                    } else if (tag.contains("EyeDye", 99)) {
                        vl.setEyeDye(tag.getInt("EyeDye"));
                    }
                    if (tag.contains("EyeColorLeft", 99)) {
                        vl.setEyeLeftDye(tag.getInt("EyeColorLeft"));
                    } else if (tag.contains("EyeLeftDye", 99)) {
                        vl.setEyeLeftDye(tag.getInt("EyeLeftDye"));
                    }

                    if (tag.contains("HairBase", 8)) vl.setLayeredHair(LayeredHair.Category.BASE, tag.getString("HairBase"));
                    if (tag.contains("HairBangs", 8)) vl.setLayeredHair(LayeredHair.Category.BANGS, tag.getString("HairBangs"));
                    if (tag.contains("HairBack", 8)) vl.setLayeredHair(LayeredHair.Category.BACK, tag.getString("HairBack"));
                    if (tag.contains("HairFront", 8)) vl.setLayeredHair(LayeredHair.Category.FRONT, tag.getString("HairFront"));
                    if (tag.contains("HairExtra", 8)) vl.setLayeredHair(LayeredHair.Category.EXTRA, tag.getString("HairExtra"));

                    if (vl.getGenetics() != null) {
                        float mGene = tag.contains("GeneMelanin", 99) ? tag.getFloat("GeneMelanin") : (tag.contains("Melanin", 99) ? tag.getFloat("Melanin") : vl.getGenetics().getGene(Genetics.MELANIN));
                        if (mGene > 0.0F) {
                            data.setMelanin(mGene);
                            vl.getGenetics().setGene(Genetics.MELANIN, mGene);
                        }
                        float hGene = tag.contains("GeneHemoglobin", 99) ? tag.getFloat("GeneHemoglobin") : (tag.contains("Hemoglobin", 99) ? tag.getFloat("Hemoglobin") : vl.getGenetics().getGene(Genetics.HEMOGLOBIN));
                        if (hGene > 0.0F) {
                            data.setHemoglobin(hGene);
                            vl.getGenetics().setGene(Genetics.HEMOGLOBIN, hGene);
                        }
                        float fGene = tag.contains("GeneFace", 99) ? tag.getFloat("GeneFace") : (tag.contains("Face", 99) ? tag.getFloat("Face") : vl.getGenetics().getGene(Genetics.FACE));
                        if (fGene > 0.0F) {
                            data.setFaceIndex((int) Math.min(10.0F, Math.max(0.0F, fGene * 11.0F)));
                            vl.getGenetics().setGene(Genetics.FACE, fGene);
                        }
                        float sGene = tag.contains("GeneSkin", 99) ? tag.getFloat("GeneSkin") : (tag.contains("Skin", 99) ? tag.getFloat("Skin") : vl.getGenetics().getGene(Genetics.SKIN));
                        if (sGene > 0.0F) {
                            data.setSkinIndex((int) Math.min(4.0F, Math.max(0.0F, sGene * 5.0F)));
                            vl.getGenetics().setGene(Genetics.SKIN, sGene);
                        }
                        if (vl.getGenetics().getGender() != null && vl.getGenetics().getGender() != net.conczin.mca.entity.ai.relationship.Gender.UNASSIGNED) {
                            boolean fem = (vl.getGenetics().getGender() == net.conczin.mca.entity.ai.relationship.Gender.FEMALE);
                            data.setFemale(fem);
                            citizen.setFemale(fem);
                        }
                    }
                    data.setCustomized(true);
                    CitizenMCAManager.saveData(citizen, data);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
