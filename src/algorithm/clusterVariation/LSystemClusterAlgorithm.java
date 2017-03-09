package algorithm.clusterVariation;

import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.List;

import helpers.base.MapHelper;
import helpers.clusterVariation.ClusterDirectionHelper;
import interfaces.ClusterConfiguration;
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
		List<Integer> orthogonalDirections = ClusterDirectionHelper.orthogonalDirections(direction);

		int extension = 0;
		if (branchType == ClusterConfiguration.ARTERIAL_BRANCH) {
			while (currentPoint.isMapLimit(direction)) {
				int nextPointId = currentPoint.findNeighbour(direction);
				currentPoint = landMap.findPoint(nextPointId);
				extension++;
			}
		} else {
			while (!currentPoint.getType().equals(ClusterConfiguration.NODE_MARK)) {
				int nextPointId = currentPoint.findNeighbour(direction);
				currentPoint = landMap.findPoint(nextPointId);
				extension++;
			}

			extendRouteToTrueSize(currentPoint, orthogonalDirections.get(0), branchType, true);
			extendRouteToTrueSize(currentPoint, orthogonalDirections.get(1), branchType, false);
		}

		while (!currentPoint.isMapLimit(direction)) {
			if (currentPoint.getType().equals(ClusterConfiguration.EMPTY_MARK)) {
				landMap.markVariation(currentPoint.getId(), branchType, ClusterConfiguration.TYPE_NO_NODE);
			}
			extendRouteToTrueSize(currentPoint, orthogonalDirections.get(0), branchType, true);
			extendRouteToTrueSize(currentPoint, orthogonalDirections.get(1), branchType, false);
			int nextPointId = currentPoint.findNeighbour(direction);
			currentPoint = landMap.findPoint(nextPointId);
			extension++;
		}

		clusterLandRoute.setExtension(extension);
		clusterLandRoute.setFinalPointId(currentPoint.getId());
		clusterLandRoute.setType(branchType);
		landMap.getLandRoutes().add(clusterLandRoute);
	}

	public static void createTransversalRoute(int entryPointId, int direction, int branchType) {
		ClusterLandPoint currentPoint = landMap.findPoint(entryPointId);
		// due to the vastness of the maps there is a error margin to cover
		ClusterLandRoute clusterLandRoute = new ClusterLandRoute();
		clusterLandRoute.setDirection(direction);
		clusterLandRoute.setInitialPointId(entryPointId);

		int extension = 0;
		while (currentPoint.isMapLimit(direction)) {
			int nextPointId = currentPoint.findNeighbour(direction);
			currentPoint = landMap.findPoint(nextPointId);
			extension++;
		}

		List<Integer> orthogonalDirections = ClusterDirectionHelper.orthogonalDirections(direction);
		while (!currentPoint.isMapLimit(direction)) {
			if (currentPoint.getType().equals(ClusterConfiguration.EMPTY_MARK)) {
				landMap.markVariation(currentPoint.getId(), branchType, ClusterConfiguration.TYPE_NO_NODE);
				extendRouteToTrueSize(currentPoint, orthogonalDirections.get(0), branchType, true);
				extendRouteToTrueSize(currentPoint, orthogonalDirections.get(1), branchType, false);
			} else if (currentPoint.getType().equals(ClusterConfiguration.NODE_MARK)) {
				extendRouteToTrueSize(currentPoint, orthogonalDirections.get(0), branchType, true);
				extendRouteToTrueSize(currentPoint, orthogonalDirections.get(1), branchType, false);
			}
			int nextPointId = currentPoint.findNeighbour(direction);
			currentPoint = landMap.findPoint(nextPointId);
			extension++;
		}

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
			extension = (ClusterConfiguration.ARTERIAL_BRANCH_SIZE) - 1;
			break;
		case ClusterConfiguration.COLLECTOR_BRANCH:
			extension = ClusterConfiguration.COLLECTOR_BRANCH_SIZE - 1;
			break;
		case ClusterConfiguration.LOCAL_BRANCH:
			extension = ClusterConfiguration.LOCAL_BRANCH_SIZE - 1;
			break;
		case ClusterConfiguration.WALK_BRANCH:
			extension = ClusterConfiguration.WALK_BRANCH_SIZE - 1;
			break;
		}

		if (!imperfect) {
			extension = 0;
		}

		if (currentPoint.getType().equals(ClusterConfiguration.NODE_MARK)) {
			landMap.markVariation(currentPoint.getId(), branchType, ClusterConfiguration.TYPE_NO_NODE);
		}

		int nextPointId = currentPoint.findNeighbour(direction);
		currentPoint = landMap.findPoint(nextPointId);
		while (!currentPoint.isMapLimit(direction) && (extension > 0)) {
			landMap.markVariation(currentPoint.getId(), branchType, ClusterConfiguration.TYPE_NO_NODE);
			nextPointId = currentPoint.findNeighbour(direction);
			currentPoint = landMap.findPoint(nextPointId);
			extension--;
		}

		if (!currentPoint.isMapLimit(direction) && currentPoint.getType().equals(ClusterConfiguration.EMPTY_MARK)) {
			landMap.markVariation(currentPoint.getId(), ClusterConfiguration.NODE,
					ClusterConfiguration.TYPE_INNER_NODE);
		}
	}

	public static void clusterize() {
		// 1. we need to now the main route size
		ClusterLandRoute mainRoute = landMap.getLandRoutes().get(0);
		int entryPointId = mainRoute.getInitialPointId();
		// Once the collector branches are created we need to create the non
		// collector running orthogonal to the main
		List<Integer> orthogonalDirections = ClusterDirectionHelper.orthogonalDirections(mainRoute.getDirection());
		while (true) {
			entryPointId = MapHelper.moveKeyByOffsetAndDirection(entryPointId,
					ClusterConfiguration.BASE_CLUSTER_SIZE + ClusterConfiguration.COLLECTOR_BRANCH_SIZE,
					mainRoute.getDirection());
			if (landMap.landPointisOnMap(entryPointId) && landMap.intersectMainRoute(entryPointId)) {
				createRoute(entryPointId, orthogonalDirections.get(0), ClusterConfiguration.COLLECTOR_BRANCH);
				createRoute(entryPointId, orthogonalDirections.get(1), ClusterConfiguration.COLLECTOR_BRANCH);
			} else
				break;
		}

		int upperParallelId = mainRoute.getInitialPointId();
		int [] key= MapHelper.breakKey(upperParallelId);
		key[0] = 1;
		upperParallelId = MapHelper.formKey(key[0], key[1]);
		
		int current = 0;
		while (true) {
			if (current % 2 == 0) {
				// the parallel should be houseLength (as 8 is already used
				// should be somewhere between 12 and more) and then road
				int totalMobility = (2 * ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE) + 11
						+ (current == 0 ? ClusterConfiguration.ARTERIAL_BRANCH_SIZE - 26 : 0);
				upperParallelId = MapHelper.moveKeyByOffsetAndDirection(upperParallelId, totalMobility,
						orthogonalDirections.get(0));
			} else {
				// then BASE_CLUSTER_SIZE
				upperParallelId = MapHelper.moveKeyByOffsetAndDirection(upperParallelId,
						ClusterConfiguration.BASE_CLUSTER_SIZE, orthogonalDirections.get(0));
			}
			if (!landMap.landPointisOnMap(upperParallelId))
				break;
			createTransversalRoute(upperParallelId, mainRoute.getDirection(), ClusterConfiguration.LOCAL_BRANCH);
			current++;
		}

		int lowerParallelId = mainRoute.getInitialPointId();
		key= MapHelper.breakKey(lowerParallelId);
		key[0] = 1;
		lowerParallelId = MapHelper.formKey(key[0], key[1]);
		current = 0;
		while (true) {
			if (current % 2 == 0) {
				int totalMobility = (2 * ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE) + 11
						+ (current == 0 ? ClusterConfiguration.ARTERIAL_BRANCH_SIZE - 11 : 0);
				lowerParallelId = MapHelper.moveKeyByOffsetAndDirection(lowerParallelId, totalMobility,
						orthogonalDirections.get(1));
			} else {
				lowerParallelId = MapHelper.moveKeyByOffsetAndDirection(lowerParallelId,
						ClusterConfiguration.BASE_CLUSTER_SIZE, orthogonalDirections.get(1));
			}
			if (!landMap.landPointisOnMap(lowerParallelId))
				break;
			createTransversalRoute(lowerParallelId, mainRoute.getDirection(), ClusterConfiguration.LOCAL_BRANCH);
			current++;
		}
	}

	public static void optimizeClusterization() {
		// For finding the empty "n" on Y from bottom to top
		for (int x = 0; x < landMap.getPointsx(); x++) {
			int emptySpaces = 0;
			boolean successivesN = false;
			boolean enteredPolygon = false;
			boolean leavePolygon = false;
			int y;
			
			for (y = 0; y < landMap.getPointsy(); y++) {
				if (!enteredPolygon && !landMap.getLandPoint(MapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
					enteredPolygon = true;
				} else if (enteredPolygon && landMap.getLandPoint(MapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
					leavePolygon = true;
				}

				if (!leavePolygon && landMap.getLandPoint(MapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.EMPTY_MARK)) {
					emptySpaces++;
				}

				if (!leavePolygon && landMap.getLandPoint(MapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					if (landMap.getLandPoint(MapHelper.formKey(x, y+1)).getType()
							.equals(ClusterConfiguration.NODE_MARK)) {
						successivesN = true;
					}
				}
				
				if (leavePolygon == true)
					break;
			}
			if (successivesN && emptySpaces > 0) {
				for (int j = y; j > (y - emptySpaces); j--) {
					landMap.getLandPoint(MapHelper.formKey(x, j)).setType(ClusterConfiguration.NODE_MARK);
				}
			}
		}

		// For finding the empty "n" on X from left to right
		for (int y = 0; y < landMap.getPointsy(); y++) {
			int emptySpaces = 0;
			boolean successivesN = false;
			boolean enteredPolygon = false;
			boolean leavePolygon = false;
			int x;
			for (x = 0; x < landMap.getPointsx(); x++) {
				if (!enteredPolygon && !landMap.getLandPoint(MapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
					enteredPolygon = true;
				} else if (enteredPolygon && landMap.getLandPoint(MapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
					leavePolygon = true;
				}

				if (!leavePolygon && landMap.getLandPoint(MapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.EMPTY_MARK)) {
					emptySpaces++;
				}

				if (!leavePolygon && landMap.getLandPoint(MapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					if (landMap.getLandPoint(MapHelper.formKey(x + 1, y)).getType()
							.equals(ClusterConfiguration.NODE_MARK)) {
						successivesN = true;
					}
				}

				if (leavePolygon == true)
					break;
			}
			if (successivesN && emptySpaces > 0) {
				for (int j = x; j > (x - emptySpaces); j--) {
					landMap.getLandPoint(MapHelper.formKey(j, y)).setType(ClusterConfiguration.NODE_MARK);
				}
			}
		}
	}
}