package dk.mwittrock.cpilint.rules;

import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.issues.IflowNameMatchesIssue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class IflowNameMatchesRule extends RuleBase {

	private Pattern namePattern;

	IflowNameMatchesRule(Pattern namePattern) {
		this.namePattern = namePattern;
	}

	@Override
	public void inspect(IflowArtifact iflow) {
		IflowArtifactTag tag = iflow.getTag();
		Matcher matcher = this.namePattern.matcher(tag.getName());
		if (!matcher.find()){
			consumer.consume(new IflowNameMatchesIssue(tag, namePattern.toString()));
		}
	}

}
