package interfaces;

public interface ClusterConfiguration {
	public static int BASE_CLUSTER_SIZE = 272;
	
	public static int COLLECTOR_BRANCH_SIZE = 16;
	public static int LOCAL_BRANCH_SIZE = 10;
	public static int WALK_BRANCH_SIZE = 4;
	
	public static int HOUSE_SIDE_MINIMUN_SIZE = 8;
	public static int HOUSE_SIDE_MAXIMUN_SIZE = 15;
	public static int HOUSE_DEPTH_MINIMUN_SIZE = 12; //8*12=96	

	public static int ARTERIAL_BRANCH = 0;
	public static int COLLECTOR_BRANCH = 1;
	public static int LOCAL_BRANCH = 2;
}