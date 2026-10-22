package com.strefiz.multicore.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link LegacyHandoff}.
 */
class LegacyHandoffTest {

	@Test
	void enqueuedCallbacksRunOnMainThreadInOrder() throws InterruptedException {
		com.strefiz.multicore.thread.MainThread.capture();
		LegacyHandoff handoff = LegacyHandoff.create();
		List<Integer> order = new ArrayList<>();

		for (int i = 0; i < 3; i++) {
			final int value = i;
			handoff.enqueue(() -> order.add(value));
		}

		assertEquals(3, handoff.drainToMain());
		assertEquals(List.of(0, 1, 2), order);
		assertFalse(handoff.hasPending());
	}

	@Test
	void drainIsIdempotentAndReturnsZeroWhenEmpty() {
		com.strefiz.multicore.thread.MainThread.capture();
		LegacyHandoff handoff = LegacyHandoff.create();

		assertEquals(0, handoff.drainToMain());
		assertFalse(handoff.hasPending());
	}

	@Test
	void callbackExceptionDoesNotPropagate() {
		com.strefiz.multicore.thread.MainThread.capture();
		LegacyHandoff handoff = LegacyHandoff.create();
		AtomicReference<Throwable> caught = new AtomicReference<>();

		handoff.enqueue(() -> {
			throw new IllegalStateException("boom");
		});
		handoff.enqueue(() -> caught.set(new IllegalStateException("applied")));

		int applied = handoff.drainToMain();

		assertEquals(1, applied); // only the second callback ran successfully
		assertTrue(caught.get() instanceof IllegalStateException);
		assertEquals("applied", caught.get().getMessage());
	}
}
