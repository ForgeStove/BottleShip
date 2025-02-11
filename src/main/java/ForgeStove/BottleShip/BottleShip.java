package ForgeStove.BottleShip;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import org.jetbrains.annotations.NotNull;

import static ForgeStove.BottleShip.BottleShip.MOD_ID;
import static ForgeStove.BottleShip.Config.CONFIG_SPEC;
import static net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB;
import static net.minecraft.network.chat.Component.translatable;
import static net.minecraft.world.item.CreativeModeTab.builder;
import static net.minecraft.world.item.Item.Properties;
import static net.minecraft.world.item.Rarity.UNCOMMON;
import static net.minecraftforge.fml.config.ModConfig.Type.SERVER;
import static net.minecraftforge.registries.DeferredRegister.create;
import static net.minecraftforge.registries.ForgeRegistries.ITEMS;
@Mod(MOD_ID) public class BottleShip {
	public static final String MOD_ID = "bottle_ship";
	public static final RegistryObject<Item> BOTTLE_WITHOUT_SHIP;
	public static final RegistryObject<Item> BOTTLE_WITH_SHIP;
	public static final RegistryObject<CreativeModeTab> TAB_REGISTRY_OBJECT;
	public static final DeferredRegister<Item> ITEM_DEFERRED_REGISTER;
	public static final DeferredRegister<CreativeModeTab> TAB_DEFERRED_REGISTER;
	static {
		ITEM_DEFERRED_REGISTER = create(ITEMS, MOD_ID);
		TAB_DEFERRED_REGISTER = create(CREATIVE_MODE_TAB, MOD_ID);
		BOTTLE_WITHOUT_SHIP = ITEM_DEFERRED_REGISTER.register(
				"bottle_without_ship",
				() -> new BottleWithoutShipItem(new Properties())
		);
		BOTTLE_WITH_SHIP = ITEM_DEFERRED_REGISTER.register(
				"bottle_with_ship",
				() -> new BottleWithShipItem(new Properties().stacksTo(1).rarity(UNCOMMON).fireResistant())
		);
		TAB_REGISTRY_OBJECT = TAB_DEFERRED_REGISTER.register(
				"tab." + MOD_ID,
				() -> builder().title(translatable("tab." + MOD_ID))
						.icon(() -> BOTTLE_WITH_SHIP.get().getDefaultInstance())
						.displayItems((parameters, output) -> output.accept(BOTTLE_WITHOUT_SHIP.get()))
						.build()
		);
	}
	public BottleShip(@NotNull FMLJavaModLoadingContext context) {
		IEventBus modEventBus = context.getModEventBus();
		context.registerConfig(SERVER, CONFIG_SPEC);
		ITEM_DEFERRED_REGISTER.register(modEventBus);
		TAB_DEFERRED_REGISTER.register(modEventBus);
	}
}
