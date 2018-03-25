package nl.tudelft.distributed.team17.application;

import nl.tudelft.distributed.team17.model.WorldState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Wraps a world state and provides a single abstraction with which synchronous access to a WorldState can be made
 */
@Component
public class CurrentWorldState
{
	private WorldState currentWorldState;
	private WorldState consideredWorldState;

	private Ledger ledger;

	@Autowired
	public CurrentWorldState()
	{
		this.currentWorldState = WorldState.initial();
		this.consideredWorldState = currentWorldState;
		this.ledger = Ledger.genesis();
	}

	public synchronized void applyToConsideredWorldState(Consumer<WorldState> fn, Callback callback)
	{
		// if ledger is being published then must wait on new accepted state before applying command,
		// all other callers will be blocked anyway as one caller is waiting inside

		fn.accept(consideredWorldState);

		// Command is accepted, need to exit method to allow other to do the same
		// Use callback to return the accepted state once the command has been published
	}
}
