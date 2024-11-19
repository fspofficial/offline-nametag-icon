package com.syanicxd.offlinenametagicon;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class offlinenametagiconMod implements ModInitializer {
    public static final String MOD_ID = "offlinenametagicon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Offline Nametag Badge Mod");
    }
}