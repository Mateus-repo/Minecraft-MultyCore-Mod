package com.strefiz.multicore.command;

import java.util.Locale;
import java.util.Map;

/**
 * Pure formatting of the {@code /multicore} command output.
 *
 * <p>Kept separate from Brigadier so the text can be unit-tested without a game. The messages are
 * plain strings built with {@link String#format(Locale, String, Object...)} using
 * {@link Locale#ROOT} so the output never depends on the server's locale (decimal separators!).
 */
public final class StatusFormatter {

	private static final String RESTART_WARNING =
			"Changes to threads/mode/regions only apply after a restart.";

	private StatusFormatter() {
	}

	/**
	 * Formats the {@code /multicore status} output.
	 *
	 * @param source the runtime state to describe
	 * @return the multi-line status text
	 */
	public static String status(StatusSource source) {
		StringBuilder text = new StringBuilder();
		text.append("MultiCore: mode=").append(source.mode())
				.append(", workers=").append(source.workerThreads())
				.append(", threads=").append(source.threadCount())
				.append(", ticks=").append(source.metrics().tickCount());
		text.append(String.format(Locale.ROOT, "%n  MSPT last=%.2f avg=%.2f max=%.2f",
				source.metrics().lastMspt(), source.metrics().averageMspt(), source.metrics().maxMspt()));

		Map<String, Double> threadTimes = source.threadTimesMs();
		if (threadTimes.isEmpty()) {
			text.append("\n  per-thread: (no work recorded yet)");
		} else {
			text.append("\n  per-thread:");
			for (Map.Entry<String, Double> entry : threadTimes.entrySet()) {
				text.append(String.format(Locale.ROOT, "%n    %s: %.2f ms", entry.getKey(), entry.getValue()));
			}
		}

		String rollup = source.metricsRollup();
		if (rollup != null && !rollup.isBlank()) {
			text.append("\n  rollup: ").append(rollup);
		}
		return text.toString();
	}

	/**
	 * Formats the {@code /multicore compat} output.
	 *
	 * @param summary the compatibility summary (empty when Phase 2 is not implemented)
	 * @return the text to show
	 */
	public static String compat(String summary) {
		if (summary == null || summary.isBlank()) {
			return "MultiCore: compatibility layer not active yet (Phase 2).";
		}
		return "MultiCore compatibility:\n" + summary;
	}

	/**
	 * Formats the {@code /multicore compat dump} output.
	 *
	 * @param dump the compatibility dump (empty when Phase 2 is not implemented)
	 * @return the text to show
	 */
	public static String compatDump(String dump) {
		if (dump == null || dump.isBlank()) {
			return "MultiCore: compatibility layer not active yet (Phase 2).";
		}
		return dump;
	}

	/**
	 * Formats the {@code /multicore regions} output.
	 *
	 * @param summary the region summary (empty when Phase 3 is not implemented)
	 * @return the text to show
	 */
	public static String regions(String summary) {
		if (summary == null || summary.isBlank()) {
			return "MultiCore: regionised tick not active yet (Phase 3).";
		}
		return "MultiCore regions:\n" + summary;
	}

	/**
	 * @return the message shown when a reload is requested.
	 */
	public static String reloadWarning() {
		return "MultiCore: configuration saved. " + RESTART_WARNING;
	}

	/**
	 * @return the message shown when the mod is running single-threaded.
	 */
	public static String singleThreaded() {
		return "MultiCore: 0 workers configured — behaving exactly like vanilla.";
	}
}
