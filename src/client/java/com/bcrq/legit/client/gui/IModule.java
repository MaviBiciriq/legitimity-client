package com.bcrq.legit.client.gui;

import java.util.List;

/** The small module surface used by the floating ClickGUI. */
public interface IModule {
	String getName();
	String getCategory();
	String getDescription();
	boolean isEnabled();
	void toggle();
	List<ModuleSetting<?>> getSettings();
}
