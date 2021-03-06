package helpers.matricial2035Variation;

import interfaces.matricial2035Variation.Matricial2035Constants;

public class Matricial2035MapHelper {

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
		case Matricial2035Constants.NORTH:
			return formKey(xy[0], xy[1] + size);
		case Matricial2035Constants.SOUTH:
			return formKey(xy[0], xy[1] - size);
		case Matricial2035Constants.EAST:
			return formKey(xy[0] + size, xy[1]);
		case Matricial2035Constants.WEST:
			return formKey(xy[0] - size, xy[1]);
		}
		return -1;
	}

	public static int[] moveKeyByOffsetAndDirection(int[] xy, int size, int direction) {
		int[] movedXY = new int[]{xy[0],xy[1]};
		switch (direction) {
		case Matricial2035Constants.NORTH:
			movedXY[1] = movedXY[1] + size;
			return movedXY;
		case Matricial2035Constants.SOUTH:
			movedXY[1] = movedXY[1] - size;
			return movedXY;
		case Matricial2035Constants.EAST:
			movedXY[0] = movedXY[0] + size;
			return movedXY;
		case Matricial2035Constants.WEST:
			movedXY[0] = movedXY[0] - size;
			return movedXY;
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

	/**
	 * Due to direction being strongly related to X and Y completitude it must be passed
	 * */
	public static int[] moveKeyByGradientAndOffset(int[] currentXY, int[] finalXY, int size, Double gradient,
			double offset, int direction) {
		if((direction == Matricial2035Constants.WEST) || (direction == Matricial2035Constants.EAST)){
			double[] newXY = new double[2];
			newXY[0] = currentXY[0] - size;
			newXY[1] = (int) (gradient * newXY[0] + offset);
			double bottomdistance = Math.sqrt(Math.pow(finalXY[0] - newXY[0], 2) + Math.pow(finalXY[1] - newXY[1], 2));

			double[] newBXY = new double[2];
			newBXY[0] = currentXY[0] + size;
			newBXY[1] = (int) (gradient * newXY[0] + offset);
			double upperdistance = Math.sqrt(Math.pow(finalXY[0] - newBXY[0], 2) + Math.pow(finalXY[1] - newBXY[1], 2));

			int[] responseXY = new int[2];
			if (bottomdistance < upperdistance) {
				responseXY[0]= (int) newXY[0];
				responseXY[1]= (int) newXY[1];
				return responseXY;
			} else {
				responseXY[0]= (int) newBXY[0];
				responseXY[1]= (int) newBXY[1];
				return responseXY;
			}			
		}else{
			double[] newXY = new double[2];
			newXY[1] = currentXY[1] - size;
			newXY[0] = (int) ((newXY[1] - offset)/gradient);
			double bottomdistance = Math.sqrt(Math.pow(finalXY[0] - newXY[0], 2) + Math.pow(finalXY[1] - newXY[1], 2));

			double[] newBXY = new double[2];
			newBXY[1] = currentXY[1] + size;
			newBXY[0] = (int) ((newXY[1] - offset)/gradient);
			double upperdistance = Math.sqrt(Math.pow(finalXY[0] - newBXY[0], 2) + Math.pow(finalXY[1] - newBXY[1], 2));

			int[] responseXY = new int[2];
			if (bottomdistance < upperdistance) {
				responseXY[0]= (int) newXY[0];
				responseXY[1]= (int) newXY[1];
				return responseXY;
			} else {
				responseXY[0]= (int) newBXY[0];
				responseXY[1]= (int) newBXY[1];
				return responseXY;
			}	
		}
	}
}