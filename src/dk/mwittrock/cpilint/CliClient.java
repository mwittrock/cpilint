package dk.mwittrock.cpilint;

import java.io.Console;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

import ch.qos.logback.classic.Level;
import dk.mwittrock.cpilint.api.CloudIntegrationApi;
import dk.mwittrock.cpilint.api.CloudIntegrationOdataApi;
import dk.mwittrock.cpilint.consumers.ConsoleIssueConsumer;
import dk.mwittrock.cpilint.consumers.IssueConsumer;
import dk.mwittrock.cpilint.rules.Rule;
import dk.mwittrock.cpilint.rules.RuleFactoryError;
import dk.mwittrock.cpilint.suppliers.DirectoryIflowArtifactSupplier;
import dk.mwittrock.cpilint.suppliers.FileIflowArtifactSupplier;
import dk.mwittrock.cpilint.suppliers.IflowArtifactSupplier;
import dk.mwittrock.cpilint.suppliers.IflowArtifactSupplierError;
import dk.mwittrock.cpilint.suppliers.TenantAllArtifactsSupplier;
import dk.mwittrock.cpilint.suppliers.TenantSingleArtifactsSupplier;

public final class CliClient {
	
	private static final Logger logger = LoggerFactory.getLogger(CliClient.class);
	private static final String VERSION = "1.0.2";
	private static final String COPYRIGHT = "2019-2021 Morten N. Wittrock";
	private static final int EXIT_STATUS_SUCCESS = 0;
	private static final int EXIT_STATUS_ISSUES = 1;
	private static final int EXIT_STATUS_ERRORS = 2;
	private static final String COMMAND_LINE_ERROR_MESSAGE = "There is a problem with your command line arguments. Please run cpilint -help for usage information.";
	private static final String CLI_OPTION_HELP = "help";
	private static final String CLI_OPTION_VERSION = "version";
	private static final String CLI_OPTION_SKIP_IFLOWS = "skip-iflows";
	private static final String CLI_OPTION_SKIP_SAP_PACKAGES = "skip-sap-packages";
	private static final String CLI_OPTION_IFLOWS = "iflows";
	private static final String CLI_OPTION_PASSWORD = "password";
	private static final String CLI_OPTION_USERNAME = "username";
	private static final String CLI_OPTION_TMN_HOST = "tmn-host";
	private static final String CLI_OPTION_FILES = "files";
	private static final String CLI_OPTION_DIRECTORY = "directory";
	private static final String CLI_OPTION_RULES = "rules";
	private static final String CLI_OPTION_BORING = "boring";
	private static final String CLI_OPTION_DEBUG = "debug";
	
	private static enum RunMode {
		VERSION_MODE,
		HELP_MODE,
		FILE_SUPPLIER_MODE,
		DIRECTORY_SUPPLIER_MODE,
		TENANT_SUPPLIER_SINGLE_MODE,
		TENANT_SUPPLIER_MULTI_MODE
	}
	
	private CliClient() {
		throw new AssertionError("Never supposed to be instantiated");
	}
	
	public static void main(String[] args) {
		// If logging is requested, it should be set up before anything else.
		configureLogging(args);
		logger.info("CPILint version {}", VERSION);
		logger.info("Launching CliClient");
		logArguments(args);
		// Parse the command line arguments.
		CommandLine cl = parseArguments(args);
		// Determine the run mode.
		RunMode mode = determineMode(cl);
		logger.info("Mode detected: {}", mode);
		/*
		 * The help and version modes are handled separately.
		 */
		if (mode == RunMode.HELP_MODE) {
	        printUsage();
	        System.exit(EXIT_STATUS_SUCCESS);
		}
		if (mode == RunMode.VERSION_MODE) {
			printVersionBanner();
			System.exit(EXIT_STATUS_SUCCESS);
		}
		/*
		 * Prepare to run a test.
		 */
		IflowArtifactSupplier supplier = supplierFromCommandLine(mode, cl);
		Collection<Rule> rules = rulesFromCommandLine(cl);
		IssueConsumer consumer = issueConsumerFromCommandLine(cl);
		/*
		 *  Now, create a CpiLint object and run the test.
		 */
		printVersionBanner();
		System.out.println();
		if (!cl.hasOption(CLI_OPTION_BORING)) {
			printAsciiArtLogo();
			System.out.println();
		}
		CpiLint linter = new CpiLint(supplier, rules, consumer);
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
	
	private static RunMode determineMode(CommandLine cl) {
		RunMode mode = null;
		if (versionMode(cl)) {
			mode = RunMode.VERSION_MODE;
		} else if (helpMode(cl)) {
			mode = RunMode.HELP_MODE;
		} else if (fileSupplierMode(cl)) {
			mode = RunMode.FILE_SUPPLIER_MODE;
		} else if (directorySupplierMode(cl)) {
			mode = RunMode.DIRECTORY_SUPPLIER_MODE;
		} else if (tenantSupplierSingleMode(cl)) {
			mode = RunMode.TENANT_SUPPLIER_SINGLE_MODE;
		} else if (tenantSupplierMultiMode(cl)) {
			mode = RunMode.TENANT_SUPPLIER_MULTI_MODE;
		} else {
			logger.error("Could not determine run mode from command line arguments");
			exitWithErrorMessage(COMMAND_LINE_ERROR_MESSAGE);
		}
		return mode;
	}

	private static String resultMessage(IflowArtifactSupplier supplier, IssueConsumer consumer) {
		int artifactsSupplied = supplier.artifactsSupplied();
		int issuesConsumed = consumer.issuesConsumed();
		return String.format("Inspection of %d iflow %s resulted in %d %s found.",
			artifactsSupplied,
			artifactSingularPlural(artifactsSupplied),
			issuesConsumed,
			issueSingularPlural(issuesConsumed));
	}
	
	private static String artifactSingularPlural(int count) {
		return count == 1 ? "artifact" : "artifacts";
	}
	
	private static String issueSingularPlural(int count) {
		return count == 1 ? "issue" : "issues";
	}

	private static Collection<Rule> rulesFromCommandLine(CommandLine cl) {
		/*
		 * We're assuming that the command line arguments have been validated at
		 * this point. I.e. we can safely assume that the -rules option is present
		 * and has exactly one argument.
		 */
		Collection<Rule> rules = null;
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
			rules = RulesFile.fromPath(rulesPath);
		} catch (RulesFileError e) {
			logger.error("Rules file error", e);
			exitWithErrorMessage("There was an error in the rules file: " + e.getMessage());
		} catch (RuleFactoryError e) {
			logger.error("Rule factory error", e);
			exitWithErrorMessage("There was an error creating a rule: " + e.getMessage());
		}		
		return rules;
	}
	
	private static IssueConsumer issueConsumerFromCommandLine(CommandLine cl) {
		/*
		 *  For now, the IssueConsumer is hardcoded to be the only IssueConsumer 
		 *  we've got (i.e. ConsoleIssueConsumer).
		 */
		return new ConsoleIssueConsumer();	
	}

	private static IflowArtifactSupplier supplierFromCommandLine(RunMode mode, CommandLine cl) {
		/*
		 * We're assuming that the command line arguments have been validated at
		 * this point. I.e. we can safely assume that the arguments describe
		 * exactly one IflowArtifactSupplier fully, and that the mode correctly
		 * identifies that supplier.
		 */
		IflowArtifactSupplier supplier = null;
		if (mode == RunMode.DIRECTORY_SUPPLIER_MODE) {
			supplier = directorySupplierFromCommandLine(cl);
		} else if (mode == RunMode.FILE_SUPPLIER_MODE) {
			supplier = fileSupplierFromCommandLine(cl);
		} else if (mode == RunMode.TENANT_SUPPLIER_SINGLE_MODE) {
			supplier = tenantSupplierSingleFromCommandLine(cl);
		} else if (mode == RunMode.TENANT_SUPPLIER_MULTI_MODE) {
			supplier = tenantSupplierMultiFromCommandLine(cl);
		} else {
			/*
			 * Given that the command line arguments ought to be valid,
			 * this should never happen.
			 */
			logger.error("Method supplierFromCommandLine called with an unexpected RunMode");
			exitWithErrorMessage("Internal CPILint error.");
		}
		return supplier;
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
	
	private static IflowArtifactSupplier tenantSupplierSingleFromCommandLine(CommandLine cl) {
		String tmnHost = cl.getOptionValue(CLI_OPTION_TMN_HOST);
		String username = cl.getOptionValue(CLI_OPTION_USERNAME);
		char[] password = cl.hasOption(CLI_OPTION_PASSWORD) ? cl.getOptionValue(CLI_OPTION_PASSWORD).toCharArray() : promptForPassword(username);
		CloudIntegrationApi api = new CloudIntegrationOdataApi(tmnHost, username, password);
		// TODO: A duplicate iflow ID should just be ignored, but right now it throws an exception.
		Set<String> fetchIflowArtifactIds = Set.of(cl.getOptionValues(CLI_OPTION_IFLOWS));
		return new TenantSingleArtifactsSupplier(api, fetchIflowArtifactIds);
	}

	private static IflowArtifactSupplier tenantSupplierMultiFromCommandLine(CommandLine cl) {
		String tmnHost = cl.getOptionValue(CLI_OPTION_TMN_HOST);
		String username = cl.getOptionValue(CLI_OPTION_USERNAME);
		char[] password = cl.hasOption(CLI_OPTION_PASSWORD) ? cl.getOptionValue(CLI_OPTION_PASSWORD).toCharArray() : promptForPassword(username);
		CloudIntegrationApi api = new CloudIntegrationOdataApi(tmnHost, username, password);
		boolean skipSapPackages = cl.hasOption(CLI_OPTION_SKIP_SAP_PACKAGES);
		Set<String> skipIflowArtifactIds = cl.hasOption(CLI_OPTION_SKIP_IFLOWS) ? Set.of(cl.getOptionValues(CLI_OPTION_SKIP_IFLOWS)) : Collections.emptySet();
		return new TenantAllArtifactsSupplier(api, skipSapPackages, skipIflowArtifactIds);
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
		System.out.println("cpilint -version");
		System.out.println();
		System.out.println("To see usage information (this message):");
		System.out.println("cpilint -help");
		System.out.println();
		System.out.println("To apply rules to individual iflow artifact files:");
		System.out.println("cpilint -rules <file> -files <file> ...");
		System.out.println();
		System.out.println("To apply rules to all iflow artifact files in a directory:");
		System.out.println("cpilint -rules <file> -directory <dir>");
		System.out.println();
		System.out.println("To apply rules to individual iflow artifacts in your tenant:");
		System.out.println("cpilint -rules <file> -tmn-host <host> -username <user> [-password <password>] -iflows <id> ...");
		System.out.println();
		System.out.println("To apply rules to all iflow artifacts in your tenant:");
		System.out.println("cpilint -rules <file> -tmn-host <host> -username <user> [-password <password>] [-skip-sap-packages] [-skip-iflows <id> ...]");
		System.out.println();
		System.out.println("Apply the optional -skip-sap-packages option if you want to skip all SAP packages.");
		System.out.println();
		System.out.println("Apply the optional -skip-iflows <id> ... option if you want to skip certain iflow artifacts.");
		System.out.println();
		System.out.println("If the tenant password is not provided, you will be prompted for it.");
		System.out.println();
		System.out.println("To remove the ASCII art logo from CPILint's output, add the -boring option.");
		System.out.println();
		System.out.println("To create a debug log file, add the -debug option.");
	}

	private static void printAsciiArtLogo() {
		/*
		 * The logo was created with the "Text to ASCII Art Generator"
		 * (http://patorjk.com/software/taag/) using the Banner3 font.
		 */
		System.out.println(" ######  ########  #### ##       #### ##    ## ######## ");
		System.out.println("##    ## ##     ##  ##  ##        ##  ###   ##    ##    ");
		System.out.println("##       ##     ##  ##  ##        ##  ####  ##    ##    ");
		System.out.println("##       ########   ##  ##        ##  ## ## ##    ##    ");
		System.out.println("##       ##         ##  ##        ##  ##  ####    ##    ");
		System.out.println("##    ## ##         ##  ##        ##  ##   ###    ##    ");
		System.out.println(" ######  ##        #### ######## #### ##    ##    ##    ");
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
        // Add the directory iflow artifact supplier option.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_DIRECTORY)
            .required(false)
            .hasArg()
            .argName("dir")
            .desc("Process all iflow artifact files in this directory")
            .build());
        // Add the Tenant Management Node host option.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_TMN_HOST)
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
        // Add the debug option.
        options.addOption(Option.builder()
        	.longOpt(CLI_OPTION_DEBUG)
            .required(false)
            .hasArg(false)
            .desc("Create a debug log file")
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
    	 * 
    	 * The -files option must have at least one argument.
    	 */    	
    	Collection<String> mandatory = List.of(CLI_OPTION_RULES, CLI_OPTION_FILES);
    	Collection<String> optional = List.of(CLI_OPTION_BORING, CLI_OPTION_DEBUG);
    	return checkOptions(cl, mandatory, optional) && cl.getOptionValues(CLI_OPTION_FILES).length >= 1;
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
    	 * 
    	 * The -directory option must have exactly one argument.
    	 */
    	Collection<String> mandatory = List.of(CLI_OPTION_RULES, CLI_OPTION_DIRECTORY);
    	Collection<String> optional = List.of(CLI_OPTION_BORING, CLI_OPTION_DEBUG);
    	return checkOptions(cl, mandatory, optional) && cl.getOptionValues(CLI_OPTION_DIRECTORY).length == 1;
    }
    
    private static boolean tenantSupplierSingleMode(CommandLine cl) {
    	/*
    	 * The following options are mandatory in this mode:
    	 * 
    	 * + rules
    	 * + tenant-host
    	 * + username
    	 * + iflows
    	 * 
    	 * The following options are optional in this mode:
    	 * 
    	 * + password
    	 * + boring
    	 * + debug
    	 * 
    	 * The -iflows option must have at least one argument.
    	 */
    	Collection<String> mandatory = List.of(CLI_OPTION_RULES, CLI_OPTION_TMN_HOST, CLI_OPTION_USERNAME, CLI_OPTION_IFLOWS);
    	Collection<String> optional = List.of(CLI_OPTION_PASSWORD, CLI_OPTION_BORING, CLI_OPTION_DEBUG);
    	return checkOptions(cl, mandatory, optional) && cl.getOptionValues(CLI_OPTION_IFLOWS).length >= 1;
    }

    private static boolean tenantSupplierMultiMode(CommandLine cl) {
    	/*
    	 * The following options are mandatory in this mode:
    	 * 
    	 * + rules
    	 * + tenant-host
    	 * + username
    	 * 
    	 * The following options are optional:
    	 * 
    	 * + password
    	 * + skip-sap-packages
    	 * + skip-iflows
    	 * + boring
    	 * + debug
    	 * 
    	 * if the -skip-iflows option is present, it must have at least one argument.
    	 */
    	Collection<String> mandatory = List.of(CLI_OPTION_RULES, CLI_OPTION_TMN_HOST, CLI_OPTION_USERNAME);
    	Collection<String> optional = List.of(CLI_OPTION_PASSWORD, CLI_OPTION_SKIP_SAP_PACKAGES, CLI_OPTION_SKIP_IFLOWS, CLI_OPTION_BORING, CLI_OPTION_DEBUG);
    	return checkOptions(cl, mandatory, optional) && !(cl.hasOption(CLI_OPTION_SKIP_IFLOWS) && cl.getOptionValues(CLI_OPTION_SKIP_IFLOWS).length == 0);
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
    
    private static void exitWithErrorMessage(String errorMessage) {
    	// TODO: Log the error message and the exit code.
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
    
}
