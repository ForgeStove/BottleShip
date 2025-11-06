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
		// 获取所有连接和接触的船只
		var connectedShipIds = traverseGetAllTouchingShips(level, ship.getId());
		// 计算最小缩放值
		var minScale = getMinScale(level, ship, connectedShipIds);
		// 目标位置和旋转
		Vector3dc targetPos = new Vector3d(x, y, z);
		var targetRotation = ship.getTransform().getShipToWorldRotation();
		// 当前主船的缩放
		var scaling = ship.getTransform().getShipToWorldScaling();
		var currentScale = (scaling.x() + scaling.y() + scaling.z()) / 3.0;
		var scaleBy = minScale != currentScale ? currentScale / minScale : 1.0;
		// 先传送所有连接的船只
		for (long otherShipId : connectedShipIds) {
			if (otherShipId == ship.getId()) continue;
			var otherShip = VSGameUtilsKt.getShipObjectWorld(level).getLoadedShips().getById(otherShipId);
			if (otherShip != null) {
				// 计算相对于主船的位置
				var otherPosWorld = otherShip.getTransform().getPositionInWorld();
				var mainPosWorld = ship.getTransform().getPositionInWorld();
				// 世界坐标转换到主船坐标系
				var relativePos = new Vector3d(otherPosWorld).sub(mainPosWorld);
				var mainRotInv = new Quaterniond(ship.getTransform().getShipToWorldRotation()).invert();
				relativePos.rotate(mainRotInv);
				// 从主船坐标系转换到新的世界坐标
				var newPos = new Vector3d(relativePos);
				newPos.rotate(targetRotation);
				newPos.add(targetPos);
				// 计算新的旋转
				var diff = new Quaterniond(targetRotation).mul(mainRotInv);
				var newRotation = new Quaterniond(diff).mul(otherShip.getTransform().getShipToWorldRotation());
				// 计算新的缩放
				var otherScaling = otherShip.getTransform().getShipToWorldScaling();
				var otherScale = (otherScaling.x() + otherScaling.y() + otherScaling.z()) / 3.0;
				var newScale = otherScale * scaleBy;
				// 传送船只
				ShipTeleportData teleportData = new ShipTeleportDataImpl(
					newPos,
					newRotation,
					otherShip.getVelocity(),
					otherShip.getOmega(),
					otherShip.getChunkClaimDimension(),
					newScale
				);
				VSGameUtilsKt.getShipObjectWorld(level).teleportShip(otherShip, teleportData);
			}
		}
		// 最后传送主船
		var newMainScale = currentScale * scaleBy;
		ShipTeleportData mainTeleportData = new ShipTeleportDataImpl(
			targetPos,
			targetRotation,
			ship.getVelocity(),
			ship.getOmega(),
			ship.getChunkClaimDimension(),
			newMainScale
		);
		VSGameUtilsKt.getShipObjectWorld(level).teleportShip(ship, mainTeleportData);
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
		// 添加主船的缩放
		var mainScaling = mainShip.getTransform().getShipToWorldScaling();
		scales.add((mainScaling.x() + mainScaling.y() + mainScaling.z()) / 3.0);
		// 添加所有连接船只的缩放
		for (long shipId : connectedShipIds) {
			var ship = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
			if (ship != null) {
				var scaling = ship.getTransform().getShipToWorldScaling();
				scales.add((scaling.x() + scaling.y() + scaling.z()) / 3.0);
			}
		}
		return scales.stream().min(Double::compare).orElse(1.0);
	}
	/**
	 * 遍历并获取与指定船只接触的所有船只，通过 AABB 相交检测
	 *
	 * @param level  服务器世界
	 * @param shipId 起始船只 ID
	 * @return 所有连接和接触的船只 ID 集合
	 */
	public static @NotNull Set<Long> traverseGetAllTouchingShips(@NotNull ServerLevel level, long shipId) {
		// 获取维度 ID（每个维度的地面物体 ID）
		Set<Long> dimensionIds = new HashSet<>(VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable().values());
		// 用于处理船只的栈
		List<Long> stack = new ArrayList<>();
		stack.add(shipId);
		// 已遍历船只的集合
		Set<Long> traversedShips = new LinkedHashSet<>(dimensionIds);
		// 处理栈中的所有船只
		while (!stack.isEmpty()) {
			long currentShipId = stack.remove(stack.size() - 1); // 移除最后一个
			if (!traversedShips.contains(currentShipId)) {
				// 标记当前船只为已遍历
				traversedShips.add(currentShipId);
				// 获取船只对象并查找相交的船只
				var ship = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(currentShipId);
				// 获取与此船只的 AABB 相交的所有船只
				if (ship != null) for (var intersectingShip : VSGameUtilsKt.getShipsIntersecting(level, ship.getWorldAABB())) {
					var id = intersectingShip.getId();
					if (!traversedShips.contains(id) && !dimensionIds.contains(id)) stack.add(id);
				}
			}
		}
		// 从结果中移除维度 ID 中的船只
		traversedShips.removeAll(dimensionIds);
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
