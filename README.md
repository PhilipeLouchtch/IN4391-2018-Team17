# TUDelft IN4391 - Distributed Computer Systems assignment
# Blazej Kula & Philippe Louchtch

## Introduction
This is the "Knights and Dragons" game version of the assignment. Our resilience & redundancy solution is inspired by the blockchain. Commands are sent to some server, that server then broadcasts the command to all known servers and also tries to apply the command on its own WorldState. If the server succeeds in applying, the command is added to its _current_ ledger. When a Ledger timeout occurs, the ledger is closed and is exchanged with other servers. Once the exchange is completed, the ledgers a _consensus_ phase is run, here the _best_ ledger is selected by the amount of commands the ledger was able to successfully apply. If there is a tie, the ledgers contain a _tie-breaker_ number which is then used to resolve the tie.

The ledger exchange mechanism, as previously mentioned, is blockchain inspired. However, there is no proof of work or any other trust mechanism. The servers are imagined to be run either by a single or trusted parties. The communications between servers happens within their private network.

This project was an academic exercise only. Corners were cut.

## Running it

### Build the Docker Image

IntelliJ: run the Gradle `docker` task under Tasks>docker
Gradle: ```gradlew docker```

### Initialize Docker Swarm
run: ```docker swarm init```

### Deploy the Stack
In the root of the repository
run: ```docker stack deploy -c docker-compose.yml ds-server```

### Stopping
Stop the stack:
```docker stack rm ds-server```

Nuclear option:
```docker swarm leave --force```
