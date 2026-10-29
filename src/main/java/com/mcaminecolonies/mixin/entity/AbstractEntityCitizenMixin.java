package com.mcaminecolonies.mixin.entity;

import com.mcaminecolonies.data.CitizenMCAManager;
import com.mcaminecolonies.data.IMCAEntityCitizen;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin on AbstractEntityCitizen providing MCA synched data storage, duck typing, and growth ticking.
 */
@Mixin(value = AbstractEntityCitizen.class, remap = false)
public abstract class AbstractEntityCitizenMixin extends PathfinderMob implements IMCAEntityCitizen {

    @Unique
    private static final EntityDataAccessor<CompoundTag> MCAMINECOLONIES$MCA_DATA =
            SynchedEntityData.defineId(AbstractEntityCitizen.class, EntityDataSerializers.COMPOUND_TAG);

    protected AbstractEntityCitizenMixin(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void mcaminecolonies$defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(MCAMINECOLONIES$MCA_DATA, new CompoundTag());
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void mcaminecolonies$aiStep(CallbackInfo ci) {
        CitizenMCAManager.tickCitizen((AbstractEntityCitizen) (Object) this);
    }

    @Override
    public CompoundTag mcaminecolonies$getMCAData() {
        return this.entityData.get(MCAMINECOLONIES$MCA_DATA);
    }

    @Override
    public void mcaminecolonies$setMCAData(CompoundTag tag) {
        this.entityData.set(MCAMINECOLONIES$MCA_DATA, tag);
    }

    @Override
    public boolean mcaminecolonies$hasMCAData() {
        CompoundTag tag = mcaminecolonies$getMCAData();
        return tag != null && !tag.isEmpty();
    }
}
