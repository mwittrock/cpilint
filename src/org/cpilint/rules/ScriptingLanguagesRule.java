package org.cpilint.rules;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.cpilint.IflowXml;
import org.cpilint.artifacts.IflowArtifact;
import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.issues.DisallowedScriptingLanguageIssue;
import org.cpilint.issues.Issue;
import org.cpilint.model.ScriptingLanguage;
import org.cpilint.model.XmlModel;
import org.cpilint.model.XmlModelFactory;
import net.sf.saxon.s9api.XdmNode;

final class ScriptingLanguagesRule extends RuleBase {
	
	private final boolean allowed;
	private final Set<ScriptingLanguage> scriptingLanguages;

	ScriptingLanguagesRule(boolean allowed, Set<ScriptingLanguage> scriptingLanguages) {
		this.allowed = allowed;
		this.scriptingLanguages = new HashSet<>(scriptingLanguages);
	}
	
	@Override
	public void inspect(IflowArtifact iflow) {
		IflowXml iflowXml = iflow.getIflowXml();
		XmlModel model = XmlModelFactory.getModelFor(iflowXml);
		IflowArtifactTag tag = iflow.getTag();
		// We are only checking for the scripting languages that are _not_ allowed.
		Set<ScriptingLanguage> disallowedLanguages = allowed ? ScriptingLanguage.allValuesExcept(scriptingLanguages) : scriptingLanguages;
		Function<ScriptingLanguage, String> xpathFunction = l -> model.xpathForScriptSteps(l);
		Function<ScriptingLanguage, Function<XdmNode, Issue>> issueFunctionFunction = l -> n -> new DisallowedScriptingLanguageIssue(ruleId, tag, model.getStepNameFromElement(n), model.getStepIdFromElement(n), l);
		XpathRulesUtil.iterateMultipleXpathsAndConsumeIssues(iflowXml, disallowedLanguages, xpathFunction, issueFunctionFunction, consumer::consume);
	}

}
