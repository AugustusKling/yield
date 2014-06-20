package yield.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Loads function from external package (JAR file).
 */
public class FunctionLoader {
	/**
	 * @param args
	 *            Function alias for use in config file followed by qualified
	 *            implementation of {@link FunctionConfig} followed by JAR URL.
	 *            For example
	 *            {@code youralias yield.contrib.Every "file:/tmp/generator.jar"}
	 *            .
	 * @return Aliased reference to implementation.
	 */
	@Nonnull
	public FunctionDefinition load(String args) {
		Pattern p = Pattern
				.compile("^\\s*(?<functionName>(?:-|\\w)+)\\s+(?<qualifiedName>(?:\\w+|\\.)+)\\s+[\"]?(?<jar>[^\"]+)[\"]$");
		Matcher m = p.matcher(args);
		if (m.find()) {
			// Function definition seem sound, try to load.
			String functionName = m.group("functionName");
			String qualifiedName = m.group("qualifiedName");
			String jar = m.group("jar");

			ClassLoader loader;
			try {
				loader = URLClassLoader.newInstance(new URL[] { new URL(jar) },
						getClass().getClassLoader());
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException("JAR URL malformed.", e);
			}
			Class<?> clazz;
			try {
				clazz = Class.forName(qualifiedName, true, loader);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Class " + qualifiedName
						+ " not found in JAR " + jar, e);
			}
			Class<? extends FunctionConfig> loadedClass = clazz
					.asSubclass(FunctionConfig.class);

			try {
				return new FunctionDefinition(functionName,
						loadedClass.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IllegalArgumentException(
						"Could not defined function " + functionName, e);
			}
		} else {
			throw new IllegalArgumentException(
					"Cannot parse function definition.");
		}
	}

}
