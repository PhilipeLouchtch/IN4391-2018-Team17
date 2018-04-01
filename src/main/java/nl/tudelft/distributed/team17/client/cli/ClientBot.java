package nl.tudelft.distributed.team17.client.cli;

import nl.tudelft.distributed.team17.infrastructure.api.rest.PlayerEndpoints;
import nl.tudelft.distributed.team17.infrastructure.api.rest.dto.AttackCommandDTO;
import nl.tudelft.distributed.team17.infrastructure.api.rest.dto.HealCommandDTO;
import nl.tudelft.distributed.team17.infrastructure.api.rest.dto.MoveCommandDTO;
import nl.tudelft.distributed.team17.infrastructure.api.rest.dto.SpawnCommandDTO;
import nl.tudelft.distributed.team17.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ClientBot implements Runnable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientBot.class);

    private RestTemplate restTemplate;

    private String serverAddress;
    private String clientId;
    private Unit clientUnit;
    private WorldState currentWorldState;


    public ClientBot(String serverAddress, String clientId)
    {
        this.serverAddress = serverAddress;
        this.clientId = clientId;
        this.restTemplate = new RestTemplate();
        LOGGER.info(String.format("[%s]: started talking to [%s]", clientId, serverAddress));
    }

    @Override
    public void run()
    {
        // get worldstate, if it does not contain player unit (so it is out first entry to the game) create it
        if(!updateWorldState())
        {
            // request spawning player unit
            this.clientUnit = spawn();
            // get worldstates until it contains client unit so we can take actions
            while(!updateWorldState())
            {
                sleep(1000);
            }
        }
        // otherwise enter action loop
        while(!clientUnit.isDead())
        {
            sleep(1000);
            if(!currentWorldState.anyDragonsLeft())
            {
                LOGGER.info(String.format("[%s, %s]: all dragons are dead, players win", clientId, currentWorldState.getWorldStateClock()));
                return;
            }
            performAction();
            if(!updateWorldState())
            {
                LOGGER.info(String.format("[%s, %s]: worldState did not contain player unit, should not happen", clientId, currentWorldState.getWorldStateClock()));
                throw new RuntimeException("Worldstate did not contain player unit. should not happen");
            }
        }
        LOGGER.info(String.format("[%s, %s]: player died", clientId, currentWorldState.getWorldStateClock()));
    }

    private void sleep(int miliseconds)
    {
        try
        {
            TimeUnit.MILLISECONDS.sleep(miliseconds);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        }
    }

    private void performAction()
    {
        List<Unit> playersInRange = currentWorldState.playersInRangeOfUnit(clientUnit, 5);
        for (int i = 0; i < playersInRange.size(); i++)
        {
            Unit player = playersInRange.get(i);
            UnitHealth playerHealth = player.getUnitHealth();
            if(playerHealth.halfHealthOrLess())
            {
                performHealPlayerAction(player);
                return;
            }
        }

        Optional<Unit> closestDragon = currentWorldState.getClosestDragonToUnit(clientId);
        // if there are no dragons then end
        if(!closestDragon.isPresent())
        {
            LOGGER.error(String.format("[%s, %s]: Did not find any dragon, should not happen", clientId, currentWorldState.getWorldStateClock()));
            throw new RuntimeException("Did not find any dragon, should not happen");
        }
        Unit closestDragonValue = closestDragon.get();
        Location closestDragonLocation = closestDragonValue.getLocation();
        Location clientLocation = clientUnit.getLocation();

        // We need to move towards that dragon
        if(clientLocation.distanceTo(closestDragonLocation) > 2)
        {
            performMoveTowardsDragonAction(closestDragonValue);
        }
        // We can attack that dragon
        else
        {
            performAttackDragonAction(closestDragonValue);
        }
    }

    private void performAttackDragonAction(Unit dragon)
    {
        LOGGER.info(String.format("[%s, %s]: Attacking closest dragon [%s]", clientId, currentWorldState.getWorldStateClock(), dragon.getId()));
        AttackCommandDTO attackCommandDTO = new AttackCommandDTO(clientId, currentWorldState.getWorldStateClock(), dragon.getLocation());
        makeRequest(PlayerEndpoints.attackPlayerEndpoint, attackCommandDTO);
        LOGGER.info(String.format("[%s, %s]: Attacked closest dragon [%s]", clientId, currentWorldState.getWorldStateClock(), dragon.getId()));
    }

    private void performMoveTowardsDragonAction(Unit dragon)
    {
        Location dragonLocation = dragon.getLocation();
        Location clientLocation = clientUnit.getLocation();
        List<Direction> directions = clientLocation.getMoveDirectionsTowards(dragonLocation);
        for (Direction direction: directions)
        {
            Location movedClient = clientLocation.moved(direction);
            if(!currentWorldState.locationOccupied(movedClient))
            {
                LOGGER.info(String.format("[%s, %s]: Moving towards closest dragon [%s], direction [%s]", clientId, currentWorldState.getWorldStateClock(), dragon.getId(), direction));
                MoveCommandDTO moveCommandDTO = new MoveCommandDTO(clientId, currentWorldState.getWorldStateClock(), direction);
                makeRequest(PlayerEndpoints.movePlayerEndpoint, moveCommandDTO);
                return;
            }
        }
        LOGGER.info(String.format("[%s, %s]: Can't move towards closest dragon [%s], paths are blocked", clientId, currentWorldState.getWorldStateClock(), dragon.getId()));
    }

    private void performHealPlayerAction(Unit player)
    {
        LOGGER.info(String.format("[%s, %s]: Healing player [%s]", clientId, currentWorldState.getWorldStateClock(), player.getId()));
        HealCommandDTO healCommandDTO =
                new HealCommandDTO(clientId, currentWorldState.getWorldStateClock(), player.getLocation());
        makeRequest(PlayerEndpoints.healPlayerEndpoint, healCommandDTO);
        LOGGER.info(String.format("[%s, %s]: Healed player [%s]", clientId, currentWorldState.getWorldStateClock(), player.getId()));
    }

    private Unit spawn()
    {
        LOGGER.info(String.format("[%s]: requesting spawning", clientId));
        SpawnCommandDTO spawnCommandDTO = new SpawnCommandDTO(clientId, currentWorldState.getWorldStateClock());
        Unit unit = makeRequest(spawnCommandDTO, PlayerEndpoints.spawnPlayerEndpoint, Unit.class);

        if (unit == null)
        {
            LOGGER.info(String.format(("[%s]: could not spawn, attempting another try"), clientId));
            unit = spawn();
        }

        Location location = unit.getLocation();
        LOGGER.info(String.format(("[%s]: spawned on (%d,%d)"), clientId, location.getX(), location.getY()));
        return unit;
    }

    // Returns true if client unit found in the returned worldState, false if not found in the returned worldState
    public boolean updateWorldState()
    {
	    WorldState worldState;
	    while(true)
        {
            if (currentWorldState == null)
            {
                LOGGER.info(String.format("[%s, null]: Requesting worldState for first time", clientId));
            }
            else
            {
                LOGGER.info(String.format("[%s, %s]: Requesting next worldState", clientId, currentWorldState.getWorldStateClock()));
            }

            try
            {
                worldState = makeRequest(PlayerEndpoints.worldStatePlayerEndpoint, WorldState.class);
            }
            catch(ResourceAccessException reaEx)
            {
                if (currentWorldState == null)
                {
                    LOGGER.info(String.format("[%s, null]: Requesting worldState failed because server unavailable, retrying in 1s", clientId));
                }
                else
                {
                    LOGGER.info(String.format("[%s, %s]: Requesting worldState failed because server unavailable, retrying in 1s", clientId, currentWorldState.getWorldStateClock()));
                }
                sleep(1000);
                continue;
            }
            break;
        }
        if (currentWorldState == null)
        {
            LOGGER.info(String.format("[%s, null]: Got worldState [%s]", clientId, worldState.getWorldStateClock()));
        }
        else
        {
            LOGGER.info(String.format("[%s, %s]: Got worldState [%s]", clientId, currentWorldState.getWorldStateClock(), worldState.getWorldStateClock()));
        }
        this.currentWorldState = worldState;

        Optional<Unit> playerUnit = worldState.findPlayerUnit(clientId);
        if(playerUnit.isPresent())
        {
            this.clientUnit = playerUnit.get();
            return true;
        }

        return false;
    }

    private URI createUri(String endPointUrl)
    {
        URI uri = URI.create("http://" + serverAddress + endPointUrl);
        LOGGER.trace(String.format("[%s]: Next call URI - %s", clientId, uri.toString()));
        return uri;
    }

    private <T, E> T makeRequest(E requestBody, String endPoint, Class<T> responseType)
    {
        HttpEntity<E> request = new HttpEntity<>(requestBody);
        ResponseEntity<T> response =
                restTemplate.postForEntity(createUri(endPoint), request, responseType);
        T responseBody = response.getBody();
        return responseBody;
    }

    private <T> T makeRequest(String endPoint, Class<T> responseType)
    {
        T responseBody = restTemplate.getForObject(createUri(endPoint), responseType);
        return responseBody;
    }

    private <E> void makeRequest(String endPoint, E requestBody)
    {
        HttpEntity<E> request = new HttpEntity<>(requestBody);
        try
        {
            restTemplate.put(createUri(endPoint), request);
        }
        catch (HttpServerErrorException ex)
        {
            LOGGER.error("Error during request to {}", endPoint);
        }
    }
}
