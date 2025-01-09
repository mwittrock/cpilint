package org.cpilint.model;

import org.cpilint.CpiLintError;

@SuppressWarnings("serial")
public final class XmlModelError extends CpiLintError {
	
	public XmlModelError(String message) {
		super(message);
	}
	
	public XmlModelError(String message, Throwable cause) {
		super(message, cause);
	}

}
