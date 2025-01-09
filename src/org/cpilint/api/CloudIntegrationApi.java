package org.cpilint.api;

import java.util.Set;

import org.cpilint.artifacts.IflowArtifact;

public interface CloudIntegrationApi {
	
	public IflowArtifact getIflowArtifact(String iflowArtifactId);
	
	public Set<String> getEditableIntegrationPackageIds(boolean skipSapPackages);
	
	public Set<String> getIflowArtifactIdsFromPackage(String packageId, boolean skipDrafts);
	
}
