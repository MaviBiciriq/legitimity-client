package com.bcrq.legit.client.mixin;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.module.modules.NoParticlesModule;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
	@Inject(method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;)V", at = @At("HEAD"), cancellable = true)
	private void legitimity$cancelEmitter(Entity entity, ParticleEffect parameters, CallbackInfo ci) {
		if (isDisabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;I)V", at = @At("HEAD"), cancellable = true)
	private void legitimity$cancelEmitterTimed(Entity entity, ParticleEffect parameters, int maxAge, CallbackInfo ci) {
		if (isDisabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
	private void legitimity$cancelParticles(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> cir) {
		if (isDisabled()) {
			cir.setReturnValue(null);
		}
	}

	@Inject(method = "addBlockBreakParticles", at = @At("HEAD"), cancellable = true)
	private void legitimity$cancelBlockBreak(BlockPos pos, BlockState state, CallbackInfo ci) {
		if (isDisabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "addBlockBreakingParticles", at = @At("HEAD"), cancellable = true)
	private void legitimity$cancelBlockBreaking(BlockPos pos, Direction direction, CallbackInfo ci) {
		if (isDisabled()) {
			ci.cancel();
		}
	}

	private boolean isDisabled() {
		NoParticlesModule module = LegitimityClient.get().getModuleManager().getModule(NoParticlesModule.class);
		return module != null && module.isEnabled();
	}
}
