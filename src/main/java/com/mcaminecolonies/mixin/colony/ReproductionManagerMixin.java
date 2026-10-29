package com.mcaminecolonies.mixin.colony;

import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.colony.managers.ReproductionManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin on ReproductionManager enhancing colony child birth with MCA Reborn birth mechanics.
 */
@Mixin(value = ReproductionManager.class, remap = false)
public abstract class ReproductionManagerMixin {

    @Shadow
    @Final
    private Colony colony;

    @Inject(method = "trySpawnChild", at = @At("TAIL"))
    private void mcaminecolonies$onChildBirth(CallbackInfo ci) {
        if (this.colony != null && this.colony.getWorld() != null) {
            // Play MCA-style baby birth chime at colony center
            this.colony.getWorld().playSound(
                    null,
                    this.colony.getCenter().getX(),
                    this.colony.getCenter().getY(),
                    this.colony.getCenter().getZ(),
                    SoundEvents.PLAYER_LEVELUP,
                    SoundSource.NEUTRAL,
                    0.7F,
                    1.4F
            );
        }
    }
}
