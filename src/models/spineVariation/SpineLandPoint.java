package models.spineVariation;

import helpers.clusterVariation.ClusterMapHelper;
import interfaces.spineVariation.SpineConfiguration;
import interfaces.spineVariation.SpineConstants;
import models.clusterVariation.ClusterLandPoint;

public class SpineLandPoint {
	private int id = -1;
	private int x = -1;
	private int y = -1;
	private String type = SpineConfiguration.OUTSIDE_POLYGON_MARK;
	private int nodeType;
	public SpineLandPoint(int x, int y){
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
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setNodeType(int nodeType) {
		this.nodeType = nodeType;
	}
	
	public int getNodeType(){
		return nodeType;
	}
	
	/* Helper functions */
	public int findNeighbour(int direction) {
		switch (direction) {
		case SpineConstants.EAST:
			return ClusterMapHelper.formKey(x + 1, y);
		case SpineConstants.WEST:
			return ClusterMapHelper.formKey(x - 1, y);
		case SpineConstants.NORTH:
			return ClusterMapHelper.formKey(x, y - 1);
		case SpineConstants.SOUTH:
			return ClusterMapHelper.formKey(x, y + 1);
		}
		return -1;
	}

	public int findDirection(ClusterLandPoint mainRouteExit) {
		int xDiff = mainRouteExit.getX() - this.getX();
		int yDiff = mainRouteExit.getX() - this.getY();

		if (yDiff > 0) {
			return SpineConstants.NORTH;
		} else if (yDiff < 0) {
			return SpineConstants.SOUTH;
		}

		if (xDiff > 0) {
			return SpineConstants.WEST;
		} else if (xDiff < 0) {
			return SpineConstants.EAST;
		}
		return -1; // In case of error or pointing the same point
	}

	public boolean isMapLimit() {
		if (type.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
			return true;
		else
			return false;
	}
}
