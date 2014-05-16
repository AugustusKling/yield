package yield.config.function;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.TypedYielder;
import yield.core.Yielder;
import yield.json.JsonEvent;
import yield.output.shell.RunCommand;

public class Shell extends FunctionConfig {
	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		Yielder<JsonEvent> yielder = getYielderTypesafe(JsonEvent.class,
				ConfigReader.LAST_SOURCE, context);
		String[] dirAndCommand = args.split(" ", 2);
		if (dirAndCommand.length < 2) {
			throw new IllegalArgumentException(
					"Working directory and command template expected.");
		}
		ArrayList<String> commandTemplate = new ArrayList<>();
		for (String commandFragment : dirAndCommand[1].split(" ")) {
			commandTemplate.add(commandFragment);
		}
		yielder.bind(new RunCommand(Paths.get(dirAndCommand[0]),
				commandTemplate));
		return wrapResultingYielder(yielder);
	}

	@Override
	protected String getResultEventType() {
		return JsonEvent.class.getName();
	}
}
