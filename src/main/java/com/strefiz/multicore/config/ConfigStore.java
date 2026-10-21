package com.strefiz.multicore.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads and persists {@link MultiCoreConfig} as a JSON file in the Minecraft {@code config/} folder.
 *
 * <p>The file location is injectable through the constructor so the codec can be unit-tested with a
 * temporary path; {@link #defaultStore()} resolves the real location through Fabric Loader.
 *
 * <p>Read errors (missing file, malformed JSON, wrong types) never throw: the defaults are used and
 * written back so the user always gets a valid file.
 */
public final class ConfigStore {

	/** Name of the configuration file inside the Minecraft {@code config/} folder. */
	public static final String FILE_NAME = "multicore.json";

	private static final Logger LOGGER = LoggerFactory.getLogger("multicore/config");

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final Path file;

	/**
	 * Creates a store bound to an explicit file.
	 *
	 * @param file the JSON file to read/write
	 */
	public ConfigStore(Path file) {
		this.file = file;
	}

	/**
	 * Creates the store used by the game, pointing at {@code config/multicore.json}.
	 *
	 * @return a store bound to the Fabric config directory
	 */
	public static ConfigStore defaultStore() {
		return new ConfigStore(FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME));
	}

	/** @return the file this store reads and writes. */
	public Path file() {
		return file;
	}

	/**
	 * Loads the configuration, falling back to defaults on any problem, and persists the normalized
	 * result back to disk.
	 *
	 * @return the loaded (and normalized) configuration, never {@code null}
	 */
	public MultiCoreConfig load() {
		if (!Files.exists(file)) {
			MultiCoreConfig defaults = new MultiCoreConfig().normalized();
			save(defaults);
			return defaults;
		}
		try {
			String json = Files.readString(file, StandardCharsets.UTF_8);
			MultiCoreConfig loaded = gson.fromJson(json, MultiCoreConfig.class);
			MultiCoreConfig config = (loaded == null ? new MultiCoreConfig() : loaded).normalized();
			save(config);
			return config;
		} catch (IOException | RuntimeException e) {
			LOGGER.warn("Could not read {} ({}); using defaults", file, e.toString());
			MultiCoreConfig defaults = new MultiCoreConfig().normalized();
			save(defaults);
			return defaults;
		}
	}

	/**
	 * Persists the configuration, creating the parent directory when needed.
	 *
	 * @param config the configuration to write
	 */
	public void save(MultiCoreConfig config) {
		try {
			Path parent = file.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.writeString(file, gson.toJson(config.normalized()), StandardCharsets.UTF_8);
		} catch (IOException | RuntimeException e) {
			LOGGER.warn("Could not write {} ({})", file, e.toString());
		}
	}
}
