package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.BooleanSetting;
import com.bcrq.legit.client.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class ParkourModule extends Module {
	private final BooleanSetting onlyMoving = addSetting(
			new BooleanSetting(this, "Only Moving", "Only jump while movement keys are held.", true));
	private final BooleanSetting noSneak = addSetting(
			new BooleanSetting(this, "No Sneak", "Do not jump while sneaking.", true));
	private final NumberSetting edgeDistance = addSetting(
			new NumberSetting(this, "Edge Distance", "How far ahead to check for an edge.", 0.34D, 0.12D, 0.70D, 0.02D));
	private final NumberSetting cooldownTicks = addSetting(
			new NumberSetting(this, "Cooldown", "Ticks to wait after an automatic jump.", 2.0D, 0.0D, 10.0D, 1.0D));

	private int cooldown;

	public ParkourModule() {
		super("Parkour", "Automatically jumps at block edges.", ModuleCategory.MOVEMENT);
	}

	@Override
	public void onEnable() {
		cooldown = 0;
	}

	@Override
	public void onTick(MinecraftClient client) {
		if (client.player == null || client.world == null || client.currentScreen != null) {
			return;
		}

		if (cooldown > 0) {
			cooldown--;
			return;
		}

		ClientPlayerEntity player = client.player;
		if (!player.isOnGround()) {
			return;
		}
		if (noSneak.getValue() && player.isSneaking()) {
			return;
		}
		if (onlyMoving.getValue() && !isMovementPressed(client)) {
			return;
		}

		Vec3d direction = movementDirection(client, player);
		if (direction.lengthSquared() < 0.0001D) {
			return;
		}

		direction = direction.normalize();
		double distance = edgeDistance.getValue();
		BlockPos below = blockPos(player.getX(), player.getY() - 0.45D, player.getZ());
		BlockPos aheadBelow = blockPos(player.getX() + direction.x * distance, player.getY() - 0.55D, player.getZ() + direction.z * distance);
		BlockPos aheadFeet = blockPos(player.getX() + direction.x * distance, player.getY() + 0.05D, player.getZ() + direction.z * distance);

		boolean standingOnBlock = !client.world.getBlockState(below).isReplaceable();
		boolean edgeAhead = client.world.getBlockState(aheadBelow).isReplaceable();
		boolean bodyClear = client.world.getBlockState(aheadFeet).isReplaceable();

		if (standingOnBlock && edgeAhead && bodyClear) {
			player.jump();
			cooldown = cooldownTicks.getValue().intValue();
		}
	}

	private boolean isMovementPressed(MinecraftClient client) {
		return client.options.forwardKey.isPressed()
				|| client.options.backKey.isPressed()
				|| client.options.leftKey.isPressed()
				|| client.options.rightKey.isPressed();
	}

	private Vec3d movementDirection(MinecraftClient client, ClientPlayerEntity player) {
		double yaw = Math.toRadians(player.getYaw());
		double forward = client.options.forwardKey.isPressed() ? 1.0D : client.options.backKey.isPressed() ? -1.0D : 0.0D;
		double side = client.options.leftKey.isPressed() ? 1.0D : client.options.rightKey.isPressed() ? -1.0D : 0.0D;
		double x = -Math.sin(yaw) * forward + Math.cos(yaw) * side;
		double z = Math.cos(yaw) * forward + Math.sin(yaw) * side;

		if (x * x + z * z > 0.0001D) {
			return new Vec3d(x, 0.0D, z);
		}

		Vec3d velocity = player.getVelocity();
		return new Vec3d(velocity.x, 0.0D, velocity.z);
	}

	private BlockPos blockPos(double x, double y, double z) {
		return new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
	}
}
