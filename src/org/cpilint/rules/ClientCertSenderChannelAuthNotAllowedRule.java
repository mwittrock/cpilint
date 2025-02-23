package org.cpilint.rules;

import java.util.Set;
import java.util.function.Function;

import org.cpilint.IflowXml;
import org.cpilint.artifacts.IflowArtifact;
import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.issues.ClientCertSenderChannelAuthNotAllowedIssue;
import org.cpilint.issues.Issue;
import org.cpilint.model.SenderAdapter;
import org.cpilint.model.XmlModel;
import org.cpilint.model.XmlModelFactory;
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
		Function<SenderAdapter, Function<XdmNode, Issue>> issueFunctionFunction = a -> n -> new ClientCertSenderChannelAuthNotAllowedIssue(ruleId, tag, a, model.getChannelNameFromElement(n), model.getChannelIdFromElement(n));
		XpathRulesUtil.iterateMultipleXpathsAndConsumeIssues(iflowXml, adaptersOfInterest, xpathFunction, issueFunctionFunction, consumer::consume);
	}

}
