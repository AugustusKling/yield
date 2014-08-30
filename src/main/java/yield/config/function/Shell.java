package yield.config.function;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.ShortDocumentation;
import yield.config.TypedYielder;
import yield.core.BaseControlQueueProvider;
import yield.core.EventListener;
import yield.core.EventQueue;
import yield.core.Yielder;
import yield.core.event.MetaEvent;
import yield.json.JsonEvent;
import yield.output.shell.RunCommand;

@ShortDocumentation(text = "Runs shell command. Fields from JSON serve as command's placeholder values.")
public class Shell extends FunctionConfig {
	/**
	 * Creates a {@link RunCommand} which is used only once so that input event
	 * can be merged with command execution output.
	 */
	private static class CommandExecutor extends BaseControlQueueProvider
			implements EventListener<MetaEvent<String>> {
		private ShellQueue shellQueue;
		private JsonEvent input;

		public CommandExecutor(ShellQueue shellQueue, JsonEvent input) {
			this.shellQueue = shellQueue;
			this.input = input;
		}

		public void run(Path workingDirectory, ArrayList<String> commandTemplate) {
			RunCommand runCommand = new RunCommand(workingDirectory,
					commandTemplate);
			runCommand.bind(this);
			runCommand.feed(input);
		}

		@Override
		public void feed(MetaEvent<String> e) {
			JsonEvent output = new JsonEvent(this.input);
			try {
				String stdout = e.get();
				output.put("type", "success");
				output.put("stdout", stdout);
			} catch (Exception ex) {
				output.put("type", "error");
				output.put("stderr", ex.getMessage());
			}
			this.shellQueue.feedBoundQueues(output);
		}
	}

	/**
	 * Takes jobs to execute, yields a result per executed command.
	 */
	private static class ShellQueue extends EventQueue<JsonEvent> {
		private Path workingDirectory;
		private ArrayList<String> commandTemplate;

		public ShellQueue(Path workingDirectory,
				ArrayList<String> commandTemplate) {
			this.workingDirectory = workingDirectory;
			this.commandTemplate = commandTemplate;
		}

		@Override
		public void feed(JsonEvent logEvent) {
			CommandExecutor exec = new CommandExecutor(this, logEvent);
			exec.run(workingDirectory, commandTemplate);
		}

		@Override
		public void feedBoundQueues(JsonEvent logEvent) {
			super.feedBoundQueues(logEvent);
		}
	}

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		Yielder<JsonEvent> yielder = getYielderTypesafe(JsonEvent.class,
				ConfigReader.LAST_SOURCE, context);
		String[] dirAndCommand = args.split(" ", 2);
		if (dirAndCommand.length < 2) {
			throw new IllegalArgumentException(
					"Working directory and command template expected.");
		}
		ArrayList<String> commandTemplate = new ArrayList<>();
		for (String commandFragment : dirAndCommand[1].split(" ")) {
			commandTemplate.add(commandFragment);
		}

		ShellQueue shellQueue = new ShellQueue(Paths.get(dirAndCommand[0]),
				commandTemplate);
		yielder.bind(shellQueue);

		return wrapResultingYielder(shellQueue);
	}

	@Override
	protected String getResultEventType() {
		return JsonEvent.class.getName();
	}
}
