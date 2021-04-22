package dk.mwittrock.cpilint.rules;

import java.util.HashMap;
import java.util.Map;

import dk.mwittrock.cpilint.model.ReceiverAdapter;

public final class ReceiverAdaptersRuleFactory extends AllowDisallowRuleFactoryBase<ReceiverAdapter, ReceiverAdaptersRule> {

	private static final Map<String, ReceiverAdapter> receiverAdapters;
	
	static {
		receiverAdapters = new HashMap<>();
		receiverAdapters.put("amqp", ReceiverAdapter.AMQP);
		receiverAdapters.put("ariba", ReceiverAdapter.ARIBA);
		receiverAdapters.put("as2", ReceiverAdapter.AS2);
		receiverAdapters.put("as4", ReceiverAdapter.AS4);
		receiverAdapters.put("elster", ReceiverAdapter.ELSTER);
		receiverAdapters.put("facebook", ReceiverAdapter.FACEBOOK);
		receiverAdapters.put("ftp", ReceiverAdapter.FTP);
		receiverAdapters.put("odata", ReceiverAdapter.ODATA);
		receiverAdapters.put("http", ReceiverAdapter.HTTP);
		receiverAdapters.put("idoc", ReceiverAdapter.IDOC);
		receiverAdapters.put("jdbc", ReceiverAdapter.JDBC);
		receiverAdapters.put("jms", ReceiverAdapter.JMS);
		receiverAdapters.put("kafka", ReceiverAdapter.KAFKA);
		receiverAdapters.put("ldap", ReceiverAdapter.LDAP);
		receiverAdapters.put("mail", ReceiverAdapter.MAIL);
		receiverAdapters.put("odc", ReceiverAdapter.ODC);
		receiverAdapters.put("openconnectors", ReceiverAdapter.OPENCONNECTORS);
		receiverAdapters.put("processdirect", ReceiverAdapter.PROCESSDIRECT);
		receiverAdapters.put("rfc", ReceiverAdapter.RFC);
		receiverAdapters.put("sftp", ReceiverAdapter.SFTP);
		receiverAdapters.put("soap", ReceiverAdapter.SOAP);
		receiverAdapters.put("successfactors", ReceiverAdapter.SUCCESSFACTORS);
		receiverAdapters.put("twitter", ReceiverAdapter.TWITTER);
		receiverAdapters.put("xi", ReceiverAdapter.XI);
	}
	
	public ReceiverAdaptersRuleFactory() {
		super(
			"allowed-receiver-adapters",
			"disallowed-receiver-adapters",
			receiverAdapters,
			(a, r) -> new ReceiverAdaptersRule(a, r)
		);
	}
	
}
