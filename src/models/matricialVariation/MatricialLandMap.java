package models.matricialVariation;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import helpers.matricialVariation.MatricialDirectionHelper;
import helpers.matricialVariation.MatricialMapHelper;
import interfaces.matricialVariation.MatricialConfiguration;
import interfaces.matricialVariation.MatricialConstants;
import models.configuration.ConfigurationEntry;




public class MatricialLandMap {
	private int pointsx = -1;
	private int pointsy = -1;
	
	private MatricialLandPoint centroid;

	private Map<Integer, MatricialLandPoint> map;

	private MatricialLandRoute landRoute;
	private List<MatricialLandRoute> landRoutes = new ArrayList<>();
	private List<Integer> nodes = new ArrayList<>();
	List<List<Integer>> fullPolygon;
	private  List<Integer>coordinates;
	private List<MatricialLandPoint> polygonNodes;
	private double polygonalArea;
	
	private ConfigurationEntry configuration;

	public void setConfiguration(ConfigurationEntry configurationEntry) {
		this.configuration = configurationEntry;
	}
	
	public MatricialLandMap(int pointsx, int pointsy) {
		this.setPointsx(++pointsx);
		this.setPointsy(++pointsy);

		map = new HashMap<>();
		for (int i = 0; i < pointsx; i++) {
			for (int j = 0; j < pointsy; j++) {
				map.put(MatricialMapHelper.formKey(i, j), new MatricialLandPoint(i, j));
			}
		}
	}
	
	public int getPointsx() {
		return pointsx;
	}
	public void setCoordinates(List<Integer>coordinates){
		this.coordinates=coordinates;
	}
	public List<Integer> getCoordinates(){
		return this.coordinates;
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
	
	public MatricialLandPoint getLandPoint(int pointId) {
		return map.get(pointId);
	}

	public MatricialLandPoint getCentroid() {
		return centroid;
	}

	public void setCentroid(MatricialLandPoint centroid) {
		this.centroid = centroid;
	}

	public MatricialLandPoint findPoint(int entryPointId) {
		return map.get(entryPointId);
	}

	public void setLandRoute(MatricialLandRoute landRoute) {
		this.landRoute = landRoute;
	}

	public MatricialLandRoute getLandRoute() {
		return landRoute;
	}
	
	public List<MatricialLandRoute> getLandRoutes() {
		return landRoutes;
	}

	public void setLandRoutes(List<MatricialLandRoute> landRoutes) {
		this.landRoutes = landRoutes;
	}
	
	public void createBorderFromPolygon(List<MatricialLandPoint> polygon) {
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
					getLandPoint(MatricialMapHelper.formKey(polygon.get(i).getX(), w))
							.setType(MatricialConstants.POLYGON_LIMIT);
					truePolygon.add(MatricialMapHelper.formKey(polygon.get(i).getX(), w));
				}
				continue;
			}

			double gradient = (polygon.get(j).getY() - polygon.get(i).getY()) * 1.0 / underscore;
			// 2nd, gradient=0; straight in the X axis
			int lower = polygon.get(i).getX() < polygon.get(j).getX() ? polygon.get(i).getX() : polygon.get(j).getX();
			int upper = polygon.get(i).getX() > polygon.get(j).getX() ? polygon.get(i).getX() : polygon.get(j).getX();
			if (gradient == 0) {
				for (int w = lower; w <= upper; w++) {
					getLandPoint(MatricialMapHelper.formKey(w, polygon.get(i).getY()))
							.setType(MatricialConstants.POLYGON_LIMIT);
					truePolygon.add(MatricialMapHelper.formKey(w, polygon.get(i).getY()));
				}
				continue;
			}

			double b = polygon.get(j).getY() - gradient * polygon.get(j).getX();
			// 3nd the gradient is positive/negative.
			for (int w = lower; w <= upper; w++) {
				float y = MatricialMapHelper.round(gradient * w + b);
				if (y == (int) y) // quick and dirty convertion check
				{
					getLandPoint(MatricialMapHelper.formKey(w, (int) y)).setType(MatricialConstants.POLYGON_LIMIT);
					truePolygon.add(MatricialMapHelper.formKey(w, (int) y));
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
				MatricialLandPoint initialLandPoint = getLandPoint(fullPolygon.get(x).get(y));
				polygonRow.add(fullPolygon.get(x).get(y));
				MatricialLandPoint finalLandPoint = getLandPoint(fullPolygon.get(x).get(y + 1));

				int wi = initialLandPoint.getX();
				int wf = finalLandPoint.getX();

				if (initialLandPoint.getY() > finalLandPoint.getY()) {
					for (int z = initialLandPoint.getY() - 1; z >= finalLandPoint.getY() + 1; z--) {
						if (wi != wf) {
							if (isPolygonBorder(wi, z)) {
								polygonRow.add(MatricialMapHelper.formKey(wi, z));
							}
						}

						if (isPolygonBorder(wf, z)) {
							polygonRow.add(MatricialMapHelper.formKey(wf, z));
						}
					}
				} else {
					for (int z = initialLandPoint.getY() + 1; z <= finalLandPoint.getY() - 1; z++) {
						if (wi != wf) {
							if (isPolygonBorder(wi, z)) {
								polygonRow.add(MatricialMapHelper.formKey(wi, z));
							}
						}
						if (isPolygonBorder(wf, z)) {
							polygonRow.add(MatricialMapHelper.formKey(wf, z));
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
		return (findPoint(MatricialMapHelper.formKey(x - 1, y)).getType().equals(MatricialConfiguration.OUTSIDE_POLYGON_MARK)
				|| findPoint(MatricialMapHelper.formKey(x + 1, y)).getType().equals(MatricialConfiguration.OUTSIDE_POLYGON_MARK))
				&& !findPoint(MatricialMapHelper.formKey(x, y)).getType().equals(MatricialConfiguration.OUTSIDE_POLYGON_MARK);
	}
	
	private void fillPolygonalArea() {
		for (int x = 0; x < pointsx; x++) {
			int count = 0;
			int pInitialLimit = -1;
			boolean reversed = false;

			for (int y = 0; y < pointsy; y++) {
				if (getLandPoint(MatricialMapHelper.formKey(x, y)).getType() == MatricialConstants.POLYGON_LIMIT) {
					count++;
					pInitialLimit = pInitialLimit == -1 ? y : pInitialLimit;
				}

				switch (count) {
				case 0:
					getLandPoint(MatricialMapHelper.formKey(x, y)).setType(MatricialConfiguration.OUTSIDE_POLYGON_MARK);
					break;
				case 2:
					if (!reversed) {
						for (int w = pInitialLimit + 1; w < y; w++) {
							getLandPoint(MatricialMapHelper.formKey(x, w)).setType(MatricialConfiguration.EMPTY_MARK);
						}
						reversed = true;
					} else {
						getLandPoint(MatricialMapHelper.formKey(x, y)).setType(MatricialConfiguration.OUTSIDE_POLYGON_MARK);
					}
					break;
				}
			}
		}
	}
	
	// Variation for the creation of zones
		public void createBorderFromPolygon(List<Integer> polygon, String markType) {
			for (int i = 0; i < polygon.size(); i++) {
				int xyInitial[] = MatricialMapHelper.breakKey(polygon.get(i));
				int xyFinal[] = MatricialMapHelper.breakKey(polygon.get((i + 1) % polygon.size()));

				int underscore = (xyFinal[0] - xyInitial[0]);
				if (underscore == 0) {
					int lower = xyInitial[1] < xyFinal[1] ? xyInitial[1] : xyFinal[1];
					int upper = xyInitial[1] > xyFinal[1] ? xyInitial[1] : xyFinal[1];

					for (int w = lower; w <= upper; w++) {
						getLandPoint(MatricialMapHelper.formKey(xyInitial[0], w)).setType(markType);
					}
					continue;
				}

				double gradient = (xyFinal[1] - xyInitial[1]) * 1.0 / underscore;
				// 2nd, gradient=0; straight in the X axis
				int lower = xyInitial[0] < xyFinal[0] ? xyInitial[0] : xyFinal[0];
				int upper = xyInitial[0] > xyFinal[0] ? xyInitial[0] : xyFinal[0];
				if (gradient == 0) {
					for (int w = lower; w <= upper; w++) {
						getLandPoint(MatricialMapHelper.formKey(w, xyInitial[1])).setType(markType);
					}
					continue;
				}

				double b = xyFinal[1] - gradient * xyFinal[0];
				// 3nd the gradient is positive/negative.
				for (int w = lower; w <= upper; w++) {
					float y = MatricialMapHelper.round(gradient * w + b);
					if (y == (int) y) // quick and dirty convertion check
					{
						getLandPoint(MatricialMapHelper.formKey(w, (int) y)).setType(markType);
					}
				}
			}
		}
		
		public void clearDottedLimits() {
			for (int x = 0; x < fullPolygon.size(); x++) {
				for (int i = 0; i < fullPolygon.get(x).size(); i++) {
					getLandPoint(fullPolygon.get(x).get(i)).setType(MatricialConfiguration.EMPTY_MARK);
				}
			}
		}
		
		public void findPolygonalArea(List<MatricialLandPoint> polygon) {
			int absoluteArea = 0;
			for (int i = 0; i < polygon.size(); i++) {
				absoluteArea += (polygon.get(i).getX() * polygon.get((i + 1) % polygon.size()).getY())
						- (polygon.get(i).getY() * polygon.get((i + 1) % polygon.size()).getX());
			}
			setPolygonalArea(Math.abs(absoluteArea) / 2);
		}
		
		private void findCentroid(List<MatricialLandPoint> polygon) {
			this.setCentroid(new MatricialLandPoint(pointsx / 2, pointsy / 2));
		}
		
		public void printMapToFile() {
			try {
				PrintWriter writer = new PrintWriter("printed-map.txt", "UTF-8");
				for (int j = pointsy - 1; j >= 0; j--) {
					for (int i = 0; i < pointsx; i++) {
						writer.print(getLandPoint(MatricialMapHelper.formKey(i, j)).getType());
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
						//getLandPoint(MatricialMapHelper.formKey(i, j)).getType()
						if(i==ejex && j==ejey)writer.print("x");
						else writer.print(getLandPoint(MatricialMapHelper.formKey(i, j)).getType());
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
			case MatricialConfiguration.ARTERIAL_BRANCH:
				variation = MatricialConfiguration.ARTERIAL_MARK;
				break;
			case MatricialConfiguration.COLLECTOR_BRANCH:
				variation = MatricialConfiguration.COLLECTOR_MARK;
				break;
			case MatricialConfiguration.LOCAL_BRANCH:
				variation = MatricialConfiguration.LOCAL_MARK;
				break;
			case MatricialConfiguration.NODE:
				variation = MatricialConfiguration.NODE_MARK;
				break;
			}
			map.get(entryPointId).setType(variation);
			map.get(entryPointId).setNodeType(nodeType);
		}
		
		public boolean landPointisOnMap(int pointId) {
			int[] xy = MatricialMapHelper.breakKey(pointId);
			return xy[0] < pointsx && xy[0] > 0 && xy[1] < pointsy && xy[1] > 0;
		}

		public List<Integer> getNodes() {
			return nodes;
		}

		public void setNodes(List<Integer> nodes) {
			this.nodes = nodes;
		}

		public List<MatricialLandPoint> getPolygonNodes() {
			return polygonNodes;
		}
		
		public void setPolygonNodes(List<MatricialLandPoint> polygonNodes) {
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
			if (!findPoint(MatricialMapHelper.formKey(x, y)).getType().equals(MatricialConfiguration.NODE_MARK)) {
				return false;
			}

			if ((y + 1 != pointsy) && (y - 1 != -1) && (x - 1 != -1) && (x + 1 != pointsx)) {
				if (findPoint(MatricialMapHelper.formKey(x, y + 1)).getType().equals(MatricialConfiguration.NODE_MARK)
						&& findPoint(MatricialMapHelper.formKey(x, y - 1)).getType()
								.equals(MatricialConfiguration.OUTSIDE_POLYGON_MARK)
						&& findPoint(MatricialMapHelper.formKey(x + 1, y)).getType().equals(MatricialConfiguration.EMPTY_MARK)
						&& (!findPoint(MatricialMapHelper.formKey(x - 1, y)).getType().equals(MatricialConfiguration.EMPTY_MARK)
								|| !findPoint(MatricialMapHelper.formKey(x + 1, y)).getType()
										.equals(MatricialConfiguration.NODE_MARK))) {
					return true;
				}
			}

			return false;
		}
		
		public boolean intersectMainRoute(int entryPointId) {
			int initialPoint = landRoute.getInitialPointId();
			int finalPoint = landRoute.getFinalPointId();

			int[] initialXY = MatricialMapHelper.breakKey(initialPoint);
			int[] finalXY = MatricialMapHelper.breakKey(finalPoint);
			int[] entryXY = MatricialMapHelper.breakKey(entryPointId);

			return (initialXY[0] < entryXY[0] && entryXY[0] < finalXY[0])
					|| ((initialXY[1] < entryXY[1] && entryXY[1] < finalXY[1]));
		}
		
		public boolean isNormalNode(int x, int y) {
			// up,down,left,right
			if (!findPoint(MatricialMapHelper.formKey(x, y)).getType().equals(MatricialConfiguration.NODE_MARK)) {
				return false;
			}
			int node = 0;
			boolean[] nodeInBorder = new boolean[] { false, false, false, false };
			int outside = 0;
			if (y + 1 != pointsy) {
				if (findPoint(MatricialMapHelper.formKey(x, y + 1)).getType().equals(MatricialConfiguration.NODE_MARK)) {
					node++;
					nodeInBorder[0] = true;
				}

				if (findPoint(MatricialMapHelper.formKey(x, y + 1)).getType()
						.equals(MatricialConfiguration.OUTSIDE_POLYGON_MARK))
					outside++;
			}

			if (y - 1 != -1) {
				if (findPoint(MatricialMapHelper.formKey(x, y - 1)).getType().equals(MatricialConfiguration.NODE_MARK)) {
					node++;
					nodeInBorder[1] = true;
				}

				if (findPoint(MatricialMapHelper.formKey(x, y - 1)).getType()
						.equals(MatricialConfiguration.OUTSIDE_POLYGON_MARK))
					outside++;
			}

			if (x - 1 != -1) {
				if (findPoint(MatricialMapHelper.formKey(x - 1, y)).getType().equals(MatricialConfiguration.NODE_MARK)) {
					node++;
					nodeInBorder[2] = true;
				}

				if (findPoint(MatricialMapHelper.formKey(x - 1, y)).getType()
						.equals(MatricialConfiguration.OUTSIDE_POLYGON_MARK))
					outside++;
			}

			if (x + 1 != pointsx) {
				if (findPoint(MatricialMapHelper.formKey(x + 1, y)).getType().equals(MatricialConfiguration.NODE_MARK)) {
					node++;
					nodeInBorder[3] = true;
				}

				if (findPoint(MatricialMapHelper.formKey(x + 1, y)).getType()
						.equals(MatricialConfiguration.OUTSIDE_POLYGON_MARK))
					outside++;
			}

			if ((y + 1 != pointsy) && (y - 1 != -1) && (x - 1 != -1) && (x + 1 != pointsx)) {
				if ((findPoint(MatricialMapHelper.formKey(x, y - 1)).getType().equals(MatricialConfiguration.ARTERIAL_MARK)
						|| findPoint(MatricialMapHelper.formKey(x, y - 1)).getType().equals(MatricialConfiguration.LOCAL_MARK))
						&& findPoint(MatricialMapHelper.formKey(x - 1, y)).getType()
								.equals(MatricialConfiguration.COLLECTOR_MARK)
						&& findPoint(MatricialMapHelper.formKey(x + 1, y)).getType().equals(MatricialConfiguration.NODE_MARK)
						&& findPoint(MatricialMapHelper.formKey(x, y + 1)).getType().equals(MatricialConfiguration.NODE_MARK)) {
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
		
		
		
		public MatricialPolygon joinWithPolygonalBorder(MatricialPolygon matricialPolygon) {
			int initialVertex = matricialPolygon.getPoints().get(0);
			int finalVertex = matricialPolygon.getPoints().get(matricialPolygon.getPoints().size() - 1);
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
				matricialPolygon.setComplete(true);
				return matricialPolygon;
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
					matricialPolygon.printPolygon();
					return matricialPolygon;
				}

				int initialVertex0 = fullPolygon.get(initialVertexSide).get(0);
				int initialVertexFinal = fullPolygon.get(initialVertexSide)
						.get(fullPolygon.get(initialVertexSide).size() - 1);
				int finalVertex0 = fullPolygon.get(finalVertexSide).get(0);
				int finalVertexFinal = fullPolygon.get(finalVertexSide).get(fullPolygon.get(finalVertexSide).size() - 1);

				if (initialVertex0 == finalVertex0) {
					matricialPolygon.getPoints().add(initialVertex0);
					matricialPolygon.setComplete(true);
					// System.out.println("Complete data polygon");
					matricialPolygon.printPolygon();
					return matricialPolygon;
				} else if (initialVertex0 == finalVertexFinal) {
					matricialPolygon.getPoints().add(initialVertex0);
					matricialPolygon.setComplete(true);
					// System.out.println("Complete data polygon");
					matricialPolygon.printPolygon();
					return matricialPolygon;
				} else if (initialVertexFinal == finalVertex0) {
					matricialPolygon.getPoints().add(initialVertexFinal);
					matricialPolygon.setComplete(true);
					// System.out.println("Complete data polygon");
					matricialPolygon.printPolygon();
					return matricialPolygon;
				} else if (initialVertexFinal == finalVertexFinal) {
					matricialPolygon.getPoints().add(initialVertexFinal);
					matricialPolygon.setComplete(true);
					// System.out.println("Complete data polygon");
					matricialPolygon.printPolygon();
					return matricialPolygon;
				}
				// System.out.println("Polygon with more than 4 sides");
			}
			return matricialPolygon;
		}
		
		public Object lotize(List<Integer> list, int direction, int beginning) {
			if (beginning >= list.size()) {
				return 0;
			}

			int seed = 0;
			boolean lotizable = true, notUniform = false;
			int[] currentXY = MatricialMapHelper.breakKey(list.get(beginning));
			int[] finalXY = MatricialMapHelper.breakKey(list.get((beginning + 1) % list.size()));
			Double gradient = (currentXY[1] - finalXY[1]) * 1.0 / (currentXY[0] - finalXY[0]);
			double offset = finalXY[1] - gradient * finalXY[0];
			if (direction == MatricialConstants.EAST || direction == MatricialConstants.WEST) {
				if (gradient.doubleValue() == 0.0) {
					currentXY[0] = direction == MatricialConstants.EAST ? currentXY[0] + 1 : currentXY[0];
					MatricialBuilding matricialBuilding = createWalkRoute(currentXY, false, direction, beginning);
					if (matricialBuilding != null) {
						currentXY = MatricialMapHelper.moveKeyByOffsetAndDirection(currentXY,
								MatricialConfiguration.WALK_BRANCH_SIZE, direction);
					}

					finalXY[0] = direction == MatricialConstants.EAST ? finalXY[0] : finalXY[0] + 1;
					matricialBuilding = createWalkRoute(finalXY, true, direction, beginning);
					if (matricialBuilding != null) {
						finalXY = MatricialMapHelper.moveKeyByOffsetAndDirection(finalXY,
								MatricialConfiguration.WALK_BRANCH_SIZE, MatricialDirectionHelper.oppositeDirection(direction));
					}
				} else {
					notUniform = true;
					currentXY = createNonOrthogonalWalkRoute(list.get(beginning), list.get((beginning + 1) % list.size()),
							finalXY, false, gradient);

					finalXY = createNonOrthogonalWalkRoute(list.get(beginning), list.get((beginning + 1) % list.size()),
							finalXY, true, gradient);
				}
			} else if (direction == MatricialConstants.SOUTH || direction == MatricialConstants.NORTH) {
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
				if ((direction == MatricialConstants.EAST) || (direction == MatricialConstants.NORTH))
					done = currentXY[0] >= finalXY[0] && currentXY[1] >= finalXY[1];
				else
					done = currentXY[0] <= finalXY[0] && currentXY[1] <= finalXY[1];

				if (done) {
					if (notUniform) {
						int newDirection = MatricialDirectionHelper.orthogonalDirectionFromPointToPoint(
								list.get((beginning + 1) % list.size()), list.get((beginning + 2) % list.size()));
						return lotize(list, newDirection, ++beginning);
					}
					switch (direction) {
					case MatricialConstants.EAST:
						return lotize(list, MatricialConstants.NORTH, ++beginning);
					case MatricialConstants.NORTH:
						return lotize(list, MatricialConstants.WEST, ++beginning);
					case MatricialConstants.WEST:
						return lotize(list, MatricialConstants.SOUTH, ++beginning);
					case MatricialConstants.SOUTH:
						return 0;
					}
				}

				if (gradient.doubleValue() == 0.0 || gradient.isInfinite()) {
					lotizable = canBeLotized(currentXY, MatricialConfiguration.HOUSE_SIDE_MINIMUN_SIZE,
							MatricialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2, direction);
					if (lotizable) {
						createDoubleLot(currentXY, MatricialConfiguration.HOUSE_SIDE_MINIMUN_SIZE,
								MatricialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE, direction, seed % 4);
						currentXY = MatricialMapHelper.moveKeyByOffsetAndDirection(currentXY,
								MatricialConfiguration.HOUSE_SIDE_MINIMUN_SIZE, direction);
						seed += 2;
					} else {
						currentXY = MatricialMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, direction);
					}
				} else {
					lotizable = canBeNonOrthogonallyLotized(currentXY, finalXY,
							MatricialConfiguration.HOUSE_SIDE_MINIMUN_SIZE, MatricialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE,
							direction, gradient);
					if (lotizable) {
						createNonOrthogonalLot(currentXY, finalXY, MatricialConfiguration.HOUSE_SIDE_MINIMUN_SIZE,
								MatricialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE, direction, gradient, seed % 4);
						currentXY = MatricialMapHelper.moveKeyByGradientAndOffset(currentXY, finalXY,
								MatricialConfiguration.HOUSE_SIDE_MINIMUN_SIZE, gradient, offset, direction);
						seed += 2;
					} else {
						currentXY = MatricialMapHelper.moveKeyByGradientAndOffset(currentXY, finalXY, 1, gradient, offset,
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
							if (landPointisOnMap(MatricialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
								String type = findPoint(MatricialMapHelper.formKey((int) variation[0], (int) variation[1]))
										.getType();
								if (type.equals(MatricialConfiguration.CLUSTER_ENTRANCE_MARK))
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
							if (landPointisOnMap(MatricialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
								String type = findPoint(MatricialMapHelper.formKey((int) variation[0], (int) variation[1]))
										.getType();
								if (type.equals(MatricialConfiguration.CLUSTER_ENTRANCE_MARK))
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
					if (landPointisOnMap(MatricialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
						String type = findPoint(MatricialMapHelper.formKey((int) variation[0], (int) variation[1])).getType();
						if (type.equals(MatricialConfiguration.CLUSTER_ENTRANCE_MARK))
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
							if (landPointisOnMap(MatricialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
								findPoint(MatricialMapHelper.formKey((int) variation[0], (int) variation[1])).setType("" + seed);
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
							if (landPointisOnMap(MatricialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
								findPoint(MatricialMapHelper.formKey((int) variation[0], (int) variation[1])).setType("" + seed);
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
					if (landPointisOnMap(MatricialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
						findPoint(MatricialMapHelper.formKey((int) variation[0], (int) variation[1])).setType("" + seed);
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

			if (distance < MatricialConfiguration.CLUSTER_ENTRANCE_SIZE) {
				return;
			}

			if (beginXY[1] < finalXY[1]) {
				tbXY[1] = (int) (beginXY[1] + (distance) / 2);
				tbXY[0] = (int) ((tbXY[1] - offset) / gradient);

				tfXY[1] = (int) (finalXY[1] - (distance / 2 - MatricialConfiguration.CLUSTER_ENTRANCE_SIZE));
				tfXY[0] = (int) ((tfXY[1] - offset) / gradient);
			} else {
				isUpDown = true;
				tbXY[1] = (int) (finalXY[1] + (distance) / 2);
				tbXY[0] = (int) ((tbXY[1] - offset) / gradient);

				tfXY[1] = (int) (beginXY[1] - (distance / 2 - MatricialConfiguration.CLUSTER_ENTRANCE_SIZE));
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

			for (int j = 0; j < MatricialConfiguration.CLUSTER_ENTRANCE_SIZE; j++) {
				currentXY[0] = tbXY[0] + j;
				currentXY[1] = (int) (gradient * currentXY[0] + offset);
				// orthogonalOffset = -orthogonalGradient * currentXY[0] +
				// currentXY[1];

				if (isUpDown && (oldVariation != -1) && ((oldVariation + 1) < currentXY[1])) {
					// we take the reminder y that are needed for an exact answer
					for (int w = oldVariation + 1; w < currentXY[1]; w++) {
						for (int i = 0; i < 2 * MatricialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE; i++) {
							variation[0] = currentXY[0] + (!inverse ? i : -i);
							variation[1] = w;
							// orthogonalGradient * variation[0] + orthogonalOffset;
							if (landPointisOnMap(MatricialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
								findPoint(MatricialMapHelper.formKey((int) variation[0], (int) variation[1]))
										.setType(MatricialConfiguration.CLUSTER_ENTRANCE_MARK);
							}
						}
						countYFactor++;
						if (countYFactor == MatricialConfiguration.CLUSTER_ENTRANCE_SIZE) {
							break;
						}
					}
				} else if ((oldVariation != -1) && ((oldVariation - 1) > currentXY[1])) {
					// we take the reminder y that are needed for an exact answer
					for (int w = currentXY[1] + 1; w < oldVariation; w++) {
						for (int i = 0; i < 2 * MatricialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE; i++) {
							variation[0] = currentXY[0] + (!inverse ? i : -i);
							variation[1] = w;
							// orthogonalGradient * variation[0] + orthogonalOffset;
							if (landPointisOnMap(MatricialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
								findPoint(MatricialMapHelper.formKey((int) variation[0], (int) variation[1]))
										.setType(MatricialConfiguration.CLUSTER_ENTRANCE_MARK);
							}
						}
						countYFactor++;
						if (countYFactor == MatricialConfiguration.CLUSTER_ENTRANCE_SIZE) {
							break;
						}
					}
				}

				if (countYFactor == MatricialConfiguration.CLUSTER_ENTRANCE_SIZE) {
					break;
				}

				// We need to find the furthest
				for (int i = 0; i < 2 * MatricialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE; i++) {
					variation[0] = currentXY[0] + (!inverse ? i : -i);
					variation[1] = currentXY[1];
					// orthogonalGradient * variation[0] + orthogonalOffset;
					if (landPointisOnMap(MatricialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
						findPoint(MatricialMapHelper.formKey((int) variation[0], (int) variation[1]))
								.setType(MatricialConfiguration.CLUSTER_ENTRANCE_MARK);
					}
				}
				countYFactor++;
				if (countYFactor == MatricialConfiguration.CLUSTER_ENTRANCE_SIZE) {
					break;
				}
				oldVariation = (int) currentXY[1];
			}
		}
		private int[] createNonOrthogonalWalkRoute(Integer initialPoint, Integer finalPoint, int[] beginXY, boolean inverse,Double gradient){
			double offset = -gradient * beginXY[0] + beginXY[1];
			double orthogonalGradient = -1 / gradient;
			double orthogonalOffset = -orthogonalGradient * beginXY[0] + beginXY[1];
			int[] initialXY = MatricialMapHelper.breakKey(initialPoint);
			int[] finalXY = MatricialMapHelper.breakKey(finalPoint);
			boolean down = false;
			if (inverse) {
				if (finalXY[0] - initialXY[0] > 0) {
					// EAST
					down = true;
					beginXY[0] = finalXY[0] - MatricialConfiguration.WALK_BRANCH_SIZE;
					beginXY[1] = (int) (gradient * beginXY[0] + offset);
				} else {
					// WEST
					down = false;
					beginXY[0] = initialXY[0] - MatricialConfiguration.WALK_BRANCH_SIZE;
					beginXY[1] = (int) (gradient * beginXY[0] + offset);
				}
			}else {
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
			for (int j = 0; j < MatricialConfiguration.WALK_BRANCH_SIZE; j++) {
				currentXY[0] = beginXY[0] + j;
				currentXY[1] = (int) (gradient * currentXY[0] + offset);
				orthogonalOffset = -orthogonalGradient * currentXY[0] + currentXY[1];

				for (int i = 0; i < 2 * MatricialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE; i++) {
					variation[1] = currentXY[1] + (!down ? i : -i);
					variation[0] = (variation[1] - orthogonalOffset) / orthogonalGradient;
					if (landPointisOnMap(MatricialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
						findPoint(MatricialMapHelper.formKey((int) variation[0], (int) variation[1]))
								.setType(MatricialConfiguration.WALK_MARK);
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
			upperMiddle[1] = ((currentXY[1] + finalXY[1]) / 2) + (MatricialConfiguration.CLUSTER_ENTRANCE_SIZE) / 2;

			int lowerMiddle[] = new int[2];
			lowerMiddle[0] = currentXY[0];
			lowerMiddle[1] = ((currentXY[1] + finalXY[1]) / 2) - (MatricialConfiguration.CLUSTER_ENTRANCE_SIZE) / 2;

			if ((direction == MatricialConstants.NORTH) && (lowerMiddle[1] < currentXY[1] || upperMiddle[1] > finalXY[1]))
				return;
			else if ((direction == MatricialConstants.SOUTH)
					&& (lowerMiddle[1] > currentXY[1] || upperMiddle[1] < finalXY[1]))
				return;
			createInsideClusterRoute(upperMiddle, MatricialMapHelper.formKey(lowerMiddle[0], lowerMiddle[1]), direction,
					MatricialConfiguration.WALK_BRANCH, MatricialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2,
					MatricialConfiguration.CLUSTER_ENTRANCE_MARK);
		}
		
		private MatricialBuilding createWalkRoute(int[] currentXY, boolean isInverse, int direction, int rotation) {
			if (isInverse) {
				return createInsideClusterRoute(currentXY,
						MatricialMapHelper.moveKeyByOffsetAndDirection(MatricialMapHelper.formKey(currentXY[0], currentXY[1]),
								MatricialConfiguration.WALK_BRANCH_SIZE, MatricialDirectionHelper.oppositeDirection(direction)),
						direction, MatricialConfiguration.WALK_BRANCH, MatricialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2,
						MatricialConfiguration.WALK_MARK);
			} else {
				return createInsideClusterRoute(currentXY,
						MatricialMapHelper.moveKeyByOffsetAndDirection(MatricialMapHelper.formKey(currentXY[0], currentXY[1]),
								MatricialConfiguration.WALK_BRANCH_SIZE, direction),
						direction, MatricialConfiguration.WALK_BRANCH, MatricialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2,
						MatricialConfiguration.WALK_MARK);
			}
		}
		
		private boolean canBeLotized(int[] currentXY, int houseSideSize, int doublehouseDepthSize, int direction) {
			switch (direction) {
			case MatricialConstants.EAST:
				for (int i = currentXY[0]; i < currentXY[0] + houseSideSize; i++) {
					for (int j = currentXY[1]; j > currentXY[1] - doublehouseDepthSize; j--) {
						if (landPointisOnMap(MatricialMapHelper.formKey(i, j))) {
							String type = findPoint(MatricialMapHelper.formKey(i, j)).getType();
							if (type.equals(MatricialConfiguration.CLUSTER_ENTRANCE_MARK)) {
								return false;
							}
						} else {
							return false;
						}
					}
				}
				break;
			case MatricialConstants.NORTH:
				for (int i = currentXY[1]; i < currentXY[1] + houseSideSize; i++) {
					for (int j = currentXY[0]; j < currentXY[0] + doublehouseDepthSize; j++) {
						if (landPointisOnMap(MatricialMapHelper.formKey(j, i))) {
							String type = findPoint(MatricialMapHelper.formKey(j, i)).getType();
							if (type.equals(MatricialConfiguration.CLUSTER_ENTRANCE_MARK)) {
								return false;
							}
						} else {
							return false;
						}
					}
				}
				break;
			case MatricialConstants.WEST:
				for (int i = currentXY[0]; i >= currentXY[0] - houseSideSize; i--) {
					for (int j = currentXY[1]; j < currentXY[1] + doublehouseDepthSize; j++) {
						if (landPointisOnMap(MatricialMapHelper.formKey(i, j))) {
							String type = findPoint(MatricialMapHelper.formKey(i, j)).getType();
							if (type.equals(MatricialConfiguration.CLUSTER_ENTRANCE_MARK)) {
								return false;
							}
						} else {
							return false;
						}
					}
				}
				break;
			case MatricialConstants.SOUTH:
				for (int i = currentXY[1]; i >= currentXY[1] - houseSideSize; i--) {
					for (int j = currentXY[0]; j > currentXY[0] - doublehouseDepthSize; j--) {
						if (landPointisOnMap(MatricialMapHelper.formKey(j, i))) {
							String type = findPoint(MatricialMapHelper.formKey(j, i)).getType();
							if (type.equals(MatricialConfiguration.CLUSTER_ENTRANCE_MARK)) {
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
			case MatricialConstants.EAST:
				for (int i = currentXY[0]; i < currentXY[0] + houseSideMinimunSize; i++) {
					for (int j = currentXY[1]; j > currentXY[1] - houseDepthMinimunSize; j--) {
						findPoint(MatricialMapHelper.formKey(i, j)).setType("" + serialNumber);
					}
					for (int j = currentXY[1] - (houseDepthMinimunSize + 1); j > currentXY[1]
							- 2 * houseDepthMinimunSize; j--) {
						findPoint(MatricialMapHelper.formKey(i, j)).setType("" + (serialNumber + 1));
					}
				}
				break;
			case MatricialConstants.NORTH:
				for (int i = currentXY[1]; i < currentXY[1] + houseSideMinimunSize; i++) {
					for (int j = currentXY[0]; j < currentXY[0] + houseDepthMinimunSize; j++) {
						findPoint(MatricialMapHelper.formKey(j, i)).setType("" + serialNumber);
					}
					for (int j = currentXY[0] + houseDepthMinimunSize + 1; j < currentXY[0]
							+ 2 * houseDepthMinimunSize; j++) {
						findPoint(MatricialMapHelper.formKey(j, i)).setType("" + (serialNumber + 1));
					}
				}
				break;
			case MatricialConstants.WEST:
				for (int i = currentXY[0]; i >= currentXY[0] - houseSideMinimunSize; i--) {
					for (int j = currentXY[1]; j < currentXY[1] + houseDepthMinimunSize; j++) {
						findPoint(MatricialMapHelper.formKey(i, j)).setType("" + serialNumber);
					}
					for (int j = currentXY[1] + houseDepthMinimunSize + 1; j < currentXY[1]
							+ 2 * houseDepthMinimunSize; j++) {
						findPoint(MatricialMapHelper.formKey(i, j)).setType("" + (serialNumber + 1));
					}
				}
				break;
			case MatricialConstants.SOUTH:
				for (int i = currentXY[1]; i >= currentXY[1] - houseSideMinimunSize; i--) {
					for (int j = currentXY[0]; j > currentXY[0] - houseDepthMinimunSize; j--) {
						findPoint(MatricialMapHelper.formKey(j, i)).setType("" + serialNumber);
					}
					for (int j = currentXY[0] - (houseDepthMinimunSize + 1); j > currentXY[0]
							- 2 * houseDepthMinimunSize; j--) {
						findPoint(MatricialMapHelper.formKey(j, i)).setType("" + (serialNumber + 1));
					}
				}
				break;
			}
		}
		
		private MatricialBuilding createInsideClusterRoute(int[] currentXY, int finalKey, int direction, int type, int depth,
				String markType) {
			int[] finalXY = MatricialMapHelper.breakKey(finalKey);
			int lower, upper;
			MatricialBuilding matricialBuilding = new MatricialBuilding();

			if ((direction == MatricialConstants.NORTH) || (direction == MatricialConstants.SOUTH)) {
				if (currentXY[1] > finalXY[1]) {
					lower = finalXY[1];
					upper = currentXY[1];
				} else {
					lower = currentXY[1];
					upper = finalXY[1];
				}

				if (direction == MatricialConstants.SOUTH) {
					for (int i = lower; i < upper; i++) {
						for (int j = currentXY[0]; j > currentXY[0] - depth; j--) {
							findPoint(MatricialMapHelper.formKey(j, i)).setType(markType);
						}
					}
				} else {
					for (int i = lower; i < upper; i++) {
						for (int j = currentXY[0]; j < currentXY[0] + depth; j++) {
							findPoint(MatricialMapHelper.formKey(j, i)).setType(markType);
						}
					}
				}
			} else if ((direction == MatricialConstants.EAST) || (direction == MatricialConstants.WEST)) {
				if (currentXY[0] > finalXY[0]) {
					lower = finalXY[0];
					upper = currentXY[0];
				} else {
					lower = currentXY[0];
					upper = finalXY[0];
				}

				if (direction == MatricialConstants.EAST) {
					for (int i = lower; i < upper; i++) {
						for (int j = currentXY[1]; j > currentXY[1] - depth; j--) {
							findPoint(MatricialMapHelper.formKey(i, j)).setType(markType);
						}
					}
				} else {
					for (int i = lower; i < upper; i++) {
						for (int j = currentXY[1]; j < currentXY[1] + depth; j++) {
							findPoint(MatricialMapHelper.formKey(i, j)).setType(markType);
						}
					}
				}
			}

			matricialBuilding.setType(markType);
			matricialBuilding.setNumber(0);
			return matricialBuilding;
		}
		
		public String stringify() {
			String mapString = "";
			for (int j = pointsy - 1; j >= 0; j--) {
				String type = getLandPoint(MatricialMapHelper.formKey(0, j)).getType();
				int repetitions = 1;
				for (int i = 1; i < pointsx; i++) {
					if (type.equals(getLandPoint(MatricialMapHelper.formKey(i, j)).getType())) {
						repetitions++;
					} else {
						mapString += type + "" + repetitions + ",";
						repetitions = 1;
						type = getLandPoint(MatricialMapHelper.formKey(i, j)).getType();
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
				String type = getLandPoint(MatricialMapHelper.formKey(0, j)).getType();
				if (mapMap.get(type) == null) {
					mapMap.put(type, new ArrayList<>());
				}
				int i;
				for (i = 1; i < pointsx; i++) {
					if (!type.equals(getLandPoint(MatricialMapHelper.formKey(i, j)).getType())) {
						mapMap.get(type).add(MatricialMapHelper.formKey(i - 1, j));
						type = getLandPoint(MatricialMapHelper.formKey(i, j)).getType();
						if (mapMap.get(type) == null) {
							mapMap.put(type, new ArrayList<>());
						}
						mapMap.get(type).add(MatricialMapHelper.formKey(i, j));
					}
				}
				mapMap.get(type).add(MatricialMapHelper.formKey(i, j));
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
						  /*if(getLandPoint(MatricialMapHelper.formKey(i, j)).getType().equals("n")){
							 writer.print("e");
						 }
						 else */
						writer.print(getLandPoint(MatricialMapHelper.formKey(i, j)).getType());
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
						  /*if(getLandPoint(MatricialMapHelper.formKey(i, j)).getType().equals("n")){
							 writer.print("e");
						 }
						 else */
						writer.print(getLandPoint(MatricialMapHelper.formKey(i, j)).getType());
					}
					writer.println();
				}
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public List<Integer> mostLargeSide(){
			//we gonna insert both extrems coordinates of the largest side
			//meuclidean method
			List<Integer>axisLongSide=new ArrayList<>();
			double basicDist,finalDist;
			List<Integer> finalList;
			finalDist=0.0;
			for(int i=0,j=0;j<(coordinates.size()/2)-1;i+=2,j++){
				basicDist= (coordinates.get(i+2)-coordinates.get(i))*(coordinates.get(i+2)-coordinates.get(i))+
						(coordinates.get(i+3)-coordinates.get(i+1))*(coordinates.get(i+3)-coordinates.get(i+1));
				
				basicDist=Math.sqrt(basicDist);	
				
				if(basicDist>finalDist){
					axisLongSide=new ArrayList<>();
					axisLongSide.add(coordinates.get(i));
					axisLongSide.add(coordinates.get(i+1));
					axisLongSide.add(coordinates.get(i+2));
					axisLongSide.add(coordinates.get(i+3));
					finalDist=basicDist;
				}
			}
			//ultima distancia
			basicDist= (coordinates.get(coordinates.size()-2)-coordinates.get(coordinates.size()-4))*(coordinates.get(coordinates.size()-2)-coordinates.get(coordinates.size()-4))+
					(coordinates.get(coordinates.size()-1)-coordinates.get(coordinates.size()-3))*(coordinates.get(coordinates.size()-1)-coordinates.get(coordinates.size()-3));
			basicDist=Math.sqrt(basicDist);	
			
			if(basicDist>finalDist){
				axisLongSide=new ArrayList<>();
				axisLongSide.add(coordinates.get(coordinates.size()-4));
				axisLongSide.add(coordinates.get(coordinates.size()-3));
				axisLongSide.add(coordinates.get(coordinates.size()-2));
				axisLongSide.add(coordinates.get(coordinates.size()-1));
				finalDist=basicDist;
			}

			return axisLongSide;
		}
	public int[] createMainRoute(int refPoint,int centroid, List<Integer> polygon){
		int[] auxPoints = new int[2];
		int pointVerif;
		int k = 0;
		for(int i= 0;i < polygon.size();i++){
			pointVerif = findIntersectionPointIntoTwoStraight
					(refPoint,centroid , polygon.get(i % polygon.size()), polygon.get((i+1) % polygon.size()), true);
			if(pointVerif != -1){
				auxPoints[k] = pointVerif;
				k++;
			}
		}
	return auxPoints; 
	}
	
	public int findIntersectionPointIntoTwoStraight(int rect1Ini, int rect1End, int rect2Ini, int rect2End, boolean belong){
		int pointSolution =-1;
		int xyRec1Ini[] = MatricialMapHelper.breakKey(rect1Ini);
		int xyRec1End[] = MatricialMapHelper.breakKey(rect1End);
		int xyRec2Ini[] = MatricialMapHelper.breakKey(rect2Ini);
		int xyRec2End[] = MatricialMapHelper.breakKey(rect2End);
		
		int underscore1 = (xyRec1End[0] - xyRec1Ini[0]);
		int underscore2 = (xyRec2End[0] - xyRec2Ini[0]);
		double gradient1 = 0;
		double gradient2 = 0;
		double b1 = 0;
		double b2 = 0;
		
		if(underscore1 == 0 && underscore2 == 0) return pointSolution; // would be the same straight or paralell
		if(underscore1 != 0){
			gradient1 = (xyRec1End[1] - xyRec1Ini[1]) * 1.0 / underscore1;
			b1 = (xyRec1Ini[0]*xyRec1End[1] -(xyRec1End[0]*xyRec1Ini[1]))/(xyRec1Ini[0] -xyRec1End[0]);
		}
		if(underscore2 != 0){
			gradient2 = (xyRec2End[1] - xyRec2Ini[1]) * 1.0 / underscore2;
			b2 = (xyRec2Ini[0]*xyRec2End[1] -(xyRec2End[0]*xyRec2Ini[1]))/(xyRec2Ini[0] -xyRec2End[0]);
		}
		if((gradient1 == 0 && gradient2 == 0)|| (gradient1 == gradient2)) return pointSolution;  //would be the same straight or paralell
		
		
		if (underscore1 == 0 && underscore2 != 0) {
			int lower = xyRec1Ini[1] < xyRec1End[1] ? xyRec1Ini[1] : xyRec1End[1];
			int upper = xyRec1Ini[1] > xyRec1End[1] ? xyRec1Ini[1] : xyRec1End[1];	
			double yAux = xyRec1End[0]*gradient2 + b2;
				if(((lower-2) <= yAux && yAux <= (upper+2))|| !belong){
					
					MatricialMapHelper.round(yAux);
					pointSolution =  MatricialMapHelper.formKey( xyRec1End[0],(int)yAux);
					
					return pointSolution;
				}

				
		
		}
		if (underscore2 == 0 && underscore1 != 0) {
			int lower = xyRec2Ini[1] < xyRec2End[1] ? xyRec2Ini[1] : xyRec2End[1];
			int upper = xyRec2Ini[1] > xyRec2End[1] ? xyRec2Ini[1] : xyRec2End[1];
				double yAux = xyRec2End[0]*gradient1 + b1;
				
				if(((lower-2) <= yAux && yAux <= (upper+2))|| !belong){ //verify if the point belong to the straight
					MatricialMapHelper.round(yAux);
					pointSolution =  MatricialMapHelper.formKey(xyRec2End[0],(int)yAux);
					return pointSolution;
				}
				
		}
		if (underscore1 != 0 && underscore2 != 0) {
				if(gradient1 == 0){
					int lower = xyRec1Ini[0] < xyRec1End[0] ? xyRec1Ini[0] : xyRec1End[0];
					int upper = xyRec1Ini[0] > xyRec1End[0] ? xyRec1Ini[0] : xyRec1End[0];
					double xAux = (xyRec1End[1]- b2)/gradient2;
					if(((lower-2)) <= xAux && xAux <= (upper+2)|| !belong){
						
						MatricialMapHelper.round(xAux);
						pointSolution =  MatricialMapHelper.formKey( (int)xAux, xyRec1End[1]);
						
						return pointSolution;
					}
					
				}
				if(gradient2 == 0){
					int lower = xyRec2Ini[0] < xyRec2End[0] ? xyRec2Ini[0] : xyRec2End[0];
					int upper = xyRec2Ini[0] > xyRec2End[0] ? xyRec2Ini[0] : xyRec2End[0];
					double xAux = (xyRec2End[1]- b1)/gradient1;
					if(((lower-2) <= xAux && xAux <= (upper+2))|| !belong){
						
						MatricialMapHelper.round(xAux);
						pointSolution =  MatricialMapHelper.formKey((int)xAux,xyRec2End[1]);
						
						return pointSolution;
					}
				}
			
			int lowerx = xyRec2Ini[0] < xyRec2End[0] ? xyRec2Ini[0] : xyRec2End[0];
			int upperx = xyRec2Ini[0] > xyRec2End[0] ? xyRec2Ini[0] : xyRec2End[0];
			int lowery = xyRec2Ini[1] < xyRec2End[1] ? xyRec2Ini[1] : xyRec2End[1];
			int uppery = xyRec2Ini[1] > xyRec2End[1] ? xyRec2Ini[1] : xyRec2End[1];
			double xAux = (b2 -b1)/(gradient1- gradient2);
			double yAux = gradient1*(xAux) + b1;		
			
				
				if(((lowerx-2) <= xAux && xAux <= (upperx+2) && (lowery-2) <= yAux && yAux <= (uppery+2)) || !belong){//verify if the point belong to the straight because it wouldn't be include in both
					MatricialMapHelper.round(xAux);
					MatricialMapHelper.round(yAux);
					pointSolution =  MatricialMapHelper.formKey((int)xAux, (int)yAux);	
				
					return pointSolution;
				}
				
		
		}
		
		return pointSolution; //if dosnt fint, or dont exist, return -1
	}
	
	
	public void createACustomRoute(int inicialPoint, int finalPoint,int size, String markType) {
		
		int xyInitial[] = MatricialMapHelper.breakKey(inicialPoint);
		int xyFinal[] = MatricialMapHelper.breakKey(finalPoint);
		int cont= 1;
		int sign = 1; 
		int aux = 0;
		int underscore = (xyFinal[0] - xyInitial[0]);
		if (underscore == 0) {
			
			createALine(inicialPoint,finalPoint,markType);
			for (int w = 0; w <= size-1; w++) {
				int auxInitPoint = MatricialMapHelper.formKey(xyInitial[0] + cont*sign, xyInitial[1]) ;
				int auxFinPoint = MatricialMapHelper.formKey(xyFinal[0] + cont*sign , xyFinal[1]);
				if(!landPointisOnMap(auxInitPoint) || !landPointisOnMap(auxFinPoint)){
					//System.out.println("point out of range");
					return;
				}
				createALine(auxInitPoint,auxFinPoint,markType);
				sign=sign*(-1);
				if(aux == 1){
					cont++;
					aux=0;
					continue;
				}
				aux++;
			}
			return;
		}

		double gradient = (xyFinal[1] - xyInitial[1]) * 1.0 / underscore;
		// 2nd, gradient=0; straight in the X axis
		if (gradient == 0) {
			createALine(inicialPoint,finalPoint,markType);
			for (int w = 0; w < size; w++) {
				int auxInitPoint = MatricialMapHelper.formKey(xyInitial[0] , xyInitial[1] + cont*sign) ;
				int auxFinPoint = MatricialMapHelper.formKey(xyFinal[0]   , xyFinal[1] + cont*sign);	
				if(!landPointisOnMap(auxInitPoint) || !landPointisOnMap(auxFinPoint)){
					//System.out.println("point out of range");
					return;
				}
				createALine(auxInitPoint,auxFinPoint,markType);
				sign=sign*(-1);
				if(aux == 1){
					cont++;
					aux=0;
					continue;
				}
				aux++;
			}
			return;
		}
		// 3nd the gradient is positive/negative.
		double contGradient = (-1)*(1.0/gradient);
		double b1 = xyInitial[1] - contGradient * xyInitial[0];
		double b2 = xyFinal[1] - contGradient * xyFinal[0];
		int x1,x2,y1,y2;
		createALine(inicialPoint,finalPoint,markType);
		for (int w = 0; w <= size-1; w++) {
			if(Math.abs(xyFinal[0]-xyInitial[0]) <  Math.abs(xyFinal[1]-xyInitial[1])){
				 x1 = xyInitial[0] + cont*sign;
				 x2 = xyFinal[0] + cont*sign;
				 y1 =  (int)MatricialMapHelper.round(contGradient * x1 + b1);
				 y2 =  (int)MatricialMapHelper.round(contGradient * x2 + b2);
			}else{
				 y1 = xyInitial[1] + cont*sign;
				 y2 = xyFinal[1] + cont*sign;
				 x1 =  (int)MatricialMapHelper.round((y1 - b1)/contGradient);
				 x2 =  (int)MatricialMapHelper.round((y2 - b2)/contGradient);
				
			}
	
			int auxInitPoint = MatricialMapHelper.formKey(x1, y1) ;
			int auxFinPoint = MatricialMapHelper.formKey( x2, y2);	
			if(!landPointisOnMap(auxInitPoint) || !landPointisOnMap(auxFinPoint)){
				//System.out.println("point out of range");
				return;
			}
			if((size/2) <= distanceOfPointToPoint(auxInitPoint,inicialPoint)){
				createALine(auxInitPoint,auxFinPoint,markType);
				break;
			}
			createALine(auxInitPoint,auxFinPoint,markType);
					
			sign=sign*(-1);
			if(aux == 1){
				cont++;
				aux=0;
				continue;
			}
			aux++;
		}
		return;
		
		
	}

	public void createALine(int inicialPoint, int finalPoint, String markType) {
		int xyInitial[] = MatricialMapHelper.breakKey(inicialPoint);
		int xyFinal[] = MatricialMapHelper.breakKey(finalPoint);
	
		int underscore = (xyFinal[0] - xyInitial[0]);
		if (underscore == 0) {
			int lower = xyInitial[1] < xyFinal[1] ? xyInitial[1] : xyFinal[1];
			int upper = xyInitial[1] > xyFinal[1] ? xyInitial[1] : xyFinal[1];
	
			for (int w = lower; w <= upper; w++) {
				getLandPoint(MatricialMapHelper.formKey(xyInitial[0], w)).setType(markType);
			}
			return;
		}
	
		double gradient = (xyFinal[1] - xyInitial[1]) * 1.0 / underscore;
		// 2nd, gradient=0; straight in the X axis
		int lowerx = xyInitial[0] < xyFinal[0] ? xyInitial[0] : xyFinal[0];
		int upperx = xyInitial[0] > xyFinal[0] ? xyInitial[0] : xyFinal[0];
		if (gradient == 0) {
			for (int w = lowerx; w <= upperx; w++) {
				if (landPointisOnMap(MatricialMapHelper.formKey(xyInitial[0], w))) {
					getLandPoint(MatricialMapHelper.formKey(w, xyInitial[1])).setType(markType);
				}
			}
			return;
		}
		int lowery = xyInitial[1] < xyFinal[1] ? xyInitial[1] : xyFinal[1];
		int uppery = xyInitial[1] > xyFinal[1] ? xyInitial[1] : xyFinal[1];
	
		double b = xyFinal[1] - gradient * xyFinal[0];
		// 3nd the gradient is positive/negative.
		for (int w = lowerx; w <= upperx; w++) {
			float y = MatricialMapHelper.round(gradient * w + b);
				if (landPointisOnMap(MatricialMapHelper.formKey(xyInitial[0], w))) {
					getLandPoint(MatricialMapHelper.formKey(w, (int) y)).setType(markType);
				}
		}
		for (int w = lowery; w <= uppery; w++) {
			float x = MatricialMapHelper.round( (w - b)/gradient);
			if (x == (int) x) // quick and dirty convertion check
			{
				if (landPointisOnMap(MatricialMapHelper.formKey(xyInitial[0], w))) {
					getLandPoint(MatricialMapHelper.formKey((int) x, w)).setType(markType);
				}
			}
		}
		
		
	}
	public double distanceOfPointToPoint(int pointi, int pointf) {
		int xyPointF[] = MatricialMapHelper.breakKey(pointf);
		int xyPointI[] = MatricialMapHelper.breakKey(pointi);
		return Math.sqrt(Math.pow(xyPointF[0] - xyPointI[0], 2) + Math.pow(xyPointF[1] - xyPointI[1], 2));
	}	
	
	public int findPointOnStreightToDistance(int pointIni,int pointEnd,double distance){
		int xyInitial[] = MatricialMapHelper.breakKey(pointIni);
		int xyFinal[] = MatricialMapHelper.breakKey(pointEnd);
		int underscore = (xyFinal[0] - xyInitial[0]);
		double gradient = 0;
		double b = 0;
		int pointSolution = -1;
		
		if (underscore == 0) {	
				if(xyInitial[1] < xyFinal[1] ){ 
					pointSolution =  MatricialMapHelper.formKey(xyFinal[0],(xyInitial[1] + (int)distance));		
				}else{
					pointSolution =  MatricialMapHelper.formKey(xyFinal[0],(xyInitial[1] - (int)distance));		
				}
				/*
				if(!landPointisOnMap(pointSolution)){
					//System.out.println("point out of range");
					return -1;
				}
				*/
				return pointSolution;
		}else{
			gradient = (xyFinal[1] - xyInitial[1]) * 1.0 / underscore;
			b = (xyInitial[0]*xyFinal[1] -(xyFinal[0]*xyInitial[1]))/(xyInitial[0] -xyFinal[0]);
			if(gradient == 0){
				if(xyInitial[0] < xyFinal[0]){ //verify if the point belong to the straight
					pointSolution =  MatricialMapHelper.formKey( (xyInitial[0] + (int)distance),xyFinal[1] );		
				}else{
					pointSolution =  MatricialMapHelper.formKey( (xyInitial[0] - (int)distance),xyFinal[1] );	
				}
				/*
				if(!landPointisOnMap(pointSolution)){
					//System.out.println("point out of range");
					return -1;
				}
				*/
				return pointSolution;
			}
			int sign,init,end;
			boolean onX = false;
			if(Math.abs(xyFinal[0]-xyInitial[0]) <  Math.abs(xyFinal[1]-xyInitial[1])){
			//do on Y
				init = xyInitial[1];
				end = xyFinal[1];
				if(xyInitial[1] < xyFinal[1]) sign = 1;
				else{
					sign = -1;
				}
			}else{
			//do on X
				init = xyInitial[0];
				end = xyFinal[0];
				if(xyInitial[0] < xyFinal[0] ) sign = 1;
				else{ 
					sign = -1;
				}
				onX = true;
			}
			if(sign == 1){
				for(int i = init; i <= end; i++){
					double x,y;
					if(onX){
						x = i;
						y= gradient*x + b;
					}else{
						y= i;
						x = (y-b)/gradient; 
					}
					MatricialMapHelper.round(x);
					MatricialMapHelper.round(y);
					pointSolution =  MatricialMapHelper.formKey( (int)x,(int)y );
					double auxDist = distanceOfPointToPoint(pointIni,pointSolution);
					if(auxDist >= distance){
						/*if(!landPointisOnMap(pointSolution)){
							//System.out.println("point out of range");
							return -1;
						}
						*/
						return pointSolution;
					}
				}
			}else if(sign == -1){
				for(int i = init; i >= end; i--){
					double x,y;
					if(onX){
						x = i;
						y= gradient*x + b;
					}else{
						y= i;
						x = (y-b)/gradient; 
					}
					MatricialMapHelper.round(x);
					MatricialMapHelper.round(y);
					pointSolution =  MatricialMapHelper.formKey( (int)x,(int)y );
					double auxDist = distanceOfPointToPoint(pointIni,pointSolution);
					if(auxDist >= distance){
						/*if(!landPointisOnMap(pointSolution)){
							//System.out.println("point out of range");
							return -1;
						}*/
						return pointSolution;
					}
				}
			}
			if(!landPointisOnMap(pointSolution)){
				//System.out.println("point out of range");
				return -1;
			}
			return pointSolution;
		}	
	}
	public int findProyectionPointIntoParalelStraights(int rect1Ini, int rect1End, int pointRef, boolean belong){ ////WARNING////////
		int pointSolution = -1;
		int xyRec1Ini[] = MatricialMapHelper.breakKey(rect1Ini);
		int xyRec1End[] = MatricialMapHelper.breakKey(rect1End);
		int xyPointRef[] = MatricialMapHelper.breakKey(pointRef);
		int underscore = (xyRec1End[0] - xyRec1Ini[0]);
		double gradient = 0;
		double b = 0;
		if (underscore == 0) {
			int lower = xyRec1Ini[1] < xyRec1End[1] ? xyRec1Ini[1] : xyRec1End[1];
			int upper = xyRec1Ini[1] > xyRec1End[1] ? xyRec1Ini[1] : xyRec1End[1];	
				if((lower <= xyPointRef[1] && xyPointRef[1] <= upper)|| !belong){ //verify if the point belong to the straight
					pointSolution =  MatricialMapHelper.formKey(xyRec1Ini[0],xyPointRef[1] );		
				}
				return pointSolution;
		}else{
			gradient = (xyRec1End[1] - xyRec1Ini[1]) * 1.0 / underscore;
			b = (xyRec1Ini[0]*xyRec1End[1] -(xyRec1End[0]*xyRec1Ini[1]))/(xyRec1Ini[0] -xyRec1End[0]);
			if(gradient == 0){
				int lower = xyRec1Ini[0] < xyRec1End[0] ? xyRec1Ini[0] : xyRec1End[0];
				int upper = xyRec1Ini[0] > xyRec1End[0] ? xyRec1Ini[0] : xyRec1End[0];
				if((lower <= xyPointRef[0] && xyPointRef[0] <= upper)|| !belong){ //verify if the point belong to the straight
					pointSolution =  MatricialMapHelper.formKey( xyPointRef[0],xyRec1Ini[1] );		
				}
				return pointSolution;
			}
			double contGrad = -1*(1/gradient);
			double b2 = xyPointRef[1] - contGrad*xyPointRef[0];
			int lower = xyRec1Ini[0] < xyRec1End[0] ? xyRec1Ini[0] : xyRec1End[0];
			int upper = xyRec1Ini[0] > xyRec1End[0] ? xyRec1Ini[0] : xyRec1End[0];
			double x = (b2-b)/(gradient-contGrad);
			double y = contGrad*x + b2;
			MatricialMapHelper.round(x);
			MatricialMapHelper.round(y);
			if((lower <= x && x <= upper)|| !belong){
				pointSolution =  MatricialMapHelper.formKey( (int)x,(int)y );
			}
		}
		
		return pointSolution;
	}
	
}
