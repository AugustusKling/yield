package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.FunctionConfig;
import yield.config.ShortDocumentation;
import yield.config.TypedYielder;
import yield.core.EventQueue;
import yield.core.EventType;
import yield.core.Yielder;

@ShortDocumentation(text = "Queue that yields events from multiple input queues.")
public class Union extends FunctionConfig {
	@Nonnull
	private EventType resultType = new EventType(Object.class);

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		EventQueue<Object> queue = new EventQueue<>(Object.class);
		String[] inputs = args.split("\\s*,\\s*");
		for (String input : inputs) {
			String inputName = input.trim();
			if (!context.containsKey(inputName)) {
				throw new IllegalArgumentException("Cannot read from queue "
						+ inputName + " because it is not defined.");
			} else {
				Yielder<Object> typedYielder = getYielderTypesafe(Object.class,
						inputName, context);
				resultType = resultType.or(typedYielder.getOutputType());
				typedYielder.bind(queue);
			}
		}
		return wrapResultingYielder(queue);
	}

	@Override
	@Nonnull
	protected EventType getResultEventType() {
		return this.resultType;
	}
}
