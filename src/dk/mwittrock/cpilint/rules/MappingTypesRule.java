package dk.mwittrock.cpilint.rules;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import dk.mwittrock.cpilint.IflowXml;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.issues.DisallowedMappingTypeIssue;
import dk.mwittrock.cpilint.issues.Issue;
import dk.mwittrock.cpilint.model.MappingType;
import dk.mwittrock.cpilint.model.XmlModel;
import dk.mwittrock.cpilint.model.XmlModelFactory;
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
		Function<MappingType, Function<XdmNode, Issue>> issueFunctionFunction = t -> n -> new DisallowedMappingTypeIssue(tag, model.getStepNameFromElement(n), model.getStepIdFromElement(n), t);
		XpathRulesUtil.iterateMultipleXpathsAndConsumeIssues(iflowXml, disallowedTypes, xpathFunction, issueFunctionFunction, consumer::consume);
	}

}
