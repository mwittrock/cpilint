package dk.mwittrock.cpilint.suppliers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

public final class TenantIflowArtifactSupplier implements IflowArtifactSupplier {

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
	
	private static final Logger logger = LoggerFactory.getLogger(TenantIflowArtifactSupplier.class);
	private final String tmnHost;
	private final String apiUsername;
	private final char[] apiPassword;
	private final boolean fetchSingleIflowArtifacts;
	private final HttpClient httpClient;
	private final XQueryCompiler xqueryCompiler;
	private Iterator<String> iflowArtifactIdIterator;
	private boolean skipSapPackages;
	private Set<String> skipIflowArtifactIds;
	public int artifactsSupplied = 0;
	
	private TenantIflowArtifactSupplier(String tmnHost, String apiUsername, char[] apiPassword, boolean fetchSingleIflowArtifacts) {
		this.tmnHost = Objects.requireNonNull(tmnHost, "tenantHost must not be null");
		this.apiUsername = Objects.requireNonNull(apiUsername, "apiUsername must not be null");
		this.apiPassword = Objects.requireNonNull(apiPassword, "apiPassword must not be null");
		this.fetchSingleIflowArtifacts = fetchSingleIflowArtifacts;
        httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
		xqueryCompiler = new Processor(false).newXQueryCompiler();
	}

	public TenantIflowArtifactSupplier(String tmnHost, String apiUsername, char[] apiPassword, boolean skipSapPackages, Set<String> skipIflowArtifactIds) {
		this(tmnHost, apiUsername, apiPassword, false);
		iflowArtifactIdIterator = null; // Will be set in the setup() method.
		this.skipSapPackages = skipSapPackages;
		// It's okay for skipIds to reference an empty Set, but it must not be null.
		Objects.requireNonNull(skipIflowArtifactIds, "skipIflowArtifactIds must not be null");
		this.skipIflowArtifactIds = new HashSet<>(skipIflowArtifactIds);
	}
	
	public TenantIflowArtifactSupplier(String tmnHost, String apiUsername, char[] apiPassword, Set<String> fetchIflowArtifactIds) {
		this(tmnHost, apiUsername, apiPassword, true);
		// fetchIflowArtifactIds must not be null, and must not be empty.
		Objects.requireNonNull(fetchIflowArtifactIds, "fetchIflowArtifactIds must not be null");
		if (fetchIflowArtifactIds.isEmpty()) {
			throw new IllegalArgumentException("No iflow artifact IDs provided");
		}
		iflowArtifactIdIterator = new HashSet<>(fetchIflowArtifactIds).iterator();
	}
	
	@Override
	public void setup() {
		try {
			if (!fetchSingleIflowArtifacts) {
				iflowArtifactIdIterator = retrieveIflowArtifactIdsFromTenant().iterator();
			}
		} catch (SaxonApiException e) {
			throw new IflowArtifactSupplierError("Error while processing package contents API response", e);
		}
	}

	@Override
	public IflowArtifact supply() {
		checkForNullIterator();
		if (!canSupply()) {
			throw new IllegalStateException("supply() called even though canSupply() returns false");
		}
		String iflowArtifactId = iflowArtifactIdIterator.next();
		InputStream iflowArtifactInputStream = retrieveIflowArtifact(iflowArtifactId);
		IflowArtifact iflowArtifact;
		try {
			iflowArtifact = ZipArchiveIflowArtifact.from(iflowArtifactInputStream);
		} catch (IOException | SaxonApiException e) {
			throw new IflowArtifactSupplierError("Error while processing iflow artifact contents", e);
		}
		artifactsSupplied++;
		return iflowArtifact;
	}

	@Override
	public boolean canSupply() {
		checkForNullIterator();
		boolean canSupply = iflowArtifactIdIterator.hasNext();
		/*
		 *  If no more iflow artifacts can be supplied, we no longer need the
		 *  API password, and we therefore blank it out for security purposes.
		 */
		if (!canSupply) {
			Arrays.fill(apiPassword, ' ');
		}
		return canSupply;
	}

	@Override
	public void shutdown() {
		// No shutdown steps needed.
	}
	
	@Override
	public int artifactsSupplied() {
		return artifactsSupplied;
	}

	private void checkForNullIterator() {
		if (iflowArtifactIdIterator == null) {
			throw new IllegalStateException("iflowArtifactIdIterator not initialized");
		}
	}

	private URI tenantUriFromPath(String path) {
		URI uri;
		try {
			uri = new URI(URI_SCHEME, tmnHost, path, null); // The null indicates no fragment, i.e. no location specified with #location.
		} catch (URISyntaxException e) {
			throw new IflowArtifactSupplierError("Bad tenant URI error", e);
		}
		return uri;
	}
	
	private URI iflowArtifactUriFromIflowArtifactId(String iflowArtifactId) {
		StringBuilder pathBuilder = new StringBuilder();
		pathBuilder.append(ODATA_API_BASE_PATH);
		pathBuilder.append("IntegrationDesigntimeArtifacts(Id='");
		pathBuilder.append(iflowArtifactId);
		pathBuilder.append("',Version='active')/$value");
		URI iflowArtifactUri = tenantUriFromPath(pathBuilder.toString());
		logger.debug("Iflow artifact URI generated for ID {}: {}", iflowArtifactId, iflowArtifactUri);
		return iflowArtifactUri;
	}

	private URI contentPackagesUri() {
		StringBuilder pathBuilder = new StringBuilder();
		pathBuilder.append(ODATA_API_BASE_PATH);
		pathBuilder.append("IntegrationPackages");
		URI contentPackagesUri = tenantUriFromPath(pathBuilder.toString());
		logger.debug("Content packages URI generated: {}", contentPackagesUri);
		return contentPackagesUri;
	}
	
	private URI artifactsUriFromPackageId(String packageId) {
		StringBuilder pathBuilder = new StringBuilder();
		pathBuilder.append(ODATA_API_BASE_PATH);
		pathBuilder.append("IntegrationPackages('");
		pathBuilder.append(packageId);
		pathBuilder.append("')/IntegrationDesigntimeArtifacts");
		URI artifactsUri = tenantUriFromPath(pathBuilder.toString());
		logger.debug("Artifacts URI generated for package {}: {}", packageId, artifactsUri);
		return artifactsUri;
	}

	private String basicAuthHeaderValue() {
	    return "Basic " + Base64.getEncoder().encodeToString((apiUsername + ":" + new String(apiPassword)).getBytes(StandardCharsets.UTF_8));
	}
	
	private HttpResponse<InputStream> httpGetRequest(URI uri) {
		/*
		 * Why do we always send basic authentication credentials? The private
		 * OData API does not return 401 and a challenge if credentials are 
		 * missing, and the Java 11 HTTP client does not send basic authentication
		 * credentials unless so challenged. Setting an Authenticator therefore
		 * only worked with the public API, and for now we're stuck with both the
		 * public and the private OData API.
		 */
        HttpRequest request = HttpRequest.newBuilder()
           	.uri(uri)
           	.header("Authorization", basicAuthHeaderValue())
           	.GET()
            .build();
        HttpResponse<InputStream> response;
		try {
			response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {
			throw new IflowArtifactSupplierError("HTTP request error", e);
		}
		return response;
	}
	
	private InputStream retrieveIflowArtifact(String iflowArtifactId) {
		logger.debug("Retrieving iflow artifact: {}", iflowArtifactId);
		URI uri = iflowArtifactUriFromIflowArtifactId(iflowArtifactId);
		HttpResponse<InputStream> response = httpGetRequest(uri);
		if (response.statusCode() == HTTP_BAD_REQUEST_STATUS_CODE) {
			/*
			 * This probably means that the iflow artifact's package is read-only.
			 * This should never happen, unless the user provided the iflow
			 * artifact ID. The exception message is adjusted accordingly.
			 */
			String message;
			if (fetchSingleIflowArtifacts) {
				message = String.format("HTTP status 400 Bad Request returned for iflow artifact ID '%s'. This probably means that the artifact's package is read-only.", iflowArtifactId);
			} else {
				message = String.format("Unexpected HTTP status 400 Bad Request for iflow artifact ID '%s'", iflowArtifactId);
			}
			throw new IflowArtifactSupplierError(message);
		}
		if (response.statusCode() == HTTP_UNAUTHORIZED_STATUS_CODE) {
			throw new IflowArtifactSupplierError(MESSAGE_NOT_AUTHENTICATED);
		}
		if (response.statusCode() == HTTP_FORBIDDEN_STATUS_CODE) {
			throw new IflowArtifactSupplierError(MESSAGE_NOT_AUTHORIZED);
		}
		if (response.statusCode() == HTTP_NOT_FOUND_STATUS_CODE) {
			/*
			 * The provided iflow artifact ID could not be found in the tenant.
			 * This should never happen, unless the user provided the iflow
			 * artifact ID. The exception message is adjusted accordingly.
			 */
			String message;
			if (fetchSingleIflowArtifacts) {
				message = String.format("Iflow artifact ID '%s' could not be found", iflowArtifactId);
			} else {
				message = String.format("Unexpected HTTP status 404 Not Found for iflow artifact ID '%s'", iflowArtifactId);
			}
			throw new IflowArtifactSupplierError(message);			
		}
		// If we get this far, the HTTP status code _should_ be 200.
		if (response.statusCode() != HTTP_OKAY_STATUS_CODE) {
			String message = String.format("Unexpected HTTP status code %d when retrieving iflow artifact ID '%s'", response.statusCode(), iflowArtifactId);
			throw new IflowArtifactSupplierError(message);
		}
		// Check that we have the expected response type.
		Optional<String> responseType = response.headers().firstValue(CONTENT_TYPE_RESPONSE_HEADER);
		if (responseType.isPresent() && !responseType.get().equals(EXPECTED_IFLOW_ARTIFACT_RESPONSE_TYPE)) {
			String message = String.format("Unexpected response type '%s' when retrieving iflow artifact from OData API", responseType.get());
			throw new IflowArtifactSupplierError(message);
		}
		return response.body();
	}

	private Set<String> retrieveIflowArtifactIdsFromTenant() throws SaxonApiException {
		logger.info("Retrieving all artifact IDs from tenant");
		Set<String> iflowArtifactIds = new HashSet<>();
		// An XQueryEvaluator object can be safely reused within a single thread.
		XQueryEvaluator evaluator = createXqueryEvaluator("iflow-artifact-ids-from-api-response.xquery");
		for (String packageId : retrievePackageIdsFromTenant()) {
			iflowArtifactIds.addAll(retrieveIflowArtifactIdsFromPackage(packageId, evaluator));
		}
		return Collections.unmodifiableSet(iflowArtifactIds);
	}
	
	private Set<String> retrievePackageIdsFromTenant() throws SaxonApiException {
		/*
		 *  Which XQuery query to execute depends on whether we want to skip
		 *  SAP packages or not.
		 */
		logger.info("Retrieving all package IDs from tenant");
		logger.debug(skipSapPackages ? "SAP packages will be skipped" : "SAP packages will be included");
		String xqueryFile = skipSapPackages ? "package-ids-from-api-response.xquery" : "package-ids-from-api-response-include-sap.xquery";
		XQueryEvaluator evaluator = createXqueryEvaluator(xqueryFile);
		Set<String> packages = getApiResponseAndEvaluateXquery(contentPackagesUri(), evaluator);
		logger.debug("{} package IDs retrieved: {}", packages.size(), packages.stream().collect(Collectors.joining(",")));
		return packages;
	}
	
	private Set<String> retrieveIflowArtifactIdsFromPackage(String packageId, XQueryEvaluator evaluator) throws SaxonApiException {
		assert skipIflowArtifactIds != null;
		Set<String> iflowArtifactIds = getApiResponseAndEvaluateXquery(artifactsUriFromPackageId(packageId), evaluator); 
		Set<String> filteredIds = iflowArtifactIds
			.stream()
			.filter(i -> !skipIflowArtifactIds.contains(i))
			.collect(Collectors.toSet());
		logger.debug("{} iflow artifact IDs retrieved from package {}: {}", 
				filteredIds.size(),
				packageId,
				filteredIds.stream().collect(Collectors.joining(",")));
		// If iflow artifact IDs were actually skipped, the log should reflect that.
		if (filteredIds.size() < iflowArtifactIds.size()) {
			logger.debug("The following iflow artifact IDs were skipped: {}",
			iflowArtifactIds.stream().filter(i -> !filteredIds.contains(i)).collect(Collectors.joining(",")));
		}
		return filteredIds;
	}
	
	private Set<String> getApiResponseAndEvaluateXquery(URI uri, XQueryEvaluator evaluator) throws SaxonApiException {
		HttpResponse<InputStream> response = httpGetRequest(uri);
		// Check for HTTP status 401 Unauthorized.
		if (response.statusCode() == HTTP_UNAUTHORIZED_STATUS_CODE) {
			throw new IflowArtifactSupplierError(MESSAGE_NOT_AUTHENTICATED);
		}
		if (response.statusCode() == HTTP_FORBIDDEN_STATUS_CODE) {
			throw new IflowArtifactSupplierError(MESSAGE_NOT_AUTHORIZED);
		}
		// At this point anything but HTTP status 200 OK is an error.
		if (response.statusCode() != HTTP_OKAY_STATUS_CODE) {
			String message = String.format("Unexpected HTTP status code %d when retrieving packages from tenant", response.statusCode());
			throw new IflowArtifactSupplierError(message);
		}
		// Execute the XQuery query.
		XdmValue result = evaluateXquery(response.body(), evaluator);
		// Extract the IDs and return them.
		return result
			.stream()
			.map(XdmItem::getStringValue)
			.collect(Collectors.toSet());
	}
	
	private XQueryEvaluator createXqueryEvaluator(String xqueryFilename) throws SaxonApiException {
		String xquery = JarResourceUtil.loadXqueryResource(xqueryFilename);
		XQueryExecutable exe = xqueryCompiler.compile(xquery);
		return exe.load();
	}
	
	private static XdmValue evaluateXquery(InputStream apiResponse, XQueryEvaluator evaluator) throws SaxonApiException {
		evaluator.setSource(new StreamSource(apiResponse));
		return evaluator.evaluate();
	}

}
