package dk.mwittrock.cpilint.rules;

import java.util.function.Function;

import dk.mwittrock.cpilint.IflowXml;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.issues.CsrfProtectionRequiredIssue;
import dk.mwittrock.cpilint.issues.Issue;
import dk.mwittrock.cpilint.model.SenderAdapter;
import dk.mwittrock.cpilint.model.XmlModel;
import dk.mwittrock.cpilint.model.XmlModelFactory;
import net.sf.saxon.s9api.XdmNode;

final class CsrfProtectionRequiredRule extends RuleBase {
	
	@Override
	public void inspect(IflowArtifact iflow) {
		IflowXml iflowXml = iflow.getIflowXml();
		XmlModel model = XmlModelFactory.getModelFor(iflowXml);
		IflowArtifactTag tag = iflow.getTag();
		String noCsrfChannelsXpath = model.xpathForSenderChannels(SenderAdapter.HTTPS, model.channelPredicateForNoCsrfProtection());
		Function<XdmNode, Issue> issueFunction = n -> new CsrfProtectionRequiredIssue(tag, model.getChannelNameFromElement(n), model.getChannelIdFromElement(n));
		XpathRulesUtil.iterateSingleXpathAndConsumeIssues(iflowXml, noCsrfChannelsXpath, issueFunction, consumer::consume);
	}

}
