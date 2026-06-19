package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.InputMetrics;
import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.NumberSetting;
import com.bcrq.legit.client.util.AnimationUtil;
import com.bcrq.legit.client.util.UiRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;

public final class MotionHudModule extends Module {
	private final NumberSetting x = addSetting(new NumberSetting(this, "X", "Hud x position.", 14.0D, 0.0D, 600.0D, 1.0D));
	private final NumberSetting y = addSetting(new NumberSetting(this, "Y", "Hud y position.", 44.0D, 0.0D, 400.0D, 1.0D));
	private final NumberSetting scale = addSetting(new NumberSetting(this, "Scale", "Hud size multiplier.", 1.0D, 0.7D, 1.8D, 0.05D));

	// Per-key press animation progress [0.0 = released, 1.0 = pressed]
	private float animW;
	private float animA;
	private float animS;
	private float animD;
	private float animLmb;
	private float animRmb;

	public MotionHudModule() {
		super("Motion HUD", "Displays keystrokes and live CPS in a compact overlay.", ModuleCategory.HUD);
	}

	@Override
	public void onHudRender(DrawContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		int baseX = x.getValue().intValue();
		int baseY = y.getValue().intValue();
		int size = (int) Math.round(26.0D * scale.getValue());
		int gap = 4;

		// Update per-key animation targets this frame
		float speed = 0.30f;
		animW   = AnimationUtil.smooth(animW,   client.options.forwardKey.isPressed() ? 1.0f : 0.0f, speed);
		animA   = AnimationUtil.smooth(animA,   client.options.leftKey.isPressed()    ? 1.0f : 0.0f, speed);
		animS   = AnimationUtil.smooth(animS,   client.options.backKey.isPressed()    ? 1.0f : 0.0f, speed);
		animD   = AnimationUtil.smooth(animD,   client.options.rightKey.isPressed()   ? 1.0f : 0.0f, speed);
		animLmb = AnimationUtil.smooth(animLmb, client.options.attackKey.isPressed()  ? 1.0f : 0.0f, speed);
		animRmb = AnimationUtil.smooth(animRmb, client.options.useKey.isPressed()     ? 1.0f : 0.0f, speed);

		drawKey(context, client.options.forwardKey,  "W",   baseX + size + gap,          baseY,                   size, animW);
		drawKey(context, client.options.leftKey,      "A",   baseX,                       baseY + size + gap,      size, animA);
		drawKey(context, client.options.backKey,      "S",   baseX + size + gap,          baseY + size + gap,      size, animS);
		drawKey(context, client.options.rightKey,     "D",   baseX + (size + gap) * 2,   baseY + size + gap,      size, animD);

		int mouseWidth = size + (size / 2);
		drawMouseKey(context, client.options.attackKey, "LMB " + InputMetrics.getLeftCps(),
				baseX,                   baseY + (size + gap) * 2 + gap, mouseWidth, size - 2, animLmb);
		drawMouseKey(context, client.options.useKey,    "RMB " + InputMetrics.getRightCps(),
				baseX + mouseWidth + gap, baseY + (size + gap) * 2 + gap, mouseWidth, size - 2, animRmb);
	}

	private void drawKey(DrawContext context, KeyBinding keyBinding, String label, int x, int y, int size, float anim) {
		// Click-in nudge: key shifts down 1px when fully pressed, spring back on release
		int nudge = Math.round(AnimationUtil.easeOutCubic(anim));
		int drawY = y + nudge;

		// Interpolate background and border using animation progress
		int background = UiRenderUtil.lerpColor(UiRenderUtil.PANEL, UiRenderUtil.ACCENT_SOFT, anim);
		int border     = UiRenderUtil.lerpColor(UiRenderUtil.OUTLINE, UiRenderUtil.ACCENT, anim);

		UiRenderUtil.drawPanel(context, x, drawY, size, size, background, border);
		context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, label,
				x + (size / 2), drawY + ((size - 8) / 2), UiRenderUtil.TEXT);
	}

	private void drawMouseKey(DrawContext context, KeyBinding keyBinding, String label, int x, int y, int width, int height, float anim) {
		int nudge = Math.round(AnimationUtil.easeOutCubic(anim));
		int drawY = y + nudge;

		int background = UiRenderUtil.lerpColor(UiRenderUtil.PANEL_ALT, UiRenderUtil.ACCENT_SOFT, anim);
		int border     = UiRenderUtil.lerpColor(UiRenderUtil.OUTLINE, UiRenderUtil.ACCENT, anim);

		UiRenderUtil.drawPanel(context, x, drawY, width, height, background, border);
		context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, label,
				x + (width / 2), drawY + ((height - 8) / 2), UiRenderUtil.TEXT);
	}
}
