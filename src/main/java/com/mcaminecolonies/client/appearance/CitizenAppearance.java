package com.mcaminecolonies.client.appearance;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.core.entity.citizen.AbstractEntityCitizen;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Manages consistent, deterministic visual appearance mappings (skin, hair, eyes, clothes)
 * for MineColonies Citizens using MCA Reborn human styling.
 */
public class CitizenAppearance {

    public static final String[] SKIN_TONES = {
        "light_1", "light_2", "medium_1", "medium_2", "tan_1", "tan_2", "warm_1", "dark_1", "dark_2"
    };

    public static final String[] HAIR_COLORS = {
        "black", "dark_brown", "brown", "chestnut", "blonde", "platinum", "red", "auburn", "gray"
    };

    public static final String[] EYE_COLORS = {
        "brown", "dark_brown", "blue", "light_blue", "green", "emerald", "hazel", "amber", "gray"
    };

    private final boolean isFemale;
    private final boolean useSlimModel;
    private final String skinTone;
    private final String hairColor;
    private final String eyeColor;
    private final int hairStyleIndex;
    private final int outfitVariant;

    public CitizenAppearance(UUID uuid, boolean isMale) {
        this.isFemale = !isMale;
        this.useSlimModel = isFemale;

        long lsb = uuid.getLeastSignificantBits();
        long msb = uuid.getMostSignificantBits();

        int seed1 = (int) Math.abs(lsb % 1000000007L);
        int seed2 = (int) Math.abs(msb % 1000000009L);

        this.skinTone = SKIN_TONES[seed1 % SKIN_TONES.length];
        this.hairColor = HAIR_COLORS[(seed1 / 13) % HAIR_COLORS.length];
        this.eyeColor = EYE_COLORS[(seed2 / 17) % EYE_COLORS.length];
        this.hairStyleIndex = Math.abs((seed2 / 31) % 6);
        this.outfitVariant = Math.abs((seed1 / 7) % 4);
    }

    public static CitizenAppearance getAppearance(AbstractEntityCitizen citizen) {
        UUID uuid = citizen.getUUID();
        boolean isMale = citizen.isMale();
        return new CitizenAppearance(uuid, isMale);
    }

    public boolean isSlimModel() {
        return useSlimModel;
    }

    public boolean isFemale() {
        return isFemale;
    }

    public String getSkinTone() {
        return skinTone;
    }

    public String getHairColor() {
        return hairColor;
    }

    public String getEyeColor() {
        return eyeColor;
    }

    public int getHairStyleIndex() {
        return hairStyleIndex;
    }

    public int getOutfitVariant() {
        return outfitVariant;
    }

    /**
     * Resolves the primary base skin texture location.
     */
    public ResourceLocation getBaseSkinTexture() {
        String genderDir = isFemale ? "female" : "male";
        return ResourceLocation.fromNamespaceAndPath(
            "mcaminecolonies",
            "textures/entity/citizen/" + genderDir + "/base_" + skinTone + ".png"
        );
    }

    /**
     * Resolves job/profession specific clothing overlay texture.
     */
    public ResourceLocation getClothingTexture(String jobName) {
        String cleanJob = (jobName == null || jobName.isEmpty()) ? "citizen" : jobName.toLowerCase();
        String genderDir = isFemale ? "female" : "male";
        return ResourceLocation.fromNamespaceAndPath(
            "mcaminecolonies",
            "textures/entity/citizen/" + genderDir + "/clothing/" + cleanJob + "_" + outfitVariant + ".png"
        );
    }

    /**
     * Resolves hair layer texture.
     */
    public ResourceLocation getHairTexture() {
        String genderDir = isFemale ? "female" : "male";
        return ResourceLocation.fromNamespaceAndPath(
            "mcaminecolonies",
            "textures/entity/citizen/" + genderDir + "/hair/hair_" + hairStyleIndex + "_" + hairColor + ".png"
        );
    }

    /**
     * Resolves eyes layer texture.
     */
    public ResourceLocation getEyesTexture() {
        return ResourceLocation.fromNamespaceAndPath(
            "mcaminecolonies",
            "textures/entity/citizen/eyes/eyes_" + eyeColor + ".png"
        );
    }
}
