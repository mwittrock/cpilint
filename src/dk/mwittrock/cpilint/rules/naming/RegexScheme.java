package dk.mwittrock.cpilint.rules.naming;

import java.util.Objects;
import java.util.regex.Pattern;

public final class RegexScheme implements NamingScheme {
	
	private final Pattern pattern;
	
	public RegexScheme(Pattern pattern) {
		this.pattern = Objects.requireNonNull(pattern, "pattern must not be null");
	}

	@Override
	public boolean test(String name) {
		/*
		 * For now, the assumption is that the pattern describes the full name.
		 * For that reason, we'll use matches() in the following.
		 */
		return pattern.matcher(name).matches();
	}

}
