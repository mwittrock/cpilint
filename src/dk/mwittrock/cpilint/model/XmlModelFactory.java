package dk.mwittrock.cpilint.model;

import dk.mwittrock.cpilint.IflowXml;

public final class XmlModelFactory {
	
	private static XmlModel instance = new DefaultXmlModel();
	
	private XmlModelFactory() {
		throw new AssertionError("Never supposed to be instantiated");
	}
	
	public static XmlModel getModelFor(IflowXml iflowXml) {
		/*
		 * At the moment, the factory returns the same model regardless
		 * of the iflow XML. At a later date, it will return different
		 * models for the various iflow XML flavours, or throw an exception
		 * if a model could not be found.
		 */
		return instance;
	}

}
