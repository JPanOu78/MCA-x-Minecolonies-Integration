package com.mcaminecolonies.event;

import com.mcaminecolonies.client.appearance.CitizenAgeState;
import com.mcaminecolonies.client.gui.CitizenCustomizationScreen;
import com.mcaminecolonies.data.CitizenMCAData;
import com.mcaminecolonies.data.CitizenMCAManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Handles customizing citizens using MCA items, dyes, scissors/combs, potions, and growth food.
 */
@EventBusSubscriber(modid = "mcaminecolonies")
public class CitizenInteractionHandler {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof AbstractEntityCitizen citizen)) return;

        Player player = event.getEntity();
        ItemStack held = event.getItemStack();
        InteractionHand hand = event.getHand();

        if (held.isEmpty()) return;

        Item item = held.getItem();
        String itemId = BuiltInRegistries.ITEM.getKey(item).toString();

        CitizenMCAData data = CitizenMCAManager.getData(citizen);
        boolean modified = false;

        // 1. Dye Items -> Change Hair Color
        if (item instanceof DyeItem dyeItem) {
            int color = getDyeRgb(dyeItem.getDyeColor());
            data.setHairColor(color);
            data.setCustomized(true);
            modified = true;

            if (!player.isCreative()) {
                held.shrink(1);
            }

            playEffect(citizen, SoundEvents.DYE_USE, ParticleTypes.GLOW);
            player.displayClientMessage(Component.translatable("message.mcaminecolonies.hair_dyed"), true);
        }
        // 2. Comb / Shears -> Cycle Hairstyle
        else if (itemId.contains("comb") || item == Items.SHEARS) {
            data.setHairIndex(data.getHairIndex() + 1);
            data.setCustomized(true);
            modified = true;

            playEffect(citizen, SoundEvents.SHEEP_SHEAR, ParticleTypes.CRIT);
            player.displayClientMessage(Component.literal("Hairstyle: #" + (data.getHairIndex() + 1)), true);
        }
        // 3. Needle & Thread -> Cycle Clothing
        else if (itemId.contains("needle") || itemId.contains("sewing")) {
            data.setClothingIndex(data.getClothingIndex() + 1);
            data.setCustomized(true);
            modified = true;

            playEffect(citizen, SoundEvents.ARMOR_EQUIP_LEATHER.value(), ParticleTypes.HAPPY_VILLAGER);
            player.displayClientMessage(Component.literal("Clothing Outfit: #" + (data.getClothingIndex() + 1)), true);
        }
        // 4. Potion of Femininity -> Set Female
        else if (itemId.contains("potion_of_femininity") || itemId.contains("feminine")) {
            citizen.setFemale(true);
            data.setFemale(true);
            data.setCustomized(true);
            modified = true;

            if (!player.isCreative()) {
                held.shrink(1);
                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }

            playEffect(citizen, SoundEvents.BREWING_STAND_BREW, ParticleTypes.HEART);
            player.displayClientMessage(Component.translatable("message.mcaminecolonies.gender_female"), true);
        }
        // 5. Potion of Masculinity -> Set Male
        else if (itemId.contains("potion_of_masculinity") || itemId.contains("masculine")) {
            citizen.setFemale(false);
            data.setFemale(false);
            data.setCustomized(true);
            modified = true;

            if (!player.isCreative()) {
                held.shrink(1);
                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }

            playEffect(citizen, SoundEvents.BREWING_STAND_BREW, ParticleTypes.HEART);
            player.displayClientMessage(Component.translatable("message.mcaminecolonies.gender_male"), true);
        }
        // 6. Food for growth (Golden Apple, Cake, Apple, Cookie) when citizen is not an adult
        else if (!data.getAgeState(citizen.isBaby()).isAdult() && (
                item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE ||
                item == Items.CAKE || item == Items.APPLE || item == Items.COOKIE || itemId.contains("baby_food")
        )) {
            int growthBonus = (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) ? 6000 : 2000;
            data.addAge(growthBonus);
            modified = true;

            if (!player.isCreative()) {
                held.shrink(1);
            }

            playEffect(citizen, SoundEvents.GENERIC_EAT, ParticleTypes.HAPPY_VILLAGER);

            if (data.getAge() >= CitizenAgeState.ADULT.getMinAge()) {
                citizen.setIsChild(false);
                data.setAge(CitizenAgeState.ADULT.getMinAge());
                player.displayClientMessage(Component.literal("§aThe child has fully grown into an adult citizen!"), true);
            } else {
                CitizenAgeState newState = data.getAgeState(citizen.isBaby());
                player.displayClientMessage(Component.literal("§eGrowth accelerated! Current stage: " + newState.getDisplayName()), true);
            }
        }
        // 7. Villager Editor / Dressing Mirror -> Open Customization Screen
        else if (itemId.contains("editor") || itemId.contains("mirror")) {
            if (event.getLevel().isClientSide()) {
                Minecraft.getInstance().setScreen(new CitizenCustomizationScreen(citizen));
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
            return;
        }

        if (modified) {
            CitizenMCAManager.saveData(citizen, data);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        }
    }

    private static void playEffect(AbstractEntityCitizen citizen, net.minecraft.sounds.SoundEvent sound, net.minecraft.core.particles.ParticleOptions particle) {
        if (citizen.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, citizen.getX(), citizen.getY(), citizen.getZ(),
                    sound, SoundSource.PLAYERS, 1.0F, 1.0F);
            serverLevel.sendParticles(particle,
                    citizen.getX(), citizen.getY() + 1.0, citizen.getZ(),
                    8, 0.3, 0.3, 0.3, 0.05);
        }
    }

    private static int getDyeRgb(DyeColor color) {
        return switch (color) {
            case WHITE -> 0xFFF0F0F0;
            case ORANGE -> 0xFFD87F33;
            case MAGENTA -> 0xFFB24CD8;
            case LIGHT_BLUE -> 0xFF6689D3;
            case YELLOW -> 0xFFE6C280; // Blonde
            case LIME -> 0xFF7FCC19;
            case PINK -> 0xFFF27FA5;
            case GRAY -> 0xFF4D4D4D;
            case LIGHT_GRAY -> 0xFF9E8774;
            case CYAN -> 0xFF169C9C;
            case PURPLE -> 0xFF7B2FBE;
            case BLUE -> 0xFF2C42A5;
            case BROWN -> 0xFF593A22;
            case GREEN -> 0xFF3B5919;
            case RED -> 0xFFB04A26; // Ginger / Auburn
            case BLACK -> 0xFF1E1B1B;
        };
    }
}

