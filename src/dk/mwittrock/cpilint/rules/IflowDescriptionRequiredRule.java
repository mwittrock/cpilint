package dk.mwittrock.cpilint.rules;

import dk.mwittrock.cpilint.IflowXml;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.issues.IflowDescriptionRequiredIssue;
import dk.mwittrock.cpilint.model.XmlModel;
import dk.mwittrock.cpilint.model.XmlModelFactory;
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
			consumer.consume(new IflowDescriptionRequiredIssue(tag));
		}
	}

}
