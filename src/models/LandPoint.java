package models;

import helpers.base.MapHelper;
import interfaces.Constants;

public class LandPoint {
	private int id = -1;
	private int x = -1;
	private int y = -1;
	private String type = Constants.INSIDE_POLYGON;
	private boolean isPolygonLimit = false;
	private boolean isMapLimitXW = false;
	private boolean isMapLimitXE = false;
	private boolean isMapLimitYS = false;
	private boolean isMapLimitYN = false;
	private boolean isPartOfSubRoute = false;
	private boolean isPartOfRoute = false;
	private int nextNodeOfRoute = -1;
	private int lastNodeOfRoute = -1;
	private int nextNodeOfSubRoute = -1;
	private int lastNodeOfSubRoute = -1;

	public LandPoint(int x, int y, boolean isMapLimitXW, boolean isMapLimitXE, boolean isMapLimitYS,
			boolean isMapLimitYN, boolean isPolygonLimit) {
		this.x = x;
		this.y = y;
		this.isPolygonLimit = isPolygonLimit;
		this.setMapLimitXW(isMapLimitXW);
		this.setMapLimitXE(isMapLimitXE);
		this.setMapLimitYS(isMapLimitYS);
		this.setMapLimitYN(isMapLimitYN);

		this.id = x * 10000 + y; /* Result space = y: 0 -9999 y x = 0 9999 */
	}

	public int getId() {
		return id;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public boolean isPolygonLimit() {
		return isPolygonLimit;
	}

	public void setPolygonLimit(boolean isPolygonLimit) {
		this.isPolygonLimit = isPolygonLimit;
	}

	public boolean isMapLimitXW() {
		return isMapLimitXW;
	}

	public void setMapLimitXW(boolean isMapLimitXW) {
		this.isMapLimitXW = isMapLimitXW;
	}

	public boolean isMapLimitXE() {
		return isMapLimitXE;
	}

	public void setMapLimitXE(boolean isMapLimitXE) {
		this.isMapLimitXE = isMapLimitXE;
	}

	public boolean isMapLimitYS() {
		return isMapLimitYS;
	}

	public void setMapLimitYS(boolean isMapLimitYS) {
		this.isMapLimitYS = isMapLimitYS;
	}

	public boolean isMapLimitYN() {
		return isMapLimitYN;
	}

	public void setMapLimitYN(boolean isMapLimitYN) {
		this.isMapLimitYN = isMapLimitYN;
	}

	public boolean isPartOfSubRoute() {
		return isPartOfSubRoute;
	}

	public void setPartOfSubRoute(boolean isPartOfSubRoute) {
		this.isPartOfSubRoute = isPartOfSubRoute;
	}

	public boolean isPartOfRoute() {
		return isPartOfRoute;
	}

	public void setPartOfRoute(boolean isPartOfRoute) {
		this.isPartOfRoute = isPartOfRoute;
	}

	public int getNextNodeOfRoute() {
		return nextNodeOfRoute;
	}

	public void setNextNodeOfRoute(int nextNodeOfRoute) {
		this.nextNodeOfRoute = nextNodeOfRoute;
	}

	public int getLastNodeOfRoute() {
		return lastNodeOfRoute;
	}

	public void setLastNodeOfRoute(int lastNodeOfRoute) {
		this.lastNodeOfRoute = lastNodeOfRoute;
	}

	public int getNextNodeOfSubRoute() {
		return nextNodeOfSubRoute;
	}

	public void setNextNodeOfSubRoute(int nextNodeOfSubRoute) {
		this.nextNodeOfSubRoute = nextNodeOfSubRoute;
	}

	public int getLastNodeOfSubRoute() {
		return lastNodeOfSubRoute;
	}

	public void setLastNodeOfSubRoute(int lastNodeOfSuRoute) {
		this.lastNodeOfSubRoute = lastNodeOfSuRoute;
	}

	/* Helper functions */
	public int findNeighbour(int direction) {
		switch (direction) {
		case Constants.EAST:
			if (isMapLimitXE()) {
				return -1;
			} else {
				return MapHelper.formKey(x + 1, y);
			}
		case Constants.WEST:
			if (isMapLimitXW()) {
				return -1;
			} else {
				return MapHelper.formKey(x - 1, y);
			}
		case Constants.NORTH:
			if (isMapLimitYN()) {
				return -1;
			} else {
				return MapHelper.formKey(x, y + 1);
			}
		case Constants.SOUTH:
			if (isMapLimitYS()) {
				return -1;
			} else {
				return MapHelper.formKey(x, y - 1);
			}
		}
		return -1;
	}
	
	public int findDirection(LandPoint mainRouteExit) {
		int xDiff = mainRouteExit.getX() - this.getX();
		int yDiff = mainRouteExit.getX() - this.getY();
		
		if (yDiff > 0) {
	          return Constants.NORTH;
		} else if (yDiff < 0) {
	          return Constants.SOUTH;
		}
		
		if (xDiff > 0) {
          return Constants.WEST;
		} else if (xDiff < 0) {
	          return Constants.EAST;
		}
		return -1; //In case of error or pointing the same point
	}

	public boolean isMapLimit(int direction) {
		switch(direction){
		case Constants.EAST:
			return isMapLimitXE;
		case Constants.WEST:
			return isMapLimitXW;
		case Constants.NORTH:
			return isMapLimitYN;
		case Constants.SOUTH:
			return isMapLimitYS;
		}
		return false;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
