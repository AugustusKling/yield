package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.ShortDocumentation;
import yield.config.TypedYielder;
import yield.core.Yielder;
import yield.output.Printer;

@ShortDocumentation(text = "Prints events to standard output.")
public class Print extends FunctionConfig {
	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		Printer<Object> printer = new Printer<>(args);
		Yielder<Object> typedYielder = getYielderTypesafe(Object.class,
				ConfigReader.LAST_SOURCE, context);
		typedYielder.bind(printer);
		return wrapResultingYielder(typedYielder);
	}
}