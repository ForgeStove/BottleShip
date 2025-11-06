package com.forgestove.bottle_ship;
import com.forgestove.bottle_ship.content.Registry;
import com.mojang.logging.LogUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
@Mod(BottleShip.ID)
public class BottleShip {
	public static final String ID = "bottle_ship";
	public static final BSConfig CONFIG = AutoConfig.register(BSConfig.class, Toml4jConfigSerializer::new).getConfig();
	public static final Logger LOGGER = LogUtils.getLogger();
	public BottleShip() {
		Registry.register(FMLJavaModLoadingContext.get().getModEventBus());
		if (FMLEnvironment.dist.isClient()) BSConfig.register();
	}
}
