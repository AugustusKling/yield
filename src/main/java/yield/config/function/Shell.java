package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.TypedYielder;
import yield.core.Yielder;
import yield.json.JsonEvent;
import yield.output.shell.RunCommand;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode config = objectMapper.createObjectNode();
		config.put("directory", dirAndCommand[0]);
		ArrayNode commandTemplate = objectMapper.createArrayNode();
		for (String commandFragment : dirAndCommand[1].split(" ")) {
			commandTemplate.add(commandFragment);
		}
		config.put("commandTemplate", commandTemplate);
		yielder.bind(new RunCommand(null, config));
		return wrapResultingYielder(yielder);
	}

	@Override
	protected String getResultEventType() {
		return JsonEvent.class.getName();
	}
}
