package com.strefiz.multicore.region;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RegionSection}.
 */
class RegionSectionTest {

	@Test
	void fromChunkConvertsByShift() {
		// chunkShift = 4 => 16x16 chunk sections
		assertEquals(new RegionSection(0, 0), RegionSection.fromChunk(0, 0, 4));
		assertEquals(new RegionSection(0, 0), RegionSection.fromChunk(15, 15, 4));
		assertEquals(new RegionSection(1, 0), RegionSection.fromChunk(16, 0, 4));
		assertEquals(new RegionSection(1, -1), RegionSection.fromChunk(16, -16, 4));
		assertEquals(new RegionSection(-1, 0), RegionSection.fromChunk(-1, 0, 4));
		assertEquals(new RegionSection(-1, -1), RegionSection.fromChunk(-16, -1, 4));
	}

	@Test
	void equalsAndHashCode() {
		assertEquals(new RegionSection(5, 3), new RegionSection(5, 3));
		assertNotEquals(new RegionSection(5, 3), new RegionSection(5, 4));
		assertEquals(new RegionSection(5, 3).hashCode(), new RegionSection(5, 3).hashCode());
	}

	@Test
	void toStringContainsCoords() {
		assertEquals("RegionSection(7, 9)", new RegionSection(7, 9).toString());
	}
}
