package yield.config.function;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.ShortDocumentation;
import yield.config.TypedYielder;
import yield.core.MappedQueue;
import yield.core.ValueMapper;
import yield.core.event.FailureEvent;
import yield.core.event.MetaEvent;
import yield.core.event.SuccessEvent;
import yield.output.file.FileAppender;

@ShortDocumentation(text = "Saves to a file with one line per event.")
public class Save extends FunctionConfig {
	protected Path parsePath(String args) {
		String filename = args.trim().replaceFirst("^\"", "")
				.replaceFirst("\"$", "");
		return Paths.get(filename);
	}

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		final FileAppender<Object> appender = new FileAppender<>(
				parsePath(args));
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
		context.get(ConfigReader.LAST_SOURCE).yielder.bind(input);
		return wrapResultingYielder(input.getQueue());
	}

	@Override
	public String getResultEventType() {
		return MetaEvent.class.getName() + "<java.lang.Object>";
	}
}