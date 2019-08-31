package dk.mwittrock.cpilint.model;

import java.util.Set;

public enum ScriptingLanguage {
	
	GROOVY("Groovy"),
	JAVASCRIPT("JavaScript");
	
	private final String name;
	
	ScriptingLanguage(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public static Set<ScriptingLanguage> allValuesExcept(Set<ScriptingLanguage> scriptingLanguages) {
		return ModelUtil.allValuesExcept(scriptingLanguages);
	}

}
