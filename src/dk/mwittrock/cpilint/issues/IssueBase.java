package dk.mwittrock.cpilint.issues;

abstract class IssueBase implements Issue {
	
	private final String message;
	
	protected IssueBase(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}
	
}
