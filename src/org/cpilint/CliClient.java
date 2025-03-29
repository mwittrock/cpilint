package org.cpilint;

import java.io.Console;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import org.json.JSONException;

import ch.qos.logback.classic.Level;
import org.cpilint.api.CloudIntegrationApi;
import org.cpilint.api.CloudIntegrationOdataApi;
import org.cpilint.auth.AuthorizationServer;
import org.cpilint.consumers.ConsoleIssueConsumer;
import org.cpilint.consumers.ExemptionFilteringIssueConsumer;
import org.cpilint.consumers.IssueConsumer;
import org.cpilint.rules.RuleFactoryError;
import org.cpilint.suppliers.DirectoryIflowArtifactSupplier;
import org.cpilint.suppliers.FileIflowArtifactSupplier;
import org.cpilint.suppliers.IflowArtifactSupplier;
import org.cpilint.suppliers.IflowArtifactSupplierError;
import org.cpilint.suppliers.TenantAllArtifactsSupplier;
import org.cpilint.suppliers.TenantIndividualPackagesSupplier;
import org.cpilint.suppliers.TenantSingleArtifactsSupplier;
import org.cpilint.suppliers.UnpackedFileIflowArtifactSupplier;
import org.cpilint.util.HttpUtil;

public final class CliClient {
	
	private static final Logger logger = LoggerFactory.getLogger(CliClient.class);
	private static final String VERSION = "1.0.5";
	private static final String COPYRIGHT = "2019-2025 Morten N. Wittrock";
	private static final List<String> LOG_SYSTEM_PROPERTIES = List.of("java.version", "java.vendor", "java.vm.version", "java.vm.vendor", "java.vm.name", "os.name", "os.arch", "os.version");
	private static final int EXIT_STATUS_SUCCESS = 0;
	private static final int EXIT_STATUS_ISSUES = 1;
	private static final int EXIT_STATUS_ERRORS = 2;
	private static final String COMMAND_LINE_ERROR_MESSAGE = "There is a problem with your command line arguments. Please run cpilint -help for usage information.";
	private static final String CLI_OPTION_HELP = "help";
	private static final String CLI_OPTION_VERSION = "version";
	private static final String CLI_OPTION_SKIP_IFLOWS = "skip-iflows";
	private static final String CLI_OPTION_SKIP_SAP_PACKAGES = "skip-sap-packages";
	private static final String CLI_OPTION_SKIP_PACKAGES = "skip-packages";
	private static final String CLI_OPTION_SKIP_DRAFTS = "skip-drafts";
	private static final String CLI_OPTION_IFLOWS = "iflows";
	private static final String CLI_OPTION_PACKAGES = "packages";
	private static final String CLI_OPTION_PASSWORD = "password";
	private static final String CLI_OPTION_USERNAME = "username";
	private static final String CLI_OPTION_HOST = "host";
	private static final String CLI_OPTION_FILES = "files";
	private static final String CLI_OPTION_UNPACKED_FILES = "unpacked-files";
	private static final String CLI_OPTION_DIRECTORY = "directory";
	private static final String CLI_OPTION_RULES = "rules";
	private static final String CLI_OPTION_BORING = "boring";
	private static final String CLI_OPTION_DEBUG = "debug";
	private static final String CLI_OPTION_VERCHECK = "vercheck";
	private static final String CLI_OPTION_SKIPVERCHECK = "skipvercheck";
	private static final String CLI_OPTION_KEY = "key";
	private static final String CPILINT_WIKI_URL = "https://github.com/mwittrock/cpilint/wiki";
	private static final String SERVICE_KEY_FIELD_OAUTH = "oauth";
	private static final String SERVICE_KEY_FIELD_CLIENTSECRET = "clientsecret";
	private static final String SERVICE_KEY_FIELD_CLIENTID = "clientid";
	private static final String SERVICE_KEY_FIELD_URL = "url";
	private static final String SERVICE_KEY_FIELD_TOKENURL = "tokenurl";
	private static final String SERVICE_KEY_VALIDATION_ERROR = "Service key JSON does not have the expected format.";
	private static final String LATEST_RELEASE_DOWNLOAD_URL = "https://cpilint.org/latest";
	
	private static enum RunMode {
		VERSION_MODE,
		VERSION_CHECK_MODE,
		HELP_MODE,
		FILE_SUPPLIER_MODE,
		UNPACKED_FILE_SUPPLIER_MODE,
		DIRECTORY_SUPPLIER_MODE,
		TENANT_SUPPLIER_SINGLE_MODE,
		TENANT_SUPPLIER_MULTI_MODE,
		TENANT_SUPPLIER_PACKAGES_MODE
	}
	
	private CliClient() {
		throw new AssertionError("Never supposed to be instantiated");
	}

	public static void main(String[] args) {
		/*
		 * The actual launcher code is in the run method. It _should_ catch all unchecked
		 * exceptions thrown by CPILint. The call to run is wrapped in a try/catch block
		 * below to get a last chance to catch anything previously uncaught. This ensures
		 * that CPILint always exits with the documented status code.
		 */
		try {
			run(args);
		} catch (Throwable t) {
			logger.error("Previously uncaught Throwable", t);
			exitWithErrorMessage("Uncaught Throwable; run CPILint in debug mode for more information.");
		}
	}

	private static void run(String[] args) {
		// If logging is requested, it should be set up before anything else.
		configureLogging(args);
		logger.info("CPILint version {}", VERSION);
		for (String prop : LOG_SYSTEM_PROPERTIES) {
			logger.debug("{} = {}", prop, System.getProperty(prop));
		}
		logger.info("Launching CliClient");
		logArguments(args);
		// Parse the command line arguments.
		CommandLine cl = parseArguments(args);
		// Determine the run mode.
		RunMode mode = determineRunMode(cl);
		logger.info("Run mode detected: {}", mode);
		// The help, version and version check modes are handled separately.
		if (mode == RunMode.HELP_MODE) {
	        printUsage();
	        System.exit(EXIT_STATUS_SUCCESS);
		}
		if (mode == RunMode.VERSION_MODE) {
			printVersionBanner();
			System.exit(EXIT_STATUS_SUCCESS);
		}
		if (mode == RunMode.VERSION_CHECK_MODE) {
			versionCheck(true);
			System.exit(EXIT_STATUS_SUCCESS);
		}
		/*
		 * Prepare to run a test. The rules file is processed first, so that
		 * the user is not asked to needlessly type in their password, if
		 * it turns out there's a problem with the rules file.  
		 */
		RulesFile rulesFile = rulesFileFromCommandLine(cl);
		IflowArtifactSupplier supplier = supplierFromCommandLine(mode, cl);
		/*
		 * If there are any exemptions, use the IssueConsumer that filters out
		 * issues based on exemptions. Otherwise use the default IssueConsumer.
		 */
		IssueConsumer consumer = rulesFile.getExemptions().isEmpty() 
			? new ConsoleIssueConsumer()
			: new ExemptionFilteringIssueConsumer(rulesFile.getExemptions(), new ConsoleIssueConsumer());
		// Print the version banner and ASCII art (unless we're in boring mode).
		printVersionBanner();
		System.out.println();
		if (!cl.hasOption(CLI_OPTION_BORING)) {
			printAsciiArtLogo();
			System.out.println();
			easter();
		}
		// Run the version check (unless asked not to).
		if (cl.hasOption(CLI_OPTION_SKIPVERCHECK)) {
			logger.info("Skipping version check");
		} else {
			logger.info("Performing version check");
			versionCheck(false);
		}
		// Now, create a CpiLint object and run the test.
		CpiLint linter = new CpiLint(supplier, rulesFile.getRules(), consumer);
		try {
			linter.run();
		} catch (IflowArtifactSupplierError e) {
			logger.error("Iflow artifact supplier error", e);
			exitWithErrorMessage("There was an error while retrieving iflow artifacts: " + e.getMessage());
		} catch (CpiLintError e) {
			logger.error("CPILint error", e);
			exitWithErrorMessage("An error occurred: " + e.getMessage());
		}
		logger.info("Iflow artifacts supplied: {}", supplier.artifactsSupplied());
		logger.info("Issues found: {}", consumer.issuesConsumed());
		if (consumer.issuesConsumed() > 0) {
			System.out.println();
		}
		System.out.println(resultMessage(supplier, consumer));
		int exitStatus = consumer.issuesConsumed() > 0 ? EXIT_STATUS_ISSUES : EXIT_STATUS_SUCCESS;  
		logger.info("Exiting CliClient with status {}", exitStatus);
		System.exit(exitStatus);
	}
	
	private static void logArguments(String[] args) {
		/*
		 * Make sure to not log the tenant password, if one is provided.
		 */
		List<String> cliArgs = new ArrayList<>(Arrays.asList(args));
		int pwdPos = cliArgs.indexOf("-" + CLI_OPTION_PASSWORD);
		if (pwdPos != -1 && pwdPos < cliArgs.size() - 1) {
			/*
			 * The password option is present and it's not the last argument. We'll
			 * assume that the argument following it is the password, and therefore
			 * blank it out with asterisks, preventing it from being written to the log.
			 */
			cliArgs.set(pwdPos + 1, "*******");
		}
		logger.debug("Command line arguments provided: {}", cliArgs.stream().map(a -> "'" + a + "'").collect(Collectors.joining(" ")));
	}
	
	private static RunMode determineRunMode(CommandLine cl) {
		RunMode mode = null;
		if (versionMode(cl)) {
			mode = RunMode.VERSION_MODE;
		} else if (versionCheckMode(cl)) {
			mode = RunMode.VERSION_CHECK_MODE;
		} else if (helpMode(cl)) {
			mode = RunMode.HELP_MODE;
		} else if (fileSupplierMode(cl)) {
			mode = RunMode.FILE_SUPPLIER_MODE;
		} else if (unpackedFileSupplierMode(cl)) {
			mode = RunMode.UNPACKED_FILE_SUPPLIER_MODE;
		} else if (directorySupplierMode(cl)) {
			mode = RunMode.DIRECTORY_SUPPLIER_MODE;
		} else if (tenantSupplierSingleMode(cl)) {
			mode = RunMode.TENANT_SUPPLIER_SINGLE_MODE;
		} else if (tenantSupplierMultiMode(cl)) {
			mode = RunMode.TENANT_SUPPLIER_MULTI_MODE;
		} else if (tenantSupplierPackagesMode(cl)) {
			mode = RunMode.TENANT_SUPPLIER_PACKAGES_MODE;
		} else {
			logger.error("Could not determine run mode from command line arguments");
			exitWithErrorMessage(COMMAND_LINE_ERROR_MESSAGE);
		}
		return mode;
	}

	private static String resultMessage(IflowArtifactSupplier supplier, IssueConsumer consumer) {
		int artifactsSupplied = supplier.artifactsSupplied();
		int issuesConsumed = consumer.issuesConsumed();
		String message;
		if (artifactsSupplied == 0) {
			message = "No iflows were inspected.";
		} else {
			message = String.format("Inspecting %d %s resulted in %d %s found.",
				artifactsSupplied,
				iflowSingularPlural(artifactsSupplied),
				issuesConsumed,
				issueSingularPlural(issuesConsumed));
		}
		return message;
	}
	
	private static String iflowSingularPlural(int count) {
		return count == 1 ? "iflow" : "iflows";
	}
	
	private static String issueSingularPlural(int count) {
		return count == 1 ? "issue" : "issues";
	}

	private static RulesFile rulesFileFromCommandLine(CommandLine cl) {
		/*
		 * We're assuming that the command line arguments have been validated at
		 * this point. I.e. we can safely assume that the -rules option is present
		 * and has exactly one argument.
		 */
		RulesFile rulesFile = null;
		Path rulesPath = Paths.get(cl.getOptionValue(CLI_OPTION_RULES));
		if (Files.notExists(rulesPath)) {
			logger.error("Rules file does not exist: '{}'", rulesPath);
			exitWithErrorMessage("The provided rules file does not exist.");
		}
		if (!Files.isRegularFile(rulesPath)) {
			logger.error("Rules file is not a file: '{}'", rulesPath);
			exitWithErrorMessage("The provided rules file is not a file.");
		}
		try {
			rulesFile = RulesFile.fromPath(rulesPath);
		} catch (RulesFileError e) {
			logger.error("Rules file error", e);
			exitWithErrorMessage("There was a rules file error: " + e.getMessage());
		} catch (RuleFactoryError e) {
			logger.error("Rule factory error", e);
			exitWithErrorMessage("There was an error creating a rule: " + e.getMessage());
		}
		return rulesFile;
	}

	private static IflowArtifactSupplier supplierFromCommandLine(RunMode mode, CommandLine cl) {
		/*
		 * We're assuming that the command line arguments have been validated at
		 * this point. I.e. we can safely assume that the arguments describe
		 * exactly one IflowArtifactSupplier fully, and that the mode correctly
		 * identifies that supplier.
		 */
		return switch (mode) {
			case DIRECTORY_SUPPLIER_MODE -> directorySupplierFromCommandLine(cl);
			case FILE_SUPPLIER_MODE -> fileSupplierFromCommandLine(cl);
			case UNPACKED_FILE_SUPPLIER_MODE -> unpackedFileSupplierFromCommandLine(cl);
			case TENANT_SUPPLIER_SINGLE_MODE -> tenantSupplierSingleFromCommandLine(cl);
			case TENANT_SUPPLIER_MULTI_MODE ->tenantSupplierMultiFromCommandLine(cl);
			case TENANT_SUPPLIER_PACKAGES_MODE -> tenantSupplierPackagesFromCommandLine(cl);
			default -> throw new AssertionError("Unexpected RunMode: " + mode);
		};
	}
	
	private static IflowArtifactSupplier directorySupplierFromCommandLine(CommandLine cl) {
		Path directoryPath = Paths.get(cl.getOptionValue(CLI_OPTION_DIRECTORY));
		if (Files.notExists(directoryPath)) {
			logger.error("Provided directory does not exist: '{}'", directoryPath);
			exitWithErrorMessage("Provided directory does not exist.");
		}
		if (!Files.isDirectory(directoryPath)) {
			logger.error("Provided directory is not a directory: '{}'", directoryPath);
			exitWithErrorMessage("Provided directory is not a directory.");
		}
		return new DirectoryIflowArtifactSupplier(directoryPath);
	}
	
	private static IflowArtifactSupplier fileSupplierFromCommandLine(CommandLine cl) {
		Set<Path> filePaths = Stream.of(cl.getOptionValues(CLI_OPTION_FILES))
			.map(Paths::get)
			.collect(Collectors.toSet());
		// Make sure that all files exist.
		Set<Path> nonexistingFiles = filePaths.stream().filter(p -> Files.notExists(p)).collect(Collectors.toSet());
		if (!nonexistingFiles.isEmpty()) {
			String message = "The following iflow artifact files do not exist: " +
				nonexistingFiles.stream().map(Path::toString).collect(Collectors.joining(", "));
			logger.error(message);
			exitWithErrorMessage(message);
		}
		// Make sure that all files are actually files.
		Set<Path> notRegularFiles = filePaths.stream().filter(p -> !Files.isRegularFile(p)).collect(Collectors.toSet());
		if (!notRegularFiles.isEmpty()) {
			String message = "The following iflow artifact files are not actually files: " +
				notRegularFiles.stream().map(Path::toString).collect(Collectors.joining(", "));
			logger.error(message);
			exitWithErrorMessage(message);
		}
		// All files are alright.
		return new FileIflowArtifactSupplier(filePaths);
	}

	private static IflowArtifactSupplier unpackedFileSupplierFromCommandLine(CommandLine cl) {
		Set<Path> dirPaths = Stream.of(cl.getOptionValues(CLI_OPTION_UNPACKED_FILES))
			.map(Path::of)
			.collect(Collectors.toSet());
		// Make sure that all directories exist.
		Set<String> nonexistingDirectories = dirPaths
			.stream()
			.filter(Files::notExists)
			.map(Path::toString)
			.collect(Collectors.toSet());
		if (!nonexistingDirectories.isEmpty()) {
			exitWithErrorMessage("The following unpacked iflow artifact directories do not exist: %s"
				.formatted(String.join(", ", nonexistingDirectories)));
		}
		// Make sure that all directories are actually directories.
		Set<String> notDirectories = dirPaths
			.stream()
			.filter(p -> !Files.isDirectory(p))
			.map(Path::toString)
			.collect(Collectors.toSet());
		if (!notDirectories.isEmpty()) {
			exitWithErrorMessage("The following unpacked iflow artifact directories are not actually directories: %s"
				.formatted(String.join(", ", notDirectories)));
		}
		// All directories are alright.
		return new UnpackedFileIflowArtifactSupplier(dirPaths);
	}
	
	private static IflowArtifactSupplier tenantSupplierSingleFromCommandLine(CommandLine cl) {
		CloudIntegrationApi api = cloudIntegrationApiFromCommandLine(cl);
		Set<String> fetchIflowArtifactIds = new HashSet<>(Arrays.asList(cl.getOptionValues(CLI_OPTION_IFLOWS)));
		return new TenantSingleArtifactsSupplier(api, fetchIflowArtifactIds);
	}

	private static IflowArtifactSupplier tenantSupplierMultiFromCommandLine(CommandLine cl) {
		CloudIntegrationApi api = cloudIntegrationApiFromCommandLine(cl);
		boolean skipSapPackages = cl.hasOption(CLI_OPTION_SKIP_SAP_PACKAGES);
		boolean skipDrafts = cl.hasOption(CLI_OPTION_SKIP_DRAFTS);
		Set<String> skipIflowArtifactIds = cl.hasOption(CLI_OPTION_SKIP_IFLOWS) ? new HashSet<>(Arrays.asList(cl.getOptionValues(CLI_OPTION_SKIP_IFLOWS))) : Collections.emptySet();
		Set<String> skipPackageIds = cl.hasOption(CLI_OPTION_SKIP_PACKAGES) ? new HashSet<>(Arrays.asList(cl.getOptionValues(CLI_OPTION_SKIP_PACKAGES))) : Collections.emptySet();
		return new TenantAllArtifactsSupplier(api, skipSapPackages, skipDrafts, skipIflowArtifactIds, skipPackageIds);
	}
	
	private static IflowArtifactSupplier tenantSupplierPackagesFromCommandLine(CommandLine cl) {
		CloudIntegrationApi api = cloudIntegrationApiFromCommandLine(cl);
		boolean skipDrafts = cl.hasOption(CLI_OPTION_SKIP_DRAFTS);
		Set<String> skipIflowArtifactIds = cl.hasOption(CLI_OPTION_SKIP_IFLOWS) ? new HashSet<>(Arrays.asList(cl.getOptionValues(CLI_OPTION_SKIP_IFLOWS))) : Collections.emptySet();
		Set<String> packageIds = new HashSet<>(Arrays.asList(cl.getOptionValues(CLI_OPTION_PACKAGES)));
		return new TenantIndividualPackagesSupplier(api, skipDrafts, packageIds, skipIflowArtifactIds);
	}

	private static CloudIntegrationApi cloudIntegrationApiFromCommandLine(CommandLine cl) {
		CloudIntegrationApi api = null;
		if (basicAuthenticationMode(cl)) {
			final String hostname = cl.getOptionValue(CLI_OPTION_HOST);
			final String username = cl.getOptionValue(CLI_OPTION_USERNAME);
			final char[] password = cl.hasOption(CLI_OPTION_PASSWORD) ? cl.getOptionValue(CLI_OPTION_PASSWORD).toCharArray() : promptForPassword(username);
			api = new CloudIntegrationOdataApi(hostname, username, password);
		} else if (oauthClientCredentialsMode(cl)) {
			final Path keyPath = Paths.get(cl.getOptionValue(CLI_OPTION_KEY));
			final JSONObject key = readServiceKey(keyPath).getJSONObject(SERVICE_KEY_FIELD_OAUTH);
			final String clientId = key.getString(SERVICE_KEY_FIELD_CLIENTID);
			final String clientSecret = key.getString(SERVICE_KEY_FIELD_CLIENTSECRET);
			String hostname = null;
			try {
				hostname = new URI(key.getString(SERVICE_KEY_FIELD_URL)).getHost();
			} catch (URISyntaxException e) {
				logger.error("Bad URL in service key", e);
				exitWithErrorMessage("Malformed URL in service key.");
			}
			URI tokenUrl = null;
			try {
				tokenUrl = new URI(key.getString(SERVICE_KEY_FIELD_TOKENURL) + "?grant_type=client_credentials");
			} catch (URISyntaxException e) {
				logger.error("Bad token URL in service key", e);
				exitWithErrorMessage("Malformed token URL in service key.");
			}
			final AuthorizationServer authServer = AuthorizationServer.newInstance(clientId, clientSecret, tokenUrl);
			api = new CloudIntegrationOdataApi(hostname, authServer);
		} else {
			// This should never happen.
			throw new AssertionError("Unexpected authentication mode");
		}
		return api;
	}

	private static JSONObject readServiceKey(Path keyPath) {
		// Make sure that the service key file exists and actually is a file.
		if (Files.notExists(keyPath)) {
			logger.error("Service key file does not exist: '{}'", keyPath);
			exitWithErrorMessage("The provided service key file does not exist.");
		}
		if (!Files.isRegularFile(keyPath)) {
			logger.error("Service key file is not a file: '{}'", keyPath);
			exitWithErrorMessage("The provided service key file is not a file.");
		}
		// Read the service key file's contents.
		logger.info("Reading service key file");
		String content = null;
		try {
			content = Files.readString(keyPath);
		} catch (IOException e) {
			logger.error("Exception while reading service key file", e);
			exitWithErrorMessage("I/O error reading service key file.");
		}
		// Now parse it as JSON.
		logger.info("Parsing service key contents");
		JSONObject json = null;
		try {
			json = new JSONObject(content);
		} catch (JSONException e) {
			logger.error("Exception while parsing service key", e);
			exitWithErrorMessage("There was a problem parsing the service key JSON content.");
		}
		// Validate the JSON object.
		validateServiceKey(json);
		return json;
	}

	private static void validateServiceKey(JSONObject json) {
		logger.info("Validating the service key JSON");
		/*
		 * The object should contain a single field, containing another object
		 * that contains the fields we are interested in.
		 */
		if (!json.has(SERVICE_KEY_FIELD_OAUTH)) {
			logger.error("Service key JSON does not have an oauth field");
			exitWithErrorMessage(SERVICE_KEY_VALIDATION_ERROR);
		}
		Object oauthObject = json.get(SERVICE_KEY_FIELD_OAUTH);
		if (!(oauthObject instanceof JSONObject)) {
			logger.error("Service key oauth field does not contain an object");
			exitWithErrorMessage(SERVICE_KEY_VALIDATION_ERROR);
		}
		JSONObject oauthJsonObject = (JSONObject) oauthObject;
		// Check that all required fields are present.
        Collection<String> missingFields = Stream.of(SERVICE_KEY_FIELD_CLIENTID, SERVICE_KEY_FIELD_CLIENTSECRET, SERVICE_KEY_FIELD_URL, SERVICE_KEY_FIELD_TOKENURL)
            .filter(f -> !oauthJsonObject.has(f))
            .collect(Collectors.toSet());
        if (!missingFields.isEmpty()) {
            String message = String.format("Service key has missing fields: %s", missingFields.stream().collect(Collectors.joining(",")));
			logger.error(message);
            exitWithErrorMessage(SERVICE_KEY_VALIDATION_ERROR);
        }
		logger.info("Service key JSON was valid");
	}
	
	private static char[] promptForPassword(String username) {
		logger.info("Interactively prompting for tenant password");
		Console console = System.console();
		if (console == null) {
			logger.error("Unable to get a Console object when prompting for password");
			exitWithErrorMessage("Error when prompting for password.");
		}
		char[] password = console.readPassword("Please enter the password for tenant user %s: ", username);
		logger.info("Password entered");
		System.out.println();
		return password;
	}
	
	private static CommandLine parseArguments(String[] args) {
		Options opts = prepareCliOptions();
		CommandLineParser parser = new DefaultParser();
		CommandLine cl = null;
		try {
			cl = parser.parse(opts, args);
		} catch (ParseException e) {
			logger.error("Error parsing command line arguments", e);
			exitWithErrorMessage(COMMAND_LINE_ERROR_MESSAGE);
		}
		/*
		 * If there are unrecognized options at this point, the command
		 * line arguments are considered invalid. 
		 */
		if (!cl.getArgList().isEmpty()) {
			logger.error("There were unrecognized command line arguments: {}", cl.getArgList().stream().collect(Collectors.joining(" ")));
			exitWithErrorMessage(COMMAND_LINE_ERROR_MESSAGE);
		}
		logger.info("Command line arguments parsed successfully");
		return cl;
	}

	private static void printVersionBanner() {
		System.out.printf("CPILint version %s, copyright (c) %s%n", VERSION, COPYRIGHT);
	}
	
	private static void printUsage() {
		printVersionBanner();
		System.out.println();
		System.out.println("To see the current version number:");
		System.out.println(">cpilint -version");
		System.out.println();
		System.out.println("To see usage information (this message):");
		System.out.println(">cpilint -help");
		System.out.println();
		System.out.println("To check if you are running the latest version of CPILint:");
		System.out.println(">cpilint -vercheck");
		System.out.println();
		System.out.println("To inspect individual iflow files on the local machine:");
		System.out.println(">cpilint -rules <file> -files <file> ...");
		System.out.println();
		System.out.println("To inspect all iflow files in a directory on the local machine:");
		System.out.println(">cpilint -rules <file> -directory <dir>");
		System.out.println();
		System.out.println("To inspect individual iflows unpacked into separate directories on the local machine:");
		System.out.println(">cpilint -rules <file> -unpacked-files <dir> ...");
		System.out.println();
		System.out.println("To inspect all iflows in your tenant using OAuth 2.0:");
		System.out.println(">cpilint -rules <file> -key <file>");
		System.out.println();
		System.out.println("When inspecting all iflows in your tenant you have the following options:");
		System.out.println();
		System.out.println("* Use -skip-sap-packages to skip SAP packages");
		System.out.println("* Use -skip-packages <id> ... to skip certain packages");
		System.out.println("* Use -skip-drafts to skip draft iflows");
		System.out.println("* Use -skip-iflows <id> ... to skip certain iflows");
		System.out.println();
		System.out.println("To inspect the iflows in individual packages in your tenant using OAuth 2.0:");
		System.out.println(">cpilint -rules <file> -key <file> -packages <id> ...");
		System.out.println();
		System.out.println("When inspecting iflows in individual packages you have the following options:");
		System.out.println();
		System.out.println("* Use -skip-drafts to skip draft iflows");
		System.out.println("* Use -skip-iflows <id> ... to skip certain iflows");
		System.out.println();
		System.out.println("To inspect individual iflows in your tenant using OAuth 2.0:");
		System.out.println(">cpilint -rules <file> -key <file> -iflows <id> ...");
		System.out.println();
		System.out.println("To specify the tenant hostname and use basic authentication instead of OAuth 2.0, replace the -key <file> option with -host <host> and -username <user>.");
		System.out.println();
		System.out.println("When using basic authentication, you can provide your tenant password with the optional -password <password> option. If you don't, you will be prompted for the password interactively.");
		System.out.println();
		System.out.println("To remove the ASCII art logo from CPILint's output, add the -boring option.");
		System.out.println();
		System.out.println("To create a debug log file, add the -debug option.");
		System.out.println();
		System.out.println("To skip the automatic version check, add the -skipvercheck option.");
		System.out.println();
		System.out.println("The full CPILint documentation is available in the project wiki: " + CPILINT_WIKI_URL);
	}

	private static void printAsciiArtLogo() {
		/*
		 * The logo was created with the "Text to ASCII Art Generator"
		 * (http://patorjk.com/software/taag/) using the 3D-ASCII font.
		 */
		System.out.println(" ________  ________  ___  ___       ___  ________   _________   ");
		System.out.println("|\\   ____\\|\\   __  \\|\\  \\|\\  \\     |\\  \\|\\   ___  \\|\\___   ___\\ ");
		System.out.println("\\ \\  \\___|\\ \\  \\|\\  \\ \\  \\ \\  \\    \\ \\  \\ \\  \\\\ \\  \\|___ \\  \\_| ");
		System.out.println(" \\ \\  \\    \\ \\   ____\\ \\  \\ \\  \\    \\ \\  \\ \\  \\\\ \\  \\   \\ \\  \\  ");
		System.out.println("  \\ \\  \\____\\ \\  \\___|\\ \\  \\ \\  \\____\\ \\  \\ \\  \\\\ \\  \\   \\ \\  \\ ");
		System.out.println("   \\ \\_______\\ \\__\\    \\ \\__\\ \\_______\\ \\__\\ \\__\\\\ \\__\\   \\ \\__\\");
		System.out.println("    \\|_______|\\|__|     \\|__|\\|_______|\\|__|\\|__| \\|__|    \\|__|");
	}
	
    private static Options prepareCliOptions() {
    	/*
    	 * The option descriptions are not used at the moment, since the usage
    	 * message is handcrafted. I've kept them in the code, nevertheless,
    	 * since we might need them in the future.
    	 */
        Options options = new Options();
        // Add the version option.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_VERSION)
        	.required(false)
            .hasArg(false)
            .desc("Display the CPILint version and exit")
            .build());
        // Add the help option.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_HELP)
        	.required(false)
            .hasArg(false)
            .desc("Display usage information and exit")
            .build());
        // Add the boring mode option.
        options.addOption(Option.builder()
            .longOpt(CLI_OPTION_BORING)
        	.required(false)
            .hasArg(false)
            .desc("Boring mode, i.e. no ASCII art")
            .build());
        // Add the rules file option.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_RULES)
            .required(false)
            .hasArg()
            .argName("file")
            .desc("Apply the rules in this file")
            .build());
        // Add the file iflow artifact supplier option.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_FILES)
            .required(false)
            .hasArgs()
            .argName("file")
            .desc("Process these iflow artifact files")
            .build());
        // Add the unpacked file iflow artifact supplier option.
        options.addOption(Option.builder()
            .longOpt(CLI_OPTION_UNPACKED_FILES)
            .required(false)
            .hasArgs()
            .argName("dir")
            .desc("Process these unpacked iflow artifacts")
            .build());
		// Add the directory iflow artifact supplier option.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_DIRECTORY)
            .required(false)
            .hasArg()
            .argName("dir")
            .desc("Process all iflow artifact files in this directory")
            .build());
        // Add the hostname option.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_HOST)
            .required(false)
            .hasArg()
            .argName("host")
            .desc("Process iflow artifacts from this tenant")
            .build());
        // Add the tenant user name option.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_USERNAME)
            .required(false)
            .hasArg()
            .argName("user")
            .desc("Tenant user name")
            .build());
        // Add the tenant user password option.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_PASSWORD)
            .required(false)
            .hasArg()
            .argName(CLI_OPTION_PASSWORD)
            .desc("Tenant user password")
            .build());
        // Add the option to specify which iflow artifact IDs to inspect.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_IFLOWS)
            .required(false)
            .hasArgs()
            .argName("id")
            .desc("Inspect these iflow artifact IDs")
            .build());
        // Add the option to specify which packages to inspect.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_PACKAGES)
            .required(false)
            .hasArgs()
            .argName("id")
            .desc("Inspect these packages")
            .build());
        // Add the option to skip SAP packages when retrieving all iflow artifacts from the tenant.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_SKIP_SAP_PACKAGES)
        	.required(false)
            .hasArg(false)
            .desc("Skip SAP packages")
            .build());
        // Add the option to skip certain iflow artifact IDs when inspecting iflow artifacts.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_SKIP_IFLOWS)
            .required(false)
            .hasArgs()
            .argName("id")
            .desc("Skip these iflow artifact IDs")
            .build());
        // Add the option to skip certain packages when inspecting all iflows in a tenant.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_SKIP_PACKAGES)
            .required(false)
            .hasArgs()
            .argName("id")
            .desc("Skip these packages")
            .build());
        // Add the option to skip draft iflow artifacts.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_SKIP_DRAFTS)
        	.required(false)
            .hasArg(false)
            .desc("Skip draft iflow artifacts")
            .build());
        // Add the debug option.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_DEBUG)
            .required(false)
            .hasArg(false)
            .desc("Create a debug log file")
            .build());
        // Add the version check option.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_VERCHECK)
            .required(false)
            .hasArg(false)
            .desc("Check if a newer version of CPILint exists")
            .build());
        // Add the option to skip the version check.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_SKIPVERCHECK)
            .required(false)
            .hasArg(false)
            .desc("Skip the version check")
            .build());
        // Add the service key option.
        options.addOption(Option.builder()
			.longOpt(CLI_OPTION_KEY)
            .required(false)
            .hasArg()
            .argName("file")
            .desc("Use this service key for authentication")
            .build());
		// All done.
        return options;
    }
    
    private static boolean versionMode(CommandLine cl) {
    	/*
    	 *  In this mode, only the -version option has been provided.
    	 */
    	return checkForSingleOption(cl, CLI_OPTION_VERSION);
    }

    private static boolean versionCheckMode(CommandLine cl) {
    	/*
    	 * The following options are mandatory in this mode:
    	 * 
    	 * + vercheck
    	 * 
    	 * The following options are optional in this mode:
    	 * 
    	 * + debug
    	 */ 
    	Collection<String> mandatory = List.of(CLI_OPTION_VERCHECK);
    	Collection<String> optional = List.of(CLI_OPTION_DEBUG);
    	return checkOptions(cl, mandatory, optional);
    }

    private static boolean helpMode(CommandLine cl) {
    	/*
    	 *  In this mode, only the -help option has been provided.
    	 */
    	return checkForSingleOption(cl, CLI_OPTION_HELP);
    }
    
    private static boolean fileSupplierMode(CommandLine cl) {
    	/*
    	 * The following options are mandatory in this mode:
    	 * 
    	 * + rules
    	 * + files
    	 * 
    	 * The following options are optional in this mode:
    	 * 
    	 * + boring
    	 * + debug
		 * + skipvercheck
    	 * 
    	 * The -files option must have at least one argument.
    	 */    	
    	Collection<String> mandatory = List.of(CLI_OPTION_RULES, CLI_OPTION_FILES);
    	Collection<String> optional = List.of(CLI_OPTION_BORING, CLI_OPTION_DEBUG, CLI_OPTION_SKIPVERCHECK);
    	return checkOptions(cl, mandatory, optional) && cl.getOptionValues(CLI_OPTION_FILES).length >= 1;
    }

    private static boolean unpackedFileSupplierMode(CommandLine cl) {
        /*
         * The following options are mandatory in this mode:
         *
         * + rules
         * + unpacked-files
         *
         * The following options are optional in this mode:
         *
         * + boring
         * + debug
         * + skipvercheck
         *
         * The -unpacked-files option must have at least one argument.
         */
        Collection<String> mandatory = List.of(CLI_OPTION_RULES, CLI_OPTION_UNPACKED_FILES);
        Collection<String> optional = List.of(CLI_OPTION_BORING, CLI_OPTION_DEBUG, CLI_OPTION_SKIPVERCHECK);
        return checkOptions(cl, mandatory, optional) && cl.getOptionValues(CLI_OPTION_UNPACKED_FILES).length >= 1;
    }

    private static boolean directorySupplierMode(CommandLine cl) {
    	/*
    	 * The following options are mandatory in this mode:
    	 * 
    	 * + rules
    	 * + directory
    	 * 
    	 * The following options are optional in this mode:
    	 * 
    	 * + boring
    	 * + debug
		 * + skipvercheck
    	 * 
    	 * The -directory option must have exactly one argument.
    	 */
    	Collection<String> mandatory = List.of(CLI_OPTION_RULES, CLI_OPTION_DIRECTORY);
    	Collection<String> optional = List.of(CLI_OPTION_BORING, CLI_OPTION_DEBUG, CLI_OPTION_SKIPVERCHECK);
    	return checkOptions(cl, mandatory, optional) && cl.getOptionValues(CLI_OPTION_DIRECTORY).length == 1;
    }
    
    private static boolean tenantSupplierSingleMode(CommandLine cl) {
    	/*
    	 * The following options are mandatory in this mode:
    	 * 
    	 * + rules
    	 * + iflows
    	 * 
    	 * The following options are optional in this mode:
    	 * 
		 * + key
		 * + host
		 * + username
		 * + password
    	 * + boring
    	 * + debug
		 * + skipvercheck
		 *
		 * Either a service key or a hostname and a username must be provided.
    	 */
		Collection<String> mandatory = List.of(CLI_OPTION_RULES, CLI_OPTION_IFLOWS);
		Collection<String> optional = List.of(CLI_OPTION_KEY, CLI_OPTION_HOST, CLI_OPTION_USERNAME, CLI_OPTION_PASSWORD, CLI_OPTION_BORING, CLI_OPTION_DEBUG, CLI_OPTION_SKIPVERCHECK);
		return checkOptions(cl, mandatory, optional) && authAndHostProvided(cl);
    }

    private static boolean tenantSupplierMultiMode(CommandLine cl) {
    	/*
    	 * The following options are mandatory in this mode:
    	 * 
    	 * + rules
		 *
    	 * The following options are optional:
    	 * 
		 * + key
		 * + host
		 * + username
		 * + password
    	 * + skip-sap-packages
    	 * + skip-iflows
    	 * + skip-packages
    	 * + skip-drafts
    	 * + boring
    	 * + debug
		 * + skipvercheck
		 *
		 * Either a service key or a hostname and a username must be provided.
    	 */
		Collection<String> mandatory = List.of(CLI_OPTION_RULES);
		Collection<String> optional = List.of(CLI_OPTION_KEY, CLI_OPTION_HOST, CLI_OPTION_USERNAME, CLI_OPTION_PASSWORD, CLI_OPTION_SKIP_SAP_PACKAGES, CLI_OPTION_SKIP_IFLOWS, CLI_OPTION_SKIP_PACKAGES, CLI_OPTION_SKIP_DRAFTS, CLI_OPTION_BORING, CLI_OPTION_DEBUG, CLI_OPTION_SKIPVERCHECK);
		return checkOptions(cl, mandatory, optional) && authAndHostProvided(cl);
    }
    
    private static boolean tenantSupplierPackagesMode(CommandLine cl) {
    	/*
    	 * The following options are mandatory in this mode:
    	 * 
    	 * + rules
    	 * + packages
    	 * 
    	 * The following options are optional:
    	 * 
		 * + key
		 * + host
		 * + username
		 * + password
    	 * + skip-iflows
    	 * + skip-drafts
    	 * + boring
    	 * + debug
		 * + skipvercheck
		 *
		 * Either a service key or a hostname and a username must be provided.
    	 */
		Collection<String> mandatory = List.of(CLI_OPTION_RULES, CLI_OPTION_PACKAGES);
		Collection<String> optional = List.of(CLI_OPTION_KEY, CLI_OPTION_HOST, CLI_OPTION_USERNAME, CLI_OPTION_PASSWORD, CLI_OPTION_SKIP_IFLOWS, CLI_OPTION_SKIP_DRAFTS, CLI_OPTION_BORING, CLI_OPTION_DEBUG, CLI_OPTION_SKIPVERCHECK);
		return checkOptions(cl, mandatory, optional) && authAndHostProvided(cl);
    }
    
    private static boolean checkOptions(CommandLine cl, Collection<String> mandatory, Collection<String> optional) {
    	/*
    	 * All the provided mandatory options must be present. Also, each
    	 * present option must be one of the provided mandatory or optional
    	 * options. 
    	 */
    	boolean allMandatoryOptionsPresent = mandatory.stream().allMatch(o -> cl.hasOption(o));
    	boolean noAdditionalOptionsPresent = Stream.of(cl.getOptions()).map(Option::getLongOpt).allMatch(o -> mandatory.contains(o) || optional.contains(o));
    	return allMandatoryOptionsPresent && noAdditionalOptionsPresent;
    }
    
    private static boolean checkForSingleOption(CommandLine cl, String singleOption) {
    	return cl.hasOption(singleOption) && cl.getOptions().length == 1;
    }

	private static boolean authAndHostProvided(CommandLine cl) {
		/*
		 * OAuth Client Credentials mode and basic authentication mode are mutually
		 * exclusive, so a regular OR is enough in the following (we don't need an
		 * exclusive OR).
		 */
		return oauthClientCredentialsMode(cl) || basicAuthenticationMode(cl);
	}

	private static boolean oauthClientCredentialsMode(CommandLine cl) {
		/*
		 * In OAuth Client Credentials mode a service key is provided on the
		 * command line and host name, user name and password are NOT provided.
		 */
		return cl.hasOption(CLI_OPTION_KEY) &&
			!cl.hasOption(CLI_OPTION_HOST) &&
			!cl.hasOption(CLI_OPTION_USERNAME) &&
			!cl.hasOption(CLI_OPTION_PASSWORD);
	}

	private static boolean basicAuthenticationMode(CommandLine cl) {
		/*
		 * In basic authentication mode host name and user name are provided on the
		 * command line and a service key is NOT provided.
		 */
		return !cl.hasOption(CLI_OPTION_KEY) &&
			cl.hasOption(CLI_OPTION_HOST) &&
			cl.hasOption(CLI_OPTION_USERNAME);
	}
    
    private static void exitWithErrorMessage(String errorMessage) {
    	logger.info("Exiting CliClient with error status {} and message: {}", EXIT_STATUS_ERRORS, errorMessage);
    	System.err.println(errorMessage);
		System.exit(EXIT_STATUS_ERRORS);
    }

	private static void configureLogging(String[] args) {
		/*
		 * The full Apache Commons CLI parsing is not performed here. We're
		 * only looking for the debug option. If it's present, the log level
		 * is set to DEBUG.
		 */
		if (Stream.of(args).anyMatch(a -> a.equals("-" + CLI_OPTION_DEBUG))) {
			/*
			 * This is the only place where Logback is referenced directly. 
			 * The log statements throughout the code only use the SLF4J API. 
			 */
			ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
			rootLogger.setLevel(Level.DEBUG);
		}
	}

	private static void versionCheck(boolean userInitiated) {
		/*
		 * If the user initiated the version check, any errors that might occur
		 * are reported (i.e. printed on stderr). If the version check is run
		 * as part of normal program execution (i.e. not user initiated), any
		 * errors that might occur are logged but not reported. In this mode,
		 * if anything fails we'll just return from the method.
		 */
		String currentVersion = null;
		try {
			currentVersion = retrieveCurrentVersion();
		} catch (VersionCheckError e) {
			logger.error("Exception retrieving current version number", e);
			if (userInitiated) {
				exitWithErrorMessage("There was a problem retrieving the current version number.");
			} else {
				return;
			}
		}
		logger.debug("The current version is {}", currentVersion);
		// Assumption: If the current version is different from the locally installed version, the locally installed version is older.
		if (VERSION.equals(currentVersion)) {
			logger.info("CPILint is up to date");
			if (userInitiated) {
				printVersionBanner();
				System.out.println();
				System.out.println("Congratulations! You are running the latest version of CPILint.");
			}
		} else {
			logger.info("CPILint is not up to date");
			if (userInitiated) {
				String message = "You are running version %s of CPILint. Version %s is available and can be downloaded here: %s".formatted(VERSION, currentVersion, LATEST_RELEASE_DOWNLOAD_URL);
				printVersionBanner();
				System.out.println();
				System.out.println(message);
			} else {
				String message = "Please note that you are not running the latest version of CPILint. You can download the latest version (%s) here: %s".formatted(currentVersion, LATEST_RELEASE_DOWNLOAD_URL);
				System.out.println(message);
				System.out.println();
			}
		}
	}

	private static String retrieveCurrentVersion() {
		final String apiEndpoint = "https://api.github.com/repos/mwittrock/cpilint/releases/latest";
		final String tagRegex = "^v\\d+(?:\\.\\d+)+$";
		// Fetch the GitHub API response.
		URI apiUri = null;
		try {
			apiUri = new URI(apiEndpoint);
		} catch (URISyntaxException e) {
			// This should never happen!
			throw new VersionCheckError("Unexpected GitHub API URI issue", e);
		}
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(apiUri)
            .GET()
            .build();
        HttpResponse<String> response = null;
		try {
			logger.debug("Calling GitHub API at {}", apiEndpoint);
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			throw new VersionCheckError("Exception calling GitHub API", e);
		}
		// Only proceed if we received HTTP status OK.
		final int status = response.statusCode();
		if (status != HttpUtil.HTTP_OKAY_STATUS_CODE) {
			throw new VersionCheckError("Unexpected HTTP status code returned by GitHub API: %d".formatted(status));
		}
		// Parse the JSON response.
		JSONObject parsed = null;
		try {
			logger.debug("Parsing GitHub API response");
			parsed = new JSONObject(response.body());
		} catch (JSONException e) {
			throw new VersionCheckError("Exception parsing GitHub API response", e);
		}
		// Extract the release tag.
		String tag = null;
		try {
			tag = parsed.getString("tag_name");
		} catch (JSONException e) {
			throw new VersionCheckError("JSONException retrieving tag_name", e);
		}
		logger.debug("Extracted tag '{}' from GitHub API response", tag);
		// Check that the tag has the expected format.
		if (!tag.matches(tagRegex)) {
			throw new VersionCheckError("Extracted tag does not have the expected format");
		}
		// The version number is everything following the initial 'v'.
		String latestVersion = tag.substring(1);
		return latestVersion;
	}

	private static void easter() {
		final LocalDate release = LocalDate.of(2019, Month.AUGUST, 31);
		final LocalDate today = LocalDate.now();
		if (today.getDayOfMonth() == release.getDayOfMonth() && today.getMonth() == release.getMonth()) {
			System.out.println(String.format("Today, CPILint is %d years old; happy birthday to me \\o/", today.getYear() - release.getYear()));
			System.out.println();
		}
	}

}