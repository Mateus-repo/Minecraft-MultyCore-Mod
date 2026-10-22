package com.strefiz.multicore.compat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MultiCoreCompat} with a fake handler.
 */
class MultiCoreCompatTest {

	private static final class FakeHandler implements MultiCoreCompat.Handler {
		boolean runOnMainCalled;
		boolean threadSafe;

		@Override
		public void runOnMain(Runnable callback) {
			runOnMainCalled = true;
			callback.run();
		}

		@Override
		public boolean isThreadSafe() {
			return threadSafe;
		}
	}

	@Test
	void runOnMainDelegatesToHandler() {
		FakeHandler handler = new FakeHandler();
		MultiCoreCompat.setHandler(handler);
		MultiCoreCompat.runOnMain(() -> { });

		assertTrue(handler.runOnMainCalled);
		MultiCoreCompat.setHandler(null);
	}

	@Test
	void isThreadSafeDelegatesToHandler() {
		MultiCoreCompat.setHandler(null);
		assertFalse(MultiCoreCompat.isThreadSafe());

		FakeHandler handler = new FakeHandler();
		handler.threadSafe = true;
		MultiCoreCompat.setHandler(handler);
		assertTrue(MultiCoreCompat.isThreadSafe());
		MultiCoreCompat.setHandler(null);
	}
}
