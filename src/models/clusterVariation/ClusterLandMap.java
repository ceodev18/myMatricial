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

	private ClusterLandPoint centroid;
	private int baseArea;
	private int polygonalArea;

	private int numberOfClusters;

	private int emptyFocalArea;
	private int fullFocalArea;

	private Map<Integer, ClusterLandPoint> map;

	private List<ClusterLandRoute> landRoutes = new ArrayList<>();
	private List<Integer> nodes = new ArrayList<>();
	private List<Integer> polygonFull;
	private List<ClusterLandPoint> polygonNodes;

	public ClusterLandMap(int pointsx, int pointsy) {
		this.setPointsx(pointsx);
		this.setPointsy(pointsy);
		this.setBaseArea(pointsx * pointsy);

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

	public ClusterLandPoint getCentroid() {
		return centroid;
	}

	public void setCentroid(ClusterLandPoint centroid) {
		this.centroid = centroid;
	}

	public int getBaseArea() {
		return baseArea;
	}

	public void setBaseArea(int baseArea) {
		this.baseArea = baseArea;
	}

	public int getPolygonalArea() {
		return polygonalArea;
	}

	public void setPolygonalArea(int polygonalArea) {
		this.polygonalArea = polygonalArea;
	}

	public ClusterLandPoint findPoint(int entryPointId) {
		return map.get(entryPointId);
	}

	/**
	 * This method marks all points that are not inside the polygon border as
	 * restricted area. This must be an ordered set of consecutive points (after
	 * all the input from android looks like that.
	 */
	public void createBorderFromPolygon(List<ClusterLandPoint> polygon) {
		setPolygonNodes(polygon);
		List<List<Integer>> fullPolygon = new ArrayList<>();
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
					getLandPoint(ClusterMapHelper.formKey(polygon.get(i).getX(), w)).setType(ClusterConstants.POLYGON_LIMIT);
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
					getLandPoint(ClusterMapHelper.formKey(w, polygon.get(i).getY())).setType(ClusterConstants.POLYGON_LIMIT);
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

		// true chained polygon
		polygonFull = new ArrayList<>();
		for (int i = 0; i < fullPolygon.get(0).size(); i++) {
			polygonFull.add(fullPolygon.get(0).get(i));
		}
		fullPolygon.remove(0);

		int i = 0;
		while (fullPolygon.size() > 0) {
			// normal case
			if (fullPolygon.get(i).get(0).intValue() == polygonFull.get(polygonFull.size() - 1).intValue()) {
				for (int j = 1; j < fullPolygon.get(i).size(); j++) {
					polygonFull.add(fullPolygon.get(i).get(j));
				}
				fullPolygon.remove(i);
				i = 0;
				continue;
			}
			// inverted case
			if (fullPolygon.get(i).get(fullPolygon.get(i).size() - 1).intValue() == polygonFull
					.get(polygonFull.size() - 1).intValue()) {
				for (int j = fullPolygon.get(i).size() - 1; j > -1; j--) {
					polygonFull.add(fullPolygon.get(i).get(j));
				}
				fullPolygon.remove(i);
				i = -1;
			}
			i++;
		}

		// we fill everything outside of it with Xs
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
					getLandPoint(ClusterMapHelper.formKey(x, y)).setType(ClusterConstants.OUTSIDE_POLYGON);
					break;
				case 1:
					if (getLandPoint(ClusterMapHelper.formKey(x, y)).getType() != ClusterConstants.POLYGON_LIMIT) {
						getLandPoint(ClusterMapHelper.formKey(x, y)).setType(ClusterConstants.OUTSIDE_POLYGON);
					}
					break;
				case 2:
					if (!reversed) {
						for (int w = pInitialLimit + 1; w < y; w++) {
							getLandPoint(ClusterMapHelper.formKey(x, w)).setType(ClusterConstants.INSIDE_POLYGON);
						}
						reversed = true;
					} else {
						getLandPoint(ClusterMapHelper.formKey(x, y)).setType(ClusterConstants.OUTSIDE_POLYGON);
					}
					break;
				}
			}
		}

		findPolygonalArea(polygon);
		findCentroid(polygon);
		setNumberOfClusters(polygonalArea / 800);
		setEmptyFocalArea((int) ((polygonalArea) * 0.08)); // ((Integer)(polygonalArea/10000)>10?(polygonalArea)*0.3:(polygonalArea)*0.08));
		setFullFocalArea((int) (polygonalArea * 0.05));
		clearDottedLimits();
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
		for (int i = 0; i < pointsx; i++) {
			for (int j = 0; j < pointsy; j++) {
				ClusterLandPoint clusterLandPoint = this.getLandPoint(ClusterMapHelper.formKey(i, j));
				if (clusterLandPoint.getType().equals(ClusterConfiguration.POLYGON_BORDER)) {
					clusterLandPoint.setType(ClusterConfiguration.EMPTY_MARK);
				}
			}
		}
	}

	public int getNumberOfParks() {
		// the problem with this is that we should minimize areas.
		if (polygonalArea > 1000000) {
			return 16;
		} else {
			return emptyFocalArea / 800 < 16 ? emptyFocalArea / 800 : 8;// 240*25
		}
	}

	private void findPolygonalArea(List<ClusterLandPoint> polygon) {
		int absoluteArea = 0;
		for (int i = 0; i < polygon.size(); i++) {
			absoluteArea += (polygon.get(i).getX() * polygon.get((i + 1) % polygon.size()).getY())
					- (polygon.get(i).getY() * polygon.get((i + 1) % polygon.size()).getX());
		}
		setPolygonalArea(Math.abs(absoluteArea) / 2);
	}

	private void findCentroid(List<ClusterLandPoint> polygon) {
		this.setCentroid(new ClusterLandPoint(pointsx / 2, pointsy / 2));
	}

	public void printMapToFile() {
		try {
			PrintWriter writer = new PrintWriter("printed-map.txt", "UTF-8");
			for (int j = pointsy - 1; j >= 0; j--) {
				for (int i = 0; i < pointsx; i++) {
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

	public int getNumberOfClusters() {
		return numberOfClusters;
	}

	public void setNumberOfClusters(int numberOfClusters) {
		this.numberOfClusters = numberOfClusters;
	}

	public int getEmptyFocalArea() {
		return emptyFocalArea;
	}

	public void setEmptyFocalArea(int emptyFocalArea) {
		this.emptyFocalArea = emptyFocalArea;
	}

	public int getFullFocalArea() {
		return fullFocalArea;
	}

	public void setFullFocalArea(int fullFocalArea) {
		this.fullFocalArea = fullFocalArea;
	}

	public List<ClusterLandRoute> getLandRoutes() {
		return landRoutes;
	}

	public void setLandRoutes(List<ClusterLandRoute> landRoutes) {
		this.landRoutes = landRoutes;
	}

	public boolean landPointisOnMap(int pointId) {
		int[] xy = ClusterMapHelper.breakKey(pointId);
		return xy[0] < pointsx && xy[0] > 0 && xy[1] < pointsy && xy[1] > 0;
	}

	public List<Integer> getNodes() {
		return nodes;
	}

	public List<Integer> getFinalPolygon() {
		return polygonFull;
	}

	public void setNodes(List<Integer> nodes) {
		this.nodes = nodes;
	}

	public List<ClusterLandPoint> getPolygonNodes() {
		return polygonNodes;
	}

	public void setPolygonNodes(List<ClusterLandPoint> polygonNodes) {
		this.polygonNodes = polygonNodes;
	}

	public boolean intersectMainRoute(int entryPointId) {
		int initialPoint = landRoutes.get(0).getInitialPointId();
		int finalPoint = landRoutes.get(0).getFinalPointId();

		int[] initialXY = ClusterMapHelper.breakKey(initialPoint);
		int[] finalXY = ClusterMapHelper.breakKey(finalPoint);
		int[] entryXY = ClusterMapHelper.breakKey(entryPointId);

		return (initialXY[0] < entryXY[0] && entryXY[0] < finalXY[0])
				|| ((initialXY[1] < entryXY[1] && entryXY[1] < finalXY[1]));
	}

	public boolean isNode(int x, int y) {
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

			if (findPoint(ClusterMapHelper.formKey(x, y + 1)).getType().equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if (y - 1 != -1) {
			if (findPoint(ClusterMapHelper.formKey(x, y - 1)).getType().equals(ClusterConfiguration.NODE_MARK)) {
				node++;
				nodeInBorder[1] = true;
			}

			if (findPoint(ClusterMapHelper.formKey(x, y - 1)).getType().equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if (x - 1 != -1) {
			if (findPoint(ClusterMapHelper.formKey(x - 1, y)).getType().equals(ClusterConfiguration.NODE_MARK)) {
				node++;
				nodeInBorder[2] = true;
			}

			if (findPoint(ClusterMapHelper.formKey(x - 1, y)).getType().equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if (x + 1 != pointsx) {
			if (findPoint(ClusterMapHelper.formKey(x + 1, y)).getType().equals(ClusterConfiguration.NODE_MARK)) {
				node++;
				nodeInBorder[3] = true;
			}

			if (findPoint(ClusterMapHelper.formKey(x + 1, y)).getType().equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if ((y + 1 != pointsy) && (y - 1 != -1) && (x - 1 != -1) && (x + 1 != pointsx)) {
			if ((findPoint(ClusterMapHelper.formKey(x, y - 1)).getType().equals(ClusterConfiguration.ARTERIAL_MARK)
					|| findPoint(ClusterMapHelper.formKey(x, y - 1)).getType().equals(ClusterConfiguration.LOCAL_MARK))
					&& findPoint(ClusterMapHelper.formKey(x - 1, y)).getType().equals(ClusterConfiguration.COLLECTOR_MARK)
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

	public void joinWithPolygonalBorder(ClusterPolygon clusterPolygon) {
		// we find the first polygon border
		int initialPoint;
		for (initialPoint = 0; initialPoint < polygonFull.size(); initialPoint++) {
			if (polygonFull.get(initialPoint).intValue() == clusterPolygon.getPoints().get(0).intValue()) {
				break;
			}
		}

		int finalPoint;
		for (finalPoint = 0; finalPoint < polygonFull.size(); finalPoint++) {
			if (polygonFull.get(finalPoint).intValue() == clusterPolygon.getPoints()
					.get(clusterPolygon.getPoints().size() - 1).intValue()) {
				break;
			}
		}

		if ((initialPoint != polygonFull.size()) && (finalPoint != polygonFull.size())) {
			if (initialPoint < finalPoint) {
				for (int i = initialPoint + 1; i < finalPoint; i++) {
					for (int j = 0; j < polygonNodes.size() - 1; j++) {
						if (polygonFull.get(i).intValue() == polygonNodes.get(j).getId()) {
							clusterPolygon.getPoints().add(polygonFull.get(i));
						}
					}
				}
			} else {
				for (int i = finalPoint + 1; i < initialPoint; i++) {
					for (int j = 0; j < polygonNodes.size() + 1; j++) {
						if (polygonFull.get(i).intValue() == polygonNodes.get(j).getId()) {
							clusterPolygon.getPoints().add(polygonFull.get(i));
						}
					}
				}
			}
			clusterPolygon.setComplete(true);
		}
	}

	// TODO incomplete lotization methods
	public Object lotize(List<Integer> list, int direction, int beginning) {
		if (beginning >= list.size()) {
			return 0;
		}

		int seed = 0;
		boolean lotizable = true, notUniform = false;
		int[] currentXY = ClusterMapHelper.breakKey(list.get(beginning));
		int[] finalXY = ClusterMapHelper.breakKey(list.get((beginning + 1) % list.size()));
		Double gradient = (currentXY[1] - finalXY[1]) * 1.0 / (currentXY[0] - finalXY[0]);

		if (direction == ClusterConstants.EAST || direction == ClusterConstants.WEST) {
			if (gradient.doubleValue() == 0.0) {
				currentXY[0] = direction == ClusterConstants.EAST ? currentXY[0] + 1 : currentXY[0];
				ClusterBuilding clusterBuilding = createWalkRoute(currentXY, false, direction, beginning);
				if (clusterBuilding != null) {
					currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, ClusterConfiguration.WALK_BRANCH_SIZE,
							direction);
				}

				finalXY[0] = direction == ClusterConstants.EAST ? finalXY[0] : finalXY[0] + 1;
				clusterBuilding = createWalkRoute(finalXY, true, direction, beginning);
				if (clusterBuilding != null) {
					finalXY = ClusterMapHelper.moveKeyByOffsetAndDirection(finalXY, ClusterConfiguration.WALK_BRANCH_SIZE,
							ClusterDirectionHelper.oppositeDirection(direction));
				}
			} else {
				notUniform = true;
				System.out.println("Non orthogonal east/west walk detected");
			}
		} else if (direction == ClusterConstants.SOUTH || direction == ClusterConstants.NORTH) {
			if (gradient.isInfinite()) {// means it is a route connection and a
										// perfect one at it
				createClusterEntrance(currentXY, finalXY, direction);
			} else {
				System.out.println("Non orthogonal south/north walk detected");
				notUniform = true;
			}
		}

		// TODO the not uniform type of polygon will be develop next
		if (notUniform) {
			return -1;
		}

		while (true) {
			boolean done = false;
			if ((direction == ClusterConstants.EAST) || (direction == ClusterConstants.NORTH))
				done = currentXY[0] >= finalXY[0] && currentXY[1] >= finalXY[1];
			else
				done = currentXY[0] <= finalXY[0] && currentXY[1] <= finalXY[1];

			if (done) {
				switch (direction) {
				case ClusterConstants.EAST:
					return lotize(list, ClusterConstants.NORTH, ++beginning);
				case ClusterConstants.NORTH:
					return lotize(list, ClusterConstants.WEST, ++beginning);
				case ClusterConstants.WEST:
					return lotize(list, ClusterConstants.SOUTH, ++beginning);
				case ClusterConstants.SOUTH:
					return 0;
				}
			}

			lotizable = canBeLotized(currentXY, ClusterConfiguration.HOUSE_SIDE_MINIMUN_SIZE,
					ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2, direction);
			if (lotizable) {
				createDoubleLot(currentXY, ClusterConfiguration.HOUSE_SIDE_MINIMUN_SIZE,
						ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE, direction, seed % 10);
				currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY,
						ClusterConfiguration.HOUSE_SIDE_MINIMUN_SIZE, direction);
				seed += 2;
			} else {
				currentXY = ClusterMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, direction);
			}
		}
	}

	private void createClusterEntrance(int[] currentXY, int[] finalXY, int direction) {
		// NORTH then it goes toward x+
		// SOUTH toward x-
		// given that x is the same, y is our indicator for the middle
		int upperMiddle[] = new int[2];
		upperMiddle[0] = currentXY[0];
		upperMiddle[1] = ((currentXY[1] + finalXY[1]) / 2) + (ClusterConfiguration.CLUSTER_ENTRANCE_SIZE) / 2;

		int lowerMiddle[] = new int[2];
		lowerMiddle[0] = currentXY[0];
		lowerMiddle[1] = ((currentXY[1] + finalXY[1]) / 2) - (ClusterConfiguration.CLUSTER_ENTRANCE_SIZE) / 2;

		if ((direction == ClusterConstants.NORTH) && (lowerMiddle[1] < currentXY[1] || upperMiddle[1] > finalXY[1]))
			return;
		else if ((direction == ClusterConstants.SOUTH) && (lowerMiddle[1] > currentXY[1] || upperMiddle[1] < finalXY[1]))
			return;
		createInsideClusterRoute(upperMiddle, ClusterMapHelper.formKey(lowerMiddle[0], lowerMiddle[1]), direction,
				ClusterConfiguration.WALK_BRANCH, ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2,
				ClusterConfiguration.CLUSTER_ENTRANCE_MARK);
	}

	private ClusterBuilding createWalkRoute(int[] currentXY, boolean isInverse, int direction, int rotation) {
		if (isInverse) {
			return createInsideClusterRoute(currentXY,
					ClusterMapHelper.moveKeyByOffsetAndDirection(ClusterMapHelper.formKey(currentXY[0], currentXY[1]),
							ClusterConfiguration.WALK_BRANCH_SIZE, ClusterDirectionHelper.oppositeDirection(direction)),
					direction, ClusterConfiguration.WALK_BRANCH, ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2,
					ClusterConfiguration.WALK_MARK);
		} else {
			return createInsideClusterRoute(currentXY,
					ClusterMapHelper.moveKeyByOffsetAndDirection(ClusterMapHelper.formKey(currentXY[0], currentXY[1]),
							ClusterConfiguration.WALK_BRANCH_SIZE, direction),
					direction, ClusterConfiguration.WALK_BRANCH, ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2,
					ClusterConfiguration.WALK_MARK);
		}
	}

	private boolean canBeLotized(int[] currentXY, int houseSideSize, int doublehouseDepthSize, int direction) {
		switch (direction) {
		case ClusterConstants.EAST:
			for (int i = currentXY[0]; i < currentXY[0] + houseSideSize; i++) {
				for (int j = currentXY[1]; j > currentXY[1] - doublehouseDepthSize; j--) {
					if (landPointisOnMap(ClusterMapHelper.formKey(i, j))) {
						String type = findPoint(ClusterMapHelper.formKey(i, j)).getType();
						if (!type.equals(ClusterConfiguration.EMPTY_MARK) && !type.equals("e")) {
							return false;
						}
					} else {
						return false;
					}
				}
			}
			break;
		case ClusterConstants.NORTH:
			for (int i = currentXY[1]; i < currentXY[1] + houseSideSize; i++) {
				for (int j = currentXY[0]; j < currentXY[0] + doublehouseDepthSize; j++) {
					if (landPointisOnMap(ClusterMapHelper.formKey(j, i))) {
						String type = findPoint(ClusterMapHelper.formKey(j, i)).getType();
						if (!type.equals(ClusterConfiguration.EMPTY_MARK) && !type.equals("e")) {
							return false;
						}
					} else {
						return false;
					}
				}
			}
			break;
		case ClusterConstants.WEST:
			for (int i = currentXY[0]; i >= currentXY[0] - houseSideSize; i--) {
				for (int j = currentXY[1]; j < currentXY[1] + doublehouseDepthSize; j++) {
					if (landPointisOnMap(ClusterMapHelper.formKey(i, j))) {
						String type = findPoint(ClusterMapHelper.formKey(i, j)).getType();
						if (!type.equals(ClusterConfiguration.EMPTY_MARK) && !type.equals("e")) {
							return false;
						}
					} else {
						return false;
					}
				}
			}
			break;
		case ClusterConstants.SOUTH:
			for (int i = currentXY[1]; i >= currentXY[1] - houseSideSize; i--) {
				for (int j = currentXY[0]; j > currentXY[0] - doublehouseDepthSize; j--) {
					if (landPointisOnMap(ClusterMapHelper.formKey(j, i))) {
						String type = findPoint(ClusterMapHelper.formKey(j, i)).getType();
						if (!type.equals(ClusterConfiguration.EMPTY_MARK) && !type.equals("e")) {
							return false;
						}
					} else {
						return false;
					}
				}
			}
			break;
		}
		return true;
	}

	private void createDoubleLot(int[] currentXY, int houseSideMinimunSize, int houseDepthMinimunSize, int direction,
			int serialNumber) {
		switch (direction) {
		case ClusterConstants.EAST:
			for (int i = currentXY[0]; i < currentXY[0] + houseSideMinimunSize; i++) {
				for (int j = currentXY[1]; j > currentXY[1] - houseDepthMinimunSize; j--) {
					findPoint(ClusterMapHelper.formKey(i, j)).setType("" + serialNumber);
				}
				for (int j = currentXY[1] - (houseDepthMinimunSize + 1); j > currentXY[1]
						- 2 * houseDepthMinimunSize; j--) {
					findPoint(ClusterMapHelper.formKey(i, j)).setType("" + (serialNumber + 1));
				}
			}
			break;
		case ClusterConstants.NORTH:
			for (int i = currentXY[1]; i < currentXY[1] + houseSideMinimunSize; i++) {
				for (int j = currentXY[0]; j < currentXY[0] + houseDepthMinimunSize; j++) {
					findPoint(ClusterMapHelper.formKey(j, i)).setType("" + serialNumber);
				}
				for (int j = currentXY[0] + houseDepthMinimunSize + 1; j < currentXY[0]
						+ 2 * houseDepthMinimunSize; j++) {
					findPoint(ClusterMapHelper.formKey(j, i)).setType("" + (serialNumber + 1));
				}
			}
			break;
		case ClusterConstants.WEST:
			for (int i = currentXY[0]; i >= currentXY[0] - houseSideMinimunSize; i--) {
				for (int j = currentXY[1]; j < currentXY[1] + houseDepthMinimunSize; j++) {
					findPoint(ClusterMapHelper.formKey(i, j)).setType("" + serialNumber);
				}
				for (int j = currentXY[1] + houseDepthMinimunSize + 1; j < currentXY[1]
						+ 2 * houseDepthMinimunSize; j++) {
					findPoint(ClusterMapHelper.formKey(i, j)).setType("" + (serialNumber + 1));
				}
			}
			break;
		case ClusterConstants.SOUTH:
			for (int i = currentXY[1]; i >= currentXY[1] - houseSideMinimunSize; i--) {
				for (int j = currentXY[0]; j > currentXY[0] - houseDepthMinimunSize; j--) {
					findPoint(ClusterMapHelper.formKey(j, i)).setType("" + serialNumber);
				}
				for (int j = currentXY[0] - (houseDepthMinimunSize + 1); j > currentXY[0]
						- 2 * houseDepthMinimunSize; j--) {
					findPoint(ClusterMapHelper.formKey(j, i)).setType("" + (serialNumber + 1));
				}
			}
			break;
		}
	}

	private ClusterBuilding createInsideClusterRoute(int[] currentXY, int finalKey, int direction, int type, int depth,
			String markType) {
		int[] finalXY = ClusterMapHelper.breakKey(finalKey);
		int lower, upper;
		ClusterBuilding clusterBuilding = new ClusterBuilding();

		if ((direction == ClusterConstants.NORTH) || (direction == ClusterConstants.SOUTH)) {
			if (currentXY[1] > finalXY[1]) {
				lower = finalXY[1];
				upper = currentXY[1];
			} else {
				lower = currentXY[1];
				upper = finalXY[1];
			}

			if (direction == ClusterConstants.SOUTH) {
				for (int i = lower; i < upper; i++) {
					for (int j = currentXY[0]; j > currentXY[0] - depth; j--) {
						findPoint(ClusterMapHelper.formKey(j, i)).setType(markType);
					}
				}
			} else {
				for (int i = lower; i < upper; i++) {
					for (int j = currentXY[0]; j < currentXY[0] + depth; j++) {
						findPoint(ClusterMapHelper.formKey(j, i)).setType(markType);
					}
				}
			}
		} else if ((direction == ClusterConstants.EAST) || (direction == ClusterConstants.WEST)) {
			if (currentXY[0] > finalXY[0]) {
				lower = finalXY[0];
				upper = currentXY[0];
			} else {
				lower = currentXY[0];
				upper = finalXY[0];
			}

			if (direction == ClusterConstants.EAST) {
				for (int i = lower; i < upper; i++) {
					for (int j = currentXY[1]; j > currentXY[1] - depth; j--) {
						findPoint(ClusterMapHelper.formKey(i, j)).setType(markType);
					}
				}
			} else {
				for (int i = lower; i < upper; i++) {
					for (int j = currentXY[1]; j < currentXY[1] + depth; j++) {
						findPoint(ClusterMapHelper.formKey(i, j)).setType(markType);
					}
				}
			}
		}

		clusterBuilding.setType(markType);
		clusterBuilding.setNumber(0);
		return clusterBuilding;
	}
	
	public String stringify() {
		String mapString = "";
		for (int j = pointsy - 1; j >= 0; j--) {
			String type = getLandPoint(ClusterMapHelper.formKey(0, j)).getType();
			int repetitions = 1;
			for (int i = 1; i < pointsx; i++) {
				if (type.equals(getLandPoint(ClusterMapHelper.formKey(i, j)).getType())) {
					repetitions++;
				} else {
					mapString += type + "-" + repetitions + ",";
					repetitions = 1;
					type = getLandPoint(ClusterMapHelper.formKey(i, j)).getType();
				}
			}
			mapString += type + "-" + repetitions + ",";
			mapString += "\n";
		}
		return mapString;
	}
	
}