package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.BooleanSetting;
import com.bcrq.legit.client.setting.EnumSetting;
import com.bcrq.legit.client.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * Vape-style velocity / anti-knockback module.
 * <p>
 * Reduces or modifies incoming knockback with multiple AC bypass
 * strategies: chance-based application, sprint reset, delayed
 * application, jump reset, and reverse push.
 */
public final class VelocityModule extends Module {

	// ── Core settings ────────────────────────────────────────────────────────
	private final NumberSetting horizontal = addSetting(
			new NumberSetting(this, "Horizontal", "Horizontal knockback kept %.", 0.0D, 0.0D, 100.0D, 1.0D));
	private final NumberSetting vertical = addSetting(
			new NumberSetting(this, "Vertical", "Vertical knockback kept %.", 0.0D, 0.0D, 100.0D, 1.0D));
	private final EnumSetting<VelocityMode> mode = addSetting(
			new EnumSetting<>(this, "Mode", "Anti-knockback strategy.", VelocityMode.NORMAL, VelocityMode.class));

	// ── AC bypass settings ───────────────────────────────────────────────────
	private final NumberSetting chance = addSetting(
			new NumberSetting(this, "Chance", "Probability of reducing knockback %.", 100.0D, 0.0D, 100.0D, 1.0D));
	private final NumberSetting delayTicks = addSetting(
			new NumberSetting(this, "Delay", "Ticks before applying reduction.", 0.0D, 0.0D, 5.0D, 1.0D));
	private final BooleanSetting onlyGround = addSetting(
			new BooleanSetting(this, "Only Ground", "Only apply while on the ground.", false));
	private final BooleanSetting onlySprint = addSetting(
			new BooleanSetting(this, "Only Sprint", "Only apply while sprinting (Vape bypass).", false));
	private final BooleanSetting sprintReset = addSetting(
			new BooleanSetting(this, "Sprint Reset", "Re-enable sprint after knockback.", true));
	private final BooleanSetting jumpReset = addSetting(
			new BooleanSetting(this, "Jump Reset", "Jump on knockback to reset vertical velocity.", false));

	// ── Reverse mode ─────────────────────────────────────────────────────────
	private final NumberSetting reverseStrength = addSetting(
			new NumberSetting(this, "Reverse Strength", "Forward push multiplier (Reverse mode only).", 0.4D, 0.0D, 1.0D, 0.05D));

	// ── Internal state ───────────────────────────────────────────────────────
	private final Random random = new Random();
	private Vec3d pendingVelocity;
	private int pendingDelay;
	private boolean shouldSprintReset;

	public VelocityModule() {
		super("Velocity", "Reduces knockback with anti-cheat bypasses.", ModuleCategory.COMBAT);
	}

	@Override
	public void onEnable() {
		pendingVelocity = null;
		pendingDelay = 0;
		shouldSprintReset = false;
	}

	@Override
	public void onTick(MinecraftClient client) {
		if (client.player == null) return;
		ClientPlayerEntity player = client.player;

		// Handle delayed velocity application
		if (pendingVelocity != null) {
			if (--pendingDelay <= 0) {
				player.setVelocity(pendingVelocity);
				pendingVelocity = null;
				postKnockbackActions(player);
			}
		}

		// Sprint reset on next tick after hit
		if (shouldSprintReset) {
			if (sprintReset.getValue() && player.input.playerInput.forward()) {
				player.setSprinting(true);
			}
			shouldSprintReset = false;
		}
	}

	// ── Called from mixin when velocity packet arrives for the player ─────────

	/**
	 * Processes an incoming velocity update for the local player.
	 * Called from {@code ClientPlayNetworkHandlerMixin} after the
	 * vanilla handler has already applied the velocity.
	 */
	public void onVelocityUpdate(ClientPlayerEntity player) {
		// Conditional AC bypass checks
		if (onlyGround.getValue() && !player.isOnGround()) return;
		if (onlySprint.getValue() && !player.isSprinting()) return;

		// Chance bypass — some hits go through unmodified
		if (chance.getValue() < 100.0D && random.nextDouble() * 100.0D > chance.getValue()) return;

		Vec3d vel = player.getVelocity();
		double h = horizontal.getValue() / 100.0D;
		double v = vertical.getValue() / 100.0D;

		Vec3d modified = switch (mode.getValue()) {
			case REVERSE -> {
				double str = reverseStrength.getValue();
				yield new Vec3d(-vel.x * str, vel.y * v, -vel.z * str);
			}
			case JUMP_RESET -> new Vec3d(vel.x * h, 0.0D, vel.z * h);
			default -> new Vec3d(vel.x * h, vel.y * v, vel.z * h);
		};

		int wait = delayTicks.getValue().intValue();
		if (wait > 0) {
			pendingVelocity = modified;
			pendingDelay = wait;
		} else {
			player.setVelocity(modified);
			postKnockbackActions(player);
		}

		shouldSprintReset = sprintReset.getValue();
	}

	private void postKnockbackActions(ClientPlayerEntity player) {
		if (jumpReset.getValue() && player.isOnGround()) {
			player.jump();
		}
	}

	// ── Mode enum ────────────────────────────────────────────────────────────

	public enum VelocityMode {
		NORMAL("Normal"), REVERSE("Reverse"), JUMP_RESET("Jump Reset");
		private final String label;
		VelocityMode(String label) { this.label = label; }
		@Override public String toString() { return label; }
	}
}
