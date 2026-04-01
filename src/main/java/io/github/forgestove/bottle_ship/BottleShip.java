package io.github.forgestove.bottle_ship;
import com.mojang.logging.LogUtils;
import io.github.forgestove.bottle_ship.content.BSRegistry;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
@Mod(BottleShip.ID)
public class BottleShip {
	public static final String ID = "bottle_ship";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final BSConfig config = AutoConfig.register(BSConfig.class, Toml4jConfigSerializer::new).getConfig();
	public BottleShip() {
		BSRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
		if (FMLEnvironment.dist.isClient()) BSConfig.register();
	}
}
