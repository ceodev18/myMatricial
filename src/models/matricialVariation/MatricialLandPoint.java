package models.matricialVariation;

import helpers.matricialVariation.MatricialMapHelper;
import interfaces.matricialVariation.MatricialConfiguration;
import interfaces.matricialVariation.MatricialConstants;

public class MatricialLandPoint {
	private int id = -1;
	private int x = -1;
	private int y = -1;
	private String type = MatricialConfiguration.OUTSIDE_POLYGON_MARK;
	private int nodeType;
	
	public MatricialLandPoint(int x, int y){
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
		case MatricialConstants.EAST:
			return MatricialMapHelper.formKey(x + 1, y);
		case MatricialConstants.WEST:
			return MatricialMapHelper.formKey(x - 1, y);
		case MatricialConstants.NORTH:
			return MatricialMapHelper.formKey(x, y - 1);
		case MatricialConstants.SOUTH:
			return MatricialMapHelper.formKey(x, y + 1);
		}
		return -1;
	}
	
	public int findDirection(MatricialLandPoint mainRouteExit) {
		int xDiff = mainRouteExit.getX() - this.getX();
		int yDiff = mainRouteExit.getY() - this.getY();

		if (yDiff > 0) {
			return MatricialConstants.NORTH;
		} else if (yDiff < 0) {
			return MatricialConstants.SOUTH;
		}

		if (xDiff > 0) {
			return MatricialConstants.WEST;
		} else if (xDiff < 0) {
			return MatricialConstants.EAST;
		}
		return -1; // In case of error or pointing the same point
	}
	
	public boolean isMapLimit() {
		if (type.equals(MatricialConfiguration.OUTSIDE_POLYGON_MARK))
			return true;
		else
			return false;
	}

}
