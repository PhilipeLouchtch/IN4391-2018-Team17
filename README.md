# IN4391-ds-assignment
The Big Assignment of the Distributed Systems course

## Docker

Run the Gradle `docker` task under Tasks>docker

## Runnning with docker swarm

Run the following commands in the root of this repo:

```docker swarm init```

```docker stack deploy -c docker-compose.yml ds-server```

## Killing it again
Either:
```docker swarm leave --force```

Or just the stack:
```docker stack rm ds-server```