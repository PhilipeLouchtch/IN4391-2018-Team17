package nl.tudelft.distributed.team17.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.tudelft.distributed.team17.application.Ledger;
import nl.tudelft.distributed.team17.model.command.Command;

import java.util.List;

public class LedgerDto
{
	@JsonProperty("commands")
	private List<Command> commands;

	@JsonProperty("commandsAcceptedByLedgerChain")
	private int commandsAcceptedByLedgerChain;

	@JsonProperty("generation")
	private int generation;

	@JsonProperty("roll")
	private int roll;

	public LedgerDto(List<Command> commands, int commandsAcceptedByLedgerChain, int generation, int roll)
	{
		this();
		this.commands = commands;
		this.commandsAcceptedByLedgerChain = commandsAcceptedByLedgerChain;
		this.generation = generation;
		this.roll = roll;
	}

	public static LedgerDto from(Ledger ledger)
	{
		return new LedgerDto(ledger.getCommands(), ledger.getNumCommandsAcceptedSoFar(), ledger.getGeneration(), ledger.getTieBreaker());
	}

	public List<Command> getCommands()
	{
		return commands;
	}

	public int getCommandsAcceptedByLedgerChain()
	{
		return commandsAcceptedByLedgerChain;
	}

	public int getGeneration()
	{
		return generation;
	}

	public int getRoll()
	{
		return roll;
	}

	public Ledger toLedger()
	{
		return new Ledger(null, null, this.generation, true, commandsAcceptedByLedgerChain, roll);
	}

	// For Jackson
	private LedgerDto()
	{
	}
}
