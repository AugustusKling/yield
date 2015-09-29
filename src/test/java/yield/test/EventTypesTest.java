package yield.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import yield.core.EventListener;
import yield.core.EventType;

public class EventTypesTest {
	@Test
	public void noGenericStrictEquality() {
		assertTrue(EventType.ALL.isUsableAs(EventType.ALL));
		assertFalse(EventType.ALL.isUsableAs(new EventType(String.class)));

		assertTrue(new EventType(String.class).isUsableAs(new EventType(
				String.class)));
		assertFalse(new EventType(String.class).isUsableAs(new EventType(
				Number.class)));

		assertTrue(new EventType(Object.class).isUsableAs(new EventType(
				Object.class)));
		assertFalse(new EventType(Object.class).isUsableAs(new EventType(
				Number.class)));

		assertTrue(new EventType(CharSequence.class).isUsableAs(new EventType(
				CharSequence.class)));
		assertFalse(new EventType(CharSequence.class).isUsableAs(new EventType(
				EventListener.class)));
	}

	@Test
	public void noGenericCovariantEquality() {
		assertTrue(new EventType(String.class).isUsableAs(new EventType(
				CharSequence.class)));
		assertTrue(new EventType(String.class).isUsableAs(new EventType(
				Object.class)));
	}

	@Test
	public void generics() {
		EventType listObject = new EventType(List.class)
				.withGeneric(new EventType(Object.class));
		EventType listString = new EventType(List.class)
				.withGeneric(new EventType(String.class));

		assertTrue(listString.isUsableAs(listString));
		assertTrue(listObject.isUsableAs(listObject));

		assertTrue(listString.isUsableAs(listObject));
		assertFalse(listObject.isUsableAs(listString));

		EventType map = new EventType(Map.class);
		EventType mapObjectObject = new EventType(Map.class).withGeneric(
				Object.class).withGeneric(Object.class);
		EventType mapStringNumber = new EventType(Map.class).withGeneric(
				String.class).withGeneric(Number.class);

		assertTrue(mapStringNumber.isUsableAs(mapStringNumber));
		assertTrue(mapStringNumber.isUsableAs(mapObjectObject));
		assertTrue(mapStringNumber.isUsableAs(map));
		assertTrue(map.isUsableAs(mapObjectObject));
	}

	@Test
	public void disjunctionTypes() {
		EventType doubleOrFloat = new EventType(Double.class).or(Float.class);

		assertTrue(new EventType(Double.class).isUsableAs(doubleOrFloat));
		assertTrue(new EventType(Float.class).isUsableAs(doubleOrFloat));
		assertFalse(doubleOrFloat.isUsableAs(new EventType(Double.class)));
		assertFalse(doubleOrFloat.isUsableAs(new EventType(Float.class)));
	}

	@Test
	public void intersectionTypes() {
		EventType cloneableAndSerializable = new EventType(Cloneable.class)
				.and(Serializable.class);
		EventType cloneableOrSerializable = new EventType(Cloneable.class)
				.or(Serializable.class);
		EventType cloneable = new EventType(Cloneable.class);
		EventType serializable = new EventType(Serializable.class);

		assertTrue(cloneableAndSerializable
				.isUsableAs(cloneableAndSerializable));
		assertTrue(cloneableAndSerializable.isUsableAs(cloneable));
		assertTrue(cloneableAndSerializable.isUsableAs(serializable));
		assertTrue(cloneableAndSerializable.isUsableAs(new EventType(
				Object.class)));

		assertFalse(cloneableAndSerializable.isUsableAs(new EventType(
				String.class)));
		assertFalse(cloneable.isUsableAs(cloneableAndSerializable));
		assertFalse(serializable.isUsableAs(cloneableAndSerializable));

		assertTrue(cloneableAndSerializable.isUsableAs(cloneableOrSerializable));
		assertFalse(cloneableOrSerializable
				.isUsableAs(cloneableAndSerializable));
	}

	@Test
	public void mixedGenerics() {
		EventType set = new EventType(Set.class).withGeneric(String.class);

		assertTrue(set.isUsableAs(EventType.ALL));
		assertTrue(set.isUsableAs(new EventType(Set.class)));

		assertFalse(EventType.ALL.isUsableAs(set));
		assertFalse(new EventType(Set.class).isUsableAs(set));
	}
}
