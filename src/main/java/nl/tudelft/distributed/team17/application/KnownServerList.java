package nl.tudelft.distributed.team17.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class KnownServerList
{
	private Set<String> knownServers;

	@Autowired
	public KnownServerList()
	{
		this(new HashSet<>());
	}

	public KnownServerList(Set<String> knownServers)
	{
		this.knownServers = knownServers;
	}

	/**
	 * Saves the server location into the KnownServerList
	 * @param serverLocation the serverlocation to add
	 */
	public void acceptServer(String serverLocation)
	{
		knownServers.add(serverLocation.trim().intern());
	}

	/**
	 * Returns an unmodifiable set of known server locations
	 * @return An unmodifiable set
	 */
	public Set<String> getKnownServers()
	{
		return Collections.unmodifiableSet(knownServers);
	}
}
