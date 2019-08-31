package dk.mwittrock.cpilint.suppliers;

import dk.mwittrock.cpilint.artifacts.IflowArtifact;

public interface IflowArtifactSupplier {
	
	public void setup();
	
	public IflowArtifact supply();
	
	public boolean canSupply();

	public void shutdown();
	
	public int artifactsSupplied();

}
