package com.bcrq.legit.client.mixin;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.module.modules.CrispHitModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(net.minecraft.entity.projectile.ProjectileUtil.class)
public abstract class ProjectileUtilMixin {
	@ModifyVariable(
		method = "getEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;F)Lnet/minecraft/util/hit/EntityHitResult;",
		at = @At("HEAD"),
		argsOnly = true
	)
	private static float legitimity$expandMargin(float margin, World world, Entity entity) {
		CrispHitModule module = LegitimityClient.get().getModuleManager().getModule(CrispHitModule.class);
		if (module == null || !module.isEnabled() || entity != MinecraftClient.getInstance().player) {
			return margin;
		}
		return margin + module.getAimAssistMargin();
	}
}
