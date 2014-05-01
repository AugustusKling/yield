package yield.output.file;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import yield.core.EventListener;
import yield.core.Main;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Writes event stream to text file.
 * 
 * @param <Event>
 *            Type of incoming events.
 */
public class FileAppender<Event> implements EventListener<Event> {
	private Path file;

	public FileAppender(Main main, ObjectNode config) {
		this.file = Paths.get(config.get("file").textValue());
	}

	public FileAppender(Path file) {
		this.file = file;
	}

	@Override
	public void feed(Event e) {
		try (FileWriter writer = new FileWriter(file.toFile(), true)) {
			writer.write(e.toString() + System.lineSeparator());
		} catch (IOException e1) {
			throw new RuntimeException("Could not append to file.", e1);
		}
	}

}
