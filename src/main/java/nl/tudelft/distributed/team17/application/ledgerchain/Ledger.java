package nl.tudelft.distributed.team17.application.ledgerchain;

import nl.tudelft.distributed.team17.model.WorldState;

public class Ledger
{
	transient WorldState worldState;
	private Ledger previous;
}