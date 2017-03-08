package algorithm.clusterVariation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import helpers.base.DirectionHelper;
import helpers.base.MapHelper;
import helpers.clusterVariation.CDirectionHelper;
import interfaces.ClusterConfiguration;
import interfaces.Constants;
import models.clusterVariation.ClusterLandMap;
import models.clusterVariation.ClusterLandPoint;
import models.clusterVariation.ClusterLandRoute;
import models.clusterVariation.ClusterPolygon;

public class LSystemClusterAlgorithm {
	public static ClusterLandMap landMap;
	public static List<ClusterPolygon> polygons = new ArrayList<>();

	public static void createRoute(int entryPointId, int direction, int branchType) {
		ClusterLandPoint currentPoint = landMap.findPoint(entryPointId);
		// due to the vastness of the maps there is a error margin to cover
		ClusterLandRoute clusterLandRoute = new ClusterLandRoute();
		clusterLandRoute.setDirection(direction);
		clusterLandRoute.setInitialPointId(entryPointId);

		int extension = 0;
		if (branchType == ClusterConfiguration.ARTERIAL_BRANCH) {
			while (currentPoint.isMapLimit(direction)) {
				int nextPointId = currentPoint.findNeighbour(direction);
				currentPoint = landMap.findPoint(nextPointId);
				extension++;
			}
		} else {
			while (!currentPoint.getType().equals(ClusterConfiguration.EMPTY)) {
				int nextPointId = currentPoint.findNeighbour(direction);
				currentPoint = landMap.findPoint(nextPointId);
				extension++;
			}
		}

		boolean first = true;
		while (!currentPoint.isMapLimit(direction)) {
			if (first) {
				first = false;
			} else {
				if (!currentPoint.getType().equals(ClusterConfiguration.EMPTY)) {
				} else {
					landMap.markVariation(currentPoint.getId(), branchType, ClusterConfiguration.TYPE_NO_NODE);
				}
			}

			List<Integer> orthogonalDirections = CDirectionHelper.orthogonalDirections(direction);
			extendRouteToTrueSize(currentPoint, orthogonalDirections.get(0), branchType, true);
			extendRouteToTrueSize(currentPoint, orthogonalDirections.get(1), branchType, false);
			int nextPointId = currentPoint.findNeighbour(direction);
			currentPoint = landMap.findPoint(nextPointId);
			extension++;
		}
		landMap.markVariation(currentPoint.getId(), ClusterConfiguration.NODE, ClusterConfiguration.TYPE_OUTER_NODE);

		clusterLandRoute.setExtension(extension);
		clusterLandRoute.setFinalPointId(currentPoint.getId());
		clusterLandRoute.setType(branchType);
		landMap.getLandRoutes().add(clusterLandRoute);
	}

	private static void extendRouteToTrueSize(ClusterLandPoint currentPoint, Integer direction, int branchType,
			boolean imperfect) {
		int extension = 0;
		switch (branchType) {
		case ClusterConfiguration.ARTERIAL_BRANCH:
			extension = (ClusterConfiguration.ARTERIAL_BRANCH_SIZE) / 2 - (imperfect ? 1 : 0);
			break;
		case ClusterConfiguration.COLLECTOR_BRANCH:// As it is taken into
													// account during the route
													// separation only left
			extension = (ClusterConfiguration.COLLECTOR_BRANCH_SIZE - 1) * (imperfect ? 1 : 0);
			break;
		case ClusterConfiguration.LOCAL_BRANCH:
			extension = ClusterConfiguration.LOCAL_BRANCH_SIZE / 2 - (imperfect ? 1 : 0);
			break;
		case ClusterConfiguration.WALK_BRANCH:
			extension = ClusterConfiguration.WALK_BRANCH_SIZE / 2 - (imperfect ? 1 : 0);
			break;
		}

		int nextPointId = currentPoint.findNeighbour(direction);
		currentPoint = landMap.findPoint(nextPointId);
		while (!currentPoint.isMapLimit(direction) && (extension > 0)) {
			if (currentPoint.getType().equals(ClusterConfiguration.EMPTY)) {
				landMap.markVariation(currentPoint.getId(), branchType, ClusterConfiguration.TYPE_NO_NODE);
			} else {
				break;
			}
			nextPointId = currentPoint.findNeighbour(direction);
			currentPoint = landMap.findPoint(nextPointId);
			extension--;
		}

		if (!currentPoint.isMapLimit(direction) && currentPoint.getType().equals(ClusterConfiguration.EMPTY)) {
			landMap.markVariation(currentPoint.getId(), ClusterConfiguration.NODE,
					ClusterConfiguration.TYPE_INNER_NODE);
		}
	}

	public static void clusterize() {
		// 1. we need to now the main route size
		ClusterLandRoute mainRoute = landMap.getLandRoutes().get(0);
		int numClusters = mainRoute.getExtension() / ClusterConfiguration.BASE_CLUSTER_SIZE;
		int entryPointId = mainRoute.getInitialPointId();

		// Once the collector branches are created we need to create the non
		// collector running orthogonal to the main
		List<Integer> orthogonalDirections = CDirectionHelper.orthogonalDirections(mainRoute.getDirection());
		for (int i = 0; i < numClusters; i++) {
			entryPointId = MapHelper.moveKeyByOffsetAndDirection(entryPointId,
					ClusterConfiguration.BASE_CLUSTER_SIZE + ClusterConfiguration.COLLECTOR_BRANCH_SIZE,
					mainRoute.getDirection());
			if (entryPointId != -1) {
				createRoute(entryPointId, orthogonalDirections.get(0), ClusterConfiguration.COLLECTOR_BRANCH);
				createRoute(entryPointId, orthogonalDirections.get(1), ClusterConfiguration.COLLECTOR_BRANCH);
			} else {
				break;
			}
		}

		int upperParallelId = mainRoute.getInitialPointId();
		int current = 0;
		while (true) {
			if (current % 2 == 0) {
				// the parallel should be houseLength (as 8 is already used
				// should be somewhere between 12 and more) and then road
				upperParallelId = MapHelper.moveKeyByOffsetAndDirection(upperParallelId,
						2 * ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE, orthogonalDirections.get(0));
			} else {
				// then BASE_CLUSTER_SIZE
				upperParallelId = MapHelper.moveKeyByOffsetAndDirection(upperParallelId,
						ClusterConfiguration.BASE_CLUSTER_SIZE, orthogonalDirections.get(0));
			}
			if (!landMap.landPointisOnMap(upperParallelId))
				break;
			createRoute(upperParallelId, mainRoute.getDirection(), ClusterConfiguration.LOCAL_BRANCH);
			current++;
		}

		int lowerParallelId = mainRoute.getInitialPointId();
		current = 0;
		while (true) {
			if (current % 2 == 0) {
				lowerParallelId = MapHelper.moveKeyByOffsetAndDirection(lowerParallelId,
						2 * ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE, orthogonalDirections.get(1));
			} else {
				lowerParallelId = MapHelper.moveKeyByOffsetAndDirection(lowerParallelId,
						ClusterConfiguration.BASE_CLUSTER_SIZE, orthogonalDirections.get(1));
			}
			if (!landMap.landPointisOnMap(upperParallelId))
				break;
			createRoute(lowerParallelId, mainRoute.getDirection(), ClusterConfiguration.LOCAL_BRANCH);
			current++;
		}
		// We define the cluster areas.
		defineFigures();
	}

	private static void defineFigures() {
		MapHelper.orderNodes(landMap.getNodes());
		Map<Integer, List<Integer>> mappedPoints = new HashMap<>();
		for (int i = 0; i < landMap.getNodes().size(); i++) {
			mappedPoints.put(landMap.getNodes().get(i), new ArrayList<>());
		}

		// we put every point on the map and find their associated nodes in all
		// directions
		for (int i = 0; i < landMap.getNodes().size(); i++) {
			int pointId = landMap.getNodes().get(i);
			int[] xy = MapHelper.breakKey(pointId);
			Iterator<Entry<Integer, List<Integer>>> it = mappedPoints.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, List<Integer>> pair = (Map.Entry<Integer, List<Integer>>) it.next();
				int[] xyC = MapHelper.breakKey(pair.getKey());
				if (pointId != pair.getKey().intValue() && (xy[0] == xyC[0] || xy[1] == xyC[1])) {
					mappedPoints.get(pointId).add(pair.getKey());
				}
			}
		}

		// we remove the furthest nodes in every direction so that we have one
		// closest on every direction
		Iterator<Entry<Integer, List<Integer>>> it = mappedPoints.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, List<Integer>> pair = (Map.Entry<Integer, List<Integer>>) it.next();
			int closeNorth = 0, closeSouth = 0, closeEast = 0, closeWest = 0;
			for (int i = 0; i < pair.getValue().size(); i++) {
				int direction = CDirectionHelper.directionFromPointToPoint(landMap.getLandPoint(pair.getKey()),
						landMap.getLandPoint(pair.getValue().get(i)));
				switch (direction) {
				case Constants.SOUTH:
					if (closeSouth == 0) {
						closeSouth = pair.getValue().get(i);
					} else {
						int[] currentSouth = MapHelper.breakKey(closeSouth);
						int[] possibleSouth = MapHelper.breakKey(pair.getValue().get(i));
						if (currentSouth[1] < possibleSouth[1]) {
							closeSouth = pair.getValue().get(i);
						}
					}
					break;
				case Constants.WEST:
					if (closeWest == 0) {
						closeWest = pair.getValue().get(i);
					} else {
						int[] currentWest = MapHelper.breakKey(closeWest);
						int[] possibleWest = MapHelper.breakKey(pair.getValue().get(i));
						if (currentWest[0] < possibleWest[0]) {
							closeWest = pair.getValue().get(i);
						}
					}
					break;
				case Constants.NORTH:
					if (closeNorth == 0) {
						closeNorth = pair.getValue().get(i);
					} else {
						int[] currentNorth = MapHelper.breakKey(closeNorth);
						int[] possibleNorth = MapHelper.breakKey(pair.getValue().get(i));
						if (currentNorth[1] > possibleNorth[1]) {
							closeNorth = pair.getValue().get(i);
						}
					}
					break;
				case Constants.EAST:
					if (closeEast == 0) {
						closeEast = pair.getValue().get(i);
					} else {
						int[] currentEast = MapHelper.breakKey(closeEast);
						int[] possibleEast = MapHelper.breakKey(pair.getValue().get(i));
						if (currentEast[0] > possibleEast[0]) {
							closeEast = pair.getValue().get(i);
						}
					}
					break;
				}
			}
			// N W S E
			List<Integer> directions = new ArrayList<>();
			directions.add(closeNorth);
			directions.add(closeWest);
			directions.add(closeSouth);
			directions.add(closeEast);
			mappedPoints.put(pair.getKey(), directions);
		}

		// finally we combine to create rectangles an
		recombine(mappedPoints);
	}

	// List<ClusterPolygon>
	private static void recombine(Map<Integer, List<Integer>> mappedPoints) {
		Iterator<Entry<Integer, List<Integer>>> it = mappedPoints.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, List<Integer>> pair = (Map.Entry<Integer, List<Integer>>) it.next();
			int[] polygonVertex = new int[4];
			polygonVertex[0] = pair.getKey();
			int level = 1;
			for (int i = 0; i < 4; i++) {
				switch (i) {// N W S E
				case 0:
					if (pair.getValue().get(0) != 0) {
						recursiveRecombination(mappedPoints, pair.getValue().get(0), polygonVertex, level,
								Constants.NORTH, Constants.NORTH, -1);
					}
					break;
				case 1:
					if (pair.getValue().get(1) != 0) {
						recursiveRecombination(mappedPoints, pair.getValue().get(1), polygonVertex, level,
								Constants.WEST, Constants.WEST, -1);
					}
					break;
				case 2:
					if (pair.getValue().get(2) != 0) {
						recursiveRecombination(mappedPoints, pair.getValue().get(2), polygonVertex, level,
								Constants.SOUTH, Constants.SOUTH, -1);
					}
					break;
				case 3:
					if (pair.getValue().get(3) != 0) {
						recursiveRecombination(mappedPoints, pair.getValue().get(3), polygonVertex, level,
								Constants.EAST, Constants.EAST, -1);
					}
					break;
				}
			}
		}
		joinPolygons();
	}

	private static void joinPolygons() {
		for (int i = 0; i < polygons.size() - 1; i++) {
			if (polygons.get(i).getType() == ClusterConfiguration.CLUSTER_TYPE_TRIANGLE) {
				for (int j = i + 1; j < polygons.size(); j++) {
					if (polygons.get(j).getType() == ClusterConfiguration.CLUSTER_TYPE_TRIANGLE) {
						if (polygons.get(i).sharesDirectionAndSide(polygons.get(j))) {
							polygons.get(i).fusion(polygons.get(j));
							polygons.remove(j);
							break;
						}
					}
				}
			}
		}

	}

	private static void recursiveRecombination(Map<Integer, List<Integer>> mappedPoints, int currentVertex,
			int[] polygonVertex, int level, int direction, int firstMoveRestriction, int secondMoveRestriction) {
		if (level == 5) {
			return;
		} else {
			polygonVertex[level] = currentVertex;
			level += 1;

			if (level == 4) {
				ClusterPolygon clusterPolygon = new ClusterPolygon(polygonVertex, 4);
				for (int i = 0; i < polygons.size(); i++) {
					if (polygons.get(i).same(clusterPolygon))
						return;
				}
				polygons.add(clusterPolygon);
				return;
			}
			List<Integer> following = mappedPoints.get(currentVertex);
			if (level == 3) {
				int dir = CDirectionHelper.oppositeDirection(firstMoveRestriction);
				if (dir == Constants.NORTH) {
					if ((following.get(0) != 0)) {
						recursiveRecombination(mappedPoints, following.get(0), polygonVertex, level, Constants.NORTH,
								firstMoveRestriction, secondMoveRestriction);
					} else {
						ClusterPolygon clusterPolygon = new ClusterPolygon(polygonVertex, 3);
						for (int i = 0; i < polygons.size(); i++) {
							if (polygons.get(i).same(clusterPolygon))
								return;
						}
						polygons.add(clusterPolygon);
						return;
					}
				}

				if (dir == Constants.WEST) {
					if (following.get(1) != 0) {
						recursiveRecombination(mappedPoints, following.get(1), polygonVertex, level, Constants.WEST,
								firstMoveRestriction, secondMoveRestriction);
					} else {
						ClusterPolygon clusterPolygon = new ClusterPolygon(polygonVertex, 3);
						for (int i = 0; i < polygons.size(); i++) {
							if (polygons.get(i).same(clusterPolygon))
								return;
						}
						polygons.add(clusterPolygon);
						return;
					}
				}

				if (dir == Constants.SOUTH) {
					if (following.get(2) != 0) {
						recursiveRecombination(mappedPoints, following.get(2), polygonVertex, level, Constants.SOUTH,
								firstMoveRestriction, secondMoveRestriction);
					} else {
						ClusterPolygon clusterPolygon = new ClusterPolygon(polygonVertex, 3);
						for (int i = 0; i < polygons.size(); i++) {
							if (polygons.get(i).same(clusterPolygon))
								return;
						}
						polygons.add(clusterPolygon);
						return;
					}
				}

				if (dir == Constants.EAST) {
					if ((following.get(3) != 0)) {
						recursiveRecombination(mappedPoints, following.get(3), polygonVertex, level, Constants.EAST,
								firstMoveRestriction, secondMoveRestriction);
					} else {
						ClusterPolygon clusterPolygon = new ClusterPolygon(polygonVertex, 3);
						for (int i = 0; i < polygons.size(); i++) {
							if (polygons.get(i).same(clusterPolygon))
								return;
						}
						polygons.add(clusterPolygon);
						return;
					}
				}
			} else if (level == 2) {
				List<Integer> directions = DirectionHelper.randomOrthogonalDirection(direction);
				if ((following.get(0) != 0)
						&& (directions.get(0) == Constants.NORTH || directions.get(1) == Constants.NORTH)) {
					recursiveRecombination(mappedPoints, following.get(0), polygonVertex, level, Constants.NORTH,
							firstMoveRestriction, Constants.NORTH);
				}
				if ((following.get(1) != 0)
						&& (directions.get(0) == Constants.WEST || directions.get(1) == Constants.WEST)) {
					recursiveRecombination(mappedPoints, following.get(1), polygonVertex, level, Constants.WEST,
							firstMoveRestriction, Constants.WEST);
				}
				if ((following.get(2) != 0)
						&& (directions.get(0) == Constants.SOUTH || directions.get(1) == Constants.SOUTH)) {
					recursiveRecombination(mappedPoints, following.get(2), polygonVertex, level, Constants.SOUTH,
							firstMoveRestriction, Constants.SOUTH);
				}
				if ((following.get(3) != 0)
						&& (directions.get(0) == Constants.EAST || directions.get(1) == Constants.EAST)) {
					recursiveRecombination(mappedPoints, following.get(3), polygonVertex, level, Constants.EAST,
							firstMoveRestriction, Constants.EAST);
				}
			}
		}
	}
}