package com.nmp.cpilint.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import dk.mwittrock.cpilint.rules.RuleBase;
import dk.mwittrock.cpilint.IflowXml;
import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.issues.CleartextBasicAuthNotAllowedIssue;
import dk.mwittrock.cpilint.model.ReceiverAdapter;
import dk.mwittrock.cpilint.model.XmlModel;
import dk.mwittrock.cpilint.model.XmlModelFactory;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

final class HelloWorldRule extends RuleBase {
	
	
	@Override
	public void inspect(IflowArtifact iflow) {
		System.out.println("hello");

	}

}
