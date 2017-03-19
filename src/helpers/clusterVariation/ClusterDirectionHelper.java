package helpers.clusterVariation;

import java.util.ArrayList;
import java.util.List;

import interfaces.clusterVariation.ClusterConstants;
import models.clusterVariation.ClusterLandPoint;

public class ClusterDirectionHelper {
	public static List<Integer> orthogonalDirections(final int direction) {
		List<Integer> directions = new ArrayList<>();
		switch (direction) {
		case ClusterConstants.NORTH:
			directions.add(ClusterConstants.EAST);
			directions.add(ClusterConstants.WEST);
			break;
		case ClusterConstants.SOUTH:
			directions.add(ClusterConstants.WEST);
			directions.add(ClusterConstants.EAST);
			break;
		case ClusterConstants.EAST:
			directions.add(ClusterConstants.NORTH);
			directions.add(ClusterConstants.SOUTH);
			break;
		case ClusterConstants.WEST:
			directions.add(ClusterConstants.SOUTH);
			directions.add(ClusterConstants.NORTH);
			break;
		default:
			directions.add(-1);
		}
		return directions;
	}

	public static int oppositeDirection(int direction) {
		switch (direction) {
		case ClusterConstants.NORTH:
			return ClusterConstants.SOUTH;
		case ClusterConstants.SOUTH:
			return ClusterConstants.NORTH;
		case ClusterConstants.EAST:
			return ClusterConstants.WEST;
		case ClusterConstants.WEST:
			return ClusterConstants.EAST;
		}
		return -1;
	}

	public static int orthogonalDirectionFromPointToPoint(ClusterLandPoint currentPoint, ClusterLandPoint centroid) {
		int xDifference = centroid.getX() - currentPoint.getX();
		int yDifference = centroid.getY() - currentPoint.getY();

		if ((yDifference > 0) && (yDifference > xDifference)) {
			return ClusterConstants.NORTH;
		}

		if ((yDifference < 0) && (yDifference > xDifference)) {
			return ClusterConstants.SOUTH;
		}

		if ((xDifference > 0) && (yDifference < xDifference)) {
			return ClusterConstants.EAST;
		}

		if ((xDifference < 0) && (yDifference < xDifference)) {
			return ClusterConstants.WEST;
		}
		return -1;
	}
	
	public static int orthogonalDirectionFromPointToPoint(Integer initialPoint, Integer finalPoint) {
		int [] xyI = ClusterMapHelper.breakKey(initialPoint);
		int [] xyF = ClusterMapHelper.breakKey(finalPoint);
		
		int xDifference = xyF[0] - xyI[0];
		int yDifference = xyF[1] - xyI[1];

		if ((yDifference > 0) && ((yDifference > xDifference)|| (xDifference == 0))) {
			return ClusterConstants.NORTH;
		}

		if ((yDifference < 0) && ((yDifference > xDifference)|| (xDifference == 0))) {
			return ClusterConstants.SOUTH;
		}

		if ((xDifference > 0) && ((yDifference < xDifference)|| (yDifference == 0))) {
			return ClusterConstants.EAST;
		}

		if ((xDifference < 0) && ((yDifference < xDifference)|| (yDifference == 0))) {
			return ClusterConstants.WEST;
		}
		return -1;
	}
	
	
	public static int directionFromPointToPoint(ClusterLandPoint currentPoint, ClusterLandPoint centroid) {
		int xDifference = centroid.getX() - currentPoint.getX();
		int yDifference = centroid.getY() - currentPoint.getY();

		if (yDifference > 0) {
			return ClusterConstants.NORTH;
		}

		if (yDifference < 0) {
			return ClusterConstants.SOUTH;
		}

		if (xDifference > 0) {
			return ClusterConstants.EAST;
		}

		if (xDifference < 0) {
			return ClusterConstants.WEST;
		}
		return -1;
	}
}