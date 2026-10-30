package com.mcaminecolonies.entity;

import com.mcaminecolonies.data.CitizenMCAData;
import com.mcaminecolonies.data.CitizenMCAManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.conczin.mca.Config;
import net.conczin.mca.MCA;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.Memories;
import net.conczin.mca.entity.ai.Relationship;
import net.conczin.mca.entity.ai.relationship.CompassionateEntity;
import net.conczin.mca.entity.interaction.gifts.GiftSaturation;
import net.conczin.mca.entity.interaction.gifts.GiftType;
import net.conczin.mca.entity.interaction.gifts.Response;
import net.conczin.mca.item.SpecialCaseGift;
import net.conczin.mca.network.Network;
import net.conczin.mca.network.s2c.AnalysisResults;
import net.conczin.mca.registry.CriterionMCA;
import net.conczin.mca.resources.data.Analysis;
import net.conczin.mca.util.InventoryUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.Optional;

/**
 * Handles gift giving interactions between players and MineColonies citizens according to MCA Reborn rules:
 * validates gift preferences, calculates satisfaction based on traits/personality, enforces gift saturation
 * limits (diminishing returns & rejection upon repeated gifts), and checks fatigue.
 */
public class CitizenGiftHelper {

    public static boolean handleGift(ServerPlayer player, AbstractEntityCitizen citizen, ItemStack held) {
        if (held == null || held.isEmpty()) {
            return false;
        }

        // 1. Check MCA relationship items (Wedding Ring, Engagement Ring, Bouquet, Divorce Papers)
        if (CitizenRelationshipHelper.isRelationshipItem(held)) {
            return CitizenRelationshipHelper.handleRelationshipItem(player, held, citizen);
        }

        VillagerEntityMCA delegate = CitizenDialogueHandler.getOrCreateDelegate(citizen, player);

        // 2. Check SpecialCaseGift
        if (held.getItem() instanceof SpecialCaseGift scg) {
            scg.handle(player, delegate);
            return true;
        }

        // 3. Check fatigue: if citizen is exhausted from too many interactions, reject gift
        CitizenMCAData data = CitizenMCAManager.getData(citizen);
        if (data != null && data.getFatigue() >= 4) {
            citizen.level().playSound(null, citizen.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
            if (citizen.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, citizen.getX(), citizen.getEyeY() + 0.5, citizen.getZ(), 5, 0.3, 0.3, 0.3, 0.1);
            }
            player.sendSystemMessage(Component.literal("<" + citizen.getDisplayName().getString() + "> ")
                    .append(Component.translatable("dialogue.chat.fail")));
            return true;
        }

        // 4. Match against MCA gift types (JSON datapacks + dynamic weapon/armor/food items)
        Optional<GiftType> giftTypeOpt = GiftType.bestMatching(delegate, held, player);
        if (giftTypeOpt.isEmpty()) {
            giftTypeOpt = getDynamicGift(held, citizen);
        }

        if (giftTypeOpt.isEmpty()) {
            // Not a recognized gift in MCA
            rejectGift(player, citizen, "gift.fail");
            return true;
        }

        // 5. Evaluate satisfaction
        GiftType giftType = giftTypeOpt.get();
        Analysis analysis = giftType.getSatisfactionFor(delegate, held, player);
        int baseSatisfaction = analysis.getTotal();
        Response response = giftType.getResponse(baseSatisfaction);

        // 6. Enforce MCA Gift Saturation (desaturation penalty for repeated gifts of the same item)
        GiftSaturation saturation = null;
        if (citizen instanceof CompassionateEntity<?> comp
                && comp.getRelationships() instanceof Relationship<?> rel) {
            saturation = rel.getGiftSaturation();
        }
        if (saturation == null) {
            saturation = delegate.getRelationships().getGiftSaturation();
        }

        int satCount = saturation.get(held);
        int desaturation = (int) (satCount * Config.getInstance().giftDesaturationFactor
                * Math.pow(Math.max(0, baseSatisfaction), Config.getInstance().giftDesaturationExponent));
        if (desaturation != 0) {
            analysis.add("desaturation", -desaturation);
        }

        int totalSatisfaction = analysis.getTotal();
        Response finalResponse = giftType.getResponse(totalSatisfaction);
        totalSatisfaction = (int) (totalSatisfaction * Config.getInstance().giftSatisfactionFactor);

        // Send analysis feedback packet to player client
        Network.sendToPlayer(new AnalysisResults(analysis), player);

        // 7. Check if rejected due to villager dislike
        if (response == Response.FAIL) {
            rejectGift(player, citizen, giftType.getDialogueFor(Response.FAIL));
            return true;
        }

        // 8. Check if rejected due to saturation limit (too many of the same gift recently)
        if (finalResponse == Response.FAIL) {
            rejectGift(player, citizen, "gift.saturated");
            return true;
        }

        // 9. Gift Accepted!
        saturation.add(held);
        citizen.level().broadcastEntityEvent(citizen, (byte) 16); // Heart particles

        if (finalResponse == Response.BEST) {
            citizen.level().playSound(null, citizen.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.3F);
        } else {
            citizen.level().playSound(null, citizen.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.6F, 1.2F);
        }

        String dialogueKey = giftType.getDialogueFor(finalResponse);
        player.sendSystemMessage(Component.literal("<" + citizen.getDisplayName().getString() + "> ")
                .append(Component.translatable(dialogueKey)));

        // Consume 1 item from player hand
        ItemStack gifted = held.split(1);

        // Add to citizen inventory
        if (citizen.getInventoryCitizen() != null) {
            ItemHandlerHelper.insertItemStacked(citizen.getInventoryCitizen(), gifted, false);
        }

        // Adjust mood
        int moodChange = (int) (totalSatisfaction * Config.getInstance().giftMoodEffect
                + Config.getInstance().baseGiftMoodEffect * Math.signum(totalSatisfaction));
        delegate.getVillagerBrain().modifyMoodValue(moodChange);

        // Update hearts & fatigue
        if (data != null) {
            data.addHearts(totalSatisfaction);
            data.modFatigue(1);
            CitizenMCAManager.saveData(citizen, data);
        }

        Memories mem = delegate.getVillagerBrain().getMemoriesForPlayer(player);
        if (mem != null) {
            mem.modHearts(totalSatisfaction);
            mem.modInteractionFatigue(1);
        }

        if (citizen instanceof VillagerLike<?> vl) {
            Memories citMem = vl.getVillagerBrain().getMemoriesForPlayer(player);
            if (citMem != null) {
                citMem.modHearts(totalSatisfaction);
                citMem.modInteractionFatigue(1);
            }
        }

        // Trigger advancement
        CriterionMCA.HEARTS.trigger(player, data != null ? data.getHearts() : 50, totalSatisfaction, "gift");
        return true;
    }

    private static void rejectGift(ServerPlayer player, AbstractEntityCitizen citizen, String translationKey) {
        citizen.level().broadcastEntityEvent(citizen, (byte) 15); // Angry particles
        citizen.level().playSound(null, citizen.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
        player.sendSystemMessage(Component.literal("<" + citizen.getDisplayName().getString() + "> ")
                .append(Component.translatable(translationKey)));
    }

    private static Optional<GiftType> getDynamicGift(ItemStack stack, LivingEntity entity) {
        Item item = stack.getItem();
        if (item instanceof SwordItem) {
            double dmg = InventoryUtils.approximateDamage(stack, entity);
            dmg = Math.pow(dmg, 1.25) * 2.0;
            return Optional.of(new GiftType(item, (int) dmg, MCA.locate("swords")));
        }
        if (item instanceof ProjectileWeaponItem pwi) {
            float range = pwi.getDefaultProjectileRange();
            double score = Math.pow(range, 1.25) * 2.0;
            return Optional.of(new GiftType(item, (int) score, MCA.locate("archery")));
        }
        if (item instanceof TieredItem ti) {
            float speed = ti.getTier().getSpeed();
            double score = Math.pow(speed, 1.25) * 2.0;
            String category = "pickaxes";
            if (item instanceof AxeItem) category = "swords";
            else if (item instanceof HoeItem) category = "hoes";
            else if (item instanceof ShovelItem) category = "shovels";
            return Optional.of(new GiftType(item, (int) score, MCA.locate(category)));
        }
        if (item instanceof ArmorItem ai) {
            double def = Math.pow(ai.getDefense(), 1.25) * 1.5 + ai.getToughness() * 5.0;
            return Optional.of(new GiftType(item, (int) def, MCA.locate("armor")));
        }
        if (stack.has(DataComponents.FOOD)) {
            FoodProperties food = stack.get(DataComponents.FOOD);
            int nutrition = food != null ? food.nutrition() : 1;
            return Optional.of(new GiftType(item, nutrition * 2, MCA.locate("food")));
        }
        return Optional.empty();
    }
}
