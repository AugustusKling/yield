package yield.input.directory;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;

/**
 * Change within a folder (file add, edited, deleted).
 */
public class DirectoryEvent {
	public final Kind<Path> type;
	public final Path affectedPath;

	public DirectoryEvent(WatchEvent.Kind<Path> type, Path affectedPath) {
		this.type = type;
		this.affectedPath = affectedPath;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DirectoryEvent) {
			return type.equals(((DirectoryEvent) obj).type)
					&& affectedPath.equals(((DirectoryEvent) obj).affectedPath);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return type.toString() + " " + affectedPath.toString();
	}
}
