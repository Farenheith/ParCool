package com.alrex.parcool.utilities;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class EntityUtil {
	private static double movementThreshold = 0.1;
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
			return forwardMovement > 0 ? RelativeDirection.Front : RelativeDirection.Back;
		} else if (absSideways > absForward) {
			return sidewaysMovement > 0 ? RelativeDirection.Right : RelativeDirection.Left;
		}
		
		return null;
	}
}
