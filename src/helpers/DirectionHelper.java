package helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import interfaces.Constants;

public class DirectionHelper {
	public static List<Integer> randomOrthogonalDirection(final int direction) {
		List<Integer> directions = new ArrayList<>();
		switch (direction) {
		case Constants.NORTH:
			directions.add(Constants.EAST);
			directions.add(Constants.WEST);
			break;
		case Constants.SOUTH:
			directions.add(Constants.WEST);
			directions.add(Constants.EAST);
			break;
		case Constants.EAST:
			directions.add(Constants.NORTH);
			directions.add(Constants.SOUTH);
			break;
		case Constants.WEST:
			directions.add(Constants.SOUTH);
			directions.add(Constants.NORTH);
			break;
		default:
			directions.add(-1);
		}
		return directions;
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