package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;

public final class NoParticlesModule extends Module {
	public NoParticlesModule() {
		super("Particle Silence", "Stops combat and ambient particles from rendering client-side.", ModuleCategory.RENDER);
	}
}
