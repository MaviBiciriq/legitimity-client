package com.bcrq.legit.client.mixin;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.module.modules.KillAuraModule;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Spoofs the rotation sent in movement packets when KillAura's
 * target-lock is active. The player's camera stays independent.
 */
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

	@Inject(method = "sendMovementPackets", at = @At("HEAD"))
	private void legitimity$preSendMovement(CallbackInfo ci) {
		KillAuraModule ka = LegitimityClient.get().getModuleManager().getModule(KillAuraModule.class);
		if (ka != null && ka.hasTargetLock()) {
			ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;
			ka.storeRealRotation(self.getYaw(), self.getPitch());
			self.setYaw(ka.getLockedYaw());
			self.setPitch(ka.getLockedPitch());
		}
	}

	@Inject(method = "sendMovementPackets", at = @At("RETURN"))
	private void legitimity$postSendMovement(CallbackInfo ci) {
		KillAuraModule ka = LegitimityClient.get().getModuleManager().getModule(KillAuraModule.class);
		if (ka != null && ka.hasTargetLock()) {
			ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;
			self.setYaw(ka.getRealYaw());
			self.setPitch(ka.getRealPitch());
		}
	}
}
