package dk.mwittrock.cpilint.issues;

import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.model.XsltVersion;

public final class DisallowedXsltVersionIssue extends ArtifactIssueBase {
	
	private final String stylesheetName;
	private final XsltVersion version;
	
	public DisallowedXsltVersionIssue(IflowArtifactTag tag, String stylesheetName, XsltVersion version) {
		super(tag, String.format(
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
