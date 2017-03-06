package helpers.base;

import java.util.List;

import interfaces.Constants;

public class MapHelper {

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
		case Constants.NORTH:
			return formKey(xy[0], xy[1] + size);
		case Constants.SOUTH:
			return formKey(xy[0], xy[1] - size);
		case Constants.EAST:
			return formKey(xy[0] + size, xy[1]);
		case Constants.WEST:
			return formKey(xy[0] - size, xy[1]);
		}
		return -1;
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

	public static void orderNodes(List<Integer> nodes) {
		for (int i = 0; i < nodes.size() - 1; i++) {
			for (int j = 1; j < nodes.size(); j++) {
				int[] arrKeysi = breakKey(nodes.get(i));
				int[] arrKeysj = breakKey(nodes.get(j));
				int nodej = nodes.get(j);
				if ((arrKeysj[1] == arrKeysi[1] && arrKeysj[0] < arrKeysi[0]) || (arrKeysj[1] > arrKeysi[1])) {
					nodes.set(j, nodes.get(i));
					nodes.set(i, nodej);
				}
			}
		}
	}
}