package dk.mwittrock.cpilint.rules.naming;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DisjunctionScheme implements NamingScheme {
	
	private final List<NamingScheme> innerSchemes;
	
	public DisjunctionScheme(List<NamingScheme> innerSchemes) {
		Objects.requireNonNull(innerSchemes, "innerSchemes must not be null");
		if (innerSchemes.isEmpty()) {
			throw new IllegalArgumentException("innerSchemes must not be empty");
		}
		if (innerSchemes.stream().anyMatch(s -> s == null)) {
			throw new IllegalArgumentException("innerSchemes must not contain null elements");
		}
		this.innerSchemes = new ArrayList<>(innerSchemes);
	}

	@Override
	public boolean test(String name) {
		return innerSchemes.stream().anyMatch(s -> s.test(name));
	}

}
