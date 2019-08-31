package dk.mwittrock.cpilint.rules;

import dk.mwittrock.cpilint.consumers.IssueConsumer;

abstract class RuleBase implements Rule {
	
	protected IssueConsumer consumer;

	@Override
	public void startTesting(IssueConsumer consumer) {
		this.consumer = consumer;
	}

	@Override
	public void endTesting() {
		// Any required action is taken in subclasses.
	}

}
