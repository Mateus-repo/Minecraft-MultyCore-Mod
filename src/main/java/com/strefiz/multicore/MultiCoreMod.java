package com.strefiz.multicore;

import java.util.Map;

import com.strefiz.multicore.command.MultiCoreCommand;
import com.strefiz.multicore.command.StatusSource;
import com.strefiz.multicore.config.ConfigMode;
import com.strefiz.multicore.config.ConfigStore;
import com.strefiz.multicore.config.MultiCoreConfig;
import com.strefiz.multicore.metrics.TickMetrics;
import com.strefiz.multicore.thread.MainThread;
import com.strefiz.multicore.thread.TaskScheduler;
import com.strefiz.multicore.thread.WorkerPool;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.server.MinecraftServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point of the MultiCore server mod.
 *
 * <p>Bootstraps the configuration, the worker pool, the scheduler, the metrics and the
 * {@code /multicore} command, and wires the lifecycle:
 *
 * <ul>
 *   <li>{@code SERVER_STARTING} — capture the main thread, load the config, start the pool;</li>
 *   <li>{@code START_SERVER_TICK} — start measuring;</li>
 *   <li>{@code END_SERVER_TICK} — merge offloaded results on the main thread, stop measuring;</li>
 *   <li>{@code SERVER_STOPPED} — shut the pool down.</li>
 * </ul>
 *
 * <p>It also implements {@link StatusSource} so the command reads the live state from one place.
 */
public class MultiCoreMod implements DedicatedServerModInitializer, StatusSource {

	/** Mod id, used for logging and config lookup. */
	public static final String MOD_ID = "multicore";

	/** Logger for the whole mod. */
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private final TickMetrics metrics = new TickMetrics();

	private MultiCoreConfig config;
	private WorkerPool pool;
	private TaskScheduler scheduler;

	@Override
	public void onInitializeServer() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) ->
				MultiCoreCommand.register(dispatcher, this));
		ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
		ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped);
		ServerTickEvents.START_SERVER_TICK.register(this::onStartTick);
		ServerTickEvents.END_SERVER_TICK.register(this::onEndTick);
	}

	private void onServerStarting(MinecraftServer server) {
		MainThread.capture();
		config = ConfigStore.defaultStore().load();
		pool = new WorkerPool("multicore-worker", config.workerThreads());
		scheduler = new TaskScheduler(pool);
		pool.start();

		if (config.multithreaded()) {
			LOGGER.info("MultiCore active: {} worker thread(s), mode={}.",
					config.workerThreads(), config.mode());
		} else {
			LOGGER.info("MultiCore loaded with 0 workers — behaving like vanilla.");
		}
	}

	private void onStartTick(MinecraftServer server) {
		metrics.startTick();
	}

	private void onEndTick(MinecraftServer server) {
		if (scheduler != null) {
			scheduler.drainToMain();
		}
		metrics.endTick();
	}

	private void onServerStopped(MinecraftServer server) {
		if (pool != null) {
			pool.shutdown();
		}
	}

	@Override
	public String mode() {
		return config == null ? ConfigMode.SAFE.name() : config.mode().name();
	}

	@Override
	public int workerThreads() {
		return config == null ? 0 : config.workerThreads();
	}

	@Override
	public int threadCount() {
		return config == null ? 1 : config.threadCount();
	}

	@Override
	public TickMetrics.Snapshot metrics() {
		return metrics.snapshot();
	}

	@Override
	public Map<String, Double> threadTimesMs() {
		return metrics.threadTimesMs();
	}
}
