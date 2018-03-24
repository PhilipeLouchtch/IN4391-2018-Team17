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

	@Autowired
	public CurrentWorldState()
	{
		this(WorldState.initial());
	}

	public CurrentWorldState(WorldState currentWorldState)
	{
		this.currentWorldState = currentWorldState;
		this.consideredWorldState = currentWorldState;
	}

	public synchronized void applyToConsideredWorldState(Function<WorldState, WorldState> fn)
	{
		WorldState nextWorldState = fn.apply(consideredWorldState);
		this.consideredWorldState = nextWorldState;
	}
}
