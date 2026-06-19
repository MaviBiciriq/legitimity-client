package com.bcrq.legit.client.module;

public enum ModuleCategory {
	COMBAT("Combat"),
	MOVEMENT("Movement"),
	RENDER("Render"),
	HUD("Hud"),
	UTILITY("Utility"),
	MISC("Misc");

	private final String label;

	ModuleCategory(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
