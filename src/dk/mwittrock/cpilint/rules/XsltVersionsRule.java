package dk.mwittrock.cpilint.rules;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import dk.mwittrock.cpilint.artifacts.ArtifactResourceType;
import dk.mwittrock.cpilint.artifacts.ArtifactResource;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.issues.DisallowedXsltVersionIssue;
import dk.mwittrock.cpilint.model.XsltVersion;

final class XsltVersionsRule extends RuleBase {
	
	private final boolean allowed;
	private final Set<XsltVersion> xsltVersions;
	
	XsltVersionsRule(boolean allowed, Set<XsltVersion> xsltVersions) {
		this.allowed = allowed;
		this.xsltVersions = new HashSet<>(xsltVersions);
	}

	@Override
	public void inspect(IflowArtifact iflow) {
		// We'll check each stylesheet against the allowed XSLT versions.
		Set<XsltVersion> allowedVersions = allowed ? xsltVersions : XsltVersion.allValuesExcept(xsltVersions);
		for (ArtifactResource xsltResource : iflow.getResourcesByType(ArtifactResourceType.XSLT_MAPPING)) {
			String versionString = getXsltVersionString(xsltResource.getContents());
			if (!XsltVersion.isKnownVersionString(versionString)) {
				throw new RuleError(String.format("Unexpected XSLT version '%s' in stylesheet '%s'", versionString, xsltResource.getName()));
			}
			XsltVersion version = XsltVersion.fromVersionString(versionString);
			if (!allowedVersions.contains(version)) {
				consumer.consume(new DisallowedXsltVersionIssue(xsltResource.getTag(), xsltResource.getName(), version));
			}
		}
	}
	
	private static String getXsltVersionString(InputStream stylesheet) {
		/*
		 * The stylesheet's root element is either <xsl:stylesheet> or
		 * <xsl:transform> (they are synonymous). The root element has a
		 * mandatory "version" attribue, which contains the version number.
		 */
        SAXReader reader = new SAXReader();
        Document document;
        try {
			document = reader.read(stylesheet);
		} catch (DocumentException e) {
			throw new RuleError("Exception parsing stylesheet", e);
		}
        Element root = document.getRootElement();
        Attribute versionAttribute = root.attribute("version");
        if (versionAttribute == null) {
        	throw new RuleError("Stylesheet root element does not have a version attribute");
        }
        return versionAttribute.getValue();
	}

}
