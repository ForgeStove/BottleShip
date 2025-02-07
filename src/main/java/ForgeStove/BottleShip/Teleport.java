package ForgeStove.BottleShip;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.joml.primitives.*;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl;

import static org.valkyrienskies.mod.common.VSGameUtilsKt.*;
public class Teleport {
	public static void teleport(@NotNull ServerShip ship, @NotNull ServerLevel level, Vector3d position) {
		AABBdc worldAABB = ship.getWorldAABB();
		AABBd AABB = new AABBd(
				worldAABB.minX() - 1,
				worldAABB.minY() - 1,
				worldAABB.minZ() - 1,
				worldAABB.maxX() + 1,
				worldAABB.maxY() + 1,
				worldAABB.maxZ() + 1
		);
		Vector3dc mainPos = ship.getTransform().getPositionInWorld();
		for (ServerShip otherShip : getVsPipeline(level.getServer()).getShipWorld().getAllShips())
			if (!otherShip.equals(ship) && otherShip.getWorldAABB().intersectsAABB(AABB)) {
				Vector3dc otherPos = ship.getTransform().getPositionInWorld();
				Vector3d relativePos = new Vector3d(
						otherPos.x() - mainPos.x(),
						otherPos.y() - mainPos.y(),
						otherPos.z() - mainPos.z()
				);
				teleportShip(otherShip, level, new Vector3d(position).add(relativePos));
			}
		teleportShip(ship, level, position);
	}
	public static void teleportShip(@NotNull ServerShip ship, @NotNull ServerLevel level, Vector3d position) {
		ShipTransform shipTransform = ship.getTransform();
		Vector3dc scaling = shipTransform.getShipToWorldScaling();
		getVsPipeline(level.getServer()).getShipWorld().teleportShip(
				ship, new ShipTeleportDataImpl(
						position,
						shipTransform.getShipToWorldRotation(),
						ship.getVelocity(),
						ship.getOmega(),
						getDimensionId(level),
						(scaling.x() + scaling.y() + scaling.z()) / 3
				)
		);
	}
}
