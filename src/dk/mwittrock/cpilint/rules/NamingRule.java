package dk.mwittrock.cpilint.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.mwittrock.cpilint.IflowXml;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.issues.NamingRuleIssue;
import dk.mwittrock.cpilint.model.ChannelDirection;
import dk.mwittrock.cpilint.model.MappingType;
import dk.mwittrock.cpilint.model.Nameable;
import dk.mwittrock.cpilint.model.ReceiverAdapter;
import dk.mwittrock.cpilint.model.ScriptingLanguage;
import dk.mwittrock.cpilint.model.SenderAdapter;
import dk.mwittrock.cpilint.model.XmlModel;
import dk.mwittrock.cpilint.model.XmlModelFactory;
import dk.mwittrock.cpilint.rules.naming.NamingScheme;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

final class NamingRule extends RuleBase {
	
	private static final Map<Nameable, Function<XmlModel, String>> nameableToXpathFunctionMap;
	private static final Map<Nameable, BiFunction<XdmNode, XmlModel, String>> nameableToNameFunctionMap;
	private static final Map<Nameable, BiFunction<XdmNode, XmlModel, String>> nameableToIdentFunctionMap;
	private static final Logger logger = LoggerFactory.getLogger(NamingRule.class);
	
	static {
		// Initialize the nameableToXpathFunctionMap map.
		nameableToXpathFunctionMap = new HashMap<>();
		nameableToXpathFunctionMap.put(Nameable.CHANNEL_NAME, m -> m.xpathForChannels());
		nameableToXpathFunctionMap.put(Nameable.SENDER_CHANNEL_NAME, m -> m.xpathForChannels(m.channelPredicateForDirection(ChannelDirection.SENDER)));
		nameableToXpathFunctionMap.put(Nameable.AMQP_SENDER_CHANNEL_NAME, m -> m.xpathForSenderChannels(SenderAdapter.AMQP));
		nameableToXpathFunctionMap.put(Nameable.ARIBA_SENDER_CHANNEL_NAME, m -> m.xpathForSenderChannels(SenderAdapter.ARIBA));
		nameableToXpathFunctionMap.put(Nameable.AS2_SENDER_CHANNEL_NAME, m -> m.xpathForSenderChannels(SenderAdapter.AS2));
		nameableToXpathFunctionMap.put(Nameable.AS4_SENDER_CHANNEL_NAME, m -> m.xpathForSenderChannels(SenderAdapter.AS4));
		nameableToXpathFunctionMap.put(Nameable.FTP_SENDER_CHANNEL_NAME, m -> m.xpathForSenderChannels(SenderAdapter.FTP));
		nameableToXpathFunctionMap.put(Nameable.HTTPS_SENDER_CHANNEL_NAME, m -> m.xpathForSenderChannels(SenderAdapter.HTTPS));
		nameableToXpathFunctionMap.put(Nameable.IDOC_SENDER_CHANNEL_NAME, m -> m.xpathForSenderChannels(SenderAdapter.IDOC));
		nameableToXpathFunctionMap.put(Nameable.JMS_SENDER_CHANNEL_NAME, m -> m.xpathForSenderChannels(SenderAdapter.JMS));
		nameableToXpathFunctionMap.put(Nameable.KAFKA_SENDER_CHANNEL_NAME, m -> m.xpathForSenderChannels(SenderAdapter.KAFKA));
		nameableToXpathFunctionMap.put(Nameable.MAIL_SENDER_CHANNEL_NAME, m -> m.xpathForSenderChannels(SenderAdapter.MAIL));
		nameableToXpathFunctionMap.put(Nameable.ODATA_SENDER_CHANNEL_NAME, m -> m.xpathForSenderChannels(SenderAdapter.ODATA));
		nameableToXpathFunctionMap.put(Nameable.PROCESSDIRECT_SENDER_CHANNEL_NAME, m -> m.xpathForSenderChannels(SenderAdapter.PROCESSDIRECT));
		nameableToXpathFunctionMap.put(Nameable.SFTP_SENDER_CHANNEL_NAME, m -> m.xpathForSenderChannels(SenderAdapter.SFTP));
		nameableToXpathFunctionMap.put(Nameable.SOAP_SENDER_CHANNEL_NAME, m -> m.xpathForSenderChannels(SenderAdapter.SOAP));
		nameableToXpathFunctionMap.put(Nameable.SUCCESSFACTORS_SENDER_CHANNEL_NAME, m -> m.xpathForSenderChannels(SenderAdapter.SUCCESSFACTORS));
		nameableToXpathFunctionMap.put(Nameable.XI_SENDER_CHANNEL_NAME, m -> m.xpathForSenderChannels(SenderAdapter.XI));
		nameableToXpathFunctionMap.put(Nameable.RECEIVER_CHANNEL_NAME, m -> m.xpathForChannels(m.channelPredicateForDirection(ChannelDirection.RECEIVER)));
		nameableToXpathFunctionMap.put(Nameable.AMQP_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.AMQP));
		nameableToXpathFunctionMap.put(Nameable.ARIBA_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.ARIBA));
		nameableToXpathFunctionMap.put(Nameable.AS2_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.AS2));
		nameableToXpathFunctionMap.put(Nameable.AS4_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.AS4));
		nameableToXpathFunctionMap.put(Nameable.ELSTER_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.ELSTER));
		nameableToXpathFunctionMap.put(Nameable.FACEBOOK_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.FACEBOOK));
		nameableToXpathFunctionMap.put(Nameable.FTP_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.FTP));
		nameableToXpathFunctionMap.put(Nameable.ODATA_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.ODATA));
		nameableToXpathFunctionMap.put(Nameable.HTTP_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.HTTP));
		nameableToXpathFunctionMap.put(Nameable.IDOC_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.IDOC));
		nameableToXpathFunctionMap.put(Nameable.JDBC_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.JDBC));
		nameableToXpathFunctionMap.put(Nameable.JMS_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.JMS));
		nameableToXpathFunctionMap.put(Nameable.KAFKA_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.KAFKA));
		nameableToXpathFunctionMap.put(Nameable.LDAP_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.LDAP));
		nameableToXpathFunctionMap.put(Nameable.MAIL_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.MAIL));
		nameableToXpathFunctionMap.put(Nameable.ODC_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.ODC));
		nameableToXpathFunctionMap.put(Nameable.OPENCONNECTORS_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.OPENCONNECTORS));
		nameableToXpathFunctionMap.put(Nameable.PROCESSDIRECT_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.PROCESSDIRECT));
		nameableToXpathFunctionMap.put(Nameable.RFC_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.RFC));
		nameableToXpathFunctionMap.put(Nameable.SFTP_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.SFTP));
		nameableToXpathFunctionMap.put(Nameable.SOAP_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.SOAP));
		nameableToXpathFunctionMap.put(Nameable.SUCCESSFACTORS_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.SUCCESSFACTORS));
		nameableToXpathFunctionMap.put(Nameable.TWITTER_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.TWITTER));
		nameableToXpathFunctionMap.put(Nameable.XI_RECEIVER_CHANNEL_NAME, m -> m.xpathForReceiverChannels(ReceiverAdapter.XI));
		nameableToXpathFunctionMap.put(Nameable.MAPPING_STEP_NAME, m -> m.xpathForFlowSteps(m.stepPredicateForMappingSteps()));
		nameableToXpathFunctionMap.put(Nameable.MESSAGE_MAPPING_STEP_NAME, m -> m.xpathForMappingSteps(MappingType.MESSAGE_MAPPING));
		nameableToXpathFunctionMap.put(Nameable.XSLT_MAPPING_STEP_NAME, m -> m.xpathForMappingSteps(MappingType.XSLT_MAPPING));
		nameableToXpathFunctionMap.put(Nameable.OPERATION_MAPPING_STEP_NAME, m -> m.xpathForMappingSteps(MappingType.OPERATION_MAPPING));
		nameableToXpathFunctionMap.put(Nameable.SCRIPT_STEP_NAME, m -> m.xpathForFlowSteps(m.stepPredicateForScriptSteps()));
		nameableToXpathFunctionMap.put(Nameable.GROOVY_SCRIPT_STEP_NAME, m -> m.xpathForScriptSteps(ScriptingLanguage.GROOVY));
		nameableToXpathFunctionMap.put(Nameable.JS_SCRIPT_STEP_NAME, m -> m.xpathForScriptSteps(ScriptingLanguage.JAVASCRIPT));
		nameableToXpathFunctionMap.put(Nameable.SENDER_NAME, m -> m.xpathForSenderParticipants());
		nameableToXpathFunctionMap.put(Nameable.RECEIVER_NAME, m -> m.xpathForReceiverParticipants());
		nameableToXpathFunctionMap.put(Nameable.CONTENT_MODIFIER_STEP_NAME, m -> m.xpathForFlowSteps(m.stepPredicateForContentModifierSteps()));
		// Initialize the nameableToNameFunctionMap map.
		nameableToNameFunctionMap = new HashMap<>();
		nameableToNameFunctionMap.put(Nameable.CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.SENDER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.AMQP_SENDER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.ARIBA_SENDER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.AS2_SENDER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.AS4_SENDER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.FTP_SENDER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.HTTPS_SENDER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.IDOC_SENDER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.JMS_SENDER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.KAFKA_SENDER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.MAIL_SENDER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.ODATA_SENDER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.PROCESSDIRECT_SENDER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.SFTP_SENDER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.SOAP_SENDER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.SUCCESSFACTORS_SENDER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.XI_SENDER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.AMQP_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.ARIBA_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.AS2_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.AS4_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.ELSTER_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.FACEBOOK_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.FTP_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.ODATA_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.HTTP_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.IDOC_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.JDBC_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.JMS_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.KAFKA_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.LDAP_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.MAIL_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.ODC_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.OPENCONNECTORS_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.PROCESSDIRECT_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.RFC_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.SFTP_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.SOAP_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.SUCCESSFACTORS_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.TWITTER_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.XI_RECEIVER_CHANNEL_NAME, (n, m) -> m.getChannelNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.MAPPING_STEP_NAME, (n, m) -> m.getStepNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.MESSAGE_MAPPING_STEP_NAME, (n, m) -> m.getStepNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.XSLT_MAPPING_STEP_NAME, (n, m) -> m.getStepNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.OPERATION_MAPPING_STEP_NAME, (n, m) -> m.getStepNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.SCRIPT_STEP_NAME, (n, m) -> m.getStepNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.GROOVY_SCRIPT_STEP_NAME, (n, m) -> m.getStepNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.JS_SCRIPT_STEP_NAME, (n, m) -> m.getStepNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.SENDER_NAME, (n, m) -> m.getParticipantNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.RECEIVER_NAME, (n, m) -> m.getParticipantNameFromElement(n));
		nameableToNameFunctionMap.put(Nameable.CONTENT_MODIFIER_STEP_NAME, (n, m) -> m.getStepNameFromElement(n));
		// Initialize the nameableToIdentFunctionMap map.
		nameableToIdentFunctionMap = new HashMap<>();
		nameableToIdentFunctionMap.put(Nameable.CHANNEL_NAME, (n, m) -> String.format("channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.SENDER_CHANNEL_NAME, (n, m) -> String.format("sender channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.AMQP_SENDER_CHANNEL_NAME, (n, m) -> String.format("AMQP sender channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.ARIBA_SENDER_CHANNEL_NAME, (n, m) -> String.format("Ariba sender channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.AS2_SENDER_CHANNEL_NAME, (n, m) -> String.format("AS2 sender channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.AS4_SENDER_CHANNEL_NAME, (n, m) -> String.format("AS4 sender channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.FTP_SENDER_CHANNEL_NAME, (n, m) -> String.format("FTP sender channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.HTTPS_SENDER_CHANNEL_NAME, (n, m) -> String.format("HTTPS sender channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.IDOC_SENDER_CHANNEL_NAME, (n, m) -> String.format("IDoc sender channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.JMS_SENDER_CHANNEL_NAME, (n, m) -> String.format("JMS sender channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.KAFKA_SENDER_CHANNEL_NAME, (n, m) -> String.format("Kafka sender channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.MAIL_SENDER_CHANNEL_NAME, (n, m) -> String.format("Mail sender channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.ODATA_SENDER_CHANNEL_NAME, (n, m) -> String.format("OData sender channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.PROCESSDIRECT_SENDER_CHANNEL_NAME, (n, m) -> String.format("ProcessDirect sender channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.SFTP_SENDER_CHANNEL_NAME, (n, m) -> String.format("SFTP sender channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.SOAP_SENDER_CHANNEL_NAME, (n, m) -> String.format("SOAP sender channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.SUCCESSFACTORS_SENDER_CHANNEL_NAME, (n, m) -> String.format("SuccessFactors sender channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.XI_SENDER_CHANNEL_NAME, (n, m) -> String.format("XI sender channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.RECEIVER_CHANNEL_NAME, (n, m) -> String.format("receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.AMQP_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("AMQP receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.ARIBA_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("Ariba receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.AS2_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("AS2 receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.AS4_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("AS4 receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.ELSTER_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("Elster receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.FACEBOOK_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("Facebook receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.FTP_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("FTP receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.ODATA_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("OData receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.HTTP_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("HTTP receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.IDOC_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("IDoc receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.JDBC_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("JDBC receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.JMS_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("JMS receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.KAFKA_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("Kafka receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.LDAP_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("LDAP receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.MAIL_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("Mail receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.ODC_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("ODC receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.OPENCONNECTORS_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("OpenConnectors receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.PROCESSDIRECT_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("ProcessDirect receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.RFC_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("RFC receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.SFTP_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("SFTP receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.SOAP_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("SOAP receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.SUCCESSFACTORS_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("SuccessFactors receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.TWITTER_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("Twitter receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.XI_RECEIVER_CHANNEL_NAME, (n, m) -> String.format("XI receiver channel '%s' (ID '%s')", m.getChannelNameFromElement(n), m.getChannelIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.MAPPING_STEP_NAME, (n, m) -> String.format("mapping step '%s' (ID '%s')", m.getStepNameFromElement(n), m.getStepIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.MESSAGE_MAPPING_STEP_NAME, (n, m) -> String.format("message mapping step '%s' (ID '%s')", m.getStepNameFromElement(n), m.getStepIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.XSLT_MAPPING_STEP_NAME, (n, m) -> String.format("XSLT mapping step '%s' (ID '%s')", m.getStepNameFromElement(n), m.getStepIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.OPERATION_MAPPING_STEP_NAME, (n, m) -> String.format("operation mapping step '%s' (ID '%s')", m.getStepNameFromElement(n), m.getStepIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.SCRIPT_STEP_NAME, (n, m) -> String.format("script step '%s' (ID '%s')", m.getStepNameFromElement(n), m.getStepIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.GROOVY_SCRIPT_STEP_NAME, (n, m) -> String.format("Groovy script step '%s' (ID '%s')", m.getStepNameFromElement(n), m.getStepIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.JS_SCRIPT_STEP_NAME, (n, m) -> String.format("JavaScript script step '%s' (ID '%s')", m.getStepNameFromElement(n), m.getStepIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.SENDER_NAME, (n, m) -> String.format("Sender participant '%s' (ID '%s')", m.getParticipantNameFromElement(n), m.getParticipantIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.RECEIVER_NAME, (n, m) -> String.format("Receiver participant '%s' (ID '%s')", m.getParticipantNameFromElement(n), m.getParticipantIdFromElement(n)));
		nameableToIdentFunctionMap.put(Nameable.CONTENT_MODIFIER_STEP_NAME, (n, m) -> String.format("content modifier step '%s' (ID '%s')", m.getStepNameFromElement(n), m.getStepIdFromElement(n)));
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
				logger.debug("Checking {} name '{}'", n, iflowName);
				if (!scheme.test(iflowName)) {
					logger.debug("Name is not compliant ('{}')", message);
					consumer.consume(new NamingRuleIssue(tag, errorMessage("iflow name"), iflowName));
				}
				continue;
			}
			if (n == Nameable.IFLOW_ID) {
				String iflowId = tag.getId();
				logger.debug("Checking {} name '{}'", n, iflowId);
				if (!scheme.test(iflowId)) {
					logger.debug("Name is not compliant ('{}')", message);
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
				logger.debug("Checking {} name '{}'", n, name);
				if (!scheme.test(name)) {
					// This name does not follow the naming scheme.
					logger.debug("Name is not compliant ('{}')", message);
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
