package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.BooleanSetting;
import com.bcrq.legit.client.setting.NumberSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public final class AutoCrystalModule extends Module {

	// ── Core settings ────────────────────────────────────────────────────────
	private final BooleanSetting manualMode = addSetting(
			new BooleanSetting(this, "Manual Mode", "Only activate while holding right click.", false));
	private final NumberSetting range = addSetting(
			new NumberSetting(this, "Range", "Obsidian scan range.", 4.0D, 2.0D, 4.5D, 0.1D));
	private final NumberSetting placeDelay = addSetting(
			new NumberSetting(this, "Place Delay", "Ticks between crystal placements.", 2.0D, 0.0D, 10.0D, 1.0D));
	private final NumberSetting breakDelay = addSetting(
			new NumberSetting(this, "Break Delay", "Ticks after place before breaking.", 1.0D, 0.0D, 10.0D, 1.0D));

	// ── Aim assist settings ──────────────────────────────────────────────────
	private final BooleanSetting aimAssist = addSetting(
			new BooleanSetting(this, "Aim Assist", "Gently pull crosshair toward valid obsidian.", true));
	private final NumberSetting aimSpeed = addSetting(
			new NumberSetting(this, "Aim Speed", "Aim assist degrees per tick.", 4.0D, 1.0D, 10.0D, 0.5D));

	// ── Rotation jitter settings ─────────────────────────────────────────────
	private final NumberSetting rotJitter = addSetting(
			new NumberSetting(this, "Rot Jitter", "Rotation jitter degrees.", 1.5D, 0.0D, 4.0D, 0.1D));
	private final BooleanSetting rotJitterRandom = addSetting(
			new BooleanSetting(this, "Rot Jitter Random", "Randomize rotation jitter amount each placement.", true));

	// ── Hit vector jitter settings ───────────────────────────────────────────
	private final NumberSetting hitVecJitter = addSetting(
			new NumberSetting(this, "HitVec Jitter", "Placement hit vector spread (0=center, 1=full).", 0.6D, 0.0D, 1.0D, 0.05D));
	private final BooleanSetting hitVecRandom = addSetting(
			new BooleanSetting(this, "HitVec Random", "Randomize hit vector per placement.", true));

	// ── Break jitter settings ────────────────────────────────────────────────
	private final NumberSetting breakJitter = addSetting(
			new NumberSetting(this, "Break Jitter", "Crystal break targeting offset.", 0.3D, 0.0D, 0.8D, 0.05D));
	private final BooleanSetting breakJitterRandom = addSetting(
			new BooleanSetting(this, "Break Jitter Random", "Randomize break jitter per attack.", true));

	// ── Safety / misc ─────────────────────────────────────────────────────────
	private final BooleanSetting autoBreak = addSetting(
			new BooleanSetting(this, "Auto Break", "Automatically break placed crystals.", true));
	private final BooleanSetting antiSelfDamage = addSetting(
			new BooleanSetting(this, "Anti Self Damage", "Only place where you won't take damage.", true));
	private final BooleanSetting swing = addSetting(
			new BooleanSetting(this, "Swing", "Show arm swing animation.", true));

	// ── Internal state ────────────────────────────────────────────────────────
	private final Random random = new Random();

	// Smooth jitter state (yaw / pitch offsets that lerp slowly toward new random targets)
	private float smoothRotYaw;
	private float smoothRotPitch;
	private float targetRotYaw;
	private float targetRotPitch;
	private int jitterRefreshTimer;

	// Smooth break jitter
	private float smoothBreakX;
	private float smoothBreakY;
	private float smoothBreakZ;
	private float targetBreakX;
	private float targetBreakY;
	private float targetBreakZ;
	private int breakJitterTimer;

	private int placeTicks;
	private int breakTicks;
	private BlockPos lastPlacedPos;
	private boolean waitingForBreak;

	public AutoCrystalModule() {
		super("Auto Crystal", "Crystal PvP automation with anti-cheat bypasses.", ModuleCategory.COMBAT);
	}

	@Override
	public void onEnable() {
		placeTicks = 0;
		breakTicks = 0;
		lastPlacedPos = null;
		waitingForBreak = false;
		resetSmoothJitter();
	}

	// ── Per-tick smooth jitter update ─────────────────────────────────────────

	private void resetSmoothJitter() {
		smoothRotYaw = 0; smoothRotPitch = 0; targetRotYaw = 0; targetRotPitch = 0;
		smoothBreakX = 0; smoothBreakY = 0; smoothBreakZ = 0;
		targetBreakX = 0; targetBreakY = 0; targetBreakZ = 0;
		jitterRefreshTimer = 0;
		breakJitterTimer = 0;
	}

	/**
	 * Update smooth jitter targets every 3-7 ticks.
	 * Lerp speed 0.25 — creates organic, human-like micro-drift instead of
	 * frame-by-frame random noise that AC pattern-matches easily.
	 */
	private void tickSmoothJitter() {
		float maxRot = rotJitter.getValue().floatValue();
		float maxBrk = breakJitter.getValue().floatValue();

		if (--jitterRefreshTimer <= 0) {
			jitterRefreshTimer = 3 + random.nextInt(5); // refresh every 3-7 ticks
			float j = rotJitterRandom.getValue() ? maxRot * (0.4f + random.nextFloat() * 0.6f) : maxRot;
			targetRotYaw   = (random.nextFloat() - 0.5f) * 2f * j;
			targetRotPitch = (random.nextFloat() - 0.5f) * 2f * j;
		}

		if (--breakJitterTimer <= 0) {
			breakJitterTimer = 2 + random.nextInt(4);
			float b = breakJitterRandom.getValue() ? maxBrk * (0.3f + random.nextFloat() * 0.7f) : maxBrk;
			targetBreakX = (random.nextFloat() - 0.5f) * 2f * b;
			targetBreakY = (random.nextFloat() - 0.5f) * 2f * b * 0.5f;
			targetBreakZ = (random.nextFloat() - 0.5f) * 2f * b;
		}

		// Lerp current toward target (smooth drift, not instant snap)
		smoothRotYaw   = lerp(smoothRotYaw,   targetRotYaw,   0.25f);
		smoothRotPitch = lerp(smoothRotPitch, targetRotPitch, 0.25f);
		smoothBreakX   = lerp(smoothBreakX, targetBreakX, 0.22f);
		smoothBreakY   = lerp(smoothBreakY, targetBreakY, 0.22f);
		smoothBreakZ   = lerp(smoothBreakZ, targetBreakZ, 0.22f);
	}

	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}

	// ── Main tick ─────────────────────────────────────────────────────────────

	@Override
	public void onTick(MinecraftClient client) {
		if (client.player == null || client.world == null || client.interactionManager == null) return;
		if (client.currentScreen != null) return;

		// Manual mode: only run while RMB is held
		if (manualMode.getValue() && !client.options.useKey.isPressed()) {
			waitingForBreak = false;
			placeTicks = 0;
			return;
		}

		tickSmoothJitter();

		ClientPlayerEntity player = client.player;

		// Phase 2: break delay
		if (waitingForBreak && autoBreak.getValue()) {
			breakTicks++;
			if (breakTicks >= breakDelay.getValue().intValue()) {
				tryBreakCrystal(client, player);
				waitingForBreak = false;
				breakTicks = 0;
			}
			return;
		}

		placeTicks++;
		if (placeTicks < placeDelay.getValue().intValue()) return;

		BlockPos bestObs = findBestObsidian(client, player);

		if (bestObs != null && aimAssist.getValue()) {
			applyAimAssist(player, bestObs);
		}

		if (bestObs != null && isCrosshairOnObsidian(client, bestObs)) {
			if (ensureCrystalInHand(client)) {
				placeCrystal(client, player, bestObs);
				lastPlacedPos = bestObs;
				waitingForBreak = autoBreak.getValue();
				placeTicks = 0;
				breakTicks = 0;
			}
		}
	}

	// ── Obsidian scanning ─────────────────────────────────────────────────────

	private BlockPos findBestObsidian(MinecraftClient client, ClientPlayerEntity player) {
		double maxRange = range.getValue();
		double maxRangeSq = maxRange * maxRange;
		Vec3d eyePos = player.getEyePos();
		BlockPos playerPos = player.getBlockPos();
		int r = (int) Math.ceil(maxRange);
		BlockPos best = null;
		double bestDist = maxRangeSq;

		for (int dx = -r; dx <= r; dx++) {
			for (int dy = -r; dy <= r; dy++) {
				for (int dz = -r; dz <= r; dz++) {
					BlockPos pos = playerPos.add(dx, dy, dz);
					if (!client.world.getBlockState(pos).isOf(Blocks.OBSIDIAN)) continue;
					BlockPos above = pos.up();
					if (!client.world.getBlockState(above).isAir()) continue;
					if (!client.world.getBlockState(above.up()).isAir()) continue;
					if (hasCrystalAt(client, above)) continue;

					Vec3d obsCenter = Vec3d.ofCenter(pos).add(0, 0.5, 0);
					double distSq = eyePos.squaredDistanceTo(obsCenter);
					if (distSq > maxRangeSq) continue;
					if (!isInFov(player, obsCenter, 90.0f)) continue;

					if (antiSelfDamage.getValue() && player.getY() > pos.getY()) continue;
					if (playerPos.getX() == pos.getX() && playerPos.getZ() == pos.getZ()
							&& playerPos.getY() == pos.getY() + 1) continue;
					if (client.world.getBlockState(pos.down()).isAir()) continue;

					if (distSq < bestDist) { bestDist = distSq; best = pos; }
				}
			}
		}
		return best;
	}

	// ── FOV check ─────────────────────────────────────────────────────────────

	private boolean isInFov(ClientPlayerEntity player, Vec3d target, float fovDegrees) {
		Vec3d lookDir = player.getRotationVec(1.0f);
		Vec3d toTarget = target.subtract(player.getEyePos()).normalize();
		double angle = Math.toDegrees(Math.acos(MathHelper.clamp(lookDir.dotProduct(toTarget), -1.0, 1.0)));
		return angle <= fovDegrees / 2.0;
	}

	// ── Crosshair check ───────────────────────────────────────────────────────

	private boolean isCrosshairOnObsidian(MinecraftClient client, BlockPos obsidian) {
		if (!(client.crosshairTarget instanceof BlockHitResult bhr)) return false;
		return bhr.getBlockPos().equals(obsidian) && bhr.getSide() == Direction.UP;
	}

	// ── Aim assist ────────────────────────────────────────────────────────────

	private void applyAimAssist(ClientPlayerEntity player, BlockPos obsidian) {
		Vec3d target = Vec3d.ofCenter(obsidian).add(0, 0.5, 0);
		Vec3d delta = target.subtract(player.getEyePos());
		double hDist = Math.sqrt(delta.x * delta.x + delta.z * delta.z);

		float targetYaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
		float targetPitch = (float) Math.toDegrees(-Math.atan2(delta.y, hDist));

		float yawDiff = MathHelper.wrapDegrees(targetYaw - player.getYaw());
		float pitchDiff = targetPitch - player.getPitch();

		float speed = aimSpeed.getValue().floatValue();
		if (Math.abs(yawDiff) > 1.0f || Math.abs(pitchDiff) > 1.0f) {
			player.setYaw(player.getYaw() + MathHelper.clamp(yawDiff, -speed, speed));
			player.setPitch(MathHelper.clamp(player.getPitch() + MathHelper.clamp(pitchDiff, -speed, speed), -90f, 90f));
		}
	}

	// ── Crystal placement ─────────────────────────────────────────────────────

	private void placeCrystal(MinecraftClient client, ClientPlayerEntity player, BlockPos obsidian) {
		float spread = hitVecJitter.getValue().floatValue();
		float min = 0.5f - spread * 0.4f;
		float max = 0.5f + spread * 0.4f;
		float rx = hitVecRandom.getValue() ? min + random.nextFloat() * (max - min) : 0.5f;
		float rz = hitVecRandom.getValue() ? min + random.nextFloat() * (max - min) : 0.5f;

		Vec3d hitVec = new Vec3d(obsidian.getX() + rx, obsidian.getY() + 1.0, obsidian.getZ() + rz);
		float[] rot = calcRot(player, hitVec, true); // use smooth rot jitter
		float oy = player.getYaw(), op = player.getPitch();
		player.setYaw(rot[0]); player.setPitch(rot[1]);

		ActionResult result = client.interactionManager.interactBlock(player, Hand.MAIN_HAND,
				new BlockHitResult(hitVec, Direction.UP, obsidian, false));
		if (result.isAccepted() && swing.getValue()) player.swingHand(Hand.MAIN_HAND);

		player.setYaw(oy); player.setPitch(op);
	}

	// ── Crystal breaking ──────────────────────────────────────────────────────

	private void tryBreakCrystal(MinecraftClient client, ClientPlayerEntity player) {
		if (lastPlacedPos == null) return;
		EndCrystalEntity crystal = findCrystalAt(client, lastPlacedPos.up());
		if (crystal == null) return;

		// Use smooth break jitter for organic-looking targeting
		Vec3d crystalCenter = crystal.getPos().add(smoothBreakX, 0.5 + smoothBreakY, smoothBreakZ);
		float[] rot = calcRot(player, crystalCenter, false);
		float oy = player.getYaw(), op = player.getPitch();
		player.setYaw(rot[0]); player.setPitch(rot[1]);

		client.interactionManager.attackEntity(player, crystal);
		if (swing.getValue()) player.swingHand(Hand.MAIN_HAND);

		player.setYaw(oy); player.setPitch(op);
		lastPlacedPos = null;
	}

	// ── Utilities ─────────────────────────────────────────────────────────────

	private boolean hasCrystalAt(MinecraftClient client, BlockPos pos) {
		return findCrystalAt(client, pos) != null;
	}

	private EndCrystalEntity findCrystalAt(MinecraftClient client, BlockPos pos) {
		for (Entity e : client.world.getOtherEntities(null, new Box(pos).expand(0.5))) {
			if (e instanceof EndCrystalEntity c && c.isAlive()) return c;
		}
		return null;
	}

	private boolean ensureCrystalInHand(MinecraftClient client) {
		ClientPlayerEntity player = client.player;
		if (player.getMainHandStack().isOf(Items.END_CRYSTAL)) return true;
		if (player.getOffHandStack().isOf(Items.END_CRYSTAL)) return true;
		for (int i = 0; i < 9; i++) {
			if (player.getInventory().getStack(i).isOf(Items.END_CRYSTAL)) {
				player.getInventory().selectedSlot = i;
				return true;
			}
		}
		return false;
	}

	/**
	 * Calculates rotation toward {@code target} and adds the current smooth
	 * jitter offset — organically drifting values that look human.
	 *
	 * @param useRotJitter true for place rotation, false for break rotation
	 */
	private float[] calcRot(ClientPlayerEntity player, Vec3d target, boolean useRotJitter) {
		Vec3d d = target.subtract(player.getEyePos());
		double h = Math.sqrt(d.x * d.x + d.z * d.z);
		float yaw   = (float) Math.toDegrees(Math.atan2(-d.x, d.z));
		float pitch = (float) Math.toDegrees(-Math.atan2(d.y, h));

		if (useRotJitter) {
			yaw   += smoothRotYaw;
			pitch += smoothRotPitch;
		} else {
			// Break: use a small fraction of smooth rot jitter
			yaw   += smoothRotYaw   * 0.5f;
			pitch += smoothRotPitch * 0.5f;
		}
		return new float[]{ yaw, MathHelper.clamp(pitch, -90f, 90f) };
	}
}
