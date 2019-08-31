package dk.mwittrock.cpilint.artifacts;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public final class ArtifactResource {
	
	private IflowArtifactTag tag;
	private ArtifactResourceType type;
	private String name;
	private byte[] contents;
	
	public ArtifactResource(IflowArtifactTag tag, ArtifactResourceType type, String name, byte[] contents) {
		this.tag = tag;
		this.type = type;
		this.name = name;
		// Defensively copy the contents byte array into the instance array.
		this.contents = new byte[contents.length];
		System.arraycopy(contents, 0, this.contents, 0, contents.length);
	}
	
	public IflowArtifactTag getTag() {
		return tag;
	}
	
	public String getName() {
		return name;
	}
	
	public ArtifactResourceType getType() {
		return type;
	}
	
	public InputStream getContents() {
		return new ByteArrayInputStream(contents);
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ArtifactResource)) {
			return false;
		}
		ArtifactResource other = (ArtifactResource)o;
		return this.tag.equals(other.tag) && this.type == other.type && this.name.equals(other.name);
	}

	@Override
	public int hashCode() {
		int result = tag.hashCode();
		result = 31 * result + type.hashCode();
		result = 31 * result + name.hashCode();
		return result;
	}

}
