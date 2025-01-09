package org.cpilint.rules;

import org.cpilint.IflowXml;
import org.cpilint.artifacts.IflowArtifact;
import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.issues.MultiConditionTypeRoutersNotAllowedIssue;
import org.cpilint.model.XmlModel;
import org.cpilint.model.XmlModelFactory;
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
