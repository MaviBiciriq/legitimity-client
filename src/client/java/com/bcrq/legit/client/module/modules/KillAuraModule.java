package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.BooleanSetting;
import com.bcrq.legit.client.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * KillAura with target-lock support.
 * <p>
 * When target lock is enabled the server-side rotation is continuously
 * aimed at the current target (visible in F5) while the player's camera
 * remains fully independent. A mixin on {@code sendMovementPackets}
 * spoofs the rotation that goes to the server.
 */
public final class KillAuraModule extends Module {

	// ── Settings ─────────────────────────────────────────────────────────────
	private final NumberSetting range = addSetting(
			new NumberSetting(this, "Range", "Attack range in blocks.", 3.0D, 3.0D, 6.0D, 0.1D));
	private final NumberSetting cps = addSetting(
			new NumberSetting(this, "CPS", "Max clicks per second.", 12.0D, 5.0D, 20.0D, 1.0D));
	private final NumberSetting jitter = addSetting(
			new NumberSetting(this, "Jitter", "Random rotation offset degrees.", 1.0D, 0.0D, 3.0D, 0.1D));
	private final BooleanSetting cooldownSync = addSetting(
			new BooleanSetting(this, "Cooldown Sync", "Wait for vanilla attack cooldown.", true));
	private final BooleanSetting targetLock = addSetting(
			new BooleanSetting(this, "Target Lock", "Lock server rotation to target; camera stays free. Visible in F5.", true));
	private final NumberSetting fov = addSetting(
			new NumberSetting(this, "FOV", "Field of view for targeting (360 = all around).", 180.0D, 30.0D, 360.0D, 5.0D));
	private final BooleanSetting players = addSetting(
			new BooleanSetting(this, "Players", "Target players.", true));
	private final BooleanSetting hostiles = addSetting(
			new BooleanSetting(this, "Hostiles", "Target hostile mobs.", true));
	private final BooleanSetting passives = addSetting(
			new BooleanSetting(this, "Passives", "Target passive mobs.", false));
	private final BooleanSetting invisibles = addSetting(
			new BooleanSetting(this, "Invisibles", "Target invisible entities.", false));

	// ── Internal state ───────────────────────────────────────────────────────
	private final Random random = new Random();
	private long lastAttackTime;

	// Target lock state — read by the movement-packet mixin
	private boolean lockActive;
	private float lockedYaw;
	private float lockedPitch;
	private float realYaw;
	private float realPitch;

	public KillAuraModule() {
		super("Kill Aura", "Automatically attacks nearby entities.", ModuleCategory.COMBAT);
	}

	@Override
	public void onEnable() {
		lastAttackTime = 0;
		lockActive = false;
	}

	@Override
	public void onDisable() {
		lockActive = false;
	}

	@Override
	public void onTick(MinecraftClient client) {
		if (client.player == null || client.world == null || client.interactionManager == null) return;
		if (client.currentScreen != null) { lockActive = false; return; }

		ClientPlayerEntity player = client.player;
		double maxRange = range.getValue();
		LivingEntity target = findTarget(client, player, maxRange);

		// ── Target lock: update server-side rotation every tick ───────────
		if (targetLock.getValue() && target != null) {
			float[] rot = calcRotation(player, target);
			lockedYaw = rot[0];
			lockedPitch = rot[1];
			lockActive = true;

			// Set head/body yaw so the player model visually faces the target (F5)
			player.headYaw = lockedYaw;
			player.bodyYaw = lockedYaw;
		} else {
			lockActive = false;
		}

		if (target == null) return;

		// ── CPS timing ───────────────────────────────────────────────────
		long now = System.currentTimeMillis();
		long minDelay = (long) (1000.0 / cps.getValue());
		if (now - lastAttackTime < minDelay) return;

		// Cooldown sync
		if (cooldownSync.getValue() && player.getAttackCooldownProgress(0.0f) < 1.0f) return;

		// ── Attack with silent rotation ──────────────────────────────────
		float[] rot = calcRotation(player, target);
		float origYaw = player.getYaw();
		float origPitch = player.getPitch();
		player.setYaw(rot[0]);
		player.setPitch(rot[1]);

		client.interactionManager.attackEntity(player, target);
		player.swingHand(Hand.MAIN_HAND);
		lastAttackTime = now;

		// Restore camera rotation
		player.setYaw(origYaw);
		player.setPitch(origPitch);
	}

	// ── Target lock API (called by mixin) ────────────────────────────────────

	/** Whether the mixin should spoof movement packet rotations. */
	public boolean hasTargetLock() { return isEnabled() && lockActive; }
	public float getLockedYaw()    { return lockedYaw; }
	public float getLockedPitch()  { return lockedPitch; }
	public void storeRealRotation(float yaw, float pitch) { realYaw = yaw; realPitch = pitch; }
	public float getRealYaw()   { return realYaw; }
	public float getRealPitch() { return realPitch; }

	// ── Target finding ───────────────────────────────────────────────────────

	private LivingEntity findTarget(MinecraftClient client, ClientPlayerEntity player, double maxRange) {
		LivingEntity best = null;
		double bestDist = maxRange * maxRange;

		for (Entity entity : client.world.getEntities()) {
			if (!(entity instanceof LivingEntity living)) continue;
			if (living == player || !living.isAlive()) continue;
			if (living.isInvisibleTo(player) && !invisibles.getValue()) continue;
			if (fov.getValue() < 360.0D && !isInFov(player, living)) continue;

			double distSq = living.squaredDistanceTo(player);
			if (distSq > bestDist) continue;

			if (living instanceof PlayerEntity) {
				if (!players.getValue()) continue;
			} else if (living instanceof HostileEntity) {
				if (!hostiles.getValue()) continue;
			} else if (isPassive(living)) {
				if (!passives.getValue()) continue;
			} else {
				continue;
			}

			best = living;
			bestDist = distSq;
		}
		return best;
	}

	private boolean isInFov(ClientPlayerEntity player, Entity target) {
		Vec3d lookDir = player.getRotationVec(1.0f);
		Vec3d toTarget = target.getPos().add(0, target.getHeight() / 2.0, 0)
				.subtract(player.getEyePos()).normalize();
		double angle = Math.toDegrees(Math.acos(
				MathHelper.clamp(lookDir.dotProduct(toTarget), -1.0, 1.0)));
		return angle <= fov.getValue() / 2.0;
	}

	private boolean isPassive(LivingEntity entity) {
		return entity instanceof AnimalEntity
				|| entity instanceof MerchantEntity
				|| entity instanceof VillagerEntity;
	}

	private float[] calcRotation(ClientPlayerEntity player, Entity target) {
		Vec3d eyePos = player.getEyePos();
		Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2.0, 0);
		Vec3d delta = targetPos.subtract(eyePos);

		double hDist = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
		float yaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
		float pitch = (float) Math.toDegrees(-Math.atan2(delta.y, hDist));

		float j = jitter.getValue().floatValue();
		if (j > 0) {
			yaw += (random.nextFloat() - 0.5f) * 2f * j;
			pitch += (random.nextFloat() - 0.5f) * 2f * j;
		}
		return new float[]{ yaw, MathHelper.clamp(pitch, -90f, 90f) };
	}
}
