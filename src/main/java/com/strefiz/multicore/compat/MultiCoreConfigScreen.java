package com.strefiz.multicore.compat;

import com.strefiz.multicore.config.ConfigMode;
import com.strefiz.multicore.config.ConfigStore;
import com.strefiz.multicore.config.MultiCoreConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Native Minecraft configuration screen, opened from Mod Menu.
 *
 * <p>Every change is persisted immediately to {@code config/multicore.json}, but thread/mode/region
 * settings are only applied at server start, so the screen always shows a <em>restart required</em>
 * warning (see D-008).
 *
 * <p>This class is client-only: it references {@code net.minecraft.client} and is only constructed by
 * Mod Menu. On a dedicated server (no client) it is never loaded.
 */
public class MultiCoreConfigScreen extends Screen {

	private static final int WIDTH = 240;
	private static final int HEIGHT = 20;
	private static final int SMALL = 40;
	private static final int LARGE = 100;
	private static final int GAP = 4;

	private final Screen parent;
	private final ConfigStore store;
	private MultiCoreConfig config;

	/**
	 * Creates the screen.
	 *
	 * @param parent the screen to return to when the user is done
	 */
	public MultiCoreConfigScreen(Screen parent) {
		super(Component.translatable("config.multicore.title"));
		this.parent = parent;
		this.store = ConfigStore.defaultStore();
		this.config = store.load();
	}

	@Override
	protected void init() {
		int x = this.width / 2 - WIDTH / 2;
		int y = this.height / 6;

		addWorkersRow(x, y);
		y += 26;
		addModeRow(x, y);
		y += 26;
		addDebugRow(x, y);
		y += 30;

		MultiLineTextWidget warning = new MultiLineTextWidget(x, y,
				Component.translatable("config.multicore.restartRequired"), this.font);
		warning.setMaxWidth(WIDTH);
		this.addRenderableWidget(warning);
		y += HEIGHT * 2 + 8;

		this.addRenderableWidget(Button.builder(Component.translatable("config.multicore.done"),
				button -> Minecraft.getInstance().setScreenAndShow(this.parent))
				.bounds(x, y, WIDTH, HEIGHT).build());
	}

	@Override
	public void onClose() {
		store.save(config);
		Minecraft.getInstance().setScreenAndShow(this.parent);
	}

	private void addWorkersRow(int x, int y) {
		int labelWidth = WIDTH - SMALL * 2 - GAP * 3;
		this.addRenderableWidget(new StringWidget(x, y, labelWidth, HEIGHT, workersLabel(), this.font));
		this.addRenderableWidget(Button.builder(Component.literal("-"), button -> changeWorkers(-1))
				.bounds(x + labelWidth + GAP, y, SMALL, HEIGHT).build());
		this.addRenderableWidget(Button.builder(Component.literal("+"), button -> changeWorkers(+1))
				.bounds(x + labelWidth + GAP + SMALL + GAP, y, SMALL, HEIGHT).build());
	}

	private void addModeRow(int x, int y) {
		int labelWidth = WIDTH - LARGE - GAP;
		this.addRenderableWidget(new StringWidget(x, y, labelWidth, HEIGHT, modeLabel(), this.font));
		this.addRenderableWidget(Button.builder(modeValueLabel(), button -> cycleMode())
				.bounds(x + labelWidth + GAP, y, LARGE, HEIGHT).build());
	}

	private void addDebugRow(int x, int y) {
		int labelWidth = WIDTH - LARGE - GAP;
		this.addRenderableWidget(new StringWidget(x, y, labelWidth, HEIGHT, debugLabel(), this.font));
		this.addRenderableWidget(Button.builder(debugValueLabel(), button -> toggleDebug())
				.bounds(x + labelWidth + GAP, y, LARGE, HEIGHT).build());
	}

	private void changeWorkers(int delta) {
		config = config.withWorkerThreads(config.workerThreads() + delta);
		store.save(config);
		rebuild();
	}

	private void cycleMode() {
		ConfigMode next = config.mode() == ConfigMode.SAFE ? ConfigMode.AGGRESSIVE : ConfigMode.SAFE;
		config = config.withMode(next);
		store.save(config);
		rebuild();
	}

	private void toggleDebug() {
		config = config.withDebug(!config.debug());
		store.save(config);
		rebuild();
	}

	private void rebuild() {
		this.clearWidgets();
		this.init();
	}

	private Component workersLabel() {
		return Component.translatable("config.multicore.workers", config.workerThreads());
	}

	private Component modeLabel() {
		return Component.translatable("config.multicore.mode");
	}

	private Component modeValueLabel() {
		return Component.translatable(config.mode() == ConfigMode.SAFE
				? "config.multicore.mode.safe" : "config.multicore.mode.aggressive");
	}

	private Component debugLabel() {
		return Component.translatable("config.multicore.debug");
	}

	private Component debugValueLabel() {
		return Component.translatable(config.debug() ? "config.multicore.on" : "config.multicore.off");
	}
}
