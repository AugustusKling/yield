package yield.core;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.ConfigStream;
import yield.config.TypedYielder;
import yield.input.QueueDefined;
import yield.input.StructureChange;
import yield.input.StructureQueue;
import yield.output.Printer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class Main {
	/**
	 * Broadcasts structural changes to definitions of queue and their
	 * relations.
	 */
	private StructureQueue structureQueue = new StructureQueue();

	private Map<String, Yielder<?>> namedQueues = new HashMap<>();

	// TODO Window map.

	/**
	 * Queue that acts as glue in case reference to class is required before its
	 * definition is known.
	 * 
	 * @param <Event>
	 *            Useless but required by Java's syntax.
	 */
	private class Proxy<Event> extends EventQueue<Event> {

	}

	public Main(ObjectNode config) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		structureQueue.bind(new Printer<StructureChange>("structural>"));

		ArrayNode queues = (ArrayNode) config.get("queues");
		for (JsonNode queueConfig : queues) {
			getQueue(queueConfig);
		}
		ArrayNode objects = (ArrayNode) config.get("objects");
		for (JsonNode objectConfig : objects) {
			getObject((ObjectNode) objectConfig);
		}
	}

	/**
	 * Instantiates queues from configuration.
	 * 
	 * @param configFile
	 *            Configuration file.
	 */
	public Main(Path configFile) throws IOException {
		ConfigReader configReader = new ConfigReader();
		configReader.toQueues(new HashMap<String, TypedYielder>(),
				new ConfigStream(configFile));
	}

	/**
	 * Instantiates an object by calling its constructor(Main, ObjectNode).
	 * 
	 * @return Any object of the type given in "class".
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Nonnull
	private Object getObject(ObjectNode objectConfig) {
		Class<?> queueClass;
		TextNode queueClassNode = (TextNode) objectConfig.get("class");
		if (queueClassNode == null) {
			throw new IllegalArgumentException(
					"No class defined for configuration " + objectConfig);
		}
		String className = queueClassNode.textValue();
		try {
			queueClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("No class " + className
					+ " but requested in " + objectConfig);
		}
		Object object;
		try {
			@Nonnull
			@SuppressWarnings("null")
			Object object2 = queueClass.getConstructor(Main.class,
					ObjectNode.class).newInstance(this, objectConfig);
			object = object2;
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException(e);
		}
		if (object instanceof EventListener) {
			ArrayNode queues = (ArrayNode) objectConfig.get("queues");
			for (JsonNode queueConfig : queues) {
				getQueue(queueConfig).bind((EventListener) object);
			}
		}
		return object;
	}

	/**
	 * Returns queue by "name" or instantiates it as outlined in
	 * {@link #getObject(ObjectNode)}.
	 */
	@SuppressWarnings("unchecked")
	private <ProxyIn, ProxyOut, AbstractQueue extends EventListener<ProxyIn> & Yielder<ProxyOut>> Yielder<ProxyOut> getQueue(
			ObjectNode queueConfig) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		boolean hasName = queueConfig.has("name");
		AbstractQueue proxy = null;
		String name = null;
		if (hasName) {
			name = queueConfig.get("name").textValue();
			proxy = (AbstractQueue) namedQueues.get(name);
		}
		if (!hasName || !namedQueues.containsKey(name)
				|| proxy instanceof Proxy) {
			Object queueCandidate = getObject(queueConfig);
			Yielder<ProxyOut> queue;
			if (queueCandidate instanceof EventSource) {
				queue = (EventSource<ProxyOut>) queueCandidate;
			} else if (queueCandidate instanceof SourceProvider) {
				queue = ((SourceProvider<ProxyOut>) queueCandidate).getQueue();
			} else {
				throw new RuntimeException(
						"Require a EventQueue, but could not derive from "
								+ queueConfig);
			}
			if (!hasName) {
				return queue;
			} else if (proxy == null) {
				namedQueues.put(name, queue);
				structureQueue.feed(new QueueDefined(name, queue));
				return queue;
			} else {
				proxy.bind((EventListener<ProxyOut>) queue);
				return proxy;
			}
		} else {
			return proxy;
		}
	}

	/**
	 * Returns queue by "name" or instantiates it as outlined in
	 * {@link #getObject(ObjectNode)}.
	 */
	public Yielder<?> getQueue(JsonNode config) {
		if (config instanceof TextNode) {
			String name = config.textValue();
			if (!namedQueues.containsKey(name)) {
				Proxy<?> proxy = new Proxy<>();
				namedQueues.put(name, proxy);
				structureQueue.feed(new QueueDefined(name, proxy));
			}
			return namedQueues.get(name);
		} else {
			try {
				return getQueue((ObjectNode) config);
			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException | InvocationTargetException
					| NoSuchMethodException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

	public static void main(String[] args) throws JsonProcessingException,
			FileNotFoundException, IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		if (args[0].endsWith(".json")) {
			System.out
					.println("Warning: JSON configuration is likely to disappear shortly.");
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readTree(new FileReader(args[0]));
			if (!node.isObject()) {
				throw new IllegalArgumentException(
						"Invalid config file (must be an JSON object).");
			}
			new Main((ObjectNode) node);
		} else {
			new Main(Paths.get(args[0]));
		}
	}

}
