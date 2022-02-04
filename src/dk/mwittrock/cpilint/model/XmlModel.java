package dk.mwittrock.cpilint.model;

import net.sf.saxon.s9api.XdmNode;

public interface XmlModel {
	
	// Channel related.
	
	public String xpathForChannels(String... predicates);
	
	public String channelPredicateForDirection(ChannelDirection direction);
	
	public String channelPredicateForAdapter(ReceiverAdapter receiverAdapter);
	
	public String channelPredicateForAdapter(SenderAdapter senderAdapter);
	
	public String channelPredicateForHttpEndpoints(ReceiverAdapter receiverAdapter);
	
	public String channelPredicateForNoCsrfProtection();
	
	public String channelPredicateForBasicAuthentication(ReceiverAdapter receiverAdapter);
	
	public String channelPredicateForClientCertAuth(SenderAdapter senderAdapter);
	
	public String channelPredicateForProxyTypeOnPremise(ReceiverAdapter receiverAdapter);
	
	public String getChannelNameFromElement(XdmNode node);
	
	public String getChannelIdFromElement(XdmNode node);
	
	public default String xpathForSenderChannels(SenderAdapter senderAdapter, String... predicates) {
		/*
		 * Create a new array containing the predicates for direction and
		 * adapter type. Note that the order of the predicates doesn't
		 * matter.
		 */
		String[] newPredicates = new String[predicates.length + 2];
		newPredicates[0] = channelPredicateForDirection(ChannelDirection.SENDER);
		newPredicates[1] = channelPredicateForAdapter(senderAdapter);
		System.arraycopy(predicates, 0, newPredicates, 2, predicates.length);
		return xpathForChannels(newPredicates);
	}

	public default String xpathForReceiverChannels(ReceiverAdapter receiverAdapter, String... predicates) {
		/*
		 * Create a new array containing the predicates for direction and
		 * adapter type. Note that the order of the predicates doesn't
		 * matter.
		 */
		String[] newPredicates = new String[predicates.length + 2];
		newPredicates[0] = channelPredicateForDirection(ChannelDirection.RECEIVER);
		newPredicates[1] = channelPredicateForAdapter(receiverAdapter);
		System.arraycopy(predicates, 0, newPredicates, 2, predicates.length);
		return xpathForChannels(newPredicates);
	}
	
	public String xqueryForProcessDirectReceiverChannels();
	
	public String xqueryForProcessDirectSenderChannelAddresses();
	
	public String xqueryForCleartextBasicAuthReceiverChannels();
	
	// Flow step related.
	
	public String xpathForFlowSteps(String... predicates);
	
	public String stepPredicateForMappingSteps();
	
	public String stepPredicateForMappingType(MappingType mappingType);
	
	public String getStepNameFromElement(XdmNode node);
	
	public String getStepIdFromElement(XdmNode node);
	
	public default String xpathForMappingSteps(MappingType mappingType) {
		return xpathForFlowSteps(
			stepPredicateForMappingSteps(),
			stepPredicateForMappingType(mappingType)
		);
	}
	
	public String stepPredicateForScriptSteps();

	public String stepPredicateForContentModifierSteps();
	
	public String stepPredicateForScriptingLanguage(ScriptingLanguage scriptingLanguage);
	
	public default String xpathForScriptSteps(ScriptingLanguage scriptingLanguage) {
		return xpathForFlowSteps(
			stepPredicateForScriptSteps(),
			stepPredicateForScriptingLanguage(scriptingLanguage)
		);
	}
	
	public String stepPredicateForDataStoreSteps();
	
	public String stepPredicateForDataStoreOperation(DataStoreOperation dataStoreOperation);
	
	public String stepPredicateForUnencryptedWrite();
	
	public String xqueryForMultiConditionTypeRouters();
	
	// Participant related.

	public String xpathForSenderParticipants();

	public String xpathForReceiverParticipants();

	public String getParticipantNameFromElement(XdmNode node);

	public String getParticipantIdFromElement(XdmNode node);

	// Other iflow content.
	
	public String xpathForIflowDescription();

}
