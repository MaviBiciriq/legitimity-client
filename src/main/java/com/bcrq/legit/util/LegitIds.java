package com.bcrq.legit.util;

import com.bcrq.legit.LegitimityMod;
import net.minecraft.util.Identifier;

public final class LegitIds {
	private LegitIds() {
	}

	public static Identifier id(String path) {
		return Identifier.of(LegitimityMod.MOD_ID, path);
	}
}
