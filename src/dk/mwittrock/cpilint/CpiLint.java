package dk.mwittrock.cpilint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.consumers.IssueConsumer;
import dk.mwittrock.cpilint.rules.Rule;
import dk.mwittrock.cpilint.suppliers.IflowArtifactSupplier;

public final class CpiLint {
	
	private static final Logger logger = LoggerFactory.getLogger(CpiLint.class);
	private final IflowArtifactSupplier supplier;
	private final Collection<Rule> rules;
	private final IssueConsumer consumer;
	
	public CpiLint(IflowArtifactSupplier supplier, Collection<Rule> rules, IssueConsumer consumer) {
		this.supplier = Objects.requireNonNull(supplier, "supplier must not be null");
		this.consumer = Objects.requireNonNull(consumer, "consumer must not be null");
		Objects.requireNonNull(rules, "rules must not be null");
		if (rules.isEmpty()) {
			throw new IllegalArgumentException("Empty rules collection");
		}
		this.rules = new ArrayList<>(rules);
	}

	public void run() {
		logger.info("Starting inspection of iflow artifacts");
		rules.forEach(r -> r.startTesting(consumer));
		supplier.setup();
		while (supplier.canSupply()) {
			IflowArtifact ia = supplier.supply();
			logger.debug("Iflow artifact supplied: {}", ia.getTag());
			rules.forEach(r -> r.inspect(ia));
		}
		supplier.shutdown();
		rules.forEach(r -> r.endTesting());
		logger.info("Inspection completed");
	}

}
