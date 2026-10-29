package com.mcaminecolonies.mixin.entity;

import com.mcaminecolonies.data.IMCAEntityCitizen;
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
 * Mixin on EntityCitizen to persist MCA data across world saves.
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
            CompoundTag mcaTracked = new CompoundTag();
            CDataManager dataManager = vl.getTypeDataManager();
            if (dataManager != null) {
                dataManager.save(ent, mcaTracked);
                tag.put("MCATrackedData", mcaTracked);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void mcaminecolonies$loadMCAData(CompoundTag tag, CallbackInfo ci) {
        if ((Object) this instanceof IMCAEntityCitizen duck && tag.contains("MCAData")) {
            duck.mcaminecolonies$setMCAData(tag.getCompound("MCAData"));
        }
        if ((Object) this instanceof VillagerLike vl && (Object) this instanceof Entity ent && tag.contains("MCATrackedData")) {
            CDataManager dataManager = vl.getTypeDataManager();
            if (dataManager != null) {
                dataManager.load(ent, tag.getCompound("MCATrackedData"));
            }
        }
    }
}
