package algorithm.radialVariation;

import java.util.ArrayList;
import java.util.List;

import helpers.radialVariation.RadialDirectionHelper;
import helpers.radialVariation.RadialMapHelper;
import interfaces.radialVariation.RadialConfiguration;
import interfaces.radialVariation.RadialConstants;
import models.radialVariation.RadialLandMap;
import models.radialVariation.RadialLandPoint;
import models.radialVariation.RadialLandRoute;
import models.radialVariation.RadialPolygon;

public class radialAlgorithm {
	private RadialLandMap landMap;

	public RadialLandMap getLandMap() {
		return landMap;
	}

	public void setLandMap(RadialLandMap landMap) {
		this.landMap = landMap;
	}

	public void CreateRadialWeb() {
		List<List<Integer>> layersPolygon = new ArrayList<>();
		List<Integer> localLayer;
		RadialPolygon polygon = new RadialPolygon();

		// select the first layer
		localLayer = new ArrayList<>();
		for(int i=0; i<landMap.getPolygonNodes().size(); i++){
			localLayer.add(landMap.getPolygonNodes().get(i).getId());
		}

		polygon.setMapPoints(localLayer);
		polygon.setComplete(true);
		layersPolygon.add(localLayer); 
		localLayer = polygon.vectorShrinking(RadialConfiguration.HOUSE_SIDE_MAXIMUN_SIZE);
		layersPolygon.add(localLayer);
		localLayer = polygon.vectorShrinking(RadialConfiguration.HOUSE_SIDE_MAXIMUN_SIZE * 2);
		layersPolygon.add(localLayer);
		for (int j = 0; j < layersPolygon.size(); j++) {
			landMap.createBorderFromPolygon(layersPolygon.get(j), RadialConfiguration.LOCAL_MARK);
		}

		// create the routes
		List<List<Integer>> routes = polygon.routeZone(RadialConfiguration.HOUSE_SIDE_MAXIMUN_SIZE * 2,
				RadialConfiguration.LOCAL_BRANCH_SIZE);
		if (routes.size() > 6) {
			for (int j = 0; j < routes.size(); j++) {
				landMap.createBorderFromPolygon(routes.get(j), RadialConfiguration.LOCAL_MARK);
			}
		} else {
			routes = new ArrayList<>();
		}

	}

	public void Radialize() {
		// 1. we need to now the main route size
		RadialLandRoute mainRoute = landMap.getLandRoute();
		// Once the collector branches are created we need to create the non
		// collector running orthogonal to the main
		List<Integer> orthogonalDirections = RadialDirectionHelper.orthogonalDirections(mainRoute.getDirection());
		int entryPointId = mainRoute.getInitialPointId();
		while (true) {
			entryPointId = RadialMapHelper.moveKeyByOffsetAndDirection(entryPointId,
					RadialConfiguration.BASE_RADIAL_SIZE + RadialConfiguration.COLLECTOR_BRANCH_SIZE,
					mainRoute.getDirection());
			if (landMap.landPointisOnMap(entryPointId)) {
				createRouteVariation(entryPointId, orthogonalDirections.get(0), RadialConfiguration.COLLECTOR_BRANCH);
			} else
				break;
		}

		// Principal routes
		int upperParallelId = mainRoute.getInitialPointId();
		while (true) {
			upperParallelId = RadialMapHelper.moveKeyByOffsetAndDirection(upperParallelId,
					RadialConfiguration.BASE_RADIAL_SIZE, orthogonalDirections.get(0));
			if (!landMap.landPointisOnMap(upperParallelId))
				break;
			createRouteVariation(upperParallelId, mainRoute.getDirection(), RadialConfiguration.LOCAL_BRANCH);
		}

		int lowerParallelId = mainRoute.getInitialPointId();
		boolean first = true;
		while (true) {
			int extraSpace = first ? RadialConfiguration.ARTERIAL_BRANCH_SIZE : 0;
			first = false;
			lowerParallelId = RadialMapHelper.moveKeyByOffsetAndDirection(lowerParallelId,
					RadialConfiguration.BASE_RADIAL_SIZE + extraSpace, orthogonalDirections.get(1));
			if (!landMap.landPointisOnMap(lowerParallelId))
				break;
			createRouteVariation(lowerParallelId, mainRoute.getDirection(), RadialConfiguration.LOCAL_BRANCH);
		}
	}

	public void createRouteVariation(int axisPoint, int direction, int branchType) {
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

		// It is necessary to test that the lower and upper key are still on the
		// map.It would be impossible otherwise to create
		int upperLimitKey = RadialMapHelper.moveKeyByOffsetAndDirection(axisPoint, 1,
				RadialDirectionHelper.oppositeDirection(growDirection));
		int lowerLimitKey = RadialMapHelper.moveKeyByOffsetAndDirection(axisPoint, extension, growDirection);

		if (!landMap.landPointisOnMap(upperLimitKey) || !landMap.landPointisOnMap(lowerLimitKey)) {
			return;
		}

		createLine(upperLimitKey, direction, RadialConfiguration.NODE_MARK);
		createLine(lowerLimitKey, direction, RadialConfiguration.NODE_MARK);

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

	private int createLine(int givenXY, int direction, String markType) {
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

	private boolean markPoint(int[] newXY, String markType, Boolean in, Boolean out) {
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

	public void zonify() {
		// findZonificationAreas();
		organicZonification();
	}

	// Organic zonification: This zonification searchs the figure and
	// reduces it in a reason configured by the user. if it can be done, it
	// becomes a new Radial. If not, it becomes simply defaults into a
	// perfect zonification
	private void organicZonification() {
		for (int y = landMap.getPointsy() - 1; y >= 0; y--) {
			boolean insidePolygon = false;
			for (int x = 0; x < landMap.getPointsx(); x++) {
				if (insidePolygon && landMap.findPoint(RadialMapHelper.formKey(x, y)).getType()
						.equals(RadialConfiguration.OUTSIDE_POLYGON_MARK)) {
					break;
				}
				if (!insidePolygon && !landMap.findPoint(RadialMapHelper.formKey(x, y)).getType()
						.equals(RadialConfiguration.OUTSIDE_POLYGON_MARK)) {
					insidePolygon = true;
				}
				boolean identifiedNormalNode = false;
				if (landMap.isNormalNode(x, y)
						&& landMap.findPoint(RadialMapHelper.formKey((x + 1), y)).getType()
								.equals(RadialConfiguration.NODE_MARK)
						&& landMap.findPoint(RadialMapHelper.formKey((x + 1), (y + 1))).getType()
								.equals(RadialConfiguration.EMPTY_MARK)) {
					identifiedNormalNode = true;
					RadialPolygon RadialPolygon = new RadialPolygon();
					/* int nextPoint= */
					createOrganicCoverture(RadialMapHelper.formKey(x, y), RadialConstants.EAST, RadialPolygon);
					boolean passedThough = false;
					if (RadialPolygon.getPoints().size() == 2) {
						// finish the polygon and detect incompleteness
						completeOrganicCoverture(RadialMapHelper.formKey(x, y), RadialConstants.NORTH, RadialPolygon);
						RadialPolygon.rehashPolygon(RadialConfiguration.TYPE_COMPLETE);
						passedThough = true;
					}

					if (!RadialPolygon.isComplete() && RadialPolygon.getPoints().size() > 2) {
						System.out.println("Pre polygon join with border ");
						RadialPolygon.printPolygon();
						// RadialPolygon =
						// joinWithPolygonalBorder(RadialPolygon);
					}

					if (!passedThough && !RadialPolygon.isComplete() && RadialPolygon.getPoints().size() == 3) {
						completeOrganicCoverture(RadialMapHelper.formKey(x, y), RadialConstants.NORTH, RadialPolygon);
						RadialPolygon.rehashPolygon(RadialConfiguration.TYPE_COMPLETE);
						// RadialPolygon =
						// joinWithPolygonalBorder(RadialPolygon);
					}
					RadialPolygon.setCentroid(RadialPolygon.findCentroid());

					// we create the park
					List<List<Integer>> grass = RadialPolygon.parkZone(
							RadialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2 + RadialConfiguration.LOCAL_BRANCH_SIZE);
					for (int j = 0; j < grass.size(); j++) {
						landMap.createBorderFromPolygon(grass.get(j), RadialConfiguration.PARK_MARK);
					}

					// we create the routes
					List<List<Integer>> routes = RadialPolygon.routeZone(
							RadialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2, RadialConfiguration.LOCAL_BRANCH_SIZE);
					if (routes.size() > 6) {
						for (int j = 0; j < routes.size(); j++) {
							landMap.createBorderFromPolygon(routes.get(j), RadialConfiguration.LOCAL_MARK);
						}
					} else {
						routes = new ArrayList<>();
					}

					// we create the houses
					List<List<Integer>> lowerBorder = RadialPolygon
							.routeZone(RadialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2 - 1, 1);
					if (lowerBorder.size() > 0) {
						landMap.lotize(lowerBorder.get(0), RadialConstants.EAST, 0);
					}

					if ((routes.size() == 0)) {
						List<List<Integer>> contribution = RadialPolygon.contributionZone();
						for (int j = 0; j < contribution.size(); j++) {
							landMap.createBorderFromPolygon(contribution.get(j), RadialConfiguration.CONTRIBUTION_MARK);
						}
					}
				}

				// In case of special node (non regular spaced node
				if (!identifiedNormalNode && landMap.isSpecialNode(x, y)) {
					RadialPolygon RadialPolygon = new RadialPolygon();
					createSpecialCoverture(RadialMapHelper.formKey(x, y), RadialConstants.NORTH, RadialPolygon);
					System.out.println("Special polygon");
					RadialPolygon.printPolygon();
					RadialPolygon.rehashPolygon(RadialConfiguration.TYPE_SPECIAL);
					RadialPolygon.setCentroid(RadialPolygon.findCentroid());

					// we create the park
					List<List<Integer>> grass = RadialPolygon.parkZone(
							RadialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2 + RadialConfiguration.LOCAL_BRANCH_SIZE);
					for (int j = 0; j < grass.size(); j++) {
						landMap.createBorderFromPolygon(grass.get(j), RadialConfiguration.PARK_MARK);
					}

					// we create the routes
					List<List<Integer>> routes = RadialPolygon.routeZone(
							RadialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2, RadialConfiguration.LOCAL_BRANCH_SIZE);
					if (routes.size() > 6) {
						for (int j = 0; j < routes.size(); j++) {
							landMap.createBorderFromPolygon(routes.get(j), RadialConfiguration.LOCAL_MARK);
						}
					} else {
						routes = new ArrayList<>();
					}

					// we create the houses
					List<List<Integer>> lowerBorder = RadialPolygon
							.routeZone(RadialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2 - 1, 1);
					if (lowerBorder.size() > 0) {
						int direction = RadialDirectionHelper.orthogonalDirectionFromPointToPoint(
								lowerBorder.get(0).get(0), lowerBorder.get(0).get(1));
						landMap.lotize(lowerBorder.get(0), direction, 0);
					}

					if ((routes.size() == 0)) {
						List<List<Integer>> contribution = RadialPolygon.contributionZone();
						for (int j = 0; j < contribution.size(); j++) {
							landMap.createBorderFromPolygon(contribution.get(j), RadialConfiguration.CONTRIBUTION_MARK);
						}
					}
				}
			}
		}
		// perfectZonification();
	}

	private Object createSpecialCoverture(int initialKey, int direction, RadialPolygon RadialPolygon) {
		RadialPolygon.getPoints().add(initialKey);
		boolean borderFound = false;
		while (!borderFound) {
			initialKey = RadialMapHelper.moveKeyByOffsetAndDirection(initialKey, 1, direction);
			int[] firstNode = RadialMapHelper.breakKey(initialKey);
			switch (direction) {
			case RadialConstants.NORTH:
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0] + 1, firstNode[1])).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					return createSpecialCoverture(initialKey, RadialConstants.EAST, RadialPolygon);
				}
				break;
			case RadialConstants.EAST:
				// east, north, west, south
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1] - 1)).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					return createSpecialCoverture(initialKey, RadialConstants.SOUTH, RadialPolygon);
				}
				break;
			case RadialConstants.SOUTH:
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0] - 1, firstNode[1])).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					return createSpecialCoverture(initialKey, RadialConstants.WEST, RadialPolygon);

				}
				break;
			case RadialConstants.WEST:
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1] + 1)).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					borderFound = true;
					RadialPolygon.setComplete(true);
				}
				break;
			}

			if (!RadialPolygon.isComplete() && landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1]))
					.getType().equals(RadialConfiguration.OUTSIDE_POLYGON_MARK)) {
				switch (direction) {
				case RadialConstants.EAST:
					RadialPolygon.getPoints().add(RadialMapHelper.formKey(firstNode[0] - 1, firstNode[1]));
					break;
				case RadialConstants.NORTH:
					RadialPolygon.getPoints().add(RadialMapHelper.formKey(firstNode[0], firstNode[1] - 1));
					break;
				case RadialConstants.WEST:
					RadialPolygon.getPoints().add(RadialMapHelper.formKey(firstNode[0] + 1, firstNode[1]));
					break;
				case RadialConstants.SOUTH:
					RadialPolygon.getPoints().add(RadialMapHelper.formKey(firstNode[0], firstNode[1] + 1));
					break;
				}
				return -1;
			}
			landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1]))
					.setType(RadialConfiguration.BORDER_MARK);
		}
		return initialKey;
	}

	/**
	 * In case there is not a direct square or a lateral east, north west
	 * polygon, we seize the opposite side to complete the polygon
	 */
	private Object completeOrganicCoverture(int initialKey, int direction, RadialPolygon RadialPolygon) {
		if (direction != RadialConstants.NORTH) {
			int numExpansions = RadialPolygon.getExpansions() + 1;
			RadialPolygon.setExpansions(numExpansions);
			RadialPolygon.getPoints().add(0, initialKey);
		}

		boolean borderFound = false;
		while (!borderFound) {
			initialKey = RadialMapHelper.moveKeyByOffsetAndDirection(initialKey, 1, direction);
			int[] firstNode = RadialMapHelper.breakKey(initialKey);
			switch (direction) {
			case RadialConstants.EAST:
				// east, north, west, south
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1] - 1)).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					return completeOrganicCoverture(initialKey, RadialConstants.SOUTH, RadialPolygon);
				}
				break;
			case RadialConstants.NORTH:
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0] + 1, firstNode[1])).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					return completeOrganicCoverture(initialKey, RadialConstants.EAST, RadialPolygon);
				}
				break;
			case RadialConstants.WEST:
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1] + 1)).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					borderFound = true;
					RadialPolygon.setComplete(true);
				}
				break;
			case RadialConstants.SOUTH:
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0] - 1, firstNode[1])).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					return completeOrganicCoverture(initialKey, RadialConstants.WEST, RadialPolygon);

				}
				break;
			}

			if (!RadialPolygon.isComplete() && landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1]))
					.getType().equals(RadialConfiguration.OUTSIDE_POLYGON_MARK)) {
				switch (direction) {
				case RadialConstants.EAST:
					RadialPolygon.getPoints().add(0, RadialMapHelper.formKey(firstNode[0] - 1, firstNode[1]));
					break;
				case RadialConstants.NORTH:
					RadialPolygon.getPoints().add(0, RadialMapHelper.formKey(firstNode[0], firstNode[1] - 1));
					break;
				case RadialConstants.WEST:
					RadialPolygon.getPoints().add(0, RadialMapHelper.formKey(firstNode[0] + 1, firstNode[1]));
					break;
				case RadialConstants.SOUTH:
					RadialPolygon.getPoints().add(0, RadialMapHelper.formKey(firstNode[0], firstNode[1] + 1));
					break;
				}
				return -1;
			}
			landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1]))
					.setType(RadialConfiguration.BORDER_MARK);
		}
		return initialKey;
	}

	private int createOrganicCoverture(int initialKey, int direction, RadialPolygon RadialPolygon) {
		RadialPolygon.getPoints().add(initialKey);
		boolean borderFound = false;
		while (!borderFound) {
			initialKey = RadialMapHelper.moveKeyByOffsetAndDirection(initialKey, 1, direction);
			int[] firstNode = RadialMapHelper.breakKey(initialKey);
			switch (direction) {
			case RadialConstants.EAST:
				// east, north, west, south
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1] + 1)).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					return createOrganicCoverture(initialKey, RadialConstants.NORTH, RadialPolygon);
				}
				break;
			case RadialConstants.NORTH:
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0] - 1, firstNode[1])).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					return createOrganicCoverture(initialKey, RadialConstants.WEST, RadialPolygon);
				}
				break;
			case RadialConstants.WEST:
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1] - 1)).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					return createOrganicCoverture(initialKey, RadialConstants.SOUTH, RadialPolygon);
				}
				break;
			case RadialConstants.SOUTH:
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0] + 1, firstNode[1])).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					borderFound = true;
					RadialPolygon.setComplete(true);
				}
				break;
			}

			if (!RadialPolygon.isComplete() && landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1]))
					.getType().equals(RadialConfiguration.OUTSIDE_POLYGON_MARK)) {
				switch (direction) {
				case RadialConstants.EAST:
					RadialPolygon.getPoints().add(RadialMapHelper.formKey(firstNode[0] - 1, firstNode[1]));
					break;
				case RadialConstants.NORTH:
					RadialPolygon.getPoints().add(RadialMapHelper.formKey(firstNode[0], firstNode[1] - 1));
					break;
				case RadialConstants.WEST:
					RadialPolygon.getPoints().add(RadialMapHelper.formKey(firstNode[0] + 1, firstNode[1]));
					break;
				case RadialConstants.SOUTH:
					RadialPolygon.getPoints().add(RadialMapHelper.formKey(firstNode[0], firstNode[1] + 1));
					break;
				}
				return -1;
			}
			landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1]))
					.setType(RadialConfiguration.BORDER_MARK);
		}
		return initialKey;
	}

}
