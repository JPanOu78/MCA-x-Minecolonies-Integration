package com.mcaminecolonies.mixin.colony;

import com.mcaminecolonies.data.IMCACitizenData;
import com.mcaminecolonies.data.IMCAEntityCitizen;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.colony.CitizenData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Mixin on CitizenData to persist MCA appearance, genetics, and customization directly inside
 * MineColonies colony save files (colony_X.dat). This ensures citizen appearance remains
 * permanent even across entity respawns, recalls, and world reloads.
 */
@Mixin(value = CitizenData.class, remap = false)
public abstract class CitizenDataMixin implements IMCACitizenData {

    @Shadow
    public abstract Optional<AbstractEntityCitizen> getEntity();

    @Unique
    private CompoundTag mcaminecolonies$mcaData;

    @Inject(method = "serializeNBT(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"))
    private void onSerializeNBT(HolderLookup.Provider provider, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag tag = cir.getReturnValue();
        if (tag != null) {
            // Pull the latest MCA appearance data from the active entity if available
            this.getEntity().ifPresent(cit -> {
                if (cit instanceof IMCAEntityCitizen duck) {
                    CompoundTag active = duck.mcaminecolonies$getMCAData();
                    if (active != null && !active.isEmpty()) {
                        this.mcaminecolonies$mcaData = active.copy();
                    }
                }
            });
            if (this.mcaminecolonies$mcaData != null && !this.mcaminecolonies$mcaData.isEmpty()) {
                tag.put("MCAData", this.mcaminecolonies$mcaData.copy());
            }
        }
    }

    @Inject(method = "deserializeNBT(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void onDeserializeNBT(HolderLookup.Provider provider, CompoundTag tag, CallbackInfo ci) {
        if (tag != null) {
            if (tag.contains("MCAData", 10)) {
                this.mcaminecolonies$mcaData = tag.getCompound("MCAData");
            } else if (tag.contains("MCAColoniesData", 10)) {
                this.mcaminecolonies$mcaData = tag.getCompound("MCAColoniesData");
            }
        }
    }

    @Inject(method = "initEntityValues", at = @At("TAIL"))
    private void onInitEntityValues(CallbackInfo ci) {
        if (this.mcaminecolonies$mcaData != null && !this.mcaminecolonies$mcaData.isEmpty()) {
            this.getEntity().ifPresent(cit -> {
                if (cit instanceof IMCAEntityCitizen duck) {
                    duck.mcaminecolonies$setMCAData(this.mcaminecolonies$mcaData.copy());
                }
            });
        }
    }

    @Override
    public CompoundTag mcaminecolonies$getMCAData() {
        return this.mcaminecolonies$mcaData;
    }

    @Override
    public void mcaminecolonies$setMCAData(CompoundTag tag) {
        this.mcaminecolonies$mcaData = tag;
    }

    @Override
    public boolean mcaminecolonies$hasMCAData() {
        return this.mcaminecolonies$mcaData != null && !this.mcaminecolonies$mcaData.isEmpty();
    }
}
