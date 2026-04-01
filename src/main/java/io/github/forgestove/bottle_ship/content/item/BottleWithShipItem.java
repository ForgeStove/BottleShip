package io.github.forgestove.bottle_ship.content.item;
import io.github.forgestove.bottle_ship.content.BSRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.*;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.List;

import static io.github.forgestove.bottle_ship.BottleShip.*;
import static io.github.forgestove.bottle_ship.content.util.BottleItemHelper.*;
public class BottleWithShipItem extends Item {
	public BottleWithShipItem(@NotNull Properties properties) {
		super(properties);
	}
	@Contract("_ -> new")
	public static @NotNull Vec3 getPlayerLookDirection(@NotNull Player player) {
		var yaw = Math.toRadians(player.getYRot());
		var pitch = Math.toRadians(player.getXRot());
		var dx = -Math.sin(yaw) * Math.cos(pitch);
		var dy = -Math.sin(pitch);
		var dz = Math.cos(yaw) * Math.cos(pitch);
		return new Vec3(dx, dy, dz);
	}
	public static void releaseShipAtTarget(@NotNull ServerLevel level, @NotNull Player player, @NotNull ServerShip ship, double strength) {
		var direction = getPlayerLookDirection(player);
		var shipAABB = ship.getShipAABB();
		if (shipAABB == null) return;
		var worldAABB = ship.getWorldAABB();
		var depth = (worldAABB.maxY() - worldAABB.minY()) / 2.0;
		var massHeight = ship.getInertiaData().getCenterOfMassInShip().y() - shipAABB.minY();
		var playerPos = player.position();
		var targetX = playerPos.x + direction.x * (strength + depth);
		var targetY = playerPos.y + direction.y * (strength + massHeight);
		var targetZ = playerPos.z + direction.z * (strength + depth);
		teleportShip(level, ship, targetX, targetY, targetZ, false);
	}
	@Override
	public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
		return UseAnim.BOW;
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemStack, Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
		if (level == null) return;
		var nbt = itemStack.getTag();
		if (nbt == null) return;
		tooltip.add(Component.translatable("tooltip.%s.id".formatted(ID), Component.literal("§a%s§f".formatted(nbt.getString("ID")))));
		tooltip.add(Component.translatable("tooltip.%s.name".formatted(ID), Component.literal("§b%s§f".formatted(nbt.getString("Name")))));
		tooltip.add(Component.translatable("tooltip.%s.size".formatted(ID), Component.literal(nbt.getString("Size"))));
	}
	@Override
	public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
		return InteractionResult.PASS;
	}
	@Override
	public void onUseTick(@NotNull Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack itemStack, int tickLeft) {
		var player = getPlayer(level, livingEntity, config.bottleWithShip.chargeTime);
		if (player == null) return;
		showProgress(config.bottleWithShip.chargeTime, player);
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
		if (tickCount < config.bottleWithShip.chargeTime) return;
		if (!(livingEntity instanceof Player player)) return;
		var newStack = new ItemStack(BSRegistry.BOTTLE_WITHOUT_SHIP.get());
		var nbt = itemStack.getTag();
		if (nbt == null) {
			player.setItemInHand(player.getUsedItemHand(), newStack);
			return;
		}
		var ship = getShipFromNBT(nbt, level);
		if (ship == null) {
			player.setItemInHand(player.getUsedItemHand(), newStack);
			return;
		}
		var worldAABB = ship.getWorldAABB();
		var minDistance = Math.max(worldAABB.maxX() - worldAABB.minX(), worldAABB.maxZ() - worldAABB.minZ()) / 2.0 + 1;
		var strength = Math.max(minDistance, tickCount / 20.0 * config.bottleWithShip.chargeStrength);
		releaseShipAtTarget((ServerLevel) level, player, ship, strength);
		setItem(itemStack, level, player, newStack, config.bottleWithShip.cooldown, SoundEvents.BOTTLE_EMPTY);
	}
	@Nullable
	public ServerShip getShipFromNBT(@NotNull CompoundTag nbt, @NotNull Level level) {
		try {
			var shipID = Long.parseLong(nbt.getString("ID"));
			var server = level.getServer();
			if (server == null) return null;
			return VSGameUtilsKt.getVsPipeline(server).getShipWorld().getAllShips().getById(shipID);
		} catch (NumberFormatException e) {
			LOGGER.error("Failed to parse ship ID from bottle NBT", e);
			return null;
		}
	}
	@Override
	public int getUseDuration(@NotNull ItemStack itemStack) {
		return 72000;
	}
}
