package yield.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import yield.config.ConfigReader;
import yield.config.ConfigStream;
import yield.config.TypedYielder;

public class Main {

	/**
	 * Instantiates queues from configuration.
	 * 
	 * @param configFile
	 *            Configuration file.
	 */
	public Main(Path configFile) throws IOException {
		ConfigReader configReader = new ConfigReader();
		configReader.toQueues(new HashMap<String, TypedYielder>(),
				new ConfigStream(configFile));
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("Usage: java -jar yield.jar config.yield");
			System.out.println();
			// Print available functions.
			System.out
					.println("The following functions can be used in the config file.");
			System.out.println();
			Path helpFile = Files.createTempFile(null, null);
			Files.write(helpFile, ":functions".getBytes(StandardCharsets.UTF_8));
			new Main(helpFile);
			Files.deleteIfExists(helpFile);
		} else {
			new Main(Paths.get(args[0]));
		}
	}

}
