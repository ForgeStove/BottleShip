package io.github.forgestove.bottle_ship;
import io.github.forgestove.bottle_ship.config.ConfigHandler;
import io.github.forgestove.bottle_ship.config.annotation.*;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
public class BSConfig {
	public static final ConfigHandler<BSConfig> CONFIG_HANDLER = ConfigHandler.builder(BSConfig.class)
		.path(() -> FMLPaths.CONFIGDIR.get().resolve(BottleShip.ID + ".toml"))
		.translationPrefix(BottleShip.ID + ".config")
		.translator(key -> I18n.exists(key) ? I18n.get(key) : null)
		.logger(BottleShip.LOGGER)
		.build();
	@ConfigCategory(ordinal = 1) public final BottleWithShip bottleWithShip = new BottleWithShip();
	@ConfigCategory(ordinal = 2) public final BottleWithoutShip bottleWithoutShip = new BottleWithoutShip();
	public static void init() {
		ModLoadingContext.get()
			.registerExtensionPoint(
				ConfigScreenFactory.class,
				() -> new ConfigScreenFactory((client, parent) -> CONFIG_HANDLER.createConfigScreen(parent))
			);
	}
	public static class BottleWithShip {
		@Range(max = 50) public int chargeStrength = 5;
		@Range(max = 1440) public int chargeTime = 20;
		@Range(max = 1440) public int cooldown = 60;
	}
	public static class BottleWithoutShip {
		@Range(max = 1440) public int chargeTime = 20;
		@Range(max = 1440) public int cooldown = 60;
	}
}
