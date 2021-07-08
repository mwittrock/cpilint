package dk.mwittrock.cpilint.rules;

import dk.mwittrock.cpilint.RulesFileError;
import org.dom4j.Element;

import java.util.regex.Pattern;

public final class IflowNameMatchesRuleFactory implements RuleFactory {

	private static final String PATTERN_ELEMENT_NAME = "naming-pattern";

	@Override
	public boolean canCreateFrom(Element e) {
		return e.getName().equals("iflow-matches-name");
	}

	@Override
	public Rule createFrom(Element e) {

		String ruleElementName = e.getName();
		if (!canCreateFrom(e)) {
			throw new RuleFactoryError(String.format("Cannot create Rule object from element '%s'", e.getName()));
		}

		/*
		 * Check for existence of configuration element
		 */
		if (e.elements(PATTERN_ELEMENT_NAME).isEmpty()) {
			throw new RulesFileError(String.format("Element '%s' must contain exact one '%s' element", ruleElementName, PATTERN_ELEMENT_NAME));
		}

		/*
		 * Extract the matching pattern
		 */
		try {
			Pattern namePattern = Pattern.compile(e.elements(PATTERN_ELEMENT_NAME).get(0).getText(), Pattern.DOTALL);
			return new IflowNameMatchesRule(namePattern);
		} catch (Exception ex) {
			throw new RulesFileError(String.format("Element '%s' must contain a valid Regular Expression (RegEx)", PATTERN_ELEMENT_NAME));
		}

	}

}
