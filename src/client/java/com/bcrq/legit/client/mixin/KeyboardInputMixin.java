package com.bcrq.legit.client.mixin;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.module.modules.FreecamModule;
import com.bcrq.legit.client.module.modules.ScaffoldModule;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.input.Input;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
	@Inject(method = "tick", at = @At("TAIL"))
	private void legitimity$freezePlayerMovement(CallbackInfo ci) {
		FreecamModule freecam = LegitimityClient.get().getModuleManager().getModule(FreecamModule.class);
		if (freecam != null && freecam.isEnabled()) {
			playerInput = new PlayerInput(false, false, false, false, false, false, false);
			movementForward = 0.0f;
			movementSideways = 0.0f;
			return;
		}

		// Scaffold shift mode: force sneak when near block edge
		ScaffoldModule scaffold = LegitimityClient.get().getModuleManager().getModule(ScaffoldModule.class);
		if (scaffold != null && scaffold.wantsSneak() && !playerInput.sneak()) {
			playerInput = new PlayerInput(
					playerInput.forward(),
					playerInput.backward(),
					playerInput.left(),
					playerInput.right(),
					playerInput.jump(),
					true,  // sneak = true
					playerInput.sprint()
			);
		}
	}
}
