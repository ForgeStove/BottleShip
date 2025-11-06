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
import org.jetbrains.annotations.*;
import org.joml.*;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.apigame.ShipTeleportData;
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.lang.Math;
import java.util.*;
public class BottleItemHelper {
	/**
	 * 传送船只及其所有连接的船只
	 *
	 * @param level 服务器世界
	 * @param ship  主船只
	 * @param x     目标 X 坐标
	 * @param y     目标 Y 坐标
	 * @param z     目标 Z 坐标
	 */
	public static void teleportShip(@NotNull ServerLevel level, @NotNull ServerShip ship, double x, double y, double z) {
		var connectedShipIds = traverseGetAllTouchingShips(level, ship.getId());
		var minScale = getMinScale(level, ship, connectedShipIds);
		Vector3dc targetPos = new Vector3d(x, y, z);
		var targetRotation = ship.getTransform().getShipToWorldRotation();
		var scaling = ship.getTransform().getShipToWorldScaling();
		var currentScale = (scaling.x() + scaling.y() + scaling.z()) / 3.0;
		var scaleBy = minScale != currentScale ? currentScale / minScale : 1.0;
		var shipObjectWorld = VSGameUtilsKt.getShipObjectWorld(level);
		for (long otherShipId : connectedShipIds) {
			if (otherShipId == ship.getId()) continue;
			var otherShip = shipObjectWorld.getLoadedShips().getById(otherShipId);
			if (otherShip != null) {
				var otherPosWorld = otherShip.getTransform().getPositionInWorld();
				var mainPosWorld = ship.getTransform().getPositionInWorld();
				var relativePos = new Vector3d(otherPosWorld).sub(mainPosWorld);
				var mainRotInv = new Quaterniond(ship.getTransform().getShipToWorldRotation()).invert();
				relativePos.rotate(mainRotInv);
				var newPos = new Vector3d(relativePos);
				newPos.rotate(targetRotation);
				newPos.add(targetPos);
				var diff = new Quaterniond(targetRotation).mul(mainRotInv);
				var newRotation = new Quaterniond(diff).mul(otherShip.getTransform().getShipToWorldRotation());
				var otherScaling = otherShip.getTransform().getShipToWorldScaling();
				var otherScale = (otherScaling.x() + otherScaling.y() + otherScaling.z()) / 3.0;
				var newScale = otherScale * scaleBy;
				ShipTeleportData teleportData = new ShipTeleportDataImpl(
					newPos,
					newRotation,
					otherShip.getVelocity(),
					otherShip.getOmega(),
					otherShip.getChunkClaimDimension(),
					newScale
				);
				shipObjectWorld.teleportShip(otherShip, teleportData);
			}
		}
		ShipTeleportData mainTeleportData = new ShipTeleportDataImpl(
			targetPos,
			targetRotation,
			ship.getVelocity(),
			ship.getOmega(),
			ship.getChunkClaimDimension(),
			currentScale * scaleBy
		);
		shipObjectWorld.teleportShip(ship, mainTeleportData);
	}
	/**
	 * 获取所有船只中的最小缩放值
	 *
	 * @param level            服务器世界
	 * @param mainShip         主船只
	 * @param connectedShipIds 连接的船只 ID 集合
	 * @return 最小缩放值
	 */
	private static double getMinScale(@NotNull ServerLevel level, @NotNull ServerShip mainShip, @NotNull Set<Long> connectedShipIds) {
		List<Double> scales = new ArrayList<>();
		var mainScaling = mainShip.getTransform().getShipToWorldScaling();
		scales.add((mainScaling.x() + mainScaling.y() + mainScaling.z()) / 3.0);
		for (long shipId : connectedShipIds) {
			var ship = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
			if (ship != null) {
				var scaling = ship.getTransform().getShipToWorldScaling();
				scales.add((scaling.x() + scaling.y() + scaling.z()) / 3.0);
			}
		}
		return scales.stream().min(Double::compare).orElse(1.0);
	}
	public static @NotNull Set<Long> traverseGetAllTouchingShips(@NotNull ServerLevel level, long shipId) {
		var shipObjectWorld = VSGameUtilsKt.getShipObjectWorld(level);
		Set<Long> dimensionIds = new HashSet<>(shipObjectWorld.getDimensionToGroundBodyIdImmutable().values());
		List<Long> stack = new ArrayList<>();
		stack.add(shipId);
		Set<Long> traversedShips = new LinkedHashSet<>();
		while (!stack.isEmpty()) {
			long currentShipId = stack.remove(stack.size() - 1);
			if (traversedShips.contains(currentShipId) || dimensionIds.contains(currentShipId)) continue;
			traversedShips.add(currentShipId);
			var ship = shipObjectWorld.getAllShips().getById(currentShipId);
			if (ship != null) for (var intersectingShip : VSGameUtilsKt.getShipsIntersecting(level, ship.getWorldAABB())) {
				var id = intersectingShip.getId();
				if (!traversedShips.contains(id) && !dimensionIds.contains(id)) stack.add(id);
			}
		}
		return traversedShips;
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
