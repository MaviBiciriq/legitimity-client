package com.bcrq.legit.client.gui;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ModuleSetting<T> {
	public enum Type { BOOLEAN, DOUBLE }

	private final String name;
	private final Type type;
	private T value;
	private final double min;
	private final double max;
	private final Supplier<T> valueSource;
	private final Consumer<T> onChange;

	private ModuleSetting(String name, Type type, T value, double min, double max, Supplier<T> valueSource, Consumer<T> onChange) {
		this.name = name;
		this.type = type;
		this.value = value;
		this.min = min;
		this.max = max;
		this.valueSource = valueSource;
		this.onChange = onChange;
	}

	public static ModuleSetting<Boolean> bool(String name, boolean value, Consumer<Boolean> onChange) {
		return new ModuleSetting<>(name, Type.BOOLEAN, value, 0, 1, null, onChange);
	}

	public static ModuleSetting<Boolean> bool(String name, Supplier<Boolean> valueSource, Consumer<Boolean> onChange) {
		return new ModuleSetting<>(name, Type.BOOLEAN, valueSource.get(), 0, 1, valueSource, onChange);
	}

	public static ModuleSetting<Double> slider(String name, double value, double min, double max, Consumer<Double> onChange) {
		return new ModuleSetting<>(name, Type.DOUBLE, value, min, max, null, onChange);
	}

	public static ModuleSetting<Double> slider(String name, Supplier<Double> valueSource, double min, double max, Consumer<Double> onChange) {
		return new ModuleSetting<>(name, Type.DOUBLE, valueSource.get(), min, max, valueSource, onChange);
	}

	public String getName() { return name; }
	public Type getType() { return type; }
	public T getValue() { return valueSource == null ? value : valueSource.get(); }
	public double getMin() { return min; }
	public double getMax() { return max; }

	@SuppressWarnings("unchecked")
	public void setValue(Object value) {
		this.value = (T) value;
		if (onChange != null) onChange.accept(this.value);
	}
}
