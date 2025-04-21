package org.cpilint.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

import org.dom4j.Element;

import org.cpilint.RulesFileError;

abstract class AllowDisallowRuleFactoryBase<T, U extends Rule> implements RuleFactory {
	
	private final String allowElementName;
	private final String disallowElementName;
	private final Map<String, T> valueMap;
	private final BiFunction<Boolean, Set<T>, U> ruleFunction; 
	
	AllowDisallowRuleFactoryBase(String allowElementName, String disallowElementName, Map<String, T> valueMap, BiFunction<Boolean, Set<T>, U> ruleFunction) {
		assert allowElementName != null;
		assert !allowElementName.isBlank();
		assert disallowElementName != null;
		assert !disallowElementName.isBlank();
		assert valueMap != null;
		assert !valueMap.isEmpty();
		assert ruleFunction != null;
		this.allowElementName = allowElementName;
		this.disallowElementName = disallowElementName;
		this.valueMap = new HashMap<>(valueMap);
		this.ruleFunction = ruleFunction;
	} 

	@Override
	public boolean isFactoryFor(String ruleElementName) {
		Objects.requireNonNull(ruleElementName, "ruleElementName must not be null");
		return ruleElementName.equals(allowElementName) || ruleElementName.equals(disallowElementName);
	}

	@Override
	public U createFrom(Element e) {
		Objects.requireNonNull(e, "e must not be null");
		String ruleElementName = e.getName();
		if (!isFactoryFor(ruleElementName)) {
			throw new RuleFactoryError(String.format("Cannot create Rule object from element '%s'", ruleElementName));
		}
		boolean allowed = ruleElementName.equals(allowElementName);
		/*
		 * For allow rules, the rule element must contain at least one allow
		 * element. For disallow rules, the rule element must contain at least
		 * one disallow element.
		 */
		String configElementName = allowed ? "allow" : "disallow";
		List<Element> configElements = e.elements(configElementName);
		if (configElements.isEmpty()) {
			throw new RulesFileError(String.format("Element '%s' must contain at least one '%s' element", ruleElementName, configElementName));
		}
		/*
		 * Each allow/disallow element must contain a string that maps to a
		 * T object.
		 */
		Set<T> values = new HashSet<>();
		for (Element ce : configElements) {
			String contents = ce.getText();
			if (!valueMap.containsKey(contents)) {
				throw new RulesFileError(String.format("Unknown value '%s' in configuration of rule '%s'", contents, ruleElementName));
			}
			values.add(valueMap.get(contents));
		}
		return ruleFunction.apply(allowed, values);
	}

}
