package yield.input.file;

import java.nio.file.Path;

/**
 * Events that might occur during monitoring a path.
 */
public interface FileModificationListener {
	/**
	 * A new line was encountered.
	 * 
	 * @param path
	 *            Monitored path.
	 * @param lineContent
	 *            Content of encountered line.
	 */
	public void lineAdded(Path path, String lineContent);

	/**
	 * File was completely processed.
	 * 
	 * @param path
	 *            Monitored path.
	 */
	public void completelyRead(Path path);

	/**
	 * No file at path. No file means not having a regular files whose contents
	 * could be read.
	 * 
	 * @param path
	 *            Monitored path.
	 */
	public void noSuchFile(Path path);

	/**
	 * File is shorter than before.
	 * 
	 * @param path
	 *            Monitored path.
	 */
	public void fileRotated(Path path);
}
