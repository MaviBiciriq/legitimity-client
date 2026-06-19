package com.bcrq.legit.client.mixin;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.module.modules.EchoBotModule;
import com.bcrq.legit.client.module.modules.TrustListModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.network.ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
	@Inject(method = "attackEntity", at = @At("HEAD"))
	private void legitimity$handleLocalTargets(PlayerEntity player, Entity target, CallbackInfo ci) {
		if (player != MinecraftClient.getInstance().player) {
			return;
		}

		EchoBotModule echoBot = LegitimityClient.get().getModuleManager().getModule(EchoBotModule.class);
		if (echoBot != null && echoBot.isEnabled()) {
			echoBot.handleAttack(player, target);
		}

		TrustListModule trustList = LegitimityClient.get().getModuleManager().getModule(TrustListModule.class);
		if (trustList != null && trustList.isEnabled()) {
			trustList.markEnemy(target);
		}
	}
}
