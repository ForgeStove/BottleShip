package com.forgestove.bottle_ship.content.util;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.ClipContext.*;
import net.minecraft.world.phys.Vec3;
import net.spaceeye.vmod.utils.Vector3d;
import net.spaceeye.vmod.utils.vs.TeleportShipWithConnectedKt;
import org.jetbrains.annotations.*;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
public class BottleItemHelper {
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
	@Contract(value = "_, _ -> new", pure = true)
	public static @NotNull MutableComponent translate(@NotNull String key, @NotNull Object... args) {
		return Component.translatable(key, args);
	}
	@Contract(value = "_ -> new", pure = true)
	public static @NotNull MutableComponent literal(@NotNull String text) {
		return Component.literal(text);
	}
	@Nullable
	public static ServerPlayer getPlayer(@NotNull Level level, @NotNull LivingEntity livingEntity, int chargeTime) {
		if (level.isClientSide() || !(livingEntity instanceof ServerPlayer player)) return null;
		if (chargeTime != 0) return player;
		player.releaseUsingItem();
		return null;
	}
	public static void showProgress(int chargeTime, @NotNull Player player) {
		var progress = player.getTicksUsingItem() * 20 / chargeTime;
		var progressBar = new StringBuilder();
		for (var i = 0; i < 20; i++)
			if (i < progress) progressBar.append("§a■");
			else progressBar.append("§c■");
		player.displayClientMessage(Component.literal(progressBar.toString()), true);
	}
	@Nullable
	public static ServerShip getTargetShip(@NotNull ServerLevel level, @NotNull Player player) {
		var hitResult = level.clip(new ClipContext(
			player.getEyePosition(1.0F),
			player.getEyePosition(1.0F).add(player.getLookAngle().scale(player.getBlockReach())),
			Block.OUTLINE,
			Fluid.NONE,
			player
		));
		return VSGameUtilsKt.getShipManagingPos(level, hitResult.getBlockPos());
	}
	public static Vec3 getPlayerLookDirection(@NotNull Player player) {
		var yaw = Math.toRadians(player.getYRot());
		var pitch = Math.toRadians(player.getXRot());
		var dx = -Math.sin(yaw) * Math.cos(pitch);
		var dy = -Math.sin(pitch);
		var dz = Math.cos(yaw) * Math.cos(pitch);
		return new Vec3(dx, dy, dz);
	}
}
