package com.alrex.parcool.common.compat.shoulderSurfing;

import com.alrex.parcool.common.action.impl.Dodge;
import com.alrex.parcool.common.action.impl.Dodge.DodgeDirection;
import com.alrex.parcool.utilities.MathUtil;
import com.alrex.parcool.utilities.VectorUtil;
import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfingCamera;
import com.github.exopandora.shouldersurfing.api.client.ShoulderSurfing;
import com.github.exopandora.shouldersurfing.config.Config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.level.NoteBlockEvent.Play;

import java.lang.reflect.Field;

/**
 * Compatibility class for the "Should Surfing" mod
 */
public class ShoulderSurfingCompat {
    private static Minecraft mc = Minecraft.getInstance();
    private static Boolean isCameraDecoupled = false;
    private static boolean isLoaded = false;
    private static Object configClient = null;
    static {
        try {
            // Try to load the Config class dynamically
            Class<?> configClass = Class.forName("com.github.exopandora.shouldersurfing.config.Config");
            Field clientField = configClass.getField("CLIENT");
            configClient = clientField.get(null);
            isLoaded = true;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            isLoaded = false;
        }
    }

    public static Boolean isCameraDecoupled() {
        if (!isLoaded) return false;
        return !IsCameraInFirstPerson() && Config.CLIENT.isCameraDecoupled();
    }

    public static void forceCoupledCamera() {
        if (!isLoaded) return;
        ShoulderSurfingCompat.isCameraDecoupled = isCameraDecoupled();
        if (isCameraDecoupled) {
            Config.CLIENT.toggleCameraCoupling();
            org.apache.logging.log4j.LogManager.getLogger("ParCool").info("coupling camera");
        }
    }

    public static void releaseCoupledCamera() {
        if (!isLoaded) return;
        if (isCameraDecoupled && !isCameraDecoupled()) {
            Config.CLIENT.toggleCameraCoupling();
            org.apache.logging.log4j.LogManager.getLogger("ParCool").info("decoupling camera");
            isCameraDecoupled = false;
        }
    }
 
     public static DodgeDirection handleCustomCameraRotationForDodge(DodgeDirection direction) {
        if (!isLoaded || IsCameraInFirstPerson()) return direction;
        if (Config.CLIENT.isCameraDecoupled()) return DodgeDirection.Front;
        var player = Minecraft.getInstance().player;
        if (player == null) return direction;
        IShoulderSurfingCamera camera = ShoulderSurfing.getInstance().getCamera();
        float yaw = MathUtil.normalizeDegree(camera.getYRot() - player.getYRot());
        float yawAbs = Math.abs(yaw);
        if (yawAbs < 45) return direction;
        if (yawAbs > 135) return direction.inverse();
        if (yaw < 0) return direction.left();
        return direction.right();
     }

     private static boolean IsCameraInFirstPerson() {
        return Minecraft.getInstance().options.getCameraType().isFirstPerson();
     }
}