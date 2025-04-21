package org.cpilint.rules;

import org.dom4j.Element;

public interface RuleFactory {
	
	public boolean isFactoryFor(String ruleElementName);
	
	public Rule createFrom(Element e);

}
