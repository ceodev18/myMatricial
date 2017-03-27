package interfaces.spineVariation;

public interface SpineConstants {
	//for direction
	public int WEST = 0;
	public int SOUTH = 1;
	public int EAST = 2;
	public int NORTH = 3;
	public int SOUTH_WEST = 4;
	public int SOUTH_EAST = 5;
	public int NORTH_EAST = 6;
	public int NORTH_WEST = 7;
	
	//FOR THE ROADS
	//This are from my initial investigation
	public int AVENUE_BRANCH = 0;
	public int STREET_BRANCH = 1; //Jiron - Avenida corta
	public int ALLEY_BRANCH = 2;
	//This are from the architects
	public int ARTERIAL_BRANCH = 3;
	public int COLLECTOR_BRANCH = 4;
	public int LOCAL_BRANCH = 5;
	public int NODE = 6;
	
	public int HORIZONTAL = 1;
	public int VERTICAL = 2;
	public int ORTHOGONAL = 3;
	
	public String POLYGON_LIMIT = ".";
	public String OUTSIDE_POLYGON = " ";
	public String INSIDE_POLYGON = "l";
	public String GARDEN_LOT = "G";


}