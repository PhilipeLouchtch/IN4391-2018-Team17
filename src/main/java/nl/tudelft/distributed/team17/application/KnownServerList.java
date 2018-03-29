package nl.tudelft.distributed.team17.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class KnownServerList
{
	private Set<String> knownServers;
	private String thisServer;

	private static final Logger LOG = LoggerFactory.getLogger(KnownServerList.class);

	@Autowired
	public KnownServerList() throws UnknownHostException
	{
		this(new HashSet<>(), InetAddress.getLocalHost().getHostAddress());
	}

	public KnownServerList(Set<String> knownServers, String thisServer)
	{
		this.knownServers = new HashSet<>(knownServers);
		this.knownServers.remove(thisServer);

		this.thisServer = thisServer;
	}

	/**
	 * Saves the server location into the KnownServerList
	 * @param serverLocation the serverlocation to add
	 */
	public void acceptServer(String serverLocation)
	{
		String locationTrimmedInterned = serverLocation.trim().intern();
		if(knownServers.add(locationTrimmedInterned))
		{
			LOG.info(String.format("Accepted server new %s", locationTrimmedInterned));
		}
	}

	/**
	 * Returns an unmodifiable set of known server locations
	 * @return An unmodifiable set
	 */
	public Set<String> getAllKnownServers()
	{
		HashSet<String> servers = new HashSet<>(knownServers);
		servers.add(thisServer);

		return servers;
	}

	/**
	 * Returns all known servers except this server
	 */
	public Set<String> getKnownOtherServers()
	{
		return Collections.unmodifiableSet(knownServers);
	}
}
