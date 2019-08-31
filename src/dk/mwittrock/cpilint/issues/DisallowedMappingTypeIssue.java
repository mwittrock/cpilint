package dk.mwittrock.cpilint.issues;

import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.model.MappingType;

public final class DisallowedMappingTypeIssue extends StepIssueBase {
	
	private final MappingType mappingType;
	
	public DisallowedMappingTypeIssue(IflowArtifactTag tag, String stepName, String stepId, MappingType mappingType) {
		super(tag, stepName, stepId, String.format(
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
