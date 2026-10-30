package com.mcaminecolonies.client.gui;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.conczin.mca.client.gui.FamilyTreeScreen;
import net.conczin.mca.client.gui.LimitedVillagerEditorScreen;
import net.conczin.mca.client.gui.VillagerEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Client-only helpers to safely open MCA screens and avoid dedicated server ClassNotFoundErrors.
 */
public class CitizenClientScreens {

    public static void openVillagerEditor(AbstractEntityCitizen citizen, Player player) {
        Minecraft.getInstance().setScreen(new VillagerEditorScreen(citizen.getUUID(), player.getUUID()));
    }

    public static void openLimitedEditor(AbstractEntityCitizen citizen, Player player) {
        Minecraft.getInstance().setScreen(new LimitedVillagerEditorScreen(citizen.getUUID(), player.getUUID()));
    }

    public static void openFamilyTree(AbstractEntityCitizen citizen) {
        Minecraft.getInstance().setScreen(new FamilyTreeScreen(citizen.getUUID()));
    }

    public static void openCustomizationScreen(AbstractEntityCitizen citizen) {
        Minecraft.getInstance().setScreen(new CitizenCustomizationScreen(citizen));
    }
}

