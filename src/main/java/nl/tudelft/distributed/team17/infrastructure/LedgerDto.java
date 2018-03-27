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

	@JsonProperty("roll")
	private int roll;

	public LedgerDto(List<Command> commands, int commandsAcceptedByLedgerChain, int roll)
	{
		this();
		this.commands = commands;
		this.commandsAcceptedByLedgerChain = commandsAcceptedByLedgerChain;
		this.roll = roll;
	}

	public static LedgerDto from(Ledger ledger)
	{
		return new LedgerDto(ledger.getCommands(), ledger.getNumCommandsAcceptedSoFar(), ledger.getTieBreaker());
	}

	public List<Command> getCommands()
	{
		return commands;
	}

	public int getCommandsAcceptedByLedgerChain()
	{
		return commandsAcceptedByLedgerChain;
	}

	public int getRoll()
	{
		return roll;
	}

	// For Jackson
	private LedgerDto()
	{
	}
}
