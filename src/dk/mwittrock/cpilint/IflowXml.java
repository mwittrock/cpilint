package dk.mwittrock.cpilint;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

public final class IflowXml {

	private static final Map<String, String> namespacePrefixes = Map.of(
		"bpmn2", "http://www.omg.org/spec/BPMN/20100524/MODEL",
		"ifl", "http:///com.sap.ifl.model/Ifl.xsd"
	); 
	
	private byte[] rawDocument;
	private XdmNode docRoot;
	private XPathCompiler xpathCompiler;
	private XQueryCompiler xqueryCompiler;
	
	private IflowXml(byte[] rawDocument, XdmNode docRoot, XPathCompiler xpathCompiler, XQueryCompiler xqueryCompiler) {
		this.rawDocument = rawDocument;
		this.docRoot = docRoot;
		this.xpathCompiler = xpathCompiler;
		this.xqueryCompiler = xqueryCompiler;
	}
	
	public InputStream getRawDocument() {
		return new ByteArrayInputStream(rawDocument);
	}

	public XdmValue evaluateXpath(String xpath) {
		XdmValue value;
		try {
			value = xpathCompiler.evaluate(xpath, docRoot);
		} catch (SaxonApiException e) {
			throw new IflowXmlError("Error while processing iflow XML: " + e.getMessage(), e);
		}
		return value;		
	}
	
	public XdmValue executeXquery(String query) {
		XdmValue result;
		try {
			XQueryExecutable exe = xqueryCompiler.compile(query);
			XQueryEvaluator eval = exe.load();
			eval.setSource(docRoot.asSource());
			result = eval.evaluate();
		} catch (SaxonApiException e) {
			throw new IflowXmlError("Error executing XQuery query", e);
		}
		return result;
	}
	
	public static IflowXml fromInputStream(InputStream is) {
		// Read the document into a byte array.
		byte[] rawDocument;
		try {
			rawDocument = is.readAllBytes();
		} catch (IOException e) {
			throw new IflowXmlError("I/O error when reading iflow XML document", e);
		}
		// Parse the document.
		Processor p = new Processor(false);
		XdmNode docRoot;
		try {
			docRoot = p.newDocumentBuilder().build(new StreamSource(new ByteArrayInputStream(rawDocument)));
		} catch (SaxonApiException e) {
			throw new IflowXmlError("Error while processing iflow XML", e);
		}
		XPathCompiler xpathCompiler = p.newXPathCompiler();
		namespacePrefixes.forEach((prefix, ns) -> xpathCompiler.declareNamespace(prefix, ns));
		/*
		 *  To use an XdmNode (the document node, specifically) as a source in
		 *  XQuery evaluation, the node and the XQueryCompiler must originate
		 *  from the same Processor object. Otherwise the evaluation will fail
		 *  at runtime. 
		 */
		return new IflowXml(rawDocument, docRoot, xpathCompiler, p.newXQueryCompiler());
	}
	
}
