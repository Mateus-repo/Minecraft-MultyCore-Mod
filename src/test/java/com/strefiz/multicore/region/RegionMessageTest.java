package com.strefiz.multicore.region;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RegionMessage}.
 */
class RegionMessageTest {

	@Test
	void messageHasSourceTargetAndPayload() {
		RegionSection source = new RegionSection(0, 0);
		RegionSection target = new RegionSection(1, 0);
		String payload = "hello";

		RegionMessage<String> msg = new RegionMessage<>(source, target, payload, null);

		assertEquals(source, msg.sourceSection());
		assertEquals(target, msg.targetSection());
		assertEquals(payload, msg.payload());
	}

	@Test
	void deliverRunsCallback() {
		RegionMessage<String> msg = new RegionMessage<>(new RegionSection(0, 0),
				new RegionSection(1, 0), "x", () -> System.out.println("delivered"));

		msg.deliver(); // should not throw
		assertTrue(true);
	}

	@Test
	void toStringContainsSections() {
		RegionMessage<String> msg = new RegionMessage<>(new RegionSection(0, 0),
				new RegionSection(1, 0), "x", null);

		assertTrue(msg.toString().contains("RegionSection(0, 0)"));
		assertTrue(msg.toString().contains("RegionSection(1, 0)"));
	}
}
