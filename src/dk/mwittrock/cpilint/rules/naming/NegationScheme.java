package dk.mwittrock.cpilint.rules.naming;

import java.util.Objects;

public final class NegationScheme implements NamingScheme {
	
	private final NamingScheme innerScheme;
	
	public NegationScheme(NamingScheme innerScheme) {
		this.innerScheme = Objects.requireNonNull(innerScheme, "innerScheme must not be null");
	}

	@Override
	public boolean test(String name) {
		return !innerScheme.test(name);
	}

}
