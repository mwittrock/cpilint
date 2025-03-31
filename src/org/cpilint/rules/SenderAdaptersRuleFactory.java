package org.cpilint.rules;

import java.util.HashMap;
import java.util.Map;

import org.cpilint.model.SenderAdapter;

public final class SenderAdaptersRuleFactory extends AllowDisallowRuleFactoryBase<SenderAdapter, SenderAdaptersRule> {
	
	private static final Map<String, SenderAdapter> senderAdapters;
	
	static {
		senderAdapters = new HashMap<>();
		senderAdapters.put("advancedeventmesh", SenderAdapter.ADVANCEDEVENTMESH);
		senderAdapters.put("amqp", SenderAdapter.AMQP);
		senderAdapters.put("ariba", SenderAdapter.ARIBA);
		senderAdapters.put("as2", SenderAdapter.AS2);
		senderAdapters.put("as4", SenderAdapter.AS4);
		senderAdapters.put("azurestorage", SenderAdapter.AZURESTORAGE);
		senderAdapters.put("data store", SenderAdapter.DATASTORE);
		senderAdapters.put("dropbox", SenderAdapter.DROPBOX);
		senderAdapters.put("ftp", SenderAdapter.FTP);
		senderAdapters.put("https", SenderAdapter.HTTPS);
		senderAdapters.put("ibmmq", SenderAdapter.IBMMQ);
		senderAdapters.put("idoc", SenderAdapter.IDOC);
		senderAdapters.put("jms", SenderAdapter.JMS);
		senderAdapters.put("kafka", SenderAdapter.KAFKA);
		senderAdapters.put("mail", SenderAdapter.MAIL);
		senderAdapters.put("microsoft sharepoint", SenderAdapter.MICROSOFTSHAREPOINT);
		senderAdapters.put("odata", SenderAdapter.ODATA);
		senderAdapters.put("processdirect", SenderAdapter.PROCESSDIRECT);
		senderAdapters.put("rabbitmq", SenderAdapter.RABBITMQ);
		senderAdapters.put("sftp", SenderAdapter.SFTP);
		senderAdapters.put("slack", SenderAdapter.SLACK);
		senderAdapters.put("smb", SenderAdapter.SMB);
		senderAdapters.put("soap", SenderAdapter.SOAP);
		senderAdapters.put("splunk", SenderAdapter.SPLUNK);
		senderAdapters.put("successfactors", SenderAdapter.SUCCESSFACTORS);
		senderAdapters.put("xi", SenderAdapter.XI);
	}
	
	public SenderAdaptersRuleFactory() {
		super(
			"allowed-sender-adapters",
			"disallowed-sender-adapters",
			senderAdapters,
			(a, s) -> new SenderAdaptersRule(a,	s)
		);
	}

}
