package org.example.cpilint.extensions;

import java.util.List;

import org.cpilint.RulesFileError;
import org.cpilint.rules.ExtensionRuleFactory;
import org.cpilint.rules.Rule;
import org.cpilint.rules.RuleFactoryError;
import org.dom4j.Element;

public final class IflowNameRuleFactory implements ExtensionRuleFactory {

    private static final String RULE_ELEMENT_NAME = "iflow-name";
    private static final String RULE_ELEMENT_NS_URI = "http://example.org/cpilint/extensions";
    private static final String EXPECTED_NAME_ELEMENT_NAME = "expected-name";

    @Override
    public boolean isFactoryFor(String ruleElementName, String namespaceUri) {
        return ruleElementName.equals(RULE_ELEMENT_NAME) && namespaceUri.equals(RULE_ELEMENT_NS_URI);
    }

    @Override
    public void validateConfiguration(Element e) {
        // The rule element must have a single child element containing the expected name.
        List<Element> children = e.elements();
        if (!(children.size() == 1 && children.get(0).getName().equals(EXPECTED_NAME_ELEMENT_NAME))) {
            throw new RulesFileError("Element '%s' must have a single '%s' child element".formatted(RULE_ELEMENT_NAME, EXPECTED_NAME_ELEMENT_NAME));
        }
        String textContent = children.get(0).getText();
        if (textContent.isEmpty()) {
            throw new RulesFileError("Element '%s' must contain the expected iflow name".formatted(EXPECTED_NAME_ELEMENT_NAME));
        }
    }

    @Override
    public Rule createFrom(Element e) {
        if (!isFactoryFor(e.getName(), e.getNamespaceURI())) {
            throw new RuleFactoryError("Not a factory for element '%s' in namespace '%s'".formatted(e.getName(), e.getNamespaceURI()));
        }
        validateConfiguration(e);
        String expectedName = e.element(EXPECTED_NAME_ELEMENT_NAME).getText();
        return new IflowNameRule(expectedName);
    }
    
}