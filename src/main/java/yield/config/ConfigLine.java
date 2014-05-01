package yield.config;

import java.nio.file.Path;

/**
 * Single line in a config file.
 */
public class ConfigLine {
	/**
	 * Content of the line.
	 */
	public final String lineContent;

	/**
	 * Config file location.
	 */
	public final Path file;

	/**
	 * Location within config file.
	 */
	public final int lineNumber;

	public ConfigLine(String lineContent, Path file, int lineNumber) {
		this.lineContent = lineContent;
		this.file = file;
		this.lineNumber = lineNumber;
	}

	@Override
	public String toString() {
		return file + ":" + lineNumber;
	}
}
