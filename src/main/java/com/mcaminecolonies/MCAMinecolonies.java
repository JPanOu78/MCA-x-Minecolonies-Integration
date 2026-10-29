package com.mcaminecolonies;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MCAMinecolonies.MOD_ID)
public class MCAMinecolonies {
    public static final String MOD_ID = "mcaminecolonies";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public MCAMinecolonies(IEventBus modEventBus) {
        LOGGER.info("Initializing MCA x Minecolonies: Citizen MCA Reskin Mod...");
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("MCA x Minecolonies setup complete - Citizen visual override active.");
    }
}

