package com.forgestove.bottle_ship.content.item;
import com.forgestove.bottle_ship.BottleShip;
import com.forgestove.bottle_ship.content.config.BSConfig;
import com.forgestove.bottle_ship.content.util.Common;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.List;
public class BottleWithShipItem extends BottleWithoutShipItem {
	public BottleWithShipItem(@NotNull Properties properties) {
		super(properties);
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemStack, Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
		if (level == null) return;
		var nbt = itemStack.getTag();
		if (nbt == null) return;
		var id = BottleShip.ID;
		tooltip.add(Component.translatable("tooltip.%s.id".formatted(id), Component.literal("§b%s§f".formatted(nbt.getString("ID")))));
		tooltip.add(Component.translatable("tooltip.%s.name".formatted(id), Component.literal("§b%s§f".formatted(nbt.getString("Name")))));
		tooltip.add(Component.translatable("tooltip.%s.size".formatted(id), Component.literal(nbt.getString("Size"))));
	}
	public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
		return InteractionResult.PASS;
	}
	@Override
	public void onUseTick(@NotNull Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack itemStack, int tickLeft) {
		var player = getPlayer(level, livingEntity, BSConfig.config.bottleWithShip.chargeTime);
		if (player == null) return;
		showProgress(BSConfig.config.bottleWithShip.chargeTime, player);
	}
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
		var currentStack = player.getItemInHand(hand);
		if (level.isClientSide()) return InteractionResultHolder.pass(currentStack);
		player.startUsingItem(hand);
		return InteractionResultHolder.consume(currentStack);
	}
	@Override
	public void releaseUsing(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull LivingEntity livingEntity, int tickLeft) {
		if (level.isClientSide()) return;
		var tickCount = getUseDuration(itemStack) - tickLeft;
		if (tickCount < BSConfig.config.bottleWithShip.chargeTime) return;
		var strength = tickCount / 20 * BSConfig.config.bottleWithShip.chargeStrength;
		if (!(livingEntity instanceof Player player)) return;
		var newStack = new ItemStack(BottleShip.BOTTLE_WITHOUT_SHIP.get());
		if (itemStack.getTag() == null) {
			player.setItemInHand(player.getUsedItemHand(), newStack);
			return;
		}
		var shipID = Long.parseLong(itemStack.getTag().getString("ID"));
		var playerPosition = player.position();
		var server = level.getServer();
		if (server == null) return;
		var ship = VSGameUtilsKt.getVsPipeline(server).getShipWorld().getAllShips().getById(shipID);
		if (ship == null) {
			player.setItemInHand(player.getUsedItemHand(), newStack);
			return;
		}
		var worldAABB = ship.getWorldAABB();
		var depth = worldAABB.maxY() - worldAABB.minY();
		var yawRadians = Math.toRadians(player.getYRot());
		var pitchRadians = Math.toRadians(player.getXRot());
		var dx = -Math.sin(yawRadians) * Math.cos(pitchRadians);
		var dy = -Math.sin(pitchRadians);
		var dz = Math.cos(yawRadians) * Math.cos(pitchRadians);
		var targetX = playerPosition.x + dx * strength;
		var targetY = playerPosition.y + dy * strength;
		var targetZ = playerPosition.z + dz * strength;
		if (ship.getShipAABB() == null) return;
		var massHeight = ship.getInertiaData().getCenterOfMassInShip().y() - ship.getShipAABB().minY();
		targetX += (dx * (depth / 2));
		targetY += (dy * massHeight);
		targetZ += (dz * (depth / 2));
		Common.teleportShip((ServerLevel) level, ship, targetX, targetY, targetZ);
		Common.setItem(itemStack, level, player, newStack, BSConfig.config.bottleWithShip.cooldown, SoundEvents.BOTTLE_EMPTY);
	}
}
