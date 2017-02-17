package models;

import java.util.HashMap;
import java.util.Map;

import helpers.MapHelper;
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
				if (neighbour == null || neighbour.isPartOfRoute() || neighbour.isPartOfSubRoute() || neighbour.isMapLimit(direction)) {
					return num;
				} else {
					num++;
				}
				point = neighbour;
			}
		}
	}
	
	public void printMap() {
		for (int j = pointsy - 1; j >= 0; j--) {
			for (int i = 0; i < pointsx; i++) {
				System.out.print(getLandPoint(MapHelper.formKey(i, j)).getType());
			}
			System.out.println();
		}
	}
}