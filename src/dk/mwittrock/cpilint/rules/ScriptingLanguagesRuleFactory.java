package dk.mwittrock.cpilint.rules;

import java.util.Map;

import dk.mwittrock.cpilint.model.ScriptingLanguage;

public final class ScriptingLanguagesRuleFactory extends AllowDisallowRuleFactoryBase<ScriptingLanguage, ScriptingLanguagesRule> {
	
	private static final Map<String, ScriptingLanguage> scriptingLanguages = Map.of(
		"groovy", ScriptingLanguage.GROOVY,
		"javascript", ScriptingLanguage.JAVASCRIPT
	);
	
	public ScriptingLanguagesRuleFactory() {
		super(
			"allowed-scripting-languages",
			"disallowed-scripting-languages",
			scriptingLanguages,
			(a, l) -> new ScriptingLanguagesRule(a, l)
		);
	}

}
