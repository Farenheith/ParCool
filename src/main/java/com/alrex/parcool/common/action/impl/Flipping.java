package com.alrex.parcool.common.action.impl;

import com.alrex.parcool.client.animation.Animation;
import com.alrex.parcool.client.animation.impl.FlippingAnimator;
import com.alrex.parcool.client.input.KeyBindings;
import com.alrex.parcool.client.input.KeyRecorder;
import com.alrex.parcool.common.action.Action;
import com.alrex.parcool.common.action.Parkourability;
import com.alrex.parcool.common.action.StaminaConsumeTiming;
import com.alrex.parcool.config.ParCoolConfig;
import com.alrex.parcool.utilities.EntityUtil;
import com.alrex.parcool.utilities.EntityUtil.RelativeDirection;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.nio.ByteBuffer;

public class Flipping extends Action {
    public enum ControlType {
        PressRightAndLeft, TapMovementAndJump, PressFlippingKey;

        @OnlyIn(Dist.CLIENT)
        public boolean isInputDone(boolean justJumped, Player player) {
			switch (this) {
                case PressRightAndLeft:
                    return KeyBindings.isLeftAndRightDown();
                case PressFlippingKey:
                    return KeyRecorder.keyFlipping.isPressed();
                case TapMovementAndJump:
                    RelativeDirection direction = EntityUtil.getRelativeDirection(player);
                    return justJumped && (
                            direction == RelativeDirection.Front || direction == RelativeDirection.Back
                    );
            }
            return false;
        }
    }

    private boolean justJumped = false;

    public void onJump(Player player, Parkourability parkourability) {
        justJumped = true;
    }
	@Override
    public boolean canStart(Player player, Parkourability parkourability, ByteBuffer startInfo) {
        RelativeDirection fDirection = EntityUtil.getRelativeDirection(player);
        if (fDirection == null) fDirection = RelativeDirection.Front;
        ControlType control = ParCoolConfig.Client.FlipControl.get();
        startInfo
                .putInt(control.ordinal())
                .putInt(fDirection.ordinal());
        boolean input = control.isInputDone(justJumped, player);
        justJumped = false;
        return (input
                && !player.isShiftKeyDown()
				&& !parkourability.isDoingAny(Crawl.class, Dive.class, ChargeJump.class)
				&& !parkourability.getCancelMarks().cancelJump()
				&& parkourability.getAdditionalProperties().getNotLandingTick() <= 1
		);
	}

	@Override
    public boolean canContinue(Player player, Parkourability parkourability) {
		return !player.onGround() || getDoingTick() <= 10;
	}

	@Override
    public void onStartInLocalClient(Player player, Parkourability parkourability, ByteBuffer startData) {
        ControlType control = ControlType.values()[startData.getInt()];
        if (control != ControlType.TapMovementAndJump) player.jumpFromGround();
		Animation animation = Animation.get(player);
		if (animation != null) {
			animation.setAnimator(new FlippingAnimator(
                    RelativeDirection.values()[startData.getInt()]
			));
		}
	}

	@Override
	public void onStartInOtherClient(Player player, Parkourability parkourability, ByteBuffer startData) {
        startData.position(4); // skip (int * 1)
		Animation animation = Animation.get(player);
		if (animation != null) {
			animation.setAnimator(new FlippingAnimator(
                RelativeDirection.values()[startData.getInt()]
			));
		}
	}

	@Override
	public StaminaConsumeTiming getStaminaConsumeTiming() {
		return StaminaConsumeTiming.OnStart;
	}
}
