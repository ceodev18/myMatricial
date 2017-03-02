package models.clusterVariation;

import helpers.base.MapHelper;
import interfaces.Constants;

public class ClusterLandPoint {
	private int id = -1;
	private int x = -1;
	private int y = -1;
	private String type = Constants.INSIDE_POLYGON;

	public ClusterLandPoint(int x, int y) {
		this.x = x;
		this.y = y;
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

	/* Helper functions */
	public int findNeighbour(int direction) {
		switch (direction) {
		case Constants.EAST:
			return MapHelper.formKey(x + 1, y);
		case Constants.WEST:
			return MapHelper.formKey(x - 1, y);
		case Constants.NORTH:
			return MapHelper.formKey(x, y - 1);
		case Constants.SOUTH:
			return MapHelper.formKey(x, y + 1);
		}
		return -1;
	}

	public int findDirection(ClusterLandPoint mainRouteExit) {
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
		return -1; // In case of error or pointing the same point
	}

	public boolean isMapLimit(int direction) {
		if (type.equals(Constants.OUTSIDE_POLYGON))
			return true;
		else
			return false;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
