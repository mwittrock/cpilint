package org.cpilint.rules;

import org.cpilint.artifacts.IflowArtifact;
import org.cpilint.consumers.IssueConsumer;

public interface Rule {
	
	public void startTesting(IssueConsumer consumer);
	
	public void inspect(IflowArtifact iflow);
	
	public void endTesting();

}
