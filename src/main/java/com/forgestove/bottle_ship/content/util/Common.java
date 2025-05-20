package com.forgestove.bottle_ship.content.util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.spaceeye.vmod.utils.Vector3d;
import net.spaceeye.vmod.utils.vs.TeleportShipWithConnectedKt;
import org.jetbrains.annotations.*;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
public class Common {
	public static void teleportShip(@NotNull ServerLevel level, @NotNull ServerShip ship, double x, double y, double z) {
		var scaling = ship.getTransform().getShipToWorldScaling();
		TeleportShipWithConnectedKt.teleportShipWithConnected(
			level,
			ship,
			new Vector3d(x, y, z),
			ship.getTransform().getShipToWorldRotation(),
			(scaling.x() + scaling.y() + scaling.z()) / 3,
			VSGameUtilsKt.getDimensionId(level)
		);
	}
	public static void setItem(
		@NotNull ItemStack itemStack,
		@NotNull Level level,
		@NotNull Player player,
		@NotNull ItemStack newStack,
		int configValue,
		SoundEvent soundEvent
	) {
		player.getCooldowns().addCooldown(newStack.getItem(), configValue);
		if (itemStack.getCount() != 1) {
			itemStack.shrink(1);
			player.addItem(newStack);
		} else player.setItemInHand(player.getUsedItemHand(), newStack);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
	}
}
