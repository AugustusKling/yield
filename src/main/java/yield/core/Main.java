package yield.core;

import java.io.IOException;
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
		} else {
			new Main(Paths.get(args[0]));
		}
	}

}
