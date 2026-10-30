package com.mcaminecolonies.client.appearance;

/**
 * Represents the growth stages of a citizen, aligned with MCA Reborn age progression.
 */
public enum CitizenAgeState {
    BABY(0, 2400, 0.45F, 1.30F, 0.50F, "Baby", "baby"),
    TODDLER(0, 6000, 0.58F, 1.25F, 0.65F, "Toddler", "toddler"),
    CHILD(6000, 12000, 0.72F, 1.15F, 0.90F, "Child", "child"),
    TEEN(12000, 18000, 0.88F, 1.05F, 1.05F, "Teen", "none"),
    ADULT(18000, Integer.MAX_VALUE, 1.00F, 1.00F, 1.00F, "Adult", "adult");

    private final int minAge;
    private final int maxAge;
    private final float bodyScale;
    private final float headScale;
    private final float speedMultiplier;
    private final String displayName;
    private final String clothingFolder;

    CitizenAgeState(int minAge, int maxAge, float bodyScale, float headScale, float speedMultiplier, String displayName, String clothingFolder) {
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.bodyScale = bodyScale;
        this.headScale = headScale;
        this.speedMultiplier = speedMultiplier;
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

    public float getSpeedMultiplier() {
        return speedMultiplier;
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
        if (!isMinecoloniesChild) {
            return ADULT;
        }
        if (age < CHILD.minAge) {
            return TODDLER;
        } else if (age < TEEN.minAge) {
            return CHILD;
        } else if (age < ADULT.minAge) {
            return TEEN;
        } else {
            return ADULT;
        }
    }
}

