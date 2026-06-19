package com.bcrq.legit.client.mixin;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.module.modules.SpinModule;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelSpinMixin {
	@Shadow
	public ModelPart head;

	@Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V", at = @At("TAIL"))
	private void legitimity$spinHead(BipedEntityRenderState state, CallbackInfo ci) {
		SpinModule spin = LegitimityClient.get().getModuleManager().getModule(SpinModule.class);
		if (spin == null || !spin.shouldSpinHead()) {
			return;
		}

		head.pitch += (float) Math.toRadians(spin.getAngle(0.0f));
	}
}
