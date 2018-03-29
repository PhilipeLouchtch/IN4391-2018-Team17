package nl.tudelft.distributed.team17;

import nl.tudelft.distributed.team17.application.Bootstrapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.ExecutorService;

@SpringBootApplication
public class Application
{
	private Bootstrapper bootstrapper;
	private final ExecutorService executorService;

	public Application(Bootstrapper bootstrapper, ExecutorService executorService)
	{
		this.bootstrapper = bootstrapper;
		this.executorService = executorService;
	}

	void bootstrap()
	{
		executorService.submit(bootstrapper);
	}

	public static void main(String[] args)
	{
		ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
		ctx.getBean(Application.class).bootstrap();
	}
}
