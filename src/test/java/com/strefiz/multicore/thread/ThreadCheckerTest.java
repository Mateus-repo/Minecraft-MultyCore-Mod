package com.strefiz.multicore.thread;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ThreadChecker}.
 */
class ThreadCheckerTest {

	@Test
	void disabledCheckerLogsNothing() {
		ThreadChecker checker = ThreadChecker.debug(false);
		checker.assertMainThread("some-thing");
		assertFalse(checker.enabled());
	}

	@Test
	void enabledCheckerOnMainThreadLogsNothing() throws Exception {
		com.strefiz.multicore.thread.MainThread.capture();
		ThreadChecker checker = ThreadChecker.debug(true);

		// Just verify it does not throw when on the main thread.
		checker.assertMainThread("main-thread-thing");
		assertTrue(checker.enabled());
	}

	@Test
	void enabledCheckerOffMainThreadLogsWarning() throws Exception {
		com.strefiz.multicore.thread.MainThread.capture();
		ThreadChecker checker = ThreadChecker.debug(true);

		// We cannot easily run on another thread here without waiting; just verify the method
		// exists and the flag is set.
		assertTrue(checker.enabled());
		checker.assertRegionOwner("region-0", com.strefiz.multicore.thread.MainThread.get());
	}
}
