package com.forgestove.bottle_ship.content.item;
import com.forgestove.bottle_ship.BottleShip;
import com.forgestove.bottle_ship.content.Registry;
import com.forgestove.bottle_ship.content.util.BottleItemHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
public class BottleWithoutShipItem extends Item {
	public ServerShip ship;
	public BottleWithoutShipItem(@NotNull Properties properties) {
		super(properties);
	}
	@Override
	public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
		return UseAnim.BOW;
	}
	@Override
	public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
		var level = context.getLevel();
		if (level.isClientSide()) return InteractionResult.PASS;
		var player = context.getPlayer();
		if (player == null || player instanceof FakePlayer) return InteractionResult.FAIL;
		ship = VSGameUtilsKt.getShipManagingPos((ServerLevel) level, context.getClickedPos());
		if (ship == null) return InteractionResult.FAIL;
		player.startUsingItem(context.getHand());
		return InteractionResult.CONSUME;
	}
	@Override
	public void onUseTick(@NotNull Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack itemStack, int tickLeft) {
		var player = BottleItemHelper.getPlayer(level, livingEntity, BottleShip.CONFIG.bottleWithoutShip.chargeTime);
		if (player == null) return;
		var targetShip = BottleItemHelper.getTargetShip((ServerLevel) level, player);
		if (ship == null || !ship.equals(targetShip)) {
			player.stopUsingItem();
			player.displayClientMessage(Component.literal(""), true);
			return;
		}
		BottleItemHelper.showProgress(BottleShip.CONFIG.bottleWithoutShip.chargeTime, player);
	}
	@Override
	public void releaseUsing(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull LivingEntity livingEntity, int tickLeft) {
		if (level.isClientSide()) return;
		if (getUseDuration(itemStack) - tickLeft < BottleShip.CONFIG.bottleWithoutShip.chargeTime) return;
		if (!(livingEntity instanceof Player player)) return;
		ship = BottleItemHelper.getTargetShip((ServerLevel) level, player);
		if (ship == null) return;
		var worldAABB = ship.getWorldAABB();
		var area = new AABB(worldAABB.minX(), worldAABB.minY(), worldAABB.minZ(), worldAABB.maxX(), worldAABB.maxY(), worldAABB.maxZ());
		level.getEntities(null, area)
			.stream()
			.filter(entity -> entity instanceof Player)
			.forEach(Entity::stopRiding);
		var position = ship.getTransform().getPositionInShip();
		BottleItemHelper.teleportShip((ServerLevel) level, ship, -position.x(), position.y(), -position.z());
		var newStack = createBottleWithShip(ship);
		BottleItemHelper.setItem(itemStack, level, player, newStack, BottleShip.CONFIG.bottleWithoutShip.cooldown,
			SoundEvents.BOTTLE_FILL);
	}
	private ItemStack createBottleWithShip(@NotNull ServerShip ship) {
		var nbt = new CompoundTag();
		nbt.putString("ID", String.valueOf(ship.getId()));
		if (ship.getSlug() != null) nbt.putString("Name", ship.getSlug());
		var shipAABB = ship.getShipAABB();
		if (shipAABB != null) nbt.putString(
			"Size",
			"[§bX:§a%d §bY:§a%d §bZ:§a%d§f]".formatted(
				shipAABB.maxX() - shipAABB.minX(),
				shipAABB.maxY() - shipAABB.minY(),
				shipAABB.maxZ() - shipAABB.minZ()
			)
		);
		var newStack = new ItemStack(Registry.BOTTLE_WITH_SHIP.get());
		newStack.setTag(nbt);
		return newStack;
	}
	@Override
	public int getUseDuration(@NotNull ItemStack itemStack) {
		return 72000;
	}
}
