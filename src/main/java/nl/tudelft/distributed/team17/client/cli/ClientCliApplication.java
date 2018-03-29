package nl.tudelft.distributed.team17.client.cli;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

        for (int i = 0; i < 100; i++)
        {
            final int j = i;
             new Thread(() ->
             {
                 String clientId = String.format("clientId%d", j);
                ClientBot clientBot = new ClientBot("localhost:80", clientId);
                clientBot.run();
             }).start();
        }

    }
}
