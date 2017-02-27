package models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import helpers.base.MapHelper;
import interfaces.Constants;

public class LandMap {
	private int pointsx = -1;
	private int pointsy = -1;
	private Map<Integer, LandPoint> map;

	public LandMap(int pointsx, int pointsy) {
		this.setPointsx(pointsx);
		this.setPointsy(pointsy);

		map = new HashMap<>();
		for (int i = 0; i < pointsx; i++) {
			for (int j = 0; j < pointsy; j++) {
				boolean isMapLimitXW = (i == 0) ? true : false;
				boolean isMapLimitXE = (i == (pointsx - 1)) ? true : false;

				boolean isMapLimitYS = (j == 0) ? true : false;
				boolean isMapLimitYN = (j == (pointsy - 1)) ? true : false;

				map.put(MapHelper.formKey(i, j),
						new LandPoint(i, j, isMapLimitXW, isMapLimitXE, isMapLimitYS, isMapLimitYN, false));
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

	public LandPoint getLandPoint(int pointId) {
		return map.get(pointId);
	}

	/* Helper functions */
	public LandPoint findPoint(LandPoint point, int direction) {
		int pointId = point.findNeighbour(direction);

		if (pointId == -1) {
			return null;
		} else {
			return map.get(pointId);
		}
	}

	public LandPoint findPoint(int nextPointId, int direction, int type) {
		if (nextPointId == -1) {
			return null;
		} else {
			LandPoint point = map.get(nextPointId);
			if (point.isPartOfRoute() && type == Constants.AVENUE_BRANCH) {
				return null;
			} else if (point.isPartOfSubRoute()
					&& (type == Constants.ALLEY_BRANCH || type == Constants.STREET_BRANCH)) {
				return null;
			} else {
				return point;
			}
		}
	}

	/**
	 * Only due to it already existing on a route
	 */
	public LandPoint findPoint(int randomPointInLineId, int redirection) {
		if (randomPointInLineId == -1) {
			return null;
		} else {
			return map.get(randomPointInLineId);
		}
	}

	public void pointIsOfRoute(int id, boolean partOfRoute, int currentDepth) {
		map.get(id).setPartOfRoute(partOfRoute);
		map.get(id).setType(".");
	}

	public void pointIsOfSubRoute(int id, boolean partOfSubRoute, int currentDepth) {
		map.get(id).setPartOfSubRoute(partOfSubRoute);
		map.get(id).setType(".");
	}

	public int findDistanceToLimit(int randomPointInLineId, int direction) {
		if (randomPointInLineId == -1) {
			return 0;
		} else {
			int num = 0;
			LandPoint point = map.get(randomPointInLineId);
			while (true) {
				int neighbourId = point.findNeighbour(direction);
				LandPoint neighbour = map.get(neighbourId);
				if (neighbour == null || neighbour.isPartOfRoute() || neighbour.isPartOfSubRoute()
						|| neighbour.isMapLimit(direction)) {
					return num;
				} else {
					num++;
				}
				point = neighbour;
			}
		}
	}

	/**
	 * This method marks all points that are not inside the polygon border as
	 * restricted area. This must be an ordered set of consecutive points (after
	 * all the input from android looks like that.
	 */
	public void createBorderFromPolygon(List<LandPoint> polygon) {
		// first we create the border
		for (int i = 0, j = 1; j < polygon.size(); i++, j++) {
			int underscore = (polygon.get(j).getX() - polygon.get(i).getX());
			// there are three gradient cases.
			// 1st UNDEFINED = (get(j).getX()-get(i).getX()); straight Y axis
			if (underscore == 0) {
				int lower = polygon.get(i).getY() < polygon.get(j).getY() ? polygon.get(i).getY()
						: polygon.get(j).getY();
				int upper = polygon.get(i).getY() > polygon.get(j).getY() ? polygon.get(i).getY()
						: polygon.get(j).getY();
				for (int w = lower; w < upper; w++) {
					getLandPoint(MapHelper.formKey(polygon.get(i).getX(), w)).setType(Constants.POLYGON_LIMIT);
				}
				continue;
			}

			double gradient = (polygon.get(j).getY() - polygon.get(i).getY()) * 1.0 / underscore;
			// 2nd, gradient=0; straight in the X axis
			int lower = polygon.get(i).getX() < polygon.get(j).getX() ? polygon.get(i).getX() : polygon.get(j).getX();
			int upper = polygon.get(i).getX() > polygon.get(j).getX() ? polygon.get(i).getX() : polygon.get(j).getX();
			if (gradient == 0) {
				for (int w = lower; w < upper; w++) {
					getLandPoint(MapHelper.formKey(w, polygon.get(i).getY())).setType(Constants.POLYGON_LIMIT);
				}
				continue;
			}

			double b = polygon.get(j).getY() - gradient * polygon.get(j).getX();
			// 3nd the gradient is positive/negative.
			for (int w = lower; w <= upper; w++) {
				float y = MapHelper.round(gradient * w + b);
				if (y == (int) y) // quick and dirty convertion check
				{
					getLandPoint(MapHelper.formKey(w, (int) y)).setType(Constants.POLYGON_LIMIT);
				}
			}
		}
		// we fill everything outside of it with Xs
		for (int x = 0; x < pointsx; x++) {
			int count = 0;
			int pInitialLimit = -1;
			boolean reversed = false;
			for (int y = 0; y < pointsy; y++) {
				if (getLandPoint(MapHelper.formKey(x, y)).getType() == Constants.POLYGON_LIMIT) {
					count++;
					pInitialLimit = pInitialLimit == -1 ? y : pInitialLimit;
				}

				switch (count) {
				case 0:
					getLandPoint(MapHelper.formKey(x, y)).setType(Constants.OUTSIDE_POLYGON);
					break;
				case 1:
					if (getLandPoint(MapHelper.formKey(x, y)).getType() != Constants.POLYGON_LIMIT) {
						getLandPoint(MapHelper.formKey(x, y)).setType(Constants.OUTSIDE_POLYGON);
					}
					break;
				case 2:
					if (!reversed) {
						for (int w = pInitialLimit+1; w < y; w++) {
							getLandPoint(MapHelper.formKey(x, w)).setType(Constants.INSIDE_POLYGON);
						}
						reversed = true;
					} else {
						getLandPoint(MapHelper.formKey(x, y)).setType(Constants.OUTSIDE_POLYGON);
					}
					break;
				}
			}
		}

	}

	/**
	 * This method prints this will be the method to test if our hypothesis is
	 * right
	 */
	public void printMap() {
		for (int j = pointsy - 1; j >= 0; j--) {
			for (int i = 0; i < pointsx; i++) {
				System.out.print(getLandPoint(MapHelper.formKey(i, j)).getType());
			}
			System.out.println();
		}
	}
}