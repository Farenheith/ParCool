package com.alrex.parcool.extern.paraglider;

import com.alrex.parcool.common.capability.IStamina;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import tictim.paraglider.api.item.Paraglider;

import javax.annotation.Nullable;

public class ParagliderManager {
	private static boolean paragliderInstalled = false;

	public static boolean isParagliderInstalled() {
		return paragliderInstalled;
	}

	public static void init() {
		@Nullable
		var mod = ModList.get().getModFileById("paraglider");
		paragliderInstalled = mod != null;
	}

	@Nullable
	public static IStamina newParagliderStaminaFor(Player player) {
		if (!paragliderInstalled) return IStamina.Type.Default.newInstance(player);
		return new ParagliderStamina(player);
	}

	@OnlyIn(Dist.CLIENT)
	public static boolean isUsingParaglider(Player player) {
		return paragliderInstalled && IStamina.get(player) instanceof ParagliderStamina;
	}

	public static boolean isFallingWithParaglider(Player player) {
		if (isParagliderInstalled()) {
			ItemStack stack;
			Paraglider paragliderItem;
			if (player.getMainHandItem().getItem() instanceof Paraglider p) {
				paragliderItem = p;
				stack = player.getMainHandItem();
			} else if (player.getOffhandItem().getItem() instanceof Paraglider p) {
				paragliderItem = p;
				stack = player.getMainHandItem();
			} else
				return false;
			return paragliderItem.isParagliding(stack);
		} else {
			return false;
		}
	}
}
