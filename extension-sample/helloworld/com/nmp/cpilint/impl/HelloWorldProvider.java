package com.nmp.cpilint.impl;
import dk.mwittrock.cpilint.rules.*;

import org.dom4j.Element;

public final class HelloWorldProvider implements RuleFactory {

	@Override
	public boolean canCreateFrom(Element e) {
		return e.getName().equals("hello-world-provider");
	}

	@Override
	public Rule createFrom(Element e) {
		if (!canCreateFrom(e)) {
			throw new RuleFactoryError(String.format("Cannot create Rule object from element '%s'", e.getName()));
		}
		return new HelloWorldRule();
	}

}
