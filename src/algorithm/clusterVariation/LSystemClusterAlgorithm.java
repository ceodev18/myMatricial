package algorithm.clusterVariation;

import java.util.List;

import helpers.clusterVariation.ClusterDirectionHelper;
import helpers.clusterVariation.ClusterMapHelper;
import interfaces.clusterVariation.ClusterConfiguration;
import models.clusterVariation.ClusterLandMap;
import models.clusterVariation.ClusterLandPoint;
import models.clusterVariation.ClusterLandRoute;

//16n2
public class LSystemClusterAlgorithm {
	public static ClusterLandMap landMap;

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
		}

		ClusterLandPoint testPoint = currentPoint;
		// test if the route crosses a collector if it does not it should not
		// create anything
		while (!testPoint.isMapLimit(direction)) {
			if (testPoint.getType().equals(ClusterConfiguration.COLLECTOR_MARK)) {
				break;
			}
			int nextPointId = testPoint.findNeighbour(direction);
			testPoint = landMap.findPoint(nextPointId);
		}
		if (testPoint.isMapLimit(direction)) {
			return;
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

	// Time: 3N2
	public static void clusterize() {
		// 1. we need to now the main route size
		ClusterLandRoute mainRoute = landMap.getLandRoutes().get(0);
		int entryPointId = mainRoute.getInitialPointId();
		// Once the collector branches are created we need to create the non
		// collector running orthogonal to the main
		List<Integer> orthogonalDirections = ClusterDirectionHelper.orthogonalDirections(mainRoute.getDirection());
		while (true) {
			entryPointId = ClusterMapHelper.moveKeyByOffsetAndDirection(entryPointId,
					ClusterConfiguration.BASE_CLUSTER_SIZE + ClusterConfiguration.COLLECTOR_BRANCH_SIZE,
					mainRoute.getDirection());
			if (landMap.landPointisOnMap(entryPointId) && landMap.intersectMainRoute(entryPointId)) {
				createRoute(entryPointId, orthogonalDirections.get(0), ClusterConfiguration.COLLECTOR_BRANCH);
				createRoute(entryPointId, orthogonalDirections.get(1), ClusterConfiguration.COLLECTOR_BRANCH);
			} else
				break;
		}

		int upperParallelId = mainRoute.getInitialPointId();
		int[] key = ClusterMapHelper.breakKey(upperParallelId);
		key[0] = 1;
		upperParallelId = ClusterMapHelper.formKey(key[0], key[1]);

		int current = 0;
		while (true) {
			// the parallel should be houseLength (as 8 is already used
			// should be somewhere between 12 and more) and then road
			/*
			 * int totalMobility = (2 *
			 * ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE) + 11+ (current ==
			 * 0 ? ClusterConfiguration.ARTERIAL_BRANCH_SIZE - 26 :
			 * 0);upperParallelId =
			 * ClusterMapHelper.moveKeyByOffsetAndDirection(upperParallelId,
			 * totalMobility, orthogonalDirections.get(0)); } else {
			 */
			// then BASE_CLUSTER_SIZE
			upperParallelId = ClusterMapHelper.moveKeyByOffsetAndDirection(upperParallelId,
					ClusterConfiguration.BASE_CLUSTER_SIZE, orthogonalDirections.get(0));
			if (!landMap.landPointisOnMap(upperParallelId))
				break;
			createTransversalRoute(upperParallelId, mainRoute.getDirection(), ClusterConfiguration.LOCAL_BRANCH);
			current++;
		}

		int lowerParallelId = mainRoute.getInitialPointId();
		key = ClusterMapHelper.breakKey(lowerParallelId);
		key[0] = 1;
		lowerParallelId = ClusterMapHelper.formKey(key[0], key[1]);
		current = 0;
		while (true) {
			if (current % 2 == 0) {
				int totalMobility = (2 * ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE) + 11
						+ (current == 0 ? ClusterConfiguration.ARTERIAL_BRANCH_SIZE - 11 : 0);
				lowerParallelId = ClusterMapHelper.moveKeyByOffsetAndDirection(lowerParallelId, totalMobility,
						orthogonalDirections.get(1));
			} else {
				lowerParallelId = ClusterMapHelper.moveKeyByOffsetAndDirection(lowerParallelId,
						ClusterConfiguration.BASE_CLUSTER_SIZE, orthogonalDirections.get(1));
			}
			if (!landMap.landPointisOnMap(lowerParallelId))
				break;
			createTransversalRoute(lowerParallelId, mainRoute.getDirection(), ClusterConfiguration.LOCAL_BRANCH);
			current++;
		}
	}

	// Time: 2N2
	public static void optimizeClusterization(String markType) {
		// For finding the empty "n" on Y from bottom to top
		for (int x = 0; x < landMap.getPointsx(); x++) {
			int emptySpaces = 0;
			boolean lower = false;
			boolean upper = false;
			boolean successivesN = false;
			boolean enteredPolygon = false;
			boolean leavePolygon = false;
			int y;

			for (y = 0; y < landMap.getPointsy(); y++) {
				if (!enteredPolygon && !landMap.getLandPoint(ClusterMapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
					enteredPolygon = true;
				} else if (enteredPolygon && landMap.getLandPoint(ClusterMapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
					leavePolygon = true;
				}

				if (leavePolygon == true)
					break;

				// just for the lower part we sum the empty marks that where not
				// choosen
				if (!leavePolygon && landMap.getLandPoint(ClusterMapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.EMPTY_MARK)) {
					emptySpaces++;
				}

				if (!leavePolygon && landMap.getLandPoint(ClusterMapHelper.formKey(x, y)).getType().equals(markType)) {
					if (y != 0 && y != (landMap.getPointsy() - 1)) {
						if (landMap.getLandPoint(ClusterMapHelper.formKey(x, (y + 1))).getType().equals(markType)
								&& landMap.getLandPoint(ClusterMapHelper.formKey(x, (y - 1))).getType()
										.equals(ClusterConfiguration.EMPTY_MARK)) {
							successivesN = lower = true;
						}

						if (landMap.getLandPoint(ClusterMapHelper.formKey(x, (y - 1))).getType().equals(markType)
								&& landMap.getLandPoint(ClusterMapHelper.formKey(x, (y + 1))).getType()
										.equals(ClusterConfiguration.EMPTY_MARK)) {
							successivesN = upper = true;
						}
					}
				}

				// lower side only it retries until the case is done
				if (successivesN && (emptySpaces > 0) && lower) {
					for (int j = y; j > (y - (emptySpaces + 1)); j--) {
						landMap.getLandPoint(ClusterMapHelper.formKey(x, j)).setType(markType);
					}
					successivesN = false;
					lower = false;
				}

				// upper side is going to need me to do it until one is out
				if (successivesN && upper) {
					for (int j = y; j < landMap.getPointsy(); j++) {
						if (landMap.getLandPoint(ClusterMapHelper.formKey(x, j)).getType()
								.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
							leavePolygon = true;
							successivesN = false;
							upper = false;
							break;
						}
						landMap.getLandPoint(ClusterMapHelper.formKey(x, j)).setType(markType);
					}
				}
			}
		}

		// For finding the empty "n" on X from left to right
		// For finding the empty "n" on Y from bottom to top
		for (int y = 0; y < landMap.getPointsy(); y++) {
			int emptySpaces = 0;
			boolean left = false;
			boolean right = false;
			boolean successivesN = false;
			boolean enteredPolygon = false;
			boolean leavePolygon = false;
			int x;

			for (x = 0; x < landMap.getPointsx(); x++) {
				if (!enteredPolygon && !landMap.getLandPoint(ClusterMapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
					enteredPolygon = true;
				} else if (enteredPolygon && landMap.getLandPoint(ClusterMapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
					leavePolygon = true;
				}

				if (leavePolygon == true)
					break;

				// just for the lower part we sum the empty marks that where not
				// choosen
				if (!leavePolygon && landMap.getLandPoint(ClusterMapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.EMPTY_MARK)) {
					emptySpaces++;
				}

				if (!leavePolygon && landMap.getLandPoint(ClusterMapHelper.formKey(x, y)).getType().equals(markType)) {
					if (x != 0 && y != (landMap.getPointsx() - 1)) {
						if (landMap.getLandPoint(ClusterMapHelper.formKey((x + 1), y)).getType().equals(markType)
								&& landMap.getLandPoint(ClusterMapHelper.formKey((x - 1), y)).getType()
										.equals(ClusterConfiguration.EMPTY_MARK)) {
							successivesN = left = true;
						}

						if (landMap.getLandPoint(ClusterMapHelper.formKey((x - 1), y)).getType().equals(markType)
								&& landMap.getLandPoint(ClusterMapHelper.formKey((x + 1), y)).getType()
										.equals(ClusterConfiguration.EMPTY_MARK)) {
							successivesN = right = true;
						}
					}
				}

				// lower side only it retries until the case is done
				if (successivesN && (emptySpaces > 0) && left) {
					for (int j = x; j > (x - (emptySpaces + 1)); j--) {
						landMap.getLandPoint(ClusterMapHelper.formKey(j, y)).setType(markType);
					}
					successivesN = false;
					left = false;
				}

				// upper side is going to need me to do it until one is out
				if (successivesN && right) {
					for (int j = x; j < landMap.getPointsx(); j++) {
						if (landMap.getLandPoint(ClusterMapHelper.formKey(j, y)).getType()
								.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
							leavePolygon = true;
							successivesN = false;
							right = false;
							break;
						}
						landMap.getLandPoint(ClusterMapHelper.formKey(j, y)).setType(markType);
					}
				}
			}
		}
	}
}