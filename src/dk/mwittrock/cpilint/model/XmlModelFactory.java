package dk.mwittrock.cpilint.model;

import dk.mwittrock.cpilint.IflowXml;

public final class XmlModelFactory {
	
	private static XmlModel instance = new DefaultXmlModel();
	
	private XmlModelFactory() {
		throw new AssertionError("Never supposed to be instantiated");
	}
	
	public static XmlModel getModelFor(IflowXml iflowXml) {
		// At the moment, only one model exists, so the same model is always returned.
		return instance;
	}

}
