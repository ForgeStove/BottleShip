package com.ForgeStove.bottle_ship;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;
import org.joml.primitives.*;
import org.valkyrienskies.core.api.ships.ServerShip;

import static com.ForgeStove.bottle_ship.BottleShip.BOTTLE_WITH_SHIP;
import static com.ForgeStove.bottle_ship.Config.*;
import static com.ForgeStove.bottle_ship.Teleport.teleportShip;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.sounds.SoundEvents.BOTTLE_FILL;
import static net.minecraft.sounds.SoundSource.PLAYERS;
import static net.minecraft.world.InteractionResult.*;
import static net.minecraft.world.item.UseAnim.BOW;
import static org.valkyrienskies.mod.common.VSGameUtilsKt.getShipManagingPos;
public class BottleWithoutShipItem extends Item {
	private ServerShip ship;
	public BottleWithoutShipItem(@NotNull Properties properties) {
		super(properties);
	}
	@Override public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
		return BOW;
	}
	@Override public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide()) return PASS;
		Player player = context.getPlayer();
		if (player == null || player instanceof FakePlayer) return FAIL;
		ship = getShipManagingPos((ServerLevel) level, context.getClickedPos());
		if (ship == null) return FAIL;
		player.startUsingItem(context.getHand());
		return CONSUME;
	}
	@Override
	public void onUseTick(
			@NotNull Level level,
			@NotNull LivingEntity livingEntity,
			@NotNull ItemStack itemStack,
			int tickLeft
	) {
		onUseTickCore(level, livingEntity, itemStack, tickLeft, bottleWithoutShipChargeTime.get());
	}
	public void onUseTickCore(
			@NotNull Level level,
			@NotNull LivingEntity livingEntity,
			@NotNull ItemStack itemStack,
			int tickLeft,
			int chargeTime
	) {
		if (level.isClientSide() || chargeTime == 0 || !(livingEntity instanceof Player player)) return;
		int progress = (getUseDuration(itemStack) - tickLeft) * 1000 / chargeTime;
		StringBuilder progressBar = new StringBuilder();
		for (int i = 0; i < 18; i++) {
			if (i < progress) progressBar.append("§a■");
			else progressBar.append("§c■");
		}
		player.displayClientMessage(literal(progressBar.toString()), true);
	}
	@Override public int getUseDuration(@NotNull ItemStack itemStack) {
		return 72000;
	}
	@Override
	public void releaseUsing(
			@NotNull ItemStack itemStack,
			@NotNull Level level,
			@NotNull LivingEntity livingEntity,
			int tickLeft
	) {
		if (level.isClientSide()) return;
		if ((getUseDuration(itemStack) - tickLeft) * 1000 / 20 < bottleWithoutShipChargeTime.get()) return;
		if (!(livingEntity instanceof Player player)) return;
		if (ship == null) return;
		AABBdc worldAABB = ship.getWorldAABB();
		for (Entity entity : level.getEntities(
				null,
				new AABB(
						worldAABB.minX(),
						worldAABB.minY(),
						worldAABB.minZ(),
						worldAABB.maxX(),
						worldAABB.maxY(),
						worldAABB.maxZ()
				)
		))
			if (entity instanceof Player) entity.stopRiding();
		Vector3dc position = ship.getTransform().getPositionInShip();
		teleportShip((ServerLevel) level, ship, -position.x(), position.y(), -position.z());
		CompoundTag nbt = new CompoundTag();
		nbt.putString("ID", String.valueOf(ship.getId()));
		if (ship.getSlug() != null) nbt.putString("Name", ship.getSlug());
		AABBic shipAABB = ship.getShipAABB();
		if (shipAABB == null) return;
		nbt.putString(
				"Size", String.format(
						"[§bX:§a%d §bY:§a%d §bZ:§a%d§f]",
						shipAABB.maxX() - shipAABB.minX(),
						shipAABB.maxY() - shipAABB.minY(),
						shipAABB.maxZ() - shipAABB.minZ()
				)
		);
		ItemStack newStack = new ItemStack(BOTTLE_WITH_SHIP.get());
		newStack.setTag(nbt);
		setItem(itemStack, level, player, newStack, bottleWithoutShipCooldown, BOTTLE_FILL);
	}
	public static void setItem(
			@NotNull ItemStack itemStack,
			@NotNull Level level,
			@NotNull Player player,
			@NotNull ItemStack newStack,
			@NotNull ConfigValue<Integer> configValue,
			SoundEvent soundEvent
	) {
		player.getCooldowns().addCooldown(newStack.getItem(), configValue.get());
		if (itemStack.getCount() != 1) {
			itemStack.shrink(1);
			player.addItem(newStack);
		} else player.setItemInHand(player.getUsedItemHand(), newStack);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), soundEvent, PLAYERS, 1.0F, 1.0F);
	}
}
