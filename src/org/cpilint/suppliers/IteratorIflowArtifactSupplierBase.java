package org.cpilint.suppliers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import org.cpilint.artifacts.IflowArtifact;
import org.cpilint.artifacts.ZipArchiveIflowArtifact;
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
		Path p = iterator.next();
		assert Files.exists(p);
		IflowArtifact iflow;
		try {
			// The current Path can be either a file or a directory and they are processed differently.
			if (Files.isRegularFile(p)) {
				iflow = ZipArchiveIflowArtifact.fromArchiveFile(p);
			} else if (Files.isDirectory(p)) {
				iflow = ZipArchiveIflowArtifact.fromDirectory(p);
			} else {
				// This should never happen.
				throw new AssertionError("Current Path is neither a file nor a directory: " + p.toString());
			}
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
