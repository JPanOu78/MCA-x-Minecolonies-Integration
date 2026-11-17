package com.mcaminecolonies.data;

import com.mcaminecolonies.client.appearance.CitizenAgeState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Stores MCA-related state, appearance attributes, and growth data for a MineColonies citizen.
 */
public class CitizenMCAData {

    private int age;
    private boolean isFemale;
    private int skinIndex;
    private int hairIndex;
    private int faceIndex;
    private int hairColor;
    private int clothingIndex;
    private String customSkin;
    private int skinDye = -16777216;
    private float melanin = 0.18F;
    private float hemoglobin = 0.15F;
    private String customClothes;
    private String customHair;
    private String hairStyle;
    private boolean clothingLocked;
    private int eyeColor = -1;
    private int eyeColorLeft = -1;
    private float breast = 0.5F;
    private float size = 0.5F;
    private float width = 0.5F;
    private float eyeBrightness = 0.5F;
    private int mood;
    private boolean armorWear = true;
    private String hairBase;
    private String hairBangs;
    private String hairBack;
    private String hairFront;
    private String hairExtra;
    private final Set<String> traits = new HashSet<>();
    private String personality;
    private int hearts;
    private int fatigue;
    private boolean isCustomized;
    private UUID followTarget;
    private String moveState = "MOVE";
    private boolean inherited;

    public static final int DEFAULT_HAIR_COLOR = 0xFF4A2A18; // Natural Brown
    public static final int[] NATURAL_HAIR_COLORS = {
        0xFF1E1B1B, // Black
        0xFF4A2A18, // Dark Brown
        0xFF835432, // Chestnut Brown
        0xFFD8A050, // Blonde
        0xFFB04A26, // Ginger / Red
        0xFF9E8774, // Ash Brown
        0xFFE0E0E0  // Platinum / Grey
    };

    public CitizenMCAData() {
        this.age = CitizenAgeState.ADULT.getMinAge();
        this.isFemale = false;
        this.skinIndex = 0;
        this.hairIndex = 0;
        this.faceIndex = 0;
        this.hairColor = DEFAULT_HAIR_COLOR;
        this.clothingIndex = 0;
        this.personality = "friendly";
        this.hearts = 0;
        this.isCustomized = false;
    }

    public static CitizenMCAData createDefault(UUID uuid, boolean isFemale, boolean isChild) {
        return createDefault(uuid, 0, 0, isFemale, isChild);
    }

    public static CitizenMCAData createDefault(UUID uuid, int colonyId, int citizenId, boolean isFemale, boolean isChild) {
        CitizenMCAData data = new CitizenMCAData();
        data.setFemale(isFemale);

        int seed1;
        int seed2;
        if (citizenId > 0) {
            long seed = ((long) colonyId * 31L + (long) citizenId) * 0x5DEECE66DL + 0xBL;
            seed1 = (int) Math.abs((seed ^ (seed >>> 16)) % 1000000007L);
            seed2 = (int) Math.abs((seed ^ (seed >>> 32)) % 1000000009L);
        } else {
            long lsb = uuid != null ? uuid.getLeastSignificantBits() : 0L;
            long msb = uuid != null ? uuid.getMostSignificantBits() : 0L;
            seed1 = (int) Math.abs(lsb % 1000000007L);
            seed2 = (int) Math.abs(msb % 1000000009L);
        }

        data.setSkinIndex(seed1 % 5);
        data.setFaceIndex(seed2 % 11);
        data.setHairIndex((seed1 / 7) % 40);
        data.setHairColor(NATURAL_HAIR_COLORS[(seed2 / 13) % NATURAL_HAIR_COLORS.length]);
        data.setClothingIndex((seed2 / 11) % 10);

        // Deterministic initial hairstyle selection from MCA HairStyleList
        try {
            net.conczin.mca.resources.HairStyleList hsl = net.conczin.mca.resources.HairStyleList.getInstance();
            if (hsl != null) {
                net.conczin.mca.entity.ai.relationship.Gender mcaGender = isFemale
                        ? net.conczin.mca.entity.ai.relationship.Gender.FEMALE
                        : net.conczin.mca.entity.ai.relationship.Gender.MALE;
                java.util.List<net.conczin.mca.resources.data.skin.HairStyle> styles = hsl.getStyles(mcaGender);
                if (styles != null && !styles.isEmpty()) {
                    int sIdx = Math.abs((seed1 / 7) % styles.size());
                    data.setHairStyle(styles.get(sIdx).getIdentifier());
                }
            }
        } catch (Exception ignored) {
        }

        // Baseline genetics
        data.breast = 0.30F + ((seed1 % 40) / 100.0F); // 0.30 - 0.70
        data.size = 0.40F + ((seed2 % 25) / 100.0F);  // 0.40 - 0.65
        data.width = 0.40F + ((seed1 % 25) / 100.0F); // 0.40 - 0.65
        data.eyeBrightness = 0.35F + ((seed2 % 35) / 100.0F);

        // Rare trait rolls (deterministic from seed)
        if (seed1 % 100 == 0) {
            data.traits.add("mca:albinism");
        } else if (seed2 % 50 == 0) {
            data.traits.add("mca:heterochromia");
        }

        // Generate natural baseline melanin & hemoglobin
        float naturalMelanin = 0.12F + ((seed1 % 100) / 100.0F) * 0.15F; // range 0.12F to 0.27F
        float naturalHemoglobin = 0.10F + ((seed2 % 100) / 100.0F) * 0.12F; // range 0.10F to 0.22F
        data.setMelanin(naturalMelanin);
        data.setHemoglobin(naturalHemoglobin);

        String[] personalities = {"friendly", "playful", "serious", "shy", "flirty", "brave"};
        data.setPersonality(personalities[(seed1 / 17) % personalities.length]);
        data.setHearts(0);

        if (isChild) {
            data.setAge(0); // newborn baby
        } else {
            data.setAge(CitizenAgeState.ADULT.getMinAge());
        }

        return data;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Age", this.age);
        tag.putBoolean("IsFemale", this.isFemale);
        tag.putInt("SkinIndex", this.skinIndex);
        tag.putInt("HairIndex", this.hairIndex);
        tag.putInt("FaceIndex", this.faceIndex);
        tag.putInt("HairColor", this.hairColor);
        tag.putInt("ClothingIndex", this.clothingIndex);
        if (this.customSkin != null) tag.putString("CustomSkin", this.customSkin);
        tag.putInt("SkinDye", this.skinDye);
        tag.putFloat("Melanin", this.melanin);
        tag.putFloat("Hemoglobin", this.hemoglobin);
        if (this.customClothes != null) tag.putString("CustomClothes", this.customClothes);
        if (this.customHair != null) tag.putString("CustomHair", this.customHair);
        if (this.hairStyle != null) tag.putString("HairStyle", this.hairStyle);
        tag.putBoolean("ClothingLocked", this.clothingLocked);
        tag.putInt("EyeColor", this.eyeColor);
        tag.putInt("EyeColorLeft", this.eyeColorLeft);
        tag.putFloat("Breast", this.breast);
        tag.putFloat("Size", this.size);
        tag.putFloat("Width", this.width);
        tag.putFloat("EyeBrightness", this.eyeBrightness);
        tag.putInt("Mood", this.mood);
        tag.putBoolean("ArmorWear", this.armorWear);
        if (this.hairBase != null) tag.putString("HairBase", this.hairBase);
        if (this.hairBangs != null) tag.putString("HairBangs", this.hairBangs);
        if (this.hairBack != null) tag.putString("HairBack", this.hairBack);
        if (this.hairFront != null) tag.putString("HairFront", this.hairFront);
        if (this.hairExtra != null) tag.putString("HairExtra", this.hairExtra);
        if (!this.traits.isEmpty()) {
            ListTag list = new ListTag();
            for (String t : this.traits) {
                list.add(StringTag.valueOf(t));
            }
            tag.put("Traits", list);
        }
        tag.putString("Personality", this.personality);
        tag.putInt("Hearts", this.hearts);
        tag.putInt("Fatigue", this.fatigue);
        tag.putBoolean("IsCustomized", this.isCustomized);
        tag.putBoolean("Inherited", this.inherited);
        if (this.followTarget != null) tag.putUUID("FollowTarget", this.followTarget);
        if (this.moveState != null) tag.putString("MoveState", this.moveState);
        return tag;
    }

    public static CitizenMCAData fromNbt(CompoundTag tag, UUID fallbackUuid, boolean fallbackFemale, boolean fallbackChild) {
        if (tag == null || tag.isEmpty()) {
            return createDefault(fallbackUuid, fallbackFemale, fallbackChild);
        }
        CitizenMCAData data = new CitizenMCAData();
        data.age = tag.contains("Age") ? tag.getInt("Age") : (fallbackChild ? 0 : CitizenAgeState.ADULT.getMinAge());
        data.isFemale = tag.contains("IsFemale") ? tag.getBoolean("IsFemale") : fallbackFemale;
        data.skinIndex = tag.getInt("SkinIndex");
        data.hairIndex = tag.getInt("HairIndex");
        data.faceIndex = tag.getInt("FaceIndex");
        data.hairColor = tag.contains("HairColor") ? tag.getInt("HairColor") : DEFAULT_HAIR_COLOR;
        data.clothingIndex = tag.getInt("ClothingIndex");
        if (tag.contains("CustomSkin")) data.customSkin = tag.getString("CustomSkin");
        if (tag.contains("SkinDye")) data.skinDye = tag.getInt("SkinDye");
        if (tag.contains("HairStyle")) data.hairStyle = tag.getString("HairStyle");
        if (tag.contains("ClothingLocked")) data.clothingLocked = tag.getBoolean("ClothingLocked");
        if (tag.contains("EyeColor", 99)) data.eyeColor = tag.getInt("EyeColor");
        if (tag.contains("EyeColorLeft", 99)) data.eyeColorLeft = tag.getInt("EyeColorLeft");
        if (tag.contains("Breast", 99)) data.breast = tag.getFloat("Breast");
        if (tag.contains("Size", 99)) data.size = tag.getFloat("Size");
        if (tag.contains("Width", 99)) data.width = tag.getFloat("Width");
        if (tag.contains("EyeBrightness", 99)) data.eyeBrightness = tag.getFloat("EyeBrightness");
        if (tag.contains("Mood", 99)) data.mood = tag.getInt("Mood");
        if (tag.contains("ArmorWear")) data.armorWear = tag.getBoolean("ArmorWear");
        if (tag.contains("HairBase")) data.hairBase = tag.getString("HairBase");
        if (tag.contains("HairBangs")) data.hairBangs = tag.getString("HairBangs");
        if (tag.contains("HairBack")) data.hairBack = tag.getString("HairBack");
        if (tag.contains("HairFront")) data.hairFront = tag.getString("HairFront");
        if (tag.contains("HairExtra")) data.hairExtra = tag.getString("HairExtra");
        if (tag.contains("Traits", 9)) {
            ListTag list = tag.getList("Traits", 8);
            for (int i = 0; i < list.size(); i++) {
                data.traits.add(list.getString(i));
            }
        }
        if (tag.contains("Melanin")) data.melanin = tag.getFloat("Melanin");
        if (tag.contains("Hemoglobin")) data.hemoglobin = tag.getFloat("Hemoglobin");

        // Legacy auto-upgrade: if citizen was saved with the old buggy 0.5F defaults and is not customized,
        // upgrade to natural seed-based genetics so citizens do not render with dark muddy skin
        if (!data.isCustomized && Math.abs(data.melanin - 0.5F) < 0.001F && Math.abs(data.hemoglobin - 0.5F) < 0.001F) {
            long lsb = fallbackUuid.getLeastSignificantBits();
            long msb = fallbackUuid.getMostSignificantBits();
            int seed1 = (int) Math.abs(lsb % 1000000007L);
            int seed2 = (int) Math.abs(msb % 1000000009L);
            data.melanin = 0.12F + ((seed1 % 100) / 100.0F) * 0.15F;
            data.hemoglobin = 0.10F + ((seed2 % 100) / 100.0F) * 0.12F;
        }
        if (tag.contains("CustomClothes")) data.customClothes = tag.getString("CustomClothes");
        if (tag.contains("CustomHair")) data.customHair = tag.getString("CustomHair");
        data.personality = tag.contains("Personality") ? tag.getString("Personality") : "friendly";
        data.hearts = tag.contains("Hearts") ? tag.getInt("Hearts") : 0;
        data.fatigue = tag.contains("Fatigue") ? tag.getInt("Fatigue") : 0;
        data.inherited = tag.contains("Inherited") && tag.getBoolean("Inherited");
        data.isCustomized = tag.getBoolean("IsCustomized");
        if (tag.hasUUID("FollowTarget")) data.followTarget = tag.getUUID("FollowTarget");
        data.moveState = tag.contains("MoveState") ? tag.getString("MoveState") : "MOVE";
        return data;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = Math.max(0, age);
    }

    public void addAge(int ticks) {
        this.age = Math.max(0, this.age + ticks);
    }

    public boolean isFemale() {
        return isFemale;
    }

    public void setFemale(boolean female) {
        this.isFemale = female;
    }

    public int getSkinIndex() {
        return skinIndex;
    }

    public void setSkinIndex(int skinIndex) {
        this.skinIndex = (skinIndex % 5 + 5) % 5;
    }

    public int getHairIndex() {
        return hairIndex;
    }

    public void setHairIndex(int hairIndex) {
        this.hairIndex = (hairIndex % 40 + 40) % 40;
    }

    public int getFaceIndex() {
        return faceIndex;
    }

    public void setFaceIndex(int faceIndex) {
        this.faceIndex = (faceIndex % 11 + 11) % 11;
    }

    public float getMelanin() {
        return melanin;
    }

    public void setMelanin(float melanin) {
        this.melanin = Math.clamp(melanin, 0.0F, 1.0F);
    }

    public float getHemoglobin() {
        return hemoglobin;
    }

    public void setHemoglobin(float hemoglobin) {
        this.hemoglobin = Math.clamp(hemoglobin, 0.0F, 1.0F);
    }

    public int getHairColor() {
        return hairColor;
    }

    public void setHairColor(int hairColor) {
        this.hairColor = hairColor;
    }

    public int getClothingIndex() {
        return clothingIndex;
    }

    public void setClothingIndex(int clothingIndex) {
        this.clothingIndex = (clothingIndex % 10 + 10) % 10;
    }

    public String getCustomSkin() {
        return customSkin;
    }

    public void setCustomSkin(String customSkin) {
        this.customSkin = customSkin;
    }

    public int getSkinDye() {
        return skinDye;
    }

    public void setSkinDye(int skinDye) {
        this.skinDye = skinDye;
    }

    public String getCustomClothes() {
        return customClothes;
    }

    public void setCustomClothes(String customClothes) {
        this.customClothes = customClothes;
    }

    public String getCustomHair() {
        return customHair;
    }

    public void setCustomHair(String customHair) {
        this.customHair = customHair;
    }

    public String getHairStyle() {
        return hairStyle;
    }

    public void setHairStyle(String hairStyle) {
        this.hairStyle = hairStyle;
    }

    public String resolveHairStyle(boolean isFemale) {
        if (this.hairStyle != null && !this.hairStyle.isEmpty()) {
            return this.hairStyle;
        }
        try {
            net.conczin.mca.resources.HairStyleList hsl = net.conczin.mca.resources.HairStyleList.getInstance();
            if (hsl != null) {
                net.conczin.mca.entity.ai.relationship.Gender mcaGender = isFemale
                        ? net.conczin.mca.entity.ai.relationship.Gender.FEMALE
                        : net.conczin.mca.entity.ai.relationship.Gender.MALE;
                java.util.List<net.conczin.mca.resources.data.skin.HairStyle> styles = hsl.getStyles(mcaGender);
                if (styles != null && !styles.isEmpty()) {
                    int sIdx = Math.abs(this.hairIndex % styles.size());
                    this.hairStyle = styles.get(sIdx).getIdentifier();
                    return this.hairStyle;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    public int getHearts() {
        return hearts;
    }

    public void setHearts(int hearts) {
        this.hearts = Math.max(-100, Math.min(1000, hearts));
    }

    public void addHearts(int delta) {
        setHearts(this.hearts + delta);
    }

    public int getFatigue() {
        return fatigue;
    }

    public void setFatigue(int fatigue) {
        this.fatigue = Math.max(0, fatigue);
    }

    public void modFatigue(int delta) {
        setFatigue(this.fatigue + delta);
    }

    public boolean isCustomized() {
        return isCustomized;
    }

    public void setCustomized(boolean customized) {
        this.isCustomized = customized;
    }

    public UUID getFollowTarget() {
        return followTarget;
    }

    public void setFollowTarget(UUID followTarget) {
        this.followTarget = followTarget;
    }

    public String getMoveState() {
        return moveState != null ? moveState : "MOVE";
    }

    public void setMoveState(String moveState) {
        this.moveState = moveState != null ? moveState : "MOVE";
    }

    public net.conczin.mca.entity.ai.relationship.Personality getMCAPersonality() {
        if (this.personality == null) {
            return net.conczin.mca.entity.ai.relationship.Personality.FRIENDLY;
        }
        return switch (this.personality.toLowerCase()) {
            case "playful" -> net.conczin.mca.entity.ai.relationship.Personality.PLAYFUL;
            case "serious" -> net.conczin.mca.entity.ai.relationship.Personality.RELAXED;
            case "shy" -> net.conczin.mca.entity.ai.relationship.Personality.INTROVERTED;
            case "flirty" -> net.conczin.mca.entity.ai.relationship.Personality.FLIRTY;
            case "brave" -> net.conczin.mca.entity.ai.relationship.Personality.UPBEAT;
            case "gloomy" -> net.conczin.mca.entity.ai.relationship.Personality.GLOOMY;
            case "sensitive" -> net.conczin.mca.entity.ai.relationship.Personality.SENSITIVE;
            case "greedy" -> net.conczin.mca.entity.ai.relationship.Personality.GREEDY;
            case "crabby" -> net.conczin.mca.entity.ai.relationship.Personality.CRABBY;
            case "extroverted" -> net.conczin.mca.entity.ai.relationship.Personality.EXTROVERTED;
            default -> net.conczin.mca.entity.ai.relationship.Personality.FRIENDLY;
        };
    }

    public CitizenAgeState getAgeState(boolean isMinecoloniesChild) {
        return CitizenAgeState.fromAge(this.age, isMinecoloniesChild);
    }

    public boolean isClothingLocked() {
        return clothingLocked;
    }

    public void setClothingLocked(boolean clothingLocked) {
        this.clothingLocked = clothingLocked;
    }

    public int getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(int eyeColor) {
        this.eyeColor = eyeColor;
    }

    public int getEyeColorLeft() {
        return eyeColorLeft;
    }

    public void setEyeColorLeft(int eyeColorLeft) {
        this.eyeColorLeft = eyeColorLeft;
    }

    public float getBreast() {
        return breast;
    }

    public void setBreast(float breast) {
        this.breast = Math.max(0.0F, Math.min(1.0F, breast));
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = Math.max(0.0F, Math.min(1.0F, size));
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = Math.max(0.0F, Math.min(1.0F, width));
    }

    public float getEyeBrightness() {
        return eyeBrightness;
    }

    public void setEyeBrightness(float eyeBrightness) {
        this.eyeBrightness = Math.max(0.0F, Math.min(1.0F, eyeBrightness));
    }

    public int getMood() {
        return mood;
    }

    public void setMood(int mood) {
        this.mood = Math.max(-100, Math.min(100, mood));
    }

    public boolean isArmorWear() {
        return armorWear;
    }

    public void setArmorWear(boolean armorWear) {
        this.armorWear = armorWear;
    }

    public String getHairBase() {
        return hairBase;
    }

    public void setHairBase(String hairBase) {
        this.hairBase = hairBase;
    }

    public String getHairBangs() {
        return hairBangs;
    }

    public void setHairBangs(String hairBangs) {
        this.hairBangs = hairBangs;
    }

    public String getHairBack() {
        return hairBack;
    }

    public void setHairBack(String hairBack) {
        this.hairBack = hairBack;
    }

    public String getHairFront() {
        return hairFront;
    }

    public void setHairFront(String hairFront) {
        this.hairFront = hairFront;
    }

    public String getHairExtra() {
        return hairExtra;
    }

    public void setHairExtra(String hairExtra) {
        this.hairExtra = hairExtra;
    }

    public Set<String> getTraits() {
        return traits;
    }

    public void addTrait(String trait) {
        if (trait != null && !trait.isBlank()) {
            this.traits.add(trait);
        }
    }

    public void removeTrait(String trait) {
        this.traits.remove(trait);
    }

    public boolean hasTrait(String trait) {
        return this.traits.contains(trait);
    }

    public void setTraits(Set<String> newTraits) {
        this.traits.clear();
        if (newTraits != null) {
            this.traits.addAll(newTraits);
        }
    }

    public boolean isInherited() {
        return inherited;
    }

    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }
}

