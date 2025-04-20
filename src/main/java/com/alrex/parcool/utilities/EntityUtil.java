package com.alrex.parcool.utilities;

import java.util.HashMap;
import java.util.UUID;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class EntityUtil {
	private static double movementThreshold = 0.1;
	private static HashMap<UUID, Tuple<Integer, RelativeDirection>> lastDirections = new HashMap<>();
	public enum RelativeDirection {Front, Back, Left, Right}

	public static void addVelocity(Entity entity, Vec3 vec) {
		entity.setDeltaMovement(entity.getDeltaMovement().add(vec));
	}
	
	public static boolean isMoving(Entity player) {
		Vec3 movement = player.getDeltaMovement();
		return Math.sqrt(Math.pow(movement.x, 2) + Math.pow(movement.z, 2)) > movementThreshold;
	}

	public static RelativeDirection getRelativeDirection(Player player) {
		if (player instanceof LocalPlayer clientPlayer) {
			if (clientPlayer.input.left) {
				return RelativeDirection.Left;
			} else if (clientPlayer.input.right) {
				return RelativeDirection.Right;
			} else if (clientPlayer.input.forwardImpulse > 0) {
				return RelativeDirection.Front;
			} else if (clientPlayer.input.forwardImpulse < 0) {
				return RelativeDirection.Back;
			}
			return null;
		}
		var uuid = player.getUUID();
		var lastDirection = lastDirections.getOrDefault(uuid, null);
		if (lastDirection != null && lastDirection.getA() == player.tickCount) return lastDirection.getB();

		Vec3 movement = player.getDeltaMovement();
    
		// Get player's view direction
		net.minecraft.core.Direction viewDirection = player.getNearestViewDirection();
		
		// Calculate movement relative to player's view direction
		double forwardMovement = 0;
		double sidewaysMovement = 0;
		
		// Convert world movement to player-relative movement
		switch (viewDirection) {
			case NORTH: // -Z
				forwardMovement = -movement.z;
				sidewaysMovement = movement.x;
				break;
			case SOUTH: // +Z
				forwardMovement = movement.z;
				sidewaysMovement = -movement.x;
				break;
			case WEST: // -X
				forwardMovement = -movement.x;
				sidewaysMovement = -movement.z;
				break;
			case EAST: // +X
				forwardMovement = movement.x;
				sidewaysMovement = movement.z;
				break;
			default:
				break;
		}
		
		// Determine primary direction of movement relative to player orientation
		double absForward = Math.abs(forwardMovement);
		double absSideways = Math.abs(sidewaysMovement);

		RelativeDirection result = null;
		
		if (absForward > absSideways) {
			result = forwardMovement > 0 ? RelativeDirection.Front : RelativeDirection.Back;
		} else if (absSideways > absForward) {
			result = sidewaysMovement > 0 ? RelativeDirection.Right : RelativeDirection.Left;
		}

		lastDirections.put(uuid, new Tuple<>(player.tickCount, result));
		
		return result;
	}
}
