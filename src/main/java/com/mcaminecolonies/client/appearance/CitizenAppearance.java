package com.mcaminecolonies.client.appearance;

import com.mcaminecolonies.data.CitizenMCAData;
import com.mcaminecolonies.data.CitizenMCAManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.resources.ResourceLocation;

/**
 * Resolves visual textures, model variations, and layers for a citizen based on MCA data.
 */
public class CitizenAppearance {

    private final boolean isFemale;
    private final boolean isSlimModel;
    private final CitizenAgeState ageState;
    private final int skinIndex;
    private final int faceIndex;
    private final int hairIndex;
    private final int hairColor;
    private final int clothingIndex;

    public CitizenAppearance(AbstractEntityCitizen citizen) {
        CitizenMCAData data = CitizenMCAManager.getData(citizen);

        // Synchronize gender with Minecolonies citizen
        this.isFemale = citizen.isFemale();
        this.isSlimModel = this.isFemale;

        this.ageState = data.getAgeState(citizen.isBaby());
        this.skinIndex = data.getSkinIndex();
        this.faceIndex = data.getFaceIndex();
        this.hairIndex = data.getHairIndex();
        this.hairColor = data.getHairColor();
        this.clothingIndex = data.getClothingIndex();
    }

    public static CitizenAppearance getAppearance(AbstractEntityCitizen citizen) {
        return new CitizenAppearance(citizen);
    }

    public boolean isSlimModel() {
        return isSlimModel;
    }

    public boolean isFemale() {
        return isFemale;
    }

    public CitizenAgeState getAgeState() {
        return ageState;
    }

    public int getHairColor() {
        return hairColor;
    }

    public ResourceLocation getBaseSkinTexture() {
        String genderDir = isFemale ? "female" : "male";
        return ResourceLocation.fromNamespaceAndPath(
            "mcaminecolonies",
            "textures/entity/citizen/skin/" + genderDir + "/" + (skinIndex % 5) + ".png"
        );
    }

    public ResourceLocation getFaceTexture() {
        String genderDir = isFemale ? "female" : "male";
        return ResourceLocation.fromNamespaceAndPath(
            "mcaminecolonies",
            "textures/entity/citizen/face/normal/" + genderDir + "/" + (faceIndex % 22) + ".png"
        );
    }

    public ResourceLocation getHairTexture() {
        String genderDir = isFemale ? "female" : "male";
        return ResourceLocation.fromNamespaceAndPath(
            "mcaminecolonies",
            "textures/entity/citizen/hair/" + genderDir + "/" + (hairIndex % 40) + ".png"
        );
    }

    public ResourceLocation getClothingTexture(String minecoloniesJob) {
        String genderDir = isFemale ? "female" : "male";

        // Non-adult stages use stage-specific clothing (baby, toddler, child)
        if (!ageState.isAdult()) {
            String folder = ageState.getClothingFolder();
            int variantCount = (folder.equals("toddler") ? 20 : 10);
            return ResourceLocation.fromNamespaceAndPath(
                "mcaminecolonies",
                "textures/entity/citizen/clothing/normal/" + genderDir + "/" + folder + "/" + (clothingIndex % variantCount) + ".png"
            );
        }

        // Adult stage uses profession-specific clothing
        String mcaJob = mapMinecoloniesJobToMCA(minecoloniesJob);
        return ResourceLocation.fromNamespaceAndPath(
            "mcaminecolonies",
            "textures/entity/citizen/clothing/normal/" + genderDir + "/" + mcaJob + "/" + (clothingIndex % 10) + ".png"
        );
    }

    private static String mapMinecoloniesJobToMCA(String job) {
        if (job == null) return "none";
        String lower = job.toLowerCase();

        if (lower.contains("farm") || lower.contains("lumber") || lower.contains("forester") || lower.contains("plant") || lower.contains("florist") || lower.contains("bee") || lower.contains("compost")) {
            return "farmer";
        } else if (lower.contains("fish")) {
            return "fisherman";
        } else if (lower.contains("miner") || lower.contains("quarry") || lower.contains("nether")) {
            return "miner";
        } else if (lower.contains("blacksmith") || lower.contains("weapon")) {
            return "weaponsmith";
        } else if (lower.contains("knight") || lower.contains("guard") || lower.contains("patrol") || lower.contains("combat") || lower.contains("shield")) {
            return "guard";
        } else if (lower.contains("archer") || lower.contains("ranger") || lower.contains("bow")) {
            return "archer";
        } else if (lower.contains("smelt") || lower.contains("tool") || lower.contains("carpenter") || lower.contains("sawmill") || lower.contains("mechan") || lower.contains("builder") || lower.contains("crafter")) {
            return "toolsmith";
        } else if (lower.contains("stone") || lower.contains("mason") || lower.contains("concrete") || lower.contains("brick")) {
            return "mason";
        } else if (lower.contains("armor")) {
            return "armorer";
        } else if (lower.contains("fletch")) {
            return "fletcher";
        } else if (lower.contains("shepherd") || lower.contains("cow") || lower.contains("swine") || lower.contains("pig") || lower.contains("chicken") || lower.contains("rabbit") || lower.contains("animal") || lower.contains("herd")) {
            return "shepherd";
        } else if (lower.contains("courier") || lower.contains("delivery")) {
            return "cartographer";
        } else if (lower.contains("cleric") || lower.contains("priest") || lower.contains("enchant") || lower.contains("healer") || lower.contains("alchemist") || lower.contains("druid")) {
            return "cleric";
        } else if (lower.contains("under") || lower.contains("grave") || lower.contains("tomb") || lower.contains("cult")) {
            return "cultist";
        } else if (lower.contains("library") || lower.contains("school") || lower.contains("teacher") || lower.contains("university") || lower.contains("student") || lower.contains("book")) {
            return "librarian";
        } else if (lower.contains("baker") || lower.contains("cook") || lower.contains("chef") || lower.contains("tavern") || lower.contains("kitchen")) {
            return "baker";
        } else if (lower.contains("leather") || lower.contains("tanner") || lower.contains("dye") || lower.contains("glass")) {
            return "leatherworker";
        }
        return "none";
    }
}
