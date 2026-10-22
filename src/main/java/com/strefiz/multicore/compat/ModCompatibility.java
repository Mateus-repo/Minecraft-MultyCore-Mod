package com.strefiz.multicore.compat;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

/**
 * Scans the loaded mods at server start and classifies each one as legacy or compatible.
 *
 * <p>Honours the config's whitelist/blacklist: a mod on the blacklist is always legacy, and (in
 * AGGRESSIVE mode) only whitelisted compatible mods may use region threads. The result is a read-only
 * snapshot applied once at {@code SERVER_STARTING}.
 */
public final class ModCompatibility {

	private final Map<String, ModClassification> classifications = new TreeMap<>();
	private final Map<String, Boolean> forcedLegacy = new TreeMap<>();
	private final Map<String, Boolean> forcedCompatible = new TreeMap<>();

	public ModCompatibility() {
	}

	/**
	 * Scans all loaded mods and classifies them.
	 *
	 * @return a populated compatibility instance
	 */
	public static ModCompatibility scan() {
		ModCompatibility result = new ModCompatibility();
		Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();
		for (ModContainer mod : mods) {
			ModMetadata metadata = mod.getMetadata();
			String id = metadata.getId();
			result.classifications.put(id, ModClassifier.classify(metadata));
		}
		return result;
	}

	/**
	 * Marks a mod as always legacy, overriding its own declaration.
	 *
	 * @param modId the mod id
	 */
	public void forceLegacy(String modId) {
		forcedLegacy.put(modId, true);
		forcedCompatible.remove(modId);
	}

	/**
	 * Marks a mod as compatible, overriding its own declaration (whitelist in AGGRESSIVE mode).
	 *
	 * @param modId the mod id
	 */
	public void forceCompatible(String modId) {
		forcedCompatible.put(modId, true);
		forcedLegacy.remove(modId);
	}

	/**
	 * Records a classification for a mod (used by tests and by future programmatic registration).
	 *
	 * @param modId the mod id
	 * @param classification the classification to record
	 */
	public void classify(String modId, ModClassification classification) {
		classifications.put(modId, classification);
	}

	/**
	 * @return {@code true} when the mod must run on the main thread.
	 */
	public boolean isLegacy(String modId) {
		if (forcedLegacy.containsKey(modId)) {
			return true;
		}
		if (forcedCompatible.containsKey(modId)) {
			return false;
		}
		return classifications.getOrDefault(modId, ModClassification.LEGACY) == ModClassification.LEGACY;
	}

	/**
	 * @return {@code true} when the mod may run on region threads (AGGRESSIVE mode only).
	 */
	public boolean isCompatible(String modId) {
		return !isLegacy(modId);
	}

	/** @return an unmodifiable copy of the mod → classification map. */
	public Map<String, ModClassification> classifications() {
		return Map.copyOf(classifications);
	}

	/** @return the number of mods classified. */
	public int size() {
		return classifications.size();
	}

	/** @return how many mods are classified as compatible. */
	public int compatibleCount() {
		int count = 0;
		for (ModClassification c : classifications.values()) {
			if (c == ModClassification.COMPATIBLE) {
				count++;
			}
		}
		return count;
	}

	/** @return how many mods are classified as legacy. */
	public int legacyCount() {
		return size() - compatibleCount();
	}
}
