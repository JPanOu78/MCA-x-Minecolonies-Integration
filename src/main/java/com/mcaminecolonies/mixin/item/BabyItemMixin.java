package com.mcaminecolonies.mixin.item;

import com.mcaminecolonies.entity.ColonyAdoptionHelper;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.item.BabyItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BabyItem.class, remap = false)
public abstract class BabyItemMixin {

    @Inject(method = "birthChild", at = @At("RETURN"))
    private void mcaminecolonies$onBirthChild(ItemStack stack, ServerLevel level, ServerPlayer player, CallbackInfoReturnable<VillagerEntityMCA> cir) {
        VillagerEntityMCA birthed = cir.getReturnValue();
        if (birthed == null || birthed.isRemoved()) return;

        IColony colony = IMinecoloniesAPI.getInstance().getColonyManager().getIColony(level, birthed.blockPosition());
        if (colony == null && player != null) {
            colony = IMinecoloniesAPI.getInstance().getColonyManager().getIColony(level, player.blockPosition());
        }
        if (colony == null && player != null) {
            colony = IMinecoloniesAPI.getInstance().getColonyManager().getIColonyByOwner(level, player);
        }

        if (colony != null) {
            ColonyAdoptionHelper.convertMCAChildToCitizen(birthed, colony, level, player);
        }
    }
}
