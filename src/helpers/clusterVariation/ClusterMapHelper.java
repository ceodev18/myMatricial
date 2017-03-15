package helpers.clusterVariation;

import interfaces.clusterVariation.ClusterConstants;

public class ClusterMapHelper {

	public static int formKey(int x, int y) {
		return x * 10000 + y;
	}

	public static int[] breakKey(int key) {
		int xy[] = new int[2];
		xy[1] = key % 10000;
		xy[0] = (key - xy[1]) / 10000;
		return xy;
	}

	public static int moveKeyByOffsetAndDirection(int entryPointId, int size, int direction) {
		int[] xy = breakKey(entryPointId);
		switch (direction) {
		case ClusterConstants.NORTH:
			return formKey(xy[0], xy[1] + size);
		case ClusterConstants.SOUTH:
			return formKey(xy[0], xy[1] - size);
		case ClusterConstants.EAST:
			return formKey(xy[0] + size, xy[1]);
		case ClusterConstants.WEST:
			return formKey(xy[0] - size, xy[1]);
		}
		return -1;
	}

	public static int[] moveKeyByOffsetAndDirection(int[] xy, int size, int direction) {
		switch (direction) {
		case ClusterConstants.NORTH:
			xy[1] = xy[1] + size;
			return xy;
		case ClusterConstants.SOUTH:
			xy[1] = xy[1] - size;
			return xy;
		case ClusterConstants.EAST:
			xy[0] = xy[0] + size;
			return xy;
		case ClusterConstants.WEST:
			xy[0] = xy[0] - size;
			return xy;
		}
		return null;
	}
	
	public static int round(double d) {
		double dAbs = Math.abs(d);
		int i = (int) dAbs;
		double result = dAbs - (double) i;
		if (result < 0.5) {
			return d < 0 ? -i : i;
		} else {
			return d < 0 ? -(i + 1) : i + 1;
		}
	}
}