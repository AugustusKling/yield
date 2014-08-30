package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.ShortDocumentation;
import yield.config.TypedYielder;
import yield.output.Printer;

@ShortDocumentation(text = "Prints events to standard output.")
public class Print extends FunctionConfig {
	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		Printer<Object> mailSender = new Printer<>(args);
		TypedYielder typedYielder = context.get(ConfigReader.LAST_SOURCE);
		typedYielder.yielder.bind(mailSender);
		return typedYielder;
	}
}