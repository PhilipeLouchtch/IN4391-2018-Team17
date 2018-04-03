package nl.tudelft.distributed.team17.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.tudelft.distributed.team17.application.Ledger;
import nl.tudelft.distributed.team17.model.command.Command;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.Collections;
import java.util.List;

public class LedgerDto
{
	@JsonProperty("commands")
	private List<Command> commands;

	@JsonProperty("commandsAcceptedByLedgerChain")
	private int commandsAcceptedByLedgerChain;

	@JsonProperty("generation")
	private int generation;

	@JsonProperty("tieBreaker")
	private int tieBreaker;

	@JsonProperty("hash")
	private String hash;

	// For Jackson
	private LedgerDto()
	{
	}

	public LedgerDto(List<Command> commands, int commandsAcceptedByLedgerChain, int generation, int tieBreaker, String hash)
	{
		this();
		this.commands = commands;
		this.commandsAcceptedByLedgerChain = commandsAcceptedByLedgerChain;
		this.generation = generation;
		this.tieBreaker = tieBreaker;
		this.hash = hash;
	}

	public static LedgerDto from(Ledger ledger)
	{

		List<Command> commands = ledger.getCommands();
		int numCommandsAcceptedSoFar = ledger.getNumCommandsAcceptedSoFar();
		int generation = ledger.getGeneration();
		int tieBreaker = ledger.getTieBreaker();
		String hash = Hex.encodeHexString(ledger.getHash());

		return new LedgerDto(commands, numCommandsAcceptedSoFar, generation, tieBreaker, hash);
	}

	public Ledger toLedger()
	{
		byte[] hashAsByteArray;
		try
		{
			hashAsByteArray = Hex.decodeHex(hash);
		}
		catch (DecoderException ex)
		{
			// Idk
			throw new RuntimeException(ex);
		}

		return Ledger.makeFloating(commands, generation, commandsAcceptedByLedgerChain, tieBreaker, hashAsByteArray);
	}

	public List<Command> getCommands()
	{
		return Collections.unmodifiableList(commands);
	}

	public int getCommandsAcceptedByLedgerChain()
	{
		return commandsAcceptedByLedgerChain;
	}

	public int getGeneration()
	{
		return generation;
	}

	public int getTieBreaker()
	{
		return tieBreaker;
	}

	private String commandsToString()
	{
		return "{size=" + commands.size() + "}";
	}

	@Override
	public String toString()
	{
		return "LedgerDto{" +
				"commands=" + commandsToString() +
				", commandsAcceptedByLedgerChain=" + commandsAcceptedByLedgerChain +
				", generation=" + generation +
				", tieBreaker=" + tieBreaker +
				'}';
	}
}
