package com.bcrq.legit.client.theme;

/**
 * Central color/shape/motion token contract.
 * <p>
 * Every theme (LEGIT, M3, …) must implement this interface so that all
 * rendering code stays decoupled from concrete palette values.
 * <p>
 * Conventions:
 * <ul>
 *   <li>All color methods return packed ARGB {@code int} values.</li>
 *   <li>Alpha is part of the value — semi-transparent surfaces are intentional.</li>
 *   <li>State-variant helpers (hover, pressed, disabled) return a ready-to-draw
 *       color that already encodes the correct alpha.</li>
 * </ul>
 */
public interface ThemeTokens {

    // ────────────────────────────────────────────────────────────────────────
    // Surface hierarchy
    // ────────────────────────────────────────────────────────────────────────

    /** Deepest background — root panel / screen background. */
    int surface();

    /** Darker tint — dimmed overlay behind modals. */
    int surfaceDim();

    /** Default panel / sheet background. */
    int surfaceContainer();

    /** Elevated card / input background. */
    int surfaceContainerHigh();

    /** Bright hover/pressed tint applied over cards. */
    int surfaceBright();

    // ────────────────────────────────────────────────────────────────────────
    // On-surface text & borders
    // ────────────────────────────────────────────────────────────────────────

    /** Primary (high-emphasis) text color. */
    int onSurface();

    /** Secondary (muted) text color. */
    int onSurfaceVariant();

    /** Visible border / stroke. */
    int outline();

    /** Subtle divider / barely-visible border. */
    int outlineVariant();

    // ────────────────────────────────────────────────────────────────────────
    // Primary role (key brand / main accent)
    // ────────────────────────────────────────────────────────────────────────

    int primary();
    int primaryContainer();
    int onPrimaryContainer();

    // ────────────────────────────────────────────────────────────────────────
    // Secondary role
    // ────────────────────────────────────────────────────────────────────────

    int secondary();
    int secondaryContainer();
    int onSecondaryContainer();

    // ────────────────────────────────────────────────────────────────────────
    // Tertiary role (differentiated accent)
    // ────────────────────────────────────────────────────────────────────────

    int tertiary();
    int tertiaryContainer();
    int onTertiaryContainer();

    // ────────────────────────────────────────────────────────────────────────
    // Semantic state colors
    // ────────────────────────────────────────────────────────────────────────

    int error();
    int onError();
    int errorContainer();

    int warning();
    int success();
    int info();

    // ────────────────────────────────────────────────────────────────────────
    // Component state overlays
    // These return an ARGB color to BLEND (fill over) the base surface.
    // Alpha encodes the state-layer opacity per M3 spec.
    // ────────────────────────────────────────────────────────────────────────

    /** 8 % primary opacity — hover state layer. */
    int stateHover();

    /** 12 % primary opacity — pressed state layer. */
    int statePressed();

    /** 12 % primary opacity — focus state layer. */
    int stateFocused();

    /** Applied to both bg and text to communicate disabled. */
    int stateDisabledOverlay();

    // ────────────────────────────────────────────────────────────────────────
    // Shape scale (border radii in pixels)
    // ────────────────────────────────────────────────────────────────────────

    /** For badges, tooltips, small chips. */
    int radiusExtraSmall();

    /** For input fields, small buttons, dropdowns. */
    int radiusSmall();

    /** For module cards, setting rows. */
    int radiusMedium();

    /** For panels, large buttons, FABs. */
    int radiusLarge();

    /** For root dialogs, full-screen sheets. */
    int radiusExtraLarge();

    /** Full pill — nav rail indicator, toggle track. Returns a large sentinel. */
    int radiusFull();

    // ────────────────────────────────────────────────────────────────────────
    // Motion speed constants (for AnimationUtil.smooth / spring)
    // ────────────────────────────────────────────────────────────────────────

    /** Hover/micro interactions. */
    float speedFast();

    /** Standard state transitions. */
    float speedStandard();

    /** Panel open / close animations. */
    float speedSlow();

    // ────────────────────────────────────────────────────────────────────────
    // Elevation surface tint
    // Returns the ARGB tint color blended over a surface at each elevation
    // level (M3 uses primary color at increasing opacities).
    // ────────────────────────────────────────────────────────────────────────

    /** Tint color blended at the given elevation level (0–5). */
    int elevationTint(int level);

    // ────────────────────────────────────────────────────────────────────────
    // Theme metadata
    // ────────────────────────────────────────────────────────────────────────

    /** Human-readable name shown in the GUI switcher button. */
    String displayName();

    /** Whether this theme is a dark-mode variant. */
    boolean isDark();
}
