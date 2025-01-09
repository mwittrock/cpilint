package org.cpilint.rules;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.cpilint.IflowXml;
import org.cpilint.artifacts.IflowArtifact;
import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.issues.DisallowedReceiverAdapterIssue;
import org.cpilint.issues.Issue;
import org.cpilint.model.ReceiverAdapter;
import org.cpilint.model.XmlModel;
import org.cpilint.model.XmlModelFactory;
import net.sf.saxon.s9api.XdmNode;

final class ReceiverAdaptersRule extends RuleBase {
	
	private final boolean allowed;
	private final Set<ReceiverAdapter> adapters;
	
	ReceiverAdaptersRule(boolean allowed, Set<ReceiverAdapter> adapters) {
		this.allowed = allowed;
		this.adapters = new HashSet<>(adapters);
	}

	@Override
	public void inspect(IflowArtifact iflow) {
		IflowXml iflowXml = iflow.getIflowXml();
		XmlModel model = XmlModelFactory.getModelFor(iflowXml);
		IflowArtifactTag tag = iflow.getTag();
		// We are only checking for the receiver adapters that are _not_ allowed.
		Set<ReceiverAdapter> disallowedAdapters = allowed ? ReceiverAdapter.allValuesExcept(adapters) : adapters;
		Function<ReceiverAdapter, String> xpathFunction = a -> model.xpathForReceiverChannels(a);
		Function<ReceiverAdapter, Function<XdmNode, Issue>> issueFunctionFunction = a -> n -> new DisallowedReceiverAdapterIssue(tag, a, model.getChannelNameFromElement(n), model.getChannelIdFromElement(n));
		XpathRulesUtil.iterateMultipleXpathsAndConsumeIssues(iflowXml, disallowedAdapters, xpathFunction, issueFunctionFunction, consumer::consume);
	}

}
