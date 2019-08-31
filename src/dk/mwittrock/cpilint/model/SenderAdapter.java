package dk.mwittrock.cpilint.model;

import java.util.Set;

public enum SenderAdapter {
	
	ARIBA("Ariba"),
	HTTPS("HTTPS"),
	IDOC("IDoc"),
	MAIL("Mail"),
	ODATA("OData"),
	PROCESSDIRECT("ProcessDirect"),
	SFTP("SFTP"),
	SOAP("SOAP"),
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
