package com.strefiz.multicore.thread;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds the mod's offloaded per-tick work and submits it to the {@link TaskScheduler}.
 *
 * <p>Every offload declares a {@link Safety} level. {@link #submitAll(Safety)} only runs offloads at
 * or below the given maximum, so a SAFE-mode server can never run a task that touches world state.
 * The actual compute happens on a worker; the {@code merge} consumer runs on the main thread.
 */
public final class OffloadRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger("multicore/offload");

	private final TaskScheduler scheduler;
	private final List<Entry<?>> entries = new CopyOnWriteArrayList<>();

	/**
	 * Creates an empty registry.
	 *
	 * @param scheduler the scheduler that runs the offloaded work
	 */
	public OffloadRegistry(TaskScheduler scheduler) {
		this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
	}

	/**
	 * Registers an offload.
	 *
	 * @param name a stable name, shown in {@code /multicore status}
	 * @param safety how much of the world the compute step may touch
	 * @param work the compute step (worker thread)
	 * @param merge the merge step (main thread)
	 * @param <T> the result type
	 */
	public <T> void register(String name, Safety safety, Callable<T> work, Consumer<T> merge) {
		entries.add(new Entry<>(name, safety, work, merge));
	}

	/**
	 * Submits every registered offload whose safety is at or below {@code maxAllowed}.
	 *
	 * @param maxAllowed the highest safety level permitted in the current mode
	 */
	public void submitAll(Safety maxAllowed) {
		for (Entry<?> entry : entries) {
			if (entry.safety.ordinal() > maxAllowed.ordinal()) {
				LOGGER.debug("Skipping offload '{}' ({} > allowed {})", entry.name, entry.safety, maxAllowed);
				continue;
			}
			entry.submit();
		}
	}

	/** @return the registered offload names, in registration order. */
	public List<String> names() {
		return entries.stream().map(entry -> entry.name).toList();
	}

	/** @return how many offloads are registered. */
	public int size() {
		return entries.size();
	}

	private final class Entry<T> {

		private final String name;
		private final Safety safety;
		private final Callable<T> work;
		private final Consumer<T> merge;

		private Entry(String name, Safety safety, Callable<T> work, Consumer<T> merge) {
			this.name = name;
			this.safety = safety;
			this.work = work;
			this.merge = merge;
		}

		private void submit() {
			scheduler.submitOffload(work, merge, DeadlinePolicy.forSafety(safety));
		}
	}
}
