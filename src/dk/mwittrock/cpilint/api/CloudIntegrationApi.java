package dk.mwittrock.cpilint.api;

import java.util.Set;

import dk.mwittrock.cpilint.artifacts.IflowArtifact;

public interface CloudIntegrationApi {
	
	public IflowArtifact getIflowArtifact(String iflowArtifactId);
	
	public Set<String> getEditableIntegrationPackageIds(boolean skipSapPackages);
	
	public Set<String> getIflowArtifactIdsFromPackage(String packageId, boolean skipDrafts);
	
}
