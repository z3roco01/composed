package z3roco01.composed;

import net.fabricmc.api.ModInitializer;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
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
		Config config = new Config();
        try {
            ConfigFile.store("./fartttt", config);
        } catch (IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

	public class Config {
		@ConfigProperty
		public Item item = Items.ACACIA_LEAVES;
	}
}