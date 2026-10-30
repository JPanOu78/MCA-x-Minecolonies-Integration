package com.mcaminecolonies.mixin.entity;

import com.mcaminecolonies.data.CitizenMCAData;
import com.mcaminecolonies.data.CitizenMCAManager;
import com.mcaminecolonies.data.IMCAEntityCitizen;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.util.network.datasync.CDataManager;
import net.minecraft.nbt.CompoundTag;
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
                // Load directly from root tag (or legacy MCATrackedData if present)
                if (tag.contains("MCATrackedData")) {
                    dataManager.load(ent, tag.getCompound("MCATrackedData"));
                }
                dataManager.load(ent, tag);
            }
            // Keep CitizenMCAData in sync with any changes from VillagerEditor
            if ((Object) this instanceof AbstractEntityCitizen citizen) {
                try {
                    CitizenMCAData data = CitizenMCAManager.getData(citizen);
                    if (vl.getClothes() != null && !vl.getClothes().isEmpty()) {
                        data.setCustomClothes(vl.getClothes());
                    }
                    if (vl.getHair() != null && !vl.getHair().isEmpty()) {
                        data.setCustomHair(vl.getHair());
                    }
                    if (vl.getHairDye() != 0 && vl.getHairDye() != -16777216) {
                        data.setHairColor(vl.getHairDye());
                    }
                    CitizenMCAManager.saveData(citizen, data);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
