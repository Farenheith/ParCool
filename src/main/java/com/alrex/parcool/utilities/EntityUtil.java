package com.alrex.parcool.utilities;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityUtil {
	private static double movementThreshold = 0.1;
	private static Minecraft mc = Minecraft.getInstance();

	public static void addVelocity(Entity entity, Vec3 vec) {
		entity.setDeltaMovement(entity.getDeltaMovement().add(vec));
	}

	public static boolean isMoving(Entity player) {
		Vec3 movement = player.getDeltaMovement();
		return Math.sqrt(Math.pow(movement.x, 2) + Math.pow(movement.z, 2)) > movementThreshold;
	}

	public static Vec3 GetCameraLookAngle() {
		return mc.player.getLookAngle();
	}
}
