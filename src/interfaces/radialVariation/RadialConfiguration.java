package interfaces.radialVariation;

public interface RadialConfiguration {
	public static int BASE_RADIAL_SIZE = 272;
	
	public static int ARTERIAL_BRANCH_SIZE = 26;
	public static int COLLECTOR_BRANCH_SIZE = 16;
	public static int LOCAL_BRANCH_SIZE = 10;
	public static int WALK_BRANCH_SIZE = 4;
	
	public static int HOUSE_SIDE_MINIMUN_SIZE = 6;
	public static int HOUSE_SIDE_MAXIMUN_SIZE = 15;
	public static int HOUSE_DEPTH_MINIMUN_SIZE = 15; //8*12=96	
	public static int HOUSE_DEPTH_MAXIMUN_SIZE = 20; //8*12=96	
	public static int RADIAL_ENTRANCE_SIZE = 12;
	
	public static int ARTERIAL_BRANCH = 0;
	public static int COLLECTOR_BRANCH = 1;
	public static int LOCAL_BRANCH = 2;
	public static int WALK_BRANCH = 4;
	public static int NODE = 3;

	public static String EMPTY_MARK = "l";
	public static String OUTSIDE_POLYGON_MARK = " ";
	public static String POLYGON_BORDER = ".";
	public static String ARTERIAL_MARK = "a";
	public static String COLLECTOR_MARK = "a";
	public static String LOCAL_MARK = "a";
	public static String NODE_MARK = "n";
	public static String PARK_MARK = "p";	
	public static String WALK_MARK = "a";
	public static String RADIAL_ENTRANCE_MARK = "a";
	public static String BORDER_MARK = "e";
	public static String CENTROID = "P";

	public static int TYPE_SPECIAL = 0;
	public static int TYPE_COMPLETE = 1;
}