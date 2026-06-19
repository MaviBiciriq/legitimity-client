package com.bcrq.legit.client.theme;

/**
 * Canonical spacing / padding scale used across all components.
 * All measurements are in pixels (Minecraft GUI coordinates).
 */
public final class SpacingScale {
    private SpacingScale() {}

    /** 2 px — icon padding, thin dividers. */
    public static final int XS     = 2;

    /** 4 px — between label and icon, chip inner padding. */
    public static final int SM     = 4;

    /** 8 px — standard inner padding, row gap. */
    public static final int MD     = 8;

    /** 12 px — card inner padding, section gap. */
    public static final int LG     = 12;

    /** 16 px — panel margin, large section padding. */
    public static final int XL     = 16;

    /** 20 px — dialog margin. */
    public static final int XXL    = 20;

    /** 24 px — screen-edge safe margin. */
    public static final int XXXL   = 24;

    /** 32 px — between major sections / zones. */
    public static final int SECTION = 32;
}
