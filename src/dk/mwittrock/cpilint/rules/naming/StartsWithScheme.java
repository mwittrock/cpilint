package dk.mwittrock.cpilint.rules.naming;

import java.util.Objects;

public final class StartsWithScheme implements NamingScheme {
	
	private final String prefix;
	
	public StartsWithScheme(String prefix) {
		Objects.requireNonNull(prefix, "prefix must not be null");
		if (prefix.isEmpty()) {
			throw new IllegalArgumentException("prefix must not be the empty string");
		}
		this.prefix = prefix;
	}

	@Override
	public boolean test(String name) {
		return name.startsWith(prefix);
	}

}
