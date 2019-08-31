package dk.mwittrock.cpilint.artifacts;

public final class IflowArtifactTag {
	
	private String id;
	private String name;
	
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
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IflowArtifactTag)) {
			return false;
		}
		IflowArtifactTag other = (IflowArtifactTag)o;
		return this.id.equals(other.id) && this.name.equals(other.name);
	}
	
	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + name.hashCode();
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("IflowArtifactTag[");
		sb.append("id=").append(id).append(',');
		sb.append("name=").append(name);
		sb.append(']');
		return sb.toString();
	}

}
