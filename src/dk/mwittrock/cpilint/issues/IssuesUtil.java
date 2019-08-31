package dk.mwittrock.cpilint.issues;

import java.util.Locale;

final class IssuesUtil {

	private IssuesUtil() {
		throw new AssertionError("Never supposed to be instantiated");
	}
	
	public static String capitalizeFirst(String toCapitalize) {
		if (toCapitalize.isEmpty()) {
			throw new IllegalArgumentException("Cannot capitalize an empty string");
		}
		String capitalized;
		if (toCapitalize.length() == 1) {
			capitalized = toCapitalize.toUpperCase(Locale.ENGLISH);
		} else {
			capitalized = toCapitalize.substring(0, 1).toUpperCase(Locale.ENGLISH) + toCapitalize.substring(1);
		}
		return capitalized;
	}
	
}
