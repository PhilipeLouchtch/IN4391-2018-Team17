1. Player (= Client) connects to a server:
    a) Client sends a message to a server requesting to join the game
    b) Server receives the request to join, adds the client to its client list
    c) Server assigns random, empty place for the player on the board and assigns HP(10-20) and AP(1-10) to it
    d) Server puts this on a ledger
    e) At some point (max(time, ledgerSize)) it propagates a ledger to peer servers, they agree on an ordering, server creates new state and sends the state to the client.
        i) If there is a conflict on a spawn location of the player and the player isn't spawned the steps from c) are repeated until it can be spawned
        ii) If at this point the server dies, other servers know about its clients and will detect the client that requested to join. One of them (based on the hash maybe, so there is no extra consensus required between servers) will take a role of original, dead server and repeated from c)
    f) Client receives the state and is free to take an action
2. 