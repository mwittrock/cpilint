package dk.mwittrock.cpilint.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.dom4j.Element;

import dk.mwittrock.cpilint.RulesFileError;
import dk.mwittrock.cpilint.artifacts.ArtifactResourceType;

public final class DuplicateResourcesNotAllowedRuleFactory implements RuleFactory {
	
	private static final Map<String, ArtifactResourceType> resourceTypes;
	
	static {
		resourceTypes = new HashMap<>();
		resourceTypes.put("message-mapping", ArtifactResourceType.MESSAGE_MAPPING);
		resourceTypes.put("xslt-mapping", ArtifactResourceType.XSLT_MAPPING);
		resourceTypes.put("operation-mapping", ArtifactResourceType.OPERATION_MAPPING);
		resourceTypes.put("javascript-script", ArtifactResourceType.JAVASCRIPT_SCRIPT);
		resourceTypes.put("groovy-script", ArtifactResourceType.GROOVY_SCRIPT);
		resourceTypes.put("java-archive", ArtifactResourceType.JAVA_ARCHIVE);
		resourceTypes.put("edmx", ArtifactResourceType.EDMX);
		resourceTypes.put("wsdl", ArtifactResourceType.WSDL);
		resourceTypes.put("xml-schema", ArtifactResourceType.XSD);
	}

	@Override
	public boolean canCreateFrom(Element e) {
		return e.getName().equals("duplicate-resources-not-allowed");
	}

	@Override
	public Rule createFrom(Element e) {
		if (!canCreateFrom(e)) {
			throw new RuleFactoryError(String.format("Cannot create Rule object from element '%s'", e.getName()));
		}
		Set<String> specifiedTypeNames = e.elements("resource-type")
			.stream()
			.map(Element::getText)
			.collect(Collectors.toSet());
		// Make sure that we recognize all the specified resources.
		Set<String> unknownTypeNames = specifiedTypeNames
			.stream()
			.filter(n -> !resourceTypes.containsKey(n))
			.collect(Collectors.toSet());
		if (!unknownTypeNames.isEmpty()) {
			throw new RulesFileError(String.format("Unknown resource type(s) in rule DuplicateResourcesNotAllowed: %s", unknownTypeNames.stream().collect(Collectors.joining(","))));
		}
		// All the specified resource types are recognized.
		Set<ArtifactResourceType> specifiedTypes = specifiedTypeNames
			.stream()
			.map(resourceTypes::get)
			.collect(Collectors.toSet());
		Rule r;
		if (specifiedTypes.isEmpty()) {
			r = new DuplicateResourcesNotAllowedRule();
		} else {
			r = new DuplicateResourcesNotAllowedRule(specifiedTypes);
		}
		return r;
	}

}
