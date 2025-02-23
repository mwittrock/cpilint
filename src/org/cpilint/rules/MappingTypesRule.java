package org.cpilint.rules;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.cpilint.IflowXml;
import org.cpilint.artifacts.IflowArtifact;
import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.issues.DisallowedMappingTypeIssue;
import org.cpilint.issues.Issue;
import org.cpilint.model.MappingType;
import org.cpilint.model.XmlModel;
import org.cpilint.model.XmlModelFactory;
import net.sf.saxon.s9api.XdmNode;

final class MappingTypesRule extends RuleBase {
	
	private final boolean allowed;
	private final Set<MappingType> mappingTypes;
	
	MappingTypesRule(boolean allowed, Set<MappingType> mappingTypes) {
		this.allowed = allowed;
		this.mappingTypes = new HashSet<>(mappingTypes);
	}

	@Override
	public void inspect(IflowArtifact iflow) {
		IflowXml iflowXml = iflow.getIflowXml();
		XmlModel model = XmlModelFactory.getModelFor(iflowXml);
		IflowArtifactTag tag = iflow.getTag();
		// We are only checking for the mapping types that are _not_ allowed.
		Set<MappingType> disallowedTypes = allowed ? MappingType.allValuesExcept(mappingTypes) : mappingTypes;
		Function<MappingType, String> xpathFunction = t ->  model.xpathForMappingSteps(t);
		Function<MappingType, Function<XdmNode, Issue>> issueFunctionFunction = t -> n -> new DisallowedMappingTypeIssue(ruleId, tag, model.getStepNameFromElement(n), model.getStepIdFromElement(n), t);
		XpathRulesUtil.iterateMultipleXpathsAndConsumeIssues(iflowXml, disallowedTypes, xpathFunction, issueFunctionFunction, consumer::consume);
	}

}
