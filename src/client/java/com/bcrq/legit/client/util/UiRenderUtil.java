package com.bcrq.legit.client.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class UiRenderUtil {
	// ── Legit theme tokens ───────────────────────────────────────────────────
	public static final int PANEL = 0xD814151C;
	public static final int PANEL_SOFT = 0xC31A1C24;
	public static final int PANEL_ALT = 0xC61E2029;
	public static final int OUTLINE = 0x90444756;
	public static final int TEXT = 0xFFF3F5F7;
	public static final int MUTED = 0xFF9499A6;
	public static final int ACCENT = 0xFFFF4D6D;
	public static final int ACCENT_SOFT = 0x80FF4D6D;

	// ── Material 3 Expressive dark theme tokens (seed color: #6750A4 — M3 baseline purple) ────
	/** M3 dark surface — deepest layer, app background (#141218) */
	public static final int M3_SURFACE            = 0xFF141218;
	/** M3 surface container — panels/sheets (#211F26) */
	public static final int M3_SURFACE_CONTAINER  = 0xFF211F26;
	/** M3 surface container high — cards and elevated elements (#2B2930) */
	public static final int M3_SURFACE_VARIANT    = 0xFF2B2930;
	/** M3 surface bright — hover/pressed overlay tint (#36343B) */
	public static final int M3_SURFACE_BRIGHT     = 0xFF36343B;
	/** M3 primary — lavender, key actions (#D0BCFF) */
	public static final int M3_PRIMARY            = 0xFFD0BCFF;
	/** M3 primary container — deep purple, selected backgrounds (#4F378B) */
	public static final int M3_PRIMARY_CONTAINER  = 0xFF4F378B;
	/** M3 on-primary-container — light lavender, text on primary container (#EADDFF) */
	public static final int M3_ON_PRIMARY_CONT    = 0xFFEADDFF;
	/** M3 secondary — pale lavender (#CCC2DC) */
	public static final int M3_SECONDARY          = 0xFFCCC2DC;
	/** M3 secondary container — muted purple (#4A4458) */
	public static final int M3_SECONDARY_CONTAINER= 0xFF4A4458;
	/** M3 on-secondary-container (#E8DEF8) */
	public static final int M3_ON_SECONDARY_CONT  = 0xFFE8DEF8;
	/** M3 tertiary — pink accent (#EFB8C8) */
	public static final int M3_TERTIARY           = 0xFFEFB8C8;
	/** M3 on-surface — primary text (#E6E1E5) */
	public static final int M3_ON_SURFACE         = 0xFFE6E1E5;
	/** M3 on-surface-variant — secondary text (#CAC4D0) */
	public static final int M3_ON_SURFACE_VARIANT = 0xFFCAC4D0;
	/** M3 outline — visible borders (#938F99) */
	public static final int M3_OUTLINE            = 0xFF938F99;
	/** M3 outline-variant — subtle dividers (#49454F) */
	public static final int M3_OUTLINE_VARIANT    = 0xFF49454F;

	private UiRenderUtil() {
	}

	// ── Core drawing ────────────────────────────────────────────────────────

	public static void drawPanel(DrawContext context, int x, int y, int width, int height, int background, int border) {
		context.fill(x, y, x + width, y + height, background);
		context.drawBorder(x, y, width, height, border);
	}

	/**
	 * Draws a filled rectangle with simulated rounded corners by composing three strip fills.
	 * The corner pixels (radius×radius squares) are left transparent — gives a clean rounded look
	 * without shaders. Radius is clamped to half the smaller dimension.
	 */
	public static void drawRoundedFill(DrawContext context, int x, int y, int width, int height, int color, int radius) {
		if (width <= 0 || height <= 0) {
			return;
		}

		radius = Math.min(radius, Math.min(width / 2, height / 2));
		if (radius <= 0) {
			context.fill(x, y, x + width, y + height, color);
			return;
		}

		for (int row = 0; row < height; row++) {
			int inset = 0;
			double exactInset = 0.0D;
			if (row < radius) {
				exactInset = roundedInsetExact(radius, row);
				inset = (int)Math.ceil(exactInset);
			} else if (row >= height - radius) {
				exactInset = roundedInsetExact(radius, height - 1 - row);
				inset = (int)Math.ceil(exactInset);
			}

			int left = x + inset;
			int right = x + width - inset;
			if (right > left) {
				context.fill(left, y + row, right, y + row + 1, color);
				if (inset > 0) {
					float edgeCoverage = (float)(inset - exactInset);
					int edgeColor = withMultipliedAlpha(color, edgeCoverage * 0.85f);
					if (((edgeColor >>> 24) & 0xFF) > 0) {
						context.fill(left - 1, y + row, left, y + row + 1, edgeColor);
						context.fill(right, y + row, right + 1, y + row + 1, edgeColor);
					}
				}
			}
		}
	}

	/**
	 * Draws a rounded-corner panel (background fill + border lines).
	 * Border is drawn as four edge lines that skip the corner squares.
	 */
	public static void drawRoundedPanel(DrawContext context, int x, int y, int width, int height,
			int background, int border, int radius) {
		if (width <= 0 || height <= 0) {
			return;
		}

		radius = Math.min(radius, Math.min(width / 2, height / 2));
		if (radius <= 0 || width <= 2 || height <= 2) {
			context.fill(x, y, x + width, y + height, background);
			context.drawBorder(x, y, width, height, border);
			return;
		}

		if (((border >>> 24) & 0xFF) == 0) {
			drawRoundedFill(context, x, y, width, height, background, radius);
			return;
		}

		drawRoundedFill(context, x, y, width, height, border, radius);
		drawRoundedFill(context, x + 1, y + 1, width - 2, height - 2, background, Math.max(0, radius - 1));
	}

	private static int roundedInset(int radius, int rowFromEdge) {
		return (int)Math.ceil(roundedInsetExact(radius, rowFromEdge));
	}

	private static double roundedInsetExact(int radius, int rowFromEdge) {
		double dy = radius - rowFromEdge - 0.5D;
		double inside = Math.max(0.0D, radius * radius - dy * dy);
		return radius - Math.sqrt(inside);
	}

	public static void drawAccentStrip(DrawContext context, int x, int y, int width, int color) {
		context.fill(x, y, x + width, y + 2, color);
	}

	/**
	 * Draws a panel whose background fades vertically from colorTop to colorBottom.
	 * Achieved by splitting the panel into two halves — works with Minecraft's
	 * fillGradient which blends between the two ARGB values.
	 */
	public static void drawGradientPanel(DrawContext context, int x, int y, int width, int height,
			int colorTop, int colorBottom, int border) {
		context.fillGradient(x, y, x + width, y + height, colorTop, colorBottom);
		context.drawBorder(x, y, width, height, border);
	}

	/**
	 * Draws a panel with an animated hover glow — blends between the base
	 * background and a highlighted background by hoverProgress [0,1].
	 */
	public static void drawHoverPanel(DrawContext context, int x, int y, int width, int height,
			int baseBackground, int hoverBackground, float hoverProgress, int border) {
		int blended = lerpColor(baseBackground, hoverBackground, hoverProgress);
		context.fill(x, y, x + width, y + height, blended);
		context.drawBorder(x, y, width, height, border);
	}

	// ── Color utilities ──────────────────────────────────────────────────────

	/**
	 * Linearly interpolates between two ARGB colors component-by-component.
	 * @param t blend factor [0,1]: 0 = from, 1 = to
	 */
	public static int lerpColor(int from, int to, float t) {
		t = Math.max(0.0f, Math.min(1.0f, t));
		int aFrom = (from >> 24) & 0xFF;
		int rFrom = (from >> 16) & 0xFF;
		int gFrom = (from >> 8)  & 0xFF;
		int bFrom =  from        & 0xFF;
		int aTo   = (to   >> 24) & 0xFF;
		int rTo   = (to   >> 16) & 0xFF;
		int gTo   = (to   >> 8)  & 0xFF;
		int bTo   =  to          & 0xFF;
		int a = (int) (aFrom + (aTo - aFrom) * t);
		int r = (int) (rFrom + (rTo - rFrom) * t);
		int g = (int) (gFrom + (gTo - gFrom) * t);
		int b = (int) (bFrom + (bTo - bFrom) * t);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	/** Returns the given ARGB color with its alpha channel replaced. */
	public static int withAlpha(int color, int alpha) {
		return (AnimationUtil.clamp(alpha, 0, 255) << 24) | (color & 0x00FFFFFF);
	}

	public static int withMultipliedAlpha(int color, float multiplier) {
		multiplier = Math.max(0.0f, Math.min(1.0f, multiplier));
		int alpha = (int)(((color >>> 24) & 0xFF) * multiplier);
		return withAlpha(color, alpha);
	}

	// ── Geometry ─────────────────────────────────────────────────────────────

	public static boolean isHovered(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}

	// ── Text ─────────────────────────────────────────────────────────────────

	public static void drawText(DrawContext context, TextRenderer renderer, String text, int x, int y, int color) {
		context.drawText(renderer, text, x, y, color, false);
	}

	public static void drawText(DrawContext context, TextRenderer renderer, Text text, int x, int y, int color) {
		context.drawText(renderer, text, x, y, color, false);
	}

	// ── Theme-aware component helpers ────────────────────────────────────────

	/**
	 * Draws a card whose background has an elevation tint blended in.
	 * Simulates M3's tonal elevation by blending {@code primaryColor} into
	 * {@code surface} at the level-specific opacity.
	 *
	 * @param level  elevation 0–5 (0 = flat, 5 = highest)
	 */
	public static void drawElevatedCard(DrawContext context,
			int x, int y, int w, int h,
			int surface, int primaryColor, int borderColor,
			int level, int radius) {
		// Tint alphas per level: 0, 5%, 8%, 11%, 12%, 14%
		int[] tintAlpha = { 0, 13, 20, 28, 31, 36 };
		level = Math.max(0, Math.min(5, level));
		int tintedBg = lerpColor(surface, primaryColor, tintAlpha[level] / 255.0f);
		drawRoundedPanel(context, x, y, w, h, tintedBg, borderColor, radius);
	}

	/**
	 * Draws a state overlay (hover / pressed / focus) on top of an existing surface.
	 * The {@code stateColor} should already carry the correct alpha
	 * (see {@link com.bcrq.legit.client.theme.ThemeTokens#stateHover()}).
	 *
	 * @param progress  animation progress [0,1] — multiplied into the state alpha
	 */
	public static void drawStateLayer(DrawContext context,
			int x, int y, int w, int h,
			int stateColor, float progress, int radius) {
		if (progress < 0.01f) return;
		int alpha = (int)(((stateColor >> 24) & 0xFF) * progress);
		int blended = withAlpha(stateColor, alpha);
		drawRoundedFill(context, x, y, w, h, blended, radius);
	}

	/**
	 * Draws an M3 Navigation Rail item — full-width pill indicator for selected,
	 * transparent for unselected, with optional left accent bar.
	 *
	 * @param pillBg      background of the pill (selectedContainer color)
	 * @param accentBar   small accent bar color on the left (primary color)
	 * @param hoverAlpha  0–255 hover overlay alpha for unselected state
	 * @param selected    whether this item is active
	 */
	public static void drawNavRailItem(DrawContext context,
			int x, int y, int w, int h,
			int pillBg, int accentBar, int hoverBg, float hoverProgress,
			boolean selected, int pillRadius) {
		if (selected) {
			drawRoundedPanel(context, x, y, w, h, pillBg, 0x00000000, pillRadius);
			// Left accent bar — 4px wide, vertically centered
			int barH = h - 12;
			int barY = y + 6;
			drawRoundedFill(context, x + 2, barY, 4, barH, accentBar, 2);
		} else if (hoverProgress > 0.02f) {
			drawRoundedFill(context, x, y, w, h,
					withAlpha(hoverBg, (int)(hoverProgress * 80)), pillRadius);
		}
	}

	/**
	 * Draws an M3-style switch track + thumb.
	 * The track morphs from outline-variant color (off) to primary-container (on).
	 *
	 * @param x, y        top-left of the switch
	 * @param w, h        dimensions (recommended 46×20)
	 * @param progress    toggle animation [0=off, 1=on]
	 * @param trackOff    track bg when off (e.g. secondaryContainer)
	 * @param trackOn     track bg when on (e.g. primaryContainer)
	 * @param thumbOff    thumb color when off
	 * @param thumbOn     thumb color when on (e.g. primary)
	 */
	public static void drawM3Switch(DrawContext context,
			int x, int y, int w, int h,
			float progress,
			int trackOff, int trackOn,
			int thumbOff, int thumbOn) {
		int trackBg = lerpColor(trackOff, trackOn, progress);
		// Full-pill track
		drawRoundedFill(context, x, y, w, h, trackBg, h / 2);
		// Thumb: slides from left to right
		int thumbSize = h - 6;
		int thumbX = x + 3 + (int)((w - 6 - thumbSize) * progress);
		int thumbY = y + 3;
		int thumbColor = lerpColor(thumbOff, thumbOn, progress);
		drawRoundedFill(context, thumbX, thumbY, thumbSize, thumbSize, thumbColor, thumbSize / 2);
	}

	/**
	 * Draws an M3-style tonal button.
	 * Background: secondary-container. Text: on-secondary-container.
	 * Shape: pill (radius = h/2).
	 *
	 * @param hoverProgress  hover animation [0,1]
	 * @param baseBg         idle background (e.g. secondaryContainer)
	 * @param hoverBg        hover background
	 * @param borderColor    outline color
	 */
	public static void drawTonalButton(DrawContext context,
			int x, int y, int w, int h,
			int baseBg, int hoverBg, float hoverProgress,
			int borderColor) {
		int bg = lerpColor(baseBg, hoverBg, hoverProgress);
		drawRoundedPanel(context, x, y, w, h, bg, borderColor, h / 2);
	}

	/**
	 * Draws a panel with a vertical gradient background (top → bottom).
	 * Useful for header sections and hero areas.
	 */
	public static void drawRoundedGradientPanel(DrawContext context,
			int x, int y, int w, int h,
			int colorTop, int colorBottom,
			int borderColor, int radius) {
		if (w <= 0 || h <= 0) {
			return;
		}

		radius = Math.min(radius, Math.min(w / 2, h / 2));
		if (radius <= 0 || w <= 2 || h <= 2) {
			context.fillGradient(x, y, x + w, y + h, colorTop, colorBottom);
			context.drawBorder(x, y, w, h, borderColor);
			return;
		}

		if (((borderColor >>> 24) & 0xFF) != 0) {
			drawRoundedFill(context, x, y, w, h, borderColor, radius);
			drawRoundedGradientFill(context, x + 1, y + 1, w - 2, h - 2, colorTop, colorBottom, Math.max(0, radius - 1));
		} else {
			drawRoundedGradientFill(context, x, y, w, h, colorTop, colorBottom, radius);
		}
	}

	private static void drawRoundedGradientFill(DrawContext context,
			int x, int y, int width, int height,
			int colorTop, int colorBottom, int radius) {
		if (width <= 0 || height <= 0) {
			return;
		}

		radius = Math.min(radius, Math.min(width / 2, height / 2));
		for (int row = 0; row < height; row++) {
			int inset = 0;
			double exactInset = 0.0D;
			if (radius > 0 && row < radius) {
				exactInset = roundedInsetExact(radius, row);
				inset = (int)Math.ceil(exactInset);
			} else if (radius > 0 && row >= height - radius) {
				exactInset = roundedInsetExact(radius, height - 1 - row);
				inset = (int)Math.ceil(exactInset);
			}

			int left = x + inset;
			int right = x + width - inset;
			if (right > left) {
				float t = height <= 1 ? 0.0f : row / (float)(height - 1);
				int rowColor = lerpColor(colorTop, colorBottom, t);
				context.fill(left, y + row, right, y + row + 1, rowColor);
				if (inset > 0) {
					float edgeCoverage = (float)(inset - exactInset);
					int edgeColor = withMultipliedAlpha(rowColor, edgeCoverage * 0.85f);
					if (((edgeColor >>> 24) & 0xFF) > 0) {
						context.fill(left - 1, y + row, left, y + row + 1, edgeColor);
						context.fill(right, y + row, right + 1, y + row + 1, edgeColor);
					}
				}
			}
		}
	}
}
