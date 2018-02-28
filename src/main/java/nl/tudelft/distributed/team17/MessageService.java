package nl.tudelft.distributed.team17;

import org.springframework.stereotype.Component;

@Component
public class MessageService
{
	public String processMsg(String data)
	{
		return String.format("Got a message: [%s]", data);
	}
}
