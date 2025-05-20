package com.forgestove.bottle_ship.content.config;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.fml.ModLoadingContext;
public class BSConfig {
	public static BSConfigData config;
	public static void register() {
		config = AutoConfig.register(BSConfigData.class, Toml4jConfigSerializer::new).getConfig();
		var factory = new ConfigScreenFactory((mc, screen) -> AutoConfig.getConfigScreen(BSConfigData.class, screen).get());
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenFactory.class, () -> factory);
	}
}
