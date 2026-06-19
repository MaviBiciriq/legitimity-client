package com.bcrq.legit.client.theme;

import com.bcrq.legit.client.util.AnimationUtil;

/**
 * Animation speed constants and easing helpers.
 * <p>
 * All {@code speed*} values are designed for use with
 * {@link AnimationUtil#smooth(float, float, float)}. Higher values = faster.
 * <p>
 * Rule of thumb:
 * <ul>
 *   <li>Hover in/out → {@link #FAST}</li>
 *   <li>Button press feedback → {@link #INSTANT}</li>
 *   <li>State transitions (toggle, select) → {@link #STANDARD}</li>
 *   <li>Panel slide-in / GUI open → {@link #SLOW} with {@code easeOutBack} or {@code easeOutElastic}</li>
 *   <li>Ripple decay → {@link #RIPPLE}</li>
 * </ul>
 */
public final class MotionSpec {
    private MotionSpec() {}

    // ── Speed constants (for AnimationUtil.smooth) ──────────────────────────

    /** Effectively instant — one frame. */
    public static final float INSTANT  = 1.0f;

    /** Hover micro-interactions (≈3 frames at 60 fps). */
    public static final float FAST     = 0.38f;

    /** Standard state transitions (≈5 frames). */
    public static final float STANDARD = 0.26f;

    /** Panel/overlay entrance & exit (≈8 frames). */
    public static final float SLOW     = 0.18f;

    /** Very slow — used for the GUI open animation. */
    public static final float VERY_SLOW = 0.12f;

    /** Ripple/flash decay — quick fade. */
    public static final float RIPPLE   = 0.14f;

    // ── Easing selectors ────────────────────────────────────────────────────

    public enum Easing {
        /** Linear — raw lerp output, no curve. */
        LINEAR,
        /** Ease-out cubic — fast start, smooth deceleration. */
        EASE_OUT_CUBIC,
        /** Ease-in-out quad — symmetric accel + decel (crossfades). */
        EASE_IN_OUT_QUAD,
        /** Ease-out back — slight overshoot (premium feel). */
        EASE_OUT_BACK,
        /** Ease-out elastic — spring bounce (M3 Expressive). */
        EASE_OUT_ELASTIC,
    }

    /**
     * Applies the chosen easing curve to a normalized progress value {@code t ∈ [0,1]}.
     */
    public static float ease(float t, Easing curve) {
        return switch (curve) {
            case LINEAR          -> AnimationUtil.clamp(t, 0f, 1f);
            case EASE_OUT_CUBIC  -> AnimationUtil.easeOutCubic(t);
            case EASE_IN_OUT_QUAD-> AnimationUtil.easeInOutQuad(t);
            case EASE_OUT_BACK   -> AnimationUtil.easeOutBack(t);
            case EASE_OUT_ELASTIC-> AnimationUtil.easeOutElastic(t);
        };
    }

    // ── Theme-specific defaults ──────────────────────────────────────────────

    /**
     * Returns the recommended GUI-open easing for the given theme name.
     * M3 uses spring-elastic; Legit uses ease-out-back.
     */
    public static Easing openEasing(String themeName) {
        return "M3".equals(themeName) ? Easing.EASE_OUT_ELASTIC : Easing.EASE_OUT_BACK;
    }
}
