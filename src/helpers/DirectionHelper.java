package helpers;

import java.util.Random;
import interfaces.Constants;

public class DirectionHelper {
	public static int randomOrthogonalDirection(final int direction) {
		switch (direction) {
		case Constants.NORTH:
			if (new Random().nextInt(1) == 0)
				return Constants.EAST;
			else
				return Constants.WEST;
		case Constants.SOUTH:
			if (new Random().nextInt(1) == 0)
				return Constants.EAST;
			else
				return Constants.WEST;
		case Constants.EAST:
			if (new Random().nextInt(1) == 0)
				return Constants.NORTH;
			else
				return Constants.SOUTH;
		case Constants.WEST:
			if (new Random().nextInt(1) == 0)
				return Constants.NORTH;
			else
				return Constants.SOUTH;
		}
		return -1;
	}

	public static int oppositeDirection(int direction) {
		switch (direction) {
		case Constants.NORTH:
			return Constants.SOUTH;
		case Constants.SOUTH:
			return Constants.NORTH;
		case Constants.EAST:
			return Constants.WEST;
		case Constants.WEST:
			return Constants.EAST;
		}
		return -1;
	}
}