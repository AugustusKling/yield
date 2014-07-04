package yield.input.shipper;

import java.nio.file.Path;

import yield.input.ControlEvent;

/**
 * Signals that a file cannot be currently watched directly. The path to the
 * file is monitored however and events are yielded as soon as the file is
 * created.
 */
public class NoSuchFile implements ControlEvent {

	public Path path;

	public NoSuchFile(Path path) {
		this.path = path;
	}

}
