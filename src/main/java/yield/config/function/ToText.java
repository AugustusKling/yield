package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.TypedYielder;
import yield.core.MappedQueue;
import yield.core.ValueMapper;
import yield.core.Yielder;

public class ToText extends FunctionConfig {
	@Override
	@Nonnull
	protected String shortDescription() {
		return "Converts to text.";
	}

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		MappedQueue<Object, String> input = new MappedQueue<>(
				new ValueMapper<Object, String>() {

					@Override
					public String map(Object value) {
						return value.toString();
					}
				});
		((Yielder<Object>) context.get(ConfigReader.LAST_SOURCE).yielder)
				.bind(input);
		return wrapResultingYielder(input.getQueue());
	}

	@Override
	public String getResultEventType() {
		return String.class.getName();
	}
}