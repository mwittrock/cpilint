package org.cpilint.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cpilint.artifacts.IflowArtifact;
import org.cpilint.artifacts.PackageInfo;
import org.cpilint.artifacts.ZipArchiveIflowArtifact;
import org.cpilint.auth.AccessToken;
import org.cpilint.auth.AuthMode;
import org.cpilint.auth.AuthorizationServer;
import org.cpilint.util.JarResourceUtil;
import org.cpilint.util.HttpUtil;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

public final class CloudIntegrationOdataApi implements CloudIntegrationApi {

	private static final Logger logger = LoggerFactory.getLogger(CloudIntegrationOdataApi.class);
	private static final String MESSAGE_NOT_AUTHENTICATED = "Authentication failed";
	private static final String MESSAGE_NOT_AUTHORIZED = "Authentication was successful but authorization failed";
	private static final String ODATA_API_BASE_PATH = "/api/v1/";
	private static final String URI_SCHEME = "https";
	private static final String EXPECTED_IFLOW_ARTIFACT_RESPONSE_TYPE = "application/zip";

	private final AuthMode authMode;
	private final String hostname;
	private final String apiUsername;
	private final char[] apiPassword;
	private final AuthorizationServer authServer;
	private AccessToken accessToken;
	private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
	private final XQueryCompiler xqueryCompiler = new Processor(false).newXQueryCompiler();
	private final Map<String, PackageInfo> packageIdToPackageInfo = new HashMap<>();
	private final Map<String, PackageInfo> iflowIdToPackageInfo = new HashMap<>();

	public CloudIntegrationOdataApi(String hostname, String apiUsername, char[] apiPassword) {
		logger.debug("Instantiating CloudIntegrationOdataApi in basic authentication mode");
		authMode = AuthMode.BASIC_AUTH;
		this.hostname = Objects.requireNonNull(hostname, "hostname must not be null");
		this.apiUsername = Objects.requireNonNull(apiUsername, "apiUsername must not be null");
		this.apiPassword = Objects.requireNonNull(apiPassword, "apiPassword must not be null");
		this.authServer = null;
	}

	public CloudIntegrationOdataApi(String hostname, AuthorizationServer authServer) {
		logger.debug("Instantiating CloudIntegrationOdataApi in OAuth Client Credentials mode");
		authMode = AuthMode.OAUTH_CLIENT_CREDENTIALS;
		this.hostname = Objects.requireNonNull(hostname, "hostname must not be null");
		this.apiUsername = null;
		this.apiPassword = null;
		this.authServer = Objects.requireNonNull(authServer, "authServer must not be null");
	}

	@Override
	public IflowArtifact getIflowArtifact(String iflowArtifactId) {
		Objects.requireNonNull(iflowArtifactId, "iflowArtifactId must not be null");
		if (iflowArtifactId.isBlank()) {
			throw new IllegalArgumentException("iflowArtifactId must not be blank");
		}
		logger.debug("Retrieving iflow artifact from tenant: {}", iflowArtifactId);
		URI uri = iflowArtifactUriFromIflowArtifactId(iflowArtifactId);
		HttpResponse<InputStream> apiResponse = httpGetRequest(uri);
		final int httpStatus = apiResponse.statusCode();
		if (httpStatus == HttpUtil.HTTP_BAD_REQUEST_STATUS_CODE) {
			String message = String.format("HTTP status Bad Request returned for iflow artifact ID '%s', indicating that its package is read-only", iflowArtifactId);
			throw new CloudIntegrationApiError(message);
		}
		if (httpStatus == HttpUtil.HTTP_NOT_FOUND_STATUS_CODE) {
			String message = String.format("Iflow artifact ID '%s' could not be found", iflowArtifactId);
			throw new CloudIntegrationApiError(message);			
		}
		// At this point, the HTTP status should be OK.
		if (httpStatus != HttpUtil.HTTP_OKAY_STATUS_CODE) {
			String message = String.format("Unexpected HTTP status code %d when retrieving iflow artifact ID '%s'", httpStatus, iflowArtifactId);
			throw new CloudIntegrationApiError(message);
		}
		// Check that we have the expected response content type.
		Optional<String> contentType = apiResponse.headers().firstValue(HttpUtil.RESPONSE_HEADER_CONTENT_TYPE);
		if (contentType.isPresent() && !contentType.get().equals(EXPECTED_IFLOW_ARTIFACT_RESPONSE_TYPE)) {
			String message = String.format("Unexpected response content type '%s' when retrieving iflow artifact ID '%s'", contentType.get(), iflowArtifactId);
			throw new CloudIntegrationApiError(message);
		}
		IflowArtifact iflowArtifact;
		try {
			iflowArtifact = ZipArchiveIflowArtifact.fromArchiveStream(apiResponse.body());
		} catch (IOException | SaxonApiException e) {
			throw new CloudIntegrationApiError("Error while processing iflow artifact response", e);
		}
		// If we have package info cached for this iflow, add it to the IflowArtifactTag.
		if (iflowIdToPackageInfo.containsKey(iflowArtifactId)) {
			PackageInfo packageInfo = iflowIdToPackageInfo.get(iflowArtifactId);
			iflowArtifact.getTag().setPackageInfo(packageInfo);
		}
		return iflowArtifact;
	}

	@Override
	public Set<String> getEditableIntegrationPackageIds(boolean skipSapPackages) {
		/*
		 * Note: In addition to returning package IDs, this call also caches package
		 * info for use later.
		 */
		logger.info("Retrieving package IDs from tenant");
		logger.debug(skipSapPackages ? "SAP packages will be skipped" : "SAP packages will be included");
		XQueryEvaluator evaluator = createXqueryEvaluator("package-info-from-api-response.xquery");
		evaluator.setExternalVariable(new QName("skipSapPackages"), new XdmAtomicValue(skipSapPackages));
		// Make the API call and run the XQuery query on the response.
		HttpResponse<InputStream> apiResponse = getApiResponse(integrationPackagesUri());
		XdmValue result = evaluateXquery(apiResponse.body(), evaluator);
		/*
		 * The resulting sequence must either be empty, or the number of elements
		 * must be a multiple of two (since the sequence consists of pairs of
		 * package ID and name).
		 */
		if (!(result.size() == 0 || result.size() % 2 == 0)) {
			throw new CloudIntegrationApiError(String.format("Unexpected size (%d) of sequence returned by XQuery query", result.size()));
		}
		/*
		 * Now process the sequence two elements at a time, storing the package ID in
		 * the Set we will return and caching the package information for later.
		 */
		Iterator<XdmItem> itemIterator = result.iterator();
		Set<String> packageIds = new HashSet<>();
		while (itemIterator.hasNext()) {
			String packageId = itemIterator.next().getStringValue();
			String packageName = itemIterator.next().getStringValue();
			PackageInfo packageInfo = new PackageInfo(packageId, packageName);
			assert !packageIds.contains(packageId);
			assert !packageIdToPackageInfo.containsKey(packageId);
			packageIds.add(packageId);
			packageIdToPackageInfo.put(packageId, packageInfo);
		}
		logger.debug("{} package IDs retrieved: {}", packageIds.size(), packageIds.stream().collect(Collectors.joining(",")));
		return packageIds;
	}

	@Override
	public Set<String> getIflowArtifactIdsFromPackage(String packageId, boolean skipDrafts) {
		/*
		 * Note: In addition to returning iflow IDs, this call also caches an iflow ID to package
		 * info Map for use later.
		 */
		Objects.requireNonNull(packageId, "packageId must not be null");
		if (packageId.isBlank()) {
			throw new IllegalArgumentException("packageId must not be blank");
		}
		logger.info("Retrieving iflow artifact IDs from package {}", packageId);
		logger.debug(skipDrafts ? "Draft iflows will be skipped" : "Draft iflows will be included");
		XQueryEvaluator evaluator = createXqueryEvaluator("iflow-artifact-ids-from-api-response.xquery");
		evaluator.setExternalVariable(new QName("skipDrafts"), new XdmAtomicValue(skipDrafts));
		Set<String> iflowArtifactIds = getApiResponseAndEvaluateXquery(iflowArtifactsUriFromPackageId(packageId), evaluator);
		logger.debug("{} iflow artifact IDs retrieved from package {}: {}", iflowArtifactIds.size(), packageId, iflowArtifactIds.stream().collect(Collectors.joining(",")));
		// Cache package info for each iflow.
		for (String iflowArtifactId : iflowArtifactIds) {
			assert packageIdToPackageInfo.containsKey(packageId);
			iflowIdToPackageInfo.put(iflowArtifactId, packageIdToPackageInfo.get(packageId));
		}
		return iflowArtifactIds;
	}

	private URI tenantUriFromPath(String path) {
		assert path != null;
		assert !path.isBlank();
		URI uri;
		try {
			uri = new URI(URI_SCHEME, hostname, path, null); // The null indicates no fragment, i.e. no location specified with #location.
		} catch (URISyntaxException e) {
			throw new CloudIntegrationApiError("Bad tenant URI error", e);
		}
		return uri;
	}
	
	private URI iflowArtifactUriFromIflowArtifactId(String iflowArtifactId) {
		assert iflowArtifactId != null;
		assert !iflowArtifactId.isBlank();
		StringBuilder pathBuilder = new StringBuilder();
		pathBuilder.append(ODATA_API_BASE_PATH);
		pathBuilder.append("IntegrationDesigntimeArtifacts(Id='");
		pathBuilder.append(iflowArtifactId);
		pathBuilder.append("',Version='active')/$value");
		URI iflowArtifactUri = tenantUriFromPath(pathBuilder.toString());
		logger.debug("Iflow artifact URI generated for ID {}: {}", iflowArtifactId, iflowArtifactUri);
		return iflowArtifactUri;
	}
	
	private URI integrationPackagesUri() {
		StringBuilder pathBuilder = new StringBuilder();
		pathBuilder.append(ODATA_API_BASE_PATH);
		pathBuilder.append("IntegrationPackages");
		URI integrationPackagesUri = tenantUriFromPath(pathBuilder.toString());
		logger.debug("Integration packages URI generated: {}", integrationPackagesUri);
		return integrationPackagesUri;
	}
	
	private URI iflowArtifactsUriFromPackageId(String packageId) {
		assert packageId != null;
		assert !packageId.isBlank();
		StringBuilder pathBuilder = new StringBuilder();
		pathBuilder.append(ODATA_API_BASE_PATH);
		pathBuilder.append("IntegrationPackages('");
		pathBuilder.append(packageId);
		pathBuilder.append("')/IntegrationDesigntimeArtifacts");
		URI iflowArtifactsUri = tenantUriFromPath(pathBuilder.toString());
		logger.debug("Iflow artifacts URI generated for package {}: {}", packageId, iflowArtifactsUri);
		return iflowArtifactsUri;
	}

	private String authorizationHeaderValue() {
		String headerValue;
		if (authMode == AuthMode.BASIC_AUTH) {
			assert apiUsername != null;
			assert apiPassword != null;
			// TODO: Duplicated in AuthorizationServer; could be moved to HttpUtil
			headerValue = "Basic " + Base64.getEncoder().encodeToString((apiUsername + ":" + new String(apiPassword)).getBytes(StandardCharsets.UTF_8));
		} else if (authMode == AuthMode.OAUTH_CLIENT_CREDENTIALS) {
			assert accessToken != null;
			headerValue = "Bearer " + accessToken.getToken();
		} else {
			// This should never happen.
			throw new AssertionError("Unexpected authentication mode");
		}
	    return headerValue;
	}
	
	private HttpResponse<InputStream> httpGetRequest(URI uri) {
		return httpGetRequest(uri, false);
	}

	private HttpResponse<InputStream> httpGetRequest(URI uri, boolean tokenExpired) {
		assert uri != null;
		assert !(tokenExpired && authMode == AuthMode.BASIC_AUTH) : "Expired tokens only make sense in OAuth Client Credentials mode";
		/*
		 * Make sure we have a valid access token if the authentication mode
		 * is OAuth Client Credentials.
		 */
		if (authMode == AuthMode.OAUTH_CLIENT_CREDENTIALS) {
			if (accessToken == null || !accessToken.isValid()) {
				if (accessToken == null) {
					logger.info("Requesting first access token");
				} else {
					logger.info("Access token has expired; requesting a new one");
				}
				accessToken = authServer.requestAccessToken();
				assert accessToken.isValid();
			}
		}
		HttpRequest request = HttpRequest.newBuilder()
           	.uri(uri)
			.header(HttpUtil.REQUEST_HEADER_AUTHORIZATION, authorizationHeaderValue())
           	.GET()
            .build();
        HttpResponse<InputStream> response;
		try {
			response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {
			throw new CloudIntegrationApiError("HTTP request error", e);
		}
		final int httpStatus = response.statusCode();
		if (httpStatus == HttpUtil.HTTP_UNAUTHORIZED_STATUS_CODE) {
			/*
			 * If we are in OAuth Client Credentials mode and the access token has expired,
			 * we retry the HTTP request once with a new access token. In all other cases,
			 * nothing further can be done so we throw an exception.
			 */
			if (authMode == AuthMode.OAUTH_CLIENT_CREDENTIALS && !accessToken.isValid() && !tokenExpired) {
				logger.info("API call failed on authentication and access token has expired; retrying once with a new token");
				return httpGetRequest(uri, true);
			} else {
				throw new CloudIntegrationApiError(MESSAGE_NOT_AUTHENTICATED);
			}
		}
		if (httpStatus == HttpUtil.HTTP_FORBIDDEN_STATUS_CODE) {
			// This means insufficient authorizations regardless of authentication mode.
			throw new CloudIntegrationApiError(MESSAGE_NOT_AUTHORIZED);
		}
		return response;
	}

	private XQueryEvaluator createXqueryEvaluator(String xqueryFilename) {
		assert xqueryFilename != null;
		assert !xqueryFilename.isBlank();
		String xquery = JarResourceUtil.loadXqueryResource(xqueryFilename);
		XQueryExecutable exe;
		try {
			exe = xqueryCompiler.compile(xquery);
		} catch (SaxonApiException e) {
			throw new CloudIntegrationApiError("Error compiling XQuery file", e);
		}
		return exe.load();
	}

	private HttpResponse<InputStream> getApiResponse(URI uri) {
		assert uri != null;
		HttpResponse<InputStream> apiResponse = httpGetRequest(uri);
		final int httpStatus = apiResponse.statusCode();
		/*
		 * HTTP status codes are checked in httpGetRequest, so anything but HTTP status
		 * OK at this point is an error.
		 */
		if (httpStatus != HttpUtil.HTTP_OKAY_STATUS_CODE) {
			String message = String.format("API responded with unexpected HTTP status code %d", httpStatus);
			throw new CloudIntegrationApiError(message);
		}
		return apiResponse;
	}

	private Set<String> getApiResponseAndEvaluateXquery(URI uri, XQueryEvaluator evaluator) {
		assert uri != null;
		assert evaluator != null;
		HttpResponse<InputStream> apiResponse = getApiResponse(uri);
		// Execute the XQuery query.
		XdmValue result = evaluateXquery(apiResponse.body(), evaluator);
		// Return a Set of the string values of every item in the result sequence.
		return result
			.stream()
			.map(XdmItem::getStringValue)
			.collect(Collectors.toSet());
	}

	private static XdmValue evaluateXquery(InputStream document, XQueryEvaluator evaluator){
		assert document != null;
		assert evaluator != null;
		XdmValue result;
		try {
			evaluator.setSource(new StreamSource(document));
			result = evaluator.evaluate();
		} catch (SaxonApiException e) {
			throw new CloudIntegrationApiError("Error evaluating XQuery", e);
		}
		return result;
	}
	
}