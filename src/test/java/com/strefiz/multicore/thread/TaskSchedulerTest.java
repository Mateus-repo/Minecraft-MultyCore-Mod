package com.strefiz.multicore.thread;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TaskScheduler}: offload, barrier, merge-on-main and failure handling.
 */
class TaskSchedulerTest {

	@Test
	void offloadedWorkRunsOnWorkerAndMergesOnMain() throws InterruptedException {
		MainThread.capture();
		WorkerPool pool = new WorkerPool("test", 1);
		pool.start();
		TaskScheduler scheduler = new TaskScheduler(pool);
		AtomicReference<Thread> workThread = new AtomicReference<>();
		AtomicReference<Thread> mergeThread = new AtomicReference<>();

		scheduler.submitOffload(() -> {
			workThread.set(Thread.currentThread());
			return 42;
		}, value -> mergeThread.set(Thread.currentThread()));

		assertEquals(1, scheduler.drainToMain());
		assertNotEquals(MainThread.get(), workThread.get(), "work must run on a worker");
		assertEquals(MainThread.get(), mergeThread.get(), "merge must run on the main thread");
		pool.shutdown();
	}

	@Test
	void mergesAreAppliedInSubmissionOrderEvenWhenWorkersFinishOutOfOrder() throws InterruptedException {
		MainThread.capture();
		WorkerPool pool = new WorkerPool("test", 4);
		pool.start();
		TaskScheduler scheduler = new TaskScheduler(pool);
		List<Integer> order = new ArrayList<>();
		CountDownLatch allDone = new CountDownLatch(5);

		for (int i = 0; i < 5; i++) {
			final int value = i;
			final int delayMillis = (5 - i) * 10; // task 0 finishes last
			scheduler.submitOffload(() -> {
				Thread.sleep(delayMillis);
				allDone.countDown();
				return value;
			}, order::add);
		}

		assertTrue(allDone.await(5, TimeUnit.SECONDS));
		assertEquals(5, scheduler.drainToMain());
		assertEquals(List.of(0, 1, 2, 3, 4), order, "merges must follow submission order, not completion order");
		pool.shutdown();
	}

	@Test
	void drainWaitsForSlowWork() throws InterruptedException {
		MainThread.capture();
		WorkerPool pool = new WorkerPool("test", 1);
		pool.start();
		TaskScheduler scheduler = new TaskScheduler(pool);
		AtomicBoolean merged = new AtomicBoolean(false);

		scheduler.submitOffload(() -> {
			Thread.sleep(60);
			return "done";
		}, value -> merged.set(true));

		assertEquals(1, scheduler.drainToMain(), "drain must wait for the slow task before merging");
		assertTrue(merged.get());
		pool.shutdown();
	}

	@Test
	void runsInlineWhenPoolIsDisabled() {
		MainThread.capture();
		TaskScheduler scheduler = new TaskScheduler(new WorkerPool("test", 0));
		List<String> events = new ArrayList<>();

		scheduler.submitOffload(() -> {
			events.add("work");
			return "result";
		}, value -> events.add("merge:" + value));

		assertEquals(List.of("work", "merge:result"), events, "disabled pool must run inline, in order");
	}

	@Test
	void workerFailureDoesNotMergeAndDoesNotPropagate() throws InterruptedException {
		MainThread.capture();
		WorkerPool pool = new WorkerPool("test", 1);
		pool.start();
		TaskScheduler scheduler = new TaskScheduler(pool);
		AtomicBoolean merged = new AtomicBoolean(false);
		CountDownLatch recovered = new CountDownLatch(1);

		scheduler.submitOffload(() -> {
			throw new IllegalStateException("boom");
		}, value -> merged.set(true));
		// Runs after the failing task (same single worker), so its handler has completed.
		scheduler.submit(recovered::countDown);

		assertTrue(recovered.await(5, TimeUnit.SECONDS));
		assertEquals(0, scheduler.drainToMain());
		assertFalse(merged.get());
		pool.shutdown();
	}

	@Test
	void drainIsIdempotent() {
		MainThread.capture();
		TaskScheduler scheduler = new TaskScheduler(new WorkerPool("test", 0));

		assertEquals(0, scheduler.drainToMain());
		assertFalse(scheduler.hasPending());
	}
}
