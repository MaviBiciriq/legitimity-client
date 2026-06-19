package com.bcrq.legit.client.mixin;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.module.modules.SpinModule;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererSpinMixin {
	@Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
	private void legitimity$spinLivingEntities(LivingEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		SpinModule spin = LegitimityClient.get().getModuleManager().getModule(SpinModule.class);
		if (spin == null || !spin.shouldSpinEntities()) {
			return;
		}

		float angle = spin.getAngle(0.0f);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angle * 0.35f));
	}
}
