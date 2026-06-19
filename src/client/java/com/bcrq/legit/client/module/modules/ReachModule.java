package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.NumberSetting;

public final class ReachModule extends Module {

	private final NumberSetting attackReach = addSetting(
			new NumberSetting(this, "Attack Reach", "Entity interaction range.", 3.0D, 3.0D, 6.0D, 0.1D));
	private final NumberSetting blockReach = addSetting(
			new NumberSetting(this, "Block Reach", "Block interaction range.", 4.5D, 4.5D, 7.0D, 0.1D));

	public ReachModule() {
		super("Reach", "Extends attack and block interaction range.", ModuleCategory.COMBAT);
	}

	public double getAttackReach() {
		return attackReach.getValue();
	}

	public double getBlockReach() {
		return blockReach.getValue();
	}
}
