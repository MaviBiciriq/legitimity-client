package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.BooleanSetting;
import com.bcrq.legit.client.setting.EnumSetting;
import com.bcrq.legit.client.setting.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class ScaffoldModule extends Module {
	private static final Direction[] SUPPORT_ORDER = {
			Direction.DOWN,
			Direction.NORTH,
			Direction.SOUTH,
			Direction.EAST,
			Direction.WEST
	};

	private final EnumSetting<RotationMode> rotation = addSetting(
			new EnumSetting<>(this, "Rotation", "Placement rotation style.", RotationMode.SMOOTH, RotationMode.class));
	private final NumberSetting delay = addSetting(
			new NumberSetting(this, "Delay", "Ticks between block placements.", 1.0D, 0.0D, 6.0D, 1.0D));
	private final NumberSetting extend = addSetting(
			new NumberSetting(this, "Extend", "How many blocks to check ahead.", 1.0D, 0.0D, 4.0D, 1.0D));
	private final BooleanSetting keepY = addSetting(
			new BooleanSetting(this, "Keep Y", "Keep the first bridge level.", true));
	private final BooleanSetting switchBlocks = addSetting(
			new BooleanSetting(this, "Switch Blocks", "Select a hotbar block when needed.", true));
	private final BooleanSetting safeWalk = addSetting(
			new BooleanSetting(this, "Safe Walk", "Hold sneak near open edges.", true));
	private final BooleanSetting tower = addSetting(
			new BooleanSetting(this, "Tower", "Place below while holding jump.", true));
	private final BooleanSetting swing = addSetting(
			new BooleanSetting(this, "Swing", "Render hand swing after placement.", true));

	private int bridgeY;
	private int placeCooldown;
	private boolean wantSneak;
	private float easedYaw;
	private float easedPitch = 75.0f;

	public ScaffoldModule() {
		super("Scaffold", "Places blocks under and ahead of you while bridging.", ModuleCategory.MOVEMENT);
	}

	@Override
	public void onEnable() {
		MinecraftClient client = MinecraftClient.getInstance();
		bridgeY = client.player == null ? Integer.MIN_VALUE : blockPos(client.player.getX(), client.player.getY(), client.player.getZ()).getY() - 1;
		placeCooldown = 0;
		wantSneak = false;
		if (client.player != null) {
			easedYaw = client.player.getYaw();
			easedPitch = client.player.getPitch();
		}
	}

	@Override
	public void onDisable() {
		wantSneak = false;
		bridgeY = Integer.MIN_VALUE;
	}

	@Override
	public void onTick(MinecraftClient client) {
		if (client.player == null || client.world == null || client.interactionManager == null || client.currentScreen != null) {
			wantSneak = false;
			return;
		}

		ClientPlayerEntity player = client.player;
		if (placeCooldown > 0) {
			placeCooldown--;
		}

		int targetY = keepY.getValue() && bridgeY != Integer.MIN_VALUE
				? bridgeY
				: blockPos(player.getX(), player.getY(), player.getZ()).getY() - 1;
		BlockPos below = blockPos(player.getX(), targetY, player.getZ());
		Vec3d move = horizontalMovement(client, player);

		wantSneak = safeWalk.getValue() && isApproachingEdge(client, player, move, targetY);
		if (placeCooldown > 0 || !ensureBlock(client)) {
			return;
		}

		if (tower.getValue() && client.options.jumpKey.isPressed()) {
			BlockPos towerTarget = blockPos(player.getX(), player.getY() - 1.0D, player.getZ());
			if (tryPlace(client, player, towerTarget)) {
				resetCooldown();
				return;
			}
		}

		if (tryPlace(client, player, below)) {
			resetCooldown();
			return;
		}

		int checks = extend.getValue().intValue();
		if (move.lengthSquared() < 0.0004D || checks <= 0) {
			return;
		}

		Vec3d direction = move.normalize();
		for (int i = 1; i <= checks; i++) {
			BlockPos next = blockPos(player.getX() + direction.x * i, targetY, player.getZ() + direction.z * i);
			if (tryPlace(client, player, next)) {
				resetCooldown();
				return;
			}
		}
	}

	private boolean tryPlace(MinecraftClient client, ClientPlayerEntity player, BlockPos target) {
		if (!client.world.getBlockState(target).isReplaceable()) {
			return false;
		}

		Placement placement = findPlacement(client, target);
		if (placement == null) {
			return false;
		}

		place(client, player, placement);
		return true;
	}

	private Placement findPlacement(MinecraftClient client, BlockPos target) {
		for (Direction direction : SUPPORT_ORDER) {
			Placement placement = placementFrom(client, target, direction);
			if (placement != null) {
				return placement;
			}
		}

		for (Direction first : SUPPORT_ORDER) {
			BlockPos side = target.offset(first);
			if (!client.world.getBlockState(side).isReplaceable()) {
				continue;
			}
			for (Direction second : SUPPORT_ORDER) {
				Placement placement = placementFrom(client, side, second);
				if (placement != null) {
					return placement;
				}
			}
		}

		return null;
	}

	private Placement placementFrom(MinecraftClient client, BlockPos target, Direction direction) {
		BlockPos support = target.offset(direction);
		BlockState state = client.world.getBlockState(support);
		if (state.isAir() || state.isReplaceable()) {
			return null;
		}

		Direction face = direction.getOpposite();
		Vec3d hit = Vec3d.ofCenter(support).add(
				face.getOffsetX() * 0.5D,
				face.getOffsetY() * 0.5D,
				face.getOffsetZ() * 0.5D
		);
		return new Placement(support, face, hit);
	}

	private void place(MinecraftClient client, ClientPlayerEntity player, Placement placement) {
		float oldYaw = player.getYaw();
		float oldPitch = player.getPitch();
		float[] targetRot = rotationTo(player, placement.hit());

		if (rotation.getValue() == RotationMode.SNAP) {
			player.setYaw(targetRot[0]);
			player.setPitch(targetRot[1]);
		} else if (rotation.getValue() == RotationMode.SMOOTH) {
			easedYaw += MathHelper.wrapDegrees(targetRot[0] - easedYaw) * 0.45f;
			easedPitch += (targetRot[1] - easedPitch) * 0.45f;
			player.setYaw(easedYaw);
			player.setPitch(MathHelper.clamp(easedPitch, -90.0f, 90.0f));
		}

		BlockHitResult hit = new BlockHitResult(placement.hit(), placement.face(), placement.support(), false);
		ActionResult result = client.interactionManager.interactBlock(player, Hand.MAIN_HAND, hit);
		if (swing.getValue() && result.isAccepted()) {
			player.swingHand(Hand.MAIN_HAND);
		}

		if (rotation.getValue() != RotationMode.NONE) {
			player.setYaw(oldYaw);
			player.setPitch(oldPitch);
		}
	}

	private float[] rotationTo(ClientPlayerEntity player, Vec3d hit) {
		Vec3d delta = hit.subtract(player.getEyePos());
		double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
		float yaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
		float pitch = (float) Math.toDegrees(-Math.atan2(delta.y, horizontal));
		return new float[] { yaw, MathHelper.clamp(pitch, -90.0f, 90.0f) };
	}

	private Vec3d horizontalMovement(MinecraftClient client, ClientPlayerEntity player) {
		Vec3d velocity = player.getVelocity();
		Vec3d movement = new Vec3d(velocity.x, 0.0D, velocity.z);
		if (movement.lengthSquared() > 0.0004D) {
			return movement;
		}

		double yaw = Math.toRadians(player.getYaw());
		double forward = client.options.forwardKey.isPressed() ? 1.0D : client.options.backKey.isPressed() ? -1.0D : 0.0D;
		double side = client.options.leftKey.isPressed() ? 1.0D : client.options.rightKey.isPressed() ? -1.0D : 0.0D;
		double x = -Math.sin(yaw) * forward + Math.cos(yaw) * side;
		double z = Math.cos(yaw) * forward + Math.sin(yaw) * side;
		return new Vec3d(x, 0.0D, z);
	}

	private boolean isApproachingEdge(MinecraftClient client, ClientPlayerEntity player, Vec3d move, int targetY) {
		if (move.lengthSquared() < 0.0004D) {
			return client.world.getBlockState(blockPos(player.getX(), targetY, player.getZ())).isReplaceable();
		}

		Vec3d direction = move.normalize();
		BlockPos ahead = blockPos(player.getX() + direction.x * 0.42D, targetY, player.getZ() + direction.z * 0.42D);
		return client.world.getBlockState(ahead).isReplaceable();
	}

	private boolean ensureBlock(MinecraftClient client) {
		if (client.player.getMainHandStack().getItem() instanceof BlockItem) {
			return true;
		}
		if (!switchBlocks.getValue()) {
			return false;
		}

		for (int slot = 0; slot < 9; slot++) {
			ItemStack stack = client.player.getInventory().getStack(slot);
			if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
				client.player.getInventory().selectedSlot = slot;
				return true;
			}
		}

		return false;
	}

	private void resetCooldown() {
		placeCooldown = delay.getValue().intValue();
	}

	private BlockPos blockPos(double x, double y, double z) {
		return new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
	}

	public boolean wantsSneak() {
		return isEnabled() && wantSneak;
	}

	private record Placement(BlockPos support, Direction face, Vec3d hit) {
	}

	public enum RotationMode {
		NONE("None"),
		SMOOTH("Smooth"),
		SNAP("Snap");

		private final String label;

		RotationMode(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}
}
