package yield.config;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.function.Combine;
import yield.config.function.Listen;
import yield.config.function.Mutate;
import yield.config.function.Print;
import yield.config.function.ReadFile;
import yield.config.function.Save;
import yield.config.function.Shell;
import yield.config.function.ToJson;
import yield.config.function.ToText;
import yield.config.function.Union;
import yield.config.function.Where;
import yield.core.EventListener;
import yield.core.EventQueue;

/**
 * Parses configuration, creates queues.
 */
public class ConfigReader {
	/**
	 * Key for yielder of most recently encountered function.
	 */
	public static final String LAST_SOURCE = "previous";

	private static final TypedYielder dummyYielder = new TypedYielder(
			Void.class.getName(), new EventQueue<Object>() {
				@Override
				public void bind(EventListener<Object> listener) {
					throw new RuntimeException(
							"Trying to bind to a never yielding queue.");
				}
			});

	/**
	 * Functions available in config file.
	 */
	private Map<String, FunctionConfig> functions = new HashMap<>();

	public ConfigReader() {
		/**
		 * Loads user defined functions.
		 */
		functions.put("function", new FunctionConfig() {
			@SuppressWarnings("null")
			@Override
			public @Nonnull
			TypedYielder getSource(String args,
					Map<String, TypedYielder> context) {

				FunctionDefinition def = new FunctionLoader().load(args);
				functions.put(def.functionName, def.functionConfig);

				return dummyYielder;
			}
		});

		// Default functions.
		functions.put("readfile", new ReadFile());
		functions.put("save", new Save());
		functions.put("combine", new Combine());
		functions.put("toJSON", new ToJson());
		functions.put("toText", new ToText());
		functions.put("where", new Where());
		functions.put("listen", new Listen());
		functions.put("print", new Print());
		functions.put("union", new Union());
		functions.put("shell", new Shell());
		functions.put("mutate", new Mutate());
	}

	/**
	 * Reads configuration, instantiating the queues whilst reading.
	 * 
	 * @param context
	 *            Existing queues.
	 * @param lines
	 *            Configuration
	 * @return Amended queues.
	 */
	public Map<String, TypedYielder> toQueues(
			Map<String, TypedYielder> context, ConfigStream lines) {
		if (!context.containsKey(LAST_SOURCE)) {
			context.put(LAST_SOURCE, dummyYielder);
		}

		boolean requireFunction = true;
		for (ConfigLine configLine : lines) {
			String line = configLine.lineContent;
			// Strip comments. Everything after a hash is considered a comment.
			line = line.replaceFirst("#.*$", "");

			if (line.matches("^\\s*$")) {
				// Skip empty lines.
				continue;
			} else if (line.startsWith(":")) {
				// Execute config command.
				try {
					context = executeCommand(configLine, context);
				} catch (RuntimeException e) {
					throw new ParseException("Failed to execute command.",
							configLine, e);
				}
				continue;
			}

			boolean isAlias = line.matches("^\\s+as.*$");
			if (requireFunction && isAlias) {
				throw new ParseException("Expected function but got " + line,
						configLine);
			}
			if (isAlias) {
				String name = line.replaceFirst("^\\s+as ", "").trim();
				if (name.isEmpty()) {
					throw new ParseException("Missing name", configLine);
				}
				TypedYielder lastSource = context.get(LAST_SOURCE);
				context.put(name, lastSource);

				// Two aliases in a row do not make sense because they would be
				// the same. Force a method to follow.
				requireFunction = true;
			} else {
				if (!line.matches("^\\w.*")) {
					throw new ParseException(
							"Identation error. Function calls must start at the first character of a line.",
							configLine);
				}
				String name = line.replaceFirst("^(\\w+) .*$", "$1");
				if (functions.containsKey(name)) {
					FunctionConfig functionConfig = functions.get(name);
					@Nonnull
					TypedYielder lastSource;
					try {
						String arguments;
						if (line.contains(" ")) {
							arguments = line.replace(name + " ", "");
						} else {
							arguments = "";
						}
						lastSource = functionConfig.getSource(arguments,
								context);
					} catch (RuntimeException e) {
						throw new ParseException(e.getMessage(), configLine, e);
					}
					// Store function's resulting queue for chaining.
					context.put(LAST_SOURCE, lastSource);
				} else {
					throw new ParseException("Missing function " + name,
							configLine);
				}

				requireFunction = name.equals("function");
			}
		}

		return context;
	}

	/**
	 * Executes built-in command.
	 */
	private Map<String, TypedYielder> executeCommand(ConfigLine configLine,
			Map<String, TypedYielder> context) {
		String commandLine = configLine.lineContent.substring(1);
		String command;
		if (commandLine.contains(" ")) {
			command = commandLine.split(" ")[0];
		} else {
			command = commandLine;
		}
		switch (command) {
		case "functions":
			// Prints currently defined functions.
			System.out.println(functions);
			return context;
		case "context":
			// Prints currently defined queues.
			System.out.println(context);
			return context;
		case "include":
			// Includes another configuration file.
			return toQueues(
					context,
					new ConfigStream(configLine.file.resolveSibling(Paths
							.get(commandLine.split(" ")[1]))));
		default:
			throw new IllegalArgumentException("Command not supported: "
					+ command);
		}
	}
}
