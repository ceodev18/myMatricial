package algorithm.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import helpers.base.DirectionHelper;
import interfaces.Constants;
import models.base.LandMap;
import models.base.LandPoint;

public class LSystemRouteAlgorithm {
	public static LandMap landMap;

	
	/*
	 * variables: block_vertical block_horizontal road_vertical road_horizontal
	 * start: block_vertical 
	 * rules: 
	 * (block_vertical → block_horizontal road_vertical block_horizontal)                              
	 * (block_horizontal → block_vertical road_h
	 * horizontal block_vertical)
	 */

	public static void createRoute(int entryPointId, int direction, int type, int maxDeep, int currentDepth) {
		//we initialize with the main route
		List<Integer> randomPointInLine = bisect(entryPointId, direction, Constants.AVENUE_BRANCH, currentDepth);
		List<Integer> orthogonalDirection = DirectionHelper.randomOrthogonalDirection(direction); 
		
		currentDepth++;
		grow(randomPointInLine.get(0), orthogonalDirection.get(0), maxDeep, currentDepth, Constants.HORIZONTAL);
		grow(randomPointInLine.get(1), orthogonalDirection.get(1), maxDeep, currentDepth, Constants.HORIZONTAL);
	}

	private static void grow(Integer pointInLine, Integer direction, int maxDeep, int currentDepth, int iterationType) {		
		if (currentDepth >= maxDeep) return;
		List<Integer> randomPointInLine = bisect(pointInLine, direction, Constants.AVENUE_BRANCH, currentDepth);
		List<Integer> orthogonalDirection = DirectionHelper.randomOrthogonalDirection(direction); 
		enroute(pointInLine, direction, currentDepth);
		
		if(randomPointInLine.get(0)== -1 || orthogonalDirection.get(0)== -1) return;
		currentDepth++;
		if (iterationType == Constants.HORIZONTAL){
			grow(randomPointInLine.get(0), orthogonalDirection.get(0), maxDeep, currentDepth, Constants.VERTICAL);
			grow(randomPointInLine.get(1), orthogonalDirection.get(1), maxDeep, currentDepth, Constants.VERTICAL);
		}else {
			grow(randomPointInLine.get(0), orthogonalDirection.get(0), maxDeep, currentDepth, Constants.HORIZONTAL);
			grow(randomPointInLine.get(1), orthogonalDirection.get(1), maxDeep, currentDepth, Constants.HORIZONTAL);
		}
	}

	private static void enroute(int randomPointInLineId, int redirection, int currentDepth) {
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
					landMap.pointIsOfSubRoute(nextPointId, true, currentDepth);
					if (currentPoint == null) {
						return;
					}
				} else {
					return;
				}
			}
		}
	}

	private static List<Integer> bisect(int entryPointId, int direction, int type, int currentDepth) {
		List<Integer> points = new ArrayList<>();
		List<Integer> result = new ArrayList<>();
		LandPoint currentPoint = landMap.findPoint(entryPointId, direction);
		points.add(entryPointId);
		if (currentPoint == null){
			result.add(-1);
			return result;// TODO means is already taken, it should verify wether by
						// an avenue or a subroute. For now nothing
		}
		
		landMap.pointIsOfRoute(entryPointId, true, currentDepth);
		while (!currentPoint.isMapLimit(direction)) {
			int nextPointId = currentPoint.findNeighbour(direction);
			if (nextPointId != -1) {// means it is already occupied
				points.add(nextPointId);
				currentPoint = landMap.findPoint(nextPointId, direction, type);
				landMap.pointIsOfRoute(nextPointId, true, currentDepth);
				if (currentPoint == null) {
					result.add(-1);
					return result;
				}
			} else {
				result.add(-1);
				return result;
			}
		}

		Random random = new Random();
		int bound = (points.size()/2) - 1;
		if (bound <= 0){
			result.add(-1);
			return result;
		}
		
		int upperHalf = random.nextInt(bound) + points.size()/2;
		int lowerHalf = random.nextInt(bound);
		result.add(points.get(upperHalf));
		result.add(points.get(lowerHalf));
		points.clear();
		points = null;
		System.gc(); 
		return result;
	}
	
	/*
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
	}*/
}