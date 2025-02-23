package org.cpilint.issues;

import java.util.Optional;

import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.model.XsltVersion;

public final class DisallowedXsltVersionIssue extends IssueBase {
	
	private final String stylesheetName;
	private final XsltVersion version;
	
	public DisallowedXsltVersionIssue(Optional<String> ruleId, IflowArtifactTag tag, String stylesheetName, XsltVersion version) {
		super(ruleId, tag, String.format(
			"The XSLT stylesheet '%s' is written in XSLT version %s, which is not allowed.",
			stylesheetName,
			version.getVersionString()));
		this.stylesheetName = stylesheetName;
		this.version = version;
	}

	public String getStylesheetName() {
		return stylesheetName;
	}
	
	public XsltVersion getVersion() {
		return version;
	}

}
