package yield.output.file;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import yield.core.BaseControlQueueProvider;
import yield.core.EventListener;
import yield.input.ListenerExceutionFailed;

/**
 * Writes event stream to text file.
 * 
 * @param <Event>
 *            Type of incoming events.
 */
public class FileAppender<Event> extends BaseControlQueueProvider implements
		EventListener<Event> {
	private Path file;

	public FileAppender(Path file) {
		this.file = file;
	}

	@Override
	public void feed(Event e) {
		try (FileWriter writer = new FileWriter(file.toFile(), true)) {
			writer.write(e.toString() + System.lineSeparator());
		} catch (IOException e1) {
			this.getControlQueue().feed(
					new ListenerExceutionFailed<>(e,
							"Could not append to file.", e1));
		}
	}

}
