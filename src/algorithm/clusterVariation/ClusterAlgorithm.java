package algorithm.clusterVariation;

import java.util.List;

import helpers.clusterVariation.ClusterDirectionHelper;
import helpers.clusterVariation.ClusterMapHelper;
import interfaces.clusterVariation.ClusterConfiguration;
import interfaces.clusterVariation.ClusterConstants;
import models.clusterVariation.ClusterLandMap;
import models.clusterVariation.ClusterLandPoint;
import models.clusterVariation.ClusterLandRoute;
import models.clusterVariation.ClusterPolygon;

public class ClusterAlgorithm {
	private ClusterLandMap landMap;

	public ClusterLandMap getLandMap() {
		return landMap;
	}

	public void setLandMap(ClusterLandMap landMap) {
		this.landMap = landMap;
	}

	public void clusterize(ClusterLandPoint entryPoint) {
		// 1. we need to now the main route size
		int direction = ClusterDirectionHelper.orthogonalDirectionFromPointToPoint(entryPoint, landMap.getCentroid());
		createRouteVariation(entryPoint.getId(), direction, ClusterConfiguration.ARTERIAL_BRANCH);
		ClusterLandRoute mainRoute = landMap.getLandRoutes().get(0);
		// Once the collector branches are created we need to create the non
		// collector running orthogonal to the main
		List<Integer> orthogonalDirections = ClusterDirectionHelper.orthogonalDirections(mainRoute.getDirection());
		int entryPointId = mainRoute.getInitialPointId();
		if (mainRoute.getDirection() == ClusterConstants.EAST || mainRoute.getDirection() == ClusterConstants.WEST) {
			int[] xy = ClusterMapHelper.breakKey(entryPointId);
			xy[0] = 1;
			entryPointId = ClusterMapHelper.formKey(xy[0], xy[1]);
		} else {
			int[] xy = ClusterMapHelper.breakKey(entryPointId);
			xy[1] = 1;
			entryPointId = ClusterMapHelper.formKey(xy[0], xy[1]);
		}

		while (true) {
			entryPointId = ClusterMapHelper.moveKeyByOffsetAndDirection(entryPointId,
					ClusterConfiguration.BASE_CLUSTER_SIZE, mainRoute.getDirection());
			if (landMap.landPointisOnMap(entryPointId)) {
				createRouteVariation(entryPointId, orthogonalDirections.get(0), ClusterConfiguration.COLLECTOR_BRANCH);
			} else
				break;
		}

		// Principal routes
		int upperParallelId = mainRoute.getInitialPointId();
		while (true) {
			upperParallelId = ClusterMapHelper.moveKeyByOffsetAndDirection(upperParallelId,
					ClusterConfiguration.BASE_CLUSTER_SIZE, orthogonalDirections.get(0));
			if (!landMap.landPointisOnMap(upperParallelId))
				break;
			createRouteVariation(upperParallelId, mainRoute.getDirection(), ClusterConfiguration.LOCAL_BRANCH);
		}

		int lowerParallelId = mainRoute.getInitialPointId();
		boolean first = true;
		while (true) {
			int extraSpace = first ? ClusterConfiguration.ARTERIAL_BRANCH_SIZE - 12 : 0;
			first = false;
			lowerParallelId = ClusterMapHelper.moveKeyByOffsetAndDirection(lowerParallelId,
					ClusterConfiguration.BASE_CLUSTER_SIZE + extraSpace, orthogonalDirections.get(1));
			if (!landMap.landPointisOnMap(lowerParallelId))
				break;
			createRouteVariation(lowerParallelId, mainRoute.getDirection(), ClusterConfiguration.LOCAL_BRANCH);
		}
	}

	public void createRouteVariation(int axisPoint, int direction, int branchType) {
		int extension = 0;
		String markType = "";
		int growDirection = -1;
		ClusterLandRoute clusterLandRoute = null;
		int trueAxis = axisPoint;
		while (landMap.landPointisOnMap(trueAxis) && landMap.getLandPoint(trueAxis).getType().equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
			trueAxis = ClusterMapHelper.moveKeyByOffsetAndDirection(trueAxis, 1, direction);
		}

		switch (branchType) {
		case ClusterConfiguration.ARTERIAL_BRANCH:
			extension = ClusterConfiguration.ARTERIAL_BRANCH_SIZE;
			markType = ClusterConfiguration.ARTERIAL_MARK;
			clusterLandRoute = new ClusterLandRoute(trueAxis, direction, "a");
			break;
		case ClusterConfiguration.COLLECTOR_BRANCH:
			extension = ClusterConfiguration.COLLECTOR_BRANCH_SIZE;
			markType = ClusterConfiguration.COLLECTOR_MARK;
			clusterLandRoute = new ClusterLandRoute(trueAxis, direction, "b");
			break;
		case ClusterConfiguration.LOCAL_BRANCH:
			extension = ClusterConfiguration.LOCAL_BRANCH_SIZE;
			markType = ClusterConfiguration.LOCAL_MARK;
			clusterLandRoute = new ClusterLandRoute(trueAxis, direction, "c");
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

		// It is necessary to test that the lower and upper key are still on the
		// map.It would be impossible otherwise to create
		int upperLimitKey = ClusterMapHelper.moveKeyByOffsetAndDirection(axisPoint, 1,
				ClusterDirectionHelper.oppositeDirection(growDirection));
		int lowerLimitKey = ClusterMapHelper.moveKeyByOffsetAndDirection(axisPoint, extension, growDirection);

		if (!landMap.landPointisOnMap(upperLimitKey) || !landMap.landPointisOnMap(lowerLimitKey)) {
			return;
		}

		createLine(upperLimitKey, direction, ClusterConfiguration.NODE_MARK);
		createLine(lowerLimitKey, direction, ClusterConfiguration.NODE_MARK);

		landMap.getLandRoutes().add(clusterLandRoute);
		for (int i = 0; i < extension; i++) {
			createLine(ClusterMapHelper.moveKeyByOffsetAndDirection(axisPoint, i, growDirection), direction, markType);
		}
	}

	private int createLine(int givenXY, int direction, String markType) {
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
				}
			}
		}
		return -1;
	}

	private boolean markPoint(int[] newXY, String markType, Boolean in, Boolean out) {
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

	// Organic zonification: This zonification searchs the figure and
	// reduces it in a reason configured by the user. if it can be done, it
	// becomes a new cluster. If not, it becomes simply defaults into a
	// perfect zonification
	public /* List<ClusterPolygon> */void zonify() {
		// List<ClusterPolygon> clusterPolygons = new
		// ArrayList<ClusterPolygon>();
		for (int y = 0; y < landMap.getPointsy(); y++) {
			boolean insidePolygon = false;
			for (int x = 0; x < landMap.getPointsx(); x++) {

				if (insidePolygon && landMap.findPoint(ClusterMapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
					break;
				}
				if (!insidePolygon && !landMap.findPoint(ClusterMapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
					insidePolygon = true;
				}

				if (landMap.findPoint(ClusterMapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					ClusterPolygon clusterPolygon = new ClusterPolygon();
					createOctopianCoverture(ClusterMapHelper.formKey(x, y), clusterPolygon);
					if (clusterPolygon.getPoints().size() < 3)
						continue;
					// clusterPolygons.add(clusterPolygon);
					clusterPolygon.printPolygon();
					clusterPolygon.setCentroid(clusterPolygon.findCentroid());
					// we create the park
					List<List<Integer>> grass = clusterPolygon.parkZone(
							ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2 + ClusterConfiguration.LOCAL_BRANCH_SIZE);
					if (grass.size() == 0) {
						int maximunTries = 6;
						landMap.impreciseLotization(clusterPolygon, maximunTries);
					} else {
						for (int j = 0; j < grass.size(); j++) {
							landMap.createBorderFromPolygon(grass.get(j), ClusterConfiguration.PARK_MARK);
						}

						// we create the routes
						List<List<Integer>> routes = clusterPolygon.routeZone(
								ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2,
								ClusterConfiguration.LOCAL_BRANCH_SIZE);
						for (int j = 0; j < routes.size(); j++) {
							landMap.createBorderFromPolygon(routes.get(j), ClusterConfiguration.LOCAL_MARK);
						}

						landMap.preciseLotization(clusterPolygon, true);// with
																		// contribution
					}
				}
			}
		}
		nonOrthogonalMapLotization();
		/* return clusterPolygons; */
	}

	private void nonOrthogonalMapLotization() {
		ClusterPolygon clusterMap = landMap.getAsClusterPolygon();
		for (int i = 0; i < clusterMap.getPoints().size(); i++) {
			int[] initialXY = ClusterMapHelper.breakKey(clusterMap.getPoints().get(i));
			int[] finalXY = ClusterMapHelper
					.breakKey(clusterMap.getPoints().get((i + 1) % clusterMap.getPoints().size()));

			int driveDirection = -1;
			int growDirection = -1;

			if ((initialXY[0] != finalXY[0]) && (initialXY[1] != finalXY[1])) {
				if (Math.abs(initialXY[0] - finalXY[0]) > Math.abs(initialXY[1] - finalXY[1])) {
					driveDirection = initialXY[0] < finalXY[0] ? ClusterConstants.EAST : ClusterConstants.WEST;
					growDirection = ClusterDirectionHelper.perpendicularDirection(initialXY, clusterMap.getCentroid(),
							ClusterConstants.EAST);
				} else {
					driveDirection = initialXY[1] < finalXY[1] ? ClusterConstants.NORTH : ClusterConstants.SOUTH;
					growDirection = ClusterDirectionHelper.perpendicularDirection(initialXY, clusterMap.getCentroid(),
							ClusterConstants.NORTH);
				}
				landMap.orthogonalLotization(initialXY, finalXY, driveDirection, growDirection);
			}
		}
	}

	private void createOctopianCoverture(int nodeKey, ClusterPolygon clusterPolygon) {
		clusterPolygon.getPoints().add(nodeKey);
		landMap.findPoint(nodeKey).setType(ClusterConfiguration.BORDER_MARK);

		int roadUp = 0, roadDown = 0, roadLeft = 0, roadRight = 0;
		boolean nonOctopian = false;
		while ((roadUp != -1) || (roadDown != -1) || (roadLeft != -1) || (roadRight != -1)) {
			roadUp = extendMembraneSection(
					ClusterMapHelper.moveKeyByOffsetAndDirection(nodeKey, 1, ClusterConstants.NORTH),
					ClusterConstants.NORTH);
			roadDown = extendMembraneSection(
					ClusterMapHelper.moveKeyByOffsetAndDirection(nodeKey, 1, ClusterConstants.SOUTH),
					ClusterConstants.SOUTH);
			roadLeft = extendMembraneSection(
					ClusterMapHelper.moveKeyByOffsetAndDirection(nodeKey, 1, ClusterConstants.EAST),
					ClusterConstants.EAST);
			roadRight = extendMembraneSection(
					ClusterMapHelper.moveKeyByOffsetAndDirection(nodeKey, 1, ClusterConstants.WEST),
					ClusterConstants.WEST);

			// Especial case with borderized polygon
			if ((roadUp != -1) && (roadLeft != -1)) {
				nonOctopian = true;
				break;
			} else {
				nodeKey = Math.abs(roadUp * roadDown * roadLeft * roadRight);
				if (nodeKey != 1)
					clusterPolygon.getPoints().add(nodeKey);
			}
		}

		if (nonOctopian) {
			// non square, ABOMINATION LINE
			clusterPolygon.getPoints().add(0, roadLeft);
			clusterPolygon.getPoints().add(roadUp);
			// continued from right
			int bipolarNodeKey = roadLeft;
			nodeKey = roadUp;

			roadUp = 0;
			roadDown = 0;
			roadLeft = 0;
			roadRight = 0;
			while ((roadUp != -1) || (roadDown != -1) || (roadLeft != -1) || (roadRight != -1)) {
				roadUp = extendMembraneSection(
						ClusterMapHelper.moveKeyByOffsetAndDirection(nodeKey, 1, ClusterConstants.NORTH),
						ClusterConstants.NORTH);
				roadDown = extendMembraneSection(
						ClusterMapHelper.moveKeyByOffsetAndDirection(nodeKey, 1, ClusterConstants.SOUTH),
						ClusterConstants.SOUTH);
				roadLeft = extendMembraneSection(
						ClusterMapHelper.moveKeyByOffsetAndDirection(nodeKey, 1, ClusterConstants.EAST),
						ClusterConstants.EAST);
				roadRight = extendMembraneSection(
						ClusterMapHelper.moveKeyByOffsetAndDirection(nodeKey, 1, ClusterConstants.WEST),
						ClusterConstants.WEST);

				nodeKey = Math.abs(roadUp * roadDown * roadLeft * roadRight);
				if (nodeKey != 1)
					clusterPolygon.getPoints().add(nodeKey);
			}

			roadUp = 0;
			roadDown = 0;
			roadLeft = 0;
			roadRight = 0;
			while ((roadUp != -1) || (roadDown != -1) || (roadLeft != -1) || (roadRight != -1)) {
				roadUp = extendMembraneSection(
						ClusterMapHelper.moveKeyByOffsetAndDirection(bipolarNodeKey, 1, ClusterConstants.NORTH),
						ClusterConstants.NORTH);
				roadDown = extendMembraneSection(
						ClusterMapHelper.moveKeyByOffsetAndDirection(bipolarNodeKey, 1, ClusterConstants.SOUTH),
						ClusterConstants.SOUTH);
				roadLeft = extendMembraneSection(
						ClusterMapHelper.moveKeyByOffsetAndDirection(bipolarNodeKey, 1, ClusterConstants.EAST),
						ClusterConstants.EAST);
				roadRight = extendMembraneSection(
						ClusterMapHelper.moveKeyByOffsetAndDirection(bipolarNodeKey, 1, ClusterConstants.WEST),
						ClusterConstants.WEST);

				bipolarNodeKey = Math.abs(roadUp * roadDown * roadLeft * roadRight);
				if (bipolarNodeKey != 1)
					clusterPolygon.getPoints().add(0, bipolarNodeKey);
			}
		}
		simplifyOctopian(clusterPolygon);
	}

	private void simplifyOctopian(ClusterPolygon clusterPolygon) {

		if ((clusterPolygon.getPoints().get(0) + 1) == clusterPolygon.getPoints()
				.get(clusterPolygon.getPoints().size() - 1)) {
			clusterPolygon.getPoints().remove(clusterPolygon.getPoints().size() - 1);
		}

		if ((clusterPolygon.getPoints().get(0) + 10000) == clusterPolygon.getPoints().get(1)) {
			clusterPolygon.getPoints().remove(0);
		}
	}

	private int extendMembraneSection(int beginKey, int direction) {
		if (!landMap.landPointisOnMap(beginKey)
				|| !landMap.findPoint(beginKey).getType().equals(ClusterConfiguration.NODE_MARK)) {
			return -1;
		}

		int oldKey = -1;
		while (landMap.findPoint(beginKey).getType().equals(ClusterConfiguration.NODE_MARK)) {
			oldKey = beginKey;
			landMap.findPoint(beginKey).setType(ClusterConfiguration.BORDER_MARK);
			beginKey = ClusterMapHelper.moveKeyByOffsetAndDirection(beginKey, 1, direction);
		}
		return oldKey;
	}
}