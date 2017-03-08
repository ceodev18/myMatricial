package algorithm.clusterVariation;

import java.util.List;

import helpers.base.MapHelper;
import interfaces.ClusterConfiguration;
import interfaces.Constants;
import models.clusterVariation.ClusterLandMap;
import models.clusterVariation.ClusterPolygon;

public class ClusterLotizationAlgorithm {

	public static ClusterLandMap landMap;

	public static void zonify(ClusterPolygon figure) {
		if (figure.getType() == ClusterConfiguration.CLUSTER_TYPE_TRIANGLE) {
			fillTriangle(figure);
		} else if (figure.getType() == ClusterConfiguration.CLUSTER_TYPE_RECTANGLE) {
			archetypeCluster(figure);
		}
	}

	private static void archetypeCluster(ClusterPolygon figure) {
		List<Integer> limits = figure.getVertices();
		int markNumber = 0;
		// we do first the upper part likely to be close to the main street
		for (int i = MapHelper.breakKey(limits.get(0))[0]; i < MapHelper.breakKey(limits.get(1))[0]; 
				markNumber += 2, i += ClusterConfiguration.HOUSE_SIDE_MINIMUN_SIZE) {
			createHouseIfPossible(i, MapHelper.breakKey(limits.get(0))[1], Constants.SOUTH, markNumber % 3);// upper
		}
	}

	private static void createHouseIfPossible(int xInitialCoordinate, int yInitialCoordinate, int direction,
			int numberMark) {
		if (possibleHouse(xInitialCoordinate, yInitialCoordinate, direction)) {
			for (int i = xInitialCoordinate; i < xInitialCoordinate
					+ ClusterConfiguration.HOUSE_SIDE_MINIMUN_SIZE; i++) {
				for (int j = yInitialCoordinate; j > yInitialCoordinate
						- ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE; j--) {
					landMap.findPoint(MapHelper.formKey(i, j)).setType(""+numberMark);
				}

			}
		}
	}

	private static boolean possibleHouse(int x, int y, int direction) {
		//TODO include directions on analisis
		if(((x + ClusterConfiguration.HOUSE_SIDE_MINIMUN_SIZE) >= landMap.getPointsx()) 
				|| ((y - ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE) < 0)){
			return false;
			
		}
		for (int i = x; i < x + ClusterConfiguration.HOUSE_SIDE_MINIMUN_SIZE; i++) {//Only for south
			for (int j = y; j > y - ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE; j--) {
				if(!landMap.findPoint(MapHelper.formKey(i, j)).getType().equals(ClusterConfiguration.EMPTY)){
					return false;
				}
			}
		}
		return true;
	}

	private static void fillTriangle(ClusterPolygon figure) {
		for (int i = 0; i < figure.getVertices().size(); i++) {
			createBorder(figure.getVertices().get(i),
					figure.getVertices().get((i + 1) % (figure.getVertices().size())));
		}
		alotTriangle(figure);

	}

	private static void alotTriangle(ClusterPolygon figure) {
		int[] limits = figure.getSquareLimits();
		for (int i = limits[0]; i < limits[1]; i++) {// limits of X
			int borderLimit = 0;
			for (borderLimit = limits[2]; borderLimit < limits[3]; borderLimit++) {// limits
																					// of
																					// Y
				if (landMap.getLandPoint(MapHelper.formKey(i, borderLimit)).getType().equals(Constants.GARDEN_LOT))
					break;
			}

			borderLimit++;
			for (int j = borderLimit; j < limits[3]; j++) {
				if (landMap.getLandPoint(MapHelper.formKey(i, j)).getType().equals(Constants.INSIDE_POLYGON)) {
					landMap.getLandPoint(MapHelper.formKey(i, j)).setType(Constants.GARDEN_LOT);
				} else
					break;
			}
		}
	}

	private static void createBorder(Integer initialPoint, Integer finalPoint) {
		int[] finalXY = MapHelper.breakKey(finalPoint);
		int[] initialXY = MapHelper.breakKey(initialPoint);
		int underscore = finalXY[0] - initialXY[0];

		if (underscore == 0) {
			int lower = initialXY[1] < finalXY[1] ? initialXY[1] : finalXY[1];
			int upper = initialXY[1] > finalXY[1] ? initialXY[1] : finalXY[1];
			for (int w = lower; w < upper; w++) {
				landMap.getLandPoint(MapHelper.formKey(initialXY[0], w)).setType(Constants.GARDEN_LOT);
			}
		}

		double gradient = (finalXY[1] - initialXY[1]) * 1.0 / underscore;

		int lower = initialXY[0] < finalXY[0] ? initialXY[0] : finalXY[0];
		int upper = initialXY[0] > finalXY[0] ? initialXY[0] : finalXY[0];
		if (gradient == 0) {
			for (int w = lower; w < upper; w++) {
				landMap.getLandPoint(MapHelper.formKey(w, initialXY[1])).setType(Constants.GARDEN_LOT);
			}
		}

		double b = finalXY[1] - gradient * finalXY[0];
		// 3nd the gradient is positive/negative.
		for (int w = lower; w <= upper; w++) {
			float y = MapHelper.round(gradient * w + b);
			if (y == (int) y) // quick and dirty convertion check
			{
				landMap.getLandPoint(MapHelper.formKey(w, (int) y)).setType(Constants.GARDEN_LOT);
			}
		}
	}

}