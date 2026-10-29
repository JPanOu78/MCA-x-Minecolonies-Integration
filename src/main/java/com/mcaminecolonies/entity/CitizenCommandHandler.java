package com.mcaminecolonies.entity;

import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.interaction.EntityCommandHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

/**
 * EntityCommandHandler implementation for MineColonies citizens interacting with MCA.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CitizenCommandHandler extends EntityCommandHandler {

    public CitizenCommandHandler(Mob entity) {
        super(entity);
    }

    @Override
    public boolean handle(ServerPlayer player, String command) {
        return false;
    }
}
