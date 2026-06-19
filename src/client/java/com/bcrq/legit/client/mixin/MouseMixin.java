package com.bcrq.legit.client.mixin;

import com.bcrq.legit.client.InputMetrics;
import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.module.ModuleManager;
import com.bcrq.legit.client.module.modules.FreecamModule;
import com.bcrq.legit.client.module.modules.FreelookModule;
import com.bcrq.legit.client.module.modules.TrustListModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.glfw.GLFW;

@Mixin(Mouse.class)
public abstract class MouseMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "onMouseButton", at = @At("HEAD"))
	private void legitimity$recordClicks(long window, int button, int action, int mods, CallbackInfo ci) {
		if (window == client.getWindow().getHandle() && action == GLFW.GLFW_PRESS && client.currentScreen == null) {
			InputMetrics.recordClick(button);
			if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && client.crosshairTarget instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof PlayerEntity player) {
				TrustListModule trustList = LegitimityClient.get().getModuleManager().getModule(TrustListModule.class);
				if (trustList != null && trustList.isEnabled()) {
					trustList.markSafe(player);
				}
			}
		}
	}

	@Redirect(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"))
	private void legitimity$redirectLook(ClientPlayerEntity player, double cursorDeltaX, double cursorDeltaY) {
		ModuleManager moduleManager = LegitimityClient.get().getModuleManager();
		FreecamModule freecam = moduleManager.getModule(FreecamModule.class);
		if (freecam != null && freecam.isEnabled()) {
			freecam.handleMouse(cursorDeltaX, cursorDeltaY);
			return;
		}

		FreelookModule freelook = moduleManager.getModule(FreelookModule.class);
		if (freelook != null && freelook.isEnabled()) {
			freelook.handleMouse(cursorDeltaX, cursorDeltaY);
			return;
		}

		player.changeLookDirection(cursorDeltaX, cursorDeltaY);
	}
}
