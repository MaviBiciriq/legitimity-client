package com.bcrq.legit.client.mixin;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.module.modules.FreecamModule;
import com.bcrq.legit.client.module.modules.FreelookModule;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow
	protected abstract void setRotation(float yaw, float pitch);

	@Shadow
	protected abstract void setPos(double x, double y, double z);

	@Inject(method = "update", at = @At("TAIL"))
	private void legitimity$applyFreelook(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
		FreecamModule freecam = LegitimityClient.get().getModuleManager().getModule(FreecamModule.class);
		if (freecam != null && freecam.isEnabled()) {
			setRotation(freecam.getYaw(), freecam.getPitch());
			var position = freecam.getRenderPosition(tickDelta);
			setPos(position.x, position.y, position.z);
			return;
		}

		FreelookModule freelook = LegitimityClient.get().getModuleManager().getModule(FreelookModule.class);
		if (freelook != null && freelook.isEnabled()) {
			setRotation(freelook.getYaw(), freelook.getPitch());
		}
	}
}
