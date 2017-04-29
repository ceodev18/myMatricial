package algorithm.spineVariation;

import java.util.List;

import helpers.spineVariation.SpineDirectionHelper;
import helpers.spineVariation.SpineMapHelper;
import interfaces.spineVariation.SpineConfiguration;
import interfaces.spineVariation.SpineConstants;
import models.spineVariation.SpineLandPoint;
import models.spineVariation.SpineLandRoute;
import models.spineVariation.SpineLandMap;

public class LSystemSpineAlgorithm {
	public static SpineLandMap landMap;
	
	public static void clusterize() {
		// 1. we need to now the main route size
		SpineLandRoute mainRoute = landMap.getLandRoute();
		int entryPointId = mainRoute.getInitialPointId();
		// Once the collector branches are created we need to create the non
		// collector running orthogonal to the main
		List<Integer> orthogonalDirections = SpineDirectionHelper.orthogonalDirections(mainRoute.getDirection());
		while (true) {
			//
			entryPointId = SpineMapHelper.moveKeyByOffsetAndDirection(entryPointId,
					2*SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE + SpineConfiguration.COLLECTOR_BRANCH_SIZE,
					mainRoute.getDirection());
			if (landMap.landPointisOnMap(entryPointId) && landMap.intersectMainRoute(entryPointId)) {
				createRouteVariation(entryPointId, orthogonalDirections.get(0), SpineConfiguration.COLLECTOR_BRANCH);
			} else
				break;
		}
		
		//Principal routes
		int upperParallelId = mainRoute.getInitialPointId();
		int[] key = SpineMapHelper.breakKey(upperParallelId);
		key[0] = 1;
		upperParallelId = SpineMapHelper.formKey(key[0], key[1]);
		while (true) {
			upperParallelId = SpineMapHelper.moveKeyByOffsetAndDirection(upperParallelId,
					SpineConfiguration.BASE_CLUSTER_SIZE, orthogonalDirections.get(0));
			if (!landMap.landPointisOnMap(upperParallelId))
				break;
			createRouteVariation(upperParallelId, mainRoute.getDirection(), SpineConfiguration.LOCAL_BRANCH);
		}

		int lowerParallelId = mainRoute.getInitialPointId();
		key = SpineMapHelper.breakKey(lowerParallelId);
		key[0] = 1;
		lowerParallelId = SpineMapHelper.formKey(key[0], key[1]);
		while (true) {
			lowerParallelId = SpineMapHelper.moveKeyByOffsetAndDirection(lowerParallelId,
					SpineConfiguration.BASE_CLUSTER_SIZE, orthogonalDirections.get(1));
			if (!landMap.landPointisOnMap(lowerParallelId))
				break;
			createRouteVariation(lowerParallelId, mainRoute.getDirection(), SpineConfiguration.LOCAL_BRANCH);
		}
	}

	public static void createRouteVariation(int axisPoint, int direction, int branchType) {
		int extension = 0;
		String markType = "";
		int growDirection = -1;
		SpineLandRoute clusterLandRoute = null;
		switch (branchType) {
		case SpineConfiguration.ARTERIAL_BRANCH:
			//clusterLandRoute = new SpineLandRoute();
			clusterLandRoute.setInitialPointId(axisPoint);
			clusterLandRoute.setDirection(direction);
			extension = SpineConfiguration.ARTERIAL_BRANCH_SIZE;
			markType = SpineConfiguration.ARTERIAL_MARK;
			break;
		case SpineConfiguration.COLLECTOR_BRANCH:
			extension = SpineConfiguration.COLLECTOR_BRANCH_SIZE;
			markType = SpineConfiguration.COLLECTOR_MARK;
			break;
		case SpineConfiguration.LOCAL_BRANCH:
			extension = SpineConfiguration.LOCAL_BRANCH_SIZE;
			markType = SpineConfiguration.LOCAL_MARK;
			break;
		case SpineConfiguration.WALK_BRANCH:
			extension = SpineConfiguration.WALK_BRANCH_SIZE;
			markType = SpineConfiguration.WALK_MARK;
			break;
		}

		switch (direction) {
		case SpineConstants.EAST:
			growDirection = SpineConstants.NORTH;
			break;
		case SpineConstants.NORTH:
			growDirection = SpineConstants.WEST;
			break;
		case SpineConstants.WEST:
			growDirection = SpineConstants.SOUTH;
			break;
		case SpineConstants.SOUTH:
			growDirection = SpineConstants.EAST;
			break;
		}

		createLine(
				SpineMapHelper.moveKeyByOffsetAndDirection(axisPoint, 1,
						SpineDirectionHelper.oppositeDirection(growDirection)),
				direction, SpineConfiguration.NODE_MARK);
		createLine(SpineMapHelper.moveKeyByOffsetAndDirection(axisPoint, extension, growDirection), direction,
				SpineConfiguration.NODE_MARK);

		for (int i = 0; i < extension; i++) {
			if ((branchType == SpineConfiguration.ARTERIAL_BRANCH) && (i == 0)) {
				int finalPointid = createLine(SpineMapHelper.moveKeyByOffsetAndDirection(axisPoint, i, growDirection),
						direction, markType);
				clusterLandRoute.setFinalPointId(finalPointid);
				landMap.setLandRoute(clusterLandRoute);
			} else {
				createLine(SpineMapHelper.moveKeyByOffsetAndDirection(axisPoint, i, growDirection), direction,
						markType);
			}
		}
	}

	private static int createLine(int givenXY, int direction, String markType) {
		int[] xy = SpineMapHelper.breakKey(givenXY);
		int[] newXY = new int[2];
		Boolean in = false, out = false, changed = false;
		if ((direction == SpineConstants.NORTH) || (direction == SpineConstants.SOUTH)) {
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
					return SpineMapHelper.formKey(newXY[0], newXY[1]);
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
					return SpineMapHelper.formKey(newXY[0], newXY[1]);
				}
			}
		}
		return -1;
	}

	private static boolean markPoint(int[] newXY, String markType, Boolean in, Boolean out) {
		boolean changed = false;
		SpineLandPoint clusterLandPoint = landMap.findPoint(SpineMapHelper.formKey(newXY[0], newXY[1]));
		if (!in.booleanValue() && !clusterLandPoint.isMapLimit()) {
			changed = true;
			in = true;
		}

		if (in.booleanValue() && clusterLandPoint.isMapLimit()) {
			changed = true;
		}

		if (in.booleanValue()) {
			if (markType.equals(SpineConfiguration.NODE_MARK)) {
				if (clusterLandPoint.getType().equals(SpineConfiguration.EMPTY_MARK)) {
					clusterLandPoint.setType(markType);
				}
			} else {
				if (clusterLandPoint.getType().equals(SpineConfiguration.EMPTY_MARK)
						|| clusterLandPoint.getType().equals(SpineConfiguration.NODE_MARK)) {
					clusterLandPoint.setType(markType);
				}
			}
		}
		return changed;
	}


}
