package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.FunctionConfig;
import yield.config.ShortDocumentation;
import yield.config.TypedYielder;
import yield.core.EventQueue;

@ShortDocumentation(text = "Queue that yields events from multiple input queues.")
public class Union extends FunctionConfig {
	private String resultType = Object.class.getName();

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		EventQueue<Object> queue = new EventQueue<>();
		String[] inputs = args.split("\\s*,\\s*");
		for (String input : inputs) {
			String inputName = input.trim();
			if (!context.containsKey(inputName)) {
				throw new IllegalArgumentException("Cannot read from queue "
						+ inputName + " because it is not defined.");
			} else {
				TypedYielder typedYielder = context.get(inputName);
				if (resultType == null) {
					resultType = typedYielder.type;
				} else if (!resultType.equals(typedYielder.type)) {
					// TODO Should use closest common ancestor instead.
					resultType = Object.class.getName();
				}
				typedYielder.yielder.bind(queue);
			}
		}
		return wrapResultingYielder(queue);
	}

	@Override
	protected String getResultEventType() {
		return this.resultType;
	}
}
