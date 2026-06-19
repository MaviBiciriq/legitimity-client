package com.bcrq.legit.client.mixin;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.module.modules.ReachModule;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class ReachMixin {

	@Inject(method = "getEntityInteractionRange", at = @At("RETURN"), cancellable = true)
	private void legitimity$modifyEntityReach(CallbackInfoReturnable<Double> cir) {
		ReachModule reach = LegitimityClient.get().getModuleManager().getModule(ReachModule.class);
		if (reach != null && reach.isEnabled()) {
			cir.setReturnValue(reach.getAttackReach());
		}
	}

	@Inject(method = "getBlockInteractionRange", at = @At("RETURN"), cancellable = true)
	private void legitimity$modifyBlockReach(CallbackInfoReturnable<Double> cir) {
		ReachModule reach = LegitimityClient.get().getModuleManager().getModule(ReachModule.class);
		if (reach != null && reach.isEnabled()) {
			cir.setReturnValue(reach.getBlockReach());
		}
	}
}
