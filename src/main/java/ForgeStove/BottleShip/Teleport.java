package ForgeStove.BottleShip;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl;

import static org.valkyrienskies.mod.common.VSGameUtilsKt.getVsPipeline;
public class Teleport {
	public static void teleportShip(ServerShip ship, @NotNull ServerLevel level, Vector3d position) {
		getVsPipeline(level.getServer()).getShipWorld().teleportShip(
				ship,
				new ShipTeleportDataImpl(
						position,
						ship.getTransform().getShipToWorldRotation(),
						ship.getVelocity(),
						ship.getOmega()
				)
		);
	}
}
