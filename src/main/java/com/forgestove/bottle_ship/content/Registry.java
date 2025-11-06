package com.forgestove.bottle_ship.content;
import com.forgestove.bottle_ship.content.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

import static com.forgestove.bottle_ship.BottleShip.ID;
public class Registry {
	public static final RegistryObject<Item> BOTTLE_WITHOUT_SHIP;
	public static final RegistryObject<Item> BOTTLE_WITH_SHIP;
	public static final RegistryObject<CreativeModeTab> TAB_REGISTRY;
	public static final DeferredRegister<CreativeModeTab> TAB_REGISTER;
	public static final DeferredRegister<Item> ITEM_REGISTER;
	static {
		TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
		ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
		BOTTLE_WITHOUT_SHIP = ITEM_REGISTER.register("bottle_without_ship", () -> new BottleWithoutShipItem(new Item.Properties()));
		BOTTLE_WITH_SHIP = ITEM_REGISTER.register(
			"bottle_with_ship",
			() -> new BottleWithShipItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant())
		);
		TAB_REGISTRY = TAB_REGISTER.register(
			"tab." + ID,
			() -> CreativeModeTab.builder()
				.title(Component.translatable("tab." + ID))
				.icon(() -> BOTTLE_WITH_SHIP.get().getDefaultInstance())
				.displayItems((parameters, output) -> output.accept(BOTTLE_WITHOUT_SHIP.get()))
				.build()
		);
	}
	public static void register(IEventBus bus) {
		ITEM_REGISTER.register(bus);
		TAB_REGISTER.register(bus);
	}
}
