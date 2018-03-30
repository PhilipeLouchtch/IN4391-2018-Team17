package nl.tudelft.distributed.team17.util;

public class Sleep
{
	public static void forMilis(long milis)
	{
		try
		{
			Thread.sleep(milis);
		}
		catch (InterruptedException ex)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(ex);
		}
	}
}
