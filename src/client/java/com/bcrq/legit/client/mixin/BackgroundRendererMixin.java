package com.bcrq.legit.client.mixin;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.module.modules.ClearWaterModule;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Fog;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {
	@Inject(method = "applyFog", at = @At("RETURN"), cancellable = true)
	private static void legitimity$clearFog(net.minecraft.client.render.Camera camera, BackgroundRenderer.FogType fogType, Vector4f color, float viewDistance, boolean thickFog, float tickDelta, CallbackInfoReturnable<Fog> cir) {
		ClearWaterModule module = LegitimityClient.get().getModuleManager().getModule(ClearWaterModule.class);
		if (module == null || !module.isEnabled()) {
			return;
		}

		CameraSubmersionType submersionType = MinecraftClient.getInstance().gameRenderer.getCamera().getSubmersionType();
		boolean disable =
			(submersionType == CameraSubmersionType.WATER && module.isWaterFogDisabled())
				|| (submersionType == CameraSubmersionType.LAVA && module.isLavaFogDisabled())
				|| (submersionType == CameraSubmersionType.POWDER_SNOW && module.isPowderSnowFogDisabled())
				|| (fogType == BackgroundRenderer.FogType.FOG_TERRAIN && module.isTerrainFogDisabled())
				|| (fogType == BackgroundRenderer.FogType.FOG_SKY && module.isSkyFogDisabled());

		if (!disable) {
			return;
		}

		Fog current = cir.getReturnValue();
		float end = Math.max(viewDistance * 4.0f, 256.0f);
		cir.setReturnValue(new Fog(-8.0f, end, current.shape(), current.red(), current.green(), current.blue(), current.alpha()));
	}
}
