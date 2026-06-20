package com.bcrq.legit.client.gui;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.config.HomeNameConfig;
import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.module.ModuleManager;
import com.bcrq.legit.client.module.modules.EspModule;
import com.bcrq.legit.client.setting.BooleanSetting;
import com.bcrq.legit.client.setting.ColorSetting;
import com.bcrq.legit.client.setting.ColorValue;
import com.bcrq.legit.client.setting.EnumSetting;
import com.bcrq.legit.client.setting.NumberSetting;
import com.bcrq.legit.client.setting.Setting;
import com.bcrq.legit.client.setting.StringSetting;
import com.bcrq.legit.client.theme.GuiTheme;
import com.bcrq.legit.client.theme.ShapeScale;
import com.bcrq.legit.client.theme.ThemeTokens;
import com.bcrq.legit.client.util.AnimationUtil;
import com.bcrq.legit.client.util.UiRenderUtil;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class LegitClickGuiScreen extends Screen {
	private static final Map<ModuleCategory, Double> SAVED_SCROLL = new EnumMap<>(ModuleCategory.class);
	private static final int CATEGORY_ROW_HEIGHT = 32;
	private static final int CATEGORY_ROW_GAP = 6;
	private static final int CATEGORY_TOP_PADDING = 14;
	private static final int HOME_PANEL_WIDTH = 176;
	private static final int HOME_ROW_HEIGHT = 24;
	private static final int HOME_ROW_VISUAL_HEIGHT = 18;
	private static final int HOME_PANEL_TOP_GAP = 8;
	private static ModuleCategory savedCategory = ModuleCategory.RENDER;
	private static String savedModuleName;
	private static ModuleLayout savedModuleLayout = ModuleLayout.SINGLE;
	private static EspSettingsTab savedEspSettingsTab = EspSettingsTab.VISUALS;
	// Theme is managed globally via GuiTheme.activeTheme() / GuiTheme.cycle()
	private static final int RGB_CLICK_TARGET = 4;
	private static final long RGB_CLICK_WINDOW_MS = 1400L;
	private static final long RGB_MODE_DURATION_MS = 5000L;

	private final ModuleManager moduleManager = LegitimityClient.get().getModuleManager();

	private ModuleCategory selectedCategory = savedCategory;
	private Module selectedModule;
	private Module bindingTarget;
	private NumberSetting draggingNumber;
	private ColorSetting draggingColor;
	private int draggingColorChannel = -1;
	private StringSetting editingString;
	private double moduleScroll;
	private double settingsScroll;
	private boolean homePanelOpen;
	private int editingHomeSlot = -1;
	private String homeLabelDraft = "";
	private float openProgress;
	private int titleClickCount;
	private long firstTitleClickAt;
	private long rgbModeUntil;
	private String moduleSearch = "";
	private boolean editingModuleSearch;

	// ── Animation state ────────────────────────────────────────────────────
	/** Hover animation progress for the theme-switcher button [0=idle, 1=hovered]. */
	private float themeButtonHoverAnim = 0f;
	/** Ripple flash alpha after theme switch: decays 1→0 quickly. */
	private float themeRippleAnim = 0f;
	/** Per-category hover animation progress [0=idle, 1=hovered]. */
	private final EnumMap<ModuleCategory, Float> categoryHoverAnim = new EnumMap<>(ModuleCategory.class);
	/** Per-module card hover animation progress [0=idle, 1=hovered], keyed by module name. */
	private final java.util.HashMap<String, Float> moduleHoverAnim = new java.util.HashMap<>();
	/** Per-setting toggle animation progress, keyed by setting name. */
	private final java.util.HashMap<String, Float> settingToggleAnim = new java.util.HashMap<>();
	/** Settings panel slide-in progress [0=just switched module, 1=fully settled]. */
	private float settingsSlideAnim = 1.0f;
	private float homePanelAnim;
	/** Tracks previous selectedModule to detect changes. */
	private Module prevSelectedModule;

	public LegitClickGuiScreen() {
		super(Text.literal("Legitimity"));
	}

	private Layout layout() {
		int shortestSide = Math.min(width, height);
		int margin = clampInt(shortestSide / 18, 10, 28);
		int rootWidth = Math.min(clampInt(width - margin * 2, 760, 1288), Math.max(320, width - 10));
		int rootHeight = Math.min(clampInt(height - margin * 2, 420, 700), Math.max(260, height - 10));

		// M3 Expressive uses easeOutElastic (spring physics bounce); Legit uses easeOutBack
		float easedOpen = GuiTheme.isM3()
				? AnimationUtil.easeOutElastic(openProgress)
				: AnimationUtil.easeOutBack(openProgress);
		int rootX = (int) ((width - rootWidth) / 2.0f + (1.0f - easedOpen) * 26.0f);
		int rootY = (height - rootHeight) / 2;
		int gap = clampInt(rootWidth / 48, 8, 16);
		int contentWidth = Math.max(300, rootWidth - 32);
		int leftWidth = clampInt(contentWidth / 5, 104, 156);
		int rightWidth = clampInt(contentWidth / 4, 170, 292);
		int minCenterWidth = 190;
		int centerWidth = contentWidth - leftWidth - rightWidth - gap * 2;
		if (centerWidth < minCenterWidth) {
			int deficit = minCenterWidth - centerWidth;
			int rightShrink = Math.min(deficit, Math.max(0, rightWidth - 148));
			rightWidth -= rightShrink;
			deficit -= rightShrink;
			int leftShrink = Math.min(deficit, Math.max(0, leftWidth - 92));
			leftWidth -= leftShrink;
			deficit -= leftShrink;
			if (deficit > 0) {
				minCenterWidth = Math.max(132, minCenterWidth - deficit);
			}
			centerWidth = Math.max(minCenterWidth, contentWidth - leftWidth - rightWidth - gap * 2);
		}

		int contentY = rootY + 54;
		int contentHeight = Math.max(140, rootHeight - 110);
		int leftX = rootX + 16;
		int centerX = leftX + leftWidth + gap;
		int rightX = centerX + centerWidth + gap;
		rightWidth = Math.max(140, rootX + rootWidth - rightX - 16);
		int moduleColumns = resolveModuleColumns(centerWidth);
		int moduleCardWidth = Math.max(120, (centerWidth - 20 - (moduleColumns - 1) * 8) / moduleColumns);
		return new Layout(rootX, rootY, rootWidth, rootHeight, leftX, centerX, rightX, contentY, contentHeight, leftWidth, centerWidth, rightWidth, moduleColumns, moduleCardWidth);
	}

	private int clampInt(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private int resolveModuleColumns(int centerWidth) {
		return switch (savedModuleLayout) {
			case SINGLE -> 1;
			case DOUBLE -> Math.max(1, Math.min(2, (centerWidth - 20) / 138));
			case TRIPLE -> Math.max(1, Math.min(3, (centerWidth - 20) / 132));
		};
	}

	private TopBarLayout topBar(Layout layout) {
		int buttonHeight = 22;
		int homeWidth = 84;
		int styleWidth = 30;
		int gap = 6;
		int y = layout.rootY() + 12;
		int homeX = layout.rootX() + layout.rootWidth() - homeWidth - 18;
		int styleX = homeX - styleWidth - gap;
		int searchRight = styleX - gap;
		int searchLeftLimit = layout.rootX() + 210;
		int searchWidth = clampInt(searchRight - searchLeftLimit, 0, 240);
		int searchX = searchRight - searchWidth;
		return new TopBarLayout(searchX, styleX, homeX, y, searchWidth, styleWidth, homeWidth, buttonHeight);
	}

	@Override
	protected void init() {
		// Reset open animation every time the screen opens
		openProgress = 0.0f;
		selectedCategory = savedCategory;
		moduleScroll = SAVED_SCROLL.getOrDefault(selectedCategory, 0.0D);

		List<Module> modules = moduleManager.getModulesInCategory(selectedCategory);
		if (savedModuleName != null) {
			for (Module module : modules) {
				if (module.getName().equals(savedModuleName)) {
					selectedModule = module;
					break;
				}
			}
		}
		if (selectedModule == null || !modules.contains(selectedModule)) {
			selectedModule = modules.isEmpty() ? null : modules.getFirst();
		}
		syncSelectedModule();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	protected void applyBlur() {
		// Keep the world sharp behind the click GUI instead of using vanilla menu blur.
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		openProgress = AnimationUtil.smooth(openProgress, 1.0f, 0.18f);
		if (selectedModule != prevSelectedModule) {
			settingsSlideAnim = 0.0f;
			prevSelectedModule = selectedModule;
		}
		settingsSlideAnim = AnimationUtil.smooth(settingsSlideAnim, 1.0f, 0.20f);
		homePanelAnim = AnimationUtil.smooth(homePanelAnim, homePanelOpen ? 1.0f : 0.0f, 0.24f);
		themeRippleAnim = AnimationUtil.smooth(themeRippleAnim, 0f, 0.14f);

		int overlayAlpha = (int)(120 * openProgress);
		context.fill(0, 0, width, height, overlayAlpha << 24);
		if (themeRippleAnim > 0.01f) {
			context.fill(0, 0, width, height, ((int)(themeRippleAnim * 70)) << 24 | 0x00D0BCFF);
		}

		Layout layout = layout();
		int accent     = accentColor();
		int accentSoft = accentSoftColor();

		// ─ Root panel ─────────────────────────────────────────────────────────────────
		if (GuiTheme.isM3()) {
			renderM3ERoot(context, layout, accent);
		} else {
			UiRenderUtil.drawPanel(context, layout.rootX(), layout.rootY(),
					layout.rootWidth(), layout.rootHeight(), UiRenderUtil.PANEL_SOFT, UiRenderUtil.OUTLINE);
			UiRenderUtil.drawAccentStrip(context, layout.rootX(), layout.rootY(), layout.rootWidth(), accent);
			context.drawTextWithShadow(textRenderer, "LEGITIMITY",
					layout.rootX() + 18, layout.rootY() + 14, isRgbModeActive() ? accent : UiRenderUtil.TEXT);
			context.drawText(textRenderer, "client matrix",
					layout.rootX() + 19, layout.rootY() + 28, UiRenderUtil.MUTED, false);
		}

		renderCategories(context, layout.leftX(), layout.contentY(), layout.leftWidth(), layout.contentHeight(), mouseX, mouseY, accent, accentSoft);
		renderModules(context, layout.centerX(), layout.contentY(), layout.centerWidth(), layout.contentHeight(), mouseX, mouseY, accent, accentSoft);
		renderSettings(context, layout.rightX(), layout.contentY(), layout.rightWidth(), layout.contentHeight(), mouseX, mouseY, accent, accentSoft);
		renderHomePanel(context, layout, mouseX, mouseY, accent, accentSoft);
		renderThemeButton(context, layout, mouseX, mouseY, accent, accentSoft);

		super.render(context, mouseX, mouseY, delta);
	}

	private void renderM3ERoot(DrawContext context, Layout layout, int accent) {
		ThemeTokens t = GuiTheme.current();
		int rootBg = elevationSurface(t.surface(), 2);
		int headerTop = UiRenderUtil.withAlpha(t.primary(), 72);
		int headerBottom = UiRenderUtil.withAlpha(t.secondary(), 18);

		UiRenderUtil.drawRoundedPanel(context, layout.rootX(), layout.rootY(),
				layout.rootWidth(), layout.rootHeight(), rootBg, t.outlineVariant(), t.radiusExtraLarge());
		UiRenderUtil.drawRoundedGradientPanel(context, layout.rootX() + 1, layout.rootY() + 1,
				layout.rootWidth() - 2, 58, headerTop, headerBottom, 0x00000000, t.radiusExtraLarge());
		context.fill(layout.rootX() + 18, layout.rootY() + 50,
				layout.rootX() + layout.rootWidth() - 18, layout.rootY() + 51,
				UiRenderUtil.withAlpha(t.outlineVariant(), 150));

		UiRenderUtil.drawRoundedFill(context, layout.rootX() + 18, layout.rootY() + 14, 6, 24,
				isRgbModeActive() ? accent : t.secondary(), ShapeScale.FULL);
		context.drawTextWithShadow(textRenderer, "LEGITIMITY",
				layout.rootX() + 32, layout.rootY() + 12, isRgbModeActive() ? accent : t.primary());
		context.drawText(textRenderer, "client matrix",
				layout.rootX() + 32, layout.rootY() + 28, t.onSurfaceVariant(), false);

	}


	private void renderCategories(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, int accent, int accentSoft) {
		ThemeTokens t = GuiTheme.current();
		int r = t.radiusMedium();

		// Panel background
		if (r > 0) {
			UiRenderUtil.drawRoundedPanel(context, x, y, width, height,
					elevationSurface(t.surfaceContainer(), 1), t.outlineVariant(), t.radiusLarge());
		} else {
			UiRenderUtil.drawPanel(context, x, y, width, height, t.surfaceContainer(), t.outline());
		}

		int rowH = CATEGORY_ROW_HEIGHT;
		int rowY = y + CATEGORY_TOP_PADDING;

		for (ModuleCategory category : ModuleCategory.values()) {
			boolean selected = category == selectedCategory;
			boolean hovered  = UiRenderUtil.isHovered(mouseX, mouseY, x + 8, rowY, width - 16, rowH);

			float targetHov = (hovered && !selected) ? 1.0f : 0.0f;
			float catHov = categoryHoverAnim.getOrDefault(category, 0.0f);
			catHov = AnimationUtil.smooth(catHov, targetHov, t.speedFast());
			categoryHoverAnim.put(category, catHov);

			if (r > 0) {
				UiRenderUtil.drawNavRailItem(context, x + 6, rowY, width - 12, rowH,
						selected ? t.secondaryContainer() : t.surfaceContainerHigh(),
						selected ? t.secondary() : t.primary(),
						t.primary(),
						catHov,
						selected,
						ShapeScale.FULL);
				if (!selected) {
					UiRenderUtil.drawStateLayer(context, x + 6, rowY, width - 12, rowH,
							t.stateHover(), catHov, ShapeScale.FULL);
				}
				int textColor = selected ? t.onSecondaryContainer()
						: UiRenderUtil.lerpColor(t.onSurfaceVariant(), t.onSurface(), catHov);
				context.drawText(textRenderer, categoryLabel(category), x + 18, rowY + 11, textColor, false);
			} else {
				// Legit style
				int bg = selected ? accentSoft
						: UiRenderUtil.lerpColor(0x40161A20, 0x70242A36, catHov);
				int border = selected ? accent
						: UiRenderUtil.lerpColor(t.outline(), (accent & 0x00FFFFFF) | 0x70000000, catHov);
				int textColor = selected ? t.onSurface()
						: UiRenderUtil.lerpColor(t.onSurfaceVariant(), t.onSurface(), catHov);
				UiRenderUtil.drawPanel(context, x + 10, rowY, width - 20, rowH, bg, border);
				context.drawText(textRenderer, categoryLabel(category), x + 20, rowY + 11, textColor, false);
			}
			rowY += rowH + CATEGORY_ROW_GAP;
		}
	}

	private void renderHomePanel(DrawContext context, Layout layout, int mouseX, int mouseY, int accent, int accentSoft) {
		TopBarLayout topBar = topBar(layout);
		if (topBar.searchWidth() >= 90) {
			boolean searchHovered = UiRenderUtil.isHovered(mouseX, mouseY, topBar.searchX(), topBar.y(), topBar.searchWidth(), topBar.buttonHeight());
			drawButton(context, topBar.searchX(), topBar.y(), topBar.searchWidth(), topBar.buttonHeight(),
					searchHovered || editingModuleSearch ? panelSoft() : panelAlt(),
					searchHovered || editingModuleSearch ? accent : outlineBg());
			String searchText = moduleSearch.isBlank() ? "Search modules..." : moduleSearch;
			int searchColor = moduleSearch.isBlank() && !editingModuleSearch ? mutedCol() : textCol();
			if (editingModuleSearch) searchText += "_";
			context.drawText(textRenderer, ellipsize(searchText, topBar.searchWidth() - 22), topBar.searchX() + 10, topBar.y() + 7, searchColor, false);
		}

		boolean styleHovered = UiRenderUtil.isHovered(mouseX, mouseY, topBar.styleX(), topBar.y(), topBar.styleWidth(), topBar.buttonHeight());
		drawButton(context, topBar.styleX(), topBar.y(), topBar.styleWidth(), topBar.buttonHeight(), styleHovered ? accentSoft : panelAlt(), styleHovered ? accent : outlineBg());
		context.drawCenteredTextWithShadow(textRenderer, moduleLayoutLabel(layout.centerWidth()), topBar.styleX() + topBar.styleWidth() / 2, topBar.y() + 7, textCol());


		boolean hovered = UiRenderUtil.isHovered(mouseX, mouseY, topBar.homeX(), topBar.y(), topBar.homeWidth(), topBar.buttonHeight());
		drawButton(context, topBar.homeX(), topBar.y(), topBar.homeWidth(), topBar.buttonHeight(), hovered || homePanelOpen ? accentSoft : panelAlt(), hovered || homePanelOpen ? accent : outlineBg());
		context.drawCenteredTextWithShadow(textRenderer, "Homes", topBar.homeX() + topBar.homeWidth() / 2, topBar.y() + 7, textCol());
	}

	private void renderModules(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, int accent, int accentSoft) {
		if (GuiTheme.isM3()) {
			ThemeTokens t = GuiTheme.current();
			UiRenderUtil.drawRoundedPanel(context, x, y, width, height,
					elevationSurface(t.surfaceContainer(), 2), t.outlineVariant(), t.radiusLarge());
			context.drawTextWithShadow(textRenderer, categoryLabel(selectedCategory), x + 14, y + 12, t.secondary());
		} else {
			UiRenderUtil.drawPanel(context, x, y, width, height, UiRenderUtil.PANEL, UiRenderUtil.OUTLINE);
			context.drawTextWithShadow(textRenderer, categoryLabel(selectedCategory), x + 14, y + 12, UiRenderUtil.TEXT);
		}

		Layout layout = layout();
		List<Module> modules = getVisibleModulesInCategory(selectedCategory);
		int rowHeight = 56;
		int listTop = y + 38;
		int visibleHeight = height - 52;
		int columns = layout.moduleColumns();
		int cardWidth = layout.moduleCardWidth();
		int stride = rowHeight + 8;
		int rows = (int) Math.ceil(modules.size() / (double) columns);
		int maxScroll = Math.max(0, rows * stride - visibleHeight);
		moduleScroll = AnimationUtil.clamp(moduleScroll, 0.0D, maxScroll);
		SAVED_SCROLL.put(selectedCategory, moduleScroll);

		if (modules.isEmpty()) {
			context.drawCenteredTextWithShadow(textRenderer, "No matching modules", x + width / 2, y + height / 2 - 6, mutedCol());
			return;
		}

		context.enableScissor(x + 6, listTop, x + width - 6, y + height - 6);
		for (int i = 0; i < modules.size(); i++) {
			Module module = modules.get(i);
			int column = i % columns;
			int row = i / columns;
			int rowX = x + 10 + column * (cardWidth + 8);
			int rowY = listTop + row * stride - (int) moduleScroll;
			if (rowY + rowHeight < listTop || rowY > y + height - 10) continue;

			boolean hovered = UiRenderUtil.isHovered(mouseX, mouseY, rowX, rowY, cardWidth, rowHeight);
			boolean selected = module == selectedModule;

			float targetModHov = (hovered && !selected) ? 1.0f : 0.0f;
			float modHov = moduleHoverAnim.getOrDefault(module.getName(), 0.0f);
			modHov = AnimationUtil.smooth(modHov, targetModHov, 0.22f);
			moduleHoverAnim.put(module.getName(), modHov);

			int background, border;
			int nameColor, descColor;
			if (GuiTheme.isM3()) {
				ThemeTokens t = GuiTheme.current();
				if (selected) {
					float pulse = AnimationUtil.pulse(0.85f, 0.55f, 1.0f);
					background = t.primaryContainer();
					border = UiRenderUtil.withAlpha(t.primary(), (int)(pulse * 255));
					nameColor = t.onPrimaryContainer();
					descColor = UiRenderUtil.lerpColor(t.secondary(), t.onPrimaryContainer(), 0.5f);
				} else {
					background = UiRenderUtil.lerpColor(elevationSurface(t.surfaceContainerHigh(), 1),
							elevationSurface(t.surfaceBright(), 3), modHov);
					border = UiRenderUtil.lerpColor(t.outlineVariant(), UiRenderUtil.withAlpha(t.primary(), 140), modHov);
					nameColor = t.onSurface();
					descColor = t.onSurfaceVariant();
				}
				UiRenderUtil.drawRoundedPanel(context, rowX, rowY, cardWidth, rowHeight, background, border, t.radiusMedium());
				if (!selected) {
					UiRenderUtil.drawStateLayer(context, rowX, rowY, cardWidth, rowHeight, t.stateHover(), modHov, t.radiusMedium());
				}
			} else {
				if (selected) {
					float pulse = AnimationUtil.pulse(0.9f, 0.55f, 1.0f);
					background = 0x70212933;
					border = UiRenderUtil.withAlpha(accent, (int)(pulse * 255));
				} else {
					background = UiRenderUtil.lerpColor(0x40171B21, 0x5A212833, modHov);
					border = UiRenderUtil.lerpColor(UiRenderUtil.OUTLINE, (accent & 0x00FFFFFF) | 0x60000000, modHov);
				}
				nameColor = UiRenderUtil.TEXT;
				descColor = selected ? UiRenderUtil.lerpColor(UiRenderUtil.MUTED, UiRenderUtil.TEXT, 0.35f) : UiRenderUtil.MUTED;
				UiRenderUtil.drawPanel(context, rowX, rowY, cardWidth, rowHeight, background, border);
			}
			context.drawTextWithShadow(textRenderer, ellipsize(module.getName(), Math.max(48, cardWidth - 74)), rowX + 10, rowY + 11, nameColor);
			context.drawText(textRenderer, ellipsize(module.getDescription(), Math.max(40, cardWidth - 74)), rowX + 10, rowY + 27, descColor, false);


			int toggleWidth = 46;
			int toggleX = rowX + cardWidth - toggleWidth - 10;
			int toggleY = rowY + 15;
			float easedToggle = AnimationUtil.easeInOutQuad(module.getToggleAnimation());
			int knobOffset = (int)(22 * easedToggle);
			if (GuiTheme.isM3()) {
				ThemeTokens t = GuiTheme.current();
				UiRenderUtil.drawM3Switch(context, toggleX, toggleY, toggleWidth, 20, easedToggle,
						t.secondaryContainer(), t.primaryContainer(), t.onSurfaceVariant(), t.primary());
			} else {
				UiRenderUtil.drawPanel(context, toggleX, toggleY, toggleWidth, 20,
						module.isEnabled() ? accentSoft : 0x7012161C,
						module.isEnabled() ? accent : outlineBg());
				context.fill(toggleX + 3 + knobOffset, toggleY + 3, toggleX + 21 + knobOffset, toggleY + 17, module.isEnabled() ? accent : 0xFF737A86);
			}
		}
		context.disableScissor();
	}

	private void renderSettings(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, int accent, int accentSoft) {
		drawPanel(context, x, y, width, height, GuiTheme.isM3() ? elevationSurface(panelBg(), 2) : panelBg(), outlineBg());
		if (homePanelOpen || homePanelAnim > 0.02f) {
			renderHomesSheet(context, x, y, width, height, mouseX, mouseY, accent, accentSoft);
			return;
		}
		if (selectedModule == null) {
			context.drawCenteredTextWithShadow(textRenderer, "Select a module", x + width / 2, y + height / 2 - 6, mutedCol());
			return;
		}
		context.drawTextWithShadow(textRenderer, selectedModule.getName(), x + 14, y + 12, GuiTheme.isM3() ? GuiTheme.current().tertiary() : textCol());
		int descriptionWidth = Math.max(90, width - 28);
		context.drawWrappedText(textRenderer, Text.literal(selectedModule.getDescription()), x + 14, y + 27, descriptionWidth, mutedCol(), false);
		int descriptionHeight = Math.max(10, textRenderer.getWrappedLinesHeight(selectedModule.getDescription(), descriptionWidth));

		int contentTop = y + 31 + descriptionHeight + 12;
		if (isEspModuleSelected()) {
			renderEspTabs(context, x + 14, contentTop, width - 28, mouseX, mouseY, accent, accentSoft);
			contentTop += 34;
		}

		List<Setting<?>> visibleSettings = getVisibleSettings();
		int totalContentHeight = calculateSettingsHeight(visibleSettings);
		int visibleHeight = height - (contentTop - y) - 12;
		int maxScroll = Math.max(0, totalContentHeight - visibleHeight);
		settingsScroll = AnimationUtil.clamp(settingsScroll, 0.0D, maxScroll);
		int slideOffset = (int)((1.0f - AnimationUtil.easeOutCubic(settingsSlideAnim)) * 18);
		int cursorY = contentTop - (int)settingsScroll + slideOffset;

		context.enableScissor(x + 6, contentTop, x + width - 6, y + height - 6);
		for (Setting<?> setting : visibleSettings) {
			if (setting instanceof BooleanSetting booleanSetting) {
				if (cursorY + 28 >= contentTop && cursorY <= y + height - 6)
					drawBooleanSetting(context, booleanSetting, x + 14, cursorY, width - 28, accent, accentSoft);
				cursorY += getSettingHeight(setting);
			} else if (setting instanceof NumberSetting numberSetting) {
				if (cursorY + 36 >= contentTop && cursorY <= y + height - 6)
					drawNumberSetting(context, numberSetting, x + 14, cursorY, width - 28, accent);
				cursorY += getSettingHeight(setting);
			} else if (setting instanceof StringSetting stringSetting) {
				if (cursorY + 34 >= contentTop && cursorY <= y + height - 6)
					drawStringSetting(context, stringSetting, x + 14, cursorY, width - 28, accent);
				cursorY += getSettingHeight(setting);
			} else if (setting instanceof ColorSetting colorSetting) {
				if (cursorY + 100 >= contentTop && cursorY <= y + height - 6)
					drawColorSetting(context, colorSetting, x + 14, cursorY, width - 28, accent, accentSoft);
				cursorY += getSettingHeight(setting);
			} else if (setting instanceof EnumSetting<?> enumSetting) {
				if (cursorY + 28 >= contentTop && cursorY <= y + height - 6)
					drawEnumSetting(context, enumSetting, x + 14, cursorY, width - 28, accent);
				cursorY += getSettingHeight(setting);
			}
		}
		if (cursorY + 36 >= contentTop && cursorY <= y + height - 6)
			drawBindRow(context, x + 14, cursorY + 4, width - 28, accent);
		context.disableScissor();
	}

	private void renderHomesSheet(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, int accent, int accentSoft) {
		ThemeTokens t = GuiTheme.current();
		float sheetAnim = AnimationUtil.easeOutCubic(homePanelAnim);
		int slide = (int)((1.0f - sheetAnim) * 14.0f);
		int titleY = y + 12 + slide;

		context.drawTextWithShadow(textRenderer, "Homes", x + 14, titleY, GuiTheme.isM3() ? t.tertiary() : textCol());
		context.drawText(textRenderer, "Quick teleport slots", x + 14, titleY + 15, mutedCol(), false);

		int rowX = x + 14;
		int rowWidth = width - 28;
		int rowY = y + 48 + slide;
		int rowHeight = 28;
		int visibleBottom = y + height - 12;
		context.enableScissor(x + 6, y + 6, x + width - 6, y + height - 6);
		for (int i = 1; i <= HomeNameConfig.size(); i++) {
			if (rowY + rowHeight > y + 6 && rowY < visibleBottom) {
				int editButtonSize = 20;
				int editButtonX = rowX + rowWidth - editButtonSize - 4;
				boolean rowHovered = UiRenderUtil.isHovered(mouseX, mouseY, rowX, rowY, rowWidth, rowHeight);
				boolean editHovered = UiRenderUtil.isHovered(mouseX, mouseY, editButtonX, rowY + 4, editButtonSize, editButtonSize);
				int bg = rowHovered ? accentSoft : elevationSurface(panelAlt(), 1);
				int border = rowHovered ? accent : outlineBg();
				drawPanel(context, rowX, rowY, rowWidth, rowHeight, bg, border);
				drawButton(context, editButtonX, rowY + 4, editButtonSize, editButtonSize,
						editHovered || editingHomeSlot == i ? accentSoft : elevationSurface(panelBg(), 1),
						editHovered || editingHomeSlot == i ? accent : outlineBg());
				context.drawCenteredTextWithShadow(textRenderer, "\u270e", editButtonX + editButtonSize / 2, rowY + 10, textCol());
				String label = editingHomeSlot == i ? homeLabelDraft + "_" : HomeNameConfig.getLabel(i);
				label = ellipsize(label, rowWidth - editButtonSize - 28);
				context.drawText(textRenderer, label, rowX + 10, rowY + 10, editingHomeSlot == i ? accent : textCol(), false);
			}
			rowY += rowHeight + 8;
		}
		context.disableScissor();
	}

	private void drawBooleanSetting(DrawContext context, BooleanSetting setting, int x, int y, int width, int accent, int accentSoft) {
		drawPanel(context, x, y, width, 28, GuiTheme.isM3() ? elevationSurface(panelAlt(), 1) : panelAlt(), outlineBg());
		context.drawText(textRenderer, setting.getName(), x + 10, y + 10, textCol(), false);
		int toggleX = x + width - 54;
		float target = setting.getValue() ? 1.0f : 0.0f;
		String key = setting.getModule().getName() + "." + setting.getName();
		float anim = settingToggleAnim.getOrDefault(key, target);
		anim = AnimationUtil.smooth(anim, target, 0.18f);
		settingToggleAnim.put(key, anim);
		float eased = AnimationUtil.easeInOutQuad(anim);
		int knobOffset = (int)(20 * eased);
		if (GuiTheme.isM3()) {
			ThemeTokens t = GuiTheme.current();
			UiRenderUtil.drawM3Switch(context, toggleX, y + 5, 40, 18, eased,
					t.secondaryContainer(), t.primaryContainer(), t.onSurfaceVariant(), t.primary());
		} else {
			UiRenderUtil.drawPanel(context, toggleX, y + 5, 40, 18,
					setting.getValue() ? accentSoft : 0x7012161C,
					setting.getValue() ? accent : UiRenderUtil.OUTLINE);
			context.fill(toggleX + 3 + knobOffset, y + 8, toggleX + 17 + knobOffset, y + 20, setting.getValue() ? accent : 0xFF737A86);
		}
	}

	private void drawNumberSetting(DrawContext context, NumberSetting setting, int x, int y, int width, int accent) {
		drawPanel(context, x, y, width, 36, GuiTheme.isM3() ? elevationSurface(panelAlt(), 1) : panelAlt(), outlineBg());
		context.drawText(textRenderer, setting.getName(), x + 10, y + 8, textCol(), false);
		context.drawText(textRenderer, format(setting.getValue()), x + width - 42, y + 8, mutedCol(), false);
		int sliderX = x + 10;
		int sliderY = y + 23;
		int sliderWidth = width - 20;
		double progress = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
		int fillWidth = (int) Math.round(sliderWidth * progress);
		if (GuiTheme.isM3()) {
			ThemeTokens t = GuiTheme.current();
			UiRenderUtil.drawRoundedFill(context, sliderX, sliderY, sliderWidth, 5, t.secondaryContainer(), ShapeScale.FULL);
			UiRenderUtil.drawRoundedFill(context, sliderX, sliderY, fillWidth, 5, accent, ShapeScale.FULL);
			int thumbX = sliderX + (int)Math.round((sliderWidth - 10) * progress);
			UiRenderUtil.drawRoundedFill(context, thumbX, sliderY - 3, 10, 11, t.primary(), ShapeScale.FULL);
		} else {
			context.fill(sliderX, sliderY, sliderX + sliderWidth, sliderY + 4, 0x60131820);
			context.fill(sliderX, sliderY, sliderX + fillWidth, sliderY + 4, accent);
		}
	}

	private void drawStringSetting(DrawContext context, StringSetting setting, int x, int y, int width, int accent) {
		drawPanel(context, x, y, width, 34, GuiTheme.isM3() ? elevationSurface(panelAlt(), 1) : panelAlt(), outlineBg());
		context.drawText(textRenderer, setting.getName(), x + 10, y + 8, textCol(), false);
		boolean editing = editingString == setting;
		String value = setting.getValue().isBlank() ? "empty" : setting.getValue();
		value = ellipsize(value, Math.max(40, width - 120));
		context.drawText(textRenderer, editing ? value + "_" : value, x + 110, y + 8, editing ? accent : mutedCol(), false);
	}

	private void drawColorSetting(DrawContext context, ColorSetting setting, int x, int y, int width, int accent, int accentSoft) {
		ColorValue value = setting.getValue();
		drawPanel(context, x, y, width, 100, GuiTheme.isM3() ? elevationSurface(panelAlt(), 1) : panelAlt(), outlineBg());
		context.drawText(textRenderer, setting.getName(), x + 10, y + 8, textCol(), false);
		context.fill(x + width - 34, y + 8, x + width - 14, y + 24, setting.resolveColor());
		drawChannel(context, x + 10, y + 28, width - 20, "R", value.getRed(), 0xFFFF5C74);
		drawChannel(context, x + 10, y + 44, width - 20, "G", value.getGreen(), 0xFF7DFF8A);
		drawChannel(context, x + 10, y + 60, width - 20, "B", value.getBlue(), 0xFF7EB6FF);
		drawChannel(context, x + 10, y + 76, width - 20, "A", value.getAlpha(), 0xFFFFFFFF);
		int toggleX = x + width - 96;
		drawButton(context, toggleX, y + 8, 50, 16, value.isRainbow() ? accentSoft : 0x700F141C, value.isRainbow() ? accent : outlineBg());
		context.drawCenteredTextWithShadow(textRenderer, value.isRainbow() ? "RGB" : "Static", toggleX + 25, y + 12, textCol());
	}

	private void drawEnumSetting(DrawContext context, EnumSetting<?> setting, int x, int y, int width, int accent) {
		drawPanel(context, x, y, width, 28, GuiTheme.isM3() ? elevationSurface(panelAlt(), 1) : panelAlt(), outlineBg());
		context.drawText(textRenderer, setting.getName(), x + 10, y + 10, textCol(), false);
		String value = ellipsize(setting.getValue().toString(), Math.max(48, width - 124));
		context.drawText(textRenderer, value, x + width - textRenderer.getWidth(value) - 10, y + 10, accent, false);
	}

	private void drawChannel(DrawContext context, int x, int y, int width, String label, int value, int color) {
		context.drawText(textRenderer, label + " " + value, x, y - 1, mutedCol(), false);
		int fillWidth = (int) Math.round((width - 44) * (value / 255.0));
		if (GuiTheme.isM3()) {
			UiRenderUtil.drawRoundedFill(context, x + 44, y + 4, width - 44, 5, GuiTheme.current().secondaryContainer(), ShapeScale.FULL);
			UiRenderUtil.drawRoundedFill(context, x + 44, y + 4, fillWidth, 5, color, ShapeScale.FULL);
		} else {
			context.fill(x + 44, y + 4, x + width, y + 8, 0x60131820);
			context.fill(x + 44, y + 4, x + 44 + fillWidth, y + 8, color);
		}
	}

	private void drawBindRow(DrawContext context, int x, int y, int width, int accent) {
		drawPanel(context, x, y, width, 32, GuiTheme.isM3() ? elevationSurface(panelAlt(), 1) : panelAlt(), outlineBg());
		context.drawText(textRenderer, "Keybind", x + 10, y + 10, textCol(), false);
		String bindText = bindingTarget == selectedModule ? "Press a key..." : getKeyName(selectedModule.getKeyCode());
		bindText = ellipsize(bindText, Math.max(48, width - 84));
		context.drawText(textRenderer, bindText, x + width - textRenderer.getWidth(bindText) - 10, y + 10, bindingTarget == selectedModule ? accent : mutedCol(), false);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		Layout layout = layout();
		if (button == 0 && handleTitleClick(mouseX, mouseY, layout)) return true;
		if (button == 0 && handleThemeClick(mouseX, mouseY, layout)) return true;
		if (handleHomeClick(mouseX, mouseY, button, layout)) return true;
		if (handleCategoryClick(mouseX, mouseY, layout.leftX(), layout.contentY(), layout.leftWidth())) return true;
		if (handleModuleClick(mouseX, mouseY, layout.centerX(), layout.contentY(), layout.centerWidth(), layout.contentHeight())) return true;
		if (handleSettingClick(mouseX, mouseY, layout.rightX(), layout.contentY(), layout.rightWidth())) return true;
		return super.mouseClicked(mouseX, mouseY, button);
	}

	/** Renders the theme-switcher FAB at the bottom-left of the root panel. */
	private void renderThemeButton(DrawContext context, Layout layout, int mouseX, int mouseY, int accent, int accentSoft) {
		if (GuiTheme.isM3()) {
			ThemeTokens t = GuiTheme.current();
			String label = GuiTheme.current().displayName();
			int btnW = Math.max(104, textRenderer.getWidth(label) + 26), btnH = 28;
			int btnX = layout.rootX() + 14;
			int btnY = layout.rootY() + layout.rootHeight() - btnH - 10;
			boolean hovered = UiRenderUtil.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH);
			themeButtonHoverAnim = AnimationUtil.smooth(themeButtonHoverAnim, hovered ? 1f : 0f, 0.22f);
			int bg = UiRenderUtil.lerpColor(t.primaryContainer(), t.secondaryContainer(), themeButtonHoverAnim * 0.65f);
			UiRenderUtil.drawRoundedPanel(context, btnX, btnY, btnW, btnH, bg, t.primary(), ShapeScale.FULL);
			UiRenderUtil.drawStateLayer(context, btnX, btnY, btnW, btnH, t.stateHover(), themeButtonHoverAnim, ShapeScale.FULL);
			context.drawCenteredTextWithShadow(textRenderer, label, btnX + btnW / 2, btnY + 10, t.onPrimaryContainer());
		} else {
			// Legit: compact flat button
			int btnW = 64, btnH = 22;
			int btnX = layout.rootX() + 16;
			int btnY = layout.rootY() + layout.rootHeight() - btnH - 10;
			boolean hovered = UiRenderUtil.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH);
			themeButtonHoverAnim = AnimationUtil.smooth(themeButtonHoverAnim, hovered ? 1f : 0f, 0.22f);
			int bg = UiRenderUtil.lerpColor(UiRenderUtil.PANEL_ALT, accentSoftColor(), themeButtonHoverAnim);
			int border = UiRenderUtil.lerpColor(UiRenderUtil.OUTLINE, accent, themeButtonHoverAnim);
			UiRenderUtil.drawPanel(context, btnX, btnY, btnW, btnH, bg, border);
			context.drawCenteredTextWithShadow(textRenderer, "Legit", btnX + btnW / 2, btnY + 7, UiRenderUtil.TEXT);
		}
	}

	private boolean handleThemeClick(double mouseX, double mouseY, Layout layout) {
		int btnW = GuiTheme.isM3() ? Math.max(104, textRenderer.getWidth(GuiTheme.current().displayName()) + 26) : 64;
		int btnH = GuiTheme.isM3() ? 28 : 22;
		int btnX = layout.rootX() + (GuiTheme.isM3() ? 14 : 16);
		int btnY = layout.rootY() + layout.rootHeight() - btnH - 10;
		if (!UiRenderUtil.isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) return false;
		GuiTheme.cycle();
		openProgress = 0f;
		themeRippleAnim = 1f;
		return true;
	}

	private boolean handleTitleClick(double mouseX, double mouseY, Layout layout) {
		int titleX = layout.rootX() + 18;
		int titleY = layout.rootY() + 14;
		int titleWidth = textRenderer.getWidth("LEGITIMITY");
		if (!UiRenderUtil.isHovered(mouseX, mouseY, titleX - 2, titleY - 2, titleWidth + 4, 12)) {
			return false;
		}

		long now = System.currentTimeMillis();
		if (now - firstTitleClickAt > RGB_CLICK_WINDOW_MS) {
			titleClickCount = 0;
			firstTitleClickAt = now;
		}
		if (titleClickCount == 0) {
			firstTitleClickAt = now;
		}

		titleClickCount++;
		if (titleClickCount >= RGB_CLICK_TARGET) {
			rgbModeUntil = now + RGB_MODE_DURATION_MS;
			titleClickCount = 0;
			firstTitleClickAt = 0L;
		}
		return true;
	}

	private boolean handleHomeClick(double mouseX, double mouseY, int button, Layout layout) {
		if (button != 0) {
			return false;
		}

		TopBarLayout topBar = topBar(layout);
		if (topBar.searchWidth() >= 90 && UiRenderUtil.isHovered(mouseX, mouseY, topBar.searchX(), topBar.y(), topBar.searchWidth(), topBar.buttonHeight())) {
			editingModuleSearch = true;
			return true;
		}
		editingModuleSearch = false;

		if (UiRenderUtil.isHovered(mouseX, mouseY, topBar.styleX(), topBar.y(), topBar.styleWidth(), topBar.buttonHeight())) {
			savedModuleLayout = savedModuleLayout.next();
			moduleScroll = 0.0D;
			saveUiState();
			return true;
		}

		if (UiRenderUtil.isHovered(mouseX, mouseY, topBar.homeX(), topBar.y(), topBar.homeWidth(), topBar.buttonHeight())) {
			homePanelOpen = !homePanelOpen;
			return true;
		}

		if (!homePanelOpen) {
			return false;
		}

		int panelX = layout.rightX();
		int panelY = layout.contentY();
		int panelWidth = layout.rightWidth();
		int rowX = panelX + 14;
		int rowWidth = panelWidth - 28;
		int rowHeight = 28;
		int rowY = panelY + 48;
		for (int i = 1; i <= HomeNameConfig.size(); i++) {
			int editButtonSize = 20;
			int editButtonX = rowX + rowWidth - editButtonSize - 4;
			if (UiRenderUtil.isHovered(mouseX, mouseY, editButtonX, rowY + 4, editButtonSize, editButtonSize)) {
				editingHomeSlot = i;
				homeLabelDraft = HomeNameConfig.getLabel(i);
				return true;
			}
			if (UiRenderUtil.isHovered(mouseX, mouseY, rowX, rowY, rowWidth, rowHeight)) {
				commitHomeRename();
				sendHomeCommand(i);
				return true;
			}
			rowY += rowHeight + 8;
		}

		commitHomeRename();
		homePanelOpen = false;
		return true;
	}

	private boolean handleCategoryClick(double mouseX, double mouseY, int x, int y, int width) {
		int rowHeight = CATEGORY_ROW_HEIGHT;
		int rowY = y + CATEGORY_TOP_PADDING;
		for (ModuleCategory category : ModuleCategory.values()) {
			if (UiRenderUtil.isHovered(mouseX, mouseY, x + 6, rowY, width - 12, rowHeight)) {
				selectedCategory = category;
				savedCategory = category;
				moduleScroll = SAVED_SCROLL.getOrDefault(selectedCategory, 0.0D);
				syncSelectedModule();
				saveUiState();
				return true;
			}
			rowY += rowHeight + CATEGORY_ROW_GAP;
		}
		return false;
	}

	private boolean handleModuleClick(double mouseX, double mouseY, int x, int y, int width, int height) {
		Layout layout = layout();
		List<Module> modules = getVisibleModulesInCategory(selectedCategory);
		int rowHeight = 56;
		int listTop = y + 38;
		int columns = layout.moduleColumns();
		int cardWidth = layout.moduleCardWidth();
		int stride = rowHeight + 8;

		for (int i = 0; i < modules.size(); i++) {
			Module module = modules.get(i);
			int column = i % columns;
			int row = i / columns;
			int rowX = x + 10 + column * (cardWidth + 8);
			int rowY = listTop + row * stride - (int) moduleScroll;
			if (rowY + rowHeight < listTop || rowY > y + height - 10) {
				continue;
			}

			if (UiRenderUtil.isHovered(mouseX, mouseY, rowX, rowY, cardWidth, rowHeight)) {
				int toggleX = rowX + cardWidth - 46 - 10;
				if (UiRenderUtil.isHovered(mouseX, mouseY, toggleX, rowY + 15, 46, 20)) {
					module.toggle();
				} else {
					selectedModule = module;
					saveUiState();
				}
				return true;
			}
		}

		return false;
	}

	private boolean handleSettingClick(double mouseX, double mouseY, int x, int y, int width) {
		if (selectedModule == null) {
			return false;
		}

		int contentTop = y + 31 + Math.max(10, textRenderer.getWrappedLinesHeight(selectedModule.getDescription(), Math.max(90, width - 28))) + 12;
		if (isEspModuleSelected()) {
			if (handleEspTabClick(mouseX, mouseY, x + 14, contentTop, width - 28)) {
				return true;
			}
			contentTop += 34;
		}

		int cursorY = contentTop - (int) settingsScroll;
		for (Setting<?> setting : getVisibleSettings()) {
			if (setting instanceof BooleanSetting booleanSetting) {
				if (UiRenderUtil.isHovered(mouseX, mouseY, x + 14, cursorY, width - 28, 28)) {
					booleanSetting.setValue(!booleanSetting.getValue());
					return true;
				}
				cursorY += getSettingHeight(setting);
			} else if (setting instanceof NumberSetting numberSetting) {
				if (UiRenderUtil.isHovered(mouseX, mouseY, x + 24, cursorY + 20, width - 48, 12)) {
					draggingNumber = numberSetting;
					updateNumberFromMouse(numberSetting, mouseX, x + 24, width - 48);
					return true;
				}
				cursorY += getSettingHeight(setting);
			} else if (setting instanceof StringSetting stringSetting) {
				if (UiRenderUtil.isHovered(mouseX, mouseY, x + 14, cursorY, width - 28, 34)) {
					editingString = stringSetting;
					bindingTarget = null;
					return true;
				}
				cursorY += getSettingHeight(setting);
			} else if (setting instanceof ColorSetting colorSetting) {
				if (UiRenderUtil.isHovered(mouseX, mouseY, x + width - 82, cursorY + 8, 50, 16)) {
					ColorValue value = colorSetting.getValue();
					value.setRainbow(!value.isRainbow());
					colorSetting.setValue(value);
					return true;
				}
				for (int channel = 0; channel < 4; channel++) {
					int sliderY = cursorY + 28 + channel * 16;
					if (UiRenderUtil.isHovered(mouseX, mouseY, x + 58, sliderY + 2, width - 72, 10)) {
						draggingColor = colorSetting;
						draggingColorChannel = channel;
						updateColorFromMouse(colorSetting, channel, mouseX, x + 58, width - 72);
						return true;
					}
				}
				cursorY += getSettingHeight(setting);
			} else if (setting instanceof EnumSetting<?> enumSetting) {
				if (UiRenderUtil.isHovered(mouseX, mouseY, x + 14, cursorY, width - 28, 28)) {
					enumSetting.cycle();
					return true;
				}
				cursorY += getSettingHeight(setting);
			}
		}

		if (UiRenderUtil.isHovered(mouseX, mouseY, x + 14, cursorY + 4, width - 28, 32)) {
			bindingTarget = selectedModule;
			editingString = null;
			return true;
		}

		return false;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		Layout layout = layout();
		if (draggingNumber != null) {
			updateNumberFromMouse(draggingNumber, mouseX, layout.rightX() + 24, layout.rightWidth() - 48);
			return true;
		}
		if (draggingColor != null && draggingColorChannel >= 0) {
			updateColorFromMouse(draggingColor, draggingColorChannel, mouseX, layout.rightX() + 58, layout.rightWidth() - 72);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		draggingNumber = null;
		draggingColor = null;
		draggingColorChannel = -1;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		Layout layout = layout();
		if (UiRenderUtil.isHovered(mouseX, mouseY, layout.rightX(), layout.contentY(), layout.rightWidth(), layout.contentHeight())) {
			settingsScroll -= verticalAmount * 26.0D;
		} else {
			moduleScroll -= verticalAmount * 26.0D;
		}
		saveUiState();
		return true;
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (editingModuleSearch && !Character.isISOControl(chr)) {
			if (moduleSearch.length() < 32) {
				moduleSearch += chr;
				syncSelectedModule();
				settingsScroll = 0.0D;
			}
			return true;
		}
		if (editingHomeSlot != -1 && !Character.isISOControl(chr)) {
			if (homeLabelDraft.length() < 18) {
				homeLabelDraft += chr;
			}
			return true;
		}
		if (editingString != null && !Character.isISOControl(chr)) {
			editingString.setValue(editingString.getValue() + chr);
			return true;
		}
		return super.charTyped(chr, modifiers);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (editingModuleSearch) {
			if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !moduleSearch.isEmpty()) {
				moduleSearch = moduleSearch.substring(0, moduleSearch.length() - 1);
				syncSelectedModule();
				settingsScroll = 0.0D;
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				if (!moduleSearch.isEmpty()) {
					moduleSearch = "";
					syncSelectedModule();
					settingsScroll = 0.0D;
				} else {
					editingModuleSearch = false;
				}
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
				editingModuleSearch = false;
				return true;
			}
		}

		if (editingHomeSlot != -1) {
			if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !homeLabelDraft.isEmpty()) {
				homeLabelDraft = homeLabelDraft.substring(0, homeLabelDraft.length() - 1);
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
				commitHomeRename();
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				editingHomeSlot = -1;
				homeLabelDraft = "";
				return true;
			}
		}

		if (bindingTarget != null) {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
				bindingTarget.setKeyCode(GLFW.GLFW_KEY_UNKNOWN);
			} else {
				bindingTarget.setKeyCode(keyCode);
			}
			bindingTarget = null;
			return true;
		}

		if (editingString != null) {
			if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !editingString.getValue().isEmpty()) {
				String current = editingString.getValue();
				editingString.setValue(current.substring(0, current.length() - 1));
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
				editingString = null;
				return true;
			}
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private void updateNumberFromMouse(NumberSetting setting, double mouseX, int sliderX, int sliderWidth) {
		double progress = AnimationUtil.clamp((mouseX - sliderX) / sliderWidth, 0.0D, 1.0D);
		double value = setting.getMin() + (setting.getMax() - setting.getMin()) * progress;
		setting.setValue(value);
	}

	private void updateColorFromMouse(ColorSetting setting, int channel, double mouseX, int sliderX, int sliderWidth) {
		int value = (int) Math.round(AnimationUtil.clamp((mouseX - sliderX) / sliderWidth, 0.0D, 1.0D) * 255.0D);
		ColorValue current = setting.getValue().copy();
		if (channel == 0) {
			current.setRed(value);
		} else if (channel == 1) {
			current.setGreen(value);
		} else if (channel == 2) {
			current.setBlue(value);
		} else if (channel == 3) {
			current.setAlpha(value);
		}
		setting.setValue(current);
	}

	private String getKeyName(int keyCode) {
		if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
			return "None";
		}

		try {
			return InputUtil.fromKeyCode(keyCode, -1).getLocalizedText().getString();
		} catch (Exception ignored) {
			return "Key " + keyCode;
		}
	}

	private String format(double value) {
		if (Math.abs(value - Math.round(value)) < 0.001D) {
			return Integer.toString((int) Math.round(value));
		}

		return String.format("%.2f", value);
	}

	private String ellipsize(String value, int maxWidth) {
		if (textRenderer.getWidth(value) <= maxWidth) {
			return value;
		}

		String trimmed = textRenderer.trimToWidth(value, Math.max(0, maxWidth - textRenderer.getWidth("...")));
		return trimmed + "...";
	}



	private String categoryLabel(ModuleCategory category) {
		return switch (category) {
			case COMBAT -> "Combat";
			case MOVEMENT -> "Movement";
			case RENDER -> "Render";
			case HUD -> "Hud";
			case UTILITY -> "Utility";
			case MISC -> "Misc";
		};
	}

	private String moduleLayoutLabel(int centerWidth) {
		return resolveModuleColumns(centerWidth) + "x";
	}

	private List<Module> getVisibleModulesInCategory(ModuleCategory category) {
		List<Module> visible = new ArrayList<>();
		String query = moduleSearch.trim().toLowerCase(Locale.ROOT);
		if (query.isEmpty()) {
			// No search active → show current category
			return moduleManager.getModulesInCategory(category);
		}
		// Global search: search ALL modules across ALL categories
		for (Module module : moduleManager.getModules()) {
			if (module.getName().toLowerCase(Locale.ROOT).contains(query)
					|| module.getDescription().toLowerCase(Locale.ROOT).contains(query)) {
				visible.add(module);
			}
		}
		return visible;
	}

	private void syncSelectedModule() {
		List<Module> modules = getVisibleModulesInCategory(selectedCategory);
		if (selectedModule != null && modules.contains(selectedModule)) {
			return;
		}
		if (modules.isEmpty()) {
			selectedModule = null;
			return;
		}
		// Auto-switch to the first result's category tab
		Module first = modules.getFirst();
		if (!moduleSearch.isBlank() && first.getCategory() != selectedCategory) {
			selectedCategory = first.getCategory();
			savedCategory = selectedCategory;
		}
		selectedModule = first;
	}

	private boolean isEspModuleSelected() {
		return selectedModule instanceof EspModule;
	}

	private List<Setting<?>> getVisibleSettings() {
		if (selectedModule == null) {
			return List.of();
		}
		if (!isEspModuleSelected()) {
			return selectedModule.getSettings();
		}

		List<Setting<?>> visible = new java.util.ArrayList<>();
		for (Setting<?> setting : selectedModule.getSettings()) {
			if (resolveEspTab(setting) == savedEspSettingsTab) {
				visible.add(setting);
			}
		}
		return visible;
	}

	private EspSettingsTab resolveEspTab(Setting<?> setting) {
		if (setting instanceof ColorSetting) {
			return EspSettingsTab.COLORS;
		}

		String name = setting.getName();
		if (
			name.equals("Players") ||
			name.equals("Trust Colors") ||
			name.equals("Hostiles") ||
			name.equals("Passives") ||
			name.equals("Neutral Mobs") ||
			name.equals("Items") ||
			name.equals("Projectiles") ||
			name.equals("Own Projectiles") ||
			name.equals("Invisible Targets") ||
			name.equals("Target Range")
		) {
			return EspSettingsTab.TARGETS;
		}

		if (
			name.equals("Storage Blocks") ||
			name.equals("Utility Blocks") ||
			name.equals("Rare Blocks") ||
			name.equals("Block Range") ||
			name.equals("Rare Range") ||
			name.equals("Max Blocks")
		) {
			return EspSettingsTab.BLOCKS;
		}

		return EspSettingsTab.VISUALS;
	}

	private int getSettingHeight(Setting<?> setting) {
		if (setting instanceof BooleanSetting) {
			return 34;
		}
		if (setting instanceof NumberSetting) {
			return 42;
		}
		if (setting instanceof StringSetting) {
			return 40;
		}
		if (setting instanceof ColorSetting) {
			return 106;
		}
		if (setting instanceof EnumSetting<?>) {
			return 34;
		}
		return 0;
	}

	private void renderEspTabs(DrawContext context, int x, int y, int width, int mouseX, int mouseY, int accent, int accentSoft) {
		int gap = 6;
		int tabWidth = (width - gap * (EspSettingsTab.values().length - 1)) / EspSettingsTab.values().length;
		int tabX = x;

		for (EspSettingsTab tab : EspSettingsTab.values()) {
			boolean selected = tab == savedEspSettingsTab;
			boolean hovered = UiRenderUtil.isHovered(mouseX, mouseY, tabX, y, tabWidth, 24);
			if (GuiTheme.isM3()) {
				ThemeTokens t = GuiTheme.current();
				int background = selected ? t.tertiaryContainer() : elevationSurface(t.surfaceContainerHigh(), 1);
				int border = selected ? t.tertiary() : t.outlineVariant();
				UiRenderUtil.drawRoundedPanel(context, tabX, y, tabWidth, 24, background, border, ShapeScale.FULL);
				if (hovered && !selected) {
					UiRenderUtil.drawStateLayer(context, tabX, y, tabWidth, 24, t.stateHover(), 1.0f, ShapeScale.FULL);
				}
				context.drawCenteredTextWithShadow(textRenderer, tab.label(), tabX + tabWidth / 2, y + 8,
						selected ? t.onTertiaryContainer() : t.onSurfaceVariant());
			} else {
				int background = selected ? selectedBg() : hovered ? panelAlt() : 0x40202030;
				int border = selected ? accent : outlineBg();
				drawButton(context, tabX, y, tabWidth, 24, background, border);
				context.drawCenteredTextWithShadow(textRenderer, tab.label(), tabX + tabWidth / 2, y + 8, selected ? textCol() : mutedCol());
			}
			tabX += tabWidth + gap;
		}
	}

	private boolean handleEspTabClick(double mouseX, double mouseY, int x, int y, int width) {
		int gap = 6;
		int tabWidth = (width - gap * (EspSettingsTab.values().length - 1)) / EspSettingsTab.values().length;
		int tabX = x;

		for (EspSettingsTab tab : EspSettingsTab.values()) {
			if (UiRenderUtil.isHovered(mouseX, mouseY, tabX, y, tabWidth, 24)) {
				if (savedEspSettingsTab != tab) {
					savedEspSettingsTab = tab;
					settingsScroll = 0.0D;
				}
				return true;
			}
			tabX += tabWidth + gap;
		}

		return false;
	}

	private boolean isRgbModeActive() {
		return System.currentTimeMillis() < rgbModeUntil;
	}

	/** Returns the current accent color — RGB-mode rainbow or theme primary. */
	private int accentColor() {
		if (isRgbModeActive()) {
			float hue = (System.currentTimeMillis() % 1200L) / 1200.0f;
			return 0xFF000000 | java.awt.Color.HSBtoRGB(hue, 0.9f, 1.0f) & 0x00FFFFFF;
		}
		return GuiTheme.current().primary();
	}

	private int accentSoftColor() {
		int base = accentColor() & 0x00FFFFFF;
		int alpha = GuiTheme.isM3() ? 0x50 : 0x80;
		return (alpha << 24) | base;
	}

	// ── Theme-aware color helpers (all via ThemeTokens) ─────────────────────

	private int panelBg()         { return GuiTheme.current().surfaceContainer(); }
	private int panelSoft()       { return GuiTheme.current().surface(); }
	private int panelAlt()        { return GuiTheme.current().surfaceContainerHigh(); }
	private int outlineBg()       { return GuiTheme.current().outlineVariant(); }
	private int textCol()         { return GuiTheme.current().onSurface(); }
	private int mutedCol()        { return GuiTheme.current().onSurfaceVariant(); }

	/** Text on a selected (primary-container) background. */
	private int selectedTextCol() { return GuiTheme.current().onPrimaryContainer(); }

	/** Selected item background (primary-container in M3, accentSoft in Legit). */
	private int selectedBg()      { return GuiTheme.current().primaryContainer(); }

	private int elevationSurface(int surface, int level) {
		int tint = GuiTheme.current().elevationTint(level);
		float alpha = ((tint >>> 24) & 0xFF) / 255.0f;
		return UiRenderUtil.lerpColor(surface, 0xFF000000 | (tint & 0x00FFFFFF), alpha);
	}

	// ── Shape helpers ────────────────────────────────────────────────────────

	/** Radius for standard cards and setting rows. */
	private int panelR() { return GuiTheme.current().radiusMedium(); }

	/** Radius for buttons (tonal/filled). */
	private int btnR()   { return GuiTheme.current().radiusLarge(); }

	/** Full pill radius (nav rail, toggle track). */
	private int pillR()  { return GuiTheme.current().radiusFull(); }

	/** FAB radius. */
	private int fabR()   { return GuiTheme.current().radiusLarge(); }

	// ── Draw helpers ─────────────────────────────────────────────────────────

	/** Draws a themed card — rounded for M3, sharp for Legit. */
	private void drawPanel(DrawContext ctx, int x, int y, int w, int h, int bg, int border) {
		int r = panelR();
		if (r > 0) {
			UiRenderUtil.drawRoundedPanel(ctx, x, y, w, h, bg, border, Math.min(r, Math.max(2, h / 2)));
		} else {
			UiRenderUtil.drawPanel(ctx, x, y, w, h, bg, border);
		}
	}

	/** Draws a themed button — pill for M3, sharp for Legit. */
	private void drawButton(DrawContext ctx, int x, int y, int w, int h, int bg, int border) {
		int r = btnR();
		if (r > 0) {
			UiRenderUtil.drawRoundedPanel(ctx, x, y, w, h, bg, border, ShapeScale.FULL);
		} else {
			UiRenderUtil.drawPanel(ctx, x, y, w, h, bg, border);
		}
	}


	private int calculateSettingsHeight(List<Setting<?>> settings) {
		int total = 36;
		for (Setting<?> setting : settings) {
			total += getSettingHeight(setting);
		}
		return total;
	}

	private void saveUiState() {
		savedCategory = selectedCategory;
		savedModuleName = selectedModule == null ? null : selectedModule.getName();
		SAVED_SCROLL.put(selectedCategory, moduleScroll);
	}

	private void commitHomeRename() {
		if (editingHomeSlot == -1) {
			return;
		}

		HomeNameConfig.setLabel(editingHomeSlot, homeLabelDraft);
		LegitimityClient.get().requestSave();
		editingHomeSlot = -1;
		homeLabelDraft = "";
	}

	private void sendHomeCommand(int slot) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.getNetworkHandler() != null) {
			client.getNetworkHandler().sendChatCommand("home " + slot);
		}
		homePanelOpen = false;
		client.setScreen(null);
	}

	private record Layout(
		int rootX,
		int rootY,
		int rootWidth,
		int rootHeight,
		int leftX,
		int centerX,
		int rightX,
		int contentY,
		int contentHeight,
		int leftWidth,
		int centerWidth,
		int rightWidth,
		int moduleColumns,
		int moduleCardWidth
	) {
	}

	private enum ModuleLayout {
		SINGLE,
		DOUBLE,
		TRIPLE;

		private ModuleLayout next() {
			return switch (this) {
				case SINGLE -> DOUBLE;
				case DOUBLE -> TRIPLE;
				case TRIPLE -> SINGLE;
			};
		}
	}

	private record TopBarLayout(
		int searchX,
		int styleX,
		int homeX,
		int y,
		int searchWidth,
		int styleWidth,
		int homeWidth,
		int buttonHeight
	) {
	}

	private enum EspSettingsTab {
		VISUALS("Visuals"),
		TARGETS("Targets"),
		BLOCKS("Blocks"),
		COLORS("Colors");

		private final String label;

		EspSettingsTab(String label) {
			this.label = label;
		}

		private String label() {
			return label;
		}
	}



}
