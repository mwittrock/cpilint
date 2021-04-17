package dk.mwittrock.cpilint.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.ZipArchiveIflowArtifact;
import dk.mwittrock.cpilint.util.JarResourceUtil;
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

	private static final String AUTHORIZATION_REQUEST_HEADER = "Authorization";
	private static final String MESSAGE_NOT_AUTHENTICATED = "Wrong username, password or both";
	private static final String MESSAGE_NOT_AUTHORIZED = "User was authenticated but lacks authorizations";
	private static final String ODATA_API_BASE_PATH = "/api/v1/";
	private static final String URI_SCHEME = "https";
	private static final String EXPECTED_IFLOW_ARTIFACT_RESPONSE_TYPE = "application/zip";
	private static final String CONTENT_TYPE_RESPONSE_HEADER = "content-type";
	private static final int HTTP_OKAY_STATUS_CODE = 200;
	private static final int HTTP_BAD_REQUEST_STATUS_CODE = 400;
	private static final int HTTP_UNAUTHORIZED_STATUS_CODE = 401;
	private static final int HTTP_FORBIDDEN_STATUS_CODE = 403;
	private static final int HTTP_NOT_FOUND_STATUS_CODE = 404;

	private static final Logger logger = LoggerFactory.getLogger(CloudIntegrationOdataApi.class);
	private final String tmnHost;
	private final String apiUsername;
	private final char[] apiPassword;
	private final HttpClient httpClient;
	private final XQueryCompiler xqueryCompiler;
	
	public CloudIntegrationOdataApi(String tmnHost, String apiUsername, char[] apiPassword) {
		this.tmnHost = Objects.requireNonNull(tmnHost, "tmnHost must not be null");
		this.apiUsername = Objects.requireNonNull(apiUsername, "apiUsername must not be null");
		this.apiPassword = Objects.requireNonNull(apiPassword, "apiPassword must not be null");
        httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
		xqueryCompiler = new Processor(false).newXQueryCompiler();		
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
		if (httpStatus == HTTP_BAD_REQUEST_STATUS_CODE) {
			String message = String.format("HTTP status 400 Bad Request returned for iflow artifact ID '%s', indicating that its package is read-only", iflowArtifactId);
			throw new CloudIntegrationApiError(message);
		}
		if (httpStatus == HTTP_UNAUTHORIZED_STATUS_CODE) {
			throw new CloudIntegrationApiError(MESSAGE_NOT_AUTHENTICATED);
		}
		if (httpStatus == HTTP_FORBIDDEN_STATUS_CODE) {
			throw new CloudIntegrationApiError(MESSAGE_NOT_AUTHORIZED);
		}
		if (httpStatus == HTTP_NOT_FOUND_STATUS_CODE) {
			String message = String.format("Iflow artifact ID '%s' could not be found", iflowArtifactId);
			throw new CloudIntegrationApiError(message);			
		}
		// At this point, the HTTP status code should be 200.
		if (httpStatus != HTTP_OKAY_STATUS_CODE) {
			String message = String.format("Unexpected HTTP status code %d when retrieving iflow artifact ID '%s'", httpStatus, iflowArtifactId);
			throw new CloudIntegrationApiError(message);
		}
		// Check that we have the expected response content type.
		Optional<String> contentType = apiResponse.headers().firstValue(CONTENT_TYPE_RESPONSE_HEADER);
		if (contentType.isPresent() && !contentType.get().equals(EXPECTED_IFLOW_ARTIFACT_RESPONSE_TYPE)) {
			String message = String.format("Unexpected response content type '%s' when retrieving iflow artifact ID '%s'", contentType.get(), iflowArtifactId);
			throw new CloudIntegrationApiError(message);
		}
		IflowArtifact iflowArtifact;
		try {
			iflowArtifact = ZipArchiveIflowArtifact.from(apiResponse.body());
		} catch (IOException | SaxonApiException e) {
			throw new CloudIntegrationApiError("Error while processing iflow artifact response", e);
		}
		return iflowArtifact;
	}

	@Override
	public Set<String> getEditableIntegrationPackageIds(boolean skipSapPackages) {
		logger.info("Retrieving package IDs from tenant");
		logger.debug(skipSapPackages ? "SAP packages will be skipped" : "SAP packages will be included");
		XQueryEvaluator evaluator = createXqueryEvaluator("package-ids-from-api-response.xquery");
		evaluator.setExternalVariable(new QName("skipSapPackages"), new XdmAtomicValue(skipSapPackages));
		Set<String> packageIds = getApiResponseAndEvaluateXquery(integrationPackagesUri(), evaluator);
		logger.debug("{} package IDs retrieved: {}", packageIds.size(), packageIds.stream().collect(Collectors.joining(",")));
		return packageIds;
	}

	@Override
	public Set<String> getIflowArtifactIdsFromPackage(String packageId, boolean skipDrafts) {
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
		return iflowArtifactIds;
	}

	private URI tenantUriFromPath(String path) {
		assert path != null;
		assert !path.isBlank();
		URI uri;
		try {
			uri = new URI(URI_SCHEME, tmnHost, path, null); // The null indicates no fragment, i.e. no location specified with #location.
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

	private String basicAuthHeaderValue() {
	    return "Basic " + Base64.getEncoder().encodeToString((apiUsername + ":" + new String(apiPassword)).getBytes(StandardCharsets.UTF_8));
	}
	
	private HttpResponse<InputStream> httpGetRequest(URI uri) {
		assert uri != null;
		/*
		 * Preemptively set the authorization header, since we know in advance
		 * that basic authentication is required.
		 */
        HttpRequest request = HttpRequest.newBuilder()
           	.uri(uri)
           	.header(AUTHORIZATION_REQUEST_HEADER, basicAuthHeaderValue())
           	.GET()
            .build();
        HttpResponse<InputStream> response;
		try {
			response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {
			throw new CloudIntegrationApiError("HTTP request error", e);
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

	private Set<String> getApiResponseAndEvaluateXquery(URI uri, XQueryEvaluator evaluator) {
		assert uri != null;
		assert evaluator != null;
		HttpResponse<InputStream> apiResponse = httpGetRequest(uri);
		final int httpStatus = apiResponse.statusCode();
		if (httpStatus == HTTP_UNAUTHORIZED_STATUS_CODE) {
			throw new CloudIntegrationApiError(MESSAGE_NOT_AUTHENTICATED);
		}
		if (httpStatus == HTTP_FORBIDDEN_STATUS_CODE) {
			throw new CloudIntegrationApiError(MESSAGE_NOT_AUTHORIZED);
		}
		// At this point, anything but HTTP status 200 OK is an error.
		if (httpStatus != HTTP_OKAY_STATUS_CODE) {
			String message = String.format("API responded with unexpected HTTP status code %d", httpStatus);
			throw new CloudIntegrationApiError(message);
		}
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