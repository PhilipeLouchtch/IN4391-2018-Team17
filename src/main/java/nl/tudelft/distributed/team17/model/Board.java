package nl.tudelft.distributed.team17.model;

import distributed.systems.das.units.Unit;

public class Board
{
	static final int BOARD_SIZE = 25;

	private Unit[][] fields;

	private Board(int dimension)
	{
		this.fields = new Unit[dimension][dimension];
	}

	public static Board make()
	{
		return new Board(BOARD_SIZE);
	}

	public boolean isValidLocation(int x, int y)
	{
		if (fields.length > x || fields.length > y || x < 0 || y < 0)
		{
			return false;
		}

		return true;
	}
}
