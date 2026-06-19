package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public final class FreelookModule extends Module {
	private float yaw;
	private float pitch;

	public FreelookModule() {
		super("Freelook Beta", "Lets the camera rotate independently from the player body.", ModuleCategory.MISC);
	}

	@Override
	public void onEnable() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			yaw = client.player.getYaw();
			pitch = client.player.getPitch();
		}
	}

	public void handleMouse(double deltaX, double deltaY) {
		yaw += (float) deltaX * 0.15f;
		pitch = MathHelper.clamp(pitch + (float) deltaY * 0.15f, -90.0f, 90.0f);
	}

	public float getYaw() {
		return yaw;
	}

	public float getPitch() {
		return pitch;
	}
}
