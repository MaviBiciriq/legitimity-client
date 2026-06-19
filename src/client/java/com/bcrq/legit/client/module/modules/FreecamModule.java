package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class FreecamModule extends Module {
	private final NumberSetting speed = addSetting(new NumberSetting(this, "Speed", "Freecam fly speed.", 0.8D, 0.2D, 2.4D, 0.05D));

	private Vec3d position = Vec3d.ZERO;
	private Vec3d previousPosition = Vec3d.ZERO;
	private float yaw;
	private float pitch;

	public FreecamModule() {
		super("Freecam", "Detaches the camera from the player and flies locally.", ModuleCategory.MOVEMENT);
	}

	@Override
	public void onEnable() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) {
			setEnabled(false);
			return;
		}

		position = client.gameRenderer.getCamera().getPos();
		previousPosition = position;
		yaw = client.gameRenderer.getCamera().getYaw();
		pitch = client.gameRenderer.getCamera().getPitch();
	}

	@Override
	public void onTick(MinecraftClient client) {
		if (client.player == null) {
			setEnabled(false);
			return;
		}

		previousPosition = position;
		Vec3d movement = Vec3d.ZERO;
		Vec3d forward = Vec3d.fromPolar(0.0f, yaw);
		Vec3d right = forward.rotateY((float) (-Math.PI / 2.0D));

		if (client.options.forwardKey.isPressed()) {
			movement = movement.add(forward);
		}
		if (client.options.backKey.isPressed()) {
			movement = movement.subtract(forward);
		}
		if (client.options.leftKey.isPressed()) {
			movement = movement.subtract(right);
		}
		if (client.options.rightKey.isPressed()) {
			movement = movement.add(right);
		}
		if (client.options.jumpKey.isPressed()) {
			movement = movement.add(0.0D, 1.0D, 0.0D);
		}
		if (client.options.sneakKey.isPressed()) {
			movement = movement.add(0.0D, -1.0D, 0.0D);
		}

		if (movement.lengthSquared() > 0.0D) {
			position = position.add(movement.normalize().multiply(speed.getValue()));
		}
	}

	public void handleMouse(double deltaX, double deltaY) {
		yaw += (float) deltaX * 0.15f;
		pitch = MathHelper.clamp(pitch + (float) deltaY * 0.15f, -90.0f, 90.0f);
	}

	public Vec3d getRenderPosition(float tickDelta) {
		return previousPosition.add(position.subtract(previousPosition).multiply(tickDelta));
	}

	public float getYaw() {
		return yaw;
	}

	public float getPitch() {
		return pitch;
	}
}
