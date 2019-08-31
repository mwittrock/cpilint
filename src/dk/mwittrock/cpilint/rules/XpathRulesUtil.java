package dk.mwittrock.cpilint.rules;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import dk.mwittrock.cpilint.IflowXml;
import dk.mwittrock.cpilint.issues.Issue;
import net.sf.saxon.s9api.XdmNode;

final class XpathRulesUtil {
	
	private XpathRulesUtil() {
		throw new AssertionError("Never supposed to be instantiated");
	}
	
	static void iterateSingleXpathAndConsumeIssues(IflowXml iflowXml, String xpath, Function<XdmNode, Issue> issueFunction, Consumer<Issue> issueConsumer) {
		iflowXml.evaluateXpath(xpath)
			.stream()
			.map(XdmNode.class::cast)
			.map(issueFunction)
			.forEach(issueConsumer);
	}
	
	static <T> void iterateMultipleXpathsAndConsumeIssues(IflowXml iflowXml, Set<T> tSet, Function<T, String> xpathFunction, Function<T, Function<XdmNode, Issue>> issueFunctionFunction, Consumer<Issue> issueConsumer) {
		for (T t : tSet) {
			String xpath = xpathFunction.apply(t);
			iterateSingleXpathAndConsumeIssues(iflowXml, xpath, issueFunctionFunction.apply(t), issueConsumer);
		}
	}

	static String negateXpathPredicate(String predicate) {
		/*
		 * Returns a new predicate containing the expression of the provided
		 * predicate wrapped in the not() function, thereby negating it.
		 */
	    return "[not(" + predicate.substring(1, predicate.length() - 1) + ")]";
	}

}
