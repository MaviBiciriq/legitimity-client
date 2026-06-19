package com.bcrq.legit.client.mixin;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.module.modules.FullBrightModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Unique
	private static final StatusEffectInstance LEGITIMITY_FAKE_NIGHT_VISION =
		new StatusEffectInstance(StatusEffects.NIGHT_VISION, StatusEffectInstance.INFINITE, 0, false, false, false);

	@Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
	private void legitimity$fakeNightVisionPresence(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<Boolean> cir) {
		if (effect == StatusEffects.NIGHT_VISION && legitimity$shouldFakeNightVision()) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getStatusEffect", at = @At("HEAD"), cancellable = true)
	private void legitimity$fakeNightVisionEffect(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<StatusEffectInstance> cir) {
		if (effect == StatusEffects.NIGHT_VISION && legitimity$shouldFakeNightVision()) {
			cir.setReturnValue(LEGITIMITY_FAKE_NIGHT_VISION);
		}
	}

	@Unique
	private boolean legitimity$shouldFakeNightVision() {
		FullBrightModule module = LegitimityClient.get().getModuleManager().getModule(FullBrightModule.class);
		MinecraftClient client = MinecraftClient.getInstance();
		return module != null && module.isEnabled() && (Object) this == client.player;
	}
}
