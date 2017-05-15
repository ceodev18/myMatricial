package models.matricial2035Variation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import helpers.matricial2035Variation.Matricial2035DirectionHelper;
import helpers.matricial2035Variation.Matricial2035MapHelper;
import interfaces.matricial2035Variation.Matricial2035Configuration;
import interfaces.matricial2035Variation.Matricial2035Constants;
import models.configuration.ConfigurationEntry;

public class Matricial2035LandMap {
	private ConfigurationEntry configuration;

	public void setConfiguration(ConfigurationEntry configurationEntry) {
		this.configuration = configurationEntry;
	}

	public ConfigurationEntry getConfiguration() {
		return configuration;
	}

	private int pointsx = -1;
	private int pointsy = -1;

	private Map<Integer, Matricial2035LandPoint> map;
	private List<Matricial2035LandRoute> landRoutes = new ArrayList<>();
	private List<Integer> nodes = new ArrayList<>();
	List<List<Integer>> fullPolygon;

	public Matricial2035LandMap(int pointsx, int pointsy) {
		this.setPointsx(++pointsx);
		this.setPointsy(++pointsy);

		map = new HashMap<>();
		for (int i = 0; i < pointsx; i++) {
			for (int j = 0; j < pointsy; j++) {
				map.put(Matricial2035MapHelper.formKey(i, j), new Matricial2035LandPoint(i, j));
			}
		}
	}

	public int getPointsx() {
		return pointsx;
	}

	public void setPointsx(int pointsx) {
		this.pointsx = pointsx;
	}

	public int getPointsy() {
		return pointsy;
	}

	public void setPointsy(int pointsy) {
		this.pointsy = pointsy;
	}

	public Matricial2035LandPoint getLandPoint(int pointId) {
		return map.get(pointId);
	}

	public Matricial2035LandPoint findPoint(int entryPointId) {
		return map.get(entryPointId);
	}

	public List<Matricial2035LandRoute> getLandRoutes() {
		return landRoutes;
	}

	public void setLandRoutes(List<Matricial2035LandRoute> landRoutes) {
		this.landRoutes = landRoutes;
	}

	public List<Integer> getNodes() {
		return nodes;
	}

	public void setNodes(List<Matricial2035LandPoint> polygon) {
		this.nodes = new ArrayList<>();
		for (int i = 0; i < polygon.size(); i++) {
			nodes.add(polygon.get(i).getId());
		}
	}

	public Matricial2035LandPoint getCentroid() {
		return new Matricial2035LandPoint(pointsx / 2, pointsy / 2);
	}

	/**
	 * This method marks all points that are not inside the polygon border as
	 * restricted area. This must be an ordered set of consecutive points (after
	 * all the input from android looks like that.
	 */
	public void createMapBorder(List<Matricial2035LandPoint> polygon) {
		setNodes(polygon);
		fullPolygon = new ArrayList<>();
		// first we create the border
		for (int i = 0, j = 1; j < polygon.size(); i++, j++) {
			List<Integer> truePolygon = new ArrayList<>();
			int underscore = (polygon.get(j).getX() - polygon.get(i).getX());
			// there are three gradient cases.
			// 1st UNDEFINED = (get(j).getX()-get(i).getX()); straight Y axis
			if (underscore == 0) {
				int lower = polygon.get(i).getY() < polygon.get(j).getY() ? polygon.get(i).getY()
						: polygon.get(j).getY();
				int upper = polygon.get(i).getY() > polygon.get(j).getY() ? polygon.get(i).getY()
						: polygon.get(j).getY();

				for (int w = lower; w <= upper; w++) {
					getLandPoint(Matricial2035MapHelper.formKey(polygon.get(i).getX(), w))
							.setType(Matricial2035Constants.POLYGON_LIMIT);
					truePolygon.add(Matricial2035MapHelper.formKey(polygon.get(i).getX(), w));
				}
				continue;
			}

			double gradient = (polygon.get(j).getY() - polygon.get(i).getY()) * 1.0 / underscore;
			// 2nd, gradient=0; straight in the X axis
			int lower = polygon.get(i).getX() < polygon.get(j).getX() ? polygon.get(i).getX() : polygon.get(j).getX();
			int upper = polygon.get(i).getX() > polygon.get(j).getX() ? polygon.get(i).getX() : polygon.get(j).getX();
			if (gradient == 0) {
				for (int w = lower; w <= upper; w++) {
					getLandPoint(Matricial2035MapHelper.formKey(w, polygon.get(i).getY()))
							.setType(Matricial2035Constants.POLYGON_LIMIT);
					truePolygon.add(Matricial2035MapHelper.formKey(w, polygon.get(i).getY()));
				}
				continue;
			}

			double b = polygon.get(j).getY() - gradient * polygon.get(j).getX();
			// 3nd the gradient is positive/negative.
			for (int w = lower; w <= upper; w++) {
				float y = Matricial2035MapHelper.round(gradient * w + b);
				if (y == (int) y) // quick and dirty convertion check
				{
					getLandPoint(Matricial2035MapHelper.formKey(w, (int) y)).setType(Matricial2035Constants.POLYGON_LIMIT);
					truePolygon.add(Matricial2035MapHelper.formKey(w, (int) y));
				}
			}
			fullPolygon.add(truePolygon);
		}

		// we fill everything outside of it with Xs
		fillPolygonalArea();
		clearDottedLimits();
		updatePolygonLimits();
	}

	private void updatePolygonLimits() {
		for (int x = 0; x < fullPolygon.size(); x++) {
			List<Integer> polygonRow = new ArrayList<>();
			for (int y = 0; y < fullPolygon.get(x).size() - 1; y++) {
				Matricial2035LandPoint initialLandPoint = getLandPoint(fullPolygon.get(x).get(y));
				polygonRow.add(fullPolygon.get(x).get(y));
				Matricial2035LandPoint finalLandPoint = getLandPoint(fullPolygon.get(x).get(y + 1));

				int wi = initialLandPoint.getX();
				int wf = finalLandPoint.getX();

				if (initialLandPoint.getY() > finalLandPoint.getY()) {
					for (int z = initialLandPoint.getY() - 1; z >= finalLandPoint.getY() + 1; z--) {
						if (wi != wf) {
							if (isPolygonBorder(wi, z)) {
								polygonRow.add(Matricial2035MapHelper.formKey(wi, z));
							}
						}

						if (isPolygonBorder(wf, z)) {
							polygonRow.add(Matricial2035MapHelper.formKey(wf, z));
						}
					}
				} else {
					for (int z = initialLandPoint.getY() + 1; z <= finalLandPoint.getY() - 1; z++) {
						if (wi != wf) {
							if (isPolygonBorder(wi, z)) {
								polygonRow.add(Matricial2035MapHelper.formKey(wi, z));
							}
						}
						if (isPolygonBorder(wf, z)) {
							polygonRow.add(Matricial2035MapHelper.formKey(wf, z));
						}
					}
				}
			}
			polygonRow.add(fullPolygon.get(x).get(fullPolygon.get(x).size() - 1));
			fullPolygon.set(x, polygonRow);
		}
		fullPolygon = null;
	}

	private boolean isPolygonBorder(int x, int y) {
		int easternLimit = x + 1;
		int westernLimit = x - 1;
		int southLimit = y - 1;
		int northLimit = y + 1;

		if (westernLimit == -1 || easternLimit == pointsx || southLimit == -1 || northLimit == pointsy)
			return false;
		return (findPoint(Matricial2035MapHelper.formKey(x - 1, y)).getType()
				.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK)
				|| findPoint(Matricial2035MapHelper.formKey(x + 1, y)).getType()
						.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK))
				&& !findPoint(Matricial2035MapHelper.formKey(x, y)).getType()
						.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK);
	}

	private void fillPolygonalArea() {
		for (int x = 0; x < pointsx; x++) {
			int count = 0;
			int pInitialLimit = -1;
			boolean reversed = false;

			for (int y = 0; y < pointsy; y++) {
				if (getLandPoint(Matricial2035MapHelper.formKey(x, y)).getType() == Matricial2035Constants.POLYGON_LIMIT) {
					count++;
					pInitialLimit = pInitialLimit == -1 ? y : pInitialLimit;
				}

				switch (count) {
				case 0:
					getLandPoint(Matricial2035MapHelper.formKey(x, y)).setType(Matricial2035Configuration.OUTSIDE_POLYGON_MARK);
					break;
				case 2:
					if (!reversed) {
						for (int w = pInitialLimit + 1; w < y; w++) {
							getLandPoint(Matricial2035MapHelper.formKey(x, w)).setType(Matricial2035Configuration.EMPTY_MARK);
						}
						reversed = true;
					} else {
						getLandPoint(Matricial2035MapHelper.formKey(x, y)).setType(Matricial2035Configuration.OUTSIDE_POLYGON_MARK);
					}
					break;
				}
			}
		}
	}

	// Variation for the creation of zones
	public void createBorderFromPolygon(List<Integer> polygon, String markType) {
		for (int i = 0; i < polygon.size(); i++) {
			int xyInitial[] = Matricial2035MapHelper.breakKey(polygon.get(i));
			int xyFinal[] = Matricial2035MapHelper.breakKey(polygon.get((i + 1) % polygon.size()));

			int underscore = (xyFinal[0] - xyInitial[0]);
			if (underscore == 0) {
				int lower = xyInitial[1] < xyFinal[1] ? xyInitial[1] : xyFinal[1];
				int upper = xyInitial[1] > xyFinal[1] ? xyInitial[1] : xyFinal[1];

				for (int w = lower; w <= upper; w++) {
					getLandPoint(Matricial2035MapHelper.formKey(xyInitial[0], w)).setType(markType);
				}
				continue;
			}

			double gradient = (xyFinal[1] - xyInitial[1]) * 1.0 / underscore;
			// 2nd, gradient=0; straight in the X axis
			int lower = xyInitial[0] < xyFinal[0] ? xyInitial[0] : xyFinal[0];
			int upper = xyInitial[0] > xyFinal[0] ? xyInitial[0] : xyFinal[0];
			if (gradient == 0) {
				for (int w = lower; w <= upper; w++) {
					getLandPoint(Matricial2035MapHelper.formKey(w, xyInitial[1])).setType(markType);
				}
				continue;
			}

			double b = xyFinal[1] - gradient * xyFinal[0];
			// 3nd the gradient is positive/negative.
			for (int w = lower; w <= upper; w++) {
				float y = Matricial2035MapHelper.round(gradient * w + b);
				if (y == (int) y) // quick and dirty convertion check
				{
					getLandPoint(Matricial2035MapHelper.formKey(w, (int) y)).setType(markType);
				}
			}
		}
	}

	private void clearDottedLimits() {
		for (int x = 0; x < fullPolygon.size(); x++) {
			for (int i = 0; i < fullPolygon.get(x).size(); i++) {
				getLandPoint(fullPolygon.get(x).get(i)).setType(Matricial2035Configuration.EMPTY_MARK);
			}
		}
	}

	public void markVariation(int entryPointId, int branchType, int nodeType) {
		String variation = "-";
		switch (branchType) {
		case Matricial2035Configuration.ARTERIAL_BRANCH:
			variation = Matricial2035Configuration.ARTERIAL_MARK;
			break;
		case Matricial2035Configuration.COLLECTOR_BRANCH:
			variation = Matricial2035Configuration.COLLECTOR_MARK;
			break;
		case Matricial2035Configuration.LOCAL_BRANCH:
			variation = Matricial2035Configuration.LOCAL_MARK;
			break;
		case Matricial2035Configuration.NODE:
			variation = Matricial2035Configuration.NODE_MARK;
			break;
		}
		map.get(entryPointId).setType(variation);
		map.get(entryPointId).setNodeType(nodeType);
	}

	public boolean landPointisOnMap(int pointId) {
		int[] xy = Matricial2035MapHelper.breakKey(pointId);
		return xy[0] < pointsx && xy[0] > 0 && xy[1] < pointsy && xy[1] > 0;
	}

	public boolean isSpecialNode(int x, int y) {
		// up,down,left,right
		if (!findPoint(Matricial2035MapHelper.formKey(x, y)).getType().equals(Matricial2035Configuration.NODE_MARK)) {
			return false;
		}

		if ((y + 1 != pointsy) && (y - 1 != -1) && (x - 1 != -1) && (x + 1 != pointsx)) {
			if (findPoint(Matricial2035MapHelper.formKey(x, y + 1)).getType().equals(Matricial2035Configuration.NODE_MARK)
					&& findPoint(Matricial2035MapHelper.formKey(x, y - 1)).getType()
							.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK)
					&& findPoint(Matricial2035MapHelper.formKey(x + 1, y)).getType().equals(Matricial2035Configuration.EMPTY_MARK)
					&& (!findPoint(Matricial2035MapHelper.formKey(x - 1, y)).getType().equals(Matricial2035Configuration.EMPTY_MARK)
							|| !findPoint(Matricial2035MapHelper.formKey(x + 1, y)).getType()
									.equals(Matricial2035Configuration.NODE_MARK))) {
				return true;
			}
		}

		return false;
	}

	public boolean isNormalNode(int x, int y) {
		// up,down,left,right
		if (!findPoint(Matricial2035MapHelper.formKey(x, y)).getType().equals(Matricial2035Configuration.NODE_MARK)) {
			return false;
		}
		int node = 0;
		boolean[] nodeInBorder = new boolean[] { false, false, false, false };
		int outside = 0;
		if (y + 1 != pointsy) {
			if (findPoint(Matricial2035MapHelper.formKey(x, y + 1)).getType().equals(Matricial2035Configuration.NODE_MARK)) {
				node++;
				nodeInBorder[0] = true;
			}

			if (findPoint(Matricial2035MapHelper.formKey(x, y + 1)).getType()
					.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if (y - 1 != -1) {
			if (findPoint(Matricial2035MapHelper.formKey(x, y - 1)).getType().equals(Matricial2035Configuration.NODE_MARK)) {
				node++;
				nodeInBorder[1] = true;
			}

			if (findPoint(Matricial2035MapHelper.formKey(x, y - 1)).getType()
					.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if (x - 1 != -1) {
			if (findPoint(Matricial2035MapHelper.formKey(x - 1, y)).getType().equals(Matricial2035Configuration.NODE_MARK)) {
				node++;
				nodeInBorder[2] = true;
			}

			if (findPoint(Matricial2035MapHelper.formKey(x - 1, y)).getType()
					.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if (x + 1 != pointsx) {
			if (findPoint(Matricial2035MapHelper.formKey(x + 1, y)).getType().equals(Matricial2035Configuration.NODE_MARK)) {
				node++;
				nodeInBorder[3] = true;
			}

			if (findPoint(Matricial2035MapHelper.formKey(x + 1, y)).getType()
					.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if ((y + 1 != pointsy) && (y - 1 != -1) && (x - 1 != -1) && (x + 1 != pointsx)) {
			if ((findPoint(Matricial2035MapHelper.formKey(x, y - 1)).getType().equals(Matricial2035Configuration.ARTERIAL_MARK)
					|| findPoint(Matricial2035MapHelper.formKey(x, y - 1)).getType().equals(Matricial2035Configuration.LOCAL_MARK))
					&& findPoint(Matricial2035MapHelper.formKey(x - 1, y)).getType()
							.equals(Matricial2035Configuration.COLLECTOR_MARK)
					&& findPoint(Matricial2035MapHelper.formKey(x + 1, y)).getType().equals(Matricial2035Configuration.NODE_MARK)
					&& findPoint(Matricial2035MapHelper.formKey(x, y + 1)).getType().equals(Matricial2035Configuration.NODE_MARK)) {
				return true;
			}
		}

		if (node == 2 && (!(nodeInBorder[0] && nodeInBorder[1]) || !(nodeInBorder[2] && nodeInBorder[3]))) {
			return false;
		}

		if (node + outside > 1) {
			return true;
		}
		return false;
	}

	public void impreciseLotization(Matricial2035Polygon Matricial2035Polygon, int maximunDepth) {
		if (!Matricial2035Polygon.canBelotized(configuration.getLotConfiguration().getDepthSize()) || maximunDepth == 0) {
			return;
		}

		boolean entranceCreated = false;
		for (int i = 0; i < Matricial2035Polygon.getPoints().size(); i++) {
			// detect whereas the kind of side it is 0.0 or infinite if not
			// we simply ignore it (for now)
			int[] initialXY = Matricial2035MapHelper.breakKey(Matricial2035Polygon.getPoints().get(i));
			int[] finalXY = Matricial2035MapHelper
					.breakKey(Matricial2035Polygon.getPoints().get((i + 1) % Matricial2035Polygon.getPoints().size()));
			int growDirection = -1;
			int driveDirection = -1;

			if (initialXY[0] == finalXY[0]) {// infinite
				growDirection = Matricial2035DirectionHelper.perpendicularDirection(initialXY, Matricial2035Polygon.getCentroid(),
						Matricial2035Constants.NORTH);
				driveDirection = initialXY[1] < finalXY[1] ? Matricial2035Constants.NORTH : Matricial2035Constants.SOUTH;

			} else if (initialXY[1] == finalXY[1]) {
				growDirection = Matricial2035DirectionHelper.perpendicularDirection(initialXY, Matricial2035Polygon.getCentroid(),
						Matricial2035Constants.EAST);
				driveDirection = initialXY[0] < finalXY[0] ? Matricial2035Constants.EAST : Matricial2035Constants.WEST;
			} else {
				// The master polygon takes care of this
				continue;
			}

			int[] currentXY = initialXY;
			int seed = 0;
			while (currentXY[0] != finalXY[0] || currentXY[1] != finalXY[1]) {
				if (!entranceCreated) {
					createSmallEntranceRoute(initialXY, finalXY, driveDirection, growDirection);
					entranceCreated = true;
				}
				if (canBeLotized(currentXY, finalXY, driveDirection, growDirection,
						configuration.getLotConfiguration().getDepthSize())) {
					if (findPoint(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1])).getGramaticalType() == null) {
						int[] points = interpretBuilding(currentXY[0], currentXY[1], driveDirection, growDirection,
								configuration.getLotConfiguration().getSideSize(),
								configuration.getLotConfiguration().getDepthSize());
						findPoint(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1])).setGramaticalType(
								"l" + "-" + points[0] + "-" + points[1] + "-" + points[2] + "-" + points[3] + "-"
										+ points[4] + "-" + points[5] + "-" + points[6] + "-" + points[7]);
					}

					currentXY = lotize(currentXY, finalXY, driveDirection, growDirection, false,
							seed % Matricial2035Constants.MAX_HOUSE_COMBINATION);
					seed++;
				} else {
					currentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, 1, driveDirection);
				}
			}
		}
		// we create an inner route and try to create a new polygon to take hold
		// of the side
		List<Integer> reducedPoints = Matricial2035Polygon
				.translateTowardCenter(configuration.getLotConfiguration().getDepthSize());
		for (int i = 0; i < reducedPoints.size(); i++) {
			// detect whereas the kind of side it is 0.0 or infinite if not
			// we simply ignore it (for now)
			int[] initialXY = Matricial2035MapHelper.breakKey(reducedPoints.get(i));
			int[] finalXY = Matricial2035MapHelper.breakKey(reducedPoints.get((i + 1) % Matricial2035Polygon.getPoints().size()));
			int growDirection = -1;
			int driveDirection = -1;
			if (initialXY[0] == finalXY[0]) {// infinite
				growDirection = Matricial2035DirectionHelper.perpendicularDirection(initialXY, Matricial2035Polygon.getCentroid(),
						Matricial2035Constants.NORTH);
				driveDirection = initialXY[1] < finalXY[1] ? Matricial2035Constants.NORTH : Matricial2035Constants.SOUTH;

			} else if (initialXY[1] == finalXY[1]) {
				growDirection = Matricial2035DirectionHelper.perpendicularDirection(initialXY, Matricial2035Polygon.getCentroid(),
						Matricial2035Constants.EAST);
				driveDirection = initialXY[0] < finalXY[0] ? Matricial2035Constants.EAST : Matricial2035Constants.WEST;
			} else {
				// The map polygon takes care of this
				continue;
			}
			createInnerRoad(initialXY, finalXY, driveDirection, growDirection);
		}

		reducedPoints = Matricial2035Polygon.translateTowardCenter(
				configuration.getLotConfiguration().getDepthSize() + Matricial2035Configuration.LOCAL_BRANCH_SIZE);
		if (reducedPoints.size() != 0) {
			Matricial2035Polygon innerPolygon = new Matricial2035Polygon();
			innerPolygon.setPoints(reducedPoints);
			innerPolygon.setComplete(true);
			maximunDepth--;
			impreciseLotization(innerPolygon, maximunDepth);
		}
	}

	public void orthogonalLotization(int[] initialXY, int[] finalXY, int driveDirection, int growDirection) {
		int[] currentXY = new int[] { initialXY[0], initialXY[1] };
		int seed = 0;
		int distance = (int) Math.sqrt(Math.pow(currentXY[0] - finalXY[0], 2) + Math.pow(currentXY[1] - finalXY[1], 2));
		while (distance > 0) {
			int[] upperNearInitial = new int[2];
			int[] upperNearEnd = new int[2];

			if (canBeOrthogonallyLotized(currentXY, finalXY, driveDirection, growDirection,
					configuration.getLotConfiguration().getDepthSize(), upperNearInitial, upperNearEnd)) {

				// remembering that I need to readjust the point. Withouth this
				// the first point will always give problems
				String type = this.findPoint(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1])).getType();
				if (!type.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK)) {
					while (!type.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK)) {
						currentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, 1,
								Matricial2035DirectionHelper.oppositeDirection(growDirection));
						if (this.landPointisOnMap(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1]))) {
							type = this.findPoint(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1])).getType();
						} else {
							currentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, 1, growDirection);
							break;
						}
					}
					while (type.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK)) {
						currentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, 1, growDirection);
						if (this.landPointisOnMap(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1]))) {
							type = this.findPoint(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1])).getType();
						} else {
							currentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, 1,
									Matricial2035DirectionHelper.oppositeDirection(growDirection));
							break;
						}
					}
				} else {
					while (type.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK)) {
						currentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, 1, growDirection);
						if (this.landPointisOnMap(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1]))) {
							type = this.findPoint(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1])).getType();
						} else {
							currentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, 1,
									Matricial2035DirectionHelper.oppositeDirection(growDirection));
							break;
						}
					}
					while (!type.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK)) {
						currentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, 1,
								Matricial2035DirectionHelper.oppositeDirection(growDirection));
						if (this.landPointisOnMap(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1]))) {
							type = this.findPoint(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1])).getType();
						} else {
							currentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, 1, growDirection);
							break;
						}
					}
				}

				int[] oldCurrentXY = new int[2];
				oldCurrentXY[0] = currentXY[0];
				oldCurrentXY[1] = currentXY[1];

				currentXY = orthogonalLotize(currentXY, finalXY, driveDirection, growDirection,
						seed % Matricial2035Constants.MAX_HOUSE_COMBINATION, upperNearEnd);
				if (findPoint(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1])).getGramaticalType() == null) {
					findPoint(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1]))
							.setGramaticalType("l" + "-" + oldCurrentXY[0] + "-" + oldCurrentXY[1] + "-"
									+ upperNearInitial[0] + "-" + upperNearInitial[1] + "-" + upperNearEnd[0] + "-"
									+ upperNearEnd[1] + "-" + currentXY[0] + "-" + currentXY[1]);
				}

				seed += 1;
				distance -= configuration.getLotConfiguration().getSideSize();
			} else {
				currentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, 1, driveDirection);
				distance--;
			}
		}
	}

	private boolean canBeOrthogonallyLotized(int[] initialXY, int[] finalXY, int driveDirection, int growDirection,
			int i, int[] upperNearInitial, int[] upperNearEnd) {
		int times = configuration.getLotConfiguration().getSideSize();
		int[] currentXY = new int[] { initialXY[0], initialXY[1] };
		while (times != 0) {
			if (!landPointisOnMap(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1])))
				return false;
			String type = this.findPoint(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1])).getType();
			while (!type.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK)) {
				currentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, 1,
						Matricial2035DirectionHelper.oppositeDirection(growDirection));
				if (!landPointisOnMap(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1])))
					return false;
				type = this.findPoint(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1])).getType();
			}
			while (type.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK)) {
				currentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, 1, growDirection);
				if (!landPointisOnMap(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1])))
					return false;
				type = this.findPoint(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1])).getType();
			}

			if (currentXY[0] == finalXY[0] && currentXY[1] == finalXY[1]) {
				return false;
			}

			int growTimes = configuration.getLotConfiguration().getDepthSize() * 2;
			int[] currentOrthXY = new int[] { currentXY[0], currentXY[1] };
			while (growTimes != 0) {
				if (!this.landPointisOnMap(Matricial2035MapHelper.formKey(currentOrthXY[0], currentOrthXY[1])))
					return false;
				type = this.findPoint(Matricial2035MapHelper.formKey(currentOrthXY[0], currentOrthXY[1])).getType();
				if (!type.equals(Matricial2035Configuration.EMPTY_MARK) && !type.equals(Matricial2035Configuration.BORDER_MARK))
					return false;
				currentOrthXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentOrthXY, 1, growDirection);
				growTimes--;
			}

			if (times == configuration.getLotConfiguration().getSideSize()) {
				// uppermost point near initial point
				upperNearInitial[0] = currentOrthXY[0];
				upperNearInitial[1] = currentOrthXY[1];
			} else if (times == 1) {
				// uppermost point near final point
				upperNearEnd[0] = currentOrthXY[0];
				upperNearEnd[1] = currentOrthXY[1];
			}
			currentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, 1, driveDirection);
			times--;
		}
		return true;
	}

	private int[] orthogonalLotize(int[] initialXY, int[] finalXY, int driveDirection, int growDirection, int seed,
			int[] upperNearEnd) {
		int times = configuration.getLotConfiguration().getSideSize();
		int[] currentXY = new int[] { initialXY[0], initialXY[1] };
		while (times != 0) {
			int[] movCurrentXY = new int[] { currentXY[0], currentXY[1] };
			String type = this.findPoint(Matricial2035MapHelper.formKey(movCurrentXY[0], movCurrentXY[1])).getType();
			if (!type.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK)) {
				while (!type.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK)) {
					movCurrentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(movCurrentXY, 1,
							Matricial2035DirectionHelper.oppositeDirection(growDirection));
					type = this.findPoint(Matricial2035MapHelper.formKey(movCurrentXY[0], movCurrentXY[1])).getType();
				}
				while (type.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK)) {
					movCurrentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(movCurrentXY, 1, growDirection);
					type = this.findPoint(Matricial2035MapHelper.formKey(movCurrentXY[0], movCurrentXY[1])).getType();
				}
			} else {
				while (type.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK)) {
					movCurrentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(movCurrentXY, 1, growDirection);
					type = this.findPoint(Matricial2035MapHelper.formKey(movCurrentXY[0], movCurrentXY[1])).getType();
				}
				while (!type.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK)) {
					movCurrentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(movCurrentXY, 1,
							Matricial2035DirectionHelper.oppositeDirection(growDirection));
					type = this.findPoint(Matricial2035MapHelper.formKey(movCurrentXY[0], movCurrentXY[1])).getType();
				}
			}

			int growTimes = configuration.getLotConfiguration().getDepthSize() * 2;
			int[] currentOrthXY = new int[] { movCurrentXY[0], movCurrentXY[1] };
			while (growTimes != 0) {
				this.findPoint(Matricial2035MapHelper.formKey(currentOrthXY[0], currentOrthXY[1])).setType("" + seed);
				currentOrthXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentOrthXY, 1, growDirection);
				growTimes--;
			}

			if (times != 1) {
				currentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, 1, driveDirection);
			}
			times--;
		}

		int[] movUpperNearEndXY = new int[] { upperNearEnd[0], upperNearEnd[1] };
		int growTimes = configuration.getLotConfiguration().getDepthSize() * 2;
		while (growTimes != 0) {
			this.findPoint(Matricial2035MapHelper.formKey(movUpperNearEndXY[0], movUpperNearEndXY[1])).setType("" + seed);
			movUpperNearEndXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(movUpperNearEndXY, 1,
					Matricial2035DirectionHelper.oppositeDirection(growDirection));
			growTimes--;
		}
		currentXY = movUpperNearEndXY;
		return currentXY;
	}

	private void createSmallEntranceRoute(int[] initialXY, int[] finalXY, int driveDirection, int growDirection) {
		int times = Matricial2035Configuration.LOCAL_BRANCH_SIZE;
		int[] currentXY = new int[] { initialXY[0], initialXY[1] };
		int distance = (int) Math.sqrt(Math.pow(initialXY[0] - finalXY[0], 2) + Math.pow(initialXY[1] - finalXY[1], 2));
		if (distance < 30) {
			return;
		}
		int[] lowerXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, distance / 2, driveDirection);
		int[] upperXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, (distance / 2) + 1, driveDirection);

		while (times != 0) {
			int growTimes = configuration.getLotConfiguration().getDepthSize();
			int[] lowerOrthXY = new int[] { lowerXY[0], lowerXY[1] };
			while (growTimes != 0) {
				if (this.landPointisOnMap(Matricial2035MapHelper.formKey(lowerOrthXY[0], lowerOrthXY[1]))
						&& !this.findPoint(Matricial2035MapHelper.formKey(lowerOrthXY[0], lowerOrthXY[1])).getType()
								.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK))
					/**TODO to erase
					this.findPoint(Matricial2035MapHelper.formKey(lowerOrthXY[0], lowerOrthXY[1]))
							.setType(Matricial2035Configuration.Matricial2035_ENTRANCE_MARK);*/
				lowerOrthXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(lowerOrthXY, 1, growDirection);
				growTimes--;
			}
			lowerXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(lowerXY, 1,
					Matricial2035DirectionHelper.oppositeDirection(driveDirection));
			times--;

			if (times == 0)
				continue;

			growTimes = configuration.getLotConfiguration().getDepthSize();
			int[] upperOrthXY = new int[] { upperXY[0], upperXY[1] };
			while (growTimes != 0) {
				if (this.landPointisOnMap(Matricial2035MapHelper.formKey(upperOrthXY[0], upperOrthXY[1]))
						&& !this.findPoint(Matricial2035MapHelper.formKey(upperOrthXY[0], upperOrthXY[1])).getType()
								.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK))
				/**TODO to erase	
					this.findPoint(Matricial2035MapHelper.formKey(upperOrthXY[0], upperOrthXY[1]))
							.setType(Matricial2035Configuration.Matricial2035_ENTRANCE_MARK);
				*/
				
				upperOrthXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(upperOrthXY, 1, growDirection);
				growTimes--;
			}
			upperXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(upperXY, 1, driveDirection);
			times--;
		}
	}

	private void createInnerRoad(int[] initialXY, int[] finalXY, int driveDirection, int growDirection) {
		int times = (int) Math.sqrt(Math.pow(initialXY[0] - finalXY[0], 2) + Math.pow(initialXY[1] - finalXY[1], 2));
		while (times != 0) {
			int growTimes = Matricial2035Configuration.LOCAL_BRANCH_SIZE;
			int[] currentOrthXY = new int[] { initialXY[0], initialXY[1] };
			while (growTimes != 0) {
				if (this.landPointisOnMap(Matricial2035MapHelper.formKey(currentOrthXY[0], currentOrthXY[1]))
						&& this.findPoint(Matricial2035MapHelper.formKey(currentOrthXY[0], currentOrthXY[1])).getType()
								.equals(Matricial2035Configuration.EMPTY_MARK)) {
					this.findPoint(Matricial2035MapHelper.formKey(currentOrthXY[0], currentOrthXY[1]))
							.setType(Matricial2035Configuration.LOCAL_MARK);
				}
				currentOrthXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentOrthXY, 1, growDirection);
				growTimes--;
			}
			initialXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(initialXY, 1, driveDirection);
			times--;
		}
	}

	private boolean canBeLotized(int[] initialXY, int[] finalXY, int driveDirection, int growDirection, int depth) {
		int times = configuration.getLotConfiguration().getSideSize();
		int[] currentXY = new int[] { initialXY[0], initialXY[1] };
		while (times != 0) {
			if (currentXY[0] == finalXY[0] && currentXY[1] == finalXY[1]) {
				return false;
			}
			int growTimes = configuration.getLotConfiguration().getDepthSize();
			int[] currentOrthXY = new int[] { currentXY[0], currentXY[1] };
			while (growTimes != 0) {
				if (!this.landPointisOnMap(Matricial2035MapHelper.formKey(currentOrthXY[0], currentOrthXY[1])))
					return false;
				String type = this.findPoint(Matricial2035MapHelper.formKey(currentOrthXY[0], currentOrthXY[1])).getType();
				if (!type.equals(Matricial2035Configuration.EMPTY_MARK) && !type.equals(Matricial2035Configuration.PARK_MARK)
						&& !type.equals(Matricial2035Configuration.INTERNAL_LOCAL_MARK)
						&& !type.equals(Matricial2035Configuration.BORDER_MARK))
					return false;
				currentOrthXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentOrthXY, 1, growDirection);
				growTimes--;
			}
			currentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, 1, driveDirection);
			times--;
		}
		return true;
	}

	private int[] lotize(int[] initialXY, int[] finalXY, int driveDirection, int growDirection, boolean dual,
			int seed) {
		int times = configuration.getLotConfiguration().getSideSize();
		int[] currentXY = initialXY;
		while (times != 0) {
			int growTimes = configuration.getLotConfiguration().getDepthSize();
			int[] currentOrthXY = new int[] { currentXY[0], currentXY[1] };
			while (growTimes != 0) {
				this.findPoint(Matricial2035MapHelper.formKey(currentOrthXY[0], currentOrthXY[1])).setType("" + seed);
				currentOrthXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentOrthXY, 1, growDirection);
				growTimes--;
			}

			if (dual) {
				growTimes = configuration.getLotConfiguration().getDepthSize();
				while (growTimes != 0) {
					this.findPoint(Matricial2035MapHelper.formKey(currentOrthXY[0], currentOrthXY[1]))
							.setType("" + (seed + 1));
					currentOrthXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentOrthXY, 1, growDirection);
					growTimes--;
				}
			}

			currentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, 1, driveDirection);
			times--;
		}
		return currentXY;
	}

	public void preciseLotization(Matricial2035Polygon Matricial2035Polygon, boolean withContribution) {
		boolean contribute = false;
		for (int i = 0; i < Matricial2035Polygon.getPoints().size(); i++) {
			// detect whereas the kind of side it is 0.0 or infinite if not
			// we simply ignore it (for now)
			int[] initialXY = Matricial2035MapHelper.breakKey(Matricial2035Polygon.getPoints().get(i));
			int[] finalXY = Matricial2035MapHelper
					.breakKey(Matricial2035Polygon.getPoints().get((i + 1) % Matricial2035Polygon.getPoints().size()));
			int growDirection = -1;
			int driveDirection = -1;

			if (initialXY[0] == finalXY[0]) {// infinite
				growDirection = Matricial2035DirectionHelper.perpendicularDirection(initialXY, Matricial2035Polygon.getCentroid(),
						Matricial2035Constants.NORTH);
				driveDirection = initialXY[1] < finalXY[1] ? Matricial2035Constants.NORTH : Matricial2035Constants.SOUTH;
				createEntranceRoute(initialXY, finalXY, driveDirection, growDirection);
				if (withContribution) {
					withContribution = false;
					contribute = true;
				}
			} else if (initialXY[1] == finalXY[1]) {
				growDirection = Matricial2035DirectionHelper.perpendicularDirection(initialXY, Matricial2035Polygon.getCentroid(),
						Matricial2035Constants.EAST);
				driveDirection = initialXY[0] < finalXY[0] ? Matricial2035Constants.EAST : Matricial2035Constants.WEST;
				createWalkRoute(initialXY, finalXY, driveDirection, growDirection);
			} else {
				// Not interest in the partial solution but the global one done
				// by the map itself
				continue;
			}

			int[] currentXY = initialXY;
			int seed = 0;
			while (currentXY[0] != finalXY[0] || currentXY[1] != finalXY[1]) {
				if (contribute) {
					currentXY = createContribution(currentXY, finalXY, driveDirection, growDirection);
					// TODO contribution saved up

					contribute = false;
				} else {
					if (canBeLotized(currentXY, finalXY, driveDirection, growDirection,
							configuration.getLotConfiguration().getDepthSize() * 2)) {

						if (findPoint(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1]))
								.getGramaticalType() == null) {

							int[] points = interpretBuilding(currentXY[0], currentXY[1], driveDirection, growDirection,
									configuration.getLotConfiguration().getSideSize(),
									configuration.getLotConfiguration().getDepthSize());
							findPoint(Matricial2035MapHelper.formKey(currentXY[0], currentXY[1])).setGramaticalType(
									"l" + "-" + points[0] + "-" + points[1] + "-" + points[2] + "-" + points[3] + "-"
											+ points[4] + "-" + points[5] + "-" + points[6] + "-" + points[7]);

							int[] deeperHouse = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY,
									configuration.getLotConfiguration().getDepthSize(), growDirection);
							points = interpretBuilding(deeperHouse[0], deeperHouse[1], driveDirection, growDirection,
									configuration.getLotConfiguration().getSideSize(),
									configuration.getLotConfiguration().getDepthSize());
							findPoint(Matricial2035MapHelper.formKey(deeperHouse[0], deeperHouse[1])).setGramaticalType(
									"l" + "-" + points[0] + "-" + points[1] + "-" + points[2] + "-" + points[3] + "-"
											+ points[4] + "-" + points[5] + "-" + points[6] + "-" + points[7]);
						}

						currentXY = lotize(currentXY, finalXY, driveDirection, growDirection, true,
								seed % Matricial2035Constants.MAX_HOUSE_COMBINATION);
						seed += 2;
					} else {
						currentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, 1, driveDirection);
					}
				}
			}
		}
	}

	private int[] createContribution(int[] initialXY, int[] finalXY, int driveDirection, int growDirection) {
		int times = Matricial2035Configuration.CONTRIBUTION_SIDE_MINIMUN_SIZE;
		int[] currentXY = new int[] { initialXY[0], initialXY[1] };
		int distance = (int) Math.sqrt(Math.pow(initialXY[0] - finalXY[0], 2) + Math.pow(initialXY[1] - finalXY[1], 2));
		if (distance < times) {
			return currentXY;
		}

		while (times != 0) {
			int growTimes = configuration.getLotConfiguration().getDepthSize() * 2;
			int[] lowerOrthXY = new int[] { currentXY[0], currentXY[1] };
			while (growTimes != 0) {
				this.findPoint(Matricial2035MapHelper.formKey(lowerOrthXY[0], lowerOrthXY[1]))
						.setType(Matricial2035Configuration.CONTRIBUTION_MARK);
				lowerOrthXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(lowerOrthXY, 1, growDirection);
				growTimes--;
			}
			currentXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, 1, driveDirection);
			times--;
		}
		return currentXY;
	}

	private void createEntranceRoute(int[] initialXY, int[] finalXY, int driveDirection, int growDirection) {
		int times = 0;//Matricial2035Configuration.Matricial2035_ENTRANCE_SIZE;
		int[] currentXY = new int[] { initialXY[0], initialXY[1] };
		int distance = (int) Math.sqrt(Math.pow(initialXY[0] - finalXY[0], 2) + Math.pow(initialXY[1] - finalXY[1], 2));
		if (distance < 30) {
			return;
		}
		int[] lowerXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, distance / 2, driveDirection);
		int[] upperXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(currentXY, (distance / 2) + 1, driveDirection);

		while (times != 0) {
			int growTimes = configuration.getLotConfiguration().getDepthSize() * 2;
			int[] lowerOrthXY = new int[] { lowerXY[0], lowerXY[1] };
			while (growTimes != 0) {
				/**TODO to erase
				this.findPoint(Matricial2035MapHelper.formKey(lowerOrthXY[0], lowerOrthXY[1]))
						.setType(Matricial2035Configuration.Matricial2035_ENTRANCE_MARK);*/
				lowerOrthXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(lowerOrthXY, 1, growDirection);
				growTimes--;
			}
			lowerXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(lowerXY, 1,
					Matricial2035DirectionHelper.oppositeDirection(driveDirection));
			times--;

			if (times == 0)
				continue;

			growTimes = configuration.getLotConfiguration().getDepthSize() * 2;
			int[] upperOrthXY = new int[] { upperXY[0], upperXY[1] };
			while (growTimes != 0) {
				/**TODO
				this.findPoint(Matricial2035MapHelper.formKey(upperOrthXY[0], upperOrthXY[1]))
						.setType(Matricial2035Configuration.Matricial2035_ENTRANCE_MARK);*/
				upperOrthXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(upperOrthXY, 1, growDirection);
				growTimes--;
			}
			upperXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(upperXY, 1, driveDirection);
			times--;
		}
	}

	private void createWalkRoute(int[] initialXY, int[] finalXY, int driveDirection, int growDirection) {
		int times = Matricial2035Configuration.WALK_BRANCH_SIZE * 2;
		int distance = (int) Math.sqrt(Math.pow(initialXY[0] - finalXY[0], 2) + Math.pow(initialXY[1] - finalXY[1], 2));
		if (distance < 30) {
			return;
		}
		int[] lowerXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(initialXY,
				configuration.getLotConfiguration().getDepthSize() * 2, driveDirection);
		int[] upperXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(finalXY,
				configuration.getLotConfiguration().getDepthSize() * 2,
				Matricial2035DirectionHelper.oppositeDirection(driveDirection));

		while (times != 0) {
			int growTimes = configuration.getLotConfiguration().getDepthSize() * 2;
			int[] lowerOrthXY = new int[] { lowerXY[0], lowerXY[1] };
			while (growTimes != 0) {
				
				/**TODO to erase
				this.findPoint(Matricial2035MapHelper.formKey(lowerOrthXY[0], lowerOrthXY[1]))
						.setType(Matricial2035Configuration.Matricial2035_ENTRANCE_MARK);
				*/
				
				lowerOrthXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(lowerOrthXY, 1, growDirection);
				growTimes--;
			}
			lowerXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(lowerXY, 1, driveDirection);
			times--;

			growTimes = configuration.getLotConfiguration().getDepthSize() * 2;
			int[] upperOrthXY = new int[] { upperXY[0], upperXY[1] };
			while (growTimes != 0) {
				/**TODO to erase
				this.findPoint(Matricial2035MapHelper.formKey(upperOrthXY[0], upperOrthXY[1]))
						.setType(Matricial2035Configuration.Matricial2035_ENTRANCE_MARK);*/
				upperOrthXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(upperOrthXY, 1, growDirection);
				growTimes--;
			}
			upperXY = Matricial2035MapHelper.moveKeyByOffsetAndDirection(upperXY, 1,
					Matricial2035DirectionHelper.oppositeDirection(driveDirection));
			times--;
		}
	}

	public Matricial2035Polygon getAsMatricial2035Polygon() {
		Matricial2035Polygon Matricial2035Polygon = new Matricial2035Polygon(this.getNodes());
		Matricial2035Polygon.setComplete(true);
		return Matricial2035Polygon;
	}

	private int[] interpretBuilding(int initialX, int finalX, int driveDirection, int growDirection, int sideSize,
			int depthSize) {
		int[] coords = new int[8];
		int[] xy = new int[2];
		coords[0] = initialX;
		coords[1] = finalX;

		xy = Matricial2035MapHelper.moveKeyByOffsetAndDirection(coords, sideSize, driveDirection);
		coords[2] = xy[0];
		coords[3] = xy[1];

		xy = Matricial2035MapHelper.moveKeyByOffsetAndDirection(xy, depthSize, growDirection);
		coords[4] = xy[0];
		coords[5] = xy[1];

		xy = Matricial2035MapHelper.moveKeyByOffsetAndDirection(xy, sideSize,
				Matricial2035DirectionHelper.oppositeDirection(driveDirection));
		coords[6] = xy[0];
		coords[7] = xy[1];
		return coords;
	}

	public String stringify() {
		String mapString = "";
		for (int j = 0; j < pointsy; j++) {
			String type = getLandPoint(Matricial2035MapHelper.formKey(0, j)).getType();
			int repetitions = 1;
			for (int i = 0; i < pointsx; i++) {
				if (type.equals(getLandPoint(Matricial2035MapHelper.formKey(i, j)).getType())) {
					repetitions++;
				} else {
					mapString += type + "" + repetitions + ",";
					repetitions = 1;
					type = getLandPoint(Matricial2035MapHelper.formKey(i, j)).getType();
				}
			}
			mapString += type + "" + repetitions + ",";
			mapString += ".";
		}
		return mapString;
	}

	public String getGrammar() {
		String mapString = "";
		for (int j = 0; j < pointsy; j++) {
			for (int i = 0; i < pointsx; i++) {
				String type = getLandPoint(Matricial2035MapHelper.formKey(i, j)).getGramaticalType();
				if (type != null) {
					mapString += type + ",";
				}
			}
		}
		return mapString;// remove last ,
	}
}