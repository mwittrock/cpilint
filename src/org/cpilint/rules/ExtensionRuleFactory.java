package org.cpilint.rules;

import org.dom4j.Element;

public interface ExtensionRuleFactory {

	public boolean isFactoryFor(String ruleElementName, String namespaceUri);
	
	public Rule createFrom(Element e);

    public void validateConfiguration(Element e);
    
}
