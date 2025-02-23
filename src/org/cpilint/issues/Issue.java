package org.cpilint.issues;

import java.util.Optional;
import java.util.Set;

import org.cpilint.artifacts.IflowArtifactTag;

public interface Issue {
	
	public Optional<String> getRuleId();
	public Set<IflowArtifactTag> getTags();
	public String getMessage();
	
}
