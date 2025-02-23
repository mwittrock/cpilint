package org.cpilint.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cpilint.IflowXml;
import org.cpilint.artifacts.IflowArtifact;
import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.issues.MatchingProcessDirectChannelsRequiredIssue;
import org.cpilint.model.XmlModel;
import org.cpilint.model.XmlModelFactory;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

final class MatchingProcessDirectChannelsRequiredRule extends RuleBase {
	
	private static final Logger logger = LoggerFactory.getLogger(MatchingProcessDirectChannelsRequiredRule.class);
	private static final Pattern simpleExpressionPattern = Pattern.compile("\\$\\{.+?\\}");

	private Collection<MatchingProcessDirectChannelsRequiredIssue> issues = new ArrayList<>();
	private Set<String> senderChannelAddresses = new HashSet<>();

	@Override
	public void inspect(IflowArtifact iflow) {
		/*
		 * Create an issue for each ProcessDirect receiver channel, and store
		 * each sender ProcessDirect sender channel address.
		 */
		createIssues(iflow);
		storeChannelAddresses(iflow);
	}

	@Override
	public void endTesting() {
		/*
		 * Remove all issues for which the sender channel address exists. The
		 * remaining issues are then passed to the IssueConsumer.
		 */
		issues
			.stream()
			.filter(i -> !senderChannelAddresses.contains(i.getAddress()))
			.forEach(consumer::consume);
	}
	
	private void createIssues(IflowArtifact iflow) {
		IflowXml iflowXml = iflow.getIflowXml();
		XmlModel model = XmlModelFactory.getModelFor(iflowXml);
		IflowArtifactTag tag = iflow.getTag();
		String xquery = model.xqueryForProcessDirectReceiverChannels();
		XdmValue result = iflowXml.executeXquery(xquery);
		/*
		 * The returned sequence must either be empty, or the number of
		 * elements must be a multiple of three (since the sequence
		 * consists of tuples of channel name, channel id and ProcessDirect
		 * address).
		 */
		if (!(result.size() == 0 || result.size() % 3 == 0)) {
			throw new RuleError(String.format("Unexpected size (%d) of sequence returned by XQuery query", result.size()));
		}
		/*
		 * Process the sequence elements three at a time, creating an issue
		 * for each tuple.
		 */
		Iterator<XdmItem> itemIterator = result.iterator();
		while (itemIterator.hasNext()) {
			String channelName = itemIterator.next().getStringValue();
			String channelId = itemIterator.next().getStringValue();
			String address = itemIterator.next().getStringValue();
			/*
			 * If the address contains a Simple expression (i.e. it is configured
			 * dynamically at runtime), skip the channel and log that fact.
			 */
			if (containsSimpleExpression(address)) {
				logger.debug("Skipping channel '{}' (ID '{}') because it is dynamically configured", channelName, channelId);
				continue;
			}
			issues.add(new MatchingProcessDirectChannelsRequiredIssue(ruleId, tag, channelName, channelId, address));
		}
	}
	
	private void storeChannelAddresses(IflowArtifact iflow) {
		IflowXml iflowXml = iflow.getIflowXml();
		XmlModel model = XmlModelFactory.getModelFor(iflowXml);
		String xquery = model.xqueryForProcessDirectSenderChannelAddresses();
		/*
		 * The query returns a sequence of strings, each one a ProcessDirect
		 * sender channel address. Add each address to the Set of addresses.
		 */
		senderChannelAddresses.addAll(iflowXml.executeXquery(xquery)
			.stream()
			.map(XdmItem::getStringValue)
			.collect(Collectors.toSet()));
	}
	
	private static boolean containsSimpleExpression(String s) {
		Matcher m = simpleExpressionPattern.matcher(s);
		return m.find();
	}

}
