package com.mcaminecolonies.client.appearance;

/**
 * Represents the growth stages of a citizen, aligned with MCA Reborn age progression.
 */
public enum CitizenAgeState {
    BABY(0, 2400, 0.42F, 1.25F, "Baby", "baby"),
    TODDLER(2400, 6000, 0.58F, 1.20F, "Toddler", "toddler"),
    CHILD(6000, 12000, 0.72F, 1.15F, "Child", "child"),
    TEEN(12000, 18000, 0.88F, 1.05F, "Teen", "none"),
    ADULT(18000, Integer.MAX_VALUE, 1.00F, 1.00F, "Adult", "adult");

    private final int minAge;
    private final int maxAge;
    private final float bodyScale;
    private final float headScale;
    private final String displayName;
    private final String clothingFolder;

    CitizenAgeState(int minAge, int maxAge, float bodyScale, float headScale, String displayName, String clothingFolder) {
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.bodyScale = bodyScale;
        this.headScale = headScale;
        this.displayName = displayName;
        this.clothingFolder = clothingFolder;
    }

    public int getMinAge() {
        return minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public float getBodyScale() {
        return bodyScale;
    }

    public float getHeadScale() {
        return headScale;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getClothingFolder() {
        return clothingFolder;
    }

    public boolean isAdult() {
        return this == ADULT;
    }

    public static CitizenAgeState fromAge(int age, boolean isMinecoloniesChild) {
        if (!isMinecoloniesChild && age >= ADULT.minAge) {
            return ADULT;
        }
        if (age < TODDLER.minAge) {
            return BABY;
        } else if (age < CHILD.minAge) {
            return TODDLER;
        } else if (age < TEEN.minAge) {
            return CHILD;
        } else if (age < ADULT.minAge || isMinecoloniesChild) {
            return TEEN;
        }
        return ADULT;
    }
}
