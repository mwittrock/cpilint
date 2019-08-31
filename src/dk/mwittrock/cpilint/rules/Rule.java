package dk.mwittrock.cpilint.rules;

import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.consumers.IssueConsumer;

public interface Rule {
	
	public void startTesting(IssueConsumer consumer);
	
	public void inspect(IflowArtifact iflow);
	
	public void endTesting();

}
