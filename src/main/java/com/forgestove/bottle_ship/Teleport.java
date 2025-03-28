package com.forgestove.bottle_ship;
import net.minecraft.server.level.ServerLevel;
import net.spaceeye.vmod.utils.Vector3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.ServerShip;

import static net.spaceeye.vmod.utils.vs.TeleportShipWithConnectedKt.teleportShipWithConnected;
import static org.valkyrienskies.mod.common.VSGameUtilsKt.getDimensionId;
public class Teleport {
	public static void teleportShip(
			@NotNull ServerLevel level,
			@NotNull ServerShip ship,
			double x,
			double y,
			double z
	) {
		Vector3dc scaling = ship.getTransform().getShipToWorldScaling();
		teleportShipWithConnected(
				level,
				ship,
				new Vector3d(x, y, z),
				ship.getTransform().getShipToWorldRotation(),
				(scaling.x() + scaling.y() + scaling.z()) / 3,
				getDimensionId(level)
		);
	}
}
