package yield.input.file;

import java.nio.file.Path;

import yield.input.ControlEvent;

/**
 * Signals a has completely be read.
 */
public class FileCompletelyRead implements ControlEvent {

	public Path path;

	public FileCompletelyRead(Path path) {
		this.path = path;
	}

}
