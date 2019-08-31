package dk.mwittrock.cpilint.artifacts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.transform.stream.StreamSource;

import dk.mwittrock.cpilint.IflowXml;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmMap;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public final class ZipArchiveIflowArtifact implements IflowArtifact {
	
	private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
	private static final String EXT_PARAMS_REPLACE_XSLT_PATH = "resources/xslt/ReplaceExternalParameters.xsl";
	private static final String NAME_MANIFEST_HEADER = "Bundle-Name";
	private static final String ID_MANIFEST_HEADER = "Bundle-SymbolicName";
	private static final String IFLOW_RESOURCES_BASE_PATH = "src/main/resources/";
	private static final String EXT_PARAMS_PATH = IFLOW_RESOURCES_BASE_PATH + "parameters.prop";
	private static final Map<ArtifactResourceType, Predicate<String>> typePredicates;
	
	static {
		typePredicates = new HashMap<>();
		typePredicates.put(ArtifactResourceType.GROOVY_SCRIPT, s -> s.startsWith(IFLOW_RESOURCES_BASE_PATH + "script/") && (s.endsWith(".groovy") || s.endsWith(".gsh")));
		typePredicates.put(ArtifactResourceType.JAVASCRIPT_SCRIPT, s -> s.startsWith(IFLOW_RESOURCES_BASE_PATH + "script/") && s.endsWith(".js"));
		typePredicates.put(ArtifactResourceType.XSD, s -> s.startsWith(IFLOW_RESOURCES_BASE_PATH + "xsd/") && s.endsWith(".xsd"));
		typePredicates.put(ArtifactResourceType.MESSAGE_MAPPING, s -> s.startsWith(IFLOW_RESOURCES_BASE_PATH + "mapping/") && s.endsWith(".mmap"));
		typePredicates.put(ArtifactResourceType.XSLT_MAPPING, s -> s.startsWith(IFLOW_RESOURCES_BASE_PATH + "mapping/") && (s.endsWith(".xsl") || s.endsWith(".xslt")));
		typePredicates.put(ArtifactResourceType.IFLOW, s -> s.startsWith(IFLOW_RESOURCES_BASE_PATH + "scenarioflows/integrationflow/") && s.endsWith(".iflw"));
		typePredicates.put(ArtifactResourceType.JAVA_ARCHIVE, s -> s.startsWith(IFLOW_RESOURCES_BASE_PATH + "lib/") && (s.endsWith(".jar") || s.endsWith(".zip")));
		typePredicates.put(ArtifactResourceType.WSDL, s -> s.startsWith(IFLOW_RESOURCES_BASE_PATH + "wsdl/") && (s.endsWith(".wsdl")));
		typePredicates.put(ArtifactResourceType.EDMX, s -> s.startsWith(IFLOW_RESOURCES_BASE_PATH + "edmx/") && (s.endsWith(".edmx")));
		typePredicates.put(ArtifactResourceType.OPERATION_MAPPING, s -> s.startsWith(IFLOW_RESOURCES_BASE_PATH + "mapping/") && s.endsWith(".opmap"));
	}
	
	private final IflowArtifactTag tag;
	private final IflowXml iflowXml;
	private final Map<ArtifactResourceType, Collection<ArtifactResource>> resources;
	
	private ZipArchiveIflowArtifact(IflowArtifactTag tag, Map<ArtifactResourceType, Collection<ArtifactResource>> resources, IflowXml iflowXml) {
		// Private, since instances are returned by the static factory methods.
		this.tag = tag;
		this.resources = resources;
		this.iflowXml = iflowXml;
	}

	@Override
	public Collection<ArtifactResource> getResourcesByType(ArtifactResourceType type) {
		/*
		 * The resources map is expected to contain a key for every artifact
		 * resource type. If this iflow artifact does not contain any resources of
		 * the specified type, the resources map will contain an empty collection
		 * for that key.
		 */
		if (!resources.containsKey(type)) {
			throw new IflowArtifactError("Artifact resource type not found");
		}
		return Collections.unmodifiableCollection(resources.get(type));
	}

	@Override
	public IflowXml getIflowXml() {
		return iflowXml;
	}
	
	@Override
	public IflowArtifactTag getTag() {
		return tag;
	}

	public static IflowArtifact from(Path p) throws IOException, SaxonApiException {
		return from(Files.newInputStream(p));
	}

	public static IflowArtifact from(InputStream is) throws IOException, SaxonApiException {
		// Extract all contents of the archive.
		Map<String, byte[]> contents = extractArchiveContents(is);
		// Extract the iflow's name and ID from the manifest.
		IflowArtifactTag tag = createTag(contents.get(MANIFEST_PATH));
		// Replace external parameters in the iflow XML, if this iflow artifact
		// actually contains an external parameters file (this is not always the case).
		if (externalParametersPresent(contents)) {
			replaceExternalParameters(contents);
		}
		// Create ArtifactResource objects for all resources.
		Map<ArtifactResourceType, Collection<ArtifactResource>> resources = createResourcesMap(tag, contents);
		// Get an IflowXml object.
		IflowXml iflowXml = createIflowXml(contents);
		// All done.
		return new ZipArchiveIflowArtifact(tag, resources, iflowXml);
	}

	private static boolean externalParametersPresent(Map<String, byte[]> contents) {
		return contents.containsKey(EXT_PARAMS_PATH);
	}

	private static void replaceExternalParameters(Map<String, byte[]> contents) throws IOException, SaxonApiException {
		String iflowXmlPath = getIflowXmlPath(contents.keySet());
		InputStream iflowXml = new ByteArrayInputStream(contents.get(iflowXmlPath));
		InputStream stylesheet = ZipArchiveIflowArtifact.class.getClassLoader().getResourceAsStream(EXT_PARAMS_REPLACE_XSLT_PATH);
		Map<String, String> parametersMap = getExternalParamsMap(contents);
		byte[] newIflowXml = transformIflowXml(stylesheet, iflowXml, parametersMap);
		contents.put(iflowXmlPath, newIflowXml);
	}

	private static String getIflowXmlPath(Set<String> allPaths) {
		List<String> iflowXmlPaths = allPaths
			.stream()
			.filter(typePredicates.get(ArtifactResourceType.IFLOW))
			.collect(Collectors.toList());
		// We expect exactly one iflow XML path.
		if (iflowXmlPaths.isEmpty() || iflowXmlPaths.size() > 1) {
			throw new IflowArtifactError("Unable to locate iflow XML in artifact");
		}
		return iflowXmlPaths.get(0);
	}
	
	private static byte[] transformIflowXml(InputStream stylesheet, InputStream iflowXml, Map<String, String> parametersMap) throws SaxonApiException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
		Processor proc = new Processor(false);
        XsltCompiler comp = proc.newXsltCompiler();
        XsltExecutable exe = comp.compile(new StreamSource(stylesheet));
        XsltTransformer xslt = exe.load();
        xslt.setSource(new StreamSource(iflowXml));
        xslt.setDestination(proc.newSerializer(out));
        XdmMap xdmMap = XdmMap.makeMap(parametersMap);
        xslt.setParameter(new QName("parameterMap"), xdmMap);
        xslt.transform();
        return out.toByteArray();
	}

	private static Map<String, String> getExternalParamsMap(Map<String, byte[]> contents) throws IOException {
		Properties props = new Properties();
		props.load(new ByteArrayInputStream(contents.get(EXT_PARAMS_PATH)));
		Map<String, String> parametersMap = new HashMap<>();
		props.forEach((k, v) -> parametersMap.put((String)k, (String)v));
		return Collections.unmodifiableMap(parametersMap);
	}
	
	private static Map<String, byte[]> extractArchiveContents(InputStream is) throws IOException {
		Map<String, byte[]> contents = new HashMap<>();
		try (ZipInputStream zis = new ZipInputStream(is)) {
			ZipEntry entry = null;
			while ((entry = zis.getNextEntry()) != null) {
				String path = entry.getName();
				byte[] bytes = zis.readAllBytes();
				contents.put(path, bytes);
			}
		}
		return contents;
	}
	
	private static String extractId(String manifestValue) {
		/*
		 *  Here are the two known formats of the iflow ID manifest value:
		 *  
		 *  HCITracker
		 *  HCITracker; singleton:=true
		 *  
		 *  If our assumptions about the value retrieved from the manifest
		 *  do not hold, an IflowArtifactError is thrown.
		 *  
		 *  TODO: Confirm the format of the manifest value.
		 */
		if (manifestValue == null || manifestValue.length() == 0) {
			throw new IflowArtifactError("Empty manifest value when trying to extract iflow ID");
		}
		String[] tokens = manifestValue.split(";");
		if (tokens.length < 1 || tokens.length > 2) {
			throw new IflowArtifactError("Unexpected manifest value format when trying to extract iflow ID");
		}
		return tokens[0];
	}
	
	private static IflowXml createIflowXml(Map<String, byte[]> contents) throws IOException {
		String iflowXmlPath = getIflowXmlPath(contents.keySet());
		return IflowXml.fromInputStream(new ByteArrayInputStream(contents.get(iflowXmlPath)));
	}

	private static IflowArtifactTag createTag(byte[] manifestContents) throws IOException {
		Manifest m = new Manifest(new ByteArrayInputStream(manifestContents));
		Attributes a = m.getMainAttributes();
		if (!a.containsKey(new Attributes.Name(NAME_MANIFEST_HEADER))) {
			throw new IflowArtifactError("Iflow manifest does not contain the expected name header");
		}
		if (!a.containsKey(new Attributes.Name(ID_MANIFEST_HEADER))) {
			throw new IflowArtifactError("Iflow manifest does not contain the expected ID header");
		}
		String id = extractId(a.getValue(ID_MANIFEST_HEADER));
		String name = a.getValue(NAME_MANIFEST_HEADER);
		return new IflowArtifactTag(id, name);
	}
	
	private static Map<ArtifactResourceType, Collection<ArtifactResource>> createResourcesMap(IflowArtifactTag tag, Map<String, byte[]> contents) {
		Map<ArtifactResourceType, Collection<ArtifactResource>> resourcesMap = new HashMap<>();
		for (ArtifactResourceType type : typePredicates.keySet()) {
			Collection<ArtifactResource> resources = contents.keySet()
				.stream()
				.filter(typePredicates.get(type))
				.map(p -> new ArtifactResource(tag, type, resourceNameFromResourcePath(p), contents.get(p)))
				.collect(Collectors.toList());
			resourcesMap.put(type, resources);
		}
		return resourcesMap;
	}

	private static String resourceNameFromResourcePath(String resourcePath) {
		// All resource paths have the same base path.
		if (!resourcePath.startsWith(IFLOW_RESOURCES_BASE_PATH)) {
			throw new IllegalArgumentException("Unexpected base path for resource");
		}
		// The resource name is everything following the last slash.
		int lastSlashIndex = resourcePath.lastIndexOf('/');
		// The path cannot end in a slash.
		if (lastSlashIndex == resourcePath.length() - 1) {
			throw new IllegalArgumentException("A resource path cannot end in a slash");
		}
		return resourcePath.substring(lastSlashIndex + 1);		
	}
	
}
