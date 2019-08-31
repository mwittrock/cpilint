package dk.mwittrock.cpilint.artifacts;

import java.util.Collection;

import dk.mwittrock.cpilint.IflowXml;

public interface IflowArtifact {
	
	public Collection<ArtifactResource> getResourcesByType(ArtifactResourceType type);
	
	public IflowXml getIflowXml();
	
	public IflowArtifactTag getTag();
	
}
