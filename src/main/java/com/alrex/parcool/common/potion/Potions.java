package com.alrex.parcool.common.potion;

import com.alrex.parcool.ParCool;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Potions {
	private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, ParCool.MOD_ID);
	public static final RegistryObject<Potion> POOR_ENERGY_DRINK =
			POTIONS.register(
					"poor_energy_drink",
					() -> new Potion(
							new MobEffectInstance(com.alrex.parcool.api.Effects.INEXHAUSTIBLE.get(), 3600/*3 min*/),
							new MobEffectInstance(MobEffects.HUNGER, 100),
							new MobEffectInstance(MobEffects.POISON, 100)
					)
			);
	public static final RegistryObject<Potion> ENERGY_DRINK =
			POTIONS.register(
					"energy_drink",
					() -> new Potion(
							new MobEffectInstance(com.alrex.parcool.api.Effects.INEXHAUSTIBLE.get(), 8400/*7 min*/)
					)
			);

	public static void registerAll(IEventBus modBus) {
		POTIONS.register(modBus);
	}
}
