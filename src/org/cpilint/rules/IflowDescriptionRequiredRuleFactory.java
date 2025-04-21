package org.cpilint.rules;

import org.dom4j.Element;

public final class IflowDescriptionRequiredRuleFactory implements RuleFactory {

	@Override
	public boolean isFactoryFor(String ruleElementName) {
		return ruleElementName.equals("iflow-description-required");
	}

	@Override
	public Rule createFrom(Element e) {
		if (!isFactoryFor(e.getName())) {
			throw new RuleFactoryError(String.format("Cannot create Rule object from element '%s'", e.getName()));
		}
		return new IflowDescriptionRequiredRule();
	}

}
