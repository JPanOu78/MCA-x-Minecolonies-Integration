package com.mcaminecolonies.client.appearance;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Maps MineColonies Citizens to exact MCA Reborn visual texture assets
 * (skin, face, hair, and clothing layers) deterministically via Citizen UUID and gender.
 */
public class CitizenAppearance {

    private final boolean isFemale;
    private final boolean useSlimModel;
    private final int skinIndex;
    private final int faceIndex;
    private final int hairIndex;
    private final int clothingIndex;

    public CitizenAppearance(UUID uuid, boolean isFemale) {
        this.isFemale = isFemale;
        this.useSlimModel = isFemale;

        long lsb = uuid.getLeastSignificantBits();
        long msb = uuid.getMostSignificantBits();

        int seed1 = (int) Math.abs(lsb % 1000000007L);
        int seed2 = (int) Math.abs(msb % 1000000009L);

        // MCA provides 5 base skins (0 to 4)
        this.skinIndex = seed1 % 5;
        // MCA provides ~22 facial expressions (0 to 21)
        this.faceIndex = seed2 % 22;
        // MCA provides ~30 hairstyles
        this.hairIndex = (seed1 / 7) % 30;
        // MCA clothing variants per job
        this.clothingIndex = (seed2 / 11) % 3;
    }

    public static CitizenAppearance getAppearance(AbstractEntityCitizen citizen) {
        UUID uuid = citizen.getUUID();
        boolean isFemale = citizen.isFemale();
        return new CitizenAppearance(uuid, isFemale);
    }

    public boolean isSlimModel() {
        return useSlimModel;
    }

    public boolean isFemale() {
        return isFemale;
    }

    /**
     * MCA Base Skin Texture
     */
    public ResourceLocation getBaseSkinTexture() {
        String genderDir = isFemale ? "female" : "male";
        return ResourceLocation.fromNamespaceAndPath(
            "mca",
            "skins/skin/" + genderDir + "/" + skinIndex + ".png"
        );
    }

    /**
     * MCA Face / Eyes / Expression Layer
     */
    public ResourceLocation getFaceTexture() {
        String genderDir = isFemale ? "female" : "male";
        return ResourceLocation.fromNamespaceAndPath(
            "mca",
            "skins/face/normal/" + genderDir + "/" + faceIndex + ".png"
        );
    }

    /**
     * MCA Hair Layer Texture
     */
    public ResourceLocation getHairTexture() {
        String genderDir = isFemale ? "female" : "male";
        return ResourceLocation.fromNamespaceAndPath(
            "mca",
            "skins/hair/" + genderDir + "/" + hairIndex + ".png"
        );
    }

    /**
     * MCA Job/Clothing Layer Texture mapped from MineColonies jobs
     */
    public ResourceLocation getClothingTexture(String minecoloniesJob) {
        String mcaJob = mapMinecoloniesJobToMCA(minecoloniesJob);
        return ResourceLocation.fromNamespaceAndPath(
            "mca",
            "skins/clothing/normal/neutral/" + mcaJob + "/" + clothingIndex + ".png"
        );
    }

    private static String mapMinecoloniesJobToMCA(String job) {
        if (job == null) return "none";
        String lower = job.toLowerCase();

        if (lower.contains("farm") || lower.contains("lumber") || lower.contains("forester")) {
            return "farmer";
        } else if (lower.contains("fish")) {
            return "fisherman";
        } else if (lower.contains("miner") || lower.contains("stone") || lower.contains("mason")) {
            return "mason";
        } else if (lower.contains("smith") || lower.contains("smelt") || lower.contains("knight") || lower.contains("guard")) {
            return "weaponsmith";
        } else if (lower.contains("fletch") || lower.contains("archer")) {
            return "fletcher";
        } else if (lower.contains("cook") || lower.contains("bake") || lower.contains("butcher")) {
            return "butcher";
        } else if (lower.contains("courier") || lower.contains("delivery")) {
            return "cartographer";
        } else if (lower.contains("cleric") || lower.contains("priest") || lower.contains("enchant")) {
            return "cleric";
        } else if (lower.contains("library") || lower.contains("school") || lower.contains("teacher")) {
            return "librarian";
        }
        return "none";
    }
}
