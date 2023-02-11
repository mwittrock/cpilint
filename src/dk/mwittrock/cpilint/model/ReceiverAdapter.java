package dk.mwittrock.cpilint.model;

import java.util.Set;

public enum ReceiverAdapter {
	
	AMQP("AMQP"),
	ARIBA("Ariba"),
	AS2("AS2"),
	AS4("AS4"),
	AZURESTORAGE("AzureStorage"),
	DROPBOX("Dropbox"),
	ELSTER("Elster"),
	FACEBOOK("Facebook"),
	FTP("FTP"),
	ODATA("OData"),
	HTTP("HTTP"),
	IDOC("IDoc"),
	JDBC("JDBC"),
	JMS("JMS"),
	KAFKA("Kafka"),
	LDAP("LDAP"),
	MAIL("Mail"),
	ODC("ODC"),
	OPENCONNECTORS("OpenConnectors"),
	PROCESSDIRECT("ProcessDirect"),
	RFC("RFC"),
	SFTP("SFTP"),
	SLACK("Slack"),
	SOAP("SOAP"),
	SPLUNK("Splunk"),
	SUCCESSFACTORS("SuccessFactors"),
	TWITTER("Twitter"),
	WORKDAY("Workday"),
	XI("XI");
	
	private final String name;
	
	ReceiverAdapter(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name + " receiver adapter";
	}
	
	public static Set<ReceiverAdapter> allValuesExcept(Set<ReceiverAdapter> receiverAdapters) {
		return ModelUtil.allValuesExcept(receiverAdapters);
	}

}
