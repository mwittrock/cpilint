package org.cpilint.rules;

import org.dom4j.Element;

public final class MatchingProcessDirectChannelsRequiredRuleFactory implements RuleFactory {

	@Override
	public boolean isFactoryFor(String ruleElementName) {
		return ruleElementName.equals("matching-process-direct-channels-required");
	}

	@Override
	public Rule createFrom(Element e) {
		if (!isFactoryFor(e.getName())) {
			throw new RuleFactoryError(String.format("Cannot create Rule object from element '%s'", e.getName()));
		}
		return new MatchingProcessDirectChannelsRequiredRule();
	}

}
