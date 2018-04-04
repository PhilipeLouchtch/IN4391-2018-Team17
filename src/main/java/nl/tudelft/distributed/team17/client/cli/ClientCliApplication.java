package nl.tudelft.distributed.team17.client.cli;

import nl.tudelft.distributed.team17.util.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ClientCliApplication
{
    public static void main(String[] args)
    {
        //disabled banner, don't want to see the spring logo
        SpringApplication app = new SpringApplication(ClientCliApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        //ClientCliApplication bean = app.run(args).getBean(ClientCliApplication.class);
        //ben.run();
        ClientCliApplication properWorkingAppObject= new ClientCliApplication();
        properWorkingAppObject.run();
    }

    public void run()
    {
	    Logger main_process = LoggerFactory.getLogger(ClientCliApplication.class);

    	final int NUM_BOTS = 100;
	    List<Thread> clientBots = new ArrayList<>(NUM_BOTS);

        for (int i = 0; i < NUM_BOTS; i++)
        {
            final int j = i;
	        Thread clientBotThread = new Thread(() ->
	        {
		        String clientId = "clientId" + j;
		        ClientBot clientBot = new ClientBot("localhost", clientId);
		        clientBot.run();
	        });

	        clientBots.add(clientBotThread);
        }

	    main_process.warn("Starting {} client bots", NUM_BOTS);
        clientBots.forEach(Thread::start);

	    Sleep.forMilis(8000);

	    main_process.warn("Stopping the client bots NOW", NUM_BOTS);
	    clientBots.forEach(Thread::stop);
    }
}
