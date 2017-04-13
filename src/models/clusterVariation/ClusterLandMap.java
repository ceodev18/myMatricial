package models.clusterVariation;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import helpers.clusterVariation.ClusterDirectionHelper;
import helpers.clusterVariation.ClusterMapHelper;
import interfaces.clusterVariation.ClusterConfiguration;
import interfaces.clusterVariation.ClusterConstants;

public class ClusterLandMap {
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

	public void printMapToFile() {
		try {
			PrintWriter writer = new PrintWriter("printed-map.txt", "UTF-8");

			for (int i = pointsx - 1; i >= 0; i--) {
				for (int j = 0; j < pointsy; j++) {
					writer.print(getLandPoint(ClusterMapHelper.formKey(i, j)).getType());
				}
				writer.println();
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
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

	public void impreciseLotization(ClusterPolygon clusterPolygon) {
		if (!clusterPolygon.canBelotized()) {
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
						ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE)) {
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
				.translateTowardCenter(ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE);
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
				ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE + ClusterConfiguration.LOCAL_BRANCH_SIZE);
		if (reducedPoints.size() != 0) {
			ClusterPolygon innerPolygon = new ClusterPolygon();
			innerPolygon.setPoints(reducedPoints);
			innerPolygon.setComplete(true);
			impreciseLotization(innerPolygon);
		}
	}

	public void orthogonalLotization(int[] initialXY, int[] finalXY, int driveDirection, int growDirection) {
		int[] currentXY = new int[] { initialXY[0], initialXY[1] };
		int seed = 0;
		int distance = (int) Math.sqrt(Math.pow(currentXY[0] - finalXY[0], 2) + Math.pow(currentXY[1] - finalXY[1], 2));
		while (distance > 0) {
			if (canBeOrthogonallyLotized(currentXY, finalXY, driveDirection, growDirection,
					ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE)) {
				currentXY = orthogonalLotize(currentXY, finalXY, driveDirection, growDirection,
						seed % ClusterConstants.MAX_HOUSE_COMBINATION);
				seed += 1;
				distance -= ClusterConfiguration.HOUSE_SIDE_MINIMUN_SIZE;
			} else {
				currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, driveDirection);
				distance--;
			}
		}
	}

	private boolean canBeOrthogonallyLotized(int[] initialXY, int[] finalXY, int driveDirection, int growDirection,
			int i) {
		int times = ClusterConfiguration.HOUSE_SIDE_MINIMUN_SIZE;
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

			int growTimes = ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2;
			int[] currentOrthXY = new int[] { currentXY[0], currentXY[1] };
			while (growTimes != 0) {
				if (!this.landPointisOnMap(ClusterMapHelper.formKey(currentOrthXY[0], currentOrthXY[1])))
					return false;
				type = this.findPoint(ClusterMapHelper.formKey(currentOrthXY[0], currentOrthXY[1])).getType();
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

	private int[] orthogonalLotize(int[] initialXY, int[] finalXY, int driveDirection, int growDirection, int seed) {
		int times = ClusterConfiguration.HOUSE_SIDE_MINIMUN_SIZE;
		int[] currentXY = new int[] { initialXY[0], initialXY[1] };
		while (times != 0) {
			String type = this.findPoint(ClusterMapHelper.formKey(currentXY[0], currentXY[1])).getType();
			while (!type.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
				currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1,
						ClusterDirectionHelper.oppositeDirection(growDirection));
				type = this.findPoint(ClusterMapHelper.formKey(currentXY[0], currentXY[1])).getType();
			}
			while (type.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
				currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, growDirection);
				type = this.findPoint(ClusterMapHelper.formKey(currentXY[0], currentXY[1])).getType();
			}

			int growTimes = ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2;
			int[] currentOrthXY = new int[] { currentXY[0], currentXY[1] };
			while (growTimes != 0) {
				this.findPoint(ClusterMapHelper.formKey(currentOrthXY[0], currentOrthXY[1])).setType("" + seed);
				currentOrthXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentOrthXY, 1, growDirection);
				growTimes--;
			}
			currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, driveDirection);
			times--;
		}
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
			int growTimes = ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE;
			int[] lowerOrthXY = new int[] { lowerXY[0], lowerXY[1] };
			while (growTimes != 0) {
				if (!this.findPoint(ClusterMapHelper.formKey(lowerOrthXY[0], lowerOrthXY[1])).getType()
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

			growTimes = ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE;
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

	private void createInnerRoad(int[] initialXY, int[] finalXY, int driveDirection, int growDirection) {
		int times = (int) Math.sqrt(Math.pow(initialXY[0] - finalXY[0], 2) + Math.pow(initialXY[1] - finalXY[1], 2));
		while (times != 0) {
			int growTimes = ClusterConfiguration.LOCAL_BRANCH_SIZE;
			int[] currentOrthXY = new int[] { initialXY[0], initialXY[1] };
			while (growTimes != 0) {
				if (this.findPoint(ClusterMapHelper.formKey(currentOrthXY[0], currentOrthXY[1])).getType()
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
		int times = ClusterConfiguration.HOUSE_SIDE_MINIMUN_SIZE;
		int[] currentXY = new int[] { initialXY[0], initialXY[1] };
		while (times != 0) {
			if (currentXY[0] == finalXY[0] && currentXY[1] == finalXY[1]) {
				return false;
			}
			int growTimes = ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE;
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
		int times = ClusterConfiguration.HOUSE_SIDE_MINIMUN_SIZE;
		int[] currentXY = initialXY;
		while (times != 0) {
			int growTimes = ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE;
			int[] currentOrthXY = new int[] { currentXY[0], currentXY[1] };
			while (growTimes != 0) {
				this.findPoint(ClusterMapHelper.formKey(currentOrthXY[0], currentOrthXY[1])).setType("" + seed);
				currentOrthXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentOrthXY, 1, growDirection);
				growTimes--;
			}

			if (dual) {
				growTimes = ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE;
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
					currentXY = createConribution(currentXY, finalXY, driveDirection, growDirection);
					contribute = false;
				} else {
					if (canBeLotized(currentXY, finalXY, driveDirection, growDirection,
							ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2)) {
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

	private int[] createConribution(int[] initialXY, int[] finalXY, int driveDirection, int growDirection) {
		int times = ClusterConfiguration.CONTRIBUTION_SIDE_MINIMUN_SIZE;
		int[] currentXY = new int[] { initialXY[0], initialXY[1] };
		int distance = (int) Math.sqrt(Math.pow(initialXY[0] - finalXY[0], 2) + Math.pow(initialXY[1] - finalXY[1], 2));
		if (distance < times) {
			return currentXY;
		}

		while (times != 0) {
			int growTimes = ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2;
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
			int growTimes = ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2;
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

			growTimes = ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2;
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
				ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2, driveDirection);
		int[] upperXY = ClusterMapHelper.moveKeyByOffsetAndDirection(finalXY,
				ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2,
				ClusterDirectionHelper.oppositeDirection(driveDirection));

		while (times != 0) {
			int growTimes = ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2;
			int[] lowerOrthXY = new int[] { lowerXY[0], lowerXY[1] };
			while (growTimes != 0) {
				this.findPoint(ClusterMapHelper.formKey(lowerOrthXY[0], lowerOrthXY[1]))
						.setType(ClusterConfiguration.CLUSTER_ENTRANCE_MARK);
				lowerOrthXY = ClusterMapHelper.moveKeyByOffsetAndDirection(lowerOrthXY, 1, growDirection);
				growTimes--;
			}
			lowerXY = ClusterMapHelper.moveKeyByOffsetAndDirection(lowerXY, 1, driveDirection);
			times--;

			growTimes = ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2;
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
}