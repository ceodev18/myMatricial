package helpers.matricialVariation;

import java.util.ArrayList;
import java.util.List;

import helpers.matricialVariation.MatricialMapHelper;
import interfaces.matricialVariation.MatricialConstants;
import models.matricialVariation.MatricialLandPoint;

public class MatricialDirectionHelper {
	public static List<Integer> orthogonalDirections(final int direction) {
		List<Integer> directions = new ArrayList<>();
		switch (direction) {
		case MatricialConstants.NORTH:
			directions.add(MatricialConstants.EAST);
			directions.add(MatricialConstants.WEST);
			break;
		case MatricialConstants.SOUTH:
			directions.add(MatricialConstants.WEST);
			directions.add(MatricialConstants.EAST);
			break;
		case MatricialConstants.EAST:
			directions.add(MatricialConstants.NORTH);
			directions.add(MatricialConstants.SOUTH);
			break;
		case MatricialConstants.WEST:
			directions.add(MatricialConstants.SOUTH);
			directions.add(MatricialConstants.NORTH);
			break;
		default:
			directions.add(-1);
		}
		return directions;
	}
	

	public static int oppositeDirection(int direction) {
		switch (direction) {
		case MatricialConstants.NORTH:
			return MatricialConstants.SOUTH;
		case MatricialConstants.SOUTH:
			return MatricialConstants.NORTH;
		case MatricialConstants.EAST:
			return MatricialConstants.WEST;
		case MatricialConstants.WEST:
			return MatricialConstants.EAST;
		}
		return -1;
	}
	
	public static int orthogonalDirectionFromPointToPoint(MatricialLandPoint currentPoint, MatricialLandPoint centroid) {
		int xDifference = centroid.getX() - currentPoint.getX();
		int yDifference = centroid.getY() - currentPoint.getY();

		if ((yDifference > 0) && (yDifference > xDifference)) {
			return MatricialConstants.NORTH;
		}

		if ((yDifference < 0) && (yDifference > xDifference)) {
			return MatricialConstants.SOUTH;
		}

		if ((xDifference > 0) && (yDifference < xDifference)) {
			return MatricialConstants.EAST;
		}

		if ((xDifference < 0) && (yDifference < xDifference)) {
			return MatricialConstants.WEST;
		}
		return -1;
	}
	
	public static int orthogonalDirectionFromPointToPoint(Integer initialPoint, Integer finalPoint) {
		int [] xyI = MatricialMapHelper.breakKey(initialPoint);
		int [] xyF = MatricialMapHelper.breakKey(finalPoint);
		
		int xDifference = xyF[0] - xyI[0];
		int yDifference = xyF[1] - xyI[1];

		if ((xDifference == 0) && (yDifference < 0)) {
			return MatricialConstants.NORTH;
		}

		if ((yDifference < 0) && (xDifference == 0)) {
			return MatricialConstants.SOUTH;
		}

		if ((xDifference > 0) && (yDifference == 0)) {
			return MatricialConstants.EAST;
		}

		if ((xDifference < 0) && (yDifference == 0)) {
			return MatricialConstants.WEST;
		}
		
		
		if ((yDifference > 0) && ((yDifference > xDifference)|| (xDifference == 0))) {
			return MatricialConstants.NORTH;
		}

		if ((yDifference < 0) && ((yDifference > xDifference)|| (xDifference == 0))) {
			return MatricialConstants.SOUTH;
		}

		if ((xDifference > 0) && ((yDifference < xDifference)|| (yDifference == 0))) {
			return MatricialConstants.EAST;
		}

		if ((xDifference < 0) && ((yDifference < xDifference)|| (yDifference == 0))) {
			return MatricialConstants.WEST;
		}
		return -1;
	}
	
	public static int directionFromPointToPoint(MatricialLandPoint currentPoint, MatricialLandPoint centroid) {
		int xDifference = centroid.getX() - currentPoint.getX();
		int yDifference = centroid.getY() - currentPoint.getY();

		if (yDifference > 0) {
			return MatricialConstants.NORTH;
		}

		if (yDifference < 0) {
			return MatricialConstants.SOUTH;
		}

		if (xDifference > 0) {
			return MatricialConstants.EAST;
		}

		if (xDifference < 0) {
			return MatricialConstants.WEST;
		}
		return -1;
	}
	
}
