package models.radialVariation;

import helpers.radialVariation.RadialMapHelper;
import interfaces.radialVariation.RadialConfiguration;
import interfaces.radialVariation.RadialConstants;
import models.radialVariation.RadialLandPoint;

public class RadialLandPoint {
	private int id = -1;
	private int x = -1;
	private int y = -1;
	private String type = RadialConfiguration.OUTSIDE_POLYGON_MARK;
	private String gramaticalType = null;
	private int nodeType;
	public RadialLandPoint(int x, int y){
		this.x = x;
		this.y = y;
		this.id = x * 10000 + y; /* Result space = y: 0 -9999 y x = 0 9999 */
	}

	public int getId() {
		return id;
	}
	public void fixId() {
		int xs= getX();
		int ys= getY();
		this.id = xs * 10000 + ys;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
		fixId();
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
		fixId();
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
		case RadialConstants.EAST:
			return RadialMapHelper.formKey(x + 1, y);
		case RadialConstants.WEST:
			return RadialMapHelper.formKey(x - 1, y);
		case RadialConstants.NORTH:
			return RadialMapHelper.formKey(x, y - 1);
		case RadialConstants.SOUTH:
			return RadialMapHelper.formKey(x, y + 1);
		}
		return -1;
	}

	public int findDirection(RadialLandPoint mainRouteExit) {
		int xDiff = mainRouteExit.getX() - this.getX();
		int yDiff = mainRouteExit.getY() - this.getY();

		if (yDiff > 0) {
			return RadialConstants.NORTH;
		} else if (yDiff < 0) {
			return RadialConstants.SOUTH;
		}

		if (xDiff > 0) {
			return RadialConstants.WEST;
		} else if (xDiff < 0) {
			return RadialConstants.EAST;
		}
		return -1; // In case of error or pointing the same point
	}

	public boolean isMapLimit() {
		if (type.equals(RadialConfiguration.OUTSIDE_POLYGON_MARK))
			return true;
		else
			return false;
	}
	public String getGramaticalType() {
		return gramaticalType;
	}

	public void setGramaticalType(String gramaticalType) {
		this.gramaticalType = gramaticalType;
	}
}
