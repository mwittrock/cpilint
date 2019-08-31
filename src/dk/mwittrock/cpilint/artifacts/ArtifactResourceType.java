package dk.mwittrock.cpilint.artifacts;

public enum ArtifactResourceType {
	
	MESSAGE_MAPPING("message mapping"),
	XSLT_MAPPING("XSLT mapping"),
	OPERATION_MAPPING("operation mapping"),
	JAVASCRIPT_SCRIPT("JavaScript script"),
	GROOVY_SCRIPT("Groovy script"),
	JAVA_ARCHIVE("Java archive"),
	IFLOW("integration flow"),
	EDMX("EDMX"),
	WSDL("WSDL"),
	XSD("XML Schema");

	private final String name;
	
	private ArtifactResourceType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
