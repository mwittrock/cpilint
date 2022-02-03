package dk.mwittrock.cpilint.rules.naming;

import java.util.Objects;

public final class EqualsScheme implements NamingScheme {
	
	private final String match;
	private final boolean ignoreCase;
	
	public EqualsScheme(String match, boolean ignoreCase) {
		Objects.requireNonNull(match, "match must not be null");
		if (match.isEmpty()) {
			throw new IllegalArgumentException("match must not be the empty string");
		}
		this.match = match;
		this.ignoreCase = ignoreCase;
	}

	@Override
	public boolean test(String name) {
		return ignoreCase ? name.equalsIgnoreCase(match) : name.equals(match);
	}

}
