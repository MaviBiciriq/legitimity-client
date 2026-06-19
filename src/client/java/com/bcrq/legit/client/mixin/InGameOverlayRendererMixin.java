package com.bcrq.legit.client.mixin;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.module.modules.LowFireModule;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.hud.InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {
	@Inject(method = "renderFireOverlay", at = @At("HEAD"))
	private static void legitimity$lowFireHead(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
		LowFireModule module = LegitimityClient.get().getModuleManager().getModule(LowFireModule.class);
		if (module != null && module.isEnabled()) {
			float scale = module.getScalePercent();
			matrices.push();
			matrices.translate(0.0f, -0.64f * (1.0f - scale), 0.0f);
			matrices.scale(1.0f, scale, 1.0f);
		}
	}

	@Inject(method = "renderFireOverlay", at = @At("TAIL"))
	private static void legitimity$lowFireTail(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
		LowFireModule module = LegitimityClient.get().getModuleManager().getModule(LowFireModule.class);
		if (module != null && module.isEnabled()) {
			matrices.pop();
		}
	}
}
