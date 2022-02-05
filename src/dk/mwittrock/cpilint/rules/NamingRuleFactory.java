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
		applyToValues.put("channel.name", Nameable.CHANNEL_NAME);
		applyToValues.put("sender-channel.name", Nameable.SENDER_CHANNEL_NAME);
		applyToValues.put("amqp-sender-channel.name", Nameable.AMQP_SENDER_CHANNEL_NAME);
		applyToValues.put("ariba-sender-channel.name", Nameable.ARIBA_SENDER_CHANNEL_NAME);
		applyToValues.put("as2-sender-channel.name", Nameable.AS2_SENDER_CHANNEL_NAME);
		applyToValues.put("as4-sender-channel.name", Nameable.AS4_SENDER_CHANNEL_NAME);
		applyToValues.put("ftp-sender-channel.name", Nameable.FTP_SENDER_CHANNEL_NAME);
		applyToValues.put("https-sender-channel.name", Nameable.HTTPS_SENDER_CHANNEL_NAME);
		applyToValues.put("idoc-sender-channel.name", Nameable.IDOC_SENDER_CHANNEL_NAME);
		applyToValues.put("jms-sender-channel.name", Nameable.JMS_SENDER_CHANNEL_NAME);
		applyToValues.put("kafka-sender-channel.name", Nameable.KAFKA_SENDER_CHANNEL_NAME);
		applyToValues.put("mail-sender-channel.name", Nameable.MAIL_SENDER_CHANNEL_NAME);
		applyToValues.put("odata-sender-channel.name", Nameable.ODATA_SENDER_CHANNEL_NAME);
		applyToValues.put("processdirect-sender-channel.name", Nameable.PROCESSDIRECT_SENDER_CHANNEL_NAME);
		applyToValues.put("sftp-sender-channel.name", Nameable.SFTP_SENDER_CHANNEL_NAME);
		applyToValues.put("soap-sender-channel.name", Nameable.SOAP_SENDER_CHANNEL_NAME);
		applyToValues.put("successfactors-sender-channel.name", Nameable.SUCCESSFACTORS_SENDER_CHANNEL_NAME);
		applyToValues.put("xi-sender-channel.name", Nameable.XI_SENDER_CHANNEL_NAME);
		applyToValues.put("receiver-channel.name", Nameable.RECEIVER_CHANNEL_NAME);
		applyToValues.put("amqp-receiver-channel.name", Nameable.AMQP_RECEIVER_CHANNEL_NAME);
		applyToValues.put("ariba-receiver-channel.name", Nameable.ARIBA_RECEIVER_CHANNEL_NAME);
		applyToValues.put("as2-receiver-channel.name", Nameable.AS2_RECEIVER_CHANNEL_NAME);
		applyToValues.put("as4-receiver-channel.name", Nameable.AS4_RECEIVER_CHANNEL_NAME);
		applyToValues.put("elster-receiver-channel.name", Nameable.ELSTER_RECEIVER_CHANNEL_NAME);
		applyToValues.put("facebook-receiver-channel.name", Nameable.FACEBOOK_RECEIVER_CHANNEL_NAME);
		applyToValues.put("ftp-receiver-channel.name", Nameable.FTP_RECEIVER_CHANNEL_NAME);
		applyToValues.put("odata-receiver-channel.name", Nameable.ODATA_RECEIVER_CHANNEL_NAME);
		applyToValues.put("http-receiver-channel.name", Nameable.HTTP_RECEIVER_CHANNEL_NAME);
		applyToValues.put("idoc-receiver-channel.name", Nameable.IDOC_RECEIVER_CHANNEL_NAME);
		applyToValues.put("jdbc-receiver-channel.name", Nameable.JDBC_RECEIVER_CHANNEL_NAME);
		applyToValues.put("jms-receiver-channel.name", Nameable.JMS_RECEIVER_CHANNEL_NAME);
		applyToValues.put("kafka-receiver-channel.name", Nameable.KAFKA_RECEIVER_CHANNEL_NAME);
		applyToValues.put("ldap-receiver-channel.name", Nameable.LDAP_RECEIVER_CHANNEL_NAME);
		applyToValues.put("mail-receiver-channel.name", Nameable.MAIL_RECEIVER_CHANNEL_NAME);
		applyToValues.put("odc-receiver-channel.name", Nameable.ODC_RECEIVER_CHANNEL_NAME);
		applyToValues.put("openconnectors-receiver-channel.name", Nameable.OPENCONNECTORS_RECEIVER_CHANNEL_NAME);
		applyToValues.put("processdirect-receiver-channel.name", Nameable.PROCESSDIRECT_RECEIVER_CHANNEL_NAME);
		applyToValues.put("rfc-receiver-channel.name", Nameable.RFC_RECEIVER_CHANNEL_NAME);
		applyToValues.put("sftp-receiver-channel.name", Nameable.SFTP_RECEIVER_CHANNEL_NAME);
		applyToValues.put("soap-receiver-channel.name", Nameable.SOAP_RECEIVER_CHANNEL_NAME);
		applyToValues.put("successfactors-receiver-channel.name", Nameable.SUCCESSFACTORS_RECEIVER_CHANNEL_NAME);
		applyToValues.put("twitter-receiver-channel.name", Nameable.TWITTER_RECEIVER_CHANNEL_NAME);
		applyToValues.put("xi-receiver-channel.name", Nameable.XI_RECEIVER_CHANNEL_NAME);
		applyToValues.put("mapping.name", Nameable.MAPPING_STEP_NAME);
		applyToValues.put("message-mapping.name", Nameable.MESSAGE_MAPPING_STEP_NAME);
		applyToValues.put("xslt-mapping.name", Nameable.XSLT_MAPPING_STEP_NAME);
		applyToValues.put("operation-mapping.name", Nameable.OPERATION_MAPPING_STEP_NAME);
		applyToValues.put("script.name", Nameable.SCRIPT_STEP_NAME);
		applyToValues.put("groovy-script.name", Nameable.GROOVY_SCRIPT_STEP_NAME);
		applyToValues.put("js-script.name", Nameable.JS_SCRIPT_STEP_NAME);
		applyToValues.put("sender.name", Nameable.SENDER_NAME);
		applyToValues.put("receiver.name", Nameable.RECEIVER_NAME);
		applyToValues.put("content-modifier.name", Nameable.CONTENT_MODIFIER_STEP_NAME);
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
