package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.TypedYielder;
import yield.core.Yielder;
import yield.output.Printer;

public class Print extends FunctionConfig {
	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		Printer<Object> mailSender = new Printer<>(args);
		TypedYielder typedYielder = context.get(ConfigReader.LAST_SOURCE);
		((Yielder<Object>) typedYielder.yielder).bind(mailSender);
		return typedYielder;
	}
}