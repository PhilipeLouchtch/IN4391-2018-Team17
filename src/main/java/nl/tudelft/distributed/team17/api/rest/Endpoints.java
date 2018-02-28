package nl.tudelft.distributed.team17.api.rest;

import nl.tudelft.distributed.team17.Entity;
import nl.tudelft.distributed.team17.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class Endpoints
{
	private final static Logger LOGGER = LoggerFactory.getLogger(Endpoints.class);

	private MessageService messageService;

	public Endpoints(MessageService messageService)
	{
		this.messageService = messageService;
		System.out.println("Ctor");
	}

	@GetMapping
	@RequestMapping(path = "/repeat")
	String bla()
	{
		System.out.println("hi");
		return "go away";
	}

	@PostMapping
	@RequestMapping("/message")
	String message(@RequestBody String body)
	{
		return messageService.processMsg(body);
	}

	@PostMapping
	@RequestMapping("/entity")
	Entity modify(@RequestBody Entity entity)
	{
		return new Entity(entity.getData() + "##", entity.getName() + "@@");
	}


}
