package com.bcrq.legit.client.theme;

/**
 * Theme registry and active-theme accessor.
 * <p>
 * Usage:
 * <pre>
 *   // Get the active theme tokens anywhere in render code:
 *   ThemeTokens t = GuiTheme.current();
 *   context.fill(x, y, x+w, y+h, t.surfaceContainer());
 *
 *   // Switch theme (persisted as a static field):
 *   GuiTheme.cycle();
 * </pre>
 */
public enum GuiTheme {

    /** Legitimity dark — original crimson-red aesthetic, sharp corners. */
    LEGIT(LegitTheme.INSTANCE),

    /** Material 3 Expressive dark — lavender, pill shapes, spring animations. */
    M3(M3Theme.INSTANCE);

    // ── State ────────────────────────────────────────────────────────────────

    private static GuiTheme active = M3;

    // ── Instance ─────────────────────────────────────────────────────────────

    private final ThemeTokens tokens;

    GuiTheme(ThemeTokens tokens) {
        this.tokens = tokens;
    }

    // ── API ──────────────────────────────────────────────────────────────────

    /** Returns the token set for the currently active theme. */
    public static ThemeTokens current() {
        return active.tokens;
    }

    /** Returns the active theme enum value. */
    public static GuiTheme activeTheme() {
        return active;
    }

    /** Cycles to the next theme in declaration order, wrapping around. */
    public static void cycle() {
        GuiTheme[] values = values();
        active = values[(active.ordinal() + 1) % values.length];
    }

    /** Explicitly set the active theme. */
    public static void set(GuiTheme theme) {
        active = theme;
    }

    /** Convenience: returns true when the M3 theme is currently active. */
    public static boolean isM3() {
        return active == M3;
    }

    /** Returns the tokens for this specific enum constant. */
    public ThemeTokens tokens() {
        return tokens;
    }
}
