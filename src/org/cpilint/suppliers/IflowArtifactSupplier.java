package org.cpilint.suppliers;

import org.cpilint.artifacts.IflowArtifact;

public interface IflowArtifactSupplier {
	
	public void setup();
	
	public IflowArtifact supply();
	
	public boolean canSupply();

	public void shutdown();
	
	public int artifactsSupplied();

}
