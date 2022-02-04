package dk.mwittrock.cpilint.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.dom4j.Element;

import dk.mwittrock.cpilint.RulesFileError;
import dk.mwittrock.cpilint.model.Nameable;
import dk.mwittrock.cpilint.rules.naming.ConjunctionScheme;
import dk.mwittrock.cpilint.rules.naming.DisjunctionScheme;
import dk.mwittrock.cpilint.rules.naming.EndsWithScheme;
import dk.mwittrock.cpilint.rules.naming.EqualsScheme;
import dk.mwittrock.cpilint.rules.naming.NamingScheme;
import dk.mwittrock.cpilint.rules.naming.NegationScheme;
import dk.mwittrock.cpilint.rules.naming.RegexScheme;
import dk.mwittrock.cpilint.rules.naming.StartsWithScheme;

public final class NamingRuleFactory implements RuleFactory {
	
	private static final Map<String, Nameable> applyToValues;
	
	static {
		applyToValues = new HashMap<>();
		applyToValues.put("iflow.name", Nameable.IFLOW_NAME);
		applyToValues.put("iflow.id", Nameable.IFLOW_ID);
		applyToValues.put("channel.name", Nameable.CHANNEL);
		applyToValues.put("sender-channel.name", Nameable.SENDER_CHANNEL);
		applyToValues.put("receiver-channel.name", Nameable.RECEIVER_CHANNEL);
		applyToValues.put("mapping.name", Nameable.MAPPING);
		applyToValues.put("message-mapping.name", Nameable.MESSAGE_MAPPING);
		applyToValues.put("xslt-mapping.name", Nameable.XSLT_MAPPING);
		applyToValues.put("operation-mapping.name", Nameable.OPERATION_MAPPING);
		applyToValues.put("script.name", Nameable.SCRIPT);
		applyToValues.put("groovy-script.name", Nameable.GROOVY_SCRIPT);
		applyToValues.put("js-script.name", Nameable.JS_SCRIPT);
		applyToValues.put("sender.name", Nameable.SENDER);
		applyToValues.put("receiver.name", Nameable.RECEIVER);
		applyToValues.put("content-modifier.name", Nameable.CONTENT_MODIFIER);
	}

	@Override
	public boolean canCreateFrom(Element e) {
		Objects.requireNonNull(e, "e must not be null");
		return e.getName().equals("naming");
	}

	@Override
	public Rule createFrom(Element e) {
		Objects.requireNonNull(e, "e must not be null");
		if (!canCreateFrom(e)) {
			throw new RuleFactoryError(String.format("Cannot create Rule object from element '%s'", e.getName()));
		}
		// In the following, assume that the rules file has been validated.
		NamingScheme scheme = schemeFromElement(getOnlyChildElement(e.element("scheme")));
		String message = e.element("message").getText();
		// Determine the nameables the rule applies to.
		Set<Nameable> applyTo = new HashSet<>();
		for (Element a : e.elements("apply-to")) {
			String val = a.getText();
			assert applyToValues.containsKey(val);
			applyTo.add(applyToValues.get(val));
		}
		return new NamingRule(scheme, message, applyTo);
	}
	
	private static NamingScheme schemeFromElement(Element e) {
		String name = e.getName();
		NamingScheme scheme;
		switch (name) {
			case "starts-with":
				scheme = new StartsWithScheme(e.getText());
				break;
			case "ends-with":
				scheme = new EndsWithScheme(e.getText());
				break;
			case "equals":
				scheme = equalsSchemeFromElement(e);
				break;
			case "regex":
				scheme = regexSchemeFromElement(e);
				break;
			case "not":
				scheme = new NegationScheme(schemeFromElement(getOnlyChildElement(e)));
				break;
			case "and":
				scheme = new ConjunctionScheme(innerSchemesFromElement(e));
				break;
			case "or":
				scheme = new DisjunctionScheme(innerSchemesFromElement(e));
				break;
			default:
				throw new RulesFileError("Unknown naming scheme element: " + name);
		}
		return scheme;
	}
	
	private static NamingScheme equalsSchemeFromElement(Element e) {
		assert e.getName().equals("equals");
		String attributeVal = e.attributeValue("ignore-case");
		assert attributeVal == null || attributeVal.equals("yes") || attributeVal.equals("no");
		boolean ignoreCase = attributeVal != null && attributeVal.equals("yes");
		return new EqualsScheme(e.getText(), ignoreCase);
	}
	
	private static NamingScheme regexSchemeFromElement(Element e) {
		assert e.getName().equals("regex");
		String pattern = e.getText();
		Pattern p;
		try {
			p = Pattern.compile(pattern);
		} catch (PatternSyntaxException pse) {
			throw new RulesFileError("Invalid regular expression pattern in regex naming scheme: " + pattern, pse);
		}
		return new RegexScheme(p);
	}

	private static List<NamingScheme> innerSchemesFromElement(Element e) {
		return e.elements()
			.stream()
			.map(NamingRuleFactory::schemeFromElement)
			.collect(Collectors.toList());
	}
	
	private static Element getOnlyChildElement(Element e) {
		Iterator<Element> iterator = e.elementIterator();
		assert iterator.hasNext();
		Element child = iterator.next();
		assert !iterator.hasNext();
		return child;
	}
	
}
