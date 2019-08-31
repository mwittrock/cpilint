package dk.mwittrock.cpilint.rules;

import java.util.Set;
import java.util.function.Function;

import dk.mwittrock.cpilint.IflowXml;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.issues.CleartextBasicAuthNotAllowedIssue;
import dk.mwittrock.cpilint.issues.Issue;
import dk.mwittrock.cpilint.model.ReceiverAdapter;
import dk.mwittrock.cpilint.model.XmlModel;
import dk.mwittrock.cpilint.model.XmlModelFactory;
import net.sf.saxon.s9api.XdmNode;

final class CleartextBasicAuthNotAllowedRule extends RuleBase {
	
	private static final Set<ReceiverAdapter> adaptersOfInterest = Set.of(
		ReceiverAdapter.HTTP,
		ReceiverAdapter.ODATA,
		ReceiverAdapter.IDOC,
		ReceiverAdapter.SOAP,
		ReceiverAdapter.XI
	);
	
	@Override
	public void inspect(IflowArtifact iflow) {
		IflowXml iflowXml = iflow.getIflowXml();
		XmlModel model = XmlModelFactory.getModelFor(iflowXml);
		IflowArtifactTag tag = iflow.getTag();
		Function<ReceiverAdapter, String> xpathFunction = a -> model.xpathForReceiverChannels(a, model.channelPredicateForBasicAuthentication(a), model.channelPredicateForHttpEndpoints(a), XpathRulesUtil.negateXpathPredicate(model.channelPredicateForProxyTypeOnPremise(a)));
		Function<ReceiverAdapter, Function<XdmNode, Issue>> issueFunctionFunction = a -> n -> new CleartextBasicAuthNotAllowedIssue(tag, a, model.getChannelNameFromElement(n), model.getChannelIdFromElement(n)); 
		XpathRulesUtil.iterateMultipleXpathsAndConsumeIssues(iflowXml, adaptersOfInterest, xpathFunction, issueFunctionFunction, consumer::consume);
	}

}
