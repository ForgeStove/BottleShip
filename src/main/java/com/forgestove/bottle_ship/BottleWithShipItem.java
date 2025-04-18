package com.forgestove.bottle_ship;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.ServerShip;

import java.util.List;

import static com.forgestove.bottle_ship.BottleShip.*;
import static com.forgestove.bottle_ship.Config.*;
import static com.forgestove.bottle_ship.Teleport.teleportShip;
import static java.lang.Math.*;
import static net.minecraft.network.chat.Component.*;
import static net.minecraft.sounds.SoundEvents.BOTTLE_EMPTY;
import static net.minecraft.world.InteractionResult.PASS;
import static net.minecraft.world.InteractionResultHolder.*;
import static org.valkyrienskies.mod.common.VSGameUtilsKt.getVsPipeline;
public class BottleWithShipItem extends BottleWithoutShipItem {
	public BottleWithShipItem(@NotNull Properties properties) {
		super(properties);
	}
	@Override
	public void appendHoverText(
			@NotNull ItemStack itemStack,
			Level level,
			@NotNull List<Component> tooltip,
			@NotNull TooltipFlag flag
	) {
		if (level == null) return;
		CompoundTag nbt = itemStack.getTag();
		if (nbt == null) return;
		tooltip.add(translatable("tooltip." + MOD_ID + ".id", literal(String.format("§b%s§f", nbt.getString("ID")))));
		tooltip.add(translatable(
				"tooltip." + MOD_ID + ".name",
				literal(String.format("§b%s§f", nbt.getString("Name")))
		));
		tooltip.add(translatable("tooltip." + MOD_ID + ".size", literal(nbt.getString("Size"))));
	}
	public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
		return PASS;
	}
	@Override
	public void onUseTick(
			@NotNull Level level,
			@NotNull LivingEntity livingEntity,
			@NotNull ItemStack itemStack,
			int tickLeft
	) {
		onUseTickCore(level, livingEntity, itemStack, tickLeft, bottleWithShipChargeTime.get());
	}
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(
			@NotNull Level level,
			@NotNull Player player,
			@NotNull InteractionHand hand
	) {
		ItemStack currentStack = player.getItemInHand(hand);
		if (level.isClientSide()) return pass(currentStack);
		player.startUsingItem(hand);
		return consume(currentStack);
	}
	@Override
	public void releaseUsing(
			@NotNull ItemStack itemStack,
			@NotNull Level level,
			@NotNull LivingEntity livingEntity,
			int tickLeft
	) {
		if (level.isClientSide()) return;
		int tickCount = getUseDuration(itemStack) - tickLeft;
		if (tickCount * 1000 / 20 < bottleWithShipChargeTime.get()) return;
		int strength = tickCount / 20 * bottleWithShipChargeStrength.get();
		if (!(livingEntity instanceof Player player)) return;
		ItemStack newStack = new ItemStack(BOTTLE_WITHOUT_SHIP.get());
		if (itemStack.getTag() == null) {
			player.setItemInHand(player.getUsedItemHand(), newStack);
			return;
		}
		long shipID = Long.parseLong(itemStack.getTag().getString("ID"));
		Vec3 playerPosition = player.position();
		MinecraftServer server = level.getServer();
		if (server == null) return;
		ServerShip ship = getVsPipeline(server).getShipWorld().getAllShips().getById(shipID);
		if (ship == null) {
			player.setItemInHand(player.getUsedItemHand(), newStack);
			return;
		}
		AABBdc worldAABB = ship.getWorldAABB();
		double depth = worldAABB.maxY() - worldAABB.minY();
		double yawRadians = toRadians(player.getYRot());
		double pitchRadians = toRadians(player.getXRot());
		double dx = -sin(yawRadians) * cos(pitchRadians);
		double dy = -sin(pitchRadians);
		double dz = cos(yawRadians) * cos(pitchRadians);
		double targetX = playerPosition.x + dx * strength;
		double targetY = playerPosition.y + dy * strength;
		double targetZ = playerPosition.z + dz * strength;
		if (ship.getShipAABB() == null) return;
		double massHeight = ship.getInertiaData().getCenterOfMassInShip().y() - ship.getShipAABB().minY();
		targetX += (dx * (depth / 2));
		targetY += (dy * massHeight);
		targetZ += (dz * (depth / 2));
		teleportShip((ServerLevel) level, ship, targetX, targetY, targetZ);
		setItem(itemStack, level, player, newStack, bottleWithShipCooldown, BOTTLE_EMPTY);
	}
}
