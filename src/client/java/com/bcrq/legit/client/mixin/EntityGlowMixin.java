package com.bcrq.legit.client.mixin;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.module.modules.ChamsModule;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks Entity.isGlowing() and getTeamColorValue() so that Chams can
 * render 2-D outlines around targeted entities, visible through walls,
 * with per-entity custom colors. This piggybacks on the vanilla
 * spectral / glowing effect pipeline.
 */
@Mixin(Entity.class)
public abstract class EntityGlowMixin {

	@Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
	private void legitimity$chamsGlow(CallbackInfoReturnable<Boolean> cir) {
		ChamsModule chams = LegitimityClient.get().getModuleManager().getModule(ChamsModule.class);
		if (chams != null && chams.isEnabled() && chams.shouldGlow((Entity) (Object) this)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
	private void legitimity$chamsOutlineColor(CallbackInfoReturnable<Integer> cir) {
		ChamsModule chams = LegitimityClient.get().getModuleManager().getModule(ChamsModule.class);
		if (chams != null && chams.isEnabled()) {
			int color = chams.getOutlineColor((Entity) (Object) this);
			if (color != -1) {
				cir.setReturnValue(color);
			}
		}
	}
}
