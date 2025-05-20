package com.forgestove.bottle_ship.content.config;
import com.forgestove.bottle_ship.BottleShip;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.BoundedDiscrete;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.CollapsibleObject;
@Config(name = BottleShip.ID)
public class BSConfigData implements ConfigData {
	@CollapsibleObject(startExpanded = true) public BottleWithShip bottleWithShip = new BottleWithShip();
	@CollapsibleObject(startExpanded = true) public BottleWithoutShip bottleWithoutShip = new BottleWithoutShip();
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
