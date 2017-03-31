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
	
	public void clusterize() {
		// 1. we need to now the main route size
		ClusterLandRoute mainRoute = landMap.getLandRoute();
		// Once the collector branches are created we need to create the non
		// collector running orthogonal to the main
		List<Integer> orthogonalDirections = ClusterDirectionHelper.orthogonalDirections(mainRoute.getDirection());
		int entryPointId = mainRoute.getInitialPointId();
		while (true) {
			entryPointId = ClusterMapHelper.moveKeyByOffsetAndDirection(entryPointId,
					ClusterConfiguration.BASE_CLUSTER_SIZE + ClusterConfiguration.COLLECTOR_BRANCH_SIZE,
					mainRoute.getDirection());
			if (landMap.landPointisOnMap(entryPointId)) {
				createRouteVariation(entryPointId, orthogonalDirections.get(0), ClusterConfiguration.COLLECTOR_BRANCH);
			} else
				break;
		}
		
		//Principal routes
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
			int extraSpace = first?ClusterConfiguration.ARTERIAL_BRANCH_SIZE:0;
			first = false;
			lowerParallelId = ClusterMapHelper.moveKeyByOffsetAndDirection(lowerParallelId,
					ClusterConfiguration.BASE_CLUSTER_SIZE+extraSpace, orthogonalDirections.get(1));
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
	
	public void zonify() {
		// findZonificationAreas();
		organicZonification();
	}

	private void organicZonification() {
		// Organic zonification: This zonification searchs the figure and
		// reduces it in a reason configured by the user. if it can be done, it
		// becomes a new cluster. If not, it becomes simply defaults into a
		// perfect zonification
		// System.out.println("Limit x:" + landMap.getPointsx() + "Limit y:" +
		// landMap.getPointsy());
		// we should save the Ns used
		for (int y = landMap.getPointsy() - 1; y >= 0; y--) {
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
				boolean identifiedNormalNode = false;
				if (landMap.isNormalNode(x, y)
						&& landMap.findPoint(ClusterMapHelper.formKey((x + 1), y)).getType()
								.equals(ClusterConfiguration.NODE_MARK)
						&& landMap.findPoint(ClusterMapHelper.formKey((x + 1), (y + 1))).getType()
								.equals(ClusterConfiguration.EMPTY_MARK)) {
					identifiedNormalNode = true;
					ClusterPolygon clusterPolygon = new ClusterPolygon();
					/* int nextPoint= */
					createOrganicCoverture(ClusterMapHelper.formKey(x, y), ClusterConstants.EAST, clusterPolygon);
					boolean passedThough = false;
					if (clusterPolygon.getPoints().size() == 2) {
						// TODO finish the polygon and detect wereas is not
						// complete
						completeOrganicCoverture(ClusterMapHelper.formKey(x, y), ClusterConstants.NORTH,
								clusterPolygon);
						clusterPolygon.rehashPolygon(ClusterConfiguration.TYPE_COMPLETE);
						passedThough = true;
					}

					if (!clusterPolygon.isComplete() && clusterPolygon.getPoints().size() > 2) {
						System.out.println("Pre polygon join with border ");
						clusterPolygon.printPolygon();
						// clusterPolygon =
						// landMap.joinWithPolygonalBorder(clusterPolygon);
					}

					if (!passedThough && !clusterPolygon.isComplete() && clusterPolygon.getPoints().size() == 3) {
						// TODO solve the problem of unresolved polygon
						completeOrganicCoverture(ClusterMapHelper.formKey(x, y), ClusterConstants.NORTH,
								clusterPolygon);
						clusterPolygon.rehashPolygon(ClusterConfiguration.TYPE_COMPLETE);
						// clusterPolygon =
						// landMap.joinWithPolygonalBorder(clusterPolygon);
					}
					// clusterPolygon.printPolygon();
					clusterPolygon.setCentroid(clusterPolygon.findCentroid());

					// we create the park
					List<List<Integer>> grass = clusterPolygon.parkZone(
							ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2 + ClusterConfiguration.LOCAL_BRANCH_SIZE);
					for (int j = 0; j < grass.size(); j++) {
						landMap.createBorderFromPolygon(grass.get(j), ClusterConfiguration.PARK_MARK);
					}

					// we create the routes
					List<List<Integer>> routes = clusterPolygon.routeZone(
							ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2, ClusterConfiguration.LOCAL_BRANCH_SIZE);
					for (int j = 0; j < routes.size(); j++) {
						landMap.createBorderFromPolygon(routes.get(j), ClusterConfiguration.LOCAL_MARK);
					}

					// we create the houses
					List<List<Integer>> lowerBorder = clusterPolygon
							.routeZone(ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2 - 1, 1);
					if (lowerBorder.size() > 0) {
						landMap.lotize(lowerBorder.get(0), ClusterConstants.EAST, 0);
					}

					if ((routes.size() == 0) && (grass.size() == 0)) {
						// TODO lotize as full focus (library or other).
						// clusterPolygon.printPolygon();
					}
				}

				// TODO in case of special node (non regular spaced node
				if (!identifiedNormalNode && landMap.isSpecialNode(x, y)) {
					ClusterPolygon clusterPolygon = new ClusterPolygon();
					createSpecialCoverture(ClusterMapHelper.formKey(x, y), ClusterConstants.NORTH, clusterPolygon);
					System.out.println("Special polygon");
					clusterPolygon.printPolygon();
					clusterPolygon.rehashPolygon(ClusterConfiguration.TYPE_SPECIAL);
					// clusterPolygon.printPolygon();
					clusterPolygon.setCentroid(clusterPolygon.findCentroid());

					// we create the park
					List<List<Integer>> grass = clusterPolygon.parkZone(
							ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2 + ClusterConfiguration.LOCAL_BRANCH_SIZE);
					for (int j = 0; j < grass.size(); j++) {
						landMap.createBorderFromPolygon(grass.get(j), ClusterConfiguration.PARK_MARK);
					}

					// we create the routes
					List<List<Integer>> routes = clusterPolygon.routeZone(
							ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2, ClusterConfiguration.LOCAL_BRANCH_SIZE);
					for (int j = 0; j < routes.size(); j++) {
						landMap.createBorderFromPolygon(routes.get(j), ClusterConfiguration.LOCAL_MARK);
					}

					// we create the houses
					List<List<Integer>> lowerBorder = clusterPolygon
							.routeZone(ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2 - 1, 1);
					if (lowerBorder.size() > 0) {
						int direction = ClusterDirectionHelper.orthogonalDirectionFromPointToPoint(
								lowerBorder.get(0).get(0), lowerBorder.get(0).get(1));
						landMap.lotize(lowerBorder.get(0), direction, 0);
					}

					if ((routes.size() == 0) && (grass.size() == 0)) {
						// TODO lotize as full focus (library or other).
						// clusterPolygon.printPolygon();
					}
				}
			}
		}
		// perfectZonification();
	}

	private Object createSpecialCoverture(int initialKey, int direction, ClusterPolygon clusterPolygon) {
		// TODO Auto-generated method stub
		clusterPolygon.getPoints().add(initialKey);
		boolean borderFound = false;
		while (!borderFound) {
			initialKey = ClusterMapHelper.moveKeyByOffsetAndDirection(initialKey, 1, direction);
			int[] firstNode = ClusterMapHelper.breakKey(initialKey);
			switch (direction) {
			case ClusterConstants.NORTH:
				if (landMap.findPoint(ClusterMapHelper.formKey(firstNode[0] + 1, firstNode[1])).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					return createSpecialCoverture(initialKey, ClusterConstants.EAST, clusterPolygon);
				}
				break;
			case ClusterConstants.EAST:
				// east, north, west, south
				if (landMap.findPoint(ClusterMapHelper.formKey(firstNode[0], firstNode[1] - 1)).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					return createSpecialCoverture(initialKey, ClusterConstants.SOUTH, clusterPolygon);
				}
				break;
			case ClusterConstants.SOUTH:
				if (landMap.findPoint(ClusterMapHelper.formKey(firstNode[0] - 1, firstNode[1])).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					return createSpecialCoverture(initialKey, ClusterConstants.WEST, clusterPolygon);

				}
				break;
			case ClusterConstants.WEST:
				if (landMap.findPoint(ClusterMapHelper.formKey(firstNode[0], firstNode[1] + 1)).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					borderFound = true;
					clusterPolygon.setComplete(true);
				}
				break;
			}

			if (!clusterPolygon.isComplete() && landMap.findPoint(ClusterMapHelper.formKey(firstNode[0], firstNode[1]))
					.getType().equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
				switch (direction) {
				case ClusterConstants.EAST:
					clusterPolygon.getPoints().add(ClusterMapHelper.formKey(firstNode[0] - 1, firstNode[1]));
					break;
				case ClusterConstants.NORTH:
					clusterPolygon.getPoints().add(ClusterMapHelper.formKey(firstNode[0], firstNode[1] - 1));
					break;
				case ClusterConstants.WEST:
					clusterPolygon.getPoints().add(ClusterMapHelper.formKey(firstNode[0] + 1, firstNode[1]));
					break;
				case ClusterConstants.SOUTH:
					clusterPolygon.getPoints().add(ClusterMapHelper.formKey(firstNode[0], firstNode[1] + 1));
					break;
				}
				return -1;
			}
			landMap.findPoint(ClusterMapHelper.formKey(firstNode[0], firstNode[1]))
					.setType(ClusterConfiguration.BORDER_MARK);
		}
		return initialKey;
	}

	/**
	 * In case there is not a direct square or a lateral east, north west
	 * polygon, we seize the opposite side to complete the polygon
	 */
	private Object completeOrganicCoverture(int initialKey, int direction, ClusterPolygon clusterPolygon) {
		if (direction != ClusterConstants.NORTH) {
			int numExpansions = clusterPolygon.getExpansions() + 1;
			clusterPolygon.setExpansions(numExpansions);
			clusterPolygon.getPoints().add(0, initialKey);
		}

		boolean borderFound = false;
		while (!borderFound) {
			initialKey = ClusterMapHelper.moveKeyByOffsetAndDirection(initialKey, 1, direction);
			int[] firstNode = ClusterMapHelper.breakKey(initialKey);
			switch (direction) {
			case ClusterConstants.EAST:
				// east, north, west, south
				if (landMap.findPoint(ClusterMapHelper.formKey(firstNode[0], firstNode[1] - 1)).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					return completeOrganicCoverture(initialKey, ClusterConstants.SOUTH, clusterPolygon);
				}
				break;
			case ClusterConstants.NORTH:
				if (landMap.findPoint(ClusterMapHelper.formKey(firstNode[0] + 1, firstNode[1])).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					return completeOrganicCoverture(initialKey, ClusterConstants.EAST, clusterPolygon);
				}
				break;
			case ClusterConstants.WEST:
				if (landMap.findPoint(ClusterMapHelper.formKey(firstNode[0], firstNode[1] + 1)).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					borderFound = true;
					clusterPolygon.setComplete(true);
				}
				break;
			case ClusterConstants.SOUTH:
				if (landMap.findPoint(ClusterMapHelper.formKey(firstNode[0] - 1, firstNode[1])).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					return completeOrganicCoverture(initialKey, ClusterConstants.WEST, clusterPolygon);

				}
				break;
			}

			if (!clusterPolygon.isComplete() && landMap.findPoint(ClusterMapHelper.formKey(firstNode[0], firstNode[1]))
					.getType().equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
				switch (direction) {
				case ClusterConstants.EAST:
					clusterPolygon.getPoints().add(0, ClusterMapHelper.formKey(firstNode[0] - 1, firstNode[1]));
					break;
				case ClusterConstants.NORTH:
					clusterPolygon.getPoints().add(0, ClusterMapHelper.formKey(firstNode[0], firstNode[1] - 1));
					break;
				case ClusterConstants.WEST:
					clusterPolygon.getPoints().add(0, ClusterMapHelper.formKey(firstNode[0] + 1, firstNode[1]));
					break;
				case ClusterConstants.SOUTH:
					clusterPolygon.getPoints().add(0, ClusterMapHelper.formKey(firstNode[0], firstNode[1] + 1));
					break;
				}
				return -1;
			}
			landMap.findPoint(ClusterMapHelper.formKey(firstNode[0], firstNode[1]))
					.setType(ClusterConfiguration.BORDER_MARK);
		}
		return initialKey;
	}

	private int createOrganicCoverture(int initialKey, int direction, ClusterPolygon clusterPolygon) {
		clusterPolygon.getPoints().add(initialKey);
		boolean borderFound = false;
		while (!borderFound) {
			initialKey = ClusterMapHelper.moveKeyByOffsetAndDirection(initialKey, 1, direction);
			int[] firstNode = ClusterMapHelper.breakKey(initialKey);
			switch (direction) {
			case ClusterConstants.EAST:
				// east, north, west, south
				if (landMap.findPoint(ClusterMapHelper.formKey(firstNode[0], firstNode[1] + 1)).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					return createOrganicCoverture(initialKey, ClusterConstants.NORTH, clusterPolygon);
				}
				break;
			case ClusterConstants.NORTH:
				if (landMap.findPoint(ClusterMapHelper.formKey(firstNode[0] - 1, firstNode[1])).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					return createOrganicCoverture(initialKey, ClusterConstants.WEST, clusterPolygon);
				}
				break;
			case ClusterConstants.WEST:
				if (landMap.findPoint(ClusterMapHelper.formKey(firstNode[0], firstNode[1] - 1)).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					return createOrganicCoverture(initialKey, ClusterConstants.SOUTH, clusterPolygon);
				}
				break;
			case ClusterConstants.SOUTH:
				if (landMap.findPoint(ClusterMapHelper.formKey(firstNode[0] + 1, firstNode[1])).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					borderFound = true;
					clusterPolygon.setComplete(true);
				}
				break;
			}

			if (!clusterPolygon.isComplete() && landMap.findPoint(ClusterMapHelper.formKey(firstNode[0], firstNode[1]))
					.getType().equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
				switch (direction) {
				case ClusterConstants.EAST:
					clusterPolygon.getPoints().add(ClusterMapHelper.formKey(firstNode[0] - 1, firstNode[1]));
					break;
				case ClusterConstants.NORTH:
					clusterPolygon.getPoints().add(ClusterMapHelper.formKey(firstNode[0], firstNode[1] - 1));
					break;
				case ClusterConstants.WEST:
					clusterPolygon.getPoints().add(ClusterMapHelper.formKey(firstNode[0] + 1, firstNode[1]));
					break;
				case ClusterConstants.SOUTH:
					clusterPolygon.getPoints().add(ClusterMapHelper.formKey(firstNode[0], firstNode[1] + 1));
					break;
				}
				return -1;
			}
			landMap.findPoint(ClusterMapHelper.formKey(firstNode[0], firstNode[1]))
					.setType(ClusterConfiguration.BORDER_MARK);
		}
		return initialKey;
	}
}