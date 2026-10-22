package com.strefiz.multicore.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ModCompatibility} override logic (the Fabric Loader scan itself is covered
 * by integration testing on a real server).
 */
class ModCompatibilityTest {

	@Test
	void defaultsToLegacyForUnknownMod() {
		ModCompatibility compat = new ModCompatibility();

		assertTrue(compat.isLegacy("unknown-mod"));
		assertFalse(compat.isCompatible("unknown-mod"));
	}

	@Test
	void forceCompatibleOverridesLegacyDefault() {
		ModCompatibility compat = new ModCompatibility();
		compat.forceCompatible("some-mod");

		assertTrue(compat.isCompatible("some-mod"));
		assertFalse(compat.isLegacy("some-mod"));
	}

	@Test
	void forceLegacyOverridesCompatibleDefault() {
		ModCompatibility compat = new ModCompatibility();
		compat.forceCompatible("some-mod");
		compat.forceLegacy("some-mod");

		assertTrue(compat.isLegacy("some-mod"));
		assertFalse(compat.isCompatible("some-mod"));
	}

	@Test
	void forceCompatibleClearsForceLegacy() {
		ModCompatibility compat = new ModCompatibility();
		compat.forceLegacy("some-mod");
		compat.forceCompatible("some-mod");

		assertTrue(compat.isCompatible("some-mod"));
		assertFalse(compat.isLegacy("some-mod"));
	}

	@Test
	void countsTrackClassifications() {
		ModCompatibility compat = new ModCompatibility();
		compat.classify("mod-a", ModClassification.COMPATIBLE);
		compat.classify("mod-b", ModClassification.COMPATIBLE);
		compat.classify("mod-c", ModClassification.LEGACY);

		assertEquals(3, compat.size());
		assertEquals(2, compat.compatibleCount());
		assertEquals(1, compat.legacyCount());
	}

	@Test
	void classificationsSnapshotIsUnmodifiable() {
		ModCompatibility compat = new ModCompatibility();
		compat.classify("mod-a", ModClassification.COMPATIBLE);

		Map<String, ModClassification> snapshot = compat.classifications();

		assertEquals(ModClassification.COMPATIBLE, snapshot.get("mod-a"));
		assertTrue(snapshot.isEmpty() == false);
		assertThrowsUnmodifiable(snapshot);
	}

	private static void assertThrowsUnmodifiable(Map<String, ModClassification> map) {
		try {
			map.put("extra", ModClassification.COMPATIBLE);
			throw new AssertionError("expected UnsupportedOperationException");
		} catch (UnsupportedOperationException expected) {
			// ok
		}
	}

	@Test
	void emptyCompatibilityHasZeroCounts() {
		ModCompatibility compat = new ModCompatibility();

		assertEquals(0, compat.size());
		assertEquals(0, compat.compatibleCount());
		assertEquals(0, compat.legacyCount());
	}
}
