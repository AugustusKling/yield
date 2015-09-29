package yield.output.shell;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.annotation.Nonnull;

import yield.core.EventListener;
import yield.core.EventSource;
import yield.core.EventType;
import yield.core.event.FailureEvent;
import yield.core.event.MetaEvent;
import yield.core.event.SuccessEvent;
import yield.input.ListenerExecutionAborted;
import yield.json.JsonEvent;

/**
 * Builds a shell command and executes it asynchronously.
 * <p>
 * The command is build by pasting fields from the event into the provided
 * command template.
 */
public class RunCommand extends EventSource<MetaEvent<String>> implements
		EventListener<JsonEvent> {
	/**
	 * Working directory.
	 */
	private Path directory;

	/**
	 * Command and arguments. Patterns like <code>${fieldName}</code> will be
	 * replaced with the value of the {@code fieldName} property.
	 */
	private List<String> commandTemplate;

	public RunCommand(Path directory, List<String> commandTemplate) {
		this.directory = directory;
		this.commandTemplate = commandTemplate;
	}

	@Override
	public void feed(JsonEvent event) {
		List<String> command = getCommand(event);
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.directory(directory.toFile());
		try {
			Process process = pb.start();
			try (Scanner stdOut = new Scanner(process.getInputStream());
					Scanner stdErr = new Scanner(process.getErrorStream())) {
				if (process.waitFor() != 0) {
					String processOutput = stdErr.useDelimiter("\\A").hasNext() ? stdErr
							.next() : "";
					this.feedBoundQueues(new FailureEvent<String>(
							new RuntimeException("Shell command " + command
									+ " reported error code "
									+ process.exitValue() + "\n"
									+ processOutput)));
				} else {
					String processOutput = stdOut.useDelimiter("\\A").hasNext() ? stdOut
							.next() : "";
					this.feedBoundQueues(new SuccessEvent<String>(processOutput));
				}
			}
		} catch (IOException e) {
			feedBoundQueues(new FailureEvent<String>(new Exception(
					"Cannot execute", e)));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			this.getControlQueue().feed(new ListenerExecutionAborted());
		}
	}

	/**
	 * Fills placeholders in template with data from event.
	 * 
	 * @param e
	 *            Data for placeholders.
	 * @return Shell command and its arguments.
	 */
	private List<String> getCommand(JsonEvent e) {
		ArrayList<String> command = new ArrayList<>(commandTemplate.size());
		for (String templateElement : commandTemplate) {
			String fragment = templateElement;
			Iterator<String> fields = e.getObject().fieldNames();
			while (fields.hasNext()) {
				String fieldName = fields.next();
				fragment = fragment.replace("${" + fieldName + "}",
						e.get(fieldName));
			}
			command.add(fragment);
		}
		return command;
	}

	@Override
	@Nonnull
	public EventType getInputType() {
		return new EventType(JsonEvent.class);
	}

}
