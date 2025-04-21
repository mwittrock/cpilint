package org.cpilint.rules;

import java.util.Objects;
import java.util.Optional;

import org.cpilint.consumers.IssueConsumer;

public abstract class RuleBase implements Rule {
	
	protected IssueConsumer consumer;
	protected Optional<String> ruleId = Optional.empty();

	@Override
	public void startTesting(IssueConsumer consumer) {
		this.consumer = Objects.requireNonNull(consumer, "consumer must not be null");
	}

	@Override
	public void endTesting() {
		// Any required action is taken in subclasses.
	}

	@Override
	public void setId(String id) {
		if (ruleId.isPresent()) {
			throw new IllegalStateException("Rule ID already set");
		}
		ruleId = Optional.of(Objects.requireNonNull(id, "id must not be null"));
	}

	@Override
	public Optional<String> getId() {
		return ruleId;
	}

}
