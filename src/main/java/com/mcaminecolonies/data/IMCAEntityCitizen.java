package com.mcaminecolonies.data;

import net.minecraft.nbt.CompoundTag;

/**
 * Interface injected onto AbstractEntityCitizen to store and retrieve MCA data.
 */
public interface IMCAEntityCitizen {
    CompoundTag mcaminecolonies$getMCAData();
    void mcaminecolonies$setMCAData(CompoundTag tag);
    boolean mcaminecolonies$hasMCAData();
}
