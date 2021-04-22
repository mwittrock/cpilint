package dk.mwittrock.cpilint.rules;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import dk.mwittrock.cpilint.IflowXml;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.issues.DisallowedSenderAdapterIssue;
import dk.mwittrock.cpilint.issues.Issue;
import dk.mwittrock.cpilint.model.SenderAdapter;
import dk.mwittrock.cpilint.model.XmlModel;
import dk.mwittrock.cpilint.model.XmlModelFactory;
import net.sf.saxon.s9api.XdmNode;

final class SenderAdaptersRule extends RuleBase {
	
	private final boolean allowed;
	private final Set<SenderAdapter> adapters;
	
	SenderAdaptersRule(boolean allowed, Set<SenderAdapter> adapters) {
		assert adapters != null;
		assert !adapters.isEmpty();
		this.allowed = allowed;
		this.adapters = new HashSet<>(adapters);
	}

	@Override
	public void inspect(IflowArtifact iflow) {
		IflowXml iflowXml = iflow.getIflowXml();
		XmlModel model = XmlModelFactory.getModelFor(iflowXml);
		IflowArtifactTag tag = iflow.getTag();
		// We are only checking for the sender adapters that are _not_ allowed.
		Set<SenderAdapter> disallowedAdapters = allowed ? SenderAdapter.allValuesExcept(adapters) : adapters;
		Function<SenderAdapter, String> xpathFunction = a -> model.xpathForSenderChannels(a);
		Function<SenderAdapter, Function<XdmNode, Issue>> issueFunctionFunction = a -> n -> new DisallowedSenderAdapterIssue(tag, model.getChannelNameFromElement(n), model.getChannelIdFromElement(n), a);
		XpathRulesUtil.iterateMultipleXpathsAndConsumeIssues(iflowXml, disallowedAdapters, xpathFunction, issueFunctionFunction, consumer::consume);
	}
	
}
