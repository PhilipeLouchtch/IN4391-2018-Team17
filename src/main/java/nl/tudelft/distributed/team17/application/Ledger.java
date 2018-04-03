package nl.tudelft.distributed.team17.application;

import com.rits.cloning.Cloner;
import nl.tudelft.distributed.team17.model.WorldState;
import nl.tudelft.distributed.team17.model.command.Command;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.*;

public class Ledger
{
	private final static Logger LOG = LoggerFactory.getLogger(Ledger.class);

	private static final Ledger NO_PREVIOUS = null;
	private static final Random random = new Random();

	boolean isClosed;
	boolean isAccepted;

	private Ledger previous;

	// saves us the effort of rebuilding the state from the tx's (make transient?)
	private WorldState worldState;

	private int generation;

	// These are the "transactions" of the ledger
	private List<Command> commands;

	private int commandsAcceptedSoFar;

	// holds a tie-breaking roll
	private final int tieBreaker;

	private byte[] hashLazy;

	private Ledger(Ledger previous, WorldState worldState, int generation, boolean isClosed, int commandsAcceptedSoFar, int tieBreaker)
	{
		this.worldState = worldState;
		this.previous = previous;
		this.isClosed = isClosed;
		this.generation = generation;
		this.commandsAcceptedSoFar = commandsAcceptedSoFar;
		this.tieBreaker = tieBreaker;

		commands = new ArrayList<>();

		hashLazy = null; // lazy

		this.isAccepted = false;
	}

	public static Ledger genesis(WorldState worldState)
	{
		Objects.requireNonNull(worldState, "A genesis Ledger cannot have null as WorldState");

		final int ZERO_COMMANDS_IN_LEDGER_THUS_FAR = 0;
		final int RANDOM_ROLL_AS_TIEBREAKER = random.nextInt();
		final int GENERATION_ZERO = 0;
		final boolean LEDGER_IS_OPEN = true;

		return new Ledger(NO_PREVIOUS, worldState, GENERATION_ZERO, LEDGER_IS_OPEN, ZERO_COMMANDS_IN_LEDGER_THUS_FAR, RANDOM_ROLL_AS_TIEBREAKER);
	}

	/**
	 * Makes a Ledger that is not a Genesis ledger, and is not conected to any Ledger in the chain.
	 * Used in creating Ledger instances that are to be integrated into the local chain whose data came from an
	 * external machine or process.
	 * @param generation The generation number of the Ledger to construct
	 * @param commandsAcceptedSoFar The number of commands that the Ledger and its chain have seen
	 * @param tieBreaker The tie-breaker number generated for the Ledger
	 * @param hashAsByteArray
	 * @return A newly constructed Ledger without a link to a previous Ledger in the chain
	 */
	public static Ledger makeFloating(List<Command> commands, int generation, int commandsAcceptedSoFar, int tieBreaker, byte[] hashAsByteArray)
	{
		final boolean IS_CLOSED = true;
		final WorldState NO_WORLDSTATE = null;

		Ledger ledger = new Ledger(NO_PREVIOUS, NO_WORLDSTATE, generation, IS_CLOSED, commandsAcceptedSoFar, tieBreaker);
		ledger.commands = commands;
		ledger.hashLazy = hashAsByteArray;

		return ledger;
	}

	public synchronized WorldState getLastAcceptedWorldState()
	{
		if (isAccepted() || isGenesis())
		{
			return worldState;
		}

		return previous.worldState;
	}

	public synchronized Ledger makeNewHead()
	{
		final int roll = random.nextInt();
		final int commandsAcceptedSoFar = this.commandsAcceptedSoFar;
		final int generation = this.generation + 1;

		// Clone WorldState, so we always have an old, not changed copy
		Cloner cloner = new Cloner();
		WorldState copiedWorldState = cloner.deepClone(this.worldState);
		copiedWorldState.incrementClock(); // we're switching ledgers so the world increases its clock

		this.setClosed();

		return new Ledger(this, copiedWorldState, generation, false, commandsAcceptedSoFar, roll);
	}

	/**
	 * Adds the given command to the Ledger. Does so by first applying the command to the internal WorldState
	 * @param command
	 * @throws IllegalStateException if the Ledger is closed
	 * @throws Exception if the command could not be applied to the Ledger
	 */
	public synchronized void applyCommand(Command command)
	{
		if (isClosed())
		{
			throw new IllegalStateException("Cannot apply command to a closed Ledger");
		}

		// TODO: reject command if already applied (player & clock, hash?)

		final List<Command> playerCommandsBeforeApply = new ArrayList<>(commands);

		try
		{
			commands.add(command);
			command.apply(this.worldState);
			commandsAcceptedSoFar++;
			this.worldState.incrementClock();
		}
		catch (Exception ex)
		{
			/*  todo: do we revert worldstate?
				Although we do the checks before modifying it, so 99.9999% exceptions will not mean
			    that the world state has been modified or is in inconsistent state
			 */
			LOG.error("Could not apply command to Ledger", ex);
			commands = playerCommandsBeforeApply;
			throw ex;
		}
	}

	/**
	 * Makes this ledger the new head, in other words integrates the new ledger into our in memory chain
	 * @param losingLedger The ledger that was "current" to the instance of the application but failed to win in the consensus round
	 * @return The WorldState resulting from applying the commands contained in the ledger on the WordState of the 'previous' ledger
	 */
	public synchronized void replace(Ledger losingLedger)
	{
		this.previous = losingLedger.previous;
		this.isAccepted = true;

		// In first round a winning Ledger that came from a Dto is both a genesis and float, can only discern
		// the generation we're in through looking at the losing ledger as it still has all references
		if (losingLedger.isGenesis())
		{
			throw new RuntimeException("Fatal error: cannot replace a genesis ledger");
		}

		WorldState startingWorldState = this.previous.worldState;
		this.worldState = startingWorldState;

		try
		{
			for (int i = 0; i < commands.size(); i++)
			{
				Command command = commands.get(i);
				command.apply(worldState);
			}
		}
		catch (Exception ex)
		{
			throw new RuntimeException("Fatal error: encountered an exception during application of newly accepted Ledger's commands on top of previous ledger", ex);
		}

		// no longer need to keep this in memory, removing reference for GC
		this.previous.worldState = null;
	}

	public byte[] getHash()
	{
		if (hashLazy == null)
		{
			hashLazy = doHashLedger();
		}

		return hashLazy;
	}

	public String getHashHex()
	{
		return Hex.encodeHexString(getHash());
	}

	private byte[] doHashLedger()
	{
		MessageDigest messageDigest = new DigestUtils(MessageDigestAlgorithms.SHA_256).getMessageDigest();
		messageDigest = DigestUtils.updateDigest(messageDigest, this.previous.getHash());
		messageDigest = DigestUtils.updateDigest(messageDigest, ByteBuffer.allocate(4).putInt(generation));

		for (Command command : commands)
		{
			messageDigest = DigestUtils.updateDigest(messageDigest, command.getHash());
		}

		return messageDigest.digest();
	}

	public void setClosed()
	{
		isClosed = true;
	}

	public boolean isClosed()
	{
		return isClosed;
	}

	public boolean isAccepted()
	{
		return isAccepted;
	}

	public boolean isGenesis()
	{
		// Genesis blocks have no parent but do have a worldState
		return previous == null && worldState != null;
	}

	public boolean isFloating()
	{
		// "FLoating" blocks have no parent and no worldState
		return previous == null && worldState == null;
	}

	public int getGeneration()
	{
		return generation;
	}

	public int getNumCommandsAcceptedSoFar()
	{
		return commandsAcceptedSoFar;
	}

	public int getTieBreaker()
	{
		return tieBreaker;
	}

	public List<Command> getCommands()
	{
		return Collections.unmodifiableList(commands);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		Ledger otherLedger = (Ledger) o;
		return Arrays.equals(this.getHash(), otherLedger.getHash());
	}

	@Override
	public String toString()
	{
		return "Ledger{" +
				"isClosed=" + isClosed +
				", generation=" + generation +
				", #commands=" + commands.size() +
				", commandsAcceptedSoFar=" + commandsAcceptedSoFar +
				", tieBreaker=" + tieBreaker +
				'}';
	}
}
