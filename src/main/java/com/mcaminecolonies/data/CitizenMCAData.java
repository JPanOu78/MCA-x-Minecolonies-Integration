package com.mcaminecolonies.data;

import com.mcaminecolonies.client.appearance.CitizenAgeState;
import net.minecraft.nbt.CompoundTag;

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
    private String customClothes;
    private String customHair;
    private String personality;
    private int hearts;
    private boolean isCustomized;
    private UUID followTarget;

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
        this.hearts = 50;
        this.isCustomized = false;
    }

    public static CitizenMCAData createDefault(UUID uuid, boolean isFemale, boolean isChild) {
        CitizenMCAData data = new CitizenMCAData();
        data.setFemale(isFemale);

        long lsb = uuid.getLeastSignificantBits();
        long msb = uuid.getMostSignificantBits();
        int seed1 = (int) Math.abs(lsb % 1000000007L);
        int seed2 = (int) Math.abs(msb % 1000000009L);

        data.setSkinIndex(seed1 % 5);
        data.setFaceIndex(seed2 % 11);
        data.setHairIndex((seed1 / 7) % 40);
        data.setHairColor(NATURAL_HAIR_COLORS[(seed2 / 13) % NATURAL_HAIR_COLORS.length]);
        data.setClothingIndex((seed2 / 11) % 10);

        String[] personalities = {"friendly", "playful", "serious", "shy", "flirty", "brave"};
        data.setPersonality(personalities[(seed1 / 17) % personalities.length]);
        data.setHearts(50);

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
        if (this.customClothes != null) tag.putString("CustomClothes", this.customClothes);
        if (this.customHair != null) tag.putString("CustomHair", this.customHair);
        tag.putString("Personality", this.personality);
        tag.putInt("Hearts", this.hearts);
        tag.putBoolean("IsCustomized", this.isCustomized);
        if (this.followTarget != null) tag.putUUID("FollowTarget", this.followTarget);
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
        if (tag.contains("CustomClothes")) data.customClothes = tag.getString("CustomClothes");
        if (tag.contains("CustomHair")) data.customHair = tag.getString("CustomHair");
        data.personality = tag.contains("Personality") ? tag.getString("Personality") : "friendly";
        data.hearts = tag.contains("Hearts") ? tag.getInt("Hearts") : 50;
        data.isCustomized = tag.getBoolean("IsCustomized");
        if (tag.hasUUID("FollowTarget")) data.followTarget = tag.getUUID("FollowTarget");
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
        this.hearts = Math.max(0, Math.min(100, hearts));
    }

    public void addHearts(int delta) {
        setHearts(this.hearts + delta);
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

    public CitizenAgeState getAgeState(boolean isMinecoloniesChild) {
        return CitizenAgeState.fromAge(this.age, isMinecoloniesChild);
    }
}

