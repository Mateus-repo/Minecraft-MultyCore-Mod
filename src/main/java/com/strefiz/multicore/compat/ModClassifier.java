package com.strefiz.multicore.compat;

import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;

/**
 * Pure classification logic: given a {@link ModMetadata}, decides whether the mod is thread-safe.
 *
 * <p>No Fabric Loader or Minecraft class is referenced here except the metadata interfaces, so this
 * class is unit-testable without the game. The file lookup (jar path → mod) lives in
 * {@link ModCompatibility}.
 *
 * <p>A mod is {@link ModClassification#COMPATIBLE} only when
 * {@code "custom": { "multicore": { "threadSafe": true } }} is present in its {@code fabric.mod.json}.
 * Anything else (missing field, wrong type, {@code false}) is {@link ModClassification#LEGACY}.
 */
public final class ModClassifier {

	private static final String MULTICORE_KEY = "multicore";
	private static final String THREAD_SAFE_KEY = "threadSafe";

	private ModClassifier() {
	}

	/**
	 * Classifies a mod from its metadata.
	 *
	 * @param metadata the mod's {@code fabric.mod.json} metadata, never {@code null}
	 * @return {@link ModClassification#COMPATIBLE} when {@code threadSafe} is {@code true},
	 *     {@link ModClassification#LEGACY} otherwise
	 */
	public static ModClassification classify(ModMetadata metadata) {
		if (metadata == null) {
			return ModClassification.LEGACY;
		}
		if (!metadata.containsCustomValue(MULTICORE_KEY)) {
			return ModClassification.LEGACY;
		}
		CustomValue value = metadata.getCustomValue(MULTICORE_KEY);
		if (value == null || value.getType() != CustomValue.CvType.OBJECT) {
			return ModClassification.LEGACY;
		}
		CustomValue.CvObject object = value.getAsObject();
		if (object == null || !object.containsKey(THREAD_SAFE_KEY)) {
			return ModClassification.LEGACY;
		}
		CustomValue threadSafe = object.get(THREAD_SAFE_KEY);
		if (threadSafe == null || threadSafe.getType() != CustomValue.CvType.BOOLEAN) {
			return ModClassification.LEGACY;
		}
		return threadSafe.getAsBoolean() ? ModClassification.COMPATIBLE : ModClassification.LEGACY;
	}
}
