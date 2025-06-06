package com.alrex.parcool.common.action.impl;

import com.alrex.parcool.api.SoundEvents;
import com.alrex.parcool.client.animation.impl.ClingToCliffAnimator;
import com.alrex.parcool.client.input.KeyBindings;
import com.alrex.parcool.client.input.KeyRecorder;
import com.alrex.parcool.common.action.Action;
import com.alrex.parcool.common.action.BehaviorEnforcer;
import com.alrex.parcool.common.action.StaminaConsumeTiming;
import com.alrex.parcool.common.capability.Animation;
import com.alrex.parcool.common.capability.IStamina;
import com.alrex.parcool.common.capability.Parkourability;
import com.alrex.parcool.config.ParCoolConfig;
import com.alrex.parcool.utilities.VectorUtil;
import com.alrex.parcool.utilities.WorldUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

public class ClingToCliff extends Action {
	public enum ControlType {
		PressKey, Toggle
	}

	private static final BehaviorEnforcer.ID ID_SNEAK_CANCEL = BehaviorEnforcer.newID();
	private static final BehaviorEnforcer.ID ID_FALL_FLY_CANCEL = BehaviorEnforcer.newID();
	private float armSwingAmount = 0;
	private FacingDirection facingDirection = FacingDirection.ToWall;
	@Nullable
	private Vector3d clingWallDirection = null;

	public float getArmSwingAmount() {
		return armSwingAmount;
	}

	@Override
	public void onWorkingTick(PlayerEntity player, Parkourability parkourability, IStamina stamina) {
		player.fallDistance = 0;
	}

	public FacingDirection getFacingDirection() {
		return facingDirection;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean canStart(PlayerEntity player, Parkourability parkourability, IStamina stamina, ByteBuffer startInfo) {
		boolean value = (!stamina.isExhausted()
				&& player.getDeltaMovement().y() < 0.2
				&& !parkourability.get(HorizontalWallRun.class).isDoing()
				&& KeyBindings.getKeyGrabWall().isDown()
				&& (KeyBindings.getKeyGrabWall().getKey().equals(KeyBindings.getKeySneak().getKey()) || !player.isShiftKeyDown())
		);
		if (!value) return false;
		Vector3d wallVec = WorldUtil.getGrabbableWall(player);
		if (wallVec == null) return false;
		startInfo.putDouble(wallVec.x())
				.putDouble(wallVec.z());
		//Check whether player is facing to wall
		return 0.5 < wallVec.normalize().dot(player.getLookAngle().multiply(1, 0, 1).normalize());
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean canContinue(PlayerEntity player, Parkourability parkourability, IStamina stamina) {
		return (!stamina.isExhausted()
				&& parkourability.getActionInfo().can(ClingToCliff.class)
				&& isGrabbing()
				&& !parkourability.get(HorizontalWallRun.class).isDoing()
				&& !parkourability.get(ClimbUp.class).isDoing()
				&& WorldUtil.getGrabbableWall(player) != null
		);

	}

	private boolean isGrabbing() {
		return ParCoolConfig.Client.ClingToCliffControl.get() == ControlType.PressKey
			? KeyBindings.getKeyGrabWall().isDown()
			: !KeyRecorder.keyBindGrabWall.isPressed();
	}

    @Override
	public void onStart(PlayerEntity player, Parkourability parkourability, ByteBuffer startData) {
		parkourability.getBehaviorEnforcer().addMarkerCancellingFallFlying(ID_FALL_FLY_CANCEL, this::isDoing);
        armSwingAmount = 0;
    }

	@OnlyIn(Dist.CLIENT)
	@Override
	public void onStartInLocalClient(PlayerEntity player, Parkourability parkourability, IStamina stamina, ByteBuffer startData) {
		clingWallDirection = new Vector3d(startData.getDouble(), 0, startData.getDouble());
		facingDirection = FacingDirection.ToWall;
		armSwingAmount = 0;
		if (!KeyBindings.getKeyGrabWall().getKey().equals(KeyBindings.getKeySneak().getKey())) {
			parkourability.getBehaviorEnforcer().addMarkerCancellingSneak(ID_SNEAK_CANCEL, this::isDoing);
		}
		if (ParCoolConfig.Client.Booleans.EnableActionSounds.get())
            player.playSound(SoundEvents.CLING_TO_CLIFF.get(), 1f, 1f);
		Animation animation = Animation.get(player);
		if (animation != null) animation.setAnimator(new ClingToCliffAnimator());
	}

	@Override
	public void onStartInOtherClient(PlayerEntity player, Parkourability parkourability, ByteBuffer startData) {
		clingWallDirection = new Vector3d(startData.getDouble(), 0, startData.getDouble());
		facingDirection = FacingDirection.ToWall;
		armSwingAmount = 0;
		if (ParCoolConfig.Client.Booleans.EnableActionSounds.get())
			player.playSound(SoundEvents.CLING_TO_CLIFF.get(), 1f, 1f);
		Animation animation = Animation.get(player);
		if (animation != null) animation.setAnimator(new ClingToCliffAnimator());
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void onWorkingTickInLocalClient(PlayerEntity player, Parkourability parkourability, IStamina stamina) {
		armSwingAmount += (float) player.getDeltaMovement().multiply(1, 0, 1).lengthSqr();
		if (KeyBindings.isLeftAndRightDown()) {
			player.setDeltaMovement(0, 0, 0);
		} else {
			if (clingWallDirection != null && facingDirection == FacingDirection.ToWall) {
				Vector3d vec = clingWallDirection.yRot((float) (Math.PI / 2)).normalize().scale(0.1);
				if (KeyBindings.isKeyLeftDown()) player.setDeltaMovement(vec);
				else if (KeyBindings.isKeyRightDown()) player.setDeltaMovement(vec.reverse());
				else player.setDeltaMovement(0, 0, 0);
			} else {
				player.setDeltaMovement(0, 0, 0);
			}
		}
	}

	@Override
	public void onWorkingTickInClient(PlayerEntity player, Parkourability parkourability, IStamina stamina) {
		clingWallDirection = WorldUtil.getGrabbableWall(player);
		if (clingWallDirection == null) return;
		clingWallDirection = clingWallDirection.normalize();
		Vector3d lookingAngle = player.getLookAngle().multiply(1, 0, 1).normalize();
		Vector3d angle =
				new Vector3d(
						clingWallDirection.x() * lookingAngle.x() + clingWallDirection.z() * lookingAngle.z(), 0,
						-clingWallDirection.x() * lookingAngle.z() + clingWallDirection.z() * lookingAngle.x()
				).normalize();
		if (angle.x() > 0.342) {
			facingDirection = FacingDirection.ToWall;
		} else if (angle.z() < 0) {
			facingDirection = FacingDirection.RightAgainstWall;
		} else {
			facingDirection = FacingDirection.LeftAgainstWall;
		}
	}

	@Override
	public void saveSynchronizedState(ByteBuffer buffer) {
		buffer.putFloat(armSwingAmount);
	}

	@Override
	public void restoreSynchronizedState(ByteBuffer buffer) {
		armSwingAmount = buffer.getFloat();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void onRenderTick(TickEvent.RenderTickEvent event, PlayerEntity player, Parkourability parkourability) {
		if (isDoing() && clingWallDirection != null) {
			switch (facingDirection) {
				case ToWall:
					player.setYBodyRot((float) VectorUtil.toYawDegree(clingWallDirection));
					break;
				case RightAgainstWall:
					player.yBodyRotO = player.yBodyRot = (float) VectorUtil.toYawDegree(clingWallDirection.yRot((float) (-Math.PI / 2)));
					break;
				case LeftAgainstWall:
					player.yBodyRotO = player.yBodyRot = (float) VectorUtil.toYawDegree(clingWallDirection.yRot((float) (Math.PI / 2)));
			}
		}
	}

	@Override
	public StaminaConsumeTiming getStaminaConsumeTiming() {
		return StaminaConsumeTiming.OnWorking;
	}

	public enum FacingDirection {
		ToWall, RightAgainstWall, LeftAgainstWall
	}
}
