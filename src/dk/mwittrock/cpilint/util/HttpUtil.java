package dk.mwittrock.cpilint.util;

public final class HttpUtil {

    public static final int HTTP_OKAY_STATUS_CODE = 200;
	public static final int HTTP_BAD_REQUEST_STATUS_CODE = 400;
	public static final int HTTP_UNAUTHORIZED_STATUS_CODE = 401;
	public static final int HTTP_FORBIDDEN_STATUS_CODE = 403;
	public static final int HTTP_NOT_FOUND_STATUS_CODE = 404;

    private HttpUtil() {
        throw new AssertionError("Never supposed to be instantiated"); 
    }

}
