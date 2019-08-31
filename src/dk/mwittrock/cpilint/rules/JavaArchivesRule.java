package dk.mwittrock.cpilint.rules;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.mwittrock.cpilint.artifacts.ArtifactResource;
import dk.mwittrock.cpilint.artifacts.ArtifactResourceType;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.issues.DisallowedJavaArchiveIssue;

final class JavaArchivesRule extends RuleBase {

	private boolean allowed;
	private Set<Pattern> globPatterns;

	JavaArchivesRule(boolean allowed, Set<Pattern> globPatterns) {
		this.allowed = allowed;
		this.globPatterns = new HashSet<>(globPatterns);
	}

    @Override
    public void inspect(IflowArtifact iflow) {
        for (ArtifactResource r : iflow.getResourcesByType(ArtifactResourceType.JAVA_ARCHIVE)) {
        	String archiveName = r.getName();
        	/*
        	 * The following logic probably needs some explaining. ^ is the logical xor
        	 * operator. If globPatterns is an allow list (i.e. allowed == true) and anyMatch
        	 * is false, an issue must be created. If globPatterns is a deny list (i.e. allowed
        	 * == false) and anyMatch is true, an issue must be created. In other words, an
        	 * issue must be created if allowed xor anyMatch is true.
        	 */
        	if (allowed ^ globPatterns.stream().map(p -> p.matcher(archiveName)).anyMatch(Matcher::matches)) {
        		consumer.consume(DisallowedJavaArchiveIssue.fromResource(r));
        	}
        }
    }
	
}
