package com.bcrq.legit.client.setting;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.module.Module;
import com.google.gson.JsonElement;

public abstract class Setting<T> {
	private final Module module;
	private final String name;
	private final String description;
	private final T defaultValue;
	private T value;

	protected Setting(Module module, String name, String description, T defaultValue) {
		this.module = module;
		this.name = name;
		this.description = description;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public abstract JsonElement toJson();

	public abstract void fromJson(JsonElement element);

	public Module getModule() {
		return module;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
		LegitimityClient.get().requestSave();
	}
}
