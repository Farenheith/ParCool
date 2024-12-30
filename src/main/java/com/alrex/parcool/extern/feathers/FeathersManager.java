package com.alrex.parcool.extern.feathers;

import com.alrex.parcool.common.capability.IStamina;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;

public class FeathersManager {
    private static boolean feathersInstalled = false;

    public static boolean isFeathersInstalled() {
        return feathersInstalled;
    }

    public static void init() {
        @Nullable
        var mod = ModList.get().getModFileById("feathers");
        feathersInstalled = mod != null;
        if (isFeathersInstalled()) {
            MinecraftForge.EVENT_BUS.register(EventConsumerForFeathers.class);
        }
    }

    public static IStamina newFeathersStaminaFor(Player player) {
        if (!feathersInstalled) return IStamina.Type.Default.newInstance(player);
        return new FeathersStamina(player);
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isUsingFeathers(Player player) {
        return feathersInstalled && IStamina.get(player) instanceof FeathersStamina;
    }
}
