package io.github.forgestove.bottle_ship.content.util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.ClipContext.*;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.*;
import org.joml.*;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.lang.Math;
import java.util.*;
public class BottleItemHelper {
	/**
	 * 传送船只及其所有连接的船只
	 *
	 * @param targetLevel 服务器世界
	 * @param mainShip    主船只
	 * @param x           目标 X 坐标
	 * @param y           目标 Y 坐标
	 * @param z           目标 Z 坐标
	 */
	public static void teleportShip(
		@NotNull ServerLevel targetLevel,
		@NotNull ServerShip mainShip,
		double x,
		double y,
		double z,
		boolean toStatic
	) {
		var connectedShips = getTouchedShips(targetLevel, mainShip);
		var targetRotation = mainShip.getTransform().getShipToWorldRotation();
		var shipObjectWorld = VSGameUtilsKt.getShipObjectWorld(targetLevel);
		var dimensionId = VSGameUtilsKt.getDimensionId(targetLevel);
		for (var ship : connectedShips) {
			ship.setStatic(true);
			teleportShipEntity(targetLevel, mainShip, x, y, z, ship, targetRotation);
			var otherPosWorld = new Vector3d(ship.getTransform().getPositionInWorld());
			var mainPosWorld = mainShip.getTransform().getPositionInWorld();
			var mainRotInv = new Quaterniond(mainShip.getTransform().getShipToWorldRotation()).invert();
			var teleportData = new ShipTeleportDataImpl(
				otherPosWorld.sub(mainPosWorld).rotate(mainRotInv).rotate(targetRotation).add(x, y, z),
				new Quaterniond(targetRotation).mul(mainRotInv).mul(ship.getTransform().getShipToWorldRotation()),
				ship.getVelocity(),
				ship.getOmega(),
				dimensionId,
				null,
				null
			);
			shipObjectWorld.teleportShip(ship, teleportData);
			ship.setStatic(toStatic);
		}
	}
	private static void teleportShipEntity(
		@NotNull ServerLevel targetLevel,
		@NotNull ServerShip mainShip,
		double x,
		double y,
		double z,
		@NotNull ServerShip ship,
		Quaterniondc targetRotation
	) {
		var box = ship.getWorldAABB();
		var area = new AABB(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
		for (var entity : targetLevel.getEntitiesOfClass(Entity.class, area)) {
			if (entity instanceof Player player) {
				player.stopRiding();
				continue;
			}
			if (targetLevel.equals(entity.level())) continue;
			var entityPosWorld = new Vector3d(entity.getX(), entity.getY(), entity.getZ());
			var shipPosWorld = new Vector3d(ship.getTransform().getPositionInWorld());
			var relativePos = entityPosWorld.sub(shipPosWorld);
			var mainRotInv = new Quaterniond(mainShip.getTransform().getShipToWorldRotation()).invert();
			var newPos = relativePos.rotate(mainRotInv).rotate(targetRotation).add(x, y, z);
			entity.teleportTo(targetLevel, newPos.x, newPos.y, newPos.z, Set.of(), entity.getYRot(), entity.getXRot());
		}
	}
	public static @NotNull LinkedHashSet<ServerShip> getTouchedShips(@NotNull ServerLevel level, @NotNull ServerShip ship) {
		var shipObjectWorld = VSGameUtilsKt.getShipObjectWorld(level);
		var dimensionIds = shipObjectWorld.getDimensionToGroundBodyIdImmutable().values();
		var stack = new ArrayList<ServerShip>();
		stack.add(ship);
		var traversedShips = new LinkedHashSet<ServerShip>();
		// 用于记录船只所在的维度
		var shipDimensions = new HashMap<ServerShip, ServerLevel>();
		shipDimensions.put(ship, level);
		while (!stack.isEmpty()) {
			var currentShip = stack.remove(stack.size() - 1);
			if (traversedShips.contains(currentShip) || dimensionIds.contains(currentShip.getId())) continue;
			traversedShips.add(currentShip);
			// 获取当前船只所在的维度
			var currentDimension = shipDimensions.get(currentShip);
			if (currentDimension == null) continue;
			// 只在当前船只所在的维度中查找相交的船只
			for (var intersectingShip : VSGameUtilsKt.getShipsIntersecting(currentDimension, currentShip.getWorldAABB())) {
				if (!(intersectingShip instanceof ServerShip serverShip)) continue;
				if (!traversedShips.contains(serverShip) && !dimensionIds.contains(serverShip.getId())) {
					stack.add(serverShip);
					shipDimensions.put(serverShip, currentDimension);
				}
			}
		}
		traversedShips.remove(ship);
		traversedShips.add(ship);
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
	@Nullable
	public static ServerPlayer getPlayer(@NotNull Level level, @NotNull LivingEntity livingEntity, int chargeTime) {
		if (level.isClientSide() || !(livingEntity instanceof ServerPlayer player)) return null;
		if (chargeTime != 0) return player;
		player.releaseUsingItem();
		return null;
	}
	public static void showProgress(int chargeTime, @NotNull Player player) {
		var count = Math.max(0, Math.min(20, player.getTicksUsingItem() * 20 / chargeTime));
		var component = Component.literal("§a" + "■".repeat(count) + "§c" + "■".repeat(20 - count));
		player.displayClientMessage(component, true);
	}
	@Nullable
	public static ServerShip getTargetShip(@NotNull ServerLevel level, @NotNull Player player) {
		var eyePosition = player.getEyePosition(1.0F);
		var hitResult = level.clip(new ClipContext(
			eyePosition,
			eyePosition.add(player.getLookAngle().scale(player.getBlockReach())),
			Block.OUTLINE,
			Fluid.NONE,
			player
		));
		return VSGameUtilsKt.getShipManagingPos(level, hitResult.getBlockPos());
	}
}
