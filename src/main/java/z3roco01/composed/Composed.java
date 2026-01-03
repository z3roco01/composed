package z3roco01.composed;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Composed implements ModInitializer {
	public static final String MOD_ID = "composed";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        TestConfig config = new TestConfig();
        config.pee.add("one");
        config.pee.add("six 7");

        try {
            ConfigFile.load("./config/test.conf", config);
        } catch (IOException | IllegalAccessException e) {
            LOGGER.error(e.toString());
        }
        LOGGER.info("#written");
        LOGGER.info(config.neww);

        for(String str : config.pee) {
            LOGGER.info(str);
        }
    }
}