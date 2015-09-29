package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.FunctionConfig;
import yield.config.ShortDocumentation;
import yield.config.TypedYielder;
import yield.core.EventType;

@ShortDocumentation(text = "Switches context to given yielder.")
public class Listen extends FunctionConfig {
	@Nonnull
	private EventType resultEventType = new EventType(Object.class);

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		TypedYielder yielder = context.get(args.trim());
		if (yielder == null) {
			throw new IllegalArgumentException("Cannot find yielder "
					+ args.trim());
		} else {
			this.resultEventType = yielder.type;
			return yielder;
		}
	}

	@Override
	@Nonnull
	protected EventType getResultEventType() {
		return this.resultEventType;
	}
}