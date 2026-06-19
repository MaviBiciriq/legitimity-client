package com.bcrq.legit.client.mixin;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.module.modules.TimeWarpModule;
import com.bcrq.legit.client.module.modules.VelocityModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.network.ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Inject(method = "onWorldTimeUpdate", at = @At("HEAD"), cancellable = true)
	private void legitimity$holdCustomTime(WorldTimeUpdateS2CPacket packet, CallbackInfo ci) {
		TimeWarpModule module = LegitimityClient.get().getModuleManager().getModule(TimeWarpModule.class);
		if (module == null) {
			return;
		}

		module.captureServerTime(packet.time(), packet.timeOfDay(), packet.tickDayTime());
		if (module.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "onEntityVelocityUpdate", at = @At("RETURN"))
	private void legitimity$handleVelocity(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || packet.getEntityId() != client.player.getId()) {
			return;
		}

		VelocityModule module = LegitimityClient.get().getModuleManager().getModule(VelocityModule.class);
		if (module != null && module.isEnabled()) {
			module.onVelocityUpdate(client.player);
		}
	}
}
