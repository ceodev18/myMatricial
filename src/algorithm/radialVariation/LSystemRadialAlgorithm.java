package algorithm.radialVariation;

import java.util.List;

import helpers.RadialVariation.RadialDirectionHelper;
import helpers.RadialVariation.RadialMapHelper;
import interfaces.radialVariation.RadialConfiguration;
import interfaces.radialVariation.RadialConstants;
import models.RadialVariation.RadialLandMap;
import models.RadialVariation.RadialLandPoint;
import models.RadialVariation.RadialLandRoute;

public class LSystemRadialAlgorithm {
	public static RadialLandMap landMap;

	public static void Radialize() {
		// 1. we need to now the main route size
		RadialLandRoute mainRoute = landMap.getLandRoute();
		int entryPointId = mainRoute.getInitialPointId();
		// Once the collector branches are created we need to create the non
		// collector running orthogonal to the main
		List<Integer> orthogonalDirections = RadialDirectionHelper.orthogonalDirections(mainRoute.getDirection());
		while (true) {
			entryPointId = RadialMapHelper.moveKeyByOffsetAndDirection(entryPointId,
					RadialConfiguration.BASE_CLUSTER_SIZE + RadialConfiguration.COLLECTOR_BRANCH_SIZE,
					mainRoute.getDirection());
			if (landMap.landPointisOnMap(entryPointId) && landMap.intersectMainRoute(entryPointId)) {
				createRouteVariation(entryPointId, orthogonalDirections.get(0), RadialConfiguration.COLLECTOR_BRANCH);
			} else
				break;
		}
		
		//Principal routes
		int upperParallelId = mainRoute.getInitialPointId();
		int[] key = RadialMapHelper.breakKey(upperParallelId);
		key[0] = 1;
		upperParallelId = RadialMapHelper.formKey(key[0], key[1]);
		while (true) {
			upperParallelId = RadialMapHelper.moveKeyByOffsetAndDirection(upperParallelId,
					RadialConfiguration.BASE_CLUSTER_SIZE, orthogonalDirections.get(0));
			if (!landMap.landPointisOnMap(upperParallelId))
				break;
			createRouteVariation(upperParallelId, mainRoute.getDirection(), RadialConfiguration.LOCAL_BRANCH);
		}

		int lowerParallelId = mainRoute.getInitialPointId();
		key = RadialMapHelper.breakKey(lowerParallelId);
		key[0] = 1;
		lowerParallelId = RadialMapHelper.formKey(key[0], key[1]);
		while (true) {
			lowerParallelId = RadialMapHelper.moveKeyByOffsetAndDirection(lowerParallelId,
					RadialConfiguration.BASE_CLUSTER_SIZE, orthogonalDirections.get(1));
			if (!landMap.landPointisOnMap(lowerParallelId))
				break;
			createRouteVariation(lowerParallelId, mainRoute.getDirection(), RadialConfiguration.LOCAL_BRANCH);
		}
	}

	public static void createRouteVariation(int axisPoint, int direction, int branchType) {
		int extension = 0;
		String markType = "";
		int growDirection = -1;
		RadialLandRoute RadialLandRoute = null;
		switch (branchType) {
		case RadialConfiguration.ARTERIAL_BRANCH:
			RadialLandRoute = new RadialLandRoute();
			RadialLandRoute.setInitialPointId(axisPoint);
			RadialLandRoute.setDirection(direction);
			extension = RadialConfiguration.ARTERIAL_BRANCH_SIZE;
			markType = RadialConfiguration.ARTERIAL_MARK;
			break;
		case RadialConfiguration.COLLECTOR_BRANCH:
			extension = RadialConfiguration.COLLECTOR_BRANCH_SIZE;
			markType = RadialConfiguration.COLLECTOR_MARK;
			break;
		case RadialConfiguration.LOCAL_BRANCH:
			extension = RadialConfiguration.LOCAL_BRANCH_SIZE;
			markType = RadialConfiguration.LOCAL_MARK;
			break;
		case RadialConfiguration.WALK_BRANCH:
			extension = RadialConfiguration.WALK_BRANCH_SIZE;
			markType = RadialConfiguration.WALK_MARK;
			break;
		}

		switch (direction) {
		case RadialConstants.EAST:
			growDirection = RadialConstants.NORTH;
			break;
		case RadialConstants.NORTH:
			growDirection = RadialConstants.WEST;
			break;
		case RadialConstants.WEST:
			growDirection = RadialConstants.SOUTH;
			break;
		case RadialConstants.SOUTH:
			growDirection = RadialConstants.EAST;
			break;
		}

		createLine(
				RadialMapHelper.moveKeyByOffsetAndDirection(axisPoint, 1,
						RadialDirectionHelper.oppositeDirection(growDirection)),
				direction, RadialConfiguration.NODE_MARK);
		createLine(RadialMapHelper.moveKeyByOffsetAndDirection(axisPoint, extension, growDirection), direction,
				RadialConfiguration.NODE_MARK);

		for (int i = 0; i < extension; i++) {
			if ((branchType == RadialConfiguration.ARTERIAL_BRANCH) && (i == 0)) {
				int finalPointid = createLine(RadialMapHelper.moveKeyByOffsetAndDirection(axisPoint, i, growDirection),
						direction, markType);
				RadialLandRoute.setFinalPointId(finalPointid);
				landMap.setLandRoute(RadialLandRoute);
			} else {
				createLine(RadialMapHelper.moveKeyByOffsetAndDirection(axisPoint, i, growDirection), direction,
						markType);
			}
		}
	}

	private static int createLine(int givenXY, int direction, String markType) {
		int[] xy = RadialMapHelper.breakKey(givenXY);
		int[] newXY = new int[2];
		Boolean in = false, out = false, changed = false;
		if ((direction == RadialConstants.NORTH) || (direction == RadialConstants.SOUTH)) {
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
					return RadialMapHelper.formKey(newXY[0], newXY[1]);
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
					return RadialMapHelper.formKey(newXY[0], newXY[1]);
				}
			}
		}
		return -1;
	}

	private static boolean markPoint(int[] newXY, String markType, Boolean in, Boolean out) {
		boolean changed = false;
		RadialLandPoint RadialLandPoint = landMap.findPoint(RadialMapHelper.formKey(newXY[0], newXY[1]));
		if (!in.booleanValue() && !RadialLandPoint.isMapLimit()) {
			changed = true;
			in = true;
		}

		if (in.booleanValue() && RadialLandPoint.isMapLimit()) {
			changed = true;
		}

		if (in.booleanValue()) {
			if (markType.equals(RadialConfiguration.NODE_MARK)) {
				if (RadialLandPoint.getType().equals(RadialConfiguration.EMPTY_MARK)) {
					RadialLandPoint.setType(markType);
				}
			} else {
				if (RadialLandPoint.getType().equals(RadialConfiguration.EMPTY_MARK)
						|| RadialLandPoint.getType().equals(RadialConfiguration.NODE_MARK)) {
					RadialLandPoint.setType(markType);
				}
			}
		}
		return changed;
	}
}