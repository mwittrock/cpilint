package org.cpilint.suppliers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class UnpackedFileIflowArtifactSupplier extends IteratorIflowArtifactSupplierBase {

    public UnpackedFileIflowArtifactSupplier(Set<Path> iflowDirs) {
		if (iflowDirs.stream().anyMatch(Files::notExists)) {
			throw new IllegalArgumentException("All provided directories must exist");
		}
		if (!iflowDirs.stream().allMatch(Files::isDirectory)) {
			throw new IllegalArgumentException("All provided directories must be directories");
		}
		iterator = new HashSet<>(iflowDirs).iterator();        
    }

}
