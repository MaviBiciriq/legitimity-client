package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.BooleanSetting;
import com.bcrq.legit.client.setting.ColorSetting;
import com.bcrq.legit.client.setting.ColorValue;
import com.bcrq.legit.client.setting.EnumSetting;
import com.bcrq.legit.client.setting.NumberSetting;
import com.bcrq.legit.client.util.AnimationUtil;
import com.bcrq.legit.client.util.ColorUtil;
import com.bcrq.legit.client.util.UiRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ArrayList HUD — displays active modules on-screen with premium
 * animations, multiple color modes, and full customization.
 * <p>
 * Features:
 * <ul>
 *   <li>Slide-in / slide-out animations per module</li>
 *   <li>Color modes: Static, Rainbow, Chroma Wave, Category</li>
 *   <li>Left / Right alignment</li>
 *   <li>Background panel with configurable opacity</li>
 *   <li>Accent bar on the side edge</li>
 *   <li>Scale adjustment</li>
 *   <li>Sort by name length (longest on top)</li>
 *   <li>Smooth reordering when modules toggle</li>
 * </ul>
 */
public final class ArrayListModule extends Module {

	// ── Layout ───────────────────────────────────────────────────────────────
	private final EnumSetting<Anchor> anchor = addSetting(
			new EnumSetting<>(this, "Side", "Which side of the screen.", Anchor.RIGHT, Anchor.class));
	private final NumberSetting yOffset = addSetting(
			new NumberSetting(this, "Y Offset", "Vertical offset from top.", 4.0D, 0.0D, 200.0D, 1.0D));
	private final NumberSetting scale = addSetting(
			new NumberSetting(this, "Scale", "Text scale multiplier.", 1.0D, 0.6D, 2.0D, 0.05D));

	// ── Color ────────────────────────────────────────────────────────────────
	private final EnumSetting<ColorMode> colorMode = addSetting(
			new EnumSetting<>(this, "Color Mode", "How module names are colored.", ColorMode.CHROMA, ColorMode.class));
	private final ColorSetting staticColor = addSetting(
			new ColorSetting(this, "Color 1", "Primary color (Static, Fade, Gradient).", new ColorValue(255, 75, 110, 255, false, 0.14D)));
	private final ColorSetting secondColor = addSetting(
			new ColorSetting(this, "Color 2", "Secondary color (Fade, Gradient).", new ColorValue(90, 120, 255, 255, false, 0.14D)));
	private final NumberSetting chromaSpeed = addSetting(
			new NumberSetting(this, "Chroma Speed", "Chroma / Rainbow / Fade speed.", 0.4D, 0.05D, 2.0D, 0.05D));
	private final NumberSetting chromaSep = addSetting(
			new NumberSetting(this, "Chroma Sep", "Hue separation between items (Chroma mode).", 0.06D, 0.01D, 0.20D, 0.005D));

	// ── Visual ───────────────────────────────────────────────────────────────
	private final BooleanSetting background = addSetting(
			new BooleanSetting(this, "Background", "Draw a dark panel behind each entry.", true));
	private final NumberSetting bgAlpha = addSetting(
			new NumberSetting(this, "BG Alpha", "Background panel opacity %.", 55.0D, 10.0D, 100.0D, 1.0D));
	private final BooleanSetting accentBar = addSetting(
			new BooleanSetting(this, "Accent Bar", "Draw a colored bar on the edge.", true));
	private final BooleanSetting textShadow = addSetting(
			new BooleanSetting(this, "Text Shadow", "Draw text with shadow.", true));
	private final BooleanSetting hideHudModules = addSetting(
			new BooleanSetting(this, "Hide HUD Modules", "Don't show HUD-category modules in the list.", true));

	// ── Animation state per module ───────────────────────────────────────────
	private final Map<String, Float> slideProgress = new HashMap<>();

	public ArrayListModule() {
		super("Array List", "Displays active modules with animated list on screen.", ModuleCategory.HUD);
	}

	@Override
	public void onEnable() {
		slideProgress.clear();
	}

	@Override
	public void onHudRender(DrawContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.getWindow() == null) return;

		TextRenderer font = client.textRenderer;
		float s = scale.getValue().floatValue();
		int screenWidth = client.getWindow().getScaledWidth();
		boolean right = anchor.getValue() == Anchor.RIGHT;

		// Collect all modules and update their animation progress
		List<Module> allModules = LegitimityClient.get().getModuleManager().getModules();
		List<ModuleEntry> entries = new ArrayList<>();

		for (Module mod : allModules) {
			if (mod == this) continue;
			if (hideHudModules.getValue() && mod.getCategory() == ModuleCategory.HUD) continue;

			String name = mod.getName();
			float target = mod.isEnabled() ? 1.0f : 0.0f;
			float current = slideProgress.getOrDefault(name, 0.0f);
			current = AnimationUtil.smooth(current, target, 0.18f);

			// Snap to zero when very close (avoid floating point remnants)
			if (!mod.isEnabled() && current < 0.005f) {
				current = 0.0f;
			}
			slideProgress.put(name, current);

			if (current > 0.001f) {
				entries.add(new ModuleEntry(mod, name, current));
			}
		}

		// Sort by text width (longest first = classic array list look)
		entries.sort(Comparator.comparingInt(e -> -font.getWidth(e.name)));

		// Render
		int lineHeight = Math.round((font.fontHeight + 4) * s);
		int padding = Math.round(4 * s);
		int barWidth = Math.round(2 * s);
		int baseY = yOffset.getValue().intValue();

		for (int i = 0; i < entries.size(); i++) {
			ModuleEntry entry = entries.get(i);
			int textWidth = Math.round(font.getWidth(entry.name) * s);
			int totalWidth = textWidth + padding * 2;

			// Slide animation: modules slide in from the edge
			float eased = AnimationUtil.easeOutCubic(entry.slide);
			int slideOffset;
			if (right) {
				slideOffset = Math.round((1.0f - eased) * (totalWidth + barWidth + 4));
			} else {
				slideOffset = Math.round((1.0f - eased) * -(totalWidth + barWidth + 4));
			}

			int x;
			if (right) {
				x = screenWidth - totalWidth - barWidth + slideOffset;
			} else {
				x = barWidth + slideOffset;
			}
			int y = baseY + i * lineHeight;

			// Alpha fade based on slide progress
			int alpha = Math.round(255 * eased);
			if (alpha <= 0) continue;

			// Resolve color for this entry
			int color = resolveEntryColor(entry.module, i, alpha, entries.size());

			// Background panel
			if (background.getValue()) {
				int bgA = Math.round(alpha * (float) (bgAlpha.getValue() / 100.0));
				int bgColor = ColorUtil.argb(bgA, 10, 10, 16);
				context.fill(x, y, x + totalWidth, y + lineHeight, bgColor);
			}

			// Accent bar
			if (accentBar.getValue()) {
				int barX = right ? x + totalWidth : x - barWidth;
				context.fill(barX, y, barX + barWidth, y + lineHeight, color);
			}

			// Text
			// Apply scale using matrix if needed
			if (Math.abs(s - 1.0f) > 0.01f) {
				context.getMatrices().push();
				float textX = x + padding;
				float textY = y + Math.round(2 * s);
				context.getMatrices().translate(textX, textY, 0);
				context.getMatrices().scale(s, s, 1.0f);
				if (textShadow.getValue()) {
					context.drawTextWithShadow(font, entry.name, 0, 0, color);
				} else {
					context.drawText(font, entry.name, 0, 0, color, false);
				}
				context.getMatrices().pop();
			} else {
				int textX = x + padding;
				int textY = y + 2;
				if (textShadow.getValue()) {
					context.drawTextWithShadow(font, entry.name, textX, textY, color);
				} else {
					context.drawText(font, entry.name, textX, textY, color, false);
				}
			}
		}
	}

	private int resolveEntryColor(Module module, int index, int alpha, int totalEntries) {
		return switch (colorMode.getValue()) {
			case STATIC -> ColorUtil.withAlpha(staticColor.resolveColor(), alpha);
			case RAINBOW -> ColorUtil.rainbow(chromaSpeed.getValue(), 0.0D, alpha);
			case CHROMA -> ColorUtil.chromaWave(chromaSpeed.getValue(), index, chromaSep.getValue(), alpha);
			case FADE -> {
				// Pulse between Color 1 and Color 2 with per-index offset
				double time = System.currentTimeMillis() / 1000.0 * chromaSpeed.getValue();
				float t = (float)((Math.sin(time + index * 0.35) + 1.0) * 0.5);
				int blended = UiRenderUtil.lerpColor(staticColor.resolveColor(), secondColor.resolveColor(), t);
				yield ColorUtil.withAlpha(blended, alpha);
			}
			case GRADIENT -> {
				// Top-to-bottom gradient across the entire list
				float t = totalEntries > 1 ? (float) index / (totalEntries - 1) : 0f;
				int blended = UiRenderUtil.lerpColor(staticColor.resolveColor(), secondColor.resolveColor(), t);
				yield ColorUtil.withAlpha(blended, alpha);
			}
			case CATEGORY -> {
				int base = getCategoryColor(module.getCategory());
				yield ColorUtil.withAlpha(base, alpha);
			}
		};
	}

	private int getCategoryColor(ModuleCategory cat) {
		return switch (cat) {
			case COMBAT -> 0xFFFF5566;
			case MOVEMENT -> 0xFF55AAFF;
			case RENDER -> 0xFFAA66FF;
			case HUD -> 0xFF66FFAA;
			case UTILITY -> 0xFFFFCC55;
			case MISC -> 0xFFAAAACC;
		};
	}

	// ── Inner types ──────────────────────────────────────────────────────────

	private record ModuleEntry(Module module, String name, float slide) {}

	public enum Anchor {
		LEFT("Left"), RIGHT("Right");
		private final String label;
		Anchor(String label) { this.label = label; }
		@Override public String toString() { return label; }
	}

	public enum ColorMode {
		STATIC("Static"), RAINBOW("Rainbow"), CHROMA("Chroma"), FADE("Fade"), GRADIENT("Gradient"), CATEGORY("Category");
		private final String label;
		ColorMode(String label) { this.label = label; }
		@Override public String toString() { return label; }
	}
}
