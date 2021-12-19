package dk.mwittrock.cpilint.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public final class JarResourceUtil {
	
	private static final String RESOURCE_BASE_PATH = "resources";
	
	private JarResourceUtil() {
		throw new AssertionError("Never supposed to be instantiated");
	}
	
    private static String pathToXqueryResource(String xqueryFilename) {
		StringJoiner joiner = new StringJoiner("/");
		joiner.add(RESOURCE_BASE_PATH).add("xquery").add(xqueryFilename);
		return joiner.toString();
	}	

	public static String loadXqueryResource(String xqueryFilename) {
		String path = pathToXqueryResource(xqueryFilename);
		InputStream xqueryStream = JarResourceUtil.class.getClassLoader().getResourceAsStream(path);
		if (xqueryStream == null) {
			throw new JarResourceError(String.format("XQuery resource '%s' not found", xqueryFilename));
		}
		String xquery;
		try {
			xquery = IoUtil.inputStreamToString(xqueryStream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new JarResourceError(String.format("Error reading XQuery resource '%s'", xqueryFilename), e);
		}
		return xquery;
	}

}
