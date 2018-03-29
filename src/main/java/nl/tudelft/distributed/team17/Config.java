package nl.tudelft.distributed.team17;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class Config
{
	@Bean
	ExecutorService executorService()
	{
		return Executors.newCachedThreadPool();
	}

	@Bean
	RestTemplate restTemplate()
	{
		return new RestTemplate();
	}
}
