package com.bcrq.legit.client.mixin;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.module.ModuleManager;
import com.bcrq.legit.client.module.modules.BladeStanceModule;
import com.bcrq.legit.client.module.modules.HandStyleModule;
import com.bcrq.legit.client.module.modules.RapidEquipModule;
import com.bcrq.legit.client.module.modules.SpinModule;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
	@ModifyConstant(method = "updateHeldItems", constant = @Constant(floatValue = 0.4f))
	private float legitimity$accelerateEquip(float constant) {
		RapidEquipModule module = LegitimityClient.get().getModuleManager().getModule(RapidEquipModule.class);
		if (module == null || !module.isEnabled()) {
			return constant;
		}
		return constant * module.getSpeedMultiplier();
	}

	@Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
	private void legitimity$transformHand(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		matrices.push();

		ModuleManager manager = LegitimityClient.get().getModuleManager();
		HandStyleModule handStyle = manager.getModule(HandStyleModule.class);
		if (handStyle != null && handStyle.isEnabled()) {
			float direction = hand == Hand.MAIN_HAND ? 1.0f : -1.0f;
			matrices.translate(handStyle.getOffsetX() * direction, handStyle.getOffsetY(), handStyle.getOffsetZ());
		}

		BladeStanceModule bladeStance = manager.getModule(BladeStanceModule.class);
		if (bladeStance != null && bladeStance.isEnabled() && hand == Hand.MAIN_HAND && item.getItem() instanceof SwordItem && bladeStance.shouldRenderPose()) {
			float strength = bladeStance.getStrength();
			matrices.translate(-0.18f * strength, 0.12f * strength, -0.08f * strength);
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-38.0f * strength));
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(32.0f * strength));
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(10.0f * strength));
		}

		SpinModule spin = manager.getModule(SpinModule.class);
		if (spin != null && spin.shouldSpinHand()) {
			float angle = spin.getAngle(tickProgress);
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angle));
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle * 0.65f));
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle * 1.25f));
		}
	}

	@Inject(method = "renderFirstPersonItem", at = @At("TAIL"))
	private void legitimity$restoreHand(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		matrices.pop();
	}
}
