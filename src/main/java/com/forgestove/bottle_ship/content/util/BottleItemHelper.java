package com.forgestove.bottle_ship.content.util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.ClipContext.*;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.*;
import org.joml.*;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.apigame.ShipTeleportData;
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.*;
public class BottleItemHelper {
	/**
	 * 传送船只及其所有连接的船只
	 *
	 * @param level    服务器世界
	 * @param mainShip 主船只
	 * @param x        目标 X 坐标
	 * @param y        目标 Y 坐标
	 * @param z        目标 Z 坐标
	 */
	public static void teleportShip(
		@NotNull ServerLevel level,
		@NotNull ServerShip mainShip,
		double x,
		double y,
		double z,
		boolean toStatic
	) {
		var connectedShips = getTouchedShips(level, mainShip);
		connectedShips.remove(mainShip);
		connectedShips.add(mainShip);
		var targetRotation = mainShip.getTransform().getShipToWorldRotation();
		var shipObjectWorld = VSGameUtilsKt.getShipObjectWorld(level);
		var dimensionId = VSGameUtilsKt.getDimensionId(level);
		for (var ship : connectedShips) {
			for (var player : level.getEntitiesOfClass(ServerPlayer.class, toAABB(ship.getWorldAABB())))
				player.stopRiding();
			var otherPosWorld = new Vector3d(ship.getTransform().getPositionInWorld());
			var mainPosWorld = mainShip.getTransform().getPositionInWorld();
			var mainRotInv = new Quaterniond(mainShip.getTransform().getShipToWorldRotation()).invert();
			var scaling = ship.getTransform().getShipToWorldScaling();
			ShipTeleportData teleportData = new ShipTeleportDataImpl(
				otherPosWorld.sub(mainPosWorld).rotate(mainRotInv).rotate(targetRotation).add(x, y, z),
				new Quaterniond(targetRotation).mul(mainRotInv).mul(ship.getTransform().getShipToWorldRotation()),
				ship.getVelocity(),
				ship.getOmega(),
				dimensionId,
				(scaling.x() + scaling.y() + scaling.z()) / 3.0
			);
			shipObjectWorld.teleportShip(ship, teleportData);
			ship.setStatic(toStatic);
		}
	}
	public static @NotNull LinkedHashSet<ServerShip> getTouchedShips(@NotNull ServerLevel level, @NotNull ServerShip ship) {
		var dimensionIds = VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable().values();
		var stack = new ArrayList<ServerShip>();
		stack.add(ship);
		var traversedShips = new LinkedHashSet<ServerShip>();
		while (!stack.isEmpty()) {
			var currentShip = stack.remove(stack.size() - 1);
			if (traversedShips.contains(currentShip) || dimensionIds.contains(currentShip.getId())) continue;
			traversedShips.add(currentShip);
			for (var intersectingShip : VSGameUtilsKt.getShipsIntersecting(level, currentShip.getWorldAABB())) {
				if (!(intersectingShip instanceof ServerShip serverShip)) continue;
				if (!traversedShips.contains(serverShip) && !dimensionIds.contains(serverShip.getId())) stack.add(serverShip);
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
	@Contract("_ -> new")
	public static @NotNull AABB toAABB(AABBdc box) {
		return new AABB(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
	}
}
