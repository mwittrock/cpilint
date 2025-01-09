package org.cpilint.rules;

import java.util.Set;
import java.util.function.Function;

import org.cpilint.IflowXml;
import org.cpilint.artifacts.IflowArtifact;
import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.issues.Issue;
import org.cpilint.issues.UnencryptedEndpointsNotAllowedIssue;
import org.cpilint.model.ReceiverAdapter;
import org.cpilint.model.XmlModel;
import org.cpilint.model.XmlModelFactory;
import net.sf.saxon.s9api.XdmNode;

final class UnencryptedEndpointsNotAllowedRule extends RuleBase {
	
	private static final Set<ReceiverAdapter> adaptersOfInterest = Set.of(
		ReceiverAdapter.AS4,
		ReceiverAdapter.ODATA,
		ReceiverAdapter.HTTP,
		ReceiverAdapter.IDOC,
		ReceiverAdapter.SOAP,
		ReceiverAdapter.XI,
		ReceiverAdapter.AS2,
		ReceiverAdapter.ANAPLAN,
		ReceiverAdapter.HUBSPOT,
		ReceiverAdapter.AZURECOSMOSDB,
		ReceiverAdapter.JIRA
	);
	
	@Override
	public void inspect(IflowArtifact iflow) {
		IflowXml iflowXml = iflow.getIflowXml();
		XmlModel model = XmlModelFactory.getModelFor(iflowXml);
		IflowArtifactTag tag = iflow.getTag();
		Function<ReceiverAdapter, String> xpathFunction = a -> model.xpathForReceiverChannels(a, model.channelPredicateForHttpEndpoints(a), XpathRulesUtil.negateXpathPredicate(model.channelPredicateForProxyTypeOnPremise(a)));
		Function<ReceiverAdapter, Function<XdmNode, Issue>> issueFunctionFunction = a -> n -> new UnencryptedEndpointsNotAllowedIssue(tag, a, model.getChannelNameFromElement(n), model.getChannelIdFromElement(n));
		XpathRulesUtil.iterateMultipleXpathsAndConsumeIssues(iflowXml, adaptersOfInterest, xpathFunction, issueFunctionFunction, consumer::consume);
	}

}
