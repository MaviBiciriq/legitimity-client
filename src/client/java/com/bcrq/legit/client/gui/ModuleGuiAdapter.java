package com.bcrq.legit.client.gui;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.setting.BooleanSetting;
import com.bcrq.legit.client.setting.NumberSetting;
import com.bcrq.legit.client.setting.Setting;
import java.util.ArrayList;
import java.util.List;

/** Adapts the existing module API without changing its persistence or runtime contracts. */
public final class ModuleGuiAdapter implements IModule {
	private final Module module;

	public ModuleGuiAdapter(Module module) {
		this.module = module;
	}

	@Override public String getName() { return module.getName(); }
	@Override public String getCategory() { return module.getCategory().getLabel(); }
	@Override public String getDescription() { return module.getDescription(); }
	@Override public boolean isEnabled() { return module.isEnabled(); }
	@Override public void toggle() { module.toggle(); }

	@Override
	public List<ModuleSetting<?>> getSettings() {
		List<ModuleSetting<?>> result = new ArrayList<>();
		for (Setting<?> setting : module.getSettings()) {
			if (setting instanceof BooleanSetting booleanSetting) {
				result.add(ModuleSetting.bool(setting.getName(), booleanSetting::getValue, booleanSetting::setValue));
			} else if (setting instanceof NumberSetting numberSetting) {
				result.add(ModuleSetting.slider(setting.getName(), numberSetting::getValue,
					numberSetting.getMin(), numberSetting.getMax(), numberSetting::setValue));
			}
		}
		return result;
	}
}
