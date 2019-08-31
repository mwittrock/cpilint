package dk.mwittrock.cpilint.rules;

import java.util.HashMap;
import java.util.Map;

import dk.mwittrock.cpilint.model.XsltVersion;

public final class XsltVersionsRuleFactory extends AllowDisallowRuleFactoryBase<XsltVersion, XsltVersionsRule> {

	private static final Map<String, XsltVersion> versionStrings;
	
	static {
		versionStrings = new HashMap<>();
		for (XsltVersion version : XsltVersion.values()) {
			versionStrings.put(version.getVersionString(), version);
		}
	}

	public XsltVersionsRuleFactory() {
		super(
			"allowed-xslt-versions",
			"disallowed-xslt-versions",
			versionStrings,
			(a, v) -> new XsltVersionsRule(a, v)
		);
	}
	
}
