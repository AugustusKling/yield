package yield.input.file;

import java.nio.file.Path;

import yield.input.ControlEvent;

/**
 * Means the contents of a watched file were overwritten. Not triggered by
 * appending to a file.
 */
public class FileRotated implements ControlEvent {

	public Path path;

	public FileRotated(Path path) {
		this.path = path;
	}

}
