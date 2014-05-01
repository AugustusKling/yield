package yield.output.shell;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import yield.core.EventListener;
import yield.core.Main;
import yield.json.JsonEvent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Builds a shell command and executes it asynchronously.
 * <p>
 * The command is build by pasting fields from the event into the provided
 * command template.
 */
public class RunCommand implements EventListener<JsonEvent> {
	/**
	 * Working directory.
	 */
	private Path directory;

	/**
	 * Command and arguments. Patterns like <code>${fieldName}</code> will be
	 * replaced with the value of the {@code fieldName} property.
	 */
	private ArrayList<String> commandTemplate;

	public RunCommand(Main main, ObjectNode config) {
		directory = Paths.get(config.get("directory").textValue());
		commandTemplate = new ArrayList<>();
		Iterator<JsonNode> elements = config.get("commandTemplate").elements();
		while (elements.hasNext()) {
			commandTemplate.add(elements.next().textValue());
		}
	}

	@Override
	public void feed(JsonEvent e) {
		ProcessBuilder pb = new ProcessBuilder(getCommand(e));
		pb.directory(directory.toFile());
		try {
			pb.start();
		} catch (IOException e1) {
			e1.printStackTrace();
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

}
