package com.bcrq.legit.client.util;

import java.awt.Color;

public final class ColorUtil {
	private ColorUtil() {
	}

	public static int argb(int alpha, int red, int green, int blue) {
		return (AnimationUtil.clamp(alpha, 0, 255) << 24)
			| (AnimationUtil.clamp(red, 0, 255) << 16)
			| (AnimationUtil.clamp(green, 0, 255) << 8)
			| AnimationUtil.clamp(blue, 0, 255);
	}

	public static int withAlpha(int color, int alpha) {
		return (AnimationUtil.clamp(alpha, 0, 255) << 24) | (color & 0x00FFFFFF);
	}

	public static int rainbow(double speed, double offset, int alpha) {
		float hue = (float) (((System.currentTimeMillis() / 1000.0D) * speed + offset) % 1.0D);
		int rgb = Color.HSBtoRGB(hue, 0.86f, 1.0f);
		return withAlpha(rgb, alpha);
	}

	/**
	 * Chroma wave — each index gets a slightly offset hue, creating a
	 * cascading rainbow effect down a list. Premium client staple.
	 *
	 * @param speed      cycles per second
	 * @param index      list position (0, 1, 2, …)
	 * @param separation hue offset between consecutive items (0.03–0.08 works well)
	 * @param alpha      0–255
	 */
	public static int chromaWave(double speed, int index, double separation, int alpha) {
		float hue = (float) (((System.currentTimeMillis() / 1000.0D) * speed + index * separation) % 1.0D);
		int rgb = Color.HSBtoRGB(hue, 0.72f, 1.0f);
		return withAlpha(rgb, alpha);
	}

	/**
	 * Returns a color that smoothly oscillates between two ARGB values.
	 */
	public static int breathe(int colorA, int colorB, double speed) {
		float t = (float) ((Math.sin(System.currentTimeMillis() / 1000.0 * Math.PI * 2.0 * speed) + 1.0) * 0.5);
		return UiRenderUtil.lerpColor(colorA, colorB, t);
	}

	/** Extract red channel 0–255. */
	public static int red(int color) { return (color >> 16) & 0xFF; }
	/** Extract green channel 0–255. */
	public static int green(int color) { return (color >> 8) & 0xFF; }
	/** Extract blue channel 0–255. */
	public static int blue(int color) { return color & 0xFF; }
	/** Extract alpha channel 0–255. */
	public static int alpha(int color) { return (color >> 24) & 0xFF; }
}
