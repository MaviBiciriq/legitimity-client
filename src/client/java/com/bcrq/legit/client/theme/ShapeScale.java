package com.bcrq.legit.client.theme;

/**
 * Named border-radius constants.
 * Use these instead of hard-coded pixel values so that theme switches
 * automatically propagate the correct shape without touching render code.
 */
public final class ShapeScale {
    private ShapeScale() {}

    /** 0 px — sharp corners (Legit classic). */
    public static final int NONE        = 0;

    /** 4 px — badges, small chips, tooltips. */
    public static final int EXTRA_SMALL = 4;

    /** 8 px — input fields, dropdowns, small cards. */
    public static final int SMALL       = 8;

    /** 12 px — module cards, setting rows, standard panels. */
    public static final int MEDIUM      = 12;

    /** 16 px — large cards, FAB, main panels. */
    public static final int LARGE       = 16;

    /** 20 px — root dialogs, sheet surfaces. */
    public static final int EXTRA_LARGE = 20;

    /**
     * Full pill (9999 px — clamped internally to half the smallest dimension).
     * Use for nav-rail indicators, toggle tracks, and pill buttons.
     */
    public static final int FULL        = 9999;
}
