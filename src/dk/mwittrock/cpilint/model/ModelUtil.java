package dk.mwittrock.cpilint.model;

import java.util.EnumSet;
import java.util.Set;

final class ModelUtil {
	
	private ModelUtil() {
		throw new AssertionError("Never supposed to be instantiated");
	}
	
	static <E extends Enum<E>> Set<E> allValuesExcept(Set<E> s) {
		return EnumSet.complementOf(EnumSet.copyOf(s));
	}

	static String xpathTruePredicate() {
	    return "[true()]";
	}

	static String xpathFalsePredicate() {
	    return "[false()]";
	}
	
}
