package dk.mwittrock.cpilint.rules;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.mwittrock.cpilint.RulesFileError;

public final class JavaArchivesRuleFactory implements RuleFactory {
	
	private static final String ALLOW_ELEMENT_NAME = "allowed-java-archives";
	private static final String DISALLOW_ELEMENT_NAME = "disallowed-java-archives";
    private static final String ASTERISK_PATTERN = ".*";
    private static final String QUESTION_MARK_PATTERN = ".";

    private static final Logger logger = LoggerFactory.getLogger(JavaArchivesRuleFactory.class);
	
	@Override
	public boolean canCreateFrom(Element e) {
		String elementName = e.getName();
		return elementName.equals(ALLOW_ELEMENT_NAME) || elementName.equals(DISALLOW_ELEMENT_NAME);
	}

	@Override
	public Rule createFrom(Element e) {
		String ruleElementName = e.getName();
		if (!canCreateFrom(e)) {
			throw new RuleFactoryError(String.format("Cannot create Rule object from element '%s'", ruleElementName));
		}
		boolean allowed = ruleElementName.equals(ALLOW_ELEMENT_NAME);
		/*
		 * The allow rule element can be empty (meaning no archives at all are allowed). The
		 * disallow rule element must contain at least one disallow child element.
		 */
		String configElementName = allowed ? "allow" : "disallow";
		List<Element> configElements = e.elements(configElementName);
		if (!allowed && configElements.isEmpty()) {
			throw new RulesFileError(String.format("Element '%s' must contain at least one '%s' element", ruleElementName, configElementName));
		}
		/*
		 * Extract the globs and turn them into Pattern objects.
		 */
		Set<Pattern> globPatterns = configElements
			.stream()
			.map(Element::getText)
			.map(JavaArchivesRuleFactory::globToPattern)
			.collect(Collectors.toSet());
		return new JavaArchivesRule(allowed, globPatterns);
	}

    private static Pattern globToPattern(String glob) {
        /* 
         * Replace all consecutive asterisks with a single asterisk. This
         * makes for a more readable pattern, and the result is the same.
         */
        String canonicalGlob = glob.replaceAll("\\*+", "*");
        StringBuilder patternBuilder = new StringBuilder();
        StringBuilder literalBuilder = new StringBuilder();
        for (char c : canonicalGlob.toCharArray()) {
            if ((c == '*' || c == '?') && literalBuilder.length() > 0) {
                patternBuilder.append(Pattern.quote(literalBuilder.toString()));
                literalBuilder.setLength(0);
            }
            if (c == '*') {
                patternBuilder.append(ASTERISK_PATTERN);
            } else if (c == '?') {
                patternBuilder.append(QUESTION_MARK_PATTERN);
            } else {
                literalBuilder.append(c);
            }
        }
        if (literalBuilder.length() > 0) {
            patternBuilder.append(Pattern.quote(literalBuilder.toString()));
        }
        logger.debug("Generated pattern '{}' for glob '{}'", patternBuilder.toString(), glob);
        return Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE);
    }
	
}
