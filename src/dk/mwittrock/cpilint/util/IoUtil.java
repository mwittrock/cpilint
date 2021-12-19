package dk.mwittrock.cpilint.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public final class IoUtil {
	
	private IoUtil() {
		throw new AssertionError("Never supposed to be instantiated");
	}
	
	public static String inputStreamToString(InputStream in, Charset cs) throws IOException {
		byte[] bytes = in.readAllBytes();
		return new String(bytes, cs);
	}

}
