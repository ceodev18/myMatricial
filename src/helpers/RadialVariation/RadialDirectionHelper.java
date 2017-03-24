package helpers.RadialVariation;

import java.util.ArrayList;
import java.util.List;

import helpers.RadialVariation.RadialMapHelper;
import interfaces.radialVariation.RadialConstants;
import models.RadialVariation.RadialLandPoint;

public class RadialDirectionHelper {
	public static List<Integer> orthogonalDirections(final int direction) {
		List<Integer> directions = new ArrayList<>();
		switch (direction) {
		case RadialConstants.NORTH:
			directions.add(RadialConstants.EAST);
			directions.add(RadialConstants.WEST);
			break;
		case RadialConstants.SOUTH:
			directions.add(RadialConstants.WEST);
			directions.add(RadialConstants.EAST);
			break;
		case RadialConstants.EAST:
			directions.add(RadialConstants.NORTH);
			directions.add(RadialConstants.SOUTH);
			break;
		case RadialConstants.WEST:
			directions.add(RadialConstants.SOUTH);
			directions.add(RadialConstants.NORTH);
			break;
		default:
			directions.add(-1);
		}
		return directions;
	}

	public static int oppositeDirection(int direction) {
		switch (direction) {
		case RadialConstants.NORTH:
			return RadialConstants.SOUTH;
		case RadialConstants.SOUTH:
			return RadialConstants.NORTH;
		case RadialConstants.EAST:
			return RadialConstants.WEST;
		case RadialConstants.WEST:
			return RadialConstants.EAST;
		}
		return -1;
	}

	public static int orthogonalDirectionFromPointToPoint(RadialLandPoint currentPoint, RadialLandPoint centroid) {
		int xDifference = centroid.getX() - currentPoint.getX();
		int yDifference = centroid.getY() - currentPoint.getY();

		if ((yDifference > 0) && (yDifference > xDifference)) {
			return RadialConstants.NORTH;
		}

		if ((yDifference < 0) && (yDifference > xDifference)) {
			return RadialConstants.SOUTH;
		}

		if ((xDifference > 0) && (yDifference < xDifference)) {
			return RadialConstants.EAST;
		}

		if ((xDifference < 0) && (yDifference < xDifference)) {
			return RadialConstants.WEST;
		}
		return -1;
	}
	
	public static int orthogonalDirectionFromPointToPoint(Integer initialPoint, Integer finalPoint) {
		int [] xyI = RadialMapHelper.breakKey(initialPoint);
		int [] xyF = RadialMapHelper.breakKey(finalPoint);
		
		int xDifference = xyF[0] - xyI[0];
		int yDifference = xyF[1] - xyI[1];

		if ((xDifference == 0) && (yDifference < 0)) {
			return RadialConstants.NORTH;
		}

		if ((yDifference < 0) && (xDifference == 0)) {
			return RadialConstants.SOUTH;
		}

		if ((xDifference > 0) && (yDifference == 0)) {
			return RadialConstants.EAST;
		}

		if ((xDifference < 0) && (yDifference == 0)) {
			return RadialConstants.WEST;
		}
		
		
		if ((yDifference > 0) && ((yDifference > xDifference)|| (xDifference == 0))) {
			return RadialConstants.NORTH;
		}

		if ((yDifference < 0) && ((yDifference > xDifference)|| (xDifference == 0))) {
			return RadialConstants.SOUTH;
		}

		if ((xDifference > 0) && ((yDifference < xDifference)|| (yDifference == 0))) {
			return RadialConstants.EAST;
		}

		if ((xDifference < 0) && ((yDifference < xDifference)|| (yDifference == 0))) {
			return RadialConstants.WEST;
		}
		return -1;
	}
	
	
	public static int directionFromPointToPoint(RadialLandPoint currentPoint, RadialLandPoint centroid) {
		int xDifference = centroid.getX() - currentPoint.getX();
		int yDifference = centroid.getY() - currentPoint.getY();

		if (yDifference > 0) {
			return RadialConstants.NORTH;
		}

		if (yDifference < 0) {
			return RadialConstants.SOUTH;
		}

		if (xDifference > 0) {
			return RadialConstants.EAST;
		}

		if (xDifference < 0) {
			return RadialConstants.WEST;
		}
		return -1;
	}

}
