package helpers.spineVariation;

import java.util.ArrayList;
import java.util.List;

import helpers.spineVariation.SpineMapHelper;
import interfaces.spineVariation.SpineConstants;
import models.spineVariation.SpineLandPoint;

public class SpineDirectionHelper {
	public static List<Integer> orthogonalDirections(final int direction) {
		List<Integer> directions = new ArrayList<>();
		switch (direction) {
		case SpineConstants.NORTH:
			directions.add(SpineConstants.EAST);
			directions.add(SpineConstants.WEST);
			break;
		case SpineConstants.SOUTH:
			directions.add(SpineConstants.WEST);
			directions.add(SpineConstants.EAST);
			break;
		case SpineConstants.EAST:
			directions.add(SpineConstants.NORTH);
			directions.add(SpineConstants.SOUTH);
			break;
		case SpineConstants.WEST:
			directions.add(SpineConstants.SOUTH);
			directions.add(SpineConstants.NORTH);
			break;
		default:
			directions.add(-1);
		}
		return directions;
	}

	public static int oppositeDirection(int direction) {
		switch (direction) {
		case SpineConstants.NORTH:
			return SpineConstants.SOUTH;
		case SpineConstants.SOUTH:
			return SpineConstants.NORTH;
		case SpineConstants.EAST:
			return SpineConstants.WEST;
		case SpineConstants.WEST:
			return SpineConstants.EAST;
		}
		return -1;
	}

	public static int orthogonalDirectionFromPointToPoint(SpineLandPoint currentPoint, SpineLandPoint centroid) {
		int xDifference = centroid.getX() - currentPoint.getX();
		int yDifference = centroid.getY() - currentPoint.getY();

		if ((yDifference > 0) && (yDifference > xDifference)) {
			return SpineConstants.NORTH;
		}

		if ((yDifference < 0) && (yDifference > xDifference)) {
			return SpineConstants.SOUTH;
		}

		if ((xDifference > 0) && (yDifference < xDifference)) {
			return SpineConstants.EAST;
		}

		if ((xDifference < 0) && (yDifference < xDifference)) {
			return SpineConstants.WEST;
		}
		return -1;
	}
	
	public static int orthogonalDirectionFromPointToPoint(Integer initialPoint, Integer finalPoint) {
		int [] xyI = SpineMapHelper.breakKey(initialPoint);
		int [] xyF = SpineMapHelper.breakKey(finalPoint);
		
		int xDifference = xyF[0] - xyI[0];
		int yDifference = xyF[1] - xyI[1];

		if ((xDifference == 0) && (yDifference < 0)) {
			return SpineConstants.NORTH;
		}

		if ((yDifference < 0) && (xDifference == 0)) {
			return SpineConstants.SOUTH;
		}

		if ((xDifference > 0) && (yDifference == 0)) {
			return SpineConstants.EAST;
		}

		if ((xDifference < 0) && (yDifference == 0)) {
			return SpineConstants.WEST;
		}
		
		
		if ((yDifference > 0) && ((yDifference > xDifference)|| (xDifference == 0))) {
			return SpineConstants.NORTH;
		}

		if ((yDifference < 0) && ((yDifference > xDifference)|| (xDifference == 0))) {
			return SpineConstants.SOUTH;
		}

		if ((xDifference > 0) && ((yDifference < xDifference)|| (yDifference == 0))) {
			return SpineConstants.EAST;
		}

		if ((xDifference < 0) && ((yDifference < xDifference)|| (yDifference == 0))) {
			return SpineConstants.WEST;
		}
		return -1;
	}
	
	
	public static int directionFromPointToPoint(SpineLandPoint currentPoint, SpineLandPoint centroid) {
		int xDifference = centroid.getX() - currentPoint.getX();
		int yDifference = centroid.getY() - currentPoint.getY();

		if (yDifference > 0) {
			return SpineConstants.NORTH;
		}

		if (yDifference < 0) {
			return SpineConstants.SOUTH;
		}

		if (xDifference > 0) {
			return SpineConstants.EAST;
		}

		if (xDifference < 0) {
			return SpineConstants.WEST;
		}
		return -1;
	}

}
