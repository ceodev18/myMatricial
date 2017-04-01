package models.spineVariation;

import java.awt.Polygon;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import algorithm.spineVariation.LSystemSpineAlgorithm;
import helpers.base.MapHelper;
import helpers.clusterVariation.ClusterMapHelper;
import helpers.spineVariation.SpineDirectionHelper;
import helpers.spineVariation.SpineMapHelper;
import interfaces.spineVariation.SpineConfiguration;
import interfaces.spineVariation.SpineConfiguration;
import interfaces.spineVariation.SpineConstants;
import models.spineVariation.SpineBuilding;
import models.spineVariation.SpineLandPoint;
import models.spineVariation.SpineLandRoute;
import models.spineVariation.SpinePolygon;
import models.spineVariation.SpineLandPoint;
import models.spineVariation.SpineLandRoute;

public class SpineLandMap {
	private int pointsx = -1;
	private int pointsy = -1;

	private SpineLandPoint centroid;

	private Map<Integer, SpineLandPoint> map;

	private SpineLandRoute landRoute;
	private List<Integer> nodes = new ArrayList<>();
	List<List<Integer>> fullPolygon;
	private List<SpineLandPoint> polygonNodes;
	private double polygonalArea;

	public SpineLandMap(int pointsx, int pointsy) {
		this.setPointsx(++pointsx);
		this.setPointsy(++pointsy);

		map = new HashMap<>();
		for (int i = 0; i < pointsx; i++) {
			for (int j = 0; j < pointsy; j++) {
				map.put(SpineMapHelper.formKey(i, j), new SpineLandPoint(i, j));
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

	public SpineLandPoint getLandPoint(int pointId) {
		return map.get(pointId);
	}

	public SpineLandPoint getCentroid() {
		return centroid;
	}

	public void setCentroid(SpineLandPoint centroid) {
		this.centroid = centroid;
	}

	public SpineLandPoint findPoint(int entryPointId) {
		return map.get(entryPointId);
	}

	public void setLandRoute(SpineLandRoute landRoute) {
		this.landRoute = landRoute;
	}

	public SpineLandRoute getLandRoute() {
		return landRoute;
	}

	/**
	 * This method marks all points that are not inside the polygon border as
	 * restricted area. This must be an ordered set of consecutive points (after
	 * all the input from android looks like that.
	 */
	public void createBorderFromPolygon(List<SpineLandPoint> polygon) {
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
					getLandPoint(SpineMapHelper.formKey(polygon.get(i).getX(), w))
							.setType(SpineConstants.POLYGON_LIMIT);
					truePolygon.add(SpineMapHelper.formKey(polygon.get(i).getX(), w));
				}
				continue;
			}

			double gradient = (polygon.get(j).getY() - polygon.get(i).getY()) * 1.0 / underscore;
			// 2nd, gradient=0; straight in the X axis
			int lower = polygon.get(i).getX() < polygon.get(j).getX() ? polygon.get(i).getX() : polygon.get(j).getX();
			int upper = polygon.get(i).getX() > polygon.get(j).getX() ? polygon.get(i).getX() : polygon.get(j).getX();
			if (gradient == 0) {
				for (int w = lower; w <= upper; w++) {
					getLandPoint(SpineMapHelper.formKey(w, polygon.get(i).getY()))
							.setType(SpineConstants.POLYGON_LIMIT);
					truePolygon.add(SpineMapHelper.formKey(w, polygon.get(i).getY()));
				}
				continue;
			}

			double b = polygon.get(j).getY() - gradient * polygon.get(j).getX();
			// 3nd the gradient is positive/negative.
			for (int w = lower; w <= upper; w++) {
				float y = SpineMapHelper.round(gradient * w + b);
				if (y == (int) y) // quick and dirty convertion check
				{
					getLandPoint(SpineMapHelper.formKey(w, (int) y)).setType(SpineConstants.POLYGON_LIMIT);
					truePolygon.add(SpineMapHelper.formKey(w, (int) y));
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
				SpineLandPoint initialLandPoint = getLandPoint(fullPolygon.get(x).get(y));
				polygonRow.add(fullPolygon.get(x).get(y));
				SpineLandPoint finalLandPoint = getLandPoint(fullPolygon.get(x).get(y + 1));

				int wi = initialLandPoint.getX();
				int wf = finalLandPoint.getX();

				if (initialLandPoint.getY() > finalLandPoint.getY()) {
					for (int z = initialLandPoint.getY() - 1; z >= finalLandPoint.getY() + 1; z--) {
						if (wi != wf) {
							if (isPolygonBorder(wi, z)) {
								polygonRow.add(MapHelper.formKey(wi, z));
							}
						}

						if (isPolygonBorder(wf, z)) {
							polygonRow.add(MapHelper.formKey(wf, z));
						}
					}
				} else {
					for (int z = initialLandPoint.getY() + 1; z <= finalLandPoint.getY() - 1; z++) {
						if (wi != wf) {
							if (isPolygonBorder(wi, z)) {
								polygonRow.add(MapHelper.formKey(wi, z));
							}
						}
						if (isPolygonBorder(wf, z)) {
							polygonRow.add(MapHelper.formKey(wf, z));
						}
					}
				}
			}
			polygonRow.add(fullPolygon.get(x).get(fullPolygon.get(x).size() - 1));
			fullPolygon.set(x, polygonRow);
		}
	}

	private boolean isPolygonBorder(int x, int y) {
		int easternLimit = x+1;
		int westernLimit = x-1;
		int southLimit = y-1;
		int northLimit = y+1;
		
		if(westernLimit == -1 || easternLimit ==pointsx || southLimit==-1 || northLimit==pointsy)return false;
		return (findPoint(MapHelper.formKey(x - 1, y)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
				|| findPoint(MapHelper.formKey(x + 1, y)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
				&& !findPoint(MapHelper.formKey(x, y)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK);
	}

	private void fillPolygonalArea() {
		for (int x = 0; x < pointsx; x++) {
			int count = 0;
			int pInitialLimit = -1;
			boolean reversed = false;

			for (int y = 0; y < pointsy; y++) {
				if (getLandPoint(SpineMapHelper.formKey(x, y)).getType() == SpineConstants.POLYGON_LIMIT) {
					count++;
					pInitialLimit = pInitialLimit == -1 ? y : pInitialLimit;
				}

				switch (count) {
				case 0:
					getLandPoint(SpineMapHelper.formKey(x, y)).setType(SpineConfiguration.OUTSIDE_POLYGON_MARK);
					break;
				case 2:
					if (!reversed) {
						for (int w = pInitialLimit + 1; w < y; w++) {
							getLandPoint(SpineMapHelper.formKey(x, w)).setType(SpineConfiguration.EMPTY_MARK);
						}
						reversed = true;
					} else {
						getLandPoint(SpineMapHelper.formKey(x, y)).setType(SpineConfiguration.OUTSIDE_POLYGON_MARK);
					}
					break;
				}
			}
		}
	}

	// Variation for the creation of zones
	public void createBorderFromPolygon(List<Integer> polygon, String markType) {
		for (int i = 0; i < polygon.size(); i++) {
			int xyInitial[] = SpineMapHelper.breakKey(polygon.get(i));
			int xyFinal[] = SpineMapHelper.breakKey(polygon.get((i + 1) % polygon.size()));

			int underscore = (xyFinal[0] - xyInitial[0]);
			if (underscore == 0) {
				int lower = xyInitial[1] < xyFinal[1] ? xyInitial[1] : xyFinal[1];
				int upper = xyInitial[1] > xyFinal[1] ? xyInitial[1] : xyFinal[1];

				for (int w = lower; w <= upper; w++) {
					getLandPoint(SpineMapHelper.formKey(xyInitial[0], w)).setType(markType);
				}
				continue;
			}

			double gradient = (xyFinal[1] - xyInitial[1]) * 1.0 / underscore;
			// 2nd, gradient=0; straight in the X axis
			int lower = xyInitial[0] < xyFinal[0] ? xyInitial[0] : xyFinal[0];
			int upper = xyInitial[0] > xyFinal[0] ? xyInitial[0] : xyFinal[0];
			if (gradient == 0) {
				for (int w = lower; w <= upper; w++) {
					getLandPoint(SpineMapHelper.formKey(w, xyInitial[1])).setType(markType);
				}
				continue;
			}

			double b = xyFinal[1] - gradient * xyFinal[0];
			// 3nd the gradient is positive/negative.
			for (int w = lower; w <= upper; w++) {
				float y = SpineMapHelper.round(gradient * w + b);
				if (y == (int) y) // quick and dirty convertion check
				{
					getLandPoint(SpineMapHelper.formKey(w, (int) y)).setType(markType);
				}
			}
		}
	}

	private void clearDottedLimits() {
		for (int x = 0; x < fullPolygon.size(); x++) {
			for (int i = 0; i < fullPolygon.get(x).size(); i++) {
				getLandPoint(fullPolygon.get(x).get(i)).setType(SpineConfiguration.EMPTY_MARK);
			}
		}
	}

	private void findPolygonalArea(List<SpineLandPoint> polygon) {
		int absoluteArea = 0;
		for (int i = 0; i < polygon.size(); i++) {
			absoluteArea += (polygon.get(i).getX() * polygon.get((i + 1) % polygon.size()).getY())
					- (polygon.get(i).getY() * polygon.get((i + 1) % polygon.size()).getX());
		}
		setPolygonalArea(Math.abs(absoluteArea) / 2);
	}

	private void findCentroid(List<SpineLandPoint> polygon) {
		this.setCentroid(new SpineLandPoint(pointsx / 2, pointsy / 2));
	}

	public void printMapToFile() {
		try {
			PrintWriter writer = new PrintWriter("printed-map.txt", "UTF-8");
			for (int j = pointsy - 1; j >= 0; j--) {
				for (int i = 0; i < pointsx; i++) {
					writer.print(getLandPoint(SpineMapHelper.formKey(i, j)).getType());
				}
				writer.println();
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void printMapToFileCentroide(int ejex,int ejey) {
		try {
			PrintWriter writer = new PrintWriter("printed-map.txt", "UTF-8");
			for (int j = pointsy - 1; j >= 0; j--) {
				for (int i = 0; i < pointsx; i++) {
					//getLandPoint(SpineMapHelper.formKey(i, j)).getType()
					if(i==ejex && j==ejey)writer.print("x");
					else writer.print(getLandPoint(SpineMapHelper.formKey(i, j)).getType());
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
		case SpineConfiguration.ARTERIAL_BRANCH:
			variation = SpineConfiguration.ARTERIAL_MARK;
			break;
		case SpineConfiguration.COLLECTOR_BRANCH:
			variation = SpineConfiguration.COLLECTOR_MARK;
			break;
		case SpineConfiguration.LOCAL_BRANCH:
			variation = SpineConfiguration.LOCAL_MARK;
			break;
		case SpineConfiguration.NODE:
			variation = SpineConfiguration.NODE_MARK;
			break;
		}
		map.get(entryPointId).setType(variation);
		map.get(entryPointId).setNodeType(nodeType);
	}

	public boolean landPointisOnMap(int pointId) {
		int[] xy = SpineMapHelper.breakKey(pointId);
		return xy[0] < pointsx && xy[0] > 0 && xy[1] < pointsy && xy[1] > 0;
	}

	public List<Integer> getNodes() {
		return nodes;
	}

	public void setNodes(List<Integer> nodes) {
		this.nodes = nodes;
	}

	public List<SpineLandPoint> getPolygonNodes() {
		return polygonNodes;
	}

	public void setPolygonNodes(List<SpineLandPoint> polygonNodes) {
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
		if (!findPoint(SpineMapHelper.formKey(x, y)).getType().equals(SpineConfiguration.NODE_MARK)) {
			return false;
		}

		if ((y + 1 != pointsy) && (y - 1 != -1) && (x - 1 != -1) && (x + 1 != pointsx)) {
			if (findPoint(SpineMapHelper.formKey(x, y + 1)).getType().equals(SpineConfiguration.NODE_MARK)
					&& findPoint(SpineMapHelper.formKey(x, y - 1)).getType()
							.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
					&& findPoint(SpineMapHelper.formKey(x + 1, y)).getType().equals(SpineConfiguration.EMPTY_MARK)
					&& (!findPoint(SpineMapHelper.formKey(x - 1, y)).getType().equals(SpineConfiguration.EMPTY_MARK)
							|| !findPoint(SpineMapHelper.formKey(x + 1, y)).getType()
									.equals(SpineConfiguration.NODE_MARK))) {
				return true;
			}
		}

		return false;
	}

	public boolean intersectMainRoute(int entryPointId) {
		int initialPoint = landRoute.getInitialPointId();
		int finalPoint = landRoute.getFinalPointId();

		int[] initialXY = SpineMapHelper.breakKey(initialPoint);
		int[] finalXY = SpineMapHelper.breakKey(finalPoint);
		int[] entryXY = SpineMapHelper.breakKey(entryPointId);

		return (initialXY[0] < entryXY[0] && entryXY[0] < finalXY[0])
				|| ((initialXY[1] < entryXY[1] && entryXY[1] < finalXY[1]));
	}

	public boolean isNormalNode(int x, int y) {
		// up,down,left,right
		if (!findPoint(SpineMapHelper.formKey(x, y)).getType().equals(SpineConfiguration.NODE_MARK)) {
			return false;
		}
		int node = 0;
		boolean[] nodeInBorder = new boolean[] { false, false, false, false };
		int outside = 0;
		if (y + 1 != pointsy) {
			if (findPoint(SpineMapHelper.formKey(x, y + 1)).getType().equals(SpineConfiguration.NODE_MARK)) {
				node++;
				nodeInBorder[0] = true;
			}

			if (findPoint(SpineMapHelper.formKey(x, y + 1)).getType()
					.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if (y - 1 != -1) {
			if (findPoint(SpineMapHelper.formKey(x, y - 1)).getType().equals(SpineConfiguration.NODE_MARK)) {
				node++;
				nodeInBorder[1] = true;
			}

			if (findPoint(SpineMapHelper.formKey(x, y - 1)).getType()
					.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if (x - 1 != -1) {
			if (findPoint(SpineMapHelper.formKey(x - 1, y)).getType().equals(SpineConfiguration.NODE_MARK)) {
				node++;
				nodeInBorder[2] = true;
			}

			if (findPoint(SpineMapHelper.formKey(x - 1, y)).getType()
					.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if (x + 1 != pointsx) {
			if (findPoint(SpineMapHelper.formKey(x + 1, y)).getType().equals(SpineConfiguration.NODE_MARK)) {
				node++;
				nodeInBorder[3] = true;
			}

			if (findPoint(SpineMapHelper.formKey(x + 1, y)).getType()
					.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if ((y + 1 != pointsy) && (y - 1 != -1) && (x - 1 != -1) && (x + 1 != pointsx)) {
			if ((findPoint(SpineMapHelper.formKey(x, y - 1)).getType().equals(SpineConfiguration.ARTERIAL_MARK)
					|| findPoint(SpineMapHelper.formKey(x, y - 1)).getType().equals(SpineConfiguration.LOCAL_MARK))
					&& findPoint(SpineMapHelper.formKey(x - 1, y)).getType()
							.equals(SpineConfiguration.COLLECTOR_MARK)
					&& findPoint(SpineMapHelper.formKey(x + 1, y)).getType().equals(SpineConfiguration.NODE_MARK)
					&& findPoint(SpineMapHelper.formKey(x, y + 1)).getType().equals(SpineConfiguration.NODE_MARK)) {
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

	public SpinePolygon joinWithPolygonalBorder(SpinePolygon spinePolygon) {
		int initialVertex = spinePolygon.getPoints().get(0);
		int finalVertex = spinePolygon.getPoints().get(spinePolygon.getPoints().size() - 1);
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
			spinePolygon.setComplete(true);
			return spinePolygon;
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
				spinePolygon.printPolygon();
				return spinePolygon;
			}

			int initialVertex0 = fullPolygon.get(initialVertexSide).get(0);
			int initialVertexFinal = fullPolygon.get(initialVertexSide)
					.get(fullPolygon.get(initialVertexSide).size() - 1);
			int finalVertex0 = fullPolygon.get(finalVertexSide).get(0);
			int finalVertexFinal = fullPolygon.get(finalVertexSide).get(fullPolygon.get(finalVertexSide).size() - 1);

			if (initialVertex0 == finalVertex0) {
				spinePolygon.getPoints().add(initialVertex0);
				spinePolygon.setComplete(true);
				// System.out.println("Complete data polygon");
				spinePolygon.printPolygon();
				return spinePolygon;
			} else if (initialVertex0 == finalVertexFinal) {
				spinePolygon.getPoints().add(initialVertex0);
				spinePolygon.setComplete(true);
				// System.out.println("Complete data polygon");
				spinePolygon.printPolygon();
				return spinePolygon;
			} else if (initialVertexFinal == finalVertex0) {
				spinePolygon.getPoints().add(initialVertexFinal);
				spinePolygon.setComplete(true);
				// System.out.println("Complete data polygon");
				spinePolygon.printPolygon();
				return spinePolygon;
			} else if (initialVertexFinal == finalVertexFinal) {
				spinePolygon.getPoints().add(initialVertexFinal);
				spinePolygon.setComplete(true);
				// System.out.println("Complete data polygon");
				spinePolygon.printPolygon();
				return spinePolygon;
			}
			// System.out.println("Polygon with more than 4 sides");
		}
		return spinePolygon;
	}

	/**
	 * TODO not squared components Lotization procedure completed for all square
	 * like components
	 */
	public Object lotize(List<Integer> list, int direction, int beginning) {
		if (beginning >= list.size()) {
			return 0;
		}

		int seed = 0;
		boolean lotizable = true, notUniform = false;
		int[] currentXY = SpineMapHelper.breakKey(list.get(beginning));
		int[] finalXY = SpineMapHelper.breakKey(list.get((beginning + 1) % list.size()));
		Double gradient = (currentXY[1] - finalXY[1]) * 1.0 / (currentXY[0] - finalXY[0]);
		double offset = finalXY[1] - gradient * finalXY[0];
		if (direction == SpineConstants.EAST || direction == SpineConstants.WEST) {
			if (gradient.doubleValue() == 0.0) {
				currentXY[0] = direction == SpineConstants.EAST ? currentXY[0] + 1 : currentXY[0];
				SpineBuilding clusterBuilding = createWalkRoute(currentXY, false, direction, beginning);
				if (clusterBuilding != null) {
					currentXY = SpineMapHelper.moveKeyByOffsetAndDirection(currentXY,
							SpineConfiguration.WALK_BRANCH_SIZE, direction);
				}

				finalXY[0] = direction == SpineConstants.EAST ? finalXY[0] : finalXY[0] + 1;
				clusterBuilding = createWalkRoute(finalXY, true, direction, beginning);
				if (clusterBuilding != null) {
					finalXY = SpineMapHelper.moveKeyByOffsetAndDirection(finalXY,
							SpineConfiguration.WALK_BRANCH_SIZE, SpineDirectionHelper.oppositeDirection(direction));
				}
			} else {
				notUniform = true;
				currentXY = createNonOrthogonalWalkRoute(list.get(beginning), list.get((beginning + 1) % list.size()),
						finalXY, false, gradient);

				finalXY = createNonOrthogonalWalkRoute(list.get(beginning), list.get((beginning + 1) % list.size()),
						finalXY, true, gradient);
			}
		} else if (direction == SpineConstants.SOUTH || direction == SpineConstants.NORTH) {
			if (gradient.isInfinite()) {
				// means it is a route connection and a perfect one at it in
				// this initial case is neccesary to find the intermediate point
				// - 6 to make
				createClusterEntrance(currentXY, finalXY, direction);
			} else {
				notUniform = true;
				createNonOrthogonalClusterEntrance(currentXY, finalXY, direction, gradient);
			}
		}

		while (true) {
			boolean done = false;
			if ((direction == SpineConstants.EAST) || (direction == SpineConstants.NORTH))
				done = currentXY[0] >= finalXY[0] && currentXY[1] >= finalXY[1];
			else
				done = currentXY[0] <= finalXY[0] && currentXY[1] <= finalXY[1];

			if (done) {
				if (notUniform) {
					int newDirection = SpineDirectionHelper.orthogonalDirectionFromPointToPoint(
							list.get((beginning + 1) % list.size()), list.get((beginning + 2) % list.size()));
					return lotize(list, newDirection, ++beginning);
				}
				switch (direction) {
				case SpineConstants.EAST:
					return lotize(list, SpineConstants.NORTH, ++beginning);
				case SpineConstants.NORTH:
					return lotize(list, SpineConstants.WEST, ++beginning);
				case SpineConstants.WEST:
					return lotize(list, SpineConstants.SOUTH, ++beginning);
				case SpineConstants.SOUTH:
					return 0;
				}
			}

			if (gradient.doubleValue() == 0.0 || gradient.isInfinite()) {
				lotizable = canBeLotized(currentXY, SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE,
						SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2, direction);
				if (lotizable) {
					createDoubleLot(currentXY, SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE,
							SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE, direction, seed % 4);
					currentXY = SpineMapHelper.moveKeyByOffsetAndDirection(currentXY,
							SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE, direction);
					seed += 2;
				} else {
					currentXY = SpineMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, direction);
				}
			} else {
				lotizable = canBeNonOrthogonallyLotized(currentXY, finalXY,
						SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE, SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE,
						direction, gradient);
				if (lotizable) {
					createNonOrthogonalLot(currentXY, finalXY, SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE,
							SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE, direction, gradient, seed % 4);
					currentXY = SpineMapHelper.moveKeyByGradientAndOffset(currentXY, finalXY,
							SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE, gradient, offset, direction);
					seed += 2;
				} else {
					currentXY = SpineMapHelper.moveKeyByGradientAndOffset(currentXY, finalXY, 1, gradient, offset,
							direction);
				}
			}
		}
	}

	private boolean canBeNonOrthogonallyLotized(int[] beginXY, int[] finalXY, int houseSideSize, int houseDepthSize,
			int direction, Double gradient) {
		int[] tbXY = new int[2];
		int[] tfXY = new int[2];
		double offset = -gradient * beginXY[0] + beginXY[1];
		double distance = Math.sqrt(0 + Math.pow(beginXY[1] - finalXY[1], 2));
		boolean inverse = false;
		boolean isUpDown = false;

		if (distance < houseSideSize) {
			return false;
		}

		if (beginXY[1] < finalXY[1]) {
			tbXY[1] = (int) beginXY[1];
			tbXY[0] = (int) ((tbXY[1] - offset) / gradient);

			tfXY[1] = (int) finalXY[1];
			tfXY[0] = (int) ((tfXY[1] - offset) / gradient);
		} else {
			isUpDown = true;
			tbXY[1] = (int) finalXY[1];
			tbXY[0] = (int) ((tbXY[1] - offset) / gradient);

			tfXY[1] = (int) beginXY[1];
			tfXY[0] = (int) ((tfXY[1] - offset) / gradient);
		}

		// THIS IS FOR FINDING WHERE DOES X POINT ALL THE TIME GIVEN POINTS
		// COORDINATES. DO NOT MOVE.
		if (finalXY[0] > beginXY[0]) {
			if (finalXY[1] > beginXY[1]) {
				inverse = false;
			} else {
				inverse = true;
			}
		} else {
			if (finalXY[1] > beginXY[1]) {
				inverse = false;
			} else {
				inverse = true;
			}
		}

		if (tbXY[1] > tfXY[1])
			return false;

		// double orthogonalGradient = -1 / gradient;
		// double orthogonalOffset = -orthogonalGradient * tbXY[0] + tbXY[1];
		double variation[] = new double[2];
		int[] currentXY = new int[2];
		int countYFactor = 0;
		int oldVariation = -1;

		for (int j = 0; j < houseSideSize; j++) {
			currentXY[0] = tbXY[0] + j;
			currentXY[1] = (int) (gradient * currentXY[0] + offset);
			// orthogonalOffset = -orthogonalGradient * currentXY[0] +
			// currentXY[1];

			if (isUpDown && (oldVariation != -1) && ((oldVariation + 1) < currentXY[1])) {
				// we take the reminder y that are needed for an exact answer
				for (int w = oldVariation + 1; w < currentXY[1]; w++) {
					for (int i = 0; i < houseDepthSize; i++) {
						variation[0] = currentXY[0] + (!inverse ? i : -i);
						variation[1] = w;
						// orthogonalGradient * variation[0] + orthogonalOffset;
						if (landPointisOnMap(MapHelper.formKey((int) variation[0], (int) variation[1]))) {
							String type = findPoint(MapHelper.formKey((int) variation[0], (int) variation[1]))
									.getType();
							if (type.equals(SpineConfiguration.CLUSTER_ENTRANCE_MARK))
								return false;
						}
					}
					countYFactor++;
					if (countYFactor == houseSideSize) {
						break;
					}
				}
			} else if ((oldVariation != -1) && ((oldVariation - 1) > currentXY[1])) {
				// we take the reminder y that are needed for an exact answer
				for (int w = currentXY[1] + 1; w < oldVariation; w++) {
					for (int i = 0; i < houseDepthSize; i++) {
						variation[0] = currentXY[0] + (!inverse ? i : -i);
						variation[1] = w;
						// orthogonalGradient * variation[0] + orthogonalOffset;
						if (landPointisOnMap(MapHelper.formKey((int) variation[0], (int) variation[1]))) {
							String type = findPoint(MapHelper.formKey((int) variation[0], (int) variation[1]))
									.getType();
							if (type.equals(SpineConfiguration.CLUSTER_ENTRANCE_MARK))
								return false;
						}
					}
					countYFactor++;
					if (countYFactor == houseSideSize) {
						break;
					}
				}
			}

			if (countYFactor == houseSideSize) {
				break;
			}

			// We need to find the furthest
			for (int i = 0; i < houseDepthSize; i++) {
				variation[0] = currentXY[0] + (!inverse ? i : -i);
				variation[1] = currentXY[1];
				// orthogonalGradient * variation[0] + orthogonalOffset;
				if (landPointisOnMap(MapHelper.formKey((int) variation[0], (int) variation[1]))) {
					String type = findPoint(MapHelper.formKey((int) variation[0], (int) variation[1])).getType();
					if (type.equals(SpineConfiguration.CLUSTER_ENTRANCE_MARK))
						return false;
				}
			}
			countYFactor++;
			if (countYFactor == houseSideSize) {
				break;
			}
			oldVariation = (int) currentXY[1];
		}
		return true;
	}

	private void createNonOrthogonalLot(int[] beginXY, int[] finalXY, int houseSideSize, int houseDepthSize,
			int direction, Double gradient, int seed) {
		int[] tbXY = new int[2];
		int[] tfXY = new int[2];
		double offset = -gradient * beginXY[0] + beginXY[1];
		double distance = Math.sqrt(0 + Math.pow(beginXY[1] - finalXY[1], 2));
		boolean inverse = false;
		boolean isUpDown = false;

		if (distance < houseSideSize) {
			return;
		}

		if (beginXY[1] < finalXY[1]) {
			tbXY[1] = (int) beginXY[1];
			tbXY[0] = (int) ((tbXY[1] - offset) / gradient);

			tfXY[1] = (int) finalXY[1];
			tfXY[0] = (int) ((tfXY[1] - offset) / gradient);
		} else {
			isUpDown = true;
			tbXY[1] = (int) finalXY[1];
			tbXY[0] = (int) ((tbXY[1] - offset) / gradient);

			tfXY[1] = (int) beginXY[1];
			tfXY[0] = (int) ((tfXY[1] - offset) / gradient);
		}

		// THIS IS FOR FINDING WHERE DOES X POINT ALL THE TIME GIVEN POINTS
		// COORDINATES. DO NOT MOVE.
		if (finalXY[0] > beginXY[0]) {
			if (finalXY[1] > beginXY[1]) {
				inverse = false;
			} else {
				inverse = true;
			}
		} else {
			if (finalXY[1] > beginXY[1]) {
				inverse = false;
			} else {
				inverse = true;
			}
		}

		if (tbXY[1] > tfXY[1])
			return;

		// double orthogonalGradient = -1 / gradient;
		// double orthogonalOffset = -orthogonalGradient * tbXY[0] + tbXY[1];
		double variation[] = new double[2];
		int[] currentXY = new int[2];
		int countYFactor = 0;
		int oldVariation = -1;

		for (int j = 0; j < houseSideSize; j++) {
			currentXY[0] = tbXY[0] + j;
			currentXY[1] = (int) (gradient * currentXY[0] + offset);
			// orthogonalOffset = -orthogonalGradient * currentXY[0] +
			// currentXY[1];

			if (isUpDown && (oldVariation != -1) && ((oldVariation + 1) < currentXY[1])) {
				// we take the reminder y that are needed for an exact answer
				for (int w = oldVariation + 1; w < currentXY[1]; w++) {
					for (int i = 0; i < houseDepthSize; i++) {
						variation[0] = currentXY[0] + (!inverse ? i : -i);
						variation[1] = w;
						// orthogonalGradient * variation[0] + orthogonalOffset;
						if (landPointisOnMap(MapHelper.formKey((int) variation[0], (int) variation[1]))) {
							findPoint(MapHelper.formKey((int) variation[0], (int) variation[1])).setType("" + seed);
						}
					}
					countYFactor++;
					if (countYFactor == houseSideSize) {
						break;
					}
				}
			} else if ((oldVariation != -1) && ((oldVariation - 1) > currentXY[1])) {
				// we take the reminder y that are needed for an exact answer
				for (int w = currentXY[1] + 1; w < oldVariation; w++) {
					for (int i = 0; i < houseDepthSize; i++) {
						variation[0] = currentXY[0] + (!inverse ? i : -i);
						variation[1] = w;
						// orthogonalGradient * variation[0] + orthogonalOffset;
						if (landPointisOnMap(MapHelper.formKey((int) variation[0], (int) variation[1]))) {
							findPoint(MapHelper.formKey((int) variation[0], (int) variation[1])).setType("" + seed);
						}
					}
					countYFactor++;
					if (countYFactor == houseSideSize) {
						break;
					}
				}
			}

			if (countYFactor == houseSideSize) {
				break;
			}

			// We need to find the furthest
			for (int i = 0; i < houseDepthSize; i++) {
				variation[0] = currentXY[0] + (!inverse ? i : -i);
				variation[1] = currentXY[1];
				// orthogonalGradient * variation[0] + orthogonalOffset;
				if (landPointisOnMap(MapHelper.formKey((int) variation[0], (int) variation[1]))) {
					findPoint(MapHelper.formKey((int) variation[0], (int) variation[1])).setType("" + seed);
				}
			}
			countYFactor++;
			if (countYFactor == houseSideSize) {
				break;
			}
			oldVariation = (int) currentXY[1];
		}
	}

	private void createNonOrthogonalClusterEntrance(int[] beginXY, int[] finalXY, int direction, Double gradient) {
		int[] tbXY = new int[2];
		int[] tfXY = new int[2];
		double offset = -gradient * beginXY[0] + beginXY[1];
		double distance = Math.sqrt(0 + Math.pow(beginXY[1] - finalXY[1], 2));
		boolean inverse = false;
		boolean isUpDown = false;

		if (distance < SpineConfiguration.CLUSTER_ENTRANCE_SIZE) {
			return;
		}

		if (beginXY[1] < finalXY[1]) {
			tbXY[1] = (int) (beginXY[1] + (distance) / 2);
			tbXY[0] = (int) ((tbXY[1] - offset) / gradient);

			tfXY[1] = (int) (finalXY[1] - (distance / 2 - SpineConfiguration.CLUSTER_ENTRANCE_SIZE));
			tfXY[0] = (int) ((tfXY[1] - offset) / gradient);
		} else {
			isUpDown = true;
			tbXY[1] = (int) (finalXY[1] + (distance) / 2);
			tbXY[0] = (int) ((tbXY[1] - offset) / gradient);

			tfXY[1] = (int) (beginXY[1] - (distance / 2 - SpineConfiguration.CLUSTER_ENTRANCE_SIZE));
			tfXY[0] = (int) ((tfXY[1] - offset) / gradient);
		}

		// THIS IS FOR FINDING WHERE DOES X POINT ALL THE TIME GIVEN POINTS
		// COORDINATES. DO NOT MOVE.
		if (finalXY[0] > beginXY[0]) {
			if (finalXY[1] > beginXY[1]) {
				inverse = false;
			} else {
				inverse = true;
			}
		} else {
			if (finalXY[1] > beginXY[1]) {
				inverse = false;
			} else {
				inverse = true;
			}
		}

		if (tbXY[1] > tfXY[1])
			return;

		// double orthogonalGradient = -1 / gradient;
		// double orthogonalOffset = -orthogonalGradient * tbXY[0] + tbXY[1];
		double variation[] = new double[2];
		int[] currentXY = new int[2];
		int countYFactor = 0;
		int oldVariation = -1;

		for (int j = 0; j < SpineConfiguration.CLUSTER_ENTRANCE_SIZE; j++) {
			currentXY[0] = tbXY[0] + j;
			currentXY[1] = (int) (gradient * currentXY[0] + offset);
			// orthogonalOffset = -orthogonalGradient * currentXY[0] +
			// currentXY[1];

			if (isUpDown && (oldVariation != -1) && ((oldVariation + 1) < currentXY[1])) {
				// we take the reminder y that are needed for an exact answer
				for (int w = oldVariation + 1; w < currentXY[1]; w++) {
					for (int i = 0; i < 2 * SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE; i++) {
						variation[0] = currentXY[0] + (!inverse ? i : -i);
						variation[1] = w;
						// orthogonalGradient * variation[0] + orthogonalOffset;
						if (landPointisOnMap(MapHelper.formKey((int) variation[0], (int) variation[1]))) {
							findPoint(MapHelper.formKey((int) variation[0], (int) variation[1]))
									.setType(SpineConfiguration.CLUSTER_ENTRANCE_MARK);
						}
					}
					countYFactor++;
					if (countYFactor == SpineConfiguration.CLUSTER_ENTRANCE_SIZE) {
						break;
					}
				}
			} else if ((oldVariation != -1) && ((oldVariation - 1) > currentXY[1])) {
				// we take the reminder y that are needed for an exact answer
				for (int w = currentXY[1] + 1; w < oldVariation; w++) {
					for (int i = 0; i < 2 * SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE; i++) {
						variation[0] = currentXY[0] + (!inverse ? i : -i);
						variation[1] = w;
						// orthogonalGradient * variation[0] + orthogonalOffset;
						if (landPointisOnMap(MapHelper.formKey((int) variation[0], (int) variation[1]))) {
							findPoint(MapHelper.formKey((int) variation[0], (int) variation[1]))
									.setType(SpineConfiguration.CLUSTER_ENTRANCE_MARK);
						}
					}
					countYFactor++;
					if (countYFactor == SpineConfiguration.CLUSTER_ENTRANCE_SIZE) {
						break;
					}
				}
			}

			if (countYFactor == SpineConfiguration.CLUSTER_ENTRANCE_SIZE) {
				break;
			}

			// We need to find the furthest
			for (int i = 0; i < 2 * SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE; i++) {
				variation[0] = currentXY[0] + (!inverse ? i : -i);
				variation[1] = currentXY[1];
				// orthogonalGradient * variation[0] + orthogonalOffset;
				if (landPointisOnMap(MapHelper.formKey((int) variation[0], (int) variation[1]))) {
					findPoint(MapHelper.formKey((int) variation[0], (int) variation[1]))
							.setType(SpineConfiguration.CLUSTER_ENTRANCE_MARK);
				}
			}
			countYFactor++;
			if (countYFactor == SpineConfiguration.CLUSTER_ENTRANCE_SIZE) {
				break;
			}
			oldVariation = (int) currentXY[1];
		}
	}

	private int[] createNonOrthogonalWalkRoute(Integer initialPoint, Integer finalPoint, int[] beginXY, boolean inverse,
			Double gradient) {
		// f lines are perpendicular, M1× M2 = − 1
		double offset = -gradient * beginXY[0] + beginXY[1];

		double orthogonalGradient = -1 / gradient;
		double orthogonalOffset = -orthogonalGradient * beginXY[0] + beginXY[1];

		int[] initialXY = SpineMapHelper.breakKey(initialPoint);
		int[] finalXY = SpineMapHelper.breakKey(finalPoint);
		boolean down = false;
		if (inverse) {
			if (finalXY[0] - initialXY[0] > 0) {
				// EAST
				down = true;
				beginXY[0] = finalXY[0] - SpineConfiguration.WALK_BRANCH_SIZE;
				beginXY[1] = (int) (gradient * beginXY[0] + offset);
			} else {
				// WEST
				down = false;
				beginXY[0] = initialXY[0] - SpineConfiguration.WALK_BRANCH_SIZE;
				beginXY[1] = (int) (gradient * beginXY[0] + offset);
			}
		} else {
			if (finalXY[0] - initialXY[0] > 0) {
				// EAST
				down = true;
				beginXY[0] = initialXY[0];
				beginXY[1] = (int) (gradient * beginXY[0] + offset);
			} else {
				// WEST
				down = false;
				beginXY[0] = finalXY[0];
				beginXY[1] = (int) (gradient * beginXY[0] + offset);
			}
		}

		double variation[] = new double[2];

		int[] currentXY = new int[2];
		for (int j = 0; j < SpineConfiguration.WALK_BRANCH_SIZE; j++) {
			currentXY[0] = beginXY[0] + j;
			currentXY[1] = (int) (gradient * currentXY[0] + offset);
			orthogonalOffset = -orthogonalGradient * currentXY[0] + currentXY[1];

			for (int i = 0; i < 2 * SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE; i++) {
				variation[1] = currentXY[1] + (!down ? i : -i);
				variation[0] = (variation[1] - orthogonalOffset) / orthogonalGradient;
				if (landPointisOnMap(SpineMapHelper.formKey((int) variation[0], (int) variation[1]))) {
					findPoint(MapHelper.formKey((int) variation[0], (int) variation[1]))
							.setType(SpineConfiguration.WALK_MARK);
				}
			}
		}

		// We have the full
		currentXY[0] = currentXY[0] + (!down ? 1 : -1);
		currentXY[1] = (int) (gradient * currentXY[0] + offset);
		return currentXY;
	}

	private void createClusterEntrance(int[] currentXY, int[] finalXY, int direction) {
		// NORTH then it goes toward x+
		// SOUTH toward x-
		// given that x is the same, y is our indicator for the middle
		int upperMiddle[] = new int[2];
		upperMiddle[0] = currentXY[0];
		upperMiddle[1] = ((currentXY[1] + finalXY[1]) / 2) + (SpineConfiguration.CLUSTER_ENTRANCE_SIZE) / 2;

		int lowerMiddle[] = new int[2];
		lowerMiddle[0] = currentXY[0];
		lowerMiddle[1] = ((currentXY[1] + finalXY[1]) / 2) - (SpineConfiguration.CLUSTER_ENTRANCE_SIZE) / 2;

		if ((direction == SpineConstants.NORTH) && (lowerMiddle[1] < currentXY[1] || upperMiddle[1] > finalXY[1]))
			return;
		else if ((direction == SpineConstants.SOUTH)
				&& (lowerMiddle[1] > currentXY[1] || upperMiddle[1] < finalXY[1]))
			return;
		createInsideClusterRoute(upperMiddle, SpineMapHelper.formKey(lowerMiddle[0], lowerMiddle[1]), direction,
				SpineConfiguration.WALK_BRANCH, SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2,
				SpineConfiguration.CLUSTER_ENTRANCE_MARK);
	}

	private SpineBuilding createWalkRoute(int[] currentXY, boolean isInverse, int direction, int rotation) {
		if (isInverse) {
			return createInsideClusterRoute(currentXY,
					SpineMapHelper.moveKeyByOffsetAndDirection(SpineMapHelper.formKey(currentXY[0], currentXY[1]),
							SpineConfiguration.WALK_BRANCH_SIZE, SpineDirectionHelper.oppositeDirection(direction)),
					direction, SpineConfiguration.WALK_BRANCH, SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2,
					SpineConfiguration.WALK_MARK);
		} else {
			return createInsideClusterRoute(currentXY,
					SpineMapHelper.moveKeyByOffsetAndDirection(SpineMapHelper.formKey(currentXY[0], currentXY[1]),
							SpineConfiguration.WALK_BRANCH_SIZE, direction),
					direction, SpineConfiguration.WALK_BRANCH, SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2,
					SpineConfiguration.WALK_MARK);
		}
	}

	private boolean canBeLotized(int[] currentXY, int houseSideSize, int doublehouseDepthSize, int direction) {
		switch (direction) {
		case SpineConstants.EAST:
			for (int i = currentXY[0]; i < currentXY[0] + houseSideSize; i++) {
				for (int j = currentXY[1]; j > currentXY[1] - doublehouseDepthSize; j--) {
					if (landPointisOnMap(SpineMapHelper.formKey(i, j))) {
						String type = findPoint(SpineMapHelper.formKey(i, j)).getType();
						if (type.equals(SpineConfiguration.CLUSTER_ENTRANCE_MARK)) {
							return false;
						}
					} else {
						return false;
					}
				}
			}
			break;
		case SpineConstants.NORTH:
			for (int i = currentXY[1]; i < currentXY[1] + houseSideSize; i++) {
				for (int j = currentXY[0]; j < currentXY[0] + doublehouseDepthSize; j++) {
					if (landPointisOnMap(SpineMapHelper.formKey(j, i))) {
						String type = findPoint(SpineMapHelper.formKey(j, i)).getType();
						if (type.equals(SpineConfiguration.CLUSTER_ENTRANCE_MARK)) {
							return false;
						}
					} else {
						return false;
					}
				}
			}
			break;
		case SpineConstants.WEST:
			for (int i = currentXY[0]; i >= currentXY[0] - houseSideSize; i--) {
				for (int j = currentXY[1]; j < currentXY[1] + doublehouseDepthSize; j++) {
					if (landPointisOnMap(SpineMapHelper.formKey(i, j))) {
						String type = findPoint(SpineMapHelper.formKey(i, j)).getType();
						if (type.equals(SpineConfiguration.CLUSTER_ENTRANCE_MARK)) {
							return false;
						}
					} else {
						return false;
					}
				}
			}
			break;
		case SpineConstants.SOUTH:
			for (int i = currentXY[1]; i >= currentXY[1] - houseSideSize; i--) {
				for (int j = currentXY[0]; j > currentXY[0] - doublehouseDepthSize; j--) {
					if (landPointisOnMap(SpineMapHelper.formKey(j, i))) {
						String type = findPoint(SpineMapHelper.formKey(j, i)).getType();
						if (type.equals(SpineConfiguration.CLUSTER_ENTRANCE_MARK)) {
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
		case SpineConstants.EAST:
			for (int i = currentXY[0]; i < currentXY[0] + houseSideMinimunSize; i++) {
				for (int j = currentXY[1]; j > currentXY[1] - houseDepthMinimunSize; j--) {
					findPoint(SpineMapHelper.formKey(i, j)).setType("" + serialNumber);
				}
				for (int j = currentXY[1] - (houseDepthMinimunSize + 1); j > currentXY[1]
						- 2 * houseDepthMinimunSize; j--) {
					findPoint(SpineMapHelper.formKey(i, j)).setType("" + (serialNumber + 1));
				}
			}
			break;
		case SpineConstants.NORTH:
			for (int i = currentXY[1]; i < currentXY[1] + houseSideMinimunSize; i++) {
				for (int j = currentXY[0]; j < currentXY[0] + houseDepthMinimunSize; j++) {
					findPoint(SpineMapHelper.formKey(j, i)).setType("" + serialNumber);
				}
				for (int j = currentXY[0] + houseDepthMinimunSize + 1; j < currentXY[0]
						+ 2 * houseDepthMinimunSize; j++) {
					findPoint(SpineMapHelper.formKey(j, i)).setType("" + (serialNumber + 1));
				}
			}
			break;
		case SpineConstants.WEST:
			for (int i = currentXY[0]; i >= currentXY[0] - houseSideMinimunSize; i--) {
				for (int j = currentXY[1]; j < currentXY[1] + houseDepthMinimunSize; j++) {
					findPoint(SpineMapHelper.formKey(i, j)).setType("" + serialNumber);
				}
				for (int j = currentXY[1] + houseDepthMinimunSize + 1; j < currentXY[1]
						+ 2 * houseDepthMinimunSize; j++) {
					findPoint(SpineMapHelper.formKey(i, j)).setType("" + (serialNumber + 1));
				}
			}
			break;
		case SpineConstants.SOUTH:
			for (int i = currentXY[1]; i >= currentXY[1] - houseSideMinimunSize; i--) {
				for (int j = currentXY[0]; j > currentXY[0] - houseDepthMinimunSize; j--) {
					findPoint(SpineMapHelper.formKey(j, i)).setType("" + serialNumber);
				}
				for (int j = currentXY[0] - (houseDepthMinimunSize + 1); j > currentXY[0]
						- 2 * houseDepthMinimunSize; j--) {
					findPoint(SpineMapHelper.formKey(j, i)).setType("" + (serialNumber + 1));
				}
			}
			break;
		}
	}

	private SpineBuilding createInsideClusterRoute(int[] currentXY, int finalKey, int direction, int type, int depth,
			String markType) {
		int[] finalXY = SpineMapHelper.breakKey(finalKey);
		int lower, upper;
		SpineBuilding spineBuilding = new SpineBuilding();

		if ((direction == SpineConstants.NORTH) || (direction == SpineConstants.SOUTH)) {
			if (currentXY[1] > finalXY[1]) {
				lower = finalXY[1];
				upper = currentXY[1];
			} else {
				lower = currentXY[1];
				upper = finalXY[1];
			}

			if (direction == SpineConstants.SOUTH) {
				for (int i = lower; i < upper; i++) {
					for (int j = currentXY[0]; j > currentXY[0] - depth; j--) {
						findPoint(SpineMapHelper.formKey(j, i)).setType(markType);
					}
				}
			} else {
				for (int i = lower; i < upper; i++) {
					for (int j = currentXY[0]; j < currentXY[0] + depth; j++) {
						findPoint(SpineMapHelper.formKey(j, i)).setType(markType);
					}
				}
			}
		} else if ((direction == SpineConstants.EAST) || (direction == SpineConstants.WEST)) {
			if (currentXY[0] > finalXY[0]) {
				lower = finalXY[0];
				upper = currentXY[0];
			} else {
				lower = currentXY[0];
				upper = finalXY[0];
			}

			if (direction == SpineConstants.EAST) {
				for (int i = lower; i < upper; i++) {
					for (int j = currentXY[1]; j > currentXY[1] - depth; j--) {
						findPoint(SpineMapHelper.formKey(i, j)).setType(markType);
					}
				}
			} else {
				for (int i = lower; i < upper; i++) {
					for (int j = currentXY[1]; j < currentXY[1] + depth; j++) {
						findPoint(SpineMapHelper.formKey(i, j)).setType(markType);
					}
				}
			}
		}

		spineBuilding.setType(markType);
		spineBuilding.setNumber(0);
		return spineBuilding;
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
					mapString += type + "" + repetitions + ",";
					repetitions = 1;
					type = getLandPoint(ClusterMapHelper.formKey(i, j)).getType();
				}
			}
			mapString += type + "" + repetitions + ",";
			mapString += "|";
		}
		return mapString;
	}

	public String compress() {
		Map<String, List<Integer>> mapMap = new HashMap<>();

		String mapString = "";
		for (int j = pointsy - 1; j >= 0; j--) {
			String type = getLandPoint(SpineMapHelper.formKey(0, j)).getType();
			if (mapMap.get(type) == null) {
				mapMap.put(type, new ArrayList<>());
			}
			int i;
			for (i = 1; i < pointsx; i++) {
				if (!type.equals(getLandPoint(SpineMapHelper.formKey(i, j)).getType())) {
					mapMap.get(type).add(SpineMapHelper.formKey(i - 1, j));
					type = getLandPoint(SpineMapHelper.formKey(i, j)).getType();
					if (mapMap.get(type) == null) {
						mapMap.put(type, new ArrayList<>());
					}
					mapMap.get(type).add(SpineMapHelper.formKey(i, j));
				}
			}
			mapMap.get(type).add(SpineMapHelper.formKey(i, j));
		}

		Iterator<Entry<String, List<Integer>>> it = mapMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, List<Integer>> pair = (Map.Entry<String, List<Integer>>) it.next();
			mapString += pair.getKey();
			for (int i = 0; i < pair.getValue().size(); i++) {
				mapString += "," + pair.getValue().get(i).intValue();
			}
			mapString += "|";
		}

		return mapString;
	}
	
	public void printMapToFileNew() {
		try {
			PrintWriter writer = new PrintWriter("printed-map.txt", "UTF-8");
			for (int j = pointsy - 1; j >= 0; j--) {
				for (int i = 0; i < pointsx; i++) {
					  /*if(getLandPoint(SpineMapHelper.formKey(i, j)).getType().equals("n")){
						 writer.print("e");
					 }
					 else */
					writer.print(getLandPoint(SpineMapHelper.formKey(i, j)).getType());
				}
				writer.println();
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void printMapToFileNewCentroide() {
		try {
			PrintWriter writer = new PrintWriter("printed-map.txt", "UTF-8");
			for (int j = pointsy - 1; j >= 0; j--) {
				for (int i = 0; i < pointsx; i++) {
					  /*if(getLandPoint(SpineMapHelper.formKey(i, j)).getType().equals("n")){
						 writer.print("e");
					 }
					 else */
					writer.print(getLandPoint(SpineMapHelper.formKey(i, j)).getType());
				}
				writer.println();
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}