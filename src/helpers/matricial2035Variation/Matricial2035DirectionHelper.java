package helpers.matricial2035Variation;

import java.util.ArrayList;
import java.util.List;

import interfaces.matricial2035Variation.Matricial2035Constants;
import models.matricial2035Variation.Matricial2035LandPoint;

public class Matricial2035DirectionHelper {
	public static List<Integer> orthogonalDirections(final int direction) {
		List<Integer> directions = new ArrayList<>();
		switch (direction) {
		case Matricial2035Constants.NORTH:
			directions.add(Matricial2035Constants.EAST);
			directions.add(Matricial2035Constants.WEST);
			break;
		case Matricial2035Constants.SOUTH:
			directions.add(Matricial2035Constants.WEST);
			directions.add(Matricial2035Constants.EAST);
			break;
		case Matricial2035Constants.EAST:
			directions.add(Matricial2035Constants.NORTH);
			directions.add(Matricial2035Constants.SOUTH);
			break;
		case Matricial2035Constants.WEST:
			directions.add(Matricial2035Constants.SOUTH);
			directions.add(Matricial2035Constants.NORTH);
			break;
		default:
			directions.add(-1);
		}
		return directions;
	}

	public static int oppositeDirection(int direction) {
		switch (direction) {
		case Matricial2035Constants.NORTH:
			return Matricial2035Constants.SOUTH;
		case Matricial2035Constants.SOUTH:
			return Matricial2035Constants.NORTH;
		case Matricial2035Constants.EAST:
			return Matricial2035Constants.WEST;
		case Matricial2035Constants.WEST:
			return Matricial2035Constants.EAST;
		}
		return -1;
	}

	public static int orthogonalDirectionFromPointToPoint(Matricial2035LandPoint currentPoint,
			Matricial2035LandPoint centroid) {
		int xDifference = centroid.getX() - currentPoint.getX();
		int yDifference = centroid.getY() - currentPoint.getY();

		if ((yDifference > 0) && (yDifference > xDifference)) {
			return Matricial2035Constants.NORTH;
		}

		if ((yDifference < 0) && (yDifference > xDifference)) {
			return Matricial2035Constants.SOUTH;
		}

		if ((xDifference > 0) && (yDifference < xDifference)) {
			return Matricial2035Constants.EAST;
		}

		if ((xDifference < 0) && (yDifference < xDifference)) {
			return Matricial2035Constants.WEST;
		}
		return -1;
	}

	public static int orthogonalDirectionFromPointToPoint(Integer initialPoint, Integer finalPoint) {
		int[] xyI = Matricial2035MapHelper.breakKey(initialPoint);
		int[] xyF = Matricial2035MapHelper.breakKey(finalPoint);

		int xDifference = xyF[0] - xyI[0];
		int yDifference = xyF[1] - xyI[1];

		if ((xDifference == 0) && (yDifference < 0)) {
			return Matricial2035Constants.NORTH;
		}

		if ((yDifference < 0) && (xDifference == 0)) {
			return Matricial2035Constants.SOUTH;
		}

		if ((xDifference > 0) && (yDifference == 0)) {
			return Matricial2035Constants.EAST;
		}

		if ((xDifference < 0) && (yDifference == 0)) {
			return Matricial2035Constants.WEST;
		}

		if ((yDifference > 0) && ((yDifference > xDifference) || (xDifference == 0))) {
			return Matricial2035Constants.NORTH;
		}

		if ((yDifference < 0) && ((yDifference > xDifference) || (xDifference == 0))) {
			return Matricial2035Constants.SOUTH;
		}

		if ((xDifference > 0) && ((yDifference < xDifference) || (yDifference == 0))) {
			return Matricial2035Constants.EAST;
		}

		if ((xDifference < 0) && ((yDifference < xDifference) || (yDifference == 0))) {
			return Matricial2035Constants.WEST;
		}
		return -1;
	}

	public static int directionFromPointToPoint(Matricial2035LandPoint currentPoint, Matricial2035LandPoint centroid) {
		int xDifference = centroid.getX() - currentPoint.getX();
		int yDifference = centroid.getY() - currentPoint.getY();

		if (yDifference > 0) {
			return Matricial2035Constants.NORTH;
		}

		if (yDifference < 0) {
			return Matricial2035Constants.SOUTH;
		}

		if (xDifference > 0) {
			return Matricial2035Constants.EAST;
		}

		if (xDifference < 0) {
			return Matricial2035Constants.WEST;
		}
		return -1;
	}

	public static int perpendicularDirection(int[] initialXY, int[] centroid, int direction) {
		if ((direction == Matricial2035Constants.NORTH) || (direction == Matricial2035Constants.SOUTH)) {
			if (initialXY[0] < centroid[0]) {
				return Matricial2035Constants.EAST;
			} else {
				return Matricial2035Constants.WEST;
			}
		} else {
			if (initialXY[1] < centroid[1]) {
				return Matricial2035Constants.NORTH;
			} else {
				return Matricial2035Constants.SOUTH;
			}
		}
	}

	public static double parallelDirectionFromLargestLimit(Matricial2035LandPoint entryPoint,
			List<Matricial2035LandPoint> polygon) {
		int vertexesIndex = 0;
		// we find the vertex
		for (vertexesIndex = 0; vertexesIndex < polygon.size(); vertexesIndex++) {
			if (isFromRect(entryPoint, polygon.get(vertexesIndex), polygon.get((vertexesIndex + 1) % polygon.size()))) {
				break;
			}
		}
		int indexBehind = vertexesIndex;
		int indexBehindComplimentary = (indexBehind - 1) == -1 ? polygon.size() : indexBehind - 1;

		int indexInFront = vertexesIndex;
		int indexInFrontComplimentary = (indexInFront + 1) == polygon.size() ? 0 : indexInFront + 1;

		int indexSelected = -1;
		int indexSelectedComplimentary = -1;

		if (measureBorderSize(polygon.get(indexBehind), polygon.get(indexBehindComplimentary)) > measureBorderSize(
				polygon.get(indexInFront), polygon.get(indexInFrontComplimentary))) {
			indexSelected = indexBehind;
			indexSelectedComplimentary = indexBehindComplimentary;
		} else {
			indexSelected = indexInFront;
			indexSelectedComplimentary = indexInFrontComplimentary;
		}

		return findDirection(polygon.get(indexSelected), polygon.get(indexSelectedComplimentary));
	}

	private static double findDirection(Matricial2035LandPoint initialLandPoint,
			Matricial2035LandPoint finalLandPoint2) {
		
		return 0;
	}

	/*
	 * This are ordered from the direct opposites of the holder limit of the
	 * initial route
	 */
	private static double measureBorderSize(Matricial2035LandPoint initialPoint, Matricial2035LandPoint finalPoint) {
		int[] initialXY = Matricial2035MapHelper.breakKey(initialPoint.getId());
		int[] finalXY = Matricial2035MapHelper.breakKey(finalPoint.getId());
		return Math.sqrt(Math.pow(initialXY[0] - finalXY[0], 2) + Math.pow(initialXY[1] - finalXY[1], 2));
	}

	private static boolean isFromRect(Matricial2035LandPoint entryPoint, Matricial2035LandPoint matricial2035LandPoint,
			Matricial2035LandPoint matricial2035LandPoint2) {
		// TODO Auto-generated method stub
		return false;
	}

	public static void rotatePoints(double directionAngle, Matricial2035LandPoint matricial2035LandPoint,
			List<Matricial2035LandPoint> polygon) {
		// TODO Auto-generated method stub

	}
}