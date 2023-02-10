package dk.mwittrock.cpilint.model;

import java.util.Set;

public enum SenderAdapter {
	
	AMQP("AMQP"),
	ARIBA("Ariba"),
	AS2("AS2"),
	AS4("AS4"),
	AZURESTORAGE("AzureStorage"),
	DATASTORE("Data Store"),
	DROPBOX("Dropbox"),
	FTP("FTP"),
	HTTPS("HTTPS"),
	IDOC("IDoc"),
	JMS("JMS"),
	KAFKA("Kafka"),
	MAIL("Mail"),
	ODATA("OData"),
	PROCESSDIRECT("ProcessDirect"),
	SFTP("SFTP"),
	SLACK("Slack"),
	SOAP("SOAP"),
	SPLUNK("Splunk"),
	SUCCESSFACTORS("SuccessFactors"),
	XI("XI");
	
	private final String name;
	
	SenderAdapter(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name + " sender adapter";
	}

	public static Set<SenderAdapter> allValuesExcept(Set<SenderAdapter> senderAdapters) {
		return ModelUtil.allValuesExcept(senderAdapters);
	}

}
