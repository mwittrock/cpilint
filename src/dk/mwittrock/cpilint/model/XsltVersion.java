package dk.mwittrock.cpilint.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum XsltVersion {
	
	V10("1.0"),
	V20("2.0"),
	V30("3.0");
	
	/*
	 * Note that the enum values are initialized first, so it's not possible
	 * to refer to the static versionStrings map in the XsltVersion constructor
	 * (which would otherwise have made sense).
	 */
	
	private static final Map<String, XsltVersion> versionStrings = new HashMap<>();
	
	static {
		for (XsltVersion v : XsltVersion.values()) {
			versionStrings.put(v.getVersionString(), v);
		}
	}
	
	private String versionString;
	
	private XsltVersion(String versionString) {
		this.versionString = versionString;
	}
	
	public String getVersionString() {
		return versionString;
	}
	
	@Override
	public String toString() {
		return getVersionString();
	}
	
	public static boolean isKnownVersionString(String versionString) {
		return versionStrings.containsKey(versionString);
	}
	
	public static XsltVersion fromVersionString(String versionString) {
		if (!isKnownVersionString(versionString)) {
			throw new IllegalArgumentException("Unknown XSLT version string");
		}
		return versionStrings.get(versionString);
	}

	public static Set<XsltVersion> allValuesExcept(Set<XsltVersion> xsltVersions) {
		return ModelUtil.allValuesExcept(xsltVersions);
	}
	
}
