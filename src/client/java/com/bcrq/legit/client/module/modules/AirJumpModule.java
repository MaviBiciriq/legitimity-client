package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public final class AirJumpModule extends Module {
	private final NumberSetting jumpVelocity = addSetting(
			new NumberSetting(this, "Jump Velocity", "Vertical velocity applied for air jumps.", 0.42D, 0.20D, 0.90D, 0.01D));
	private final NumberSetting cooldownTicks = addSetting(
			new NumberSetting(this, "Cooldown", "Ticks between air jumps.", 0.0D, 0.0D, 10.0D, 1.0D));

	private boolean wasJumpPressed;
	private int cooldown;

	public AirJumpModule() {
		super("Air Jump", "Lets you jump while already airborne.", ModuleCategory.MOVEMENT);
	}

	@Override
	public void onEnable() {
		wasJumpPressed = false;
		cooldown = 0;
	}

	@Override
	public void onTick(MinecraftClient client) {
		if (client.player == null || client.world == null || client.currentScreen != null) {
			wasJumpPressed = false;
			return;
		}

		if (cooldown > 0) {
			cooldown--;
		}

		boolean jumpPressed = client.options.jumpKey.isPressed();
		ClientPlayerEntity player = client.player;
		if (jumpPressed && !wasJumpPressed && cooldown <= 0 && canAirJump(player)) {
			Vec3d velocity = player.getVelocity();
			player.setVelocity(velocity.x, jumpVelocity.getValue(), velocity.z);
			player.fallDistance = 0.0f;
			cooldown = cooldownTicks.getValue().intValue();
		}

		wasJumpPressed = jumpPressed;
	}

	private boolean canAirJump(ClientPlayerEntity player) {
		return !player.isOnGround()
				&& !player.isTouchingWater()
				&& !player.isClimbing()
				&& !player.hasVehicle()
				&& !player.getAbilities().flying;
	}
}
