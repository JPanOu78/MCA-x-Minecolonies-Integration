package com.mcaminecolonies.client.appearance;

import com.mcaminecolonies.data.CitizenMCAData;
import com.mcaminecolonies.data.CitizenMCAManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.conczin.mca.entity.VillagerLike;
import net.minecraft.resources.ResourceLocation;

/**
 * Resolves visual textures, model variations, and layers for a citizen based on MCA data and editor customization.
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
    private final String customClothes;
    private final String customHair;

    public CitizenAppearance(AbstractEntityCitizen citizen) {
        CitizenMCAData data = CitizenMCAManager.getData(citizen);

        // Synchronize gender with Minecolonies citizen
        this.isFemale = citizen.isFemale();
        this.isSlimModel = this.isFemale;

        this.ageState = data.getAgeState(citizen.isBaby());

        // Check if citizen implements VillagerLike (sync with MCA Villager Editor)
        String vClothes = null;
        String vHair = null;
        int vHairDye = 0;
        int vSkinIndex = data.getSkinIndex();
        if (citizen instanceof VillagerLike<?> vl) {
            vClothes = vl.getClothes();
            vHair = vl.getHair();
            vHairDye = vl.getHairDye();
            if (vl.getGenetics() != null) {
                vSkinIndex = (int) vl.getGenetics().getGene(net.conczin.mca.entity.ai.Genetics.SKIN);
            }
        }

        this.skinIndex = vSkinIndex;
        this.faceIndex = data.getFaceIndex();
        this.hairIndex = data.getHairIndex();
        this.clothingIndex = data.getClothingIndex();

        this.customClothes = (vClothes != null && !vClothes.isEmpty() && !vClothes.contains("/baby/")) ? vClothes : null;
        this.customHair = (vHair != null && !vHair.isEmpty()) ? vHair : null;

        if (vHairDye != 0 && vHairDye != -16777216) {
            this.hairColor = vHairDye;
        } else {
            this.hairColor = data.getHairColor();
        }
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
            "mca",
            "skins/skin/" + genderDir + "/" + (skinIndex % 5) + ".png"
        );
    }

    public ResourceLocation getFaceTexture() {
        return ResourceLocation.fromNamespaceAndPath(
            "mca",
            "skins/face/normal/" + (faceIndex % 11) + ".png"
        );
    }

    public ResourceLocation getHairTexture() {
        if (customHair != null) {
            return resolveMcaTexture(customHair, "skins/hair/" + (isFemale ? "female" : "male") + "/" + (hairIndex % 40) + ".png");
        }
        String genderDir = isFemale ? "female" : "male";
        return ResourceLocation.fromNamespaceAndPath(
            "mca",
            "skins/hair/" + genderDir + "/" + (hairIndex % 40) + ".png"
        );
    }

    public ResourceLocation getClothingTexture(String minecoloniesJob) {
        if (customClothes != null) {
            return resolveMcaTexture(customClothes, null);
        }

        String genderDir = isFemale ? "female" : "male";

        // Non-adult stages use stage-specific clothing (baby, toddler, child)
        if (!ageState.isAdult()) {
            String folder = ageState.getClothingFolder();
            int variantCount = (folder.equals("toddler") ? 20 : 10);
            return ResourceLocation.fromNamespaceAndPath(
                "mca",
                "skins/clothing/normal/" + genderDir + "/" + folder + "/" + (clothingIndex % variantCount) + ".png"
            );
        }

        // Adult stage uses profession-specific clothing
        String mcaJob = mapMinecoloniesJobToMCA(minecoloniesJob);
        return ResourceLocation.fromNamespaceAndPath(
            "mca",
            "skins/clothing/normal/" + genderDir + "/" + mcaJob + "/" + (clothingIndex % 5) + ".png"
        );
    }

    private static ResourceLocation resolveMcaTexture(String path, String fallback) {
        try {
            if (path.contains(":")) {
                return ResourceLocation.parse(path);
            }
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return ResourceLocation.fromNamespaceAndPath("mca", path);
        } catch (Exception e) {
            if (fallback != null) {
                return ResourceLocation.fromNamespaceAndPath("mca", fallback);
            }
            return ResourceLocation.fromNamespaceAndPath("mca", "skins/clothing/normal/male/none/0.png");
        }
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
        } else if (lower.contains("cleric") || lower.contains("priest") || lower.contains("enchant") || lower.contains("healer") || lower.contains("alchemist") || lower.contains("druid") || lower.contains("mage") || lower.contains("wizard") || lower.contains("spell") || lower.contains("magic") || lower.contains("sorcerer") || lower.contains("warlock")) {
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
