package org.cpilint.rules;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.dom4j.Element;

import org.cpilint.RulesFileError;

public final class UserRolesRuleFactory implements RuleFactory {

    private static final String allowElementName = "allowed-user-roles";
    private static final String disallowElementName = "disallowed-user-roles";

    @Override
    public boolean isFactoryFor(String ruleElementName) {
		Objects.requireNonNull(ruleElementName, "ruleElementName must not be null");
		return ruleElementName.equals(allowElementName) || ruleElementName.equals(disallowElementName);
    }

    @Override
    public Rule createFrom(Element e) {
		Objects.requireNonNull(e, "e must not be null");
		final String ruleElementName = e.getName();
        if (!isFactoryFor(e.getName())) {
			throw new RuleFactoryError(String.format("Cannot create Rule object from element '%s'", ruleElementName));
		}
        final boolean isAllowList = ruleElementName.equals(allowElementName);
        final String configElementName = isAllowList ? "allow" : "disallow";
		final List<Element> configElements = e.elements(configElementName);
		if (configElements.isEmpty()) {
            // With a validated rules file, this should never happen!
			throw new RulesFileError(String.format("Element '%s' must contain at least one '%s' element", ruleElementName, configElementName));
		}
        final Set<String> userRoles = configElements.stream()
            .map(Element::getText)
            .collect(Collectors.toSet());
        return new UserRolesRule(isAllowList, userRoles);
    }

}