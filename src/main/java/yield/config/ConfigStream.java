package yield.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

/**
 * Makes config file contents available as stream.
 */
public class ConfigStream implements Iterable<ConfigLine> {
	private Path configFile;

	public ConfigStream(Path configFile) {
		this.configFile = configFile;
	}

	@Override
	public Iterator<ConfigLine> iterator() {
		final List<String> lines;
		try {
			lines = Files.readAllLines(configFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new Iterator<ConfigLine>() {
			int lastEmittedLine = 0;

			@Override
			public boolean hasNext() {
				return lastEmittedLine < lines.size();
			}

			@Override
			public ConfigLine next() {
				String lineContent = lines.get(lastEmittedLine);
				lastEmittedLine = lastEmittedLine + 1;
				return new ConfigLine(lineContent, configFile, lastEmittedLine);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
