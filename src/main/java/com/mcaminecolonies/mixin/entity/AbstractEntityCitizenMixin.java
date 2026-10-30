package com.mcaminecolonies.mixin.entity;

import com.mcaminecolonies.data.CitizenMCAManager;
import com.mcaminecolonies.data.IMCAEntityCitizen;
import com.mcaminecolonies.entity.CitizenCommandHandler;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.Genetics;
import net.conczin.mca.entity.ai.Relationship;
import net.conczin.mca.entity.ai.Traits;
import net.conczin.mca.entity.ai.brain.VillagerBrain;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.conczin.mca.entity.ai.relationship.CompassionateEntity;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.entity.interaction.EntityCommandHandler;
import net.conczin.mca.util.network.datasync.CDataManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin on AbstractEntityCitizen providing MCA synched data storage, duck typing,
 * growth ticking, and implementing VillagerLike + CompassionateEntity for native MCA dialogue & interactions.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(value = AbstractEntityCitizen.class, remap = false)
public abstract class AbstractEntityCitizenMixin extends PathfinderMob implements
        IMCAEntityCitizen,
        VillagerLike<AbstractEntityCitizenMixin>,
        CompassionateEntity<Relationship<AbstractEntityCitizenMixin>> {

    @Unique
    private static final EntityDataAccessor<CompoundTag> MCAMINECOLONIES$MCA_DATA =
            SynchedEntityData.defineId(AbstractEntityCitizen.class, EntityDataSerializers.COMPOUND_TAG);

    @Unique
    private static final CDataManager MCAMINECOLONIES$DATA_MANAGER =
            VillagerLike.createTrackedData(AbstractEntityCitizen.class).build();

    @Unique
    private Relationship<AbstractEntityCitizenMixin> mcaminecolonies$relations;
    @Unique
    private VillagerBrain<AbstractEntityCitizenMixin> mcaminecolonies$mcaBrain;
    @Unique
    private Genetics mcaminecolonies$genetics;
    @Unique
    private Traits mcaminecolonies$traits;
    @Unique
    private CitizenCommandHandler mcaminecolonies$interactions;
    @Unique
    private VillagerData mcaminecolonies$villagerData;

    protected AbstractEntityCitizenMixin(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private void mcaminecolonies$initMCA() {
        if (this.mcaminecolonies$relations == null) {
            this.mcaminecolonies$relations = new Relationship<>(this);
            this.mcaminecolonies$mcaBrain = new VillagerBrain<>(this);
            this.mcaminecolonies$genetics = new Genetics(this);
            this.mcaminecolonies$traits = new Traits(this);
            this.mcaminecolonies$interactions = new CitizenCommandHandler(this);
            this.mcaminecolonies$villagerData = new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1);

            // Sync gender with MineColonies
            AbstractEntityCitizen citizen = (AbstractEntityCitizen) (Object) this;
            this.mcaminecolonies$genetics.setGender(citizen.isFemale() ? Gender.FEMALE : Gender.MALE);

            // Set tracked age state
            if (this.getTrackedValue(VillagerLike.AGE_STATE) == AgeState.UNASSIGNED) {
                this.setTrackedValue(VillagerLike.AGE_STATE, citizen.isBaby() ? AgeState.BABY : AgeState.ADULT);
            }

            // Randomize clothes and hair if empty so models and Villager Editor preview render completely
            if (this.getClothes() == null || this.getClothes().isEmpty()) {
                try {
                    this.randomizeClothes();
                } catch (Exception ignored) {
                }
            }
            if (this.getHair() == null || this.getHair().isEmpty()) {
                try {
                    this.randomizeHair();
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void mcaminecolonies$defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(MCAMINECOLONIES$MCA_DATA, new CompoundTag());
        MCAMINECOLONIES$DATA_MANAGER.register(builder);
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void mcaminecolonies$aiStep(CallbackInfo ci) {
        CitizenMCAManager.tickCitizen((AbstractEntityCitizen) (Object) this);
    }

    @Override
    public CDataManager getTypeDataManager() {
        return MCAMINECOLONIES$DATA_MANAGER;
    }

    @Override
    public Relationship<AbstractEntityCitizenMixin> getRelationships() {
        mcaminecolonies$initMCA();
        return this.mcaminecolonies$relations;
    }

    @Override
    public VillagerBrain<?> getVillagerBrain() {
        mcaminecolonies$initMCA();
        return this.mcaminecolonies$mcaBrain;
    }

    @Override
    public Genetics getGenetics() {
        mcaminecolonies$initMCA();
        return this.mcaminecolonies$genetics;
    }

    @Override
    public Traits getTraits() {
        mcaminecolonies$initMCA();
        return this.mcaminecolonies$traits;
    }

    @Override
    public EntityCommandHandler<?> getInteractions() {
        mcaminecolonies$initMCA();
        return this.mcaminecolonies$interactions;
    }

    @Override
    public VillagerData getVillagerData() {
        mcaminecolonies$initMCA();
        return this.mcaminecolonies$villagerData;
    }

    @Override
    public void setVillagerData(VillagerData data) {
        this.mcaminecolonies$villagerData = data;
    }

    @Override
    public AgeState getAgeState() {
        if (this.isBaby()) {
            return AgeState.BABY;
        }
        return AgeState.ADULT;
    }

    @Override
    public void updateAttributes() {
        // Suppress MCA's speed modifiers to prevent abrupt rushing or dashing bursts.
        // MineColonies manages citizen navigation and movement speed independently.
        var attr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null) {
            attr.removeModifier(VillagerLike.SPEED_ID);
        }
    }

    @Override
    public float getInfectionProgress() {
        return 0.0F;
    }

    @Override
    public void setInfectionProgress(float p) {
    }

    @Override
    public boolean isBurned() {
        return false;
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
