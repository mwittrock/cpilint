package org.cpilint.issues;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.cpilint.artifacts.IflowArtifactTag;
import org.cpilint.artifacts.PackageInfo;

abstract class IssueBase implements Issue {
	
	private final Optional<String> ruleId;
	private final Set<IflowArtifactTag> tags;
	private final String message;
	
	protected IssueBase(Optional<String> ruleId, IflowArtifactTag tag, String message) {
		this.ruleId = Objects.requireNonNull(ruleId, "ruleId must not be null");
		this.tags = Set.of(Objects.requireNonNull(tag, "tag must not be null"));
		this.message = Objects.requireNonNull(message, "message must not be null");
	}

	protected IssueBase(Optional<String> ruleId, Set<IflowArtifactTag> tags, String message) {
		// The set of tags must not be null and must not be empty.
		Objects.requireNonNull(tags, "tags must not be null");
		if (tags.isEmpty()) {
			throw new IllegalArgumentException("No iflow artifacts tags provided");
		}
		this.ruleId = Objects.requireNonNull(ruleId, "ruleId must not be null");
		this.tags = new HashSet<>(tags);
		this.message = Objects.requireNonNull(message, "message must not be null");
	}

	@Override
	public Optional<String> getRuleId() {
		return ruleId;
	}

	@Override
	public Set<IflowArtifactTag> getTags() {
		return tags;
	}

	@Override
	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		/*
		 * If this issue has a single source iflow, include that iflow's name,
		 * ID and package information (if available). If not, only include the
		 * message.
		 */
		if (tags.size() == 1) {
			IflowArtifactTag tag = tags.iterator().next();
			Optional<PackageInfo> pinfo = tag.getPackageInfo();
			sb.append("In iflow '%s' (ID '%s')".formatted(tag.getName(), tag.getId()));
			if (pinfo.isPresent()) {
				sb.append(" of package '%s' (ID '%s')".formatted(pinfo.get().name(), pinfo.get().id()));
			}
			sb.append(": ");
		}
		sb.append(message);
		return sb.toString();
	}

}
