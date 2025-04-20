package com.alrex.parcool.utilities;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityUtil {
	private static double movementThreshold = 0.1;
	public enum Direction {Front, Back, Left, Right}

	public static void addVelocity(Entity entity, Vec3 vec) {
		entity.setDeltaMovement(entity.getDeltaMovement().add(vec));
	}

	public static boolean isMoving(Entity player) {
		Vec3 movement = player.getDeltaMovement();
		return Math.sqrt(Math.pow(movement.x, 2) + Math.pow(movement.z, 2)) > movementThreshold;
	}

	public static Direction getDirection(Entity player) {
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
		
		if (absForward > absSideways) {
			return forwardMovement > 0 ? Direction.Front : Direction.Back;
		} else if (absSideways > absForward) {
			return sidewaysMovement > 0 ? Direction.Right : Direction.Left;
		}
		
		return null;
	}
}
