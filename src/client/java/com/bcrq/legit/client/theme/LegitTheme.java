package com.bcrq.legit.client.theme;

/**
 * Legitimity "Legit" dark theme — the original client aesthetic.
 * <p>
 * Characteristic feel: deep navy-black surfaces, crimson red accent,
 * sharp corners, compact density. "Sleek hacker terminal" energy.
 */
public final class LegitTheme implements ThemeTokens {

    public static final LegitTheme INSTANCE = new LegitTheme();
    private LegitTheme() {}

    // ── Surfaces ─────────────────────────────────────────────────────────────
    @Override public int surface()               { return 0xD814151C; }
    @Override public int surfaceDim()            { return 0xE00E0F14; }
    @Override public int surfaceContainer()      { return 0xC31A1C24; }
    @Override public int surfaceContainerHigh()  { return 0xC61E2029; }
    @Override public int surfaceBright()         { return 0xC0252830; }

    // ── Text / borders ───────────────────────────────────────────────────────
    @Override public int onSurface()             { return 0xFFF3F5F7; }
    @Override public int onSurfaceVariant()      { return 0xFF9499A6; }
    @Override public int outline()               { return 0x90444756; }
    @Override public int outlineVariant()        { return 0x603A3D4A; }

    // ── Primary (crimson) ────────────────────────────────────────────────────
    @Override public int primary()               { return 0xFFFF4D6D; }
    @Override public int primaryContainer()      { return 0x80FF4D6D; }
    @Override public int onPrimaryContainer()    { return 0xFFF3F5F7; }

    // ── Secondary (coral) ────────────────────────────────────────────────────
    @Override public int secondary()             { return 0xFFFF7A6E; }
    @Override public int secondaryContainer()    { return 0x60FF7A6E; }
    @Override public int onSecondaryContainer()  { return 0xFFF3F5F7; }

    // ── Tertiary (cyan highlight) ────────────────────────────────────────────
    @Override public int tertiary()              { return 0xFF00D2FF; }
    @Override public int tertiaryContainer()     { return 0x5000D2FF; }
    @Override public int onTertiaryContainer()   { return 0xFFF3F5F7; }

    // ── State colors ─────────────────────────────────────────────────────────
    @Override public int error()                 { return 0xFFFF5252; }
    @Override public int onError()               { return 0xFF1C0002; }
    @Override public int errorContainer()        { return 0x80FF5252; }
    @Override public int warning()               { return 0xFFFFB74D; }
    @Override public int success()               { return 0xFF66BB6A; }
    @Override public int info()                  { return 0xFF42A5F5; }

    // ── State layers ─────────────────────────────────────────────────────────
    @Override public int stateHover()            { return 0x20FF4D6D; } // 12% primary
    @Override public int statePressed()          { return 0x30FF4D6D; } // 19% primary
    @Override public int stateFocused()          { return 0x28FF4D6D; }
    @Override public int stateDisabledOverlay()  { return 0x60000000; }

    // ── Shape ────────────────────────────────────────────────────────────────
    @Override public int radiusExtraSmall()      { return 0; }  // Legit = always sharp
    @Override public int radiusSmall()           { return 0; }
    @Override public int radiusMedium()          { return 0; }
    @Override public int radiusLarge()           { return 0; }
    @Override public int radiusExtraLarge()      { return 0; }
    @Override public int radiusFull()            { return 0; }

    // ── Motion ───────────────────────────────────────────────────────────────
    @Override public float speedFast()           { return MotionSpec.FAST; }
    @Override public float speedStandard()       { return MotionSpec.STANDARD; }
    @Override public float speedSlow()           { return MotionSpec.SLOW; }

    // ── Elevation tint ───────────────────────────────────────────────────────
    // Legit doesn't use tonal elevation — returns transparent.
    @Override public int elevationTint(int level) { return 0x00000000; }

    // ── Metadata ─────────────────────────────────────────────────────────────
    @Override public String displayName()        { return "Legit"; }
    @Override public boolean isDark()            { return true; }
}
