package com.strefiz.multicore.region;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RegionMessageQueue}.
 */
class RegionMessageQueueTest {

	@Test
	void emptyQueueHasNoPending() {
		RegionMessageQueue queue = RegionMessageQueue.create();

		assertFalse(queue.hasPending());
		assertEquals(0, queue.pendingCount());
	}

	@Test
	void enqueueAndDrainDeliversMessages() {
		RegionMessageQueue queue = RegionMessageQueue.create();
		List<String> delivered = new ArrayList<>();

		queue.enqueue(new RegionMessage<>(new RegionSection(0, 0), new RegionSection(1, 0), "a",
				() -> delivered.add("a")));
		queue.enqueue(new RegionMessage<>(new RegionSection(1, 0), new RegionSection(0, 0), "b",
				() -> delivered.add("b")));

		assertTrue(queue.hasPending());
		assertEquals(2, queue.pendingCount());

		int deliveredCount = queue.drain();

		assertEquals(2, deliveredCount);
		assertEquals(List.of("a", "b"), delivered);
		assertFalse(queue.hasPending());
		assertEquals(0, queue.pendingCount());
	}

	@Test
	void drainOnEmptyQueueReturnsZero() {
		RegionMessageQueue queue = RegionMessageQueue.create();

		assertEquals(0, queue.drain());
		assertFalse(queue.hasPending());
	}
}
