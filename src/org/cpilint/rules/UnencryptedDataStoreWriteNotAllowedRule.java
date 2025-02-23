package org.cpilint.rules;

import java.util.function.Function;

import org.cpilint.IflowXml;
import org.cpilint.artifacts.IflowArtifact;
import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.issues.Issue;
import org.cpilint.issues.UnencryptedDataStoreWriteNotAllowedIssue;
import org.cpilint.model.DataStoreOperation;
import org.cpilint.model.XmlModel;
import org.cpilint.model.XmlModelFactory;
import net.sf.saxon.s9api.XdmNode;

final class UnencryptedDataStoreWriteNotAllowedRule extends RuleBase {
	
	@Override
	public void inspect(IflowArtifact iflow) {
		IflowXml iflowXml = iflow.getIflowXml();
		XmlModel model = XmlModelFactory.getModelFor(iflowXml);
		IflowArtifactTag tag = iflow.getTag();
		String unencryptedWriteXpath = model.xpathForFlowSteps(
			model.stepPredicateForDataStoreSteps(),
			model.stepPredicateForDataStoreOperation(DataStoreOperation.WRITE),
			model.stepPredicateForUnencryptedWrite());
		Function<XdmNode, Issue> issueFunction = n -> new UnencryptedDataStoreWriteNotAllowedIssue(ruleId, tag, model.getStepNameFromElement(n), model.getStepIdFromElement(n));
		XpathRulesUtil.iterateSingleXpathAndConsumeIssues(iflowXml, unencryptedWriteXpath, issueFunction, consumer::consume);
	}

}
