package dk.mwittrock.cpilint.rules;

import java.util.Map;

import dk.mwittrock.cpilint.model.MappingType;

public final class MappingTypesRuleFactory extends AllowDisallowRuleFactoryBase<MappingType, MappingTypesRule> {

	private static final Map<String, MappingType> mappingTypes = Map.of(
		"message-mapping", MappingType.MESSAGE_MAPPING,
		"operation-mapping", MappingType.OPERATION_MAPPING,
		"xslt-mapping", MappingType.XSLT_MAPPING
	);

	public MappingTypesRuleFactory() {
		super(
			"allowed-mapping-types",
			"disallowed-mapping-types",
			mappingTypes,
			(a, m) -> new MappingTypesRule(a, m)
		);
	}
	
}
