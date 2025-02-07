package org.cpilint.artifacts;

import java.util.Objects;
import java.util.Optional;

public final class IflowArtifactTag {
	
	private String id;
	private String name;
	private Optional<PackageInfo> packageInfo = Optional.empty();
	
	public IflowArtifactTag(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public void setPackageInfo(PackageInfo packageInfo) {
		if (this.packageInfo.isPresent()) {
			throw new IllegalStateException("Package info already set");
		}
		this.packageInfo = Optional.of(Objects.requireNonNull(packageInfo, "packageInfo must not be null"));
	}

	public Optional<PackageInfo> getPackageInfo() {
		return packageInfo;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IflowArtifactTag)) {
			return false;
		}
		IflowArtifactTag other = (IflowArtifactTag)o;
		return this.id.equals(other.id)
			&& this.name.equals(other.name)
			&& this.packageInfo.equals(other.packageInfo);
	}
	
	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + packageInfo.hashCode();
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("IflowArtifactTag[");
		sb.append("id=").append(id).append(',');
		sb.append("name=").append(name).append(',');
		sb.append("packageinfo=").append(packageInfo.toString());
		sb.append(']');
		return sb.toString();
	}

}
