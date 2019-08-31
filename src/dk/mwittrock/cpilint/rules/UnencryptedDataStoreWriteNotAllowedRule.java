package dk.mwittrock.cpilint.rules;

import java.util.function.Function;

import dk.mwittrock.cpilint.IflowXml;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.issues.Issue;
import dk.mwittrock.cpilint.issues.UnencryptedDataStoreWriteNotAllowedIssue;
import dk.mwittrock.cpilint.model.DataStoreOperation;
import dk.mwittrock.cpilint.model.XmlModel;
import dk.mwittrock.cpilint.model.XmlModelFactory;
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
		Function<XdmNode, Issue> issueFunction = n -> new UnencryptedDataStoreWriteNotAllowedIssue(tag, model.getStepNameFromElement(n), model.getStepIdFromElement(n));
		XpathRulesUtil.iterateSingleXpathAndConsumeIssues(iflowXml, unencryptedWriteXpath, issueFunction, consumer::consume);
	}

}
