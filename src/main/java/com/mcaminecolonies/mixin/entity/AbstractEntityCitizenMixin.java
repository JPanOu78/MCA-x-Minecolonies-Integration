package com.mcaminecolonies.mixin.entity;

import com.mcaminecolonies.client.appearance.CitizenAgeState;
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
import net.conczin.mca.resources.HairStyleList;
import net.conczin.mca.resources.data.skin.HairStyle;
import net.conczin.mca.resources.data.skin.LayeredHair;
import net.conczin.mca.entity.ai.relationship.CompassionateEntity;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.entity.interaction.EntityCommandHandler;
import net.conczin.mca.util.network.datasync.CDataManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
    private static final CDataManager MCAMINECOLONIES$DATA_MANAGER;

    @Unique
    private static final net.minecraft.resources.ResourceLocation MCAMINECOLONIES$AGE_SPEED_ID =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("mcaminecolonies", "age_speed_modifier");

    static {
        CDataManager.Builder<AbstractEntityCitizen> builder =
                VillagerLike.createTrackedData(AbstractEntityCitizen.class);
        Genetics.createTrackedData(builder);
        Traits.createTrackedData(builder);
        MCAMINECOLONIES$DATA_MANAGER = builder.build();
    }

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

            // 1. Randomize genetics for baseline parameters
            this.mcaminecolonies$genetics.randomize();

            // 2. Sync gender with MineColonies
            AbstractEntityCitizen citizen = (AbstractEntityCitizen) (Object) this;
            this.mcaminecolonies$genetics.setGender(citizen.isFemale() ? Gender.FEMALE : Gender.MALE);

            // 3. Sync initial skin/face index, melanin, hemoglobin, and personality from CitizenMCAData
            CitizenMCAData data = CitizenMCAManager.getData(citizen);
            if (data != null) {
                this.mcaminecolonies$genetics.setGene(Genetics.SKIN, (data.getSkinIndex() + 0.5F) / 5.0F);
                this.mcaminecolonies$genetics.setGene(Genetics.FACE, (data.getFaceIndex() + 0.5F) / 11.0F);
                if (data.getMelanin() > 0.0F) {
                    this.mcaminecolonies$genetics.setGene(Genetics.MELANIN, data.getMelanin());
                }
                if (data.getHemoglobin() > 0.0F) {
                    this.mcaminecolonies$genetics.setGene(Genetics.HEMOGLOBIN, data.getHemoglobin());
                }
                if (this.mcaminecolonies$mcaBrain.getPersonality() == net.conczin.mca.entity.ai.relationship.Personality.UNASSIGNED) {
                    this.mcaminecolonies$mcaBrain.setPersonality(data.getMCAPersonality());
                }
            }

            // 4. Set tracked age state (walking child citizens use CHILD, adults use ADULT)
            if (this.getTrackedValue(VillagerLike.AGE_STATE) == AgeState.UNASSIGNED || this.getTrackedValue(VillagerLike.AGE_STATE) == AgeState.BABY) {
                this.setTrackedValue(VillagerLike.AGE_STATE, citizen.isBaby() ? AgeState.CHILD : AgeState.ADULT);
            }

            // 5. Initialize skin, clothes, and hair
            int skinIdx = (data != null) ? data.getSkinIndex() : 0;
            String gender = citizen.isFemale() ? "female" : "male";
            if (this.getSkin() == null || this.getSkin().isEmpty()) {
                if (data != null && data.getCustomSkin() != null && !data.getCustomSkin().isEmpty()) {
                    this.setSkin(data.getCustomSkin());
                } else {
                    this.setSkin("mca:skins/skin/" + gender + "/" + (skinIdx % 5) + ".png");
                }
            }
            if (this.getClothes() == null || this.getClothes().isEmpty() || this.getClothes().contains("/baby/")) {
                if (data != null && data.getCustomClothes() != null && !data.getCustomClothes().isEmpty()) {
                    this.setClothes(data.getCustomClothes());
                } else {
                    int cIdx = (data != null) ? data.getClothingIndex() : 0;
                    this.setClothes("mca:skins/clothing/normal/" + gender + "/none/" + (cIdx % 5) + ".png");
                }
            }
            if (this.getHair() == null || this.getHair().isEmpty()) {
                if (data != null && data.getCustomHair() != null && !data.getCustomHair().isEmpty()) {
                    this.setHair(data.getCustomHair());
                } else {
                    int hIdx = (data != null) ? data.getHairIndex() : 0;
                    this.setHair("mca:skins/hair/" + gender + "/" + (hIdx % 40) + ".png");
                }
            }

            // Sync modern hairstyle & hair dye
            if (data != null) {
                String sId = data.resolveHairStyle(citizen.isFemale());
                if (sId != null && !sId.isEmpty() && (this.getHairStyleId() == null || this.getHairStyleId().isEmpty())) {
                    this.setHairStyleId(sId);
                    try {
                        HairStyle hs = HairStyleList.getInstance().get(sId);
                        if (hs != null) {
                            this.setHairStyle(hs);
                        }
                    } catch (Exception ignored) {
                    }
                }
                if (data.getHairColor() != 0 && data.getHairColor() != -16777216) {
                    this.setHairDye(data.getHairColor());
                }
            }

            // 6. Natural dye defaults (-1 for eyes = derive from genetics, -16777216 for skin/hair = natural)
            if (this.getEyeDye() == 0) {
                this.clearEyeDye();
            }
            if (this.getEyeLeftDye() == 0) {
                this.clearEyeLeftDye();
            }
            if (this.getSkinDye() == 0) {
                this.clearSkinDye();
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
        if (this.tickCount % 100 == 0) {
            this.updateAttributes();
        }
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
        AbstractEntityCitizen citizen = (AbstractEntityCitizen) (Object) this;
        CitizenMCAData data = CitizenMCAManager.getData(citizen);
        if (data != null) {
            com.mcaminecolonies.client.appearance.CitizenAgeState state = data.getAgeState(citizen.isBaby());
            return switch (state) {
                case BABY, TODDLER -> AgeState.TODDLER;
                case CHILD -> AgeState.CHILD;
                case TEEN -> AgeState.TEEN;
                case ADULT -> AgeState.ADULT;
            };
        }
        return citizen.isBaby() ? AgeState.CHILD : AgeState.ADULT;
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
            CompoundTag nbt = data.toNbt();
            tag.put("MCAData", nbt);
            tag.put("MCAColoniesData", nbt);
            if (data.getCustomSkin() != null) tag.putString("Skin", data.getCustomSkin());
            tag.putInt("SkinDye", data.getSkinDye());
            tag.putInt("SkinColor", data.getSkinDye());
            int hairDye = (data.getHairColor() != 0 && data.getHairColor() != -16777216) ? data.getHairColor() : this.getHairDye();
            tag.putInt("HairColor", hairDye);
            tag.putInt("HairDye", hairDye);
            if (data.getHairStyle() != null) {
                tag.putString("HairStyle", data.getHairStyle());
            } else if (this.getHairStyleId() != null && !this.getHairStyleId().isEmpty()) {
                tag.putString("HairStyle", this.getHairStyleId());
            }
            tag.putInt("EyeDye", this.getEyeDye());
            tag.putInt("EyeColor", this.getEyeDye());
            tag.putInt("EyeColorLeft", this.getEyeLeftDye());
            tag.putInt("EyeLeftDye", this.getEyeLeftDye());
            AbstractEntityCitizen citizen = (AbstractEntityCitizen) (Object) this;
            int mcaAge = citizen.isBaby()
                    ? (int) (((float) data.getAge() / (float) CitizenAgeState.ADULT.getMinAge() - 1.0F) * AgeState.getMaxAge())
                    : 0;
            tag.putInt("Age", mcaAge);
            tag.putInt("AgeState", this.getAgeState().ordinal());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag == null) return;
        if (tag.contains("MCAData", 10)) {
            this.mcaminecolonies$setMCAData(tag.getCompound("MCAData"));
        } else if (tag.contains("MCAColoniesData", 10)) {
            this.mcaminecolonies$setMCAData(tag.getCompound("MCAColoniesData"));
        }
        mcaminecolonies$initMCA();
        MCAMINECOLONIES$DATA_MANAGER.load(this, tag);
        if (this.mcaminecolonies$relations != null) {
            this.mcaminecolonies$relations.readFromNbt(tag);
        }

        AbstractEntityCitizen citizen = (AbstractEntityCitizen) (Object) this;
        CitizenMCAData data = CitizenMCAManager.getData(citizen);

        if (tag.contains("Age", 99)) {
            int ageVal = tag.getInt("Age");
            boolean isBaby = ageVal < 0;
            citizen.setIsChild(isBaby);
            citizen.getEntityData().set(AbstractEntityCitizen.DATA_IS_CHILD, isBaby);
            if (citizen.getCitizenData() != null) {
                citizen.getCitizenData().setIsChild(isBaby);
            }
            if (isBaby) {
                float progress = 1.0F + (float) ageVal / (float) AgeState.getMaxAge();
                progress = Math.clamp(progress, 0.0F, 0.99F);
                int citizenAge = (int) (progress * CitizenAgeState.ADULT.getMinAge());
                data.setAge(citizenAge);
            } else {
                data.setAge(CitizenAgeState.ADULT.getMinAge());
            }
            AgeState mcaAgeState = AgeState.byCurrentAge(ageVal);
            this.setTrackedValue(VillagerLike.AGE_STATE, mcaAgeState);
            this.refreshDimensions();
            this.updateAttributes();
        } else if (tag.contains("AgeState", 99)) {
            int asIdx = tag.getInt("AgeState");
            AgeState state = AgeState.byId(asIdx);
            boolean isBaby = (state != AgeState.ADULT && state != AgeState.UNASSIGNED);
            citizen.setIsChild(isBaby);
            citizen.getEntityData().set(AbstractEntityCitizen.DATA_IS_CHILD, isBaby);
            if (citizen.getCitizenData() != null) {
                citizen.getCitizenData().setIsChild(isBaby);
            }
            if (isBaby) {
                data.setAge(switch (state) {
                    case BABY, TODDLER -> 1000;
                    case CHILD -> 8000;
                    case TEEN -> 14000;
                    default -> 0;
                });
            } else {
                data.setAge(CitizenAgeState.ADULT.getMinAge());
            }
            this.setTrackedValue(VillagerLike.AGE_STATE, state);
            this.refreshDimensions();
            this.updateAttributes();
        } else if (tag.contains("AgeState", 8)) {
            String asStr = tag.getString("AgeState");
            try {
                AgeState state = AgeState.valueOf(asStr.toUpperCase());
                boolean isBaby = (state != AgeState.ADULT && state != AgeState.UNASSIGNED);
                citizen.setIsChild(isBaby);
                citizen.getEntityData().set(AbstractEntityCitizen.DATA_IS_CHILD, isBaby);
                if (citizen.getCitizenData() != null) {
                    citizen.getCitizenData().setIsChild(isBaby);
                }
                if (isBaby) {
                    data.setAge(switch (state) {
                        case BABY, TODDLER -> 1000;
                        case CHILD -> 8000;
                        case TEEN -> 14000;
                        default -> 0;
                    });
                } else {
                    data.setAge(CitizenAgeState.ADULT.getMinAge());
                }
                this.setTrackedValue(VillagerLike.AGE_STATE, state);
                this.refreshDimensions();
                this.updateAttributes();
            } catch (Exception ignored) {
            }
        }

        if (tag.contains("Clothes", 8)) {
            String clothes = tag.getString("Clothes");
            this.setClothes(clothes);
        }
        if (tag.contains("Hair", 8)) {
            String hair = tag.getString("Hair");
            this.setHair(hair);
        }
        if (tag.contains("HairStyle", 8)) {
            String style = tag.getString("HairStyle");
            this.setHairStyleId(style);
            try {
                HairStyle hs = HairStyleList.getInstance().get(style);
                if (hs != null) {
                    this.setHairStyle(hs);
                }
            } catch (Exception ignored) {
            }
            if (style.endsWith(".png")) {
                data.setCustomHair(style);
            } else {
                data.setCustomHair(null);
            }
            data.setHairStyle(style);
        }
        if (tag.contains("HairBase", 8)) this.setLayeredHair(LayeredHair.Category.BASE, tag.getString("HairBase"));
        if (tag.contains("HairBangs", 8)) this.setLayeredHair(LayeredHair.Category.BANGS, tag.getString("HairBangs"));
        if (tag.contains("HairBack", 8)) this.setLayeredHair(LayeredHair.Category.BACK, tag.getString("HairBack"));
        if (tag.contains("HairFront", 8)) this.setLayeredHair(LayeredHair.Category.FRONT, tag.getString("HairFront"));
        if (tag.contains("HairExtra", 8)) this.setLayeredHair(LayeredHair.Category.EXTRA, tag.getString("HairExtra"));

        if (tag.contains("Skin", 8)) {
            String skin = tag.getString("Skin");
            data.setCustomSkin(skin);
            this.setSkin(skin);
        } else if (tag.contains("Skin", 99)) {
            float skinGene = tag.getFloat("Skin");
            int sIdx = (int) Math.min(4.0F, Math.max(0.0F, skinGene * 5.0F));
            data.setSkinIndex(sIdx);
            data.setCustomSkin(null);
            if (this.mcaminecolonies$genetics != null) {
                this.mcaminecolonies$genetics.setGene(Genetics.SKIN, skinGene);
            }
        } else if (tag.contains("GeneSkin", 99)) {
            float skinGene = tag.getFloat("GeneSkin");
            int sIdx = (int) Math.min(4.0F, Math.max(0.0F, skinGene * 5.0F));
            data.setSkinIndex(sIdx);
            data.setCustomSkin(null);
            if (this.mcaminecolonies$genetics != null) {
                this.mcaminecolonies$genetics.setGene(Genetics.SKIN, skinGene);
            }
        }

        if (tag.contains("Face", 99)) {
            float faceGene = tag.getFloat("Face");
            int fIdx = (int) Math.min(10.0F, Math.max(0.0F, faceGene * 11.0F));
            data.setFaceIndex(fIdx);
            if (this.mcaminecolonies$genetics != null) {
                this.mcaminecolonies$genetics.setGene(Genetics.FACE, (fIdx + 0.5F) / 11.0F);
            }
        } else if (tag.contains("GeneFace", 99)) {
            float faceGene = tag.getFloat("GeneFace");
            int fIdx = (int) Math.min(10.0F, Math.max(0.0F, faceGene * 11.0F));
            data.setFaceIndex(fIdx);
            if (this.mcaminecolonies$genetics != null) {
                this.mcaminecolonies$genetics.setGene(Genetics.FACE, (fIdx + 0.5F) / 11.0F);
            }
        }

        if (tag.contains("Melanin", 99)) {
            float m = tag.getFloat("Melanin");
            data.setMelanin(m);
            if (this.mcaminecolonies$genetics != null) {
                this.mcaminecolonies$genetics.setGene(Genetics.MELANIN, m);
            }
        } else if (tag.contains("GeneMelanin", 99)) {
            float m = tag.getFloat("GeneMelanin");
            data.setMelanin(m);
            if (this.mcaminecolonies$genetics != null) {
                this.mcaminecolonies$genetics.setGene(Genetics.MELANIN, m);
            }
        }

        if (tag.contains("Hemoglobin", 99)) {
            float h = tag.getFloat("Hemoglobin");
            data.setHemoglobin(h);
            if (this.mcaminecolonies$genetics != null) {
                this.mcaminecolonies$genetics.setGene(Genetics.HEMOGLOBIN, h);
            }
        } else if (tag.contains("GeneHemoglobin", 99)) {
            float h = tag.getFloat("GeneHemoglobin");
            data.setHemoglobin(h);
            if (this.mcaminecolonies$genetics != null) {
                this.mcaminecolonies$genetics.setGene(Genetics.HEMOGLOBIN, h);
            }
        }

        if (tag.contains("SkinColor", 99)) {
            int sc = tag.getInt("SkinColor");
            data.setSkinDye(sc);
            this.setSkinDye(sc);
        } else if (tag.contains("SkinDye", 99)) {
            int sc = tag.getInt("SkinDye");
            data.setSkinDye(sc);
            this.setSkinDye(sc);
        }

        if (tag.contains("HairColor", 99)) {
            int hc = tag.getInt("HairColor");
            data.setHairColor(hc);
            this.setHairDye(hc);
        } else if (tag.contains("HairDye", 99)) {
            int hc = tag.getInt("HairDye");
            data.setHairColor(hc);
            this.setHairDye(hc);
        } else if (tag.contains("HairColorRed", 99) && tag.contains("HairColorGreen", 99) && tag.contains("HairColorBlue", 99)) {
            float r = tag.getFloat("HairColorRed");
            float g = tag.getFloat("HairColorGreen");
            float b = tag.getFloat("HairColorBlue");
            int ir = Math.round(r * 255.0F) & 0xFF;
            int ig = Math.round(g * 255.0F) & 0xFF;
            int ib = Math.round(b * 255.0F) & 0xFF;
            int hc = 0xFF000000 | (ir << 16) | (ig << 8) | ib;
            data.setHairColor(hc);
            this.setHairDye(hc);
        }

        if (tag.contains("EyeColor", 99)) {
            this.setEyeDye(tag.getInt("EyeColor"));
        } else if (tag.contains("EyeDye", 99)) {
            this.setEyeDye(tag.getInt("EyeDye"));
        }
        if (tag.contains("EyeColorLeft", 99)) {
            this.setEyeLeftDye(tag.getInt("EyeColorLeft"));
        } else if (tag.contains("EyeLeftDye", 99)) {
            this.setEyeLeftDye(tag.getInt("EyeLeftDye"));
        }

        if (tag.contains("Gender", 99)) {
            boolean female = (tag.getInt("Gender") == Gender.FEMALE.ordinal());
            data.setFemale(female);
            citizen.setFemale(female);
            if (this.mcaminecolonies$genetics != null) {
                this.mcaminecolonies$genetics.setGender(female ? Gender.FEMALE : Gender.MALE);
            }
            if (citizen.getCitizenData() != null) {
                citizen.getCitizenData().setGender(female);
            }
        } else if (tag.contains("Gender", 8)) {
            boolean female = "FEMALE".equalsIgnoreCase(tag.getString("Gender"));
            data.setFemale(female);
            citizen.setFemale(female);
            if (this.mcaminecolonies$genetics != null) {
                this.mcaminecolonies$genetics.setGender(female ? Gender.FEMALE : Gender.MALE);
            }
            if (citizen.getCitizenData() != null) {
                citizen.getCitizenData().setGender(female);
            }
        }

        CitizenMCAManager.saveData(citizen, data);
    }

    public void readAdditionalSaveDataForEditor(CompoundTag tag) {
        this.readAdditionalSaveData(tag);
    }

    @Override
    public void syncFromEditor(CompoundTag tag) {
        this.readAdditionalSaveData(tag);
        AbstractEntityCitizen citizen = (AbstractEntityCitizen) (Object) this;
        CitizenMCAData data = CitizenMCAManager.getData(citizen);
        if (data != null) {
            if (tag.contains("Clothes", 8)) {
                String clothes = tag.getString("Clothes");
                data.setCustomClothes(clothes);
                this.setClothes(clothes);
            }
            if (tag.contains("Hair", 8)) {
                String hair = tag.getString("Hair");
                if (hair.endsWith(".png")) {
                    data.setCustomHair(hair);
                }
                this.setHair(hair);
            }
            data.setCustomized(true);
            CitizenMCAManager.saveData(citizen, data);
        }
        if (tag.contains("CustomName", 8)) {
            String nameStr = tag.getString("CustomName");
            if (!nameStr.isBlank()) {
                try {
                    Component comp = Component.Serializer.fromJson(nameStr, this.registryAccess());
                    if (comp != null) {
                        this.setCustomName(comp);
                        if (citizen.getCitizenData() != null) {
                            citizen.getCitizenData().setName(comp.getString());
                        }
                    }
                } catch (Exception e) {
                    this.setCustomName(Component.literal(nameStr));
                    if (citizen.getCitizenData() != null) {
                        citizen.getCitizenData().setName(nameStr);
                    }
                }
            }
        }
    }

    @Override
    public void updateAttributes() {
        var attr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null) {
            attr.removeModifier(VillagerLike.SPEED_ID);
            attr.removeModifier(MCAMINECOLONIES$AGE_SPEED_ID);

            AbstractEntityCitizen citizen = (AbstractEntityCitizen) (Object) this;
            CitizenMCAData data = CitizenMCAManager.getData(citizen);
            if (data != null && citizen.isBaby()) {
                com.mcaminecolonies.client.appearance.CitizenAgeState state = data.getAgeState(true);
                float speedMultiplier = state.getSpeedMultiplier();
                if (Math.abs(speedMultiplier - 1.0F) > 0.01F) {
                    attr.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                            MCAMINECOLONIES$AGE_SPEED_ID,
                            speedMultiplier - 1.0F,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ));
                }
            }
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
