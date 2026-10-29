package com.mcaminecolonies.client.gui;

import com.mcaminecolonies.client.appearance.CitizenAgeState;
import com.mcaminecolonies.data.CitizenMCAData;
import com.mcaminecolonies.data.CitizenMCAManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * Screen providing full visual appearance customization for MineColonies citizens.
 */
public class CitizenCustomizationScreen extends Screen {

    private final AbstractEntityCitizen citizen;
    private final CitizenMCAData data;

    private static final int[] PRESET_HAIR_COLORS = {
        0xFF1E1B1B, // Black
        0xFF4A2A18, // Dark Brown
        0xFF835432, // Chestnut
        0xFFD8A050, // Blonde
        0xFFB04A26, // Auburn / Ginger
        0xFFE0E0E0, // Platinum
        0xFF6689D3, // Light Blue
        0xFFF27FA5, // Pink
        0xFF169C9C, // Cyan
        0xFF7B2FBE  // Purple
    };
    private int colorIndex = 0;

    public CitizenCustomizationScreen(AbstractEntityCitizen citizen) {
        super(Component.literal("Customize Appearance - " + citizen.getDisplayName().getString()));
        this.citizen = citizen;
        this.data = CitizenMCAManager.getData(citizen);

        for (int i = 0; i < PRESET_HAIR_COLORS.length; i++) {
            if (PRESET_HAIR_COLORS[i] == data.getHairColor()) {
                colorIndex = i;
                break;
            }
        }
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int btnW = 120;
        int btnH = 20;
        int rightX = centerX + 25;
        int startY = centerY - 80;

        // 1. Hairstyle
        addRenderableWidget(Button.builder(Component.literal("Hairstyle: #" + (data.getHairIndex() + 1)), b -> {
            data.setHairIndex(data.getHairIndex() + 1);
            b.setMessage(Component.literal("Hairstyle: #" + (data.getHairIndex() + 1)));
            save();
        }).bounds(rightX, startY, btnW, btnH).build());

        // 2. Hair Color
        addRenderableWidget(Button.builder(Component.literal("Hair Color: #" + (colorIndex + 1)), b -> {
            colorIndex = (colorIndex + 1) % PRESET_HAIR_COLORS.length;
            data.setHairColor(PRESET_HAIR_COLORS[colorIndex]);
            b.setMessage(Component.literal("Hair Color: #" + (colorIndex + 1)));
            save();
        }).bounds(rightX, startY + 24, btnW, btnH).build());

        // 3. Skin Tone
        addRenderableWidget(Button.builder(Component.literal("Skin Tone: #" + (data.getSkinIndex() + 1)), b -> {
            data.setSkinIndex(data.getSkinIndex() + 1);
            b.setMessage(Component.literal("Skin Tone: #" + (data.getSkinIndex() + 1)));
            save();
        }).bounds(rightX, startY + 48, btnW, btnH).build());

        // 4. Expression
        addRenderableWidget(Button.builder(Component.literal("Face: #" + (data.getFaceIndex() + 1)), b -> {
            data.setFaceIndex(data.getFaceIndex() + 1);
            b.setMessage(Component.literal("Face: #" + (data.getFaceIndex() + 1)));
            save();
        }).bounds(rightX, startY + 72, btnW, btnH).build());

        // 5. Outfit Variant
        addRenderableWidget(Button.builder(Component.literal("Outfit: #" + (data.getClothingIndex() + 1)), b -> {
            data.setClothingIndex(data.getClothingIndex() + 1);
            b.setMessage(Component.literal("Outfit: #" + (data.getClothingIndex() + 1)));
            save();
        }).bounds(rightX, startY + 96, btnW, btnH).build());

        // 6. Gender Toggle
        addRenderableWidget(Button.builder(Component.literal("Gender: " + (data.isFemale() ? "Female" : "Male")), b -> {
            boolean newGender = !data.isFemale();
            data.setFemale(newGender);
            citizen.setFemale(newGender);
            b.setMessage(Component.literal("Gender: " + (newGender ? "Female" : "Male")));
            save();
        }).bounds(rightX, startY + 120, btnW, btnH).build());

        // 7. Done & Back Button
        addRenderableWidget(Button.builder(Component.literal("✔ Save & Return"), b -> {
            save();
            this.minecraft.setScreen(new CitizenDialogueScreen(citizen));
        }).bounds(rightX, startY + 150, btnW, btnH).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int boxW = 320;
        int boxH = 220;
        int boxX = (this.width - boxW) / 2;
        int boxY = (this.height - boxH) / 2;

        graphics.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        graphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xDD222228);
        graphics.fill(boxX + 2, boxY + 2, boxX + boxW - 2, boxY + boxH - 2, 0xEE18181E);

        graphics.drawCenteredString(this.font, "§6§lCustomize Citizen Appearance", boxX + boxW / 2, boxY + 10, 0xFFFFAA00);

        // 3D Model Preview on Left
        if (citizen != null) {
            int avX1 = boxX + 15;
            int avY1 = boxY + 30;
            int avX2 = boxX + 140;
            int avY2 = boxY + 195;
            CitizenAgeState ageState = data.getAgeState(citizen.isBaby());
            int scale = (int) (65 * ageState.getBodyScale());

            graphics.fill(avX1, avY1, avX2, avY2, 0x55000000);
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics, avX1, avY1, avX2, avY2, scale, 0.0625F, mouseX, mouseY, citizen
            );
        }

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    private void save() {
        data.setCustomized(true);
        CitizenMCAManager.saveData(citizen, data);
        if (this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

