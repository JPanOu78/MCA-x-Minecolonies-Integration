package com.mcaminecolonies.client.gui;

import com.mcaminecolonies.client.appearance.CitizenAgeState;
import com.mcaminecolonies.data.CitizenMCAData;
import com.mcaminecolonies.data.CitizenMCAManager;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.client.gui.citizen.MainWindowCitizen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;

/**
 * Interactive MCA-style dialogue screen for MineColonies citizens.
 * Provides talking, jokes, storytelling, flirting, compliments, and appearance editing.
 */
public class CitizenDialogueScreen extends Screen {

    private final AbstractEntityCitizen citizen;
    private final ICitizenDataView citizenDataView;
    private final CitizenMCAData data;
    private String currentDialogue;
    private String dialogueAuthor;
    private int dialogueTimer;
    private final RandomSource random = RandomSource.create();

    public CitizenDialogueScreen(AbstractEntityCitizen citizen) {
        super(Component.literal("MCA Dialogue - " + citizen.getDisplayName().getString()));
        this.citizen = citizen;
        this.citizenDataView = citizen.getCitizenDataView();
        this.data = CitizenMCAManager.getData(citizen);
        this.dialogueAuthor = citizen.getDisplayName().getString();
        this.currentDialogue = getGreetingDialogue();
    }

    public CitizenDialogueScreen(ICitizenDataView view) {
        super(Component.literal("MCA Dialogue - " + view.getName()));
        this.citizenDataView = view;
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(view.getEntityId()) instanceof AbstractEntityCitizen cit) {
            this.citizen = cit;
            this.data = CitizenMCAManager.getData(cit);
        } else {
            this.citizen = null;
            this.data = new CitizenMCAData();
        }
        this.dialogueAuthor = view.getName();
        this.currentDialogue = getGreetingDialogue();
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int buttonWidth = 110;
        int buttonHeight = 20;
        int rightColumnX = centerX + 30;
        int startY = centerY - 70;

        // Action Buttons on Right
        addRenderableWidget(Button.builder(Component.literal("💬 Chat"), b -> onChat())
                .bounds(rightColumnX, startY, buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.literal("😂 Tell Joke"), b -> onJoke())
                .bounds(rightColumnX, startY + 24, buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.literal("📖 Story"), b -> onStory())
                .bounds(rightColumnX, startY + 48, buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.literal("💖 Flirt"), b -> onFlirt())
                .bounds(rightColumnX, startY + 72, buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.literal("👍 Compliment"), b -> onCompliment())
                .bounds(rightColumnX, startY + 96, buttonWidth, buttonHeight).build());

        // Customization & Back Buttons
        addRenderableWidget(Button.builder(Component.literal("✂️ Customize Look"), b -> onCustomize())
                .bounds(rightColumnX, startY + 124, buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.literal("🏛️ Colony Window"), b -> onBackToColony())
                .bounds(rightColumnX, startY + 148, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Dark translucent background box
        int boxW = 340;
        int boxH = 230;
        int boxX = (this.width - boxW) / 2;
        int boxY = (this.height - boxH) / 2;

        graphics.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        graphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xDD222228);
        graphics.fill(boxX + 2, boxY + 2, boxX + boxW - 2, boxY + boxH - 2, 0xEE18181E);

        // Header Title
        String citizenName = dialogueAuthor != null ? dialogueAuthor : "Citizen";
        graphics.drawCenteredString(this.font, "§6§l" + citizenName, boxX + boxW / 2, boxY + 10, 0xFFFFAA00);

        // Subtitle: Age stage, Gender, and Job
        CitizenAgeState ageState = data.getAgeState(citizen != null && citizen.isBaby());
        String genderStr = data.isFemale() ? "♀ Female" : "♂ Male";
        String subtitle = "§7Stage: §f" + ageState.getDisplayName() + " §7| §f" + genderStr;
        graphics.drawCenteredString(this.font, subtitle, boxX + boxW / 2, boxY + 24, 0xFFAAAAAA);

        // Render 3D Citizen Avatar on Left
        if (citizen != null) {
            int avatarX1 = boxX + 15;
            int avatarY1 = boxY + 36;
            int avatarX2 = boxX + 145;
            int avatarY2 = boxY + 175;
            int scale = (int) (60 * ageState.getBodyScale());

            graphics.fill(avatarX1, avatarY1, avatarX2, avatarY2, 0x55000000);
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics, avatarX1, avatarY1, avatarX2, avatarY2, scale, 0.0625F, mouseX, mouseY, citizen
            );
        }

        // Render Hearts Bar (Affection)
        int hearts = data.getHearts();
        graphics.drawString(this.font, "§c♥ Affection: §f" + hearts + "/100", boxX + 160, boxY + 38, 0xFFFF5555);

        // Bottom Dialogue Bubble Box
        int dlgX = boxX + 10;
        int dlgY = boxY + boxH - 52;
        int dlgW = boxW - 20;
        int dlgH = 42;

        graphics.fill(dlgX, dlgY, dlgX + dlgW, dlgY + dlgH, 0xDD2A2A35);
        graphics.fill(dlgX + 1, dlgY + 1, dlgX + dlgW - 1, dlgY + dlgH - 1, 0xEE1E1E26);

        graphics.drawString(this.font, "§e" + citizenName + ":", dlgX + 6, dlgY + 5, 0xFFFFFF55);
        graphics.drawWordWrap(this.font, Component.literal("§f\"" + currentDialogue + "\""), dlgX + 6, dlgY + 18, dlgW - 12, 0xFFFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    private void onChat() {
        playVillagerSound(1.0F);
        data.addHearts(1);
        save();
        String[] dialogues = {
            "It's always nice to stop and chat! How is your adventure going?",
            "The colony has grown so much lately! We all appreciate your hard work.",
            "I was just thinking about taking a short stroll around the plaza.",
            "A fine day in the colony! Everyone is in good spirits."
        };
        this.currentDialogue = dialogues[random.nextInt(dialogues.length)];
    }

    private void onJoke() {
        playVillagerSound(1.2F);
        data.addHearts(2);
        save();
        String[] jokes = {
            "Why did the creeper cross the road? To blow everyone away on the other side!",
            "What is an enderman's favorite music genre? Tele-pop-ic!",
            "Why do miners make great musicians? Because they always hit rock bottom and find gold!",
            "Why did the iron golem bring a flower? Because he's a gentle giant at heart!"
        };
        this.currentDialogue = jokes[random.nextInt(jokes.length)];
    }

    private void onStory() {
        playVillagerSound(0.9F);
        data.addHearts(2);
        save();
        String[] stories = {
            "Legend says this land was once wild and desolate before our colony was founded.",
            "The elders spoke of a brave builder who finished the town hall right before sunset.",
            "Every tree we chop down makes room for new homes and warm hearths for our children.",
            "I saw the guards fend off a raid of zombies last night. Truly heroic bravery!"
        };
        this.currentDialogue = stories[random.nextInt(stories.length)];
    }

    private void onFlirt() {
        playVillagerSound(1.3F);
        data.addHearts(3);
        save();
        String[] flirts = {
            "Oh! You flatter me... you're making my cheeks turn red!",
            "Are you a torch? Because you light up my darkest nights in the colony.",
            "I always find myself looking forward to when you visit me.",
            "Such sweet words! You truly know how to make someone smile."
        };
        this.currentDialogue = flirts[random.nextInt(flirts.length)];
    }

    private void onCompliment() {
        playVillagerSound(1.1F);
        data.addHearts(2);
        save();
        String[] compliments = {
            "Thank you so much! Working for this colony is a genuine honor.",
            "Your kindness means the world to all of us colonists.",
            "That really brightens my day! Thank you for noticing my efforts."
        };
        this.currentDialogue = compliments[random.nextInt(compliments.length)];
    }

    private void onCustomize() {
        if (citizen != null) {
            Minecraft.getInstance().setScreen(new CitizenCustomizationScreen(citizen));
        }
    }

    private void onBackToColony() {
        if (citizenDataView != null) {
            new MainWindowCitizen(citizenDataView).open();
        } else {
            this.onClose();
        }
    }

    private String getGreetingDialogue() {
        return "Hello! How can I help you today?";
    }

    private void save() {
        if (citizen != null) {
            CitizenMCAManager.saveData(citizen, data);
        }
    }

    private void playVillagerSound(float pitch) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.playSound(SoundEvents.VILLAGER_YES, 0.8F, pitch);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

