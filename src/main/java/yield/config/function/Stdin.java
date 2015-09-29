package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.FunctionConfig;
import yield.config.ShortDocumentation;
import yield.config.TypedYielder;
import yield.core.EventType;

@ShortDocumentation(text = "Reads from standard input (console).")
public class Stdin extends FunctionConfig {
	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		return wrapResultingYielder(new yield.input.console.Stdin());
	}

	@Override
	@Nonnull
	protected EventType getResultEventType() {
		return new EventType(String.class);
	}
}