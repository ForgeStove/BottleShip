package com.forgestove.bottle_ship;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import static net.minecraftforge.common.ForgeConfigSpec.Builder;
public class Config {
	public static ConfigValue<Integer> bottleWithoutShipChargeTime;
	public static ConfigValue<Integer> bottleWithoutShipCooldown;
	public static ConfigValue<Integer> bottleWithShipChargeTime;
	public static ConfigValue<Integer> bottleWithShipCooldown;
	public static ConfigValue<Integer> bottleWithShipChargeStrength;
	public static final Pair<Config, ForgeConfigSpec> specPair = new Builder().configure(Config::new);
	public static final ForgeConfigSpec CONFIG_SPEC = specPair.getValue();
	public Config(@NotNull Builder builder) {
		bottleWithoutShipChargeTime = builder.defineInRange("bottleWithoutShipChargeTime/ms", 1000, 0, 72000);
		bottleWithoutShipCooldown = builder.defineInRange("bottleWithoutShipCooldown/tick", 60, 0, Integer.MAX_VALUE);
		bottleWithShipChargeTime = builder.defineInRange("bottleWithShipChargeTime/ms", 1000, 0, 72000);
		bottleWithShipCooldown = builder.defineInRange("bottleWithShipCooldown/tick", 60, 0, Integer.MAX_VALUE);
		bottleWithShipChargeStrength = builder.defineInRange(
				"bottleWithShipChargeStrength/s/block",
				5,
				0,
				Integer.MAX_VALUE
		);
	}
}
