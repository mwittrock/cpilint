package org.cpilint.issues;

import java.util.Optional;

import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.model.MappingType;

public final class DisallowedMappingTypeIssue extends StepIssueBase {
	
	private final MappingType mappingType;
	
	public DisallowedMappingTypeIssue(Optional<String> ruleId, IflowArtifactTag tag, String stepName, String stepId, MappingType mappingType) {
		super(ruleId, tag, stepName, stepId, String.format(
			"Mapping step '%s' (ID '%s') executes %s mapping, which is not allowed.", 
			stepName, 
			stepId,
			mappingTypeIndefiniteForm(mappingType)));
		this.mappingType = mappingType;
	}
	
	public MappingType getMappingType() {
		return mappingType;
	}

	private static String mappingTypeIndefiniteForm(MappingType mappingType) {
		String translation;
		switch (mappingType) {
			case MESSAGE_MAPPING:
				translation = "a message";
				break;
			case XSLT_MAPPING:
				translation = "an XSLT";
				break;
			case OPERATION_MAPPING:
				translation = "an operation";
				break;
			default:
				throw new AssertionError("Unexpected mapping type: " + mappingType.name());
		}
		return translation;
	}

}
