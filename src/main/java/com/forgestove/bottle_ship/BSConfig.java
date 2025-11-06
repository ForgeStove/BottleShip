package com.forgestove.bottle_ship;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.BoundedDiscrete;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.CollapsibleObject;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.fml.ModLoadingContext;
@Config(name = BottleShip.ID)
public class BSConfig implements ConfigData {
	@CollapsibleObject(startExpanded = true) public BottleWithShip bottleWithShip = new BottleWithShip();
	@CollapsibleObject(startExpanded = true) public BottleWithoutShip bottleWithoutShip = new BottleWithoutShip();
	public static void register() {
		var factory = new ConfigScreenFactory((mc, screen) -> AutoConfig.getConfigScreen(BSConfig.class, screen).get());
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenFactory.class, () -> factory);
	}
	public static class BottleWithShip {
		@BoundedDiscrete(max = 50) public int chargeStrength = 5;
		@BoundedDiscrete(max = 1440) public int chargeTime = 20;
		@BoundedDiscrete(max = 1440) public int cooldown = 60;
	}
	public static class BottleWithoutShip {
		@BoundedDiscrete(max = 1440) public int chargeTime = 20;
		@BoundedDiscrete(max = 1440) public int cooldown = 60;
	}
}
