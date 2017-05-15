package models.matricial2035Variation;

import helpers.matricial2035Variation.Matricial2035MapHelper;
import interfaces.matricial2035Variation.Matricial2035Configuration;
import interfaces.matricial2035Variation.Matricial2035Constants;

public class Matricial2035LandPoint {
	private int id = -1;
	private int x = -1;
	private int y = -1;
	private String type = Matricial2035Configuration.OUTSIDE_POLYGON_MARK;
	private String gramaticalType = null;
	private int nodeType;

	public Matricial2035LandPoint(int x, int y) {
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
		case Matricial2035Constants.EAST:
			return Matricial2035MapHelper.formKey(x + 1, y);
		case Matricial2035Constants.WEST:
			return Matricial2035MapHelper.formKey(x - 1, y);
		case Matricial2035Constants.NORTH:
			return Matricial2035MapHelper.formKey(x, y - 1);
		case Matricial2035Constants.SOUTH:
			return Matricial2035MapHelper.formKey(x, y + 1);
		}
		return -1;
	}

	public int findDirection(Matricial2035LandPoint mainRouteExit) {
		int xDiff = mainRouteExit.getX() - this.getX();
		int yDiff = mainRouteExit.getY() - this.getY();

		if (yDiff > 0) {
			return Matricial2035Constants.NORTH;
		} else if (yDiff < 0) {
			return Matricial2035Constants.SOUTH;
		}

		if (xDiff > 0) {
			return Matricial2035Constants.WEST;
		} else if (xDiff < 0) {
			return Matricial2035Constants.EAST;
		}
		return -1; // In case of error or pointing the same point
	}

	public boolean isMapLimit() {
		if (type.equals(Matricial2035Configuration.OUTSIDE_POLYGON_MARK))
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