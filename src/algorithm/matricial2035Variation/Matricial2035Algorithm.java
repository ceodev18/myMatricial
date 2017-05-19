package algorithm.matricial2035Variation;

import java.util.List;

import helpers.matricial2035Variation.Matricial2035DirectionHelper;
import helpers.matricial2035Variation.Matricial2035MapHelper;
import interfaces.matricial2035Variation.Matricial2035Configuration;
import interfaces.matricial2035Variation.Matricial2035Constants;
import models.matricial2035Variation.Matricial2035LandMap;
import models.matricial2035Variation.Matricial2035LandPoint;
import models.matricial2035Variation.Matricial2035LandRoute;
import models.matricial2035Variation.Matricial2035Polygon;

public class Matricial2035Algorithm {
	private Matricial2035LandMap landMap;
	private double directionAngle; //the original direction angle before turning
	
	public void generateMatrix(Matricial2035LandPoint entryPoint) {	
	
	}
	
	public void setLandMap(Matricial2035LandMap landMap) {
		this.landMap = landMap;	
	}
	
	public Matricial2035LandMap getLandMap() {
		return landMap;
	}

	public double getDirectionAngle() {
		return directionAngle;
	}

	public void setDirectionAngle(double directionAngle) {
		this.directionAngle = directionAngle;
	}
	
	public void clusterize(Matricial2035LandPoint entryPoint) {
		// 1. we need to now the main route size
		int direction = Matricial2035DirectionHelper.orthogonalDirectionFromPointToPoint(entryPoint, landMap.getCentroid());

		if (landMap.getPointsx() > 1000 && landMap.getPointsy() > 1000) {
			createRouteVariation(entryPoint.getId(), direction, Matricial2035Configuration.ARTERIAL_BRANCH);
		} else if (landMap.getPointsx() > 600 && landMap.getPointsy() > 600) {
			createRouteVariation(entryPoint.getId(), direction, Matricial2035Configuration.COLLECTOR_BRANCH);
		} else {
			createRouteVariation(entryPoint.getId(), direction, Matricial2035Configuration.LOCAL_BRANCH);
		}

		if (landMap.getLandRoutes().size() == 0) {
			createRouteVariation(entryPoint.getId(), direction, Matricial2035Configuration.COLLECTOR_BRANCH);
		}
		if (landMap.getLandRoutes().size() == 0) {
			createRouteVariation(entryPoint.getId(), direction, Matricial2035Configuration.LOCAL_BRANCH);
		}
		Matricial2035LandRoute mainRoute = landMap.getLandRoutes().get(0);
		// Once the collector branches are created we need to create the non
		// collector running orthogonal to the main
		List<Integer> orthogonalDirections = Matricial2035DirectionHelper.orthogonalDirections(mainRoute.getDirection());
		int entryPointId = mainRoute.getInitialPointId();
		if (mainRoute.getDirection() == Matricial2035Constants.EAST) {
			int[] xy = Matricial2035MapHelper.breakKey(entryPointId);
			xy[0] = 1;
			entryPointId = Matricial2035MapHelper.formKey(xy[0], xy[1]);
		} else if (mainRoute.getDirection() == Matricial2035Constants.WEST) {
			int[] xy = Matricial2035MapHelper.breakKey(entryPointId);
			xy[0] = landMap.getPointsx() - 2;
			entryPointId = Matricial2035MapHelper.formKey(xy[0], xy[1]);
		} else if (mainRoute.getDirection() == Matricial2035Constants.NORTH) {
			int[] xy = Matricial2035MapHelper.breakKey(entryPointId);
			xy[1] = 1;
			entryPointId = Matricial2035MapHelper.formKey(xy[0], xy[1]);
		} else if (mainRoute.getDirection() == Matricial2035Constants.SOUTH) {
			int[] xy = Matricial2035MapHelper.breakKey(entryPointId);
			xy[1] = landMap.getPointsy() - 2;
			entryPointId = Matricial2035MapHelper.formKey(xy[0], xy[1]);
		}

		while (true) {
			entryPointId = Matricial2035MapHelper.moveKeyByOffsetAndDirection(entryPointId,
					landMap.getConfiguration().getBlockConfiguration().getSideSize(), mainRoute.getDirection());
			if (landMap.landPointisOnMap(entryPointId)) {
				createRouteVariation(entryPointId, orthogonalDirections.get(0), Matricial2035Configuration.COLLECTOR_BRANCH);
			} else
				break;
		}

		// Local routes parallel to main route
		int upperParallelId = mainRoute.getInitialPointId();
		boolean first = true;
		while (true) {
			int extraSpace;
			if (first) {
				extraSpace = findExtraSpace(mainRoute.getDirection(), "upper");
			} else {
				extraSpace = 0;
			}
			first = false;
			upperParallelId = Matricial2035MapHelper.moveKeyByOffsetAndDirection(upperParallelId,
					landMap.getConfiguration().getBlockConfiguration().getSideSize() + extraSpace,
					orthogonalDirections.get(0));
			if (!landMap.landPointisOnMap(upperParallelId))
				break;
			createRouteVariation(upperParallelId, mainRoute.getDirection(), Matricial2035Configuration.LOCAL_BRANCH);
		}

		int lowerParallelId = mainRoute.getInitialPointId();
		first = true;
		while (true) {
			int extraSpace;
			if (first) {
				extraSpace = findExtraSpace(mainRoute.getDirection(), "lower");
			} else {
				extraSpace = 0;
			}
			first = false;
			lowerParallelId = Matricial2035MapHelper.moveKeyByOffsetAndDirection(lowerParallelId,
					landMap.getConfiguration().getBlockConfiguration().getSideSize() + extraSpace,
					orthogonalDirections.get(1));
			if (!landMap.landPointisOnMap(lowerParallelId))
				break;
			createRouteVariation(lowerParallelId, mainRoute.getDirection(), Matricial2035Configuration.LOCAL_BRANCH);
		}
	}

	private int findExtraSpace(int direction, String parallel) {
		int extraSpace = 0;
		switch (direction) {
		case Matricial2035Constants.NORTH:
			if (parallel.equals("upper")) {
				extraSpace = 0;
			} else {
				extraSpace = Matricial2035Configuration.ARTERIAL_BRANCH_SIZE - 12;
			}
			break;
		case Matricial2035Constants.SOUTH:
			if (parallel.equals("upper")) {
				extraSpace = Matricial2035Configuration.ARTERIAL_BRANCH_SIZE - 12;
			} else {
				extraSpace = 0;
			}
			break;
		case Matricial2035Constants.EAST:
			if (parallel.equals("upper")) {
				extraSpace = Matricial2035Configuration.ARTERIAL_BRANCH_SIZE - 12;
			} else {
				extraSpace = 0;
			}
			break;
		case Matricial2035Constants.WEST:
			if (parallel.equals("upper")) {
				extraSpace = 0;
			} else {
				extraSpace = Matricial2035Configuration.ARTERIAL_BRANCH_SIZE - 12;
			}
			break;
		}
		return extraSpace;
	}

	public void createRouteVariation(int axisPoint, int direction, int branchType) {
		int extension = 0;
		String markType = "";
		int growDirection = -1;
		Matricial2035LandRoute clusterLandRoute = null;
		int trueAxis = axisPoint;
		while (landMap.landPointisOnMap(trueAxis)
				&& landMap.getLandPoint(trueAxis).getType().equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK)) {
			trueAxis = Matricial2035MapHelper.moveKeyByOffsetAndDirection(trueAxis, 1, direction);
		}

		switch (branchType) {
		case Matricial2035Configuration.ARTERIAL_BRANCH:
			extension = Matricial2035Configuration.ARTERIAL_BRANCH_SIZE;
			markType = Matricial2035Configuration.ARTERIAL_MARK;
			clusterLandRoute = new Matricial2035LandRoute(trueAxis, direction, "a");
			break;
		case Matricial2035Configuration.COLLECTOR_BRANCH:
			extension = Matricial2035Configuration.COLLECTOR_BRANCH_SIZE;
			markType = Matricial2035Configuration.COLLECTOR_MARK;
			clusterLandRoute = new Matricial2035LandRoute(trueAxis, direction, "b");
			break;
		case Matricial2035Configuration.LOCAL_BRANCH:
			extension = Matricial2035Configuration.LOCAL_BRANCH_SIZE;
			markType = Matricial2035Configuration.LOCAL_MARK;
			clusterLandRoute = new Matricial2035LandRoute(trueAxis, direction, "c");
			break;
		case Matricial2035Configuration.WALK_BRANCH:
			extension = Matricial2035Configuration.WALK_BRANCH_SIZE;
			markType = Matricial2035Configuration.WALK_MARK;
			break;
		}

		switch (direction) {
		case Matricial2035Constants.EAST:
			growDirection = Matricial2035Constants.NORTH;
			break;
		case Matricial2035Constants.NORTH:
			growDirection = Matricial2035Constants.WEST;
			break;
		case Matricial2035Constants.WEST:
			growDirection = Matricial2035Constants.SOUTH;
			break;
		case Matricial2035Constants.SOUTH:
			growDirection = Matricial2035Constants.EAST;
			break;
		}

		// It is necessary to test that the lower and upper key are still on the
		// map.It would be impossible otherwise to create

		int upperLimitKey = Matricial2035MapHelper.moveKeyByOffsetAndDirection(axisPoint, 1,
				Matricial2035DirectionHelper.oppositeDirection(growDirection));
		int lowerLimitKey = Matricial2035MapHelper.moveKeyByOffsetAndDirection(axisPoint, extension, growDirection);

		boolean reverse = false;
		if (!landMap.landPointisOnMap(upperLimitKey) || !landMap.landPointisOnMap(lowerLimitKey)) {
			upperLimitKey = Matricial2035MapHelper.moveKeyByOffsetAndDirection(axisPoint, extension,Matricial2035DirectionHelper.oppositeDirection( growDirection));
			lowerLimitKey = Matricial2035MapHelper.moveKeyByOffsetAndDirection(axisPoint, 1, growDirection);
			if (!landMap.landPointisOnMap(upperLimitKey) || !landMap.landPointisOnMap(lowerLimitKey)) {
				return;				
			} else {
				reverse = true;
			}
		}

		createLine(upperLimitKey, direction, Matricial2035Configuration.NODE_MARK);
		createLine(lowerLimitKey, direction, Matricial2035Configuration.NODE_MARK);

		landMap.getLandRoutes().add(clusterLandRoute);
		for (int i = 0; i < extension; i++) {
			if(reverse){
				createLine(Matricial2035MapHelper.moveKeyByOffsetAndDirection(axisPoint, i, Matricial2035DirectionHelper.oppositeDirection(growDirection)), direction, markType);
			}else{
				createLine(Matricial2035MapHelper.moveKeyByOffsetAndDirection(axisPoint, i, growDirection), direction, markType);				
			}

		}
	}

	private int createLine(int givenXY, int direction, String markType) {
		int[] xy = Matricial2035MapHelper.breakKey(givenXY);
		int[] newXY = new int[2];
		Boolean in = false, out = false, changed = false;
		if ((direction == Matricial2035Constants.NORTH) || (direction == Matricial2035Constants.SOUTH)) {
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
		Matricial2035LandPoint clusterLandPoint = landMap.findPoint(Matricial2035MapHelper.formKey(newXY[0], newXY[1]));
		if (!in.booleanValue() && !clusterLandPoint.isMapLimit()) {
			changed = true;
			in = true;
		}

		if (in.booleanValue() && clusterLandPoint.isMapLimit()) {
			changed = true;
		}

		if (in.booleanValue()) {
			if (markType.equals(Matricial2035Configuration.NODE_MARK)) {
				if (clusterLandPoint.getType().equals(Matricial2035Configuration.EMPTY_MARK)) {
					clusterLandPoint.setType(markType);
				}
			} else {
				if (clusterLandPoint.getType().equals(Matricial2035Configuration.EMPTY_MARK)
						|| clusterLandPoint.getType().equals(Matricial2035Configuration.NODE_MARK)) {
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
	public /* List<Matricial2035Polygon> */void zonify() {
		// List<Matricial2035Polygon> clusterPolygons = new
		// ArrayList<Matricial2035Polygon>();
		for (int y = 0; y < landMap.getPointsy(); y++) {
			boolean insidePolygon = false;
			for (int x = 0; x < landMap.getPointsx(); x++) {

				if (insidePolygon && landMap.findPoint(Matricial2035MapHelper.formKey(x, y)).getType()
						.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK)) {
					break;
				}
				if (!insidePolygon && !landMap.findPoint(Matricial2035MapHelper.formKey(x, y)).getType()
						.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK)) {
					insidePolygon = true;
				}

				if (landMap.findPoint(Matricial2035MapHelper.formKey(x, y)).getType()
						.equals(Matricial2035Configuration.NODE_MARK)) {
					Matricial2035Polygon clusterPolygon = new Matricial2035Polygon();
					createOctopianCoverture(Matricial2035MapHelper.formKey(x, y), clusterPolygon);
					if (clusterPolygon.getPoints().size() < 3)
						continue;
					clusterPolygon.setCentroid(clusterPolygon.findCentroid());
					// we create the park
					List<List<Integer>> grass = clusterPolygon
							.parkZone(landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2
									+ Matricial2035Configuration.LOCAL_BRANCH_SIZE);
					if (grass.size() == 0) {
						int maximunTries = 6;
						landMap.impreciseLotization(clusterPolygon, maximunTries);
					} else {
						List<Integer> grassLandBorder = grass.get(0);
						String grassGrammar = "g-";
						for (int i = 0; i < grassLandBorder.size(); i++) {
							int[] xy = Matricial2035MapHelper.breakKey(grassLandBorder.get(i));
							grassGrammar += xy[0] + "-" + xy[1];
							if (i + 1 < grassLandBorder.size()) {
								grassGrammar += "-";
							}
						}
						landMap.findPoint(grassLandBorder.get(0)).setGramaticalType(grassGrammar);

						for (int j = 0; j < grass.size(); j++) {
							landMap.createBorderFromPolygon(grass.get(j), Matricial2035Configuration.PARK_MARK);
						}

						// we create the routes
						List<List<Integer>> routes = clusterPolygon.routeZone(
								landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2,
								Matricial2035Configuration.LOCAL_BRANCH_SIZE);
						for (int j = 0; j < routes.size(); j++) {
							landMap.createBorderFromPolygon(routes.get(j), Matricial2035Configuration.LOCAL_MARK);
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
		Matricial2035Polygon clusterMap = landMap.getAsMatricial2035Polygon();
		for (int i = 0; i < clusterMap.getPoints().size(); i++) {
			int[] initialXY = Matricial2035MapHelper.breakKey(clusterMap.getPoints().get(i));
			int[] finalXY = Matricial2035MapHelper
					.breakKey(clusterMap.getPoints().get((i + 1) % clusterMap.getPoints().size()));

			int driveDirection = -1;
			int growDirection = -1;

			if ((initialXY[0] != finalXY[0]) && (initialXY[1] != finalXY[1])) {
				if (Math.abs(initialXY[0] - finalXY[0]) > Math.abs(initialXY[1] - finalXY[1])) {
					driveDirection = initialXY[0] < finalXY[0] ? Matricial2035Constants.EAST : Matricial2035Constants.WEST;
					growDirection = Matricial2035DirectionHelper.perpendicularDirection(initialXY, clusterMap.getCentroid(),
							Matricial2035Constants.EAST);
				} else {
					driveDirection = initialXY[1] < finalXY[1] ? Matricial2035Constants.NORTH : Matricial2035Constants.SOUTH;
					growDirection = Matricial2035DirectionHelper.perpendicularDirection(initialXY, clusterMap.getCentroid(),
							Matricial2035Constants.NORTH);
				}
				landMap.orthogonalLotization(initialXY, finalXY, driveDirection, growDirection);
			}
		}
	}

	private void createOctopianCoverture(int nodeKey, Matricial2035Polygon clusterPolygon) {
		clusterPolygon.getPoints().add(nodeKey);
		landMap.findPoint(nodeKey).setType(Matricial2035Configuration.BORDER_MARK);

		int roadUp = 0, roadDown = 0, roadLeft = 0, roadRight = 0;
		boolean nonOctopian = false;
		while ((roadUp != -1) || (roadDown != -1) || (roadLeft != -1) || (roadRight != -1)) {
			roadUp = extendMembraneSection(
					Matricial2035MapHelper.moveKeyByOffsetAndDirection(nodeKey, 1, Matricial2035Constants.NORTH),
					Matricial2035Constants.NORTH);
			roadDown = extendMembraneSection(
					Matricial2035MapHelper.moveKeyByOffsetAndDirection(nodeKey, 1, Matricial2035Constants.SOUTH),
					Matricial2035Constants.SOUTH);
			roadLeft = extendMembraneSection(
					Matricial2035MapHelper.moveKeyByOffsetAndDirection(nodeKey, 1, Matricial2035Constants.EAST),
					Matricial2035Constants.EAST);
			roadRight = extendMembraneSection(
					Matricial2035MapHelper.moveKeyByOffsetAndDirection(nodeKey, 1, Matricial2035Constants.WEST),
					Matricial2035Constants.WEST);

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
						Matricial2035MapHelper.moveKeyByOffsetAndDirection(nodeKey, 1, Matricial2035Constants.NORTH),
						Matricial2035Constants.NORTH);
				roadDown = extendMembraneSection(
						Matricial2035MapHelper.moveKeyByOffsetAndDirection(nodeKey, 1, Matricial2035Constants.SOUTH),
						Matricial2035Constants.SOUTH);
				roadLeft = extendMembraneSection(
						Matricial2035MapHelper.moveKeyByOffsetAndDirection(nodeKey, 1, Matricial2035Constants.EAST),
						Matricial2035Constants.EAST);
				roadRight = extendMembraneSection(
						Matricial2035MapHelper.moveKeyByOffsetAndDirection(nodeKey, 1, Matricial2035Constants.WEST),
						Matricial2035Constants.WEST);

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
						Matricial2035MapHelper.moveKeyByOffsetAndDirection(bipolarNodeKey, 1, Matricial2035Constants.NORTH),
						Matricial2035Constants.NORTH);
				roadDown = extendMembraneSection(
						Matricial2035MapHelper.moveKeyByOffsetAndDirection(bipolarNodeKey, 1, Matricial2035Constants.SOUTH),
						Matricial2035Constants.SOUTH);
				roadLeft = extendMembraneSection(
						Matricial2035MapHelper.moveKeyByOffsetAndDirection(bipolarNodeKey, 1, Matricial2035Constants.EAST),
						Matricial2035Constants.EAST);
				roadRight = extendMembraneSection(
						Matricial2035MapHelper.moveKeyByOffsetAndDirection(bipolarNodeKey, 1, Matricial2035Constants.WEST),
						Matricial2035Constants.WEST);

				bipolarNodeKey = Math.abs(roadUp * roadDown * roadLeft * roadRight);
				if (bipolarNodeKey != 1)
					clusterPolygon.getPoints().add(0, bipolarNodeKey);
			}
		}
		simplifyOctopian(clusterPolygon);
	}

	private void simplifyOctopian(Matricial2035Polygon clusterPolygon) {
		if (clusterPolygon.getPoints().size() > 1) {
			if ((clusterPolygon.getPoints().get(0) + 1) == clusterPolygon.getPoints()
					.get(clusterPolygon.getPoints().size() - 1)) {
				clusterPolygon.getPoints().remove(clusterPolygon.getPoints().size() - 1);
			}

			if (clusterPolygon.getPoints().size() > 1) {
				if ((clusterPolygon.getPoints().get(0) + 10000) == clusterPolygon.getPoints().get(1)) {
					clusterPolygon.getPoints().remove(0);
				}
			}
		}

	}

	private int extendMembraneSection(int beginKey, int direction) {
		if (!landMap.landPointisOnMap(beginKey)
				|| !landMap.findPoint(beginKey).getType().equals(Matricial2035Configuration.NODE_MARK)) {
			return -1;
		}

		int oldKey = -1;
		while (landMap.landPointisOnMap(beginKey)
				&& landMap.findPoint(beginKey).getType().equals(Matricial2035Configuration.NODE_MARK)) {
			oldKey = beginKey;
			landMap.findPoint(beginKey).setType(Matricial2035Configuration.BORDER_MARK);
			beginKey = Matricial2035MapHelper.moveKeyByOffsetAndDirection(beginKey, 1, direction);
		}
		return oldKey;
	}
}