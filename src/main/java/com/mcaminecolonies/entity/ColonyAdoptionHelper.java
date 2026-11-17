package com.mcaminecolonies.entity;

import com.mcaminecolonies.data.CitizenMCAData;
import com.mcaminecolonies.data.CitizenMCAManager;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.server.world.data.FamilyTree;
import net.conczin.mca.server.world.data.FamilyTreeNode;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Handles automatic enrollment and conversion of MCA infant/child villagers (VillagerEntityMCA)
 * into legitimate MineColonies citizens (AbstractEntityCitizen) inside a Colony.
 */
public class ColonyAdoptionHelper {

    public static boolean convertMCAChildToCitizen(VillagerEntityMCA mcaVillager, IColony colony, ServerLevel level, @Nullable ServerPlayer player) {
        if (mcaVillager == null || mcaVillager.isRemoved() || colony == null || level == null) {
            return false;
        }

        try {
            // 1. Create and register civilian in Colony CitizenManager
            ICitizenData citizenData = colony.getCitizenManager().createAndRegisterCivilianData();
            if (citizenData == null) {
                return false;
            }

            boolean isFemale = mcaVillager.getGenetics() != null && mcaVillager.getGenetics().getGender() == net.conczin.mca.entity.ai.relationship.Gender.FEMALE;
            String childName = (mcaVillager.getCustomName() != null && !mcaVillager.getCustomName().getString().isBlank())
                    ? mcaVillager.getCustomName().getString()
                    : (isFemale ? "Baby Girl" : "Baby Boy");
            citizenData.setName(childName);
            citizenData.setIsChild(true);

            // 2. Identify parents and link in MineColonies & FamilyTree
            FamilyTree tree = FamilyTree.get(level);
            FamilyTreeNode oldNode = tree.getOrCreate(mcaVillager);
            String parent1Name = player != null ? player.getName().getString() : "Colony";
            String parent2Name = "";

            List<FamilyTreeNode> parentNodes = oldNode.getParents().toList();
            for (FamilyTreeNode pNode : parentNodes) {
                if (player != null && pNode.id().equals(player.getUUID())) {
                    continue;
                }
                parent2Name = pNode.getName();
                // If the other parent is a citizen in this colony, link child to them in MineColonies
                for (ICitizenData c : colony.getCitizenManager().getCitizens()) {
                    if (c != null && c.getUUID().equals(pNode.id())) {
                        c.addChildren(citizenData.getId());
                        break;
                    }
                }
            }
            citizenData.setParents(parent1Name, parent2Name);

            // 3. Spawn the new AbstractEntityCitizen into the colony
            colony.getCitizenManager().spawnOrCreateCitizen(citizenData, level, mcaVillager.blockPosition());
            Optional<AbstractEntityCitizen> newCitizenOpt = citizenData.getEntity();
            if (newCitizenOpt.isEmpty()) {
                return false;
            }
            AbstractEntityCitizen newCitizen = newCitizenOpt.get();
            newCitizen.setFemale(isFemale);

            // 4. Transfer MCA appearance, genetics, and traits
            CitizenMCAData data = CitizenMCAManager.getData(newCitizen);
            if (data != null) {
                data.setFemale(isFemale);
                if (mcaVillager.getGenetics() != null) {
                    data.setMelanin(mcaVillager.getGenetics().getGene(net.conczin.mca.entity.ai.Genetics.MELANIN));
                    data.setHemoglobin(mcaVillager.getGenetics().getGene(net.conczin.mca.entity.ai.Genetics.HEMOGLOBIN));
                    data.setSize(mcaVillager.getGenetics().getGene(net.conczin.mca.entity.ai.Genetics.SIZE));
                    data.setWidth(mcaVillager.getGenetics().getGene(net.conczin.mca.entity.ai.Genetics.WIDTH));
                    data.setBreast(mcaVillager.getGenetics().getGene(net.conczin.mca.entity.ai.Genetics.BREAST));
                    data.setEyeBrightness(mcaVillager.getGenetics().getGene(net.conczin.mca.entity.ai.Genetics.EYE_BRIGHTNESS));
                }
                if (mcaVillager.getHairDye() != 0) {
                    data.setHairColor(mcaVillager.getHairDye());
                }
                if (mcaVillager.getHairStyleId() != null) {
                    data.setHairStyle(mcaVillager.getHairStyleId());
                }
                data.setHairBase(mcaVillager.getHairBase());
                data.setHairBangs(mcaVillager.getHairBangs());
                data.setHairBack(mcaVillager.getHairBack());
                data.setHairFront(mcaVillager.getHairFront());
                data.setHairExtra(mcaVillager.getHairExtra());
                data.setEyeColor(mcaVillager.getEyeDye());
                data.setEyeColorLeft(mcaVillager.getEyeLeftDye());
                if (mcaVillager.getTraits() != null) {
                    for (var t : mcaVillager.getTraits().getTraits()) {
                        data.addTrait(t.getId().toString());
                    }
                }
                data.setAge(0);
                data.setInherited(true);
                CitizenMCAManager.saveData(newCitizen, data);
            }

            // 5. Transfer MCA FamilyTree node to new citizen entity
            FamilyTreeNode newNode = tree.getOrCreate(newCitizen);
            for (FamilyTreeNode pNode : parentNodes) {
                newNode.assignParent(pNode);
            }
            if (player != null) {
                newNode.assignParent(tree.getOrCreate(player));
            }

            // 6. Discard the temporary MCA villager entity so it is not duplicated
            mcaVillager.discard();

            // 7. Visual and auditory celebrations
            level.playSound(null, newCitizen.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0F, 1.4F);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, newCitizen.getX(), newCitizen.getEyeY() + 0.5, newCitizen.getZ(), 15, 0.4, 0.4, 0.4, 0.1);

            if (player != null) {
                player.displayClientMessage(
                        Component.literal("§aA newborn child (§f" + childName + "§a) has officially been registered into the colony of §6" + colony.getName() + "§a!"),
                        false
                );
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
