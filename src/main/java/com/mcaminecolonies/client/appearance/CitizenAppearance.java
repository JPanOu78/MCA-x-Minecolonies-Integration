package com.mcaminecolonies.client.appearance;

import com.mcaminecolonies.data.CitizenMCAData;
import com.mcaminecolonies.data.CitizenMCAManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.conczin.mca.client.resources.ColorPalette;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.Genetics;
import net.conczin.mca.entity.ai.Traits;
import net.minecraft.resources.ResourceLocation;

/**
 * Resolves visual textures, model variations, and layers for a citizen based on MCA data and editor customization.
 */
public class CitizenAppearance {

    private final boolean isFemale;
    private final boolean isSlimModel;
    private final CitizenAgeState ageState;
    private final int skinIndex;
    private final int skinColor;
    private final int faceIndex;
    private final int hairIndex;
    private final int hairColor;
    private final int clothingIndex;
    private final String customSkin;
    private final String customClothes;
    private final String customHair;

    public CitizenAppearance(AbstractEntityCitizen citizen) {
        CitizenMCAData data = CitizenMCAManager.getData(citizen);

        // Synchronize gender with Minecolonies citizen or MCA VillagerLike data
        boolean female = citizen.isFemale();
        if (citizen instanceof VillagerLike<?> vl && vl.getGenetics() != null && vl.getGenetics().getGender() != null && vl.getGenetics().getGender() != net.conczin.mca.entity.ai.relationship.Gender.UNASSIGNED) {
            female = (vl.getGenetics().getGender() == net.conczin.mca.entity.ai.relationship.Gender.FEMALE);
        } else if (data != null) {
            female = data.isFemale();
        }
        this.isFemale = female;
        this.isSlimModel = this.isFemale;

        this.ageState = data.getAgeState(citizen.isBaby());

        // Check if citizen implements VillagerLike (sync with MCA Villager Editor)
        String vClothes = null;
        String vHair = null;
        int vHairDye = 0;
        int vSkinIndex = data.getSkinIndex();
        int vFaceIndex = data.getFaceIndex();
        float albinismMultiplier = 1.0F;
        float melanin = data.getMelanin();
        float hemoglobin = data.getHemoglobin();
        float infection = 0.0F;

        if (citizen instanceof VillagerLike<?> vl) {
            vClothes = vl.getClothes();
            vHair = vl.getHair();
            vHairDye = vl.getHairDye();
            if (vl.getTraits() != null && vl.getTraits().hasTrait(Traits.ALBINISM)) {
                albinismMultiplier = 0.1F;
            }
            if (vl.getGenetics() != null) {
                float skinGene = vl.getGenetics().getGene(Genetics.SKIN);
                if (skinGene > 0.0F) {
                    vSkinIndex = (int) Math.min(4.0F, Math.max(0.0F, skinGene * 5.0F));
                }
                float faceGene = vl.getGenetics().getGene(Genetics.FACE);
                if (faceGene > 0.0F) {
                    vFaceIndex = (int) Math.min(10.0F, Math.max(0.0F, faceGene * 11.0F));
                }
                float mGene = vl.getGenetics().getGene(Genetics.MELANIN);
                if (mGene > 0.0F) {
                    melanin = mGene * albinismMultiplier;
                }
                float hGene = vl.getGenetics().getGene(Genetics.HEMOGLOBIN);
                if (hGene > 0.0F) {
                    hemoglobin = hGene * albinismMultiplier;
                }
            }
            infection = vl.getInfectionProgress();
        }

        this.skinIndex = vSkinIndex;
        this.faceIndex = vFaceIndex;
        this.hairIndex = data.getHairIndex();
        this.clothingIndex = data.getClothingIndex();

        this.customSkin = (data.getCustomSkin() != null && !data.getCustomSkin().isEmpty()) ? data.getCustomSkin() : null;
        this.customClothes = (vClothes != null && !vClothes.isEmpty() && !vClothes.contains("/baby/")) ? vClothes : data.getCustomClothes();
        this.customHair = (vHair != null && !vHair.isEmpty()) ? vHair : data.getCustomHair();

        int resolvedHairColor = CitizenMCAData.DEFAULT_HAIR_COLOR;
        if (vHairDye != 0 && vHairDye != -16777216) {
            resolvedHairColor = (vHairDye & 0xFF000000) == 0 ? (0xFF000000 | vHairDye) : vHairDye;
        } else if (data.getHairColor() != 0 && data.getHairColor() != -16777216 && data.getHairColor() != CitizenMCAData.DEFAULT_HAIR_COLOR) {
            resolvedHairColor = (data.getHairColor() & 0xFF000000) == 0 ? (0xFF000000 | data.getHairColor()) : data.getHairColor();
        } else if (citizen instanceof VillagerLike<?> vl && vl.getGenetics() != null) {
            try {
                float eumelanin = vl.getGenetics().getGene(Genetics.EUMELANIN) * albinismMultiplier;
                float pheomelanin = vl.getGenetics().getGene(Genetics.PHEOMELANIN) * albinismMultiplier;
                int calc = ColorPalette.HAIR.getColor(eumelanin, pheomelanin, 0.0F);
                resolvedHairColor = (calc & 0xFF000000) == 0 ? (0xFF000000 | calc) : calc;
            } catch (Exception ignored) {
                resolvedHairColor = CitizenMCAData.DEFAULT_HAIR_COLOR;
            }
        } else if (data.getHairColor() != 0) {
            resolvedHairColor = (data.getHairColor() & 0xFF000000) == 0 ? (0xFF000000 | data.getHairColor()) : data.getHairColor();
        }
        this.hairColor = resolvedHairColor;

        // Calculate resolved skin color matching MCA SkinLayer
        int resolvedSkinDye = data.getSkinDye();
        if (resolvedSkinDye != 0 && resolvedSkinDye != -16777216) {
            this.skinColor = (resolvedSkinDye & 0xFF000000) == 0 ? (0xFF000000 | resolvedSkinDye) : resolvedSkinDye;
        } else {
            ResourceLocation baseSkin = this.getBaseSkinTexture();
            boolean isGrayscaleSkin = baseSkin.getPath().contains("_classic");

            int calculatedColor;
            try {
                calculatedColor = ColorPalette.SKIN.getColor(melanin, hemoglobin, infection);
            } catch (Exception e) {
                calculatedColor = 0xFFE9D4BC; // Natural skin tone fallback RGB(233, 212, 188)
            }
            if ((calculatedColor & 0xFF000000) == 0) {
                calculatedColor |= 0xFF000000;
            }

            if (isGrayscaleSkin) {
                // Classic grayscale skins (0_classic.png ... 4_classic.png) require full colormap tinting.
                // Prevent excessively dark grayscale skin by ensuring healthy minimum luminance (215.0F floor)
                int r = (calculatedColor >> 16) & 0xFF;
                int g = (calculatedColor >> 8) & 0xFF;
                int b = calculatedColor & 0xFF;
                float lum = 0.299F * r + 0.587F * g + 0.114F * b;
                if (lum < 215.0F && infection <= 0.05F) {
                    float boost = 215.0F / Math.max(1.0F, lum);
                    r = Math.min(255, Math.round(r * boost));
                    g = Math.min(255, Math.round(g * boost));
                    b = Math.min(255, Math.round(b * boost));
                    calculatedColor = 0xFF000000 | (r << 16) | (g << 8) | b;
                }
                this.skinColor = calculatedColor;
            } else {
                // Modern pre-colored skin textures (0.png through 4.png) already have RGB(232, 210, 185) built in!
                // If melanin is <= 0.25F, use clean 0xFFFFFFFF so modern skins are crisp and natural.
                // If melanin is higher, normalize tint against baseline #E9D4BC so it darkens proportionally without crushing blacks.
                if (melanin <= 0.25F && infection <= 0.05F) {
                    this.skinColor = 0xFFFFFFFF;
                } else {
                    int cr = (calculatedColor >> 16) & 0xFF;
                    int cg = (calculatedColor >> 8) & 0xFF;
                    int cb = calculatedColor & 0xFF;
                    int nr = Math.min(255, Math.round((cr / 233.0F) * 255.0F));
                    int ng = Math.min(255, Math.round((cg / 212.0F) * 255.0F));
                    int nb = Math.min(255, Math.round((cb / 188.0F) * 255.0F));
                    this.skinColor = 0xFF000000 | (nr << 16) | (ng << 8) | nb;
                }
            }
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

    public int getSkinColor() {
        return skinColor;
    }

    public ResourceLocation getBaseSkinTexture() {
        if (customSkin != null) {
            return resolveMcaTexture(customSkin, "skins/skin/" + (isFemale ? "female" : "male") + "/" + (skinIndex % 5) + ".png");
        }
        String genderDir = isFemale ? "female" : "male";
        return ResourceLocation.fromNamespaceAndPath(
            "mca",
            "skins/skin/" + genderDir + "/" + (skinIndex % 5) + ".png"
        );
    }

    public ResourceLocation getFaceTexture(AbstractEntityCitizen entity) {
        int faceIdx = (faceIndex % 11 + 11) % 11;
        int seed = (entity.tickCount / 2) + Math.abs(entity.getUUID().hashCode() % 1000);
        boolean blink = (seed % 50 == 1 || seed % 57 == 1) || entity.isSleeping() || entity.isDeadOrDying();
        if (blink) {
            return ResourceLocation.fromNamespaceAndPath("mca", "skins/face/normal/blink.png");
        }
        return ResourceLocation.fromNamespaceAndPath(
            "mca",
            "skins/face/normal/" + faceIdx + ".png"
        );
    }

    public ResourceLocation getHairTexture() {
        if (customHair != null && customHair.endsWith(".png") && !customHair.equals("skins/") && !customHair.equals("none")) {
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
        if (path == null || path.isBlank() || path.equals("skins/") || path.equals("none")) {
            if (fallback != null) {
                return ResourceLocation.fromNamespaceAndPath("mca", fallback);
            }
            return null;
        }
        try {
            if (path.contains(":")) {
                return ResourceLocation.parse(path);
            }
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (!path.startsWith("skins/")) {
                path = "skins/" + path;
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
