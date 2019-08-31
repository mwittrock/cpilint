package dk.mwittrock.cpilint.suppliers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.ZipArchiveIflowArtifact;
import net.sf.saxon.s9api.SaxonApiException;

abstract class IteratorIflowArtifactSupplierBase implements IflowArtifactSupplier {
	
	protected Iterator<Path> iterator;
	private int artifactsSupplied = 0;

	@Override
	public void setup() {
		// Setup steps implemented by subclasses, if needed.
	}

	@Override
	public IflowArtifact supply() {
		if (!canSupply()) {
			throw new IllegalStateException("Cannot supply further iflow artifacts");
		}
		IflowArtifact iflow = null;
		try {
			iflow = ZipArchiveIflowArtifact.from(iterator.next());
		} catch (IOException | SaxonApiException e) {
			throw new IflowArtifactSupplierError("Error while processing iflow artifact", e);
		}
		artifactsSupplied++;
		return iflow;
	}

	@Override
	public boolean canSupply() {
		return iterator.hasNext();
	}

	@Override
	public void shutdown() {
		// Shutdown steps implemented by subclasses, if needed.
	}
	
	@Override
	public int artifactsSupplied() {
		return artifactsSupplied;
	}

}
