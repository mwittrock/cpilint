package org.cpilint;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
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

import org.cpilint.rules.CleartextBasicAuthNotAllowedRuleFactory;
import org.cpilint.rules.ClientCertSenderChannelAuthNotAllowedRuleFactory;
import org.cpilint.rules.CsrfProtectionRequiredRuleFactory;
import org.cpilint.rules.DuplicateResourcesNotAllowedRuleFactory;
import org.cpilint.rules.ExtensionRuleFactory;
import org.cpilint.rules.IflowDescriptionRequiredRuleFactory;
import org.cpilint.rules.JavaArchivesRuleFactory;
import org.cpilint.rules.MappingTypesRuleFactory;
import org.cpilint.rules.MatchingProcessDirectChannelsRequiredRuleFactory;
import org.cpilint.rules.MultiConditionTypeRoutersNotAllowedRuleFactory;
import org.cpilint.rules.NamingConventionsRuleFactory;
import org.cpilint.rules.ReceiverAdaptersRuleFactory;
import org.cpilint.rules.Rule;
import org.cpilint.rules.RuleFactory;
import org.cpilint.rules.ScriptingLanguagesRuleFactory;
import org.cpilint.rules.SenderAdaptersRuleFactory;
import org.cpilint.rules.UnencryptedDataStoreWriteNotAllowedRuleFactory;
import org.cpilint.rules.UnencryptedEndpointsNotAllowedRuleFactory;
import org.cpilint.rules.UserRolesRuleFactory;
import org.cpilint.rules.XsltVersionsRuleFactory;
import org.cpilint.util.JarResourceUtil;

public final class RulesFile {
	
	private static final String XML_SCHEMA_RESOURCE_PATH = JarResourceUtil.RESOURCE_BASE_PATH + "/xml-schema/rules-file-schema.xsd";
	private static final String RULE_ID_ATTRIBUTE_NAME = "id";
	private static final String IMPORT_ELEMENT = "import";
	private static final String IMPORTS_ELEMENT = "imports";
	private static final String RULES_ELEMENT = "rules";
	private static final String EXTENSION_RULES_ELEMENT = "extension-rules";

	private static final Logger logger = LoggerFactory.getLogger(RulesFile.class);
	private static final Collection<RuleFactory> ruleFactories;
	private static final Collection<ExtensionRuleFactory> extensionRuleFactories;

	private final Collection<Rule> rules;
	private final Set<Exemption> exemptions;

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
		// Load extension rule factories via Service Provider Interface (if any are present).
		extensionRuleFactories = new ArrayList<>();
		ServiceLoader<ExtensionRuleFactory> spiLoader = ServiceLoader.load(ExtensionRuleFactory.class);
		for (ExtensionRuleFactory e : spiLoader) {
			logger.info("Loaded extension rule factory {}", e.getClass().getName());
			extensionRuleFactories.add(e);
		}
		logger.info("Extension rule factories loaded: {}", extensionRuleFactories.size());
	}
	
	private RulesFile(Collection<Rule> rules, Set<Exemption> exemptions) {
		assert rules != null;
		assert exemptions != null;
		this.rules = Collections.unmodifiableCollection(rules);
		this.exemptions = Collections.unmodifiableSet(exemptions);
	}

	public Collection<Rule> getRules() {
		return rules;
	}

	public Set<Exemption> getExemptions() {
		return exemptions;
	}
	
	public static RulesFile fromPath(Path rulesFilePath) {
		Objects.requireNonNull(rulesFilePath, "rulesFilePath must not be null");
		RulesFile rulesFile = fromPath(rulesFilePath, new HashSet<Path>());
		/*
		 * With the introduction of exemptions, XML Schema validation alone can no
		 * longer guarantee, that rules will be present. Therefore we check here if
		 * there are rules and fail if there are none.
		 */
		if (rulesFile.rules.isEmpty()) {
			throw new RulesFileError("No rules present after processing rules file(s)");
		}
		// Make sure that rule IDs are unique.
		Set<String> ruleIds = new HashSet<>();
		Set<String> duplicateRuleIds = rulesFile.rules
			.stream()
			.filter(r -> r.getId().isPresent())
			.map(r -> r.getId().get())
			.filter(i -> !ruleIds.add(i))
			.collect(Collectors.toSet());
		if (!duplicateRuleIds.isEmpty()) {
			throw new RulesFileError("Duplicate rule IDs in rules file(s): " + String.join(", ", duplicateRuleIds));
		}
		logger.debug("RulesFile created with {} rule(s) and {} exemption(s)", rulesFile.rules.size(), rulesFile.exemptions.size());
		return rulesFile;
	}

	private static RulesFile fromPath(Path rulesFilePath, Set<Path> visited) {
		assert rulesFilePath != null;
		assert visited != null;
		if (!Files.exists(rulesFilePath)) {
			throw new RulesFileError("Rules file does not exist: " + rulesFilePath);
		}
		if (!Files.isRegularFile(rulesFilePath)) {
			throw new RulesFileError("Rules file is not a file: " + rulesFilePath);
		}
		logger.info("Processing rules file '{}'", rulesFilePath);
		Path canonicalRulesFilePath = toCanonicalPath(rulesFilePath);
		logger.debug("Canonical rules file path: {}", canonicalRulesFilePath);
		visited.add(canonicalRulesFilePath);
		Document doc;
		try (InputStream is = Files.newInputStream(canonicalRulesFilePath)) {
			doc = parseRulesFile(is);
		} catch (IOException e) {
			throw new RulesFileError("I/O error opening rules file", e);
		} catch (DocumentException | SAXException e) {
			throw new RulesFileError("Error parsing rules file XML", e);
		}
		Collection<Rule> rules = new ArrayList<>();
		Set<Exemption> exemptions = new HashSet<>();
		/*
		 * If the rules file contains imports, process them recursively.
		 */
		Element imports = doc.getRootElement().element(IMPORTS_ELEMENT);
		if (imports != null) {
			List<Element> importElements = imports.elements(IMPORT_ELEMENT);
			logger.debug("Rules file contains {} import(s)", importElements.size());
			for (Element i : importElements) {
				logger.debug("Value of src attribute: {}", i.attributeValue("src"));
				Path importPath = canonicalRulesFilePath.getParent().resolve(i.attributeValue("src"));
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
				RulesFile imported = fromPath(canonicalImportPath, visited);
				rules.addAll(imported.rules);
				exemptions.addAll(imported.exemptions);
			}
		}
		/*
		 * Please note that rules and extension rules are processed slightly differently, since
		 * the latter must be validated by the extension rule factory, whereas the former are
		 * validated by the rules file XML Schema. First, we process the rules (if any are present).
		 */
		if (doc.getRootElement().element(RULES_ELEMENT) != null) {
			/*
			*  Get a List of all rule elements, i.e. elements below /cpilint/rules.
			*/
			List<Element> ruleElements = doc.getRootElement().element(RULES_ELEMENT).elements();
			/*
			* Now, for each rule element, get a Set of RuleFactory instances that
			* are able to process that element. We expect exactly one RuleFactory
			* to be able to do so. Zero factories and more than one factory is
			* an error, and results in a RulesFileError being thrown. If exactly
			* one factory can create a Rule object from the element, do so.
			*/
			for (Element ruleElement : ruleElements) {
				assert "".equals(ruleElement.getNamespaceURI());
				String ruleName = ruleElement.getName();
				Set<RuleFactory> factories = ruleFactories
					.stream()
					.filter(f -> f.isFactoryFor(ruleName))
					.collect(Collectors.toSet());
				if (factories.isEmpty()) {
					throw new RulesFileError("No factory available to process built-in rule '%s'".formatted(ruleName));
				}
				if (factories.size() > 1) {
					logger.debug("Multiple RuleFactory instances available for element '{}': {}",
						ruleName,
						factories.stream().map(f -> f.getClass().getName()).collect(Collectors.joining(",")));
					throw new RulesFileError("More than one factory available to process rule '%s'".formatted(ruleName));
				}
				RuleFactory factory = factories.iterator().next();
				Rule newRule = factory.createFrom(ruleElement);
				// If the rule element has an ID attribute, add the rule ID to the rule.
				String ruleId = ruleElement.attributeValue(RULE_ID_ATTRIBUTE_NAME);
				if (ruleId != null) {
					newRule.setId(ruleId);
				}
				rules.add(newRule);
			}
		}
		/*
		 * Next, we process the extension rules (if any are present).
		 */
		if (doc.getRootElement().element(EXTENSION_RULES_ELEMENT) != null) {
			List<Element> extensionRuleElements = doc.getRootElement().element(EXTENSION_RULES_ELEMENT).elements();
			/*
			 * Processed like the built-in rules, only using the ExtensionRuleFactory instances
			 * loaded via SPI and validating the extension rule elements. Please also note that
			 * all extension rule elements must belong to a namespace to avoid clashing names.
			 * The namespace URI must be passed to the isFactoryFor method.
			 */
			for (Element extensionRuleElement : extensionRuleElements) {
				String extensionRuleName = extensionRuleElement.getName();
				String extensionRuleNamespaceUri = extensionRuleElement.getNamespaceURI();
				assert !"".equals(extensionRuleNamespaceUri);
				Set<ExtensionRuleFactory> extensionFactories = extensionRuleFactories
					.stream()
					.filter(f -> f.isFactoryFor(extensionRuleName, extensionRuleNamespaceUri))
					.collect(Collectors.toSet());
				if (extensionFactories.isEmpty()) {
					throw new RulesFileError("No extension factory available to process extension rule '%s' in namespace '%s'".formatted(extensionRuleName, extensionRuleNamespaceUri));
				}
				if (extensionFactories.size() > 1) {
					logger.debug("Multiple ExtensionRuleFactory instances available for element '{}'' in namespace '{}'': {}",
						extensionRuleName,
						extensionRuleNamespaceUri,
						extensionFactories.stream().map(f -> f.getClass().getName()).collect(Collectors.joining(",")));
					throw new RulesFileError("More than one extension factory available to process extension rule '%s' in namespace '%s'".formatted(extensionRuleName, extensionRuleNamespaceUri));
				}
				ExtensionRuleFactory extensionFactory = extensionFactories.iterator().next();
				logger.debug("ExtensionRuleFactory '{}' will be used to create a Rule from extension rule element '{}' in namespace '{}'", extensionFactory.getClass().getName(), extensionRuleName, extensionRuleNamespaceUri);
				// Validate that the extension rule element contains all required configuration.
				extensionFactory.validateConfiguration(extensionRuleElement);
				Rule newRule = extensionFactory.createFrom(extensionRuleElement);
				// If the rule element has an ID attribute, add the rule ID to the rule.
				String ruleId = extensionRuleElement.attributeValue(RULE_ID_ATTRIBUTE_NAME);
				if (ruleId != null) {
					newRule.setId(ruleId);
				}
				rules.add(newRule);
			}
		}
		/*
		 * Finally, process all exemptions (if there are any).
		 */
		if (doc.getRootElement().element("exemptions") != null) {
			List<Element> exemptionElements = doc.getRootElement().element("exemptions").elements("exemption");
			for (Element e : exemptionElements) {
				final String ruleId = e.element("rule-id").getText();
				List<Element> iflowIds = e.elements("iflow-id");
				assert !iflowIds.isEmpty();
				exemptions.addAll(iflowIds
					.stream()
					.map(i -> new Exemption(ruleId, i.getText()))
					.collect(Collectors.toSet()));
				// Why aren't we reading the exemption reason? It's for documentation only.
			}
		}
		return new RulesFile(rules, exemptions);
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