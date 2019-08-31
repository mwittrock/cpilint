package dk.mwittrock.cpilint.rules;

import org.dom4j.Element;

public interface RuleFactory {
	
	public boolean canCreateFrom(Element e);
	
	public Rule createFrom(Element e);

}
