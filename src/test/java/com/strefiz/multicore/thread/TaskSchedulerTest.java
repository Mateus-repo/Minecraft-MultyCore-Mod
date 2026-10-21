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
 * Unit tests for {@link TaskScheduler}: offload, merge-on-main and failure handling.
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
		CountDownLatch merged = new CountDownLatch(1);

		scheduler.submitOffload(() -> {
			workThread.set(Thread.currentThread());
			return 42;
		}, value -> {
			mergeThread.set(Thread.currentThread());
			merged.countDown();
		});

		awaitPending(scheduler, 1);
		assertEquals(1, scheduler.drainToMain());
		assertTrue(merged.await(1, TimeUnit.SECONDS), "merge should run during drain");
		assertNotEquals(MainThread.get(), workThread.get(), "work must run on a worker");
		assertEquals(MainThread.get(), mergeThread.get(), "merge must run on the main thread");
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
	void mergesAreAppliedInQueueOrder() throws InterruptedException {
		MainThread.capture();
		WorkerPool pool = new WorkerPool("test", 1); // single worker => deterministic completion order
		pool.start();
		TaskScheduler scheduler = new TaskScheduler(pool);
		List<Integer> order = new ArrayList<>();
		CountDownLatch workDone = new CountDownLatch(3);

		for (int i = 0; i < 3; i++) {
			final int value = i;
			scheduler.submitOffload(() -> {
				workDone.countDown();
				return value;
			}, order::add);
		}

		assertTrue(workDone.await(5, TimeUnit.SECONDS));
		awaitPending(scheduler, 3);
		assertEquals(3, scheduler.drainToMain());
		assertEquals(List.of(0, 1, 2), order);
		pool.shutdown();
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
		// Runs after the failing task (same single worker), so the catch block has completed.
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

	private static void awaitPending(TaskScheduler scheduler, int expected) throws InterruptedException {
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
		while (scheduler.pendingCount() < expected && System.nanoTime() < deadline) {
			Thread.sleep(10);
		}
	}
}
