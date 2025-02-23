package org.cpilint.rules;

import org.cpilint.IflowXml;
import org.cpilint.artifacts.IflowArtifact;
import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.issues.IflowDescriptionRequiredIssue;
import org.cpilint.model.XmlModel;
import org.cpilint.model.XmlModelFactory;
import net.sf.saxon.s9api.XdmValue;

final class IflowDescriptionRequiredRule extends RuleBase {

	@Override
	public void inspect(IflowArtifact iflow) {
		IflowXml iflowXml = iflow.getIflowXml();
		XmlModel model = XmlModelFactory.getModelFor(iflowXml);
		IflowArtifactTag tag = iflow.getTag();
		XdmValue descriptionNodes = iflowXml.evaluateXpath(model.xpathForIflowDescription());
		if (descriptionNodes.size() > 1) {
			throw new RuleError("Unable to locate iflow description");
		}
		if (descriptionNodes.size() == 0) {
			consumer.consume(new IflowDescriptionRequiredIssue(ruleId, tag));
		}
	}

}
