package dk.mwittrock.cpilint.suppliers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class FileIflowArtifactSupplier extends IteratorIflowArtifactSupplierBase {
	
	public FileIflowArtifactSupplier(Set<Path> iflowFiles) {
		if (!iflowFiles.stream().allMatch(p -> Files.exists(p))) {
			throw new IllegalArgumentException("All provided files must exist");
		}
		if (!iflowFiles.stream().allMatch(p -> Files.isRegularFile(p))) {
			throw new IllegalArgumentException("All provided files must, in fact, be files");
		}
		iterator = new HashSet<>(iflowFiles).iterator();
	}

}
