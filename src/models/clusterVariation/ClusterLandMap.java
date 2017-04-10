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

	private Map<Integer, ClusterLandPoint> map;

	private ClusterLandRoute landRoute;
	private List<Integer> nodes = new ArrayList<>();
	List<List<Integer>> fullPolygon;
	private List<ClusterLandPoint> polygonNodes;
	private double polygonalArea;

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

	public ClusterLandPoint getCentroid() {
		return centroid;
	}

	public void setCentroid(ClusterLandPoint centroid) {
		this.centroid = centroid;
	}

	public ClusterLandPoint findPoint(int entryPointId) {
		return map.get(entryPointId);
	}

	public void setLandRoute(ClusterLandRoute landRoute) {
		this.landRoute = landRoute;
	}

	public ClusterLandRoute getLandRoute() {
		return landRoute;
	}

	/**
	 * This method marks all points that are not inside the polygon border as
	 * restricted area. This must be an ordered set of consecutive points (after
	 * all the input from android looks like that.
	 */
	public void createBorderFromPolygon(List<ClusterLandPoint> polygon) {
		setPolygonNodes(polygon);
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
		findPolygonalArea(polygon);
		findCentroid(polygon);
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

	public List<Integer> getNodes() {
		return nodes;
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

	public double getPolygonalArea() {
		return polygonalArea;
	}

	public void setPolygonalArea(double polygonalArea) {
		this.polygonalArea = polygonalArea;
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

	public boolean intersectMainRoute(int entryPointId) {
		int initialPoint = landRoute.getInitialPointId();
		int finalPoint = landRoute.getFinalPointId();

		int[] initialXY = ClusterMapHelper.breakKey(initialPoint);
		int[] finalXY = ClusterMapHelper.breakKey(finalPoint);
		int[] entryXY = ClusterMapHelper.breakKey(entryPointId);

		return (initialXY[0] < entryXY[0] && entryXY[0] < finalXY[0])
				|| ((initialXY[1] < entryXY[1] && entryXY[1] < finalXY[1]));
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

	public ClusterPolygon joinWithPolygonalBorder(ClusterPolygon clusterPolygon) {
		int initialVertex = clusterPolygon.getPoints().get(0);
		int finalVertex = clusterPolygon.getPoints().get(clusterPolygon.getPoints().size() - 1);
		int initialVertexSide = -1;
		int finalVertexSide = -1;

		for (int x = 0; x < fullPolygon.size(); x++) {
			for (int i = 0; i < fullPolygon.get(x).size(); i++) {
				// System.out.print(fullPolygon.get(x).get(i) + ",");

				if ((fullPolygon.get(x).get(i) == initialVertex) && (initialVertexSide == -1)) {
					initialVertexSide = x;
				}

				if ((fullPolygon.get(x).get(i) == finalVertex) && (finalVertexSide == -1)) {
					finalVertexSide = x;
				}

				if ((initialVertexSide != -1) && (finalVertexSide != -1))
					break;
			}
			if ((initialVertexSide != -1) && (finalVertexSide != -1))
				break;
		}
		// they both are on the same line. Meaning it is a triangle
		if (initialVertexSide == finalVertexSide) {
			clusterPolygon.setComplete(true);
			return clusterPolygon;
		}
		// System.out.println("Vertexes " + initialVertexSide + "||" +
		// finalVertexSide);

		if (initialVertexSide != finalVertexSide) {
			// simple complex figure
			// This polygons should be treated as if another procedure is
			// missing (going up from initial point
			// before trying again this strategy
			if ((initialVertexSide == -1 && finalVertexSide != -1)
					|| (initialVertexSide != -1 && finalVertexSide == -1)) {
				// System.out.println("Incomplete data polygon");
				clusterPolygon.printPolygon();
				return clusterPolygon;
			}

			int initialVertex0 = fullPolygon.get(initialVertexSide).get(0);
			int initialVertexFinal = fullPolygon.get(initialVertexSide)
					.get(fullPolygon.get(initialVertexSide).size() - 1);
			int finalVertex0 = fullPolygon.get(finalVertexSide).get(0);
			int finalVertexFinal = fullPolygon.get(finalVertexSide).get(fullPolygon.get(finalVertexSide).size() - 1);

			if (initialVertex0 == finalVertex0) {
				clusterPolygon.getPoints().add(initialVertex0);
				clusterPolygon.setComplete(true);
				// System.out.println("Complete data polygon");
				clusterPolygon.printPolygon();
				return clusterPolygon;
			} else if (initialVertex0 == finalVertexFinal) {
				clusterPolygon.getPoints().add(initialVertex0);
				clusterPolygon.setComplete(true);
				// System.out.println("Complete data polygon");
				clusterPolygon.printPolygon();
				return clusterPolygon;
			} else if (initialVertexFinal == finalVertex0) {
				clusterPolygon.getPoints().add(initialVertexFinal);
				clusterPolygon.setComplete(true);
				// System.out.println("Complete data polygon");
				clusterPolygon.printPolygon();
				return clusterPolygon;
			} else if (initialVertexFinal == finalVertexFinal) {
				clusterPolygon.getPoints().add(initialVertexFinal);
				clusterPolygon.setComplete(true);
				// System.out.println("Complete data polygon");
				clusterPolygon.printPolygon();
				return clusterPolygon;
			}
		}
		return clusterPolygon;
	}

	public String stringify() {
		String mapString = "";

		for (int i = 0; i < pointsx; i++) {
			String type = getLandPoint(ClusterMapHelper.formKey(i, 0)).getType();
			int repetitions = 1;
			for (int j = 0; j < pointsy; j++) {
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

	public void impreciseLotization(ClusterPolygon clusterPolygon) {
		if(!clusterPolygon.canBelotized()){
			return;
		}
		
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
				System.out.println("Non orthogonal detected");
				continue;
			}

			int[] currentXY = initialXY;
			int seed = 0;
			while (currentXY[0] != finalXY[0] || currentXY[1] != finalXY[1]) {
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
		// TODO we create an inner route and try to create a new polygon to
		// take hold of the side
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
				System.out.println("Non orthogonal detected");
				continue;
			}
			innerRoad(initialXY, finalXY, driveDirection, growDirection);
		}

		reducedPoints = clusterPolygon.translateTowardCenter(
				ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE + ClusterConfiguration.LOCAL_BRANCH_SIZE);
		if(reducedPoints.size()!=0){
			ClusterPolygon innerPolygon = new ClusterPolygon();
			innerPolygon.setPoints(reducedPoints);
			innerPolygon.setComplete(true);
			impreciseLotization(innerPolygon);
		}
	}

	private void innerRoad(int[] initialXY, int[] finalXY, int driveDirection, int growDirection) {
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
						&& !type.equals(ClusterConfiguration.LOCAL_MARK)
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

	public void preciseLotization(ClusterPolygon clusterPolygon) {
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
			} else if (initialXY[1] == finalXY[1]) {
				growDirection = ClusterDirectionHelper.perpendicularDirection(initialXY, clusterPolygon.getCentroid(),
						ClusterConstants.EAST);
				driveDirection = initialXY[0] < finalXY[0] ? ClusterConstants.EAST : ClusterConstants.WEST;
				createWalkRoute(initialXY, finalXY, driveDirection, growDirection);
			} else {
				System.out.println("Non orthogonal detected");
				continue;
			}

			int[] currentXY = initialXY;
			int seed = 0;
			while (currentXY[0] != finalXY[0] || currentXY[1] != finalXY[1]) {
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
}