package yield.config;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import yield.config.ParameterMap.Param;
import yield.config.function.Average;
import yield.config.function.Combine;
import yield.config.function.Count;
import yield.config.function.Delay;
import yield.config.function.Grok;
import yield.config.function.Listen;
import yield.config.function.Mutate;
import yield.config.function.NetworkListen;
import yield.config.function.NetworkSend;
import yield.config.function.Print;
import yield.config.function.Save;
import yield.config.function.Shell;
import yield.config.function.Stdin;
import yield.config.function.Timestamp;
import yield.config.function.ToJson;
import yield.config.function.ToText;
import yield.config.function.Union;
import yield.config.function.Watch;
import yield.config.function.Where;
import yield.core.EventListener;
import yield.core.EventQueue;

/**
 * Parses configuration, creates queues.
 */
public class ConfigReader {
	@ShortDocumentation(text = "Loads externally defined functions / plugins.")
	private final class FunctionLoaderConfig extends FunctionConfig {
		@SuppressWarnings("null")
		@Override
		public @Nonnull TypedYielder getSource(String args,
				Map<String, TypedYielder> context) {

			FunctionDefinition def = new FunctionLoader().load(args);
			functions.put(def.functionName, def.functionConfig);

			return dummyYielder;
		}
	}

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
		functions.put("function", new FunctionLoaderConfig());

		// Default functions.
		functions.put("watch", new Watch());
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
		functions.put("grok", new Grok());
		functions.put("timestamp", new Timestamp());
		functions.put("delay", new Delay());
		functions.put("stdin", new Stdin());
		functions.put("network-listen", new NetworkListen());
		functions.put("network-send", new NetworkSend());

		// Aggregators.
		functions.put("count", new Count());
		functions.put("average", new Average());
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
			Map<String, TypedYielder> context, Iterable<ConfigLine> lines) {
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
				String name = line.replaceFirst("^((?:-|\\w)+) .*$", "$1");
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
			printFunctions(functions);
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

	/**
	 * Prints listing of all available functions and their parameters along with
	 * concise documentation.
	 */
	private <Parameter extends Enum<Parameter> & Param> void printFunctions(
			Map<String, FunctionConfig> functions2) {
		// This should be determined at runtime but all Java solutions see
		// pretty hacky.
		int consoleWidth = 200;
		for (Entry<String, FunctionConfig> func : functions2.entrySet()) {
			String classDocumentation = func.getKey();
			ShortDocumentation shortDocumentation = func.getValue().getClass()
					.getAnnotation(ShortDocumentation.class);
			if (shortDocumentation != null) {
				String docString = shortDocumentation.text();
				if (docString != null && !docString.isEmpty()) {
					classDocumentation = classDocumentation + ": " + docString;
				}
			}
			printBlock(0, 1, classDocumentation, consoleWidth);

			Class<? extends Parameter> paramClass = (Class<? extends Parameter>) func
					.getValue().getParameters();
			if (paramClass != null) {
				for (Parameter param : paramClass.getEnumConstants()) {
					String line = "* " + param.name();
					ShortDocumentation paramDocumentation;
					try {
						paramDocumentation = paramClass.getField(param.name())
								.getAnnotation(ShortDocumentation.class);
					} catch (NoSuchFieldException | SecurityException e) {
						// Should never happen because the field is requested
						// from the enum that provided the parameter.
						throw new RuntimeException(e);
					}
					if (paramDocumentation != null) {
						String docString = paramDocumentation.text();
						if (docString != null && !docString.isEmpty()) {
							line = line + ": " + docString;
						}
					}
					printBlock(1, 2, line, consoleWidth);

					try {
						printBlock(4, 5, "Default: " + param.getDefault(),
								consoleWidth);
					} catch (UnsupportedOperationException e) {
						// Nothing to print in case no default value exists.
					}
				}
			}
		}
	}

	/**
	 * Prints text with indent and line breaks.
	 *
	 * @param firstLineIndent
	 *            Number of spaces to indent the first line.
	 * @param lineIndent
	 *            Number of spaces to indent the following lines.
	 * @param text
	 *            Text to print, over multiple line if it exceeds
	 *            {@code consoleWidth}.
	 * @param consoleWidth
	 *            Maximum line length including indents.
	 */
	private void printBlock(int firstLineIndent, int lineIndent,
			@Nonnull String text, int consoleWidth) {
		if (firstLineIndent < 0 || lineIndent < 0 || consoleWidth < 0) {
			throw new IllegalArgumentException(
					"Indents or console width cannot be negative.");
		}
		String toPrint;
		if (firstLineIndent > 0) {
			toPrint = String.format("%" + firstLineIndent + "s", "") + text;
		} else {
			toPrint = text;
		}
		System.out.println(toPrint.substring(0,
				Math.min(toPrint.length(), consoleWidth)));
		for (int startIndex = consoleWidth; startIndex < toPrint.length(); startIndex = startIndex
				+ consoleWidth - lineIndent) {
			String lineIndentChars;
			if (lineIndent > 0) {
				lineIndentChars = String.format("%" + lineIndent + "s", "");
			} else {
				lineIndentChars = "";
			}
			System.out.println(lineIndentChars
					+ toPrint.substring(
							startIndex,
							Math.min(toPrint.length(), startIndex
									+ consoleWidth - lineIndent)));
		}
	}
}
