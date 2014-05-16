package yield.config.function;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.FunctionConfig;
import yield.config.TypedYielder;
import yield.core.MappedQueue;
import yield.core.ValueMapper;
import yield.input.directory.DirectoryEvent;
import yield.input.directory.DirectoryWatcher;
import yield.input.shipper.ShipperFile;
import yield.json.JsonEvent;

/**
 * Watches files or directories for modifications.
 * <p>
 * Yields either {@link DirectoryEvent}s for directories or {@link String}s for
 * files.
 */
public class Watch extends FunctionConfig {
	private String resultType;

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		String path = args.trim().replaceFirst("^\"", "")
				.replaceFirst("\"$", "");
		if (path.isEmpty()) {
			throw new IllegalArgumentException(
					"Path to file or directory needs to be provided.");
		}
		Path watchable = Paths.get(path);
		if (watchable.toFile().isDirectory() || path.endsWith("/")) {
			this.resultType = JsonEvent.class.getName();
			try {
				DirectoryWatcher watcher = new DirectoryWatcher(watchable);
				MappedQueue<DirectoryEvent, JsonEvent> toJSON = new MappedQueue<>(
						new ValueMapper<DirectoryEvent, JsonEvent>() {

							@Override
							public JsonEvent map(DirectoryEvent value) {
								JsonEvent event = new JsonEvent();
								event.put("type", value.type.toString());
								event.put("affectedPath", value.affectedPath
										.toAbsolutePath().toString());
								return event;
							}
						});
				watcher.getQueue().bind(toJSON);
				return wrapResultingYielder(toJSON.getQueue());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			this.resultType = String.class.getName();
			ShipperFile watcher = new ShipperFile(watchable.toAbsolutePath());
			return wrapResultingYielder(watcher.getQueue());
		}
	}

	@Override
	protected String getResultEventType() {
		return this.resultType;
	}
}
