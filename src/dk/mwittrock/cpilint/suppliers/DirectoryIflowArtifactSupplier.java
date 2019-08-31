package dk.mwittrock.cpilint.suppliers;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DirectoryIflowArtifactSupplier extends IteratorIflowArtifactSupplierBase {
	
	private static final String IFLOW_ARCHIVE_GLOB = "*.zip";
	
	private Path dir;
	private DirectoryStream<Path> dirStream;
	
	public DirectoryIflowArtifactSupplier(Path dir) {
		if (!Files.exists(dir)) {
			throw new IllegalArgumentException("Provided directory does not exist");
		}
		if (!Files.isDirectory(dir)) {
			throw new IllegalArgumentException("Provided directory is not, in fact, a directory");
		}
		this.dir = dir;
	}

	@Override
	public void setup() {
		try {
			/*
			 * In order to be able to close it, we need to keep a reference
			 * to the DirectoryStream.
			 */
			dirStream = Files.newDirectoryStream(dir, IFLOW_ARCHIVE_GLOB);
		} catch (IOException e) {
			throw new IflowArtifactSupplierError("I/O error accessing iflow artifacts in directory", e);
		}
		iterator = dirStream.iterator();
	}

	@Override
	public boolean canSupply() {
		if (iterator == null) {
			throw new IllegalStateException("Iterator not available"); 
		}
		boolean canSupply = super.canSupply();
		if (!canSupply) {
			// We're done with the DirectoryStream.
			if (dirStream != null) {
				try {
					dirStream.close();
				} catch (IOException e) {
					throw new IflowArtifactSupplierError("I/O error closing DirectoryStream", e);
				}
			}
		}
		return canSupply;
	}

}
