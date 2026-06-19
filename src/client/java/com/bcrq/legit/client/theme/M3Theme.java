package com.bcrq.legit.client.theme;

/**
 * Material 3 Expressive dark theme.
 * <p>
 * Seed color: {@code #6750A4} (M3 baseline purple).
 * Generated via Material Theme Builder, dark scheme.
 * <p>
 * Characteristic feel: deep purple-black surfaces, lavender primary,
 * pink tertiary, maximum 20 px rounded corners, spring-elastic open
 * animations, pill-shaped navigation rail, tonal elevation overlays.
 */
public final class M3Theme implements ThemeTokens {

    public static final M3Theme INSTANCE = new M3Theme();
    private M3Theme() {}

    // ── Surfaces ─────────────────────────────────────────────────────────────
    //   M3 dark baseline surface tones from the neutral-variant palette.
    @Override public int surface()               { return 0xFF111318; }
    @Override public int surfaceDim()            { return 0xFF0B0D12; } // Neutral6  — modal overlay bg
    @Override public int surfaceContainer()      { return 0xFF1B1D24; } // Neutral12 — panels / sheets
    @Override public int surfaceContainerHigh()  { return 0xFF262934; } // Neutral17 — cards / inputs
    @Override public int surfaceBright()         { return 0xFF343846; } // Neutral22 — hover tint

    // ── Text / borders ───────────────────────────────────────────────────────
    @Override public int onSurface()             { return 0xFFF0EFF7; } // Neutral90
    @Override public int onSurfaceVariant()      { return 0xFFC9CAD8; } // NeutralVariant80
    @Override public int outline()               { return 0xFF9AA0B5; } // NeutralVariant60
    @Override public int outlineVariant()        { return 0xFF454B5F; } // NeutralVariant30

    // ── Primary (purple lavender) ────────────────────────────────────────────
    @Override public int primary()               { return 0xFFD0BCFF; } // Primary80
    @Override public int primaryContainer()      { return 0xFF4F378B; } // Primary30
    @Override public int onPrimaryContainer()    { return 0xFFF4EEFF; } // Primary95

    // ── Secondary (soft lavender) ────────────────────────────────────────────
    @Override public int secondary()             { return 0xFFA8E6CF; } // Secondary80
    @Override public int secondaryContainer()    { return 0xFF284E43; } // Secondary30
    @Override public int onSecondaryContainer()  { return 0xFFE6FFF5; } // Secondary95

    // ── Tertiary (pink rose) ──────────────────────────────────────────────────
    @Override public int tertiary()              { return 0xFFFFB1C8; } // Tertiary80
    @Override public int tertiaryContainer()     { return 0xFF673245; } // Tertiary30
    @Override public int onTertiaryContainer()   { return 0xFFFFE8EF; } // Tertiary95

    // ── State / semantic colors ───────────────────────────────────────────────
    @Override public int error()                 { return 0xFFF2B8B5; } // Error80
    @Override public int onError()               { return 0xFF601410; }
    @Override public int errorContainer()        { return 0xFF8C1D18; }
    @Override public int warning()               { return 0xFFFFD08A; }
    @Override public int success()               { return 0xFFA8E6CF; }
    @Override public int info()                  { return 0xFF9DD7FF; }

    // ── State layer overlays (M3 spec: blend primary @ given % over surface) ──
    //   M3: hover = 8%, pressed = 12%, focus = 12%  of primary (#D0BCFF)
    @Override public int stateHover()            { return 0x14D0BCFF; } // ~8%
    @Override public int statePressed()          { return 0x1FD0BCFF; } // ~12%
    @Override public int stateFocused()          { return 0x1FD0BCFF; } // ~12%
    @Override public int stateDisabledOverlay()  { return 0x61E6E1E5; } // 38% on-surface

    // ── Shape — M3 Expressive rounded scale ──────────────────────────────────
    @Override public int radiusExtraSmall()      { return 8;  }
    @Override public int radiusSmall()           { return 14; }
    @Override public int radiusMedium()          { return 22; }
    @Override public int radiusLarge()           { return 28; }
    @Override public int radiusExtraLarge()      { return 34; }
    @Override public int radiusFull()            { return ShapeScale.FULL;        } // pill

    // ── Motion — spring-elastic style ────────────────────────────────────────
    @Override public float speedFast()           { return MotionSpec.FAST; }
    @Override public float speedStandard()       { return MotionSpec.STANDARD; }
    @Override public float speedSlow()           { return MotionSpec.SLOW; }

    // ── Elevation tonal tint ──────────────────────────────────────────────────
    //   Blend primary at 5/8/11/12/14% per elevation level 0–5.
    private static final int[] TINT_ALPHA = { 0, 13, 20, 28, 31, 36 };

    @Override
    public int elevationTint(int level) {
        level = Math.max(0, Math.min(5, level));
        int alpha = TINT_ALPHA[level];
        // Return primary color at the level-specific alpha
        return (alpha << 24) | (primary() & 0x00FFFFFF);
    }

    // ── Metadata ─────────────────────────────────────────────────────────────
    @Override public String displayName()        { return "M3E (beta)"; }
    @Override public boolean isDark()            { return true; }
}
