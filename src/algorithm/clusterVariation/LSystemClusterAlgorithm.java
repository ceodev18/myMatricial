package algorithm.clusterVariation;

import java.util.List;

import helpers.clusterVariation.ClusterDirectionHelper;
import helpers.clusterVariation.ClusterMapHelper;
import interfaces.clusterVariation.ClusterConfiguration;
import interfaces.clusterVariation.ClusterConstants;
import models.clusterVariation.ClusterLandMap;
import models.clusterVariation.ClusterLandPoint;
import models.clusterVariation.ClusterLandRoute;

public class LSystemClusterAlgorithm {
	public static ClusterLandMap landMap;

	public static void clusterize() {
		// 1. we need to now the main route size
		ClusterLandRoute mainRoute = landMap.getLandRoute();
		int entryPointId = mainRoute.getInitialPointId();
		// Once the collector branches are created we need to create the non
		// collector running orthogonal to the main
		List<Integer> orthogonalDirections = ClusterDirectionHelper.orthogonalDirections(mainRoute.getDirection());
		while (true) {
			entryPointId = ClusterMapHelper.moveKeyByOffsetAndDirection(entryPointId,
					ClusterConfiguration.BASE_CLUSTER_SIZE + ClusterConfiguration.COLLECTOR_BRANCH_SIZE,
					mainRoute.getDirection());
			if (landMap.landPointisOnMap(entryPointId) && landMap.intersectMainRoute(entryPointId)) {
				createRouteVariation(entryPointId, orthogonalDirections.get(0), ClusterConfiguration.COLLECTOR_BRANCH);
				// createRoute(entryPointId, orthogonalDirections.get(1),
				// ClusterConfiguration.COLLECTOR_BRANCH);
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
			createRouteVariation(upperParallelId, mainRoute.getDirection(), ClusterConfiguration.LOCAL_BRANCH);
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
			createRouteVariation(lowerParallelId, mainRoute.getDirection(), ClusterConfiguration.LOCAL_BRANCH);
			current++;
		}
	}

	public static void createRouteVariation(int axisPoint, int direction, int branchType) {
		int extension = 0;
		String markType = "";
		int growDirection = -1;
		ClusterLandRoute clusterLandRoute = null;
		switch (branchType) {
		case ClusterConfiguration.ARTERIAL_BRANCH:
			clusterLandRoute = new ClusterLandRoute();
			clusterLandRoute.setInitialPointId(axisPoint);
			clusterLandRoute.setDirection(direction);
			extension = ClusterConfiguration.ARTERIAL_BRANCH_SIZE;
			markType = ClusterConfiguration.ARTERIAL_MARK;
			break;
		case ClusterConfiguration.COLLECTOR_BRANCH:
			extension = ClusterConfiguration.COLLECTOR_BRANCH_SIZE;
			markType = ClusterConfiguration.COLLECTOR_MARK;
			break;
		case ClusterConfiguration.LOCAL_BRANCH:
			extension = ClusterConfiguration.LOCAL_BRANCH_SIZE;
			markType = ClusterConfiguration.LOCAL_MARK;
			break;
		case ClusterConfiguration.WALK_BRANCH:
			extension = ClusterConfiguration.WALK_BRANCH_SIZE;
			markType = ClusterConfiguration.WALK_MARK;
			break;
		}

		switch (direction) {
		case ClusterConstants.EAST:
			growDirection = ClusterConstants.NORTH;
			break;
		case ClusterConstants.NORTH:
			growDirection = ClusterConstants.WEST;
			break;
		case ClusterConstants.WEST:
			growDirection = ClusterConstants.SOUTH;
			break;
		case ClusterConstants.SOUTH:
			growDirection = ClusterConstants.EAST;
			break;
		}

		createLine(
				ClusterMapHelper.moveKeyByOffsetAndDirection(axisPoint, 1,
						ClusterDirectionHelper.oppositeDirection(growDirection)),
				direction, ClusterConfiguration.NODE_MARK);
		createLine(ClusterMapHelper.moveKeyByOffsetAndDirection(axisPoint, extension, growDirection), direction,
				ClusterConfiguration.NODE_MARK);

		for (int i = 0; i < extension; i++) {
			if ((branchType == ClusterConfiguration.ARTERIAL_BRANCH) && (i == 0)) {
				int finalPointid = createLine(ClusterMapHelper.moveKeyByOffsetAndDirection(axisPoint, i, growDirection),
						direction, markType);
				clusterLandRoute.setFinalPointId(finalPointid);
				landMap.setLandRoute(clusterLandRoute);
			} else {
				createLine(ClusterMapHelper.moveKeyByOffsetAndDirection(axisPoint, i, growDirection), direction,
						markType);
			}
		}
	}

	private static int createLine(int givenXY, int direction, String markType) {
		int[] xy = ClusterMapHelper.breakKey(givenXY);
		int[] newXY = new int[2];
		Boolean in = false, out = false, changed = false;
		if ((direction == ClusterConstants.NORTH) || (direction == ClusterConstants.SOUTH)) {
			if (xy[0] > landMap.getPointsx())
				return -1;
			for (int i = 0; i < landMap.getPointsy(); i++) {
				newXY[0] = xy[0];
				newXY[1] = i;
				changed = markPoint(newXY, markType, in, out);
				if (changed && !in) {
					in = true;
					changed = false;
				}
				if (changed && in) {
					out = true;
				}
				if (out.booleanValue()) {
					newXY[0] = xy[0];
					newXY[1] = i - 1;
					return ClusterMapHelper.formKey(newXY[0], newXY[1]);
				}
			}
		} else {
			if (xy[1] > landMap.getPointsy())
				return -1;
			for (int i = 0; i < landMap.getPointsx(); i++) {
				newXY[0] = i;
				newXY[1] = xy[1];
				changed = markPoint(newXY, markType, in, out);
				if (changed && !in) {
					in = true;
					changed = false;
				}
				if (changed && in) {
					out = true;
				}
				if (out.booleanValue()) {
					newXY[0] = i - 1;
					newXY[1] = xy[1];
					return ClusterMapHelper.formKey(newXY[0], newXY[1]);
				}
			}
		}
		return -1;
	}

	private static boolean markPoint(int[] newXY, String markType, Boolean in, Boolean out) {
		boolean changed = false;
		ClusterLandPoint clusterLandPoint = landMap.findPoint(ClusterMapHelper.formKey(newXY[0], newXY[1]));
		if (!in.booleanValue() && !clusterLandPoint.isMapLimit()) {
			changed = true;
			in = true;
		}

		if (in.booleanValue() && clusterLandPoint.isMapLimit()) {
			changed = true;
		}

		if (in.booleanValue()) {
			if (markType.equals(ClusterConfiguration.NODE_MARK)) {
				if (clusterLandPoint.getType().equals(ClusterConfiguration.EMPTY_MARK)) {
					clusterLandPoint.setType(markType);
				}
			} else {
				if (clusterLandPoint.getType().equals(ClusterConfiguration.EMPTY_MARK)
						|| clusterLandPoint.getType().equals(ClusterConfiguration.NODE_MARK)) {
					clusterLandPoint.setType(markType);
				}
			}
		}
		return changed;
	}
}