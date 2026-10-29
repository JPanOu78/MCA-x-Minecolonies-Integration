package com.mcaminecolonies.mixin.entity;

import com.mcaminecolonies.data.IMCAEntityCitizen;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin on EntityCitizen to persist MCA data across world saves.
 */
@Mixin(value = EntityCitizen.class, remap = false)
public abstract class EntityCitizenMixin {

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void mcaminecolonies$saveMCAData(CompoundTag tag, CallbackInfo ci) {
        if (this instanceof IMCAEntityCitizen duck) {
            CompoundTag mcaTag = duck.mcaminecolonies$getMCAData();
            if (mcaTag != null && !mcaTag.isEmpty()) {
                tag.put("MCAData", mcaTag);
            }
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void mcaminecolonies$loadMCAData(CompoundTag tag, CallbackInfo ci) {
        if (this instanceof IMCAEntityCitizen duck && tag.contains("MCAData")) {
            duck.mcaminecolonies$setMCAData(tag.getCompound("MCAData"));
        }
    }
}
