package yield.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Type restriction with conjunct and disjunct types.
 */
public class EventType {
	/**
	 * Type that is compatible with every type.
	 */
	@Nonnull
	public static final EventType ALL = new EventType(Object.class);

	private enum SetMode {
		/**
		 * Disjunct types.
		 */
		UNION,
		/**
		 * Conjunct types.
		 */
		INTERSECT
	}

	/**
	 * Represented type.
	 */
	private Class<?> type;
	/**
	 * Generic parameters of represented type (covariant).
	 */
	private List<EventType> generics;

	/**
	 * Conjunct or disjunct types, meaning is given by {@link #setMode}.
	 */
	private List<EventType> alternatives;
	/**
	 * Defines how to correlate alternatives types to this type.
	 */
	private SetMode setMode;

	/**
	 * @param type
	 *            Represented type.
	 */
	public EventType(Class<?> type) {
		this.type = type;
		this.generics = new ArrayList<>();

		this.alternatives = new ArrayList<>();
		this.setMode = SetMode.UNION;
	}

	/**
	 * @param template
	 *            Represented type.
	 */
	private EventType(EventType template) {
		this.type = template.type;
		this.generics = new ArrayList<>(template.generics);

		this.alternatives = new ArrayList<>(template.alternatives);
		this.setMode = template.setMode;
	}

	/**
	 * @param other
	 *            Type to compare with.
	 * @return {@code true} if this type can be used in place of {@code other}
	 *         type (on the assumption that all types are covariant).
	 */
	public boolean isUsableAs(EventType other) {
		List<EventType> own = new ArrayList<>(this.alternatives);
		own.add(this);
		List<EventType> others = new ArrayList<>(other.alternatives);
		others.add(other);

		if (other.setMode == SetMode.INTERSECT) {
			for (EventType alternative : others) {
				boolean anyUseable = false;
				for (EventType some : own) {
					boolean useable = isUsableAs(some, alternative);
					if (this.setMode == SetMode.UNION) {
						if (!useable) {
							return false;
						}
					} else if (useable) {
						anyUseable = true;
					}
				}
				if (!anyUseable) {
					return false;
				}
			}
			return true;
		} else {
			if (this.setMode == SetMode.UNION) {
				boolean ret = false;
				for (EventType some : own) {
					ret = false;
					for (EventType alternative : others) {
						if (isUsableAs(some, alternative)) {
							ret = true;
						}
					}
					if (!ret)
						return ret;
				}
				return ret;
			} else {
				for (EventType some : own) {
					for (EventType alternative : others) {
						if (isUsableAs(some, alternative)) {
							return true;
						}
					}
				}
				return false;
			}
		}
	}

	/**
	 * Checks type compatibility without considering {@link #alternatives}.
	 * 
	 * @param some
	 *            Any type.
	 * @param other
	 *            Type to compare with.
	 * @return {@code true} if this type can be used in place of {@code other}
	 *         type (on the assumption that all types are covariant).
	 */
	private boolean isUsableAs(EventType some, EventType other) {
		boolean typeCompatible = other.type.isAssignableFrom(some.type);

		if (typeCompatible && other.generics.isEmpty()) {
			return true;
		} else if (typeCompatible == false
				|| getGenerics(some).size() != getGenerics(other).size()) {
			return false;
		} else {
			Iterator<EventType> thisGenericsIterator = getGenerics(some)
					.iterator();
			Iterator<EventType> otherGenericsIterator = getGenerics(other)
					.iterator();
			while (thisGenericsIterator.hasNext()) {
				EventType genericType = thisGenericsIterator.next();
				EventType otherGenericType = otherGenericsIterator.next();
				boolean genericTypeCompatible = genericType
						.isUsableAs(otherGenericType);
				if (genericTypeCompatible == false) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * @param some
	 *            Type to examine.
	 * @return Generic parameters of the type. Parameter {@link Object} is
	 *         assumed if no generic parameters were given.
	 */
	private List<EventType> getGenerics(EventType some) {
		if (some.generics.isEmpty()) {
			int declaredGenericTypes = some.type.getTypeParameters().length;
			List<EventType> generics = new ArrayList<>(declaredGenericTypes);
			for (int i = 0; i < declaredGenericTypes; i++) {
				generics.add(EventType.ALL);
			}
			return generics;
		} else {
			return some.generics;
		}
	}

	@Override
	public String toString() {
		List<EventType> own = new ArrayList<>();
		own.add(this);
		own.addAll(this.alternatives);

		StringBuilder sb = new StringBuilder();

		boolean first = true;
		for (EventType alternative : own) {
			if (first) {
				first = false;
			} else if (this.setMode == SetMode.UNION) {
				sb.append("|");

			} else if (this.setMode == SetMode.INTERSECT) {
				sb.append("&");
			}

			sb.append(alternative.type.getName());
			if (!alternative.generics.isEmpty()) {
				sb.append("<");
				boolean firstGeneric = true;
				for (EventType generic : alternative.generics) {
					if (firstGeneric) {
						firstGeneric = false;
					} else {
						sb.append(",");
					}

					sb.append(generic.toString());
				}
				sb.append(">");
			}
		}
		return sb.toString();
	}

	/**
	 * Supplies next generic type parameter.
	 * 
	 * @param eventType
	 *            Copy template.
	 * @return Type with restricted generic parameter.
	 */
	@Nonnull
	public EventType withGeneric(@Nonnull EventType eventType) {
		EventType et = new EventType(this);
		et.generics.add(eventType);
		return et;
	}

	/**
	 * Supplies next generic type parameter.
	 * 
	 * @param class1
	 *            Copy template.
	 * @return Type with restricted generic parameter.
	 */
	@Nonnull
	public EventType withGeneric(@Nonnull Class<?> class1) {
		return withGeneric(new EventType(class1));
	}

	/**
	 * @param eventType
	 *            Disjunct alternative type.
	 * @return Copy of given type with alternative.
	 */
	@Nonnull
	public EventType or(@Nonnull EventType eventType) {
		EventType et = new EventType(this);
		et.alternatives.add(eventType);
		et.setMode = SetMode.UNION;
		return et;
	}

	/**
	 * @param class1
	 *            Disjunct alternative type.
	 * @return Copy of given type with alternative.
	 */
	@Nonnull
	public EventType or(@Nonnull Class<?> class1) {
		return or(new EventType(class1));
	}

	/**
	 * @param eventType
	 *            Conjunct type.
	 * @return Copy of given type with additional type restriction.
	 */
	@Nonnull
	public EventType and(@Nonnull EventType eventType) {
		EventType et = new EventType(this);
		et.alternatives.add(eventType);
		et.setMode = SetMode.INTERSECT;
		return et;
	}

	/**
	 * @param class1
	 *            Conjunct type.
	 * @return Copy of given type with additional type restriction.
	 */
	@Nonnull
	public EventType and(@Nonnull Class<Serializable> class1) {
		return and(new EventType(class1));
	}
}
