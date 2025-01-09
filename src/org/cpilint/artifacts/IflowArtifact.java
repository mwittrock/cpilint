package org.cpilint.artifacts;

import java.util.Collection;

import org.cpilint.IflowXml;

public interface IflowArtifact {
	
	public Collection<ArtifactResource> getResourcesByType(ArtifactResourceType type);
	
	public IflowXml getIflowXml();
	
	public IflowArtifactTag getTag();
	
}
