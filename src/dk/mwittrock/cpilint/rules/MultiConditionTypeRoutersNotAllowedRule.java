package dk.mwittrock.cpilint.rules;

import dk.mwittrock.cpilint.IflowXml;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.issues.MultiConditionTypeRoutersNotAllowedIssue;
import dk.mwittrock.cpilint.model.XmlModel;
import dk.mwittrock.cpilint.model.XmlModelFactory;
import net.sf.saxon.s9api.XdmNode;

final class MultiConditionTypeRoutersNotAllowedRule extends RuleBase {

	@Override
	public void inspect(IflowArtifact iflow) {
		IflowXml iflowXml = iflow.getIflowXml();
		XmlModel model = XmlModelFactory.getModelFor(iflowXml);
		IflowArtifactTag tag = iflow.getTag();
		String query = model.xqueryForMultiConditionTypeRouters();
		iflowXml.executeXquery(query)
			.stream()
			.map(XdmNode.class::cast)
			.map(n -> new MultiConditionTypeRoutersNotAllowedIssue(tag, model.getStepNameFromElement(n), model.getStepIdFromElement(n)))
			.forEach(consumer::consume);
	}

}
