package org.cpilint.rules;

import java.util.function.Function;

import org.cpilint.IflowXml;
import org.cpilint.artifacts.IflowArtifact;
import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.issues.CsrfProtectionRequiredIssue;
import org.cpilint.issues.Issue;
import org.cpilint.model.SenderAdapter;
import org.cpilint.model.XmlModel;
import org.cpilint.model.XmlModelFactory;
import net.sf.saxon.s9api.XdmNode;

final class CsrfProtectionRequiredRule extends RuleBase {
	
	@Override
	public void inspect(IflowArtifact iflow) {
		IflowXml iflowXml = iflow.getIflowXml();
		XmlModel model = XmlModelFactory.getModelFor(iflowXml);
		IflowArtifactTag tag = iflow.getTag();
		String noCsrfChannelsXpath = model.xpathForSenderChannels(SenderAdapter.HTTPS, model.channelPredicateForNoCsrfProtection());
		Function<XdmNode, Issue> issueFunction = n -> new CsrfProtectionRequiredIssue(ruleId, tag, model.getChannelNameFromElement(n), model.getChannelIdFromElement(n));
		XpathRulesUtil.iterateSingleXpathAndConsumeIssues(iflowXml, noCsrfChannelsXpath, issueFunction, consumer::consume);
	}

}
