package algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import helpers.DirectionHelper;
import interfaces.Constants;
import models.LandMap;
import models.LandPoint;

public class LSystemRouteAlgorithm {
	public static LandMap landMap;

	/*
	 * variables: block_vertical block_horizontal road_vertical road_horizontal
	 * start: block_vertical 
	 * rules: 
	 * (block_vertical → block_horizontal road_vertical block_horizontal)                              
	 * (block_horizontal → block_vertical road_horizontal block_vertical)
	 */

	public static void createRoute(int entryPointId, int direction, int type, int maxDeep, int currentDeep) {
		int redirection = -1;
		int randomPointInLineId = -1;
		int secondRandomPointInLineId = -1;
		int roadPointLineId = -1;
		for (int i = currentDeep; i < maxDeep; i++) {
			if (i % 2 == 0) {// block vertical
				//block vertical
				randomPointInLineId = bisect(entryPointId, direction, Constants.AVENUE_BRANCH); // block horizontal
				//road horizontal
				redirection = DirectionHelper.randomOrthogonalDirection(direction); 
				roadPointLineId = enroute(randomPointInLineId, redirection);
				//2nd block vertical
				secondRandomPointInLineId = bisect(roadPointLineId, direction, Constants.AVENUE_BRANCH); // block horizontal
			} else { //horizontal
				randomPointInLineId = randomPoint(entryPointId, direction, type);
				direction = DirectionHelper.oppositeDirection(redirection);
				entryPointId = bisect(randomPointInLineId, direction, Constants.AVENUE_BRANCH);
				redirection = DirectionHelper.randomOrthogonalDirection(direction);
				enroute(entryPointId, direction);
			}
		}
	}

	private static void enroute(int randomPointInLineId, int redirection) {
		int distance= landMap.findDistanceToLimit(randomPointInLineId, redirection);
		
		if (distance == 0){
			return;
		} else {
			Random random = new Random();
			int trueDistance = random.nextInt(distance);
			LandPoint currentPoint = landMap.findPoint(randomPointInLineId, redirection);
			for(int i = 0; i< trueDistance; i++) {
				int nextPointId = currentPoint.findNeighbour(redirection);
				if (nextPointId != -1) {// means it is already occupied
					currentPoint = landMap.findPoint(nextPointId, redirection, Constants.ALLEY_BRANCH);
					landMap.pointIsOfSubRoute(nextPointId, true);
					if (currentPoint == null) {
						return;
					}
				} else {
					return;
				}
			}
		}
	}

	private static int bisect(int entryPointId, int direction, int type) {
		List<Integer> points = new ArrayList<>();
		LandPoint currentPoint = landMap.findPoint(entryPointId, direction);
		points.add(entryPointId);
		if (currentPoint == null)
			return -1;// TODO means is already taken, it should verify wether by
						// an avenue or a subroute. For now nothing
		landMap.pointIsOfRoute(entryPointId, true);

		while (!currentPoint.isMapLimit(direction)) {
			int nextPointId = currentPoint.findNeighbour(direction);
			if (nextPointId != -1) {// means it is already occupied
				points.add(nextPointId);
				currentPoint = landMap.findPoint(nextPointId, direction, type);
				landMap.pointIsOfRoute(nextPointId, true);
				if (currentPoint == null) {
					return -1;
				}
			} else {
				return -1;
			}
		}

		Random random = new Random();
		return points.get(random.nextInt(points.size() - 1));
	}
	
	private static int randomPoint(int entryPointId, int direction, int type) {
		List<Integer> points = new ArrayList<>();
		LandPoint currentPoint = landMap.findPoint(entryPointId, direction);
		points.add(entryPointId);
		if (currentPoint == null)
			return -1;
		while (!currentPoint.isMapLimit(direction)) {
			int nextPointId = currentPoint.findNeighbour(direction);
			if (nextPointId != -1) {
				points.add(nextPointId);
				currentPoint = landMap.findPoint(nextPointId, direction);
				if (currentPoint == null) {
					return -1;
				}
			} else {
				return -1;
			}
		}

		Random random = new Random();
		return points.get(random.nextInt(points.size() - 1));
	}
}