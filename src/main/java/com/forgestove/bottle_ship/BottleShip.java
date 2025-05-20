package com.forgestove.bottle_ship;
import com.forgestove.bottle_ship.content.config.BSConfig;
import com.forgestove.bottle_ship.content.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
@Mod(BottleShip.ID)
public class BottleShip {
	public static final String ID = "bottle_ship";
	public static final RegistryObject<Item> BOTTLE_WITHOUT_SHIP;
	public static final RegistryObject<Item> BOTTLE_WITH_SHIP;
	public static final RegistryObject<CreativeModeTab> TAB_REGISTRY_OBJECT;
	public static final DeferredRegister<Item> ITEM_DEFERRED_REGISTER;
	public static final DeferredRegister<CreativeModeTab> TAB_DEFERRED_REGISTER;
	static {
		ITEM_DEFERRED_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
		TAB_DEFERRED_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
		BOTTLE_WITHOUT_SHIP = ITEM_DEFERRED_REGISTER.register("bottle_without_ship", () -> new BottleWithoutShipItem(new Properties()));
		BOTTLE_WITH_SHIP = ITEM_DEFERRED_REGISTER.register(
			"bottle_with_ship",
			() -> new BottleWithShipItem(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant())
		);
		TAB_REGISTRY_OBJECT = TAB_DEFERRED_REGISTER.register(
			"tab." + ID,
			() -> CreativeModeTab.builder().title(Component.translatable("tab." + ID))
								 .icon(() -> BOTTLE_WITH_SHIP.get().getDefaultInstance())
								 .displayItems((_, output) -> output.accept(BOTTLE_WITHOUT_SHIP.get())).build()
		);
	}
	public BottleShip() {
		var eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEM_DEFERRED_REGISTER.register(eventBus);
		TAB_DEFERRED_REGISTER.register(eventBus);
		BSConfig.register();
	}
}
