package com.forgestove.bottle_ship.content.item;
import com.forgestove.bottle_ship.BottleShip;
import com.forgestove.bottle_ship.content.config.BSConfig;
import com.forgestove.bottle_ship.content.util.Common;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.ClipContext.*;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.*;
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
		var player = getPlayer(level, livingEntity, BSConfig.config.bottleWithoutShip.chargeTime);
		if (player == null) return;
		var hitResult = level.clip(new ClipContext(
			player.getEyePosition(1.0F),
			player.getEyePosition(1.0F).add(player.getLookAngle().scale(player.getBlockReach())),
			Block.OUTLINE,
			Fluid.NONE,
			player
		));
		if (ship == null || !ship.equals(VSGameUtilsKt.getShipManagingPos((ServerLevel) level, hitResult.getBlockPos()))) {
			player.stopUsingItem();
			player.displayClientMessage(Component.literal(""), true);
			return;
		}
		showProgress(BSConfig.config.bottleWithoutShip.chargeTime, player);
	}
	public @Nullable ServerPlayer getPlayer(@NotNull Level level, @NotNull LivingEntity livingEntity, int chargeTime) {
		if (level.isClientSide() || !(livingEntity instanceof ServerPlayer player)) return null;
		if (chargeTime == 0) {
			player.releaseUsingItem();
			return null;
		}
		return player;
	}
	public void showProgress(int chargeTime, @NotNull Player player) {
		var progress = player.getTicksUsingItem() * 20 / chargeTime;
		var progressBar = new StringBuilder();
		for (var i = 0; i < 20; i++) {
			if (i < progress) progressBar.append("§a■");
			else progressBar.append("§c■");
		}
		player.displayClientMessage(Component.literal(progressBar.toString()), true);
	}
	@Override
	public void releaseUsing(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull LivingEntity livingEntity, int tickLeft) {
		if (level.isClientSide()) return;
		if ((getUseDuration(itemStack) - tickLeft) < BSConfig.config.bottleWithoutShip.chargeTime) return;
		if (!(livingEntity instanceof Player player)) return;
		var hitResult = level.clip(new ClipContext(
			player.getEyePosition(1.0F),
			player.getEyePosition(1.0F).add(player.getLookAngle().scale(player.getBlockReach())),
			Block.OUTLINE,
			Fluid.NONE,
			player
		));
		ship = VSGameUtilsKt.getShipManagingPos((ServerLevel) level, hitResult.getBlockPos());
		if (ship == null) return;
		var worldAABB = ship.getWorldAABB();
		var area = new AABB(worldAABB.minX(), worldAABB.minY(), worldAABB.minZ(), worldAABB.maxX(), worldAABB.maxY(), worldAABB.maxZ());
		for (var entity : level.getEntities(null, area)) if (entity instanceof Player) entity.stopRiding();
		var position = ship.getTransform().getPositionInShip();
		Common.teleportShip((ServerLevel) level, ship, -position.x(), position.y(), -position.z());
		var nbt = new CompoundTag();
		nbt.putString("ID", String.valueOf(ship.getId()));
		if (ship.getSlug() != null) nbt.putString("Name", ship.getSlug());
		var shipAABB = ship.getShipAABB();
		if (shipAABB == null) return;
		nbt.putString(
			"Size",
			"[§bX:§a%d §bY:§a%d §bZ:§a%d§f]".formatted(
				shipAABB.maxX() - shipAABB.minX(),
				shipAABB.maxY() - shipAABB.minY(),
				shipAABB.maxZ() - shipAABB.minZ()
			)
		);
		var newStack = new ItemStack(BottleShip.BOTTLE_WITH_SHIP.get());
		newStack.setTag(nbt);
		Common.setItem(itemStack, level, player, newStack, BSConfig.config.bottleWithoutShip.cooldown, SoundEvents.BOTTLE_FILL);
	}
	@Override
	public int getUseDuration(@NotNull ItemStack itemStack) {
		return 72000;
	}
}
