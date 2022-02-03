package dk.mwittrock.cpilint.rules.naming;

import java.util.Objects;

public final class EndsWithScheme implements NamingScheme {
	
	private final String postfix;
	
	public EndsWithScheme(String postfix) {
		Objects.requireNonNull(postfix, "postfix must not be null");
		if (postfix.isEmpty()) {
			throw new IllegalArgumentException("postfix must not be the empty string");
		}
		this.postfix = postfix;
	}

	@Override
	public boolean test(String name) {
		return name.endsWith(postfix);
	}

}
