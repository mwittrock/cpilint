package org.cpilint.rules;

import java.util.Objects;

import org.cpilint.consumers.IssueConsumer;

abstract class RuleBase implements Rule {
	
	protected IssueConsumer consumer;

	@Override
	public void startTesting(IssueConsumer consumer) {
		this.consumer = Objects.requireNonNull(consumer, "consumer must not be null");
	}

	@Override
	public void endTesting() {
		// Any required action is taken in subclasses.
	}

}
