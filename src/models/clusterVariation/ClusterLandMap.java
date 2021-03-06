package models.clusterVariation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import helpers.clusterVariation.ClusterDirectionHelper;
import helpers.clusterVariation.ClusterMapHelper;
import interfaces.clusterVariation.ClusterConfiguration;
import interfaces.clusterVariation.ClusterConstants;
import models.configuration.ConfigurationEntry;

public class ClusterLandMap {
	private ConfigurationEntry configuration;

	public void setConfiguration(ConfigurationEntry configurationEntry) {
		this.configuration = configurationEntry;
	}

	public ConfigurationEntry getConfiguration() {
		return configuration;
	}

	private int pointsx = -1;
	private int pointsy = -1;

	private Map<Integer, ClusterLandPoint> map;
	private List<ClusterLandRoute> landRoutes = new ArrayList<>();
	private List<Integer> nodes = new ArrayList<>();
	List<List<Integer>> fullPolygon;

	public ClusterLandMap(int pointsx, int pointsy) {
		this.setPointsx(++pointsx);
		this.setPointsy(++pointsy);

		map = new HashMap<>();
		for (int i = 0; i < pointsx; i++) {
			for (int j = 0; j < pointsy; j++) {
				map.put(ClusterMapHelper.formKey(i, j), new ClusterLandPoint(i, j));
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

	public ClusterLandPoint getLandPoint(int pointId) {
		return map.get(pointId);
	}

	public ClusterLandPoint findPoint(int entryPointId) {
		return map.get(entryPointId);
	}

	public List<ClusterLandRoute> getLandRoutes() {
		return landRoutes;
	}

	public void setLandRoutes(List<ClusterLandRoute> landRoutes) {
		this.landRoutes = landRoutes;
	}

	public List<Integer> getNodes() {
		return nodes;
	}

	public void setNodes(List<ClusterLandPoint> polygon) {
		this.nodes = new ArrayList<>();
		for (int i = 0; i < polygon.size(); i++) {
			nodes.add(polygon.get(i).getId());
		}
	}

	public ClusterLandPoint getCentroid() {
		return new ClusterLandPoint(pointsx / 2, pointsy / 2);
	}

	/**
	 * This method marks all points that are not inside the polygon border as
	 * restricted area. This must be an ordered set of consecutive points (after
	 * all the input from android looks like that.
	 */
	public void createMapBorder(List<ClusterLandPoint> polygon) {
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
					getLandPoint(ClusterMapHelper.formKey(polygon.get(i).getX(), w))
							.setType(ClusterConstants.POLYGON_LIMIT);
					truePolygon.add(ClusterMapHelper.formKey(polygon.get(i).getX(), w));
				}
				continue;
			}

			double gradient = (polygon.get(j).getY() - polygon.get(i).getY()) * 1.0 / underscore;
			// 2nd, gradient=0; straight in the X axis
			int lower = polygon.get(i).getX() < polygon.get(j).getX() ? polygon.get(i).getX() : polygon.get(j).getX();
			int upper = polygon.get(i).getX() > polygon.get(j).getX() ? polygon.get(i).getX() : polygon.get(j).getX();
			if (gradient == 0) {
				for (int w = lower; w <= upper; w++) {
					getLandPoint(ClusterMapHelper.formKey(w, polygon.get(i).getY()))
							.setType(ClusterConstants.POLYGON_LIMIT);
					truePolygon.add(ClusterMapHelper.formKey(w, polygon.get(i).getY()));
				}
				continue;
			}

			double b = polygon.get(j).getY() - gradient * polygon.get(j).getX();
			// 3nd the gradient is positive/negative.
			for (int w = lower; w <= upper; w++) {
				float y = ClusterMapHelper.round(gradient * w + b);
				if (y == (int) y) // quick and dirty convertion check
				{
					getLandPoint(ClusterMapHelper.formKey(w, (int) y)).setType(ClusterConstants.POLYGON_LIMIT);
					truePolygon.add(ClusterMapHelper.formKey(w, (int) y));
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
				ClusterLandPoint initialLandPoint = getLandPoint(fullPolygon.get(x).get(y));
				polygonRow.add(fullPolygon.get(x).get(y));
				ClusterLandPoint finalLandPoint = getLandPoint(fullPolygon.get(x).get(y + 1));

				int wi = initialLandPoint.getX();
				int wf = finalLandPoint.getX();

				if (initialLandPoint.getY() > finalLandPoint.getY()) {
					for (int z = initialLandPoint.getY() - 1; z >= finalLandPoint.getY() + 1; z--) {
						if (wi != wf) {
							if (isPolygonBorder(wi, z)) {
								polygonRow.add(ClusterMapHelper.formKey(wi, z));
							}
						}

						if (isPolygonBorder(wf, z)) {
							polygonRow.add(ClusterMapHelper.formKey(wf, z));
						}
					}
				} else {
					for (int z = initialLandPoint.getY() + 1; z <= finalLandPoint.getY() - 1; z++) {
						if (wi != wf) {
							if (isPolygonBorder(wi, z)) {
								polygonRow.add(ClusterMapHelper.formKey(wi, z));
							}
						}
						if (isPolygonBorder(wf, z)) {
							polygonRow.add(ClusterMapHelper.formKey(wf, z));
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
		return (findPoint(ClusterMapHelper.formKey(x - 1, y)).getType()
				.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)
				|| findPoint(ClusterMapHelper.formKey(x + 1, y)).getType()
						.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK))
				&& !findPoint(ClusterMapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK);
	}

	private void fillPolygonalArea() {
		for (int x = 0; x < pointsx; x++) {
			int count = 0;
			int pInitialLimit = -1;
			boolean reversed = false;

			for (int y = 0; y < pointsy; y++) {
				if (getLandPoint(ClusterMapHelper.formKey(x, y)).getType() == ClusterConstants.POLYGON_LIMIT) {
					count++;
					pInitialLimit = pInitialLimit == -1 ? y : pInitialLimit;
				}

				switch (count) {
				case 0:
					getLandPoint(ClusterMapHelper.formKey(x, y)).setType(ClusterConfiguration.OUTSIDE_POLYGON_MARK);
					break;
				case 2:
					if (!reversed) {
						for (int w = pInitialLimit + 1; w < y; w++) {
							getLandPoint(ClusterMapHelper.formKey(x, w)).setType(ClusterConfiguration.EMPTY_MARK);
						}
						reversed = true;
					} else {
						getLandPoint(ClusterMapHelper.formKey(x, y)).setType(ClusterConfiguration.OUTSIDE_POLYGON_MARK);
					}
					break;
				}
			}
		}
	}

	// Variation for the creation of zones
	public void createBorderFromPolygon(List<Integer> polygon, String markType) {
		for (int i = 0; i < polygon.size(); i++) {
			int xyInitial[] = ClusterMapHelper.breakKey(polygon.get(i));
			int xyFinal[] = ClusterMapHelper.breakKey(polygon.get((i + 1) % polygon.size()));

			int underscore = (xyFinal[0] - xyInitial[0]);
			if (underscore == 0) {
				int lower = xyInitial[1] < xyFinal[1] ? xyInitial[1] : xyFinal[1];
				int upper = xyInitial[1] > xyFinal[1] ? xyInitial[1] : xyFinal[1];

				for (int w = lower; w <= upper; w++) {
					getLandPoint(ClusterMapHelper.formKey(xyInitial[0], w)).setType(markType);
				}
				continue;
			}

			double gradient = (xyFinal[1] - xyInitial[1]) * 1.0 / underscore;
			// 2nd, gradient=0; straight in the X axis
			int lower = xyInitial[0] < xyFinal[0] ? xyInitial[0] : xyFinal[0];
			int upper = xyInitial[0] > xyFinal[0] ? xyInitial[0] : xyFinal[0];
			if (gradient == 0) {
				for (int w = lower; w <= upper; w++) {
					getLandPoint(ClusterMapHelper.formKey(w, xyInitial[1])).setType(markType);
				}
				continue;
			}

			double b = xyFinal[1] - gradient * xyFinal[0];
			// 3nd the gradient is positive/negative.
			for (int w = lower; w <= upper; w++) {
				float y = ClusterMapHelper.round(gradient * w + b);
				if (y == (int) y) // quick and dirty convertion check
				{
					getLandPoint(ClusterMapHelper.formKey(w, (int) y)).setType(markType);
				}
			}
		}
	}

	private void clearDottedLimits() {
		for (int x = 0; x < fullPolygon.size(); x++) {
			for (int i = 0; i < fullPolygon.get(x).size(); i++) {
				getLandPoint(fullPolygon.get(x).get(i)).setType(ClusterConfiguration.EMPTY_MARK);
			}
		}
	}

	public void markVariation(int entryPointId, int branchType, int nodeType) {
		String variation = "-";
		switch (branchType) {
		case ClusterConfiguration.ARTERIAL_BRANCH:
			variation = ClusterConfiguration.ARTERIAL_MARK;
			break;
		case ClusterConfiguration.COLLECTOR_BRANCH:
			variation = ClusterConfiguration.COLLECTOR_MARK;
			break;
		case ClusterConfiguration.LOCAL_BRANCH:
			variation = ClusterConfiguration.LOCAL_MARK;
			break;
		case ClusterConfiguration.NODE:
			variation = ClusterConfiguration.NODE_MARK;
			break;
		}
		map.get(entryPointId).setType(variation);
		map.get(entryPointId).setNodeType(nodeType);
	}

	public boolean landPointisOnMap(int pointId) {
		int[] xy = ClusterMapHelper.breakKey(pointId);
		return xy[0] < pointsx && xy[0] > 0 && xy[1] < pointsy && xy[1] > 0;
	}

	public boolean isSpecialNode(int x, int y) {
		// up,down,left,right
		if (!findPoint(ClusterMapHelper.formKey(x, y)).getType().equals(ClusterConfiguration.NODE_MARK)) {
			return false;
		}

		if ((y + 1 != pointsy) && (y - 1 != -1) && (x - 1 != -1) && (x + 1 != pointsx)) {
			if (findPoint(ClusterMapHelper.formKey(x, y + 1)).getType().equals(ClusterConfiguration.NODE_MARK)
					&& findPoint(ClusterMapHelper.formKey(x, y - 1)).getType()
							.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)
					&& findPoint(ClusterMapHelper.formKey(x + 1, y)).getType().equals(ClusterConfiguration.EMPTY_MARK)
					&& (!findPoint(ClusterMapHelper.formKey(x - 1, y)).getType().equals(ClusterConfiguration.EMPTY_MARK)
							|| !findPoint(ClusterMapHelper.formKey(x + 1, y)).getType()
									.equals(ClusterConfiguration.NODE_MARK))) {
				return true;
			}
		}

		return false;
	}

	public boolean isNormalNode(int x, int y) {
		// up,down,left,right
		if (!findPoint(ClusterMapHelper.formKey(x, y)).getType().equals(ClusterConfiguration.NODE_MARK)) {
			return false;
		}
		int node = 0;
		boolean[] nodeInBorder = new boolean[] { false, false, false, false };
		int outside = 0;
		if (y + 1 != pointsy) {
			if (findPoint(ClusterMapHelper.formKey(x, y + 1)).getType().equals(ClusterConfiguration.NODE_MARK)) {
				node++;
				nodeInBorder[0] = true;
			}

			if (findPoint(ClusterMapHelper.formKey(x, y + 1)).getType()
					.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if (y - 1 != -1) {
			if (findPoint(ClusterMapHelper.formKey(x, y - 1)).getType().equals(ClusterConfiguration.NODE_MARK)) {
				node++;
				nodeInBorder[1] = true;
			}

			if (findPoint(ClusterMapHelper.formKey(x, y - 1)).getType()
					.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if (x - 1 != -1) {
			if (findPoint(ClusterMapHelper.formKey(x - 1, y)).getType().equals(ClusterConfiguration.NODE_MARK)) {
				node++;
				nodeInBorder[2] = true;
			}

			if (findPoint(ClusterMapHelper.formKey(x - 1, y)).getType()
					.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if (x + 1 != pointsx) {
			if (findPoint(ClusterMapHelper.formKey(x + 1, y)).getType().equals(ClusterConfiguration.NODE_MARK)) {
				node++;
				nodeInBorder[3] = true;
			}

			if (findPoint(ClusterMapHelper.formKey(x + 1, y)).getType()
					.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if ((y + 1 != pointsy) && (y - 1 != -1) && (x - 1 != -1) && (x + 1 != pointsx)) {
			if ((findPoint(ClusterMapHelper.formKey(x, y - 1)).getType().equals(ClusterConfiguration.ARTERIAL_MARK)
					|| findPoint(ClusterMapHelper.formKey(x, y - 1)).getType().equals(ClusterConfiguration.LOCAL_MARK))
					&& findPoint(ClusterMapHelper.formKey(x - 1, y)).getType()
							.equals(ClusterConfiguration.COLLECTOR_MARK)
					&& findPoint(ClusterMapHelper.formKey(x + 1, y)).getType().equals(ClusterConfiguration.NODE_MARK)
					&& findPoint(ClusterMapHelper.formKey(x, y + 1)).getType().equals(ClusterConfiguration.NODE_MARK)) {
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

	public void impreciseLotization(ClusterPolygon clusterPolygon, int maximunDepth) {
		if (!clusterPolygon.canBelotized(configuration.getLotConfiguration().getDepthSize()) || maximunDepth == 0) {
			return;
		}

		boolean entranceCreated = false;
		for (int i = 0; i < clusterPolygon.getPoints().size(); i++) {
			// detect whereas the kind of side it is 0.0 or infinite if not
			// we simply ignore it (for now)
			int[] initialXY = ClusterMapHelper.breakKey(clusterPolygon.getPoints().get(i));
			int[] finalXY = ClusterMapHelper
					.breakKey(clusterPolygon.getPoints().get((i + 1) % clusterPolygon.getPoints().size()));
			int growDirection = -1;
			int driveDirection = -1;

			if (initialXY[0] == finalXY[0]) {// infinite
				growDirection = ClusterDirectionHelper.perpendicularDirection(initialXY, clusterPolygon.getCentroid(),
						ClusterConstants.NORTH);
				driveDirection = initialXY[1] < finalXY[1] ? ClusterConstants.NORTH : ClusterConstants.SOUTH;

			} else if (initialXY[1] == finalXY[1]) {
				growDirection = ClusterDirectionHelper.perpendicularDirection(initialXY, clusterPolygon.getCentroid(),
						ClusterConstants.EAST);
				driveDirection = initialXY[0] < finalXY[0] ? ClusterConstants.EAST : ClusterConstants.WEST;
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
					if (findPoint(ClusterMapHelper.formKey(currentXY[0], currentXY[1])).getGramaticalType() == null) {
						int[] points = interpretBuilding(currentXY[0], currentXY[1], driveDirection, growDirection,
								configuration.getLotConfiguration().getSideSize(),
								configuration.getLotConfiguration().getDepthSize());
						findPoint(ClusterMapHelper.formKey(currentXY[0], currentXY[1])).setGramaticalType(
								"l" + "-" + points[0] + "-" + points[1] + "-" + points[2] + "-" + points[3] + "-"
										+ points[4] + "-" + points[5] + "-" + points[6] + "-" + points[7]);
					}

					currentXY = lotize(currentXY, finalXY, driveDirection, growDirection, false,
							seed % ClusterConstants.MAX_HOUSE_COMBINATION);
					seed++;
				} else {
					currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, driveDirection);
				}
			}
		}
		// we create an inner route and try to create a new polygon to take hold
		// of the side
		List<Integer> reducedPoints = clusterPolygon
				.translateTowardCenter(configuration.getLotConfiguration().getDepthSize());
		for (int i = 0; i < reducedPoints.size(); i++) {
			// detect whereas the kind of side it is 0.0 or infinite if not
			// we simply ignore it (for now)
			int[] initialXY = ClusterMapHelper.breakKey(reducedPoints.get(i));
			int[] finalXY = ClusterMapHelper.breakKey(reducedPoints.get((i + 1) % clusterPolygon.getPoints().size()));
			int growDirection = -1;
			int driveDirection = -1;
			if (initialXY[0] == finalXY[0]) {// infinite
				growDirection = ClusterDirectionHelper.perpendicularDirection(initialXY, clusterPolygon.getCentroid(),
						ClusterConstants.NORTH);
				driveDirection = initialXY[1] < finalXY[1] ? ClusterConstants.NORTH : ClusterConstants.SOUTH;

			} else if (initialXY[1] == finalXY[1]) {
				growDirection = ClusterDirectionHelper.perpendicularDirection(initialXY, clusterPolygon.getCentroid(),
						ClusterConstants.EAST);
				driveDirection = initialXY[0] < finalXY[0] ? ClusterConstants.EAST : ClusterConstants.WEST;
			} else {
				// The map polygon takes care of this
				continue;
			}
			createInnerRoad(initialXY, finalXY, driveDirection, growDirection);
		}

		reducedPoints = clusterPolygon.translateTowardCenter(
				configuration.getLotConfiguration().getDepthSize() + ClusterConfiguration.LOCAL_BRANCH_SIZE);
		if (reducedPoints.size() != 0) {
			ClusterPolygon innerPolygon = new ClusterPolygon();
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
				String type = this.findPoint(ClusterMapHelper.formKey(currentXY[0], currentXY[1])).getType();
				if (!type.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
					while (!type.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
						currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1,
								ClusterDirectionHelper.oppositeDirection(growDirection));
						if (this.landPointisOnMap(ClusterMapHelper.formKey(currentXY[0], currentXY[1]))) {
							type = this.findPoint(ClusterMapHelper.formKey(currentXY[0], currentXY[1])).getType();
						} else {
							currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, growDirection);
							break;
						}
					}
					while (type.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
						currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, growDirection);
						if (this.landPointisOnMap(ClusterMapHelper.formKey(currentXY[0], currentXY[1]))) {
							type = this.findPoint(ClusterMapHelper.formKey(currentXY[0], currentXY[1])).getType();
						} else {
							currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1,
									ClusterDirectionHelper.oppositeDirection(growDirection));
							break;
						}
					}
				} else {
					while (type.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
						currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, growDirection);
						if (this.landPointisOnMap(ClusterMapHelper.formKey(currentXY[0], currentXY[1]))) {
							type = this.findPoint(ClusterMapHelper.formKey(currentXY[0], currentXY[1])).getType();
						} else {
							currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1,
									ClusterDirectionHelper.oppositeDirection(growDirection));
							break;
						}
					}
					while (!type.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
						currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1,
								ClusterDirectionHelper.oppositeDirection(growDirection));
						if (this.landPointisOnMap(ClusterMapHelper.formKey(currentXY[0], currentXY[1]))) {
							type = this.findPoint(ClusterMapHelper.formKey(currentXY[0], currentXY[1])).getType();
						} else {
							currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, growDirection);
							break;
						}
					}
				}

				int[] oldCurrentXY = new int[2];
				oldCurrentXY[0] = currentXY[0];
				oldCurrentXY[1] = currentXY[1];

				currentXY = orthogonalLotize(currentXY, finalXY, driveDirection, growDirection,
						seed % ClusterConstants.MAX_HOUSE_COMBINATION, upperNearEnd);
				if (findPoint(ClusterMapHelper.formKey(currentXY[0], currentXY[1])).getGramaticalType() == null) {
					findPoint(ClusterMapHelper.formKey(currentXY[0], currentXY[1]))
							.setGramaticalType("l" + "-" + oldCurrentXY[0] + "-" + oldCurrentXY[1] + "-"
									+ upperNearInitial[0] + "-" + upperNearInitial[1] + "-" + upperNearEnd[0] + "-"
									+ upperNearEnd[1] + "-" + currentXY[0] + "-" + currentXY[1]);
				}

				seed += 1;
				distance -= configuration.getLotConfiguration().getSideSize();
			} else {
				currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, driveDirection);
				distance--;
			}
		}
	}

	private boolean canBeOrthogonallyLotized(int[] initialXY, int[] finalXY, int driveDirection, int growDirection,
			int i, int[] upperNearInitial, int[] upperNearEnd) {
		int times = configuration.getLotConfiguration().getSideSize();
		int[] currentXY = new int[] { initialXY[0], initialXY[1] };
		while (times != 0) {
			if (!landPointisOnMap(ClusterMapHelper.formKey(currentXY[0], currentXY[1])))
				return false;
			String type = this.findPoint(ClusterMapHelper.formKey(currentXY[0], currentXY[1])).getType();
			while (!type.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
				currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1,
						ClusterDirectionHelper.oppositeDirection(growDirection));
				if (!landPointisOnMap(ClusterMapHelper.formKey(currentXY[0], currentXY[1])))
					return false;
				type = this.findPoint(ClusterMapHelper.formKey(currentXY[0], currentXY[1])).getType();
			}
			while (type.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
				currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, growDirection);
				if (!landPointisOnMap(ClusterMapHelper.formKey(currentXY[0], currentXY[1])))
					return false;
				type = this.findPoint(ClusterMapHelper.formKey(currentXY[0], currentXY[1])).getType();
			}

			if (currentXY[0] == finalXY[0] && currentXY[1] == finalXY[1]) {
				return false;
			}

			int growTimes = configuration.getLotConfiguration().getDepthSize() * 2;
			int[] currentOrthXY = new int[] { currentXY[0], currentXY[1] };
			while (growTimes != 0) {
				if (!this.landPointisOnMap(ClusterMapHelper.formKey(currentOrthXY[0], currentOrthXY[1])))
					return false;
				type = this.findPoint(ClusterMapHelper.formKey(currentOrthXY[0], currentOrthXY[1])).getType();
				if (!type.equals(ClusterConfiguration.EMPTY_MARK) && !type.equals(ClusterConfiguration.BORDER_MARK))
					return false;
				currentOrthXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentOrthXY, 1, growDirection);
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
			currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, driveDirection);
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
			String type = this.findPoint(ClusterMapHelper.formKey(movCurrentXY[0], movCurrentXY[1])).getType();
			if (!type.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
				while (!type.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
					movCurrentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(movCurrentXY, 1,
							ClusterDirectionHelper.oppositeDirection(growDirection));
					type = this.findPoint(ClusterMapHelper.formKey(movCurrentXY[0], movCurrentXY[1])).getType();
				}
				while (type.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
					movCurrentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(movCurrentXY, 1, growDirection);
					type = this.findPoint(ClusterMapHelper.formKey(movCurrentXY[0], movCurrentXY[1])).getType();
				}
			} else {
				while (type.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
					movCurrentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(movCurrentXY, 1, growDirection);
					type = this.findPoint(ClusterMapHelper.formKey(movCurrentXY[0], movCurrentXY[1])).getType();
				}
				while (!type.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
					movCurrentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(movCurrentXY, 1,
							ClusterDirectionHelper.oppositeDirection(growDirection));
					type = this.findPoint(ClusterMapHelper.formKey(movCurrentXY[0], movCurrentXY[1])).getType();
				}
			}

			int growTimes = configuration.getLotConfiguration().getDepthSize() * 2;
			int[] currentOrthXY = new int[] { movCurrentXY[0], movCurrentXY[1] };
			while (growTimes != 0) {
				this.findPoint(ClusterMapHelper.formKey(currentOrthXY[0], currentOrthXY[1])).setType("" + seed);
				currentOrthXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentOrthXY, 1, growDirection);
				growTimes--;
			}

			if (times != 1) {
				currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, driveDirection);
			}
			times--;
		}

		int[] movUpperNearEndXY = new int[] { upperNearEnd[0], upperNearEnd[1] };
		int growTimes = configuration.getLotConfiguration().getDepthSize() * 2;
		while (growTimes != 0) {
			this.findPoint(ClusterMapHelper.formKey(movUpperNearEndXY[0], movUpperNearEndXY[1])).setType("" + seed);
			movUpperNearEndXY = ClusterMapHelper.moveKeyByOffsetAndDirection(movUpperNearEndXY, 1,
					ClusterDirectionHelper.oppositeDirection(growDirection));
			growTimes--;
		}
		currentXY = movUpperNearEndXY;
		return currentXY;
	}

	private void createSmallEntranceRoute(int[] initialXY, int[] finalXY, int driveDirection, int growDirection) {
		int times = ClusterConfiguration.LOCAL_BRANCH_SIZE;
		int[] currentXY = new int[] { initialXY[0], initialXY[1] };
		int distance = (int) Math.sqrt(Math.pow(initialXY[0] - finalXY[0], 2) + Math.pow(initialXY[1] - finalXY[1], 2));
		if (distance < 30) {
			return;
		}
		int[] lowerXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, distance / 2, driveDirection);
		int[] upperXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, (distance / 2) + 1, driveDirection);

		while (times != 0) {
			int growTimes = configuration.getLotConfiguration().getDepthSize();
			int[] lowerOrthXY = new int[] { lowerXY[0], lowerXY[1] };
			while (growTimes != 0) {
				if (this.landPointisOnMap(ClusterMapHelper.formKey(lowerOrthXY[0], lowerOrthXY[1]))
						&& !this.findPoint(ClusterMapHelper.formKey(lowerOrthXY[0], lowerOrthXY[1])).getType()
								.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK))
					this.findPoint(ClusterMapHelper.formKey(lowerOrthXY[0], lowerOrthXY[1]))
							.setType(ClusterConfiguration.CLUSTER_ENTRANCE_MARK);
				lowerOrthXY = ClusterMapHelper.moveKeyByOffsetAndDirection(lowerOrthXY, 1, growDirection);
				growTimes--;
			}
			lowerXY = ClusterMapHelper.moveKeyByOffsetAndDirection(lowerXY, 1,
					ClusterDirectionHelper.oppositeDirection(driveDirection));
			times--;

			if (times == 0)
				continue;

			growTimes = configuration.getLotConfiguration().getDepthSize();
			int[] upperOrthXY = new int[] { upperXY[0], upperXY[1] };
			while (growTimes != 0) {
				if (this.landPointisOnMap(ClusterMapHelper.formKey(upperOrthXY[0], upperOrthXY[1]))
						&& !this.findPoint(ClusterMapHelper.formKey(upperOrthXY[0], upperOrthXY[1])).getType()
								.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK))
					this.findPoint(ClusterMapHelper.formKey(upperOrthXY[0], upperOrthXY[1]))
							.setType(ClusterConfiguration.CLUSTER_ENTRANCE_MARK);
				upperOrthXY = ClusterMapHelper.moveKeyByOffsetAndDirection(upperOrthXY, 1, growDirection);
				growTimes--;
			}
			upperXY = ClusterMapHelper.moveKeyByOffsetAndDirection(upperXY, 1, driveDirection);
			times--;
		}
	}

	private void createInnerRoad(int[] initialXY, int[] finalXY, int driveDirection, int growDirection) {
		int times = (int) Math.sqrt(Math.pow(initialXY[0] - finalXY[0], 2) + Math.pow(initialXY[1] - finalXY[1], 2));
		while (times != 0) {
			int growTimes = ClusterConfiguration.LOCAL_BRANCH_SIZE;
			int[] currentOrthXY = new int[] { initialXY[0], initialXY[1] };
			while (growTimes != 0) {
				if (this.landPointisOnMap(ClusterMapHelper.formKey(currentOrthXY[0], currentOrthXY[1]))
						&& this.findPoint(ClusterMapHelper.formKey(currentOrthXY[0], currentOrthXY[1])).getType()
								.equals(ClusterConfiguration.EMPTY_MARK)) {
					this.findPoint(ClusterMapHelper.formKey(currentOrthXY[0], currentOrthXY[1]))
							.setType(ClusterConfiguration.LOCAL_MARK);
				}
				currentOrthXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentOrthXY, 1, growDirection);
				growTimes--;
			}
			initialXY = ClusterMapHelper.moveKeyByOffsetAndDirection(initialXY, 1, driveDirection);
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
				if (!this.landPointisOnMap(ClusterMapHelper.formKey(currentOrthXY[0], currentOrthXY[1])))
					return false;
				String type = this.findPoint(ClusterMapHelper.formKey(currentOrthXY[0], currentOrthXY[1])).getType();
				if (!type.equals(ClusterConfiguration.EMPTY_MARK) && !type.equals(ClusterConfiguration.PARK_MARK)
						&& !type.equals(ClusterConfiguration.INTERNAL_LOCAL_MARK)
						&& !type.equals(ClusterConfiguration.BORDER_MARK))
					return false;
				currentOrthXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentOrthXY, 1, growDirection);
				growTimes--;
			}
			currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, driveDirection);
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
				this.findPoint(ClusterMapHelper.formKey(currentOrthXY[0], currentOrthXY[1])).setType("" + seed);
				currentOrthXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentOrthXY, 1, growDirection);
				growTimes--;
			}

			if (dual) {
				growTimes = configuration.getLotConfiguration().getDepthSize();
				while (growTimes != 0) {
					this.findPoint(ClusterMapHelper.formKey(currentOrthXY[0], currentOrthXY[1]))
							.setType("" + (seed + 1));
					currentOrthXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentOrthXY, 1, growDirection);
					growTimes--;
				}
			}

			currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, driveDirection);
			times--;
		}
		return currentXY;
	}

	public void preciseLotization(ClusterPolygon clusterPolygon, boolean withContribution) {
		boolean contribute = false;
		for (int i = 0; i < clusterPolygon.getPoints().size(); i++) {
			// detect whereas the kind of side it is 0.0 or infinite if not
			// we simply ignore it (for now)
			int[] initialXY = ClusterMapHelper.breakKey(clusterPolygon.getPoints().get(i));
			int[] finalXY = ClusterMapHelper
					.breakKey(clusterPolygon.getPoints().get((i + 1) % clusterPolygon.getPoints().size()));
			int growDirection = -1;
			int driveDirection = -1;

			if (initialXY[0] == finalXY[0]) {// infinite
				growDirection = ClusterDirectionHelper.perpendicularDirection(initialXY, clusterPolygon.getCentroid(),
						ClusterConstants.NORTH);
				driveDirection = initialXY[1] < finalXY[1] ? ClusterConstants.NORTH : ClusterConstants.SOUTH;
				createEntranceRoute(initialXY, finalXY, driveDirection, growDirection);
				if (withContribution) {
					withContribution = false;
					contribute = true;
				}
			} else if (initialXY[1] == finalXY[1]) {
				growDirection = ClusterDirectionHelper.perpendicularDirection(initialXY, clusterPolygon.getCentroid(),
						ClusterConstants.EAST);
				driveDirection = initialXY[0] < finalXY[0] ? ClusterConstants.EAST : ClusterConstants.WEST;
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

						if (findPoint(ClusterMapHelper.formKey(currentXY[0], currentXY[1]))
								.getGramaticalType() == null) {

							int[] points = interpretBuilding(currentXY[0], currentXY[1], driveDirection, growDirection,
									configuration.getLotConfiguration().getSideSize(),
									configuration.getLotConfiguration().getDepthSize());
							findPoint(ClusterMapHelper.formKey(currentXY[0], currentXY[1])).setGramaticalType(
									"l" + "-" + points[0] + "-" + points[1] + "-" + points[2] + "-" + points[3] + "-"
											+ points[4] + "-" + points[5] + "-" + points[6] + "-" + points[7]);

							int[] deeperHouse = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY,
									configuration.getLotConfiguration().getDepthSize(), growDirection);
							points = interpretBuilding(deeperHouse[0], deeperHouse[1], driveDirection, growDirection,
									configuration.getLotConfiguration().getSideSize(),
									configuration.getLotConfiguration().getDepthSize());
							findPoint(ClusterMapHelper.formKey(deeperHouse[0], deeperHouse[1])).setGramaticalType(
									"l" + "-" + points[0] + "-" + points[1] + "-" + points[2] + "-" + points[3] + "-"
											+ points[4] + "-" + points[5] + "-" + points[6] + "-" + points[7]);
						}

						currentXY = lotize(currentXY, finalXY, driveDirection, growDirection, true,
								seed % ClusterConstants.MAX_HOUSE_COMBINATION);
						seed += 2;
					} else {
						currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, driveDirection);
					}
				}
			}
		}
	}

	private int[] createContribution(int[] initialXY, int[] finalXY, int driveDirection, int growDirection) {
		int times = ClusterConfiguration.CONTRIBUTION_SIDE_MINIMUN_SIZE;
		int[] currentXY = new int[] { initialXY[0], initialXY[1] };
		int distance = (int) Math.sqrt(Math.pow(initialXY[0] - finalXY[0], 2) + Math.pow(initialXY[1] - finalXY[1], 2));
		if (distance < times) {
			return currentXY;
		}

		while (times != 0) {
			int growTimes = configuration.getLotConfiguration().getDepthSize() * 2;
			int[] lowerOrthXY = new int[] { currentXY[0], currentXY[1] };
			while (growTimes != 0) {
				this.findPoint(ClusterMapHelper.formKey(lowerOrthXY[0], lowerOrthXY[1]))
						.setType(ClusterConfiguration.CONTRIBUTION_MARK);
				lowerOrthXY = ClusterMapHelper.moveKeyByOffsetAndDirection(lowerOrthXY, 1, growDirection);
				growTimes--;
			}
			currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, driveDirection);
			times--;
		}
		return currentXY;
	}

	private void createEntranceRoute(int[] initialXY, int[] finalXY, int driveDirection, int growDirection) {
		int times = ClusterConfiguration.CLUSTER_ENTRANCE_SIZE;
		int[] currentXY = new int[] { initialXY[0], initialXY[1] };
		int distance = (int) Math.sqrt(Math.pow(initialXY[0] - finalXY[0], 2) + Math.pow(initialXY[1] - finalXY[1], 2));
		if (distance < 30) {
			return;
		}
		int[] lowerXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, distance / 2, driveDirection);
		int[] upperXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, (distance / 2) + 1, driveDirection);

		while (times != 0) {
			int growTimes = configuration.getLotConfiguration().getDepthSize() * 2;
			int[] lowerOrthXY = new int[] { lowerXY[0], lowerXY[1] };
			while (growTimes != 0) {
				this.findPoint(ClusterMapHelper.formKey(lowerOrthXY[0], lowerOrthXY[1]))
						.setType(ClusterConfiguration.CLUSTER_ENTRANCE_MARK);
				lowerOrthXY = ClusterMapHelper.moveKeyByOffsetAndDirection(lowerOrthXY, 1, growDirection);
				growTimes--;
			}
			lowerXY = ClusterMapHelper.moveKeyByOffsetAndDirection(lowerXY, 1,
					ClusterDirectionHelper.oppositeDirection(driveDirection));
			times--;

			if (times == 0)
				continue;

			growTimes = configuration.getLotConfiguration().getDepthSize() * 2;
			int[] upperOrthXY = new int[] { upperXY[0], upperXY[1] };
			while (growTimes != 0) {
				this.findPoint(ClusterMapHelper.formKey(upperOrthXY[0], upperOrthXY[1]))
						.setType(ClusterConfiguration.CLUSTER_ENTRANCE_MARK);
				upperOrthXY = ClusterMapHelper.moveKeyByOffsetAndDirection(upperOrthXY, 1, growDirection);
				growTimes--;
			}
			upperXY = ClusterMapHelper.moveKeyByOffsetAndDirection(upperXY, 1, driveDirection);
			times--;
		}
	}

	private void createWalkRoute(int[] initialXY, int[] finalXY, int driveDirection, int growDirection) {
		int times = ClusterConfiguration.WALK_BRANCH_SIZE * 2;
		int distance = (int) Math.sqrt(Math.pow(initialXY[0] - finalXY[0], 2) + Math.pow(initialXY[1] - finalXY[1], 2));
		if (distance < 30) {
			return;
		}
		int[] lowerXY = ClusterMapHelper.moveKeyByOffsetAndDirection(initialXY,
				configuration.getLotConfiguration().getDepthSize() * 2, driveDirection);
		int[] upperXY = ClusterMapHelper.moveKeyByOffsetAndDirection(finalXY,
				configuration.getLotConfiguration().getDepthSize() * 2,
				ClusterDirectionHelper.oppositeDirection(driveDirection));

		while (times != 0) {
			int growTimes = configuration.getLotConfiguration().getDepthSize() * 2;
			int[] lowerOrthXY = new int[] { lowerXY[0], lowerXY[1] };
			while (growTimes != 0) {
				this.findPoint(ClusterMapHelper.formKey(lowerOrthXY[0], lowerOrthXY[1]))
						.setType(ClusterConfiguration.CLUSTER_ENTRANCE_MARK);
				lowerOrthXY = ClusterMapHelper.moveKeyByOffsetAndDirection(lowerOrthXY, 1, growDirection);
				growTimes--;
			}
			lowerXY = ClusterMapHelper.moveKeyByOffsetAndDirection(lowerXY, 1, driveDirection);
			times--;

			growTimes = configuration.getLotConfiguration().getDepthSize() * 2;
			int[] upperOrthXY = new int[] { upperXY[0], upperXY[1] };
			while (growTimes != 0) {
				this.findPoint(ClusterMapHelper.formKey(upperOrthXY[0], upperOrthXY[1]))
						.setType(ClusterConfiguration.CLUSTER_ENTRANCE_MARK);
				upperOrthXY = ClusterMapHelper.moveKeyByOffsetAndDirection(upperOrthXY, 1, growDirection);
				growTimes--;
			}
			upperXY = ClusterMapHelper.moveKeyByOffsetAndDirection(upperXY, 1,
					ClusterDirectionHelper.oppositeDirection(driveDirection));
			times--;
		}
	}

	public ClusterPolygon getAsClusterPolygon() {
		ClusterPolygon clusterPolygon = new ClusterPolygon(this.getNodes());
		clusterPolygon.setComplete(true);
		return clusterPolygon;
	}

	private int[] interpretBuilding(int initialX, int finalX, int driveDirection, int growDirection, int sideSize,
			int depthSize) {
		int[] coords = new int[8];
		int[] xy = new int[2];
		coords[0] = initialX;
		coords[1] = finalX;

		xy = ClusterMapHelper.moveKeyByOffsetAndDirection(coords, sideSize, driveDirection);
		coords[2] = xy[0];
		coords[3] = xy[1];

		xy = ClusterMapHelper.moveKeyByOffsetAndDirection(xy, depthSize, growDirection);
		coords[4] = xy[0];
		coords[5] = xy[1];

		xy = ClusterMapHelper.moveKeyByOffsetAndDirection(xy, sideSize,
				ClusterDirectionHelper.oppositeDirection(driveDirection));
		coords[6] = xy[0];
		coords[7] = xy[1];
		return coords;
	}

	public String stringify() {
		String mapString = "";
		for (int j = 0; j < pointsy; j++) {
			String type = getLandPoint(ClusterMapHelper.formKey(0, j)).getType();
			int repetitions = 1;
			for (int i = 0; i < pointsx; i++) {
				if (type.equals(getLandPoint(ClusterMapHelper.formKey(i, j)).getType())) {
					repetitions++;
				} else {
					mapString += type + "" + repetitions + ",";
					repetitions = 1;
					type = getLandPoint(ClusterMapHelper.formKey(i, j)).getType();
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
				String type = getLandPoint(ClusterMapHelper.formKey(i, j)).getGramaticalType();
				if (type != null) {
					mapString += type + ",";
				}
			}
		}
		return mapString;// remove last ,
	}
}