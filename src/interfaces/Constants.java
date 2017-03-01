package interfaces;

public interface Constants {
	//for direction
	public int WEST = 0;
	public int SOUTH = 1;
	public int EAST = 2;
	public int NORTH = 3;
	
	//for the branching of the algorithm
	public int AVENUE_BRANCH = 0;
	public int STREET_BRANCH = 1; //Jiron - Avenida corta
	public int ALLEY_BRANCH = 2;
	
	public int HORIZONTAL = 1;
	public int VERTICAL = 1;
	
	public String POLYGON_LIMIT = ".";
	public String OUTSIDE_POLYGON = " ";
	public String INSIDE_POLYGON = "l";
}