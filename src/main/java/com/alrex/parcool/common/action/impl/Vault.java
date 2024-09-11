package com.alrex.parcool.common.action.impl;

import com.alrex.parcool.api.SoundEvents;
import com.alrex.parcool.client.animation.Animation;
import com.alrex.parcool.client.animation.impl.KongVaultAnimator;
import com.alrex.parcool.client.animation.impl.SpeedVaultAnimator;
import com.alrex.parcool.client.input.KeyBindings;
import com.alrex.parcool.common.action.Action;
import com.alrex.parcool.common.action.Parkourability;
import com.alrex.parcool.common.action.StaminaConsumeTiming;
import com.alrex.parcool.config.ParCoolConfig;
import com.alrex.parcool.utilities.BufferUtil;
import com.alrex.parcool.utilities.WorldUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

public class Vault extends Action {
	public enum TypeSelectionMode {
		SpeedVault, KongVault, Dynamic
	}
	public static final int MAX_TICK = 11;

	public enum AnimationType {
		SpeedVault((byte) 0), KongVault((byte) 1);
		private final byte code;

		AnimationType(byte code) {
			this.code = code;
		}

		public byte getCode() {
			return code;
		}

		@Nullable
		public static AnimationType fromCode(byte code) {
			switch (code) {
				case 0:
					return SpeedVault;
				case 1:
					return KongVault;
			}
			return null;
		}
	}

	//only in client
	private double stepHeight = 0;
	private Vec3 stepDirection = null;

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean canStart(Player player, Parkourability parkourability, ByteBuffer startInfo) {
		Vec3 lookVec = player.getLookAngle();
		lookVec = new Vec3(lookVec.x(), 0, lookVec.z()).normalize();
		Vec3 step = WorldUtil.getVaultableStep(player);
		if (step == null) return false;
		step = step.normalize();
		//doing "vec/stepDirection" as complex number(x + z i) to calculate difference of player's direction to steps
		Vec3 dividedVec =
				new Vec3(
						lookVec.x() * step.x() + lookVec.z() * step.z(), 0,
						-lookVec.x() * step.z() + lookVec.z() * step.x()
				).normalize();
		if (dividedVec.x() < 0.707106) {
			return false;
		}
		AnimationType animationType = null;
		SpeedVaultAnimator.Type type = SpeedVaultAnimator.Type.Right;
		switch (ParCoolConfig.Client.VaultAnimationMode.get()) {
			case KongVault:
				animationType = AnimationType.KongVault;
				break;
			case SpeedVault:
				animationType = AnimationType.SpeedVault;
				type = dividedVec.z() > 0 ? SpeedVaultAnimator.Type.Right : SpeedVaultAnimator.Type.Left;
				break;
			default:
				if (dividedVec.x() > 0.99) {
					animationType = AnimationType.KongVault;
				} else {
					animationType = AnimationType.SpeedVault;
					type = dividedVec.z() > 0 ? SpeedVaultAnimator.Type.Right : SpeedVaultAnimator.Type.Left;
				}
				break;
		}
		double wallHeight = WorldUtil.getWallHeight(player);
		startInfo.put(animationType.getCode());
		BufferUtil.wrap(startInfo).putBoolean(type == SpeedVaultAnimator.Type.Right);
		startInfo
				.putDouble(step.x())
				.putDouble(step.y())
				.putDouble(step.z())
				.putDouble(wallHeight);

		return (!(ParCoolConfig.Client.Booleans.VaultKeyPressedNeeded.get() && !KeyBindings.getKeyVault().isDown())
				&& parkourability.get(FastRun.class).canActWithRunning(player)
				&& (player.onGround() || ParCoolConfig.Client.Booleans.EnableVaultInAir.get())
				&& wallHeight > player.getBbHeight() * 0.44 /*about 0.8*/
		);
	}

	@Override
	public boolean canContinue(Player player, Parkourability parkourability) {
		return getDoingTick() < MAX_TICK;
	}

	private int getVaultAnimateTime() {
		return 2;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void onStartInLocalClient(Player player, Parkourability parkourability, ByteBuffer startData) {
		AnimationType animationType = AnimationType.fromCode(startData.get());
		SpeedVaultAnimator.Type speedVaultType = BufferUtil.getBoolean(startData) ?
				SpeedVaultAnimator.Type.Right : SpeedVaultAnimator.Type.Left;
		if (ParCoolConfig.Client.Booleans.EnableActionSounds.get())
            player.playSound(SoundEvents.VAULT.get(), 1f, 1f);
		stepDirection = new Vec3(startData.getDouble(), startData.getDouble(), startData.getDouble());
		stepHeight = startData.getDouble();
		Animation animation = Animation.get(player);
		if (animation != null && animationType != null) {
			switch (animationType) {
				case SpeedVault:
					animation.setAnimator(new SpeedVaultAnimator(speedVaultType));
					break;
				case KongVault:
					animation.setAnimator(new KongVaultAnimator());
					break;
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void onStartInOtherClient(Player player, Parkourability parkourability, ByteBuffer startData) {
		AnimationType animationType = AnimationType.fromCode(startData.get());
		SpeedVaultAnimator.Type speedVaultType = BufferUtil.getBoolean(startData) ?
				SpeedVaultAnimator.Type.Right : SpeedVaultAnimator.Type.Left;
		if (ParCoolConfig.Client.Booleans.EnableActionSounds.get())
			player.playSound(SoundEvents.VAULT.get(), 1f, 1f);
		Animation animation = Animation.get(player);
		if (animation != null && animationType != null) {
			switch (animationType) {
				case SpeedVault:
					animation.setAnimator(new SpeedVaultAnimator(speedVaultType));
					break;
				case KongVault:
					animation.setAnimator(new KongVaultAnimator());
					break;
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void onWorkingTickInLocalClient(Player player, Parkourability parkourability) {
		if (stepDirection == null) return;
		if (getDoingTick() < getVaultAnimateTime()) {
			player.setDeltaMovement(
					stepDirection.x() / 10,
					((stepHeight + 0.02) / this.getVaultAnimateTime()) / (player.getBbHeight() / 1.8),
					stepDirection.z() / 10
			);
		} else if (getDoingTick() == getVaultAnimateTime()) {
			stepDirection = stepDirection.normalize();
			player.setDeltaMovement(
					stepDirection.x() * 0.45,
					0.075 * (player.getBbHeight() / 1.8),
					stepDirection.z() * 0.45
			);
		}
	}

	@Override
	public StaminaConsumeTiming getStaminaConsumeTiming() {
		return StaminaConsumeTiming.OnStart;
	}
}

