package dk.mwittrock.cpilint.rules;

import java.util.HashMap;
import java.util.Map;

import dk.mwittrock.cpilint.model.SenderAdapter;

public final class SenderAdaptersRuleFactory extends AllowDisallowRuleFactoryBase<SenderAdapter, SenderAdaptersRule> {
	
	private static final Map<String, SenderAdapter> senderAdapters;
	
	static {
		senderAdapters = new HashMap<>();
		senderAdapters.put("ariba", SenderAdapter.ARIBA);
		senderAdapters.put("https", SenderAdapter.HTTPS);
		senderAdapters.put("idoc", SenderAdapter.IDOC);
		senderAdapters.put("mail", SenderAdapter.MAIL);
		senderAdapters.put("odata", SenderAdapter.ODATA);
		senderAdapters.put("processdirect", SenderAdapter.PROCESSDIRECT);
		senderAdapters.put("sftp", SenderAdapter.SFTP);
		senderAdapters.put("soap", SenderAdapter.SOAP);
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
