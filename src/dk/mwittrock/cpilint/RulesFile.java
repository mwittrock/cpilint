package dk.mwittrock.cpilint;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ServiceLoader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.mwittrock.cpilint.rules.CleartextBasicAuthNotAllowedRuleFactory;
import dk.mwittrock.cpilint.rules.ClientCertSenderChannelAuthNotAllowedRuleFactory;
import dk.mwittrock.cpilint.rules.CsrfProtectionRequiredRuleFactory;
import dk.mwittrock.cpilint.rules.DuplicateResourcesNotAllowedRuleFactory;
import dk.mwittrock.cpilint.rules.IflowDescriptionRequiredRuleFactory;
import dk.mwittrock.cpilint.rules.JavaArchivesRuleFactory;
import dk.mwittrock.cpilint.rules.MappingTypesRuleFactory;
import dk.mwittrock.cpilint.rules.MatchingProcessDirectChannelsRequiredRuleFactory;
import dk.mwittrock.cpilint.rules.MultiConditionTypeRoutersNotAllowedRuleFactory;
import dk.mwittrock.cpilint.rules.ReceiverAdaptersRuleFactory;
import dk.mwittrock.cpilint.rules.Rule;
import dk.mwittrock.cpilint.rules.RuleFactory;
import dk.mwittrock.cpilint.rules.ScriptingLanguagesRuleFactory;
import dk.mwittrock.cpilint.rules.SenderAdaptersRuleFactory;
import dk.mwittrock.cpilint.rules.UnencryptedDataStoreWriteNotAllowedRuleFactory;
import dk.mwittrock.cpilint.rules.UnencryptedEndpointsNotAllowedRuleFactory;
import dk.mwittrock.cpilint.rules.XsltVersionsRuleFactory;

public final class RulesFile {
	
	private static final Logger logger = LoggerFactory.getLogger(RulesFile.class);
	private static final Collection<RuleFactory> ruleFactories;
	private static final ServiceLoader<RuleFactory> loader = ServiceLoader.load(RuleFactory.class);
	
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
		Iterator<RuleFactory> extensionRules = loader.iterator();
		logger.debug("Checking for extensions");
		while(extensionRules.hasNext())
		{
			RuleFactory extensionRule = extensionRules.next();
			logger.debug("Found new extension: '%s'", extensionRule.getClass().getName());
			ruleFactories.add(extensionRule);
		}
	}
	
	private RulesFile() {
		throw new AssertionError("Never supposed to be instantiated");
	}
	
	public static Collection<Rule> fromPath(Path rulesFile) {
		if (!Files.exists(rulesFile)) {
			throw new IllegalArgumentException("Provided rules file does not exist");
		}
		if (!Files.isRegularFile(rulesFile)) {
			throw new IllegalArgumentException("Provided rules file is not a file");
		}
		InputStream is;
		try {
			 is = Files.newInputStream(rulesFile);
		} catch (IOException e) {
			throw new RulesFileError("I/O error opening rules file", e);
		}
		return fromInputStream(is);
	}

	public static Collection<Rule> fromInputStream(InputStream is) {
		// Parse the rules document.
		Document doc;
		try {
			doc = parseRulesFile(is);
		} catch (DocumentException e) {
			throw new RulesFileError("Error parsing rules file XML", e);
		}
		/*
		 *  Get a List of all rule elements, i.e. elements below 
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
		Collection<Rule> rules = new ArrayList<>();
		for (Element ruleElement : ruleElements) {
			Set<RuleFactory> factories = ruleFactories
				.stream()
				.filter(f -> f.canCreateFrom(ruleElement))
				.collect(Collectors.toSet());
			if (factories.isEmpty()) {
				throw new RulesFileError(String.format("No factory available to process rule '%s'. Please check your classpath", ruleElement.getName()));
			}
			if (factories.size() > 1) {
				logger.debug("Multiple RuleFactory instances available for element {}: {}",
					ruleElement.getName(),
					factories.stream().map(f -> f.getClass().getName()).collect(Collectors.joining(",")));
				throw new RulesFileError(String.format("More than one factory available to process rule '%s'", ruleElement.getName()));
			}
			RuleFactory factory = factories.iterator().next();
			rules.add(factory.createFrom(ruleElement));
		}
		return rules;
	}
	
	private static Document parseRulesFile(InputStream is) throws DocumentException {
		// TODO: Also validate the XML.
        SAXReader reader = new SAXReader();
        Document document = reader.read(is);
        return document;
	}	

}
