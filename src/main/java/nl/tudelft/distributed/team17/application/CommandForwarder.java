package nl.tudelft.distributed.team17.application;

import nl.tudelft.distributed.team17.infrastructure.InterServerCommunication;
import nl.tudelft.distributed.team17.model.command.Command;
import org.springframework.stereotype.Service;

@Service
public class CommandForwarder
{
	private InterServerCommunication interServerCommunication;

	public CommandForwarder(InterServerCommunication interServerCommunication)
	{
		this.interServerCommunication = interServerCommunication;
	}

	public void forward(Command command)
	{
		interServerCommunication.broadcast(command);
	}
}
