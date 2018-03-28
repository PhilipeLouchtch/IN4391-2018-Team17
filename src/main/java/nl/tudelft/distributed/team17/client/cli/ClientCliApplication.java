package nl.tudelft.distributed.team17.client.cli;

import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClientCliApplication implements CommandLineRunner
{
    public static void main(String[] args)
    {
        //disabled banner, don't want to see the spring logo
        SpringApplication app = new SpringApplication(ClientCliApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception
    {
        ClientBot clientBot = new ClientBot("http://192.168.1.1", "clientId0");
        clientBot.run();
        // new Thread(() ->
        // {
        // }).start();
    }
}
