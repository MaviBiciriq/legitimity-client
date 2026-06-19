package com.bcrq.legit.client.setting;

import com.bcrq.legit.client.module.Module;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class EnumSetting<T extends Enum<T>> extends Setting<T> {
	private final Class<T> enumClass;

	public EnumSetting(Module module, String name, String description, T defaultValue, Class<T> enumClass) {
		super(module, name, description, defaultValue);
		this.enumClass = enumClass;
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(getValue().name());
	}

	@Override
	public void fromJson(JsonElement element) {
		if (element != null && element.isJsonPrimitive()) {
			try {
				setValue(Enum.valueOf(enumClass, element.getAsString()));
			} catch (IllegalArgumentException ignored) {
			}
		}
	}

	public void cycle() {
		T[] values = enumClass.getEnumConstants();
		int next = (getValue().ordinal() + 1) % values.length;
		setValue(values[next]);
	}
}
