package dk.mwittrock.cpilint.rules;

import java.util.Set;
import java.util.function.Function;

import dk.mwittrock.cpilint.IflowXml;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.issues.ClientCertSenderChannelAuthNotAllowedIssue;
import dk.mwittrock.cpilint.issues.Issue;
import dk.mwittrock.cpilint.model.SenderAdapter;
import dk.mwittrock.cpilint.model.XmlModel;
import dk.mwittrock.cpilint.model.XmlModelFactory;
import net.sf.saxon.s9api.XdmNode;

final class ClientCertSenderChannelAuthNotAllowedRule extends RuleBase {
	
	private static final Set<SenderAdapter> adaptersOfInterest = Set.of(
			SenderAdapter.HTTPS,
			SenderAdapter.IDOC,
			SenderAdapter.ODATA,
			SenderAdapter.SOAP,
			SenderAdapter.XI,
			SenderAdapter.AS2,
			SenderAdapter.AS4
		);

	@Override
	public void inspect(IflowArtifact iflow) {
		IflowXml iflowXml = iflow.getIflowXml();
		XmlModel model = XmlModelFactory.getModelFor(iflowXml);
		IflowArtifactTag tag = iflow.getTag();
		Function<SenderAdapter, String> xpathFunction = a -> model.xpathForSenderChannels(a, model.channelPredicateForClientCertAuth(a));
		Function<SenderAdapter, Function<XdmNode, Issue>> issueFunctionFunction = a -> n -> new ClientCertSenderChannelAuthNotAllowedIssue(tag, a, model.getChannelNameFromElement(n), model.getChannelIdFromElement(n));
		XpathRulesUtil.iterateMultipleXpathsAndConsumeIssues(iflowXml, adaptersOfInterest, xpathFunction, issueFunctionFunction, consumer::consume);
	}

}
