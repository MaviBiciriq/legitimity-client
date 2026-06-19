package com.bcrq.legit.client.theme;

import com.bcrq.legit.client.util.UiRenderUtil;

/**
 * Simulated elevation system for Minecraft GUI.
 * <p>
 * True drop-shadows are impossible without shaders, so we simulate depth by
 * blending a small amount of the {@code primary} color into the surface at
 * increasing opacity levels — matching M3's "elevation tonal overlay" spec.
 * <p>
 * Levels:
 * <pre>
 *  0 — root background    (0% tint)
 *  1 — standard card      (5% tint)
 *  2 — hovered card       (8% tint)
 *  3 — floating panel     (11% tint)
 *  4 — dialog             (12% tint)
 *  5 — toast / top layer  (14% tint)
 * </pre>
 */
public final class ElevationLevel {
    private ElevationLevel() {}

    /** Alpha values (0–255) per elevation level, matching M3 tonal overlay spec. */
    private static final int[] TINT_ALPHA = { 0, 13, 20, 28, 31, 36 };

    /**
     * Returns the tint ARGB to overlay on a surface at the given elevation.
     * The tint is the theme's primary color at the level-specific alpha.
     *
     * @param primaryColor  full-opacity primary color of the current theme
     * @param level         elevation level 0–5 (clamped)
     * @return ARGB tint color ready to fill over the base surface
     */
    public static int tintForLevel(int primaryColor, int level) {
        level = Math.max(0, Math.min(5, level));
        int alpha = TINT_ALPHA[level];
        return UiRenderUtil.withAlpha(primaryColor, alpha);
    }

    /**
     * Returns a surface color with the elevation tint already blended in.
     *
     * @param surface       base surface ARGB color
     * @param primaryColor  primary color of the current theme
     * @param level         elevation level 0–5
     */
    public static int elevatedSurface(int surface, int primaryColor, int level) {
        int tint = tintForLevel(primaryColor, level);
        // Convert tint alpha to [0,1] for lerp
        float t = ((tint >> 24) & 0xFF) / 255.0f;
        return UiRenderUtil.lerpColor(surface, primaryColor, t * 0.6f);
    }

    /**
     * A thin (1 px) outer glow that M3 uses on elevated surfaces to separate
     * them from the background. Returns an ARGB color; draw as a border.
     *
     * @param outlineVariant  theme's outline-variant color
     * @param level           elevation 0–5
     */
    public static int glowBorderForLevel(int outlineVariant, int level) {
        // Higher level = slightly more visible glow border
        int baseAlpha = (outlineVariant >> 24) & 0xFF;
        int boostedAlpha = Math.min(255, baseAlpha + level * 15);
        return UiRenderUtil.withAlpha(outlineVariant, boostedAlpha);
    }
}
