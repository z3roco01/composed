package z3roco01.composed;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import z3roco01.composed.annotation.ConfigProperty;
import z3roco01.composed.file.ConfigFile;

import java.io.IOException;
import java.util.ArrayList;

public class Composed implements ModInitializer {
	public static final String MOD_ID = "composed";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
    }

	public static class Config {
		public Config() {

		}

		@ConfigProperty
		public int asd = 10;
		@ConfigProperty
		public ArrayList<String> ward = new ArrayList<>();
	}
}