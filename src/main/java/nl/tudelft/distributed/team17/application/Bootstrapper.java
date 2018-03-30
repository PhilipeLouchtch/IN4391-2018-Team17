package nl.tudelft.distributed.team17.application;

import net.coolicer.functional.actions.Rethrow;
import net.coolicer.util.Try;
import nl.tudelft.distributed.team17.infrastructure.api.rest.ServerEndpoints;
import nl.tudelft.distributed.team17.util.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class Bootstrapper implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger(Bootstrapper.class);
	public static final int CHECK_PERIOD_MS = 1000;

	private LedgerController ledgerController;
	private RestTemplate restTemplate;
	private KnownServerList knownServerList;

	public Bootstrapper(LedgerController ledgerController, RestTemplate restTemplate, KnownServerList knownServerList)
	{
		this.ledgerController = ledgerController;
		this.restTemplate = restTemplate;
		this.knownServerList = knownServerList;
	}

	@Override
	public void run()
	{
		InetAddress localHost = Try.getting(InetAddress::getLocalHost).or(Rethrow.asRuntime());
		byte[] address = localHost.getAddress();

		LOG.info(String.format("Starting server at [%s]", localHost.getHostAddress()));
		String serverAbove = String.format("%d.%d.%d.%d", address[0]& 0xFF, address[1] & 0xFF, address[2]& 0xFF, (address[3] & 0xFF) + 1);
		String serverBelow = String.format("%d.%d.%d.%d", address[0]& 0xFF, address[1] & 0xFF, address[2]& 0xFF, (address[3] & 0xFF) - 1);

		URI uriAbove = URI.create("http://" + serverAbove + ServerEndpoints.serverExchangeEndpoint);
		URI uriBelow = URI.create("http://" + serverBelow + ServerEndpoints.serverExchangeEndpoint);

		List<URI> uris = new ArrayList<>();
		uris.add(uriAbove);
		uris.add(uriBelow);

		while (!ledgerController.isRunning())
		{
			for (URI uri : uris)
			{
				try
				{
					ArrayList<String> known = new ArrayList<>(knownServerList.getAllKnownServers());
					ArrayList<String> arrayList = (ArrayList<String>) restTemplate.postForObject(uri, known, known.getClass());

					arrayList.forEach(knownServerList::acceptServer);
				}
				catch (ResourceAccessException ex)
				{
					LOG.warn("Could not access server ");
				}
				catch (Exception ex)
				{
					LOG.error("Error during bootstrap, ignoring", ex);
				}
			}

			Sleep.forMilis(CHECK_PERIOD_MS);
		}
	}
}
