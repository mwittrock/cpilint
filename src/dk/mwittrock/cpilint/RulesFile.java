package dk.mwittrock.cpilint;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import dk.mwittrock.cpilint.rules.CleartextBasicAuthNotAllowedRuleFactory;
import dk.mwittrock.cpilint.rules.ClientCertSenderChannelAuthNotAllowedRuleFactory;
import dk.mwittrock.cpilint.rules.CsrfProtectionRequiredRuleFactory;
import dk.mwittrock.cpilint.rules.DuplicateResourcesNotAllowedRuleFactory;
import dk.mwittrock.cpilint.rules.IflowDescriptionRequiredRuleFactory;
import dk.mwittrock.cpilint.rules.JavaArchivesRuleFactory;
import dk.mwittrock.cpilint.rules.MappingTypesRuleFactory;
import dk.mwittrock.cpilint.rules.MatchingProcessDirectChannelsRequiredRuleFactory;
import dk.mwittrock.cpilint.rules.MultiConditionTypeRoutersNotAllowedRuleFactory;
import dk.mwittrock.cpilint.rules.NamingConventionsRuleFactory;
import dk.mwittrock.cpilint.rules.ReceiverAdaptersRuleFactory;
import dk.mwittrock.cpilint.rules.Rule;
import dk.mwittrock.cpilint.rules.RuleFactory;
import dk.mwittrock.cpilint.rules.ScriptingLanguagesRuleFactory;
import dk.mwittrock.cpilint.rules.SenderAdaptersRuleFactory;
import dk.mwittrock.cpilint.rules.UnencryptedDataStoreWriteNotAllowedRuleFactory;
import dk.mwittrock.cpilint.rules.UnencryptedEndpointsNotAllowedRuleFactory;
import dk.mwittrock.cpilint.rules.UserRolesRuleFactory;
import dk.mwittrock.cpilint.rules.XsltVersionsRuleFactory;

public final class RulesFile {
	
	private static final String XML_SCHEMA_RESOURCE_PATH = "resources/xml-schema/rules-file-schema.xsd";
	private static final Logger logger = LoggerFactory.getLogger(RulesFile.class);
	private static final Collection<RuleFactory> ruleFactories;

	// TODO: At some point, the rule factories ought to be loaded via the service provider mechanism.
	
	static {
		ruleFactories = new ArrayList<>();
		ruleFactories.add(new CsrfProtectionRequiredRuleFactory());
		ruleFactories.add(new JavaArchivesRuleFactory());
		ruleFactories.add(new MappingTypesRuleFactory());
		ruleFactories.add(new ReceiverAdaptersRuleFactory());
		ruleFactories.add(new ScriptingLanguagesRuleFactory());
		ruleFactories.add(new SenderAdaptersRuleFactory());
		ruleFactories.add(new UnencryptedEndpointsNotAllowedRuleFactory());
		ruleFactories.add(new CleartextBasicAuthNotAllowedRuleFactory());
		ruleFactories.add(new XsltVersionsRuleFactory());
		ruleFactories.add(new IflowDescriptionRequiredRuleFactory());
		ruleFactories.add(new UnencryptedDataStoreWriteNotAllowedRuleFactory());
		ruleFactories.add(new ClientCertSenderChannelAuthNotAllowedRuleFactory());
		ruleFactories.add(new MultiConditionTypeRoutersNotAllowedRuleFactory());
		ruleFactories.add(new MatchingProcessDirectChannelsRequiredRuleFactory());
		ruleFactories.add(new DuplicateResourcesNotAllowedRuleFactory());
		ruleFactories.add(new NamingConventionsRuleFactory());
		ruleFactories.add(new UserRolesRuleFactory());
	}
	
	private RulesFile() {
		throw new AssertionError("Never supposed to be instantiated");
	}
	
	public static Collection<Rule> fromPath(Path rulesFile) {
		Objects.requireNonNull(rulesFile, "rulesFile must not be null");
		return fromPath(rulesFile, new HashSet<Path>());
	}

	private static Collection<Rule> fromPath(Path rulesFile, Set<Path> visited) {
		assert rulesFile != null;
		assert visited != null;
		if (!Files.exists(rulesFile)) {
			throw new RulesFileError("Rules file does not exist: " + rulesFile);
		}
		if (!Files.isRegularFile(rulesFile)) {
			throw new RulesFileError("Rules file is not a file: " + rulesFile);
		}
		logger.info("Processing rules file '{}'", rulesFile);
		Path canonicalRulesFile = toCanonicalPath(rulesFile);
		logger.debug("Canonical rules file path: {}", canonicalRulesFile);
		visited.add(canonicalRulesFile);
		Document doc;
		try (InputStream is = Files.newInputStream(canonicalRulesFile)) {
			doc = parseRulesFile(is);
		} catch (IOException e) {
			throw new RulesFileError("I/O error opening rules file", e);
		} catch (DocumentException | SAXException e) {
			throw new RulesFileError("Error parsing rules file XML", e);
		}
		Collection<Rule> rules = new ArrayList<>();
		/*
		 * If the rules file contains imports, process them recursively.
		 */
		Element imports = doc.getRootElement().element("imports");
		if (imports != null) {
			List<Element> importElements = imports.elements("import");
			logger.debug("Rules file contains {} import(s)", importElements.size());
			for (Element i : importElements) {
				logger.debug("Value of src attribute: {}", i.attributeValue("src"));
				Path importPath = canonicalRulesFile.getParent().resolve(i.attributeValue("src"));
				if (!Files.exists(importPath)) {
					throw new RulesFileError("Imported rules file does not exist: " + importPath);
				}
				if (!Files.isRegularFile(importPath)) {
					throw new RulesFileError("Imported rules file is not a file: " + importPath);
				}
				Path canonicalImportPath = toCanonicalPath(importPath);
				logger.debug("Canonical import path: {}", canonicalImportPath);
				if (visited.contains(canonicalImportPath)) {
					// Circular import detected.
					String message = String.format("Rules file '%s' has already been processed once (i.e. imports are circular)", canonicalImportPath);
					throw new RulesFileError(message);
				}
				logger.info("Recursively processing import '{}'", canonicalImportPath);
				rules.addAll(fromPath(canonicalImportPath, visited));
			}
		}
		/*
		 * If the rules file does not contain rules (i.e. it only contains imports), we can
		 * return early.
		 */
		if (doc.getRootElement().element("rules") == null) {
			return rules;
		}
		/*
		 *  The rules file contains rules. Get a List of all rule elements, i.e. elements below
		 *  /cpilint/rules.
		 */
		List<Element> ruleElements = doc.getRootElement().element("rules").elements();
		/*
		 * Now, for each rule element, get a Set of RuleFactory instances that
		 * are able to process that element. We expect exactly one RuleFactory
		 * to be able to do so. Zero factories and more than one factory is
		 * an error, and results in a RulesFileError being thrown. If exactly
		 * one factory can create a Rule object from the element, do so and
		 * add that Rule to the collection, that will be returned from this
		 * method.
		 */
		for (Element ruleElement : ruleElements) {
			String ruleName = ruleElement.getName();
			Set<RuleFactory> factories = ruleFactories
				.stream()
				.filter(f -> f.canCreateFrom(ruleElement))
				.collect(Collectors.toSet());
			if (factories.isEmpty()) {
				throw new RulesFileError(String.format("No factory available to process rule '%s'", ruleName));
			}
			if (factories.size() > 1) {
				logger.debug("Multiple RuleFactory instances available for element {}: {}",
					ruleName,
					factories.stream().map(f -> f.getClass().getName()).collect(Collectors.joining(",")));
				throw new RulesFileError(String.format("More than one factory available to process rule '%s'", ruleName));
			}
			RuleFactory factory = factories.iterator().next();
			rules.add(factory.createFrom(ruleElement));
		}
		return rules;
	}

	private static Path toCanonicalPath(Path p) {
		assert p != null;
		assert Files.exists(p) && Files.isRegularFile(p);
		Path canonical;
		try {
			/*
			 * Why not follow symbolic links? In the specific context of one
			 * rules file importing another rules file, relative paths going
			 * up the directory tree could start behaving in unexpected ways.
			 */
			canonical = p.toRealPath(LinkOption.NOFOLLOW_LINKS);
		} catch (IOException e) {
			throw new RulesFileError("Error accessing file system", e);
		}
		return canonical;
	}
	
	private static Document parseRulesFile(InputStream is) throws DocumentException, SAXException {
        SAXReader reader = new SAXReader();
		// Disable access to XML external entities.
		reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
		reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Document document = reader.read(is);
		validateRules(document);
		logger.info("Rules file XML is valid");
        return document;
	}

	private static void validateRules(Document document) {
		try {
			SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1");
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			Schema schema = factory.newSchema(new StreamSource(RulesFile.class.getClassLoader().getResourceAsStream(XML_SCHEMA_RESOURCE_PATH)));
			Validator validator = schema.newValidator();
			validator.setErrorHandler(new ErrorHandler() {
				public void warning(SAXParseException e) throws SAXException {
					// Ignore.
				}
				public void error(SAXParseException e) throws SAXException {
					failValidation(e);
				}
				public void fatalError(SAXParseException e) throws SAXException {
					failValidation(e);
				}
				private void failValidation(SAXParseException e) {
					logger.error("Rules file schema validation error: {}", e.getMessage());
					throw new RulesFileError("The rules file format is not valid.");
				}
			});
			validator.validate(new DocumentSource(document));
		} catch (SAXException e) {
			logger.error("SAXException from Validator", e);
			throw new RulesFileError("There was an error validating the rules file.");
		} catch (IOException e) {
			logger.error("IOException while validating the rules file", e);
			throw new RulesFileError("There was an I/O error while validating the rules file.");
		}
	}

}