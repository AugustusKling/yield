package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.TypedYielder;
import yield.core.MappedQueue;
import yield.core.ValueMapper;
import yield.core.Yielder;
import yield.core.event.FailureEvent;
import yield.core.event.MetaEvent;
import yield.core.event.SuccessEvent;
import yield.output.file.FileAppender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Save extends FunctionConfig {
	@Override
	@Nonnull
	protected String shortDescription() {
		return "Saves to a file with one line per event.";
	}

	@Override
	protected ObjectNode parseArguments(String args) {
		ObjectNode config = new ObjectMapper().createObjectNode();
		String filename = args.trim().replaceFirst("^\"", "")
				.replaceFirst("\"$", "");
		config.put("file", filename);
		return config;
	}

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		final FileAppender<Object> appender = new FileAppender<>(null,
				parseArguments(args));
		MappedQueue<Object, MetaEvent<Object>> input = new MappedQueue<>(
				new ValueMapper<Object, MetaEvent<Object>>() {

					@Override
					public MetaEvent<Object> map(Object value) {
						try {
							appender.feed(value);
							return new SuccessEvent<>(null);
						} catch (Exception e) {
							return new FailureEvent<>(e);
						}
					}
				});
		((Yielder<Object>) context.get(ConfigReader.LAST_SOURCE).yielder)
				.bind(input);
		return wrapResultingYielder(input.getQueue());
	}

	@Override
	public String getResultEventType() {
		return MetaEvent.class.getName() + "<java.lang.Object>";
	}
}