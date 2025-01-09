package org.cpilint.model;

import java.util.Set;

public enum ReceiverAdapter {
	
	ADVANCEDEVENTMESH("AdvancedEventMesh"),
	AMAZONDYNAMODB("AmazonDynamoDB"),
	AMAZONEVENTBRIDGE("AmazonEventBridge"),
	AMQP("AMQP"),
	ANAPLAN("Anaplan"),
	ARIBA("Ariba"),
	AS2("AS2"),
	AS4("AS4"),
	AZURECOSMOSDB("AzureCosmosDB"),
	AZURESTORAGE("AzureStorage"),
	COUPA("Coupa"),
	DROPBOX("Dropbox"),
	ELSTER("Elster"),
	FACEBOOK("Facebook"),
	FTP("FTP"),
	HUBSPOT("HubSpot"),
	ODATA("OData"),
	HTTP("HTTP"),
	IDOC("IDoc"),
	JDBC("JDBC"),
	JIRA("Jira"),
	JMS("JMS"),
	KAFKA("Kafka"),
	LDAP("LDAP"),
	MAIL("Mail"),
	MDI("MDI"),
	MICROSOFTSHAREPOINT("Microsoft SharePoint"),
	NETSUITE("NetSuite"),
	ODC("ODC"),
	OPENCONNECTORS("OpenConnectors"),
	PROCESSDIRECT("ProcessDirect"),
	RABBITMQ("RabbitMQ"),
	RFC("RFC"),
	SERVICENOW("ServiceNow"),
	SFTP("SFTP"),
	SLACK("Slack"),
	SMB("SMB"),
	SNOWFLAKE("Snowflake"),
	SOAP("SOAP"),
	SPLUNK("Splunk"),
	SUCCESSFACTORS("SuccessFactors"),
	SUGARCRM("SugarCRM"),
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
