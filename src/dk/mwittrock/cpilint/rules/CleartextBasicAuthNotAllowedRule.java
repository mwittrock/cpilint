package dk.mwittrock.cpilint.rules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import dk.mwittrock.cpilint.IflowXml;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.issues.CleartextBasicAuthNotAllowedIssue;
import dk.mwittrock.cpilint.model.ReceiverAdapter;
import dk.mwittrock.cpilint.model.XmlModel;
import dk.mwittrock.cpilint.model.XmlModelFactory;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

final class CleartextBasicAuthNotAllowedRule extends RuleBase {
	
	private static final Map<String, ReceiverAdapter> componentTypeToReceiverAdapterMap;
	
	private String xquery;
	
	static {
		// TODO: This really ought to be in the model rather than here.
		componentTypeToReceiverAdapterMap = new HashMap<>();
		componentTypeToReceiverAdapterMap.put("HTTP", ReceiverAdapter.HTTP);
		componentTypeToReceiverAdapterMap.put("HCIOData", ReceiverAdapter.ODATA);
		componentTypeToReceiverAdapterMap.put("IDOC", ReceiverAdapter.IDOC);
		componentTypeToReceiverAdapterMap.put("XI", ReceiverAdapter.XI);
		componentTypeToReceiverAdapterMap.put("SOAP", ReceiverAdapter.SOAP);
		componentTypeToReceiverAdapterMap.put("AS2", ReceiverAdapter.AS2);
		componentTypeToReceiverAdapterMap.put("AS4", ReceiverAdapter.AS4);
	}
	
	@Override
	public void inspect(IflowArtifact iflow) {
		IflowXml iflowXml = iflow.getIflowXml();
		XmlModel model = XmlModelFactory.getModelFor(iflowXml);
		IflowArtifactTag tag = iflow.getTag();
		/*
		 *  Only load the XQuery content once. Since only one version of the
		 *  XQuery exists, this optimization is okay.
		 */
		if (xquery == null) {
			xquery = model.xqueryForCleartextBasicAuthReceiverChannels();
		}
		XdmValue result = iflowXml.executeXquery(xquery);
		/*
		 * The returned sequence must either be empty, or the number of
		 * elements must be a multiple of three (since the sequence
		 * consists of tuples of channel name, channel id and component
		 * type).
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
			String componentType = itemIterator.next().getStringValue();
			assert componentTypeToReceiverAdapterMap.containsKey(componentType);
			ReceiverAdapter adapter = componentTypeToReceiverAdapterMap.get(componentType);
			consumer.consume(new CleartextBasicAuthNotAllowedIssue(tag, adapter, channelName, channelId));
		}

	}

}
