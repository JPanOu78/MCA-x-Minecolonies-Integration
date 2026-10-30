package com.mcaminecolonies.mixin.entity;

import com.mcaminecolonies.data.CitizenMCAData;
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

            // 1. Randomize genetics to initialize all gene parameters
            this.mcaminecolonies$genetics.randomize();

            // 2. Sync gender with MineColonies
            AbstractEntityCitizen citizen = (AbstractEntityCitizen) (Object) this;
            this.mcaminecolonies$genetics.setGender(citizen.isFemale() ? Gender.FEMALE : Gender.MALE);

            // 3. Sync initial skin/face index from seed data if not customized
            CitizenMCAData data = CitizenMCAManager.getData(citizen);
            if (data != null && !data.isCustomized()) {
                this.mcaminecolonies$genetics.setGene(Genetics.SKIN, (data.getSkinIndex() + 0.5F) / 5.0F);
                this.mcaminecolonies$genetics.setGene(Genetics.FACE, (data.getFaceIndex() + 0.5F) / 22.0F);
            }

            // 4. Set tracked age state (walking child citizens use CHILD, adults use ADULT)
            if (this.getTrackedValue(VillagerLike.AGE_STATE) == AgeState.UNASSIGNED || this.getTrackedValue(VillagerLike.AGE_STATE) == AgeState.BABY) {
                this.setTrackedValue(VillagerLike.AGE_STATE, citizen.isBaby() ? AgeState.CHILD : AgeState.ADULT);
            }

            // 5. Randomize clothes and hair if empty or if previously assigned baby diaper clothing
            if (this.getClothes() == null || this.getClothes().isEmpty() || this.getClothes().contains("/baby/")) {
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
        AgeState state = (AgeState) this.getTrackedValue(VillagerLike.AGE_STATE);
        if (state == null || state == AgeState.UNASSIGNED) {
            AbstractEntityCitizen citizen = (AbstractEntityCitizen) (Object) this;
            return citizen.isBaby() ? AgeState.CHILD : AgeState.ADULT;
        }
        if (state == AgeState.BABY) {
            return AgeState.CHILD;
        }
        return state;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        mcaminecolonies$initMCA();
        MCAMINECOLONIES$DATA_MANAGER.save(this, tag);
        if (this.mcaminecolonies$relations != null) {
            this.mcaminecolonies$relations.writeToNbt(tag);
        }
        CitizenMCAData data = CitizenMCAManager.getData((AbstractEntityCitizen) (Object) this);
        if (data != null) {
            tag.put("MCAColoniesData", data.toNbt());
            if (data.getCustomSkin() != null) tag.putString("Skin", data.getCustomSkin());
            tag.putInt("SkinDye", data.getSkinDye());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag == null) return;
        mcaminecolonies$initMCA();
        MCAMINECOLONIES$DATA_MANAGER.load(this, tag);
        if (this.mcaminecolonies$relations != null) {
            this.mcaminecolonies$relations.readFromNbt(tag);
        }

        AbstractEntityCitizen citizen = (AbstractEntityCitizen) (Object) this;
        CitizenMCAData data = CitizenMCAManager.getData(citizen);
        if (tag.contains("Clothes")) data.setCustomClothes(tag.getString("Clothes"));
        if (tag.contains("Hair")) data.setCustomHair(tag.getString("Hair"));
        if (tag.contains("Skin")) data.setCustomSkin(tag.getString("Skin"));
        if (tag.contains("SkinDye")) data.setSkinDye(tag.getInt("SkinDye"));
        int hairDye = this.getHairDye();
        if (hairDye != 0 && hairDye != -16777216) data.setHairColor(hairDye);
        if (tag.contains("Gender")) {
            boolean female = "FEMALE".equalsIgnoreCase(tag.getString("Gender"));
            data.setFemale(female);
        }
        data.setCustomized(true);
        CitizenMCAManager.saveData(citizen, data);
    }

    @Override
    public void syncFromEditor(CompoundTag tag) {
        VillagerLike.super.syncFromEditor(tag);
        this.readAdditionalSaveData(tag);
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
