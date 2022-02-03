package dk.mwittrock.cpilint.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import dk.mwittrock.cpilint.IflowXml;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.issues.NamingRuleIssue;
import dk.mwittrock.cpilint.model.ChannelDirection;
import dk.mwittrock.cpilint.model.MappingType;
import dk.mwittrock.cpilint.model.Nameable;
import dk.mwittrock.cpilint.model.XmlModel;
import dk.mwittrock.cpilint.model.XmlModelFactory;
import dk.mwittrock.cpilint.rules.naming.NamingScheme;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

final class NamingRule extends RuleBase {
	
	private static final Map<Nameable, Function<XmlModel, String>> nameableToXpathFunctionMap;
	private static final Map<Nameable, BiFunction<XdmNode, XmlModel, String>> nameableToNameFunctionMap;
	private static final Map<Nameable, BiFunction<XdmNode, XmlModel, String>> nameableToIdentFunctionMap;
	
	static {
		// Initialize the nameableToXpathFunctionMap map.
		nameableToXpathFunctionMap = new HashMap<>();
		nameableToXpathFunctionMap.put(Nameable.CHANNEL, m -> m.xpathForChannels());
		nameableToXpathFunctionMap.put(Nameable.SENDER_CHANNEL, m -> m.xpathForChannels(m.channelPredicateForDirection(ChannelDirection.SENDER)));
		nameableToXpathFunctionMap.put(Nameable.RECEIVER_CHANNEL, m -> m.xpathForChannels(m.channelPredicateForDirection(ChannelDirection.RECEIVER)));
		nameableToXpathFunctionMap.put(Nameable.MAPPING, m -> m.xpathForFlowSteps(m.stepPredicateForMappingSteps()));
		nameableToXpathFunctionMap.put(Nameable.MESSAGE_MAPPING, m -> m.xpathForMappingSteps(MappingType.MESSAGE_MAPPING));
		nameableToXpathFunctionMap.put(Nameable.XSLT_MAPPING, m -> m.xpathForMappingSteps(MappingType.XSLT_MAPPING));
		nameableToXpathFunctionMap.put(Nameable.OPERATION_MAPPING, m -> m.xpathForMappingSteps(MappingType.OPERATION_MAPPING));
		// Initialize the nameableToNameFunctionMap map.
		nameableToNameFunctionMap = new HashMap<>();
		nameableToNameFunctionMap.put(Nameable.CHANNEL, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.SENDER_CHANNEL, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.RECEIVER_CHANNEL, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.MAPPING, (n, m) -> m.getStepNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.MESSAGE_MAPPING, (n, m) -> m.getStepNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.XSLT_MAPPING, (n, m) -> m.getStepNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.OPERATION_MAPPING, (n, m) -> m.getStepNameFromElement(n));
		// Initialize the nameableToIdentFunctionMap map.
		nameableToIdentFunctionMap = new HashMap<>();
		nameableToIdentFunctionMap.put(Nameable.CHANNEL, (n, m) -> String.format("channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.SENDER_CHANNEL, (n, m) -> String.format("sender channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.RECEIVER_CHANNEL, (n, m) -> String.format("receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.MAPPING, (n, m) -> String.format("mapping step '%s' (ID '%s')", m.getStepNameFromElement(n), m.getStepIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.MESSAGE_MAPPING, (n, m) -> String.format("message mapping step '%s' (ID '%s')", m.getStepNameFromElement(n), m.getStepIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.XSLT_MAPPING, (n, m) -> String.format("XSLT mapping step '%s' (ID '%s')", m.getStepNameFromElement(n), m.getStepIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.OPERATION_MAPPING, (n, m) -> String.format("operation mapping step '%s' (ID '%s')", m.getStepNameFromElement(n), m.getStepIdFromElement(n)));
		// The keys of the above maps should be identical.
		assert nameableToXpathFunctionMap.keySet().equals(nameableToNameFunctionMap.keySet());
		assert nameableToNameFunctionMap.keySet().equals(nameableToIdentFunctionMap.keySet());
	}
	
	private final NamingScheme scheme;
	private final String message;
	private final Set<Nameable> applyTo;
	
	NamingRule(NamingScheme scheme, String message, Set<Nameable> applyTo) {
		this.scheme = Objects.requireNonNull(scheme, "scheme must not be null");
		Objects.requireNonNull(message, "message must not be null");
		if (message.isBlank()) {
			throw new IllegalArgumentException("message must not be blank");
		}
		this.message = message;
		/*
		 *  The applyTo set can neither be null nor empty, and it cannot contain
		 *  a null element.
		 */
		Objects.requireNonNull(applyTo, "applyTo must not be null");
		if (applyTo.isEmpty()) {
			throw new IllegalArgumentException("applyTo must not be empty");
		}
		if (applyTo.contains(null)) {
			throw new IllegalArgumentException("applyTo must not contain null");
		}
		this.applyTo = new HashSet<>(applyTo);
	}

	@Override
	public void inspect(IflowArtifact iflow) {
		IflowXml iflowXml = iflow.getIflowXml();
		XmlModel model = XmlModelFactory.getModelFor(iflowXml);
		IflowArtifactTag tag = iflow.getTag();
		for (Nameable n : applyTo) {
			/*
			 *  Iflow name and ID are special cases, since they're the only
			 *  ones that are not extracted with XPath from the iflow XML.
			 */
			if (n == Nameable.IFLOW_NAME) {
				String iflowName = tag.getName(); 
				if (!scheme.test(iflowName)) {
					consumer.consume(new NamingRuleIssue(tag, errorMessage("iflow name"), iflowName));
				}
				continue;
			}
			if (n == Nameable.IFLOW_ID) {
				String iflowId = tag.getId();
				if (!scheme.test(iflowId)) {
					consumer.consume(new NamingRuleIssue(tag, errorMessage("iflow ID"), iflowId));
				}
				continue;
			}
			/*
			 * Since we've already asserted that the keys of the three maps are
			 * identical, we only need to assert that the current Nameable
			 * is a key in one of them. Since we got this far, the current
			 * Nameable is not the iflow name or ID.
			 */
			assert nameableToXpathFunctionMap.containsKey(n);
			String xpath = nameableToXpathFunctionMap.get(n).apply(model);
			for (XdmItem i : iflowXml.evaluateXpath(xpath)) {
				assert i.isNode();
				XdmNode node = (XdmNode)i;
				String name = nameableToNameFunctionMap.get(n).apply(node, model);
				if (!scheme.test(name)) {
					// This name does not follow the naming scheme.
					String ident = nameableToIdentFunctionMap.get(n).apply(node, model);
					consumer.consume(new NamingRuleIssue(tag, errorMessage(ident), name));
				}
			}
		}
	}
	
	private String errorMessage(String ident) {
		return String.format("The %s does not follow the naming scheme: %s", ident, message);
	}

}
