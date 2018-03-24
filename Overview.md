1. Player (= Client) connects to a server:
    a) Client sends a message to a server requesting to join the game

    b) Server receives the request to join, adds the client to its client list

    c) Server assigns random, empty place for the player on the board and assigns HP(10-20) and AP(1-10) to it

    d) Server puts this on a ledger

    e) At some point (max(time, ledgerSize)) the server propagates a ledger to peer servers, they agree on an ordering, server creates new state and sends the state to the client

        i) If there is a conflict on a spawn location of the player and the player isn't spawned the steps from c) are repeated until it can be spawned
        ii) If at this point the server dies, other servers know about its clients and will detect the client that requested to join. One of them (based on the hash maybe, so there is no extra consensus required between servers) will take a role of the original, dead server and replay to server (or start from c))

    f) Client receives the state and is free to take an action

2. Player (= Client) takes an action

    a) It can be either:

        i) Move 1 step horizontally and vertically
        ii) Strike on a dragon at most 2 steps away (horizontally and vertically, not diagonally). Dragon's HP -= AP, if HP <= 0, then remove from battlefield
        iii) Heal a player at most 5 steps away (horizontally and vertically, not diagonally). Player's HP = min(HP + AP, MaxHP), up to Player's

    b) Player sends its move to the server

    c) Server puts this move on a ledger

    d) At some point (max(time, ledgerSize)) the server propagates a ledger to peer servers, they agree on an ordering, server creates new state and sends the state to the client together with the list of hashes of considered messages in this state update (aka those accepted and rejected) so the client knows if his move has been considered. Client can move maximum of 1 move per state update. So information if his move was accepted/rejected is important for it to make informed decision to make another move, as only the earliest move will be considered and later moves will be discarded. 

        i) If there is a conflict on an action (the same space will be occupied by two players) the new state will not include player's action results and the player will be free to take new action immediately

3. Player state handling:

    a) If the client disconnects we don't care. Its player will stand staill until it receives a command

    b) If the client dies, the client will see that the player is no longer present in the state and can handle this situation

4. Ledger updates
