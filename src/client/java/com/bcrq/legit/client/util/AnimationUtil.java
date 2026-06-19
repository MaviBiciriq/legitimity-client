package com.bcrq.legit.client.util;

public final class AnimationUtil {
	private AnimationUtil() {
	}

	// ── Core lerp / smooth ──────────────────────────────────────────────────

	/** Frame-rate-independent exponential smooth (lerp). */
	public static float smooth(float current, float target, float speed) {
		return current + (target - current) * clamp(speed, 0.0f, 1.0f);
	}

	/** Frame-rate-independent exponential smooth (lerp). */
	public static double smooth(double current, double target, double speed) {
		return current + (target - current) * clamp(speed, 0.0, 1.0);
	}

	/** Linear interpolation between a and b by normalized t [0,1]. */
	public static float lerp(float a, float b, float t) {
		return a + (b - a) * clamp(t, 0.0f, 1.0f);
	}

	/** Linear interpolation between a and b by normalized t [0,1]. */
	public static double lerp(double a, double b, double t) {
		return a + (b - a) * clamp(t, 0.0, 1.0);
	}

	// ── Easing curves ───────────────────────────────────────────────────────

	/**
	 * Ease-out cubic — fast start, smooth deceleration.
	 * Great for panels sliding into view or elements appearing.
	 */
	public static float easeOutCubic(float t) {
		t = clamp(t, 0.0f, 1.0f);
		float f = 1.0f - t;
		return 1.0f - f * f * f;
	}

	/**
	 * Ease-in-out quad — symmetrical acceleration then deceleration.
	 * Great for crossfades and category transitions.
	 */
	public static float easeInOutQuad(float t) {
		t = clamp(t, 0.0f, 1.0f);
		return t < 0.5f ? 2.0f * t * t : 1.0f - (-2.0f * t + 2.0f) * (-2.0f * t + 2.0f) / 2.0f;
	}

	/**
	 * Ease-out back — slight overshoot before settling.
	 * Gives a premium "spring" feel to GUI open animations.
	 */
	public static float easeOutBack(float t) {
		t = clamp(t, 0.0f, 1.0f);
		float c1 = 1.70158f;
		float c3 = c1 + 1.0f;
		float f = t - 1.0f;
		return 1.0f + c3 * f * f * f + c1 * f * f;
	}

	/**
	 * Ease-out elastic — decaying oscillation.
	 * Use sparingly for attention-grabbing micro-animations.
	 */
	public static float easeOutElastic(float t) {
		t = clamp(t, 0.0f, 1.0f);
		if (t == 0.0f || t == 1.0f) {
			return t;
		}
		float c4 = (float) (2.0 * Math.PI / 3.0);
		return (float) (Math.pow(2.0, -10.0 * t) * Math.sin((t * 10.0 - 0.75) * c4) + 1.0);
	}

	// ── Periodic / breathing ────────────────────────────────────────────────

	/**
	 * Smooth oscillating pulse between min and max.
	 * @param speed  cycles per second
	 * @param min    minimum output value
	 * @param max    maximum output value
	 */
	public static float pulse(float speed, float min, float max) {
		float t = (float) ((Math.sin(System.currentTimeMillis() / 1000.0 * Math.PI * 2.0 * speed) + 1.0) * 0.5);
		return lerp(min, max, t);
	}

	// ── Clamp ───────────────────────────────────────────────────────────────

	public static float clamp(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}

	public static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	public static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	// ── Additional easing curves ─────────────────────────────────────────────

	/**
	 * Ease-out quint — very fast start with ultra-smooth landing.
	 * Excellent for value labels and chip pop-ins.
	 */
	public static float easeOutQuint(float t) {
		t = clamp(t, 0.0f, 1.0f);
		float f = 1.0f - t;
		return 1.0f - f * f * f * f * f;
	}

	/**
	 * Ease-out sine — gentle, organic deceleration.
	 * Good for colour crossfades and overlay fades.
	 */
	public static float easeOutSine(float t) {
		t = clamp(t, 0.0f, 1.0f);
		return (float) Math.sin(t * Math.PI / 2.0);
	}

	// ── Spring simulation ────────────────────────────────────────────────────

	/**
	 * A critically-damped spring that moves {@code current} toward {@code target}.
	 * Unlike {@link #smooth}, this version accumulates velocity over frames and
	 * produces a natural overshoot + settle behaviour.
	 *
	 * <p><b>Usage:</b> store both {@code value} and {@code velocity} as instance
	 * fields and call each render frame:
	 * <pre>
	 *   float[] state = { current, 0f }; // [value, velocity]
	 *   state = AnimationUtil.spring(state[0], state[1], target, 200f, 20f);
	 *   float drawn = state[0];
	 * </pre>
	 *
	 * @param current   current animated value
	 * @param velocity  current velocity (updated each call)
	 * @param target    goal value
	 * @param stiffness spring stiffness (100–400; higher = snappier)
	 * @param damping   damping ratio (10–30; higher = less bounce)
	 * @return float[2] where [0]=new value, [1]=new velocity
	 */
	public static float[] spring(float current, float velocity, float target,
			float stiffness, float damping) {
		float dt = 1.0f / 60.0f; // assume 60 fps
		float force    = -stiffness * (current - target);
		float dampForce = -damping * velocity;
		velocity += (force + dampForce) * dt;
		current  += velocity * dt;
		return new float[]{ current, velocity };
	}

	// ── Stagger helper ───────────────────────────────────────────────────────

	/**
	 * Returns a delayed normalised progress for staggered animations.
	 * Each item in a list starts its animation {@code delayPerItem * index} ms later.
	 *
	 * @param globalProgress  overall animation progress [0,1]
	 * @param index           item index in the staggered list
	 * @param totalItems      total number of staggered items
	 * @return clamped normalised progress [0,1] for this item
	 */
	public static float stagger(float globalProgress, int index, int totalItems) {
		if (totalItems <= 0) return clamp(globalProgress, 0f, 1f);
		float offset = index / (float) totalItems * 0.4f; // 40% total stagger window
		return clamp((globalProgress - offset) / (1.0f - offset), 0f, 1f);
	}
}
