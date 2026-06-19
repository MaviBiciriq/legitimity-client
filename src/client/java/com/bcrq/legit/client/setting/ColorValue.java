package com.bcrq.legit.client.setting;

public final class ColorValue {
	private int red;
	private int green;
	private int blue;
	private int alpha;
	private boolean rainbow;
	private double rainbowSpeed;

	public ColorValue(int red, int green, int blue, int alpha) {
		this(red, green, blue, alpha, false, 0.18D);
	}

	public ColorValue(int red, int green, int blue, int alpha, boolean rainbow, double rainbowSpeed) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
		this.rainbow = rainbow;
		this.rainbowSpeed = rainbowSpeed;
	}

	public ColorValue copy() {
		return new ColorValue(red, green, blue, alpha, rainbow, rainbowSpeed);
	}

	public int getRed() {
		return red;
	}

	public void setRed(int red) {
		this.red = red;
	}

	public int getGreen() {
		return green;
	}

	public void setGreen(int green) {
		this.green = green;
	}

	public int getBlue() {
		return blue;
	}

	public void setBlue(int blue) {
		this.blue = blue;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public boolean isRainbow() {
		return rainbow;
	}

	public void setRainbow(boolean rainbow) {
		this.rainbow = rainbow;
	}

	public double getRainbowSpeed() {
		return rainbowSpeed;
	}

	public void setRainbowSpeed(double rainbowSpeed) {
		this.rainbowSpeed = rainbowSpeed;
	}
}
