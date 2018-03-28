package nl.tudelft.distributed.team17.client.cli;

import nl.tudelft.distributed.team17.infrastructure.api.rest.dto.AttackCommandDTO;
import nl.tudelft.distributed.team17.infrastructure.api.rest.dto.HealCommandDTO;
import nl.tudelft.distributed.team17.infrastructure.api.rest.dto.MoveCommandDTO;
import nl.tudelft.distributed.team17.infrastructure.api.rest.dto.SpawnCommandDTO;
import nl.tudelft.distributed.team17.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

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
        LOGGER.info(String.format("[%s]: started", clientId));
    }

    @Override
    public void run()
    {
        this.clientUnit = spawn();
        updateWorldState();
        while(!clientUnit.isDead())
        {
            try
            {
                TimeUnit.SECONDS.sleep(1);
            }
            catch(InterruptedException e)
            {
                continue;
            }
            if(!currentWorldState.anyDragonsLeft())
            {
                LOGGER.info(String.format("[%s, %s]: all dragons are dead, players win", clientId, currentWorldState.getWorldStateClock()));
                return;
            }
            performAction();
            updateWorldState();
        }
        LOGGER.info(String.format("[%s, %s]: player died", clientId, currentWorldState.getWorldStateClock()));
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
        if(closestDragonLocation.maxDistanceTo(clientLocation) > 2)
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
        makeRequest("attack", attackCommandDTO);
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
                makeRequest("move", moveCommandDTO);
                return;
            }
        }
        LOGGER.info(String.format(String.format("[%s, %s]: Can't move towards closest dragon [%s], paths are blocked", clientId, currentWorldState.getWorldStateClock(), dragon.getId())));
    }

    private void performHealPlayerAction(Unit player)
    {
        LOGGER.info(String.format("[%s, %s]: Healing player [%s]", clientId, currentWorldState.getWorldStateClock(), player.getId()));
        HealCommandDTO healCommandDTO =
                new HealCommandDTO(clientId, currentWorldState.getWorldStateClock(), player.getLocation());
        makeRequest("heal", healCommandDTO);
        LOGGER.info(String.format("[%s, %s]: Healed player [%s]", clientId, currentWorldState.getWorldStateClock(), player.getId()));
    }

    private Unit spawn()
    {
        LOGGER.info(String.format("[%s]: requesting spawning", clientId));
        SpawnCommandDTO spawnCommandDTO = new SpawnCommandDTO(clientId);
        Unit unit = makeRequest(spawnCommandDTO, "spawn", Unit.class);
        Location location = unit.getLocation();
        LOGGER.info(String.format(("[%s]: spawned on (%i,%i)"), clientId, location.getX(), location.getY()));
        return unit;
    }

    public void updateWorldState()
    {
        LOGGER.info(String.format("[%s, %s]: Requesting new worldState", clientId, currentWorldState.getWorldStateClock()));
        WorldState worldState = makeRequest("worldstate", WorldState.class);
        LOGGER.info(String.format("[%s, %s]: Got worldState [%s]", clientId, currentWorldState.getWorldStateClock(), worldState.getWorldStateClock()));
        this.clientUnit = worldState.getPlayerUnit(clientId);
        this.currentWorldState = worldState;
    }

    private String createUrl(String endPointUrl)
    {
        return serverAddress + "/" + endPointUrl;
    }

    private <T, E> T makeRequest(E requestBody, String endPoint, Class<T> responseType)
    {
        HttpEntity<E> request = new HttpEntity<>(requestBody);
        ResponseEntity<T> response =
                restTemplate.exchange(createUrl(endPoint), HttpMethod.POST, request, responseType);
        T responseBody = response.getBody();
        return responseBody;
    }

    private <T> T makeRequest(String endPoint, Class<T> responseType)
    {
        T responseBody = restTemplate.getForObject(createUrl(endPoint), responseType);
        return responseBody;
    }

    private <E> void makeRequest(String endPoint, E requestBody)
    {
        HttpEntity<E> request = new HttpEntity<>(requestBody);
        restTemplate.exchange(createUrl(endPoint), HttpMethod.PUT, request, Void.class);
    }
}
