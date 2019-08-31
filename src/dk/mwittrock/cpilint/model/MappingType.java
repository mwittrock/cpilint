package dk.mwittrock.cpilint.model;

import java.util.Set;

public enum MappingType {
	
	MESSAGE_MAPPING,
	XSLT_MAPPING,
	OPERATION_MAPPING;
	
	public static Set<MappingType> allValuesExcept(Set<MappingType> mappingTypes) {
		return ModelUtil.allValuesExcept(mappingTypes);
	}
	
}
