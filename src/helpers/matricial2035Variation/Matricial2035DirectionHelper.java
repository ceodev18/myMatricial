package helpers.matricial2035Variation;

import java.util.ArrayList;
import java.util.List;

import interfaces.matricial2035Variation.Matricial2035Constants;
import models.matricial2035Variation.Matricial2035LandPoint;
import models.matricial2035Variation.Matricial2035RotationPoint;

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

	// I need to establish the point as a rotator index. Think of it as a
	// constellation
	public static Matricial2035RotationPoint getRotationPoint(Matricial2035LandPoint entryPoint,
			List<Matricial2035LandPoint> polygon) {
		Matricial2035RotationPoint matricial2035RotationPoint = new Matricial2035RotationPoint();
		int vertexesIndex = 0;
		// we find the vertex
		for (vertexesIndex = 0; vertexesIndex < polygon.size(); vertexesIndex++) {
			if (isFromRect(entryPoint, polygon.get(vertexesIndex), polygon.get((vertexesIndex + 1) % polygon.size()))) {
				break;
			}
		}
		int indexBehind = vertexesIndex;
		int indexBehindComplimentary = (indexBehind - 1) == -1 ? polygon.size() : indexBehind - 1;
		int indexInFront = (vertexesIndex + 1) % polygon.size();
		int indexInFrontComplimentary = (indexInFront + 1) % polygon.size();
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

		matricial2035RotationPoint.setNodeIndex(indexSelected);
		matricial2035RotationPoint.setRotationPointId(polygon.get(indexSelected).getId());
		matricial2035RotationPoint
				.setAngle(findAngle(polygon.get(indexSelected), polygon.get(indexSelectedComplimentary)));
		return matricial2035RotationPoint;
	}

	private static double findAngle(Matricial2035LandPoint initialLandPoint, Matricial2035LandPoint finalLandPoint) {
		int[] axisXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(
				Matricial2035MapHelper.breakKey(initialLandPoint.getId()), 10, Matricial2035Constants.EAST);
		int[] initialXY = Matricial2035MapHelper.breakKey(initialLandPoint.getId());
		int[] finalXY = Matricial2035MapHelper.breakKey(finalLandPoint.getId());

		double angle1 = Math.atan2(finalXY[1] - initialXY[1], finalXY[0] - initialXY[0]);
		double angle2 = Math.atan2(axisXY[1] - initialXY[1], axisXY[0] - initialXY[0]);

		float calculatedAngle = (float) Math.toDegrees(angle1 - angle2);
		if (calculatedAngle < 0)
			calculatedAngle += 360;

		return calculatedAngle;
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

	/*
	 * Simple verification that the current analyzed rect is from such point
	 */
	private static boolean isFromRect(Matricial2035LandPoint entryPoint, Matricial2035LandPoint initialLandPoint,
			Matricial2035LandPoint finalLandPoint2) {
		int[] initialXY = Matricial2035MapHelper.breakKey(initialLandPoint.getId());
		int[] finalXY = Matricial2035MapHelper.breakKey(finalLandPoint2.getId());
		int[] entryXY = Matricial2035MapHelper.breakKey(entryPoint.getId());
		// 3 cases, if x == x, y == y or xy gradient in which case it is
		// necessary to calculate
		if ((initialXY[0] == finalXY[0])) {
			return initialXY[0] == finalXY[0];
		} else if ((initialXY[0] == finalXY[0])) {
			return initialXY[1] == finalXY[1];
		} else {// Needs to be done with a tolerance ratio, as 2 curves can be
				// equal and not necessarily correct
			double normalGradient = (finalXY[1] - initialXY[1]) * 1.0 / (finalXY[0] - initialXY[0]);
			double entryGradient = (entryXY[1] - initialXY[1]) * 1.0 / (entryXY[0] - initialXY[0]);
			if (Math.abs(normalGradient - entryGradient) < 0.05) {
				return true;
			}
		}
		return false;
	}

	public static void resize(List<Matricial2035LandPoint> polygon, int[] sizes) {
		for (int i = 0; i < polygon.size(); i++) {
			int[] currentXY = Matricial2035MapHelper.breakKey(polygon.get(i).getId());
			if (sizes[0] < currentXY[0]) {
				sizes[0] = currentXY[0];
			}
			if (sizes[1] < currentXY[1]) {
				sizes[1] = currentXY[1];
			}
		}
	}

	public static void translate(List<Matricial2035LandPoint> polygon, List<Matricial2035LandPoint> entryPoints,
			int[] xy) {
		xy[0] = 0;
		xy[1] = 0;

		for (int i = 0; i < polygon.size(); i++) {
			if (polygon.get(i).getX() < xy[0]) {
				xy[0] = polygon.get(i).getX();
			}

			if (polygon.get(i).getY() < xy[1]) {
				xy[1] = polygon.get(i).getY();
			}
		}
		
		if (entryPoints.get(0).getX() < xy[0]) {
			xy[0] = entryPoints.get(0).getX();
		}

		if (entryPoints.get(0).getY() < xy[1]) {
			xy[1] = entryPoints.get(0).getY();
		}

		if (xy[0] != 0) {
			for (int i = 0; i < polygon.size(); i++) {
				polygon.get(i).setX(polygon.get(i).getX() - xy[0]);
			}
		}

		if (xy[1] != 0) {
			for (int i = 0; i < polygon.size(); i++) {
				polygon.get(i).setY(polygon.get(i).getY() - xy[1]);
			}
		}
	}
	//TODO still not giving feasable answers
}