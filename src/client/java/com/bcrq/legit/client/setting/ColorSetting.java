package com.bcrq.legit.client.setting;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.util.ColorUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class ColorSetting extends Setting<ColorValue> {
	public ColorSetting(Module module, String name, String description, ColorValue defaultValue) {
		super(module, name, description, defaultValue);
	}

	@Override
	public JsonElement toJson() {
		ColorValue value = getValue();
		JsonObject object = new JsonObject();
		object.addProperty("red", value.getRed());
		object.addProperty("green", value.getGreen());
		object.addProperty("blue", value.getBlue());
		object.addProperty("alpha", value.getAlpha());
		object.addProperty("rainbow", value.isRainbow());
		object.addProperty("rainbowSpeed", value.getRainbowSpeed());
		return object;
	}

	@Override
	public void fromJson(JsonElement element) {
		if (element == null || !element.isJsonObject()) {
			return;
		}

		JsonObject object = element.getAsJsonObject();
		ColorValue color = getValue().copy();

		if (object.has("red")) {
			color.setRed(object.get("red").getAsInt());
		}
		if (object.has("green")) {
			color.setGreen(object.get("green").getAsInt());
		}
		if (object.has("blue")) {
			color.setBlue(object.get("blue").getAsInt());
		}
		if (object.has("alpha")) {
			color.setAlpha(object.get("alpha").getAsInt());
		}
		if (object.has("rainbow")) {
			color.setRainbow(object.get("rainbow").getAsBoolean());
		}
		if (object.has("rainbowSpeed")) {
			color.setRainbowSpeed(object.get("rainbowSpeed").getAsDouble());
		}

		setValue(color);
	}

	public int resolveColor() {
		ColorValue value = getValue();
		if (value.isRainbow()) {
			return ColorUtil.rainbow(value.getRainbowSpeed(), 0.0D, value.getAlpha());
		}

		return ColorUtil.argb(value.getAlpha(), value.getRed(), value.getGreen(), value.getBlue());
	}
}
