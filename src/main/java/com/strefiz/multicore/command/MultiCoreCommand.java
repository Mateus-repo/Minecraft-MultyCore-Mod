package com.strefiz.multicore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Registers the {@code /multicore} command tree.
 *
 * <p>The command is a thin shell: all text comes from {@link StatusFormatter} so it stays testable,
 * and all state comes from {@link StatusSource} so it stays decoupled from the modules.
 */
public final class MultiCoreCommand {

	private MultiCoreCommand() {
	}

	/**
	 * Adds the {@code /multicore} command and its subcommands to a dispatcher.
	 *
	 * @param dispatcher the server command dispatcher
	 * @param source the runtime state to report
	 */
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, StatusSource source) {
		LiteralArgumentBuilder<CommandSourceStack> compat = Commands.literal("compat")
				.executes(context -> send(context.getSource(), StatusFormatter.compat(source.compatSummary())))
				.then(Commands.literal("dump")
						.executes(context -> send(context.getSource(),
								StatusFormatter.compatDump(source.compatDump()))));

		LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("multicore")
				.then(Commands.literal("status")
						.executes(context -> send(context.getSource(), StatusFormatter.status(source))))
				.then(compat)
				.then(Commands.literal("regions")
						.executes(context -> send(context.getSource(), StatusFormatter.regions(source.regionSummary()))))
				.then(Commands.literal("reload")
						.executes(context -> send(context.getSource(), StatusFormatter.reloadWarning())));
		dispatcher.register(root);
	}

	/**
	 * Registers the command through the Fabric command callback.
	 *
	 * @param source the runtime state to report
	 */
	public static void registerEvents(StatusSource source) {
		CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) ->
				register(dispatcher, source));
	}

	private static int send(CommandSourceStack source, String text) {
		for (String line : text.split("\n", -1)) {
			source.sendSystemMessage(Component.literal(line));
		}
		return 1;
	}
}
