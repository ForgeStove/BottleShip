package ForgeStove.BottleShip;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.ServerShip;

import static ForgeStove.BottleShip.BottleShip.BOTTLE_WITH_SHIP;
import static ForgeStove.BottleShip.Config.*;
import static ForgeStove.BottleShip.Teleport.teleportShip;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.sounds.SoundEvents.BOTTLE_FILL;
import static net.minecraft.sounds.SoundSource.PLAYERS;
import static net.minecraft.world.InteractionResult.*;
import static net.minecraft.world.item.UseAnim.BOW;
import static org.valkyrienskies.mod.common.VSGameUtilsKt.getShipManagingPos;
public class BottleWithoutShipItem extends Item {
	private BlockPos blockPos;
	private ServerShip ship;
	public BottleWithoutShipItem(Properties properties) {
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
		blockPos = context.getClickedPos();
		ship = getShipManagingPos((ServerLevel) level, blockPos);
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
		if (player.getVehicle() != null) player.stopRiding();
		long id = ship.getId();
		teleportShip((ServerLevel) level, ship, blockPos.getX(), blockPos.getY(), blockPos.getZ());
		CompoundTag nbt = new CompoundTag();
		nbt.putString("ID", String.valueOf(id));
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
		player.setItemInHand(player.getUsedItemHand(), newStack);
		player.getCooldowns().addCooldown(newStack.getItem(), bottleWithoutShipCooldown.get());
		level.playSound(null, player.getX(), player.getY(), player.getZ(), BOTTLE_FILL, PLAYERS, 1.0F, 1.0F);
	}
}
