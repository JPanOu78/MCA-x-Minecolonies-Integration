package com.mcaminecolonies.data;

import net.minecraft.nbt.CompoundTag;

/**
 * Duck interface implemented by CitizenData to persist MCA appearance and genetics
 * directly in MineColonies colony data (colony_X.dat).
 */
public interface IMCACitizenData {

    CompoundTag mcaminecolonies$getMCAData();

    void mcaminecolonies$setMCAData(CompoundTag tag);

    boolean mcaminecolonies$hasMCAData();
}
