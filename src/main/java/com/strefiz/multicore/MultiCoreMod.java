package com.strefiz.multicore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.DedicatedServerModInitializer;

/**
 * Entry point of the MultiCore server mod.
 *
 * <p>Bootstraps the configuration, the worker pool, the compatibility layer and the
 * {@code /multicore} command. The heavy wiring lives in the modules; this class only owns the
 * lifecycle so it stays easy to read.
 */
public class MultiCoreMod implements DedicatedServerModInitializer {

	/** Mod id, used for logging and config lookup. */
	public static final String MOD_ID = "multicore";

	/** Logger for the whole mod. */
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeServer() {
		LOGGER.info("MultiCore initialising (server)...");
		// Phase 1 wiring goes here: MultiCoreConfig.load(), WorkerPool, TaskScheduler,
		// ModCompatibility, MultiCoreCommand.
	}
}
