package algorithm.clusterVariation;

import helpers.base.MapHelper;
import interfaces.ClusterConfiguration;
import interfaces.Constants;
import models.clusterVariation.ClusterLandMap;
import models.clusterVariation.ClusterPolygon;

public class ClusterLotizationAlgorithm {

	public static ClusterLandMap landMap;

	public static void zonify(ClusterPolygon figure) {
		if (figure.getType() == ClusterConfiguration.CLUSTER_TYPE_TRIANGLE) {
			fillemptyArea(figure);
		} else {

		}
	}

	private static void fillemptyArea(ClusterPolygon figure) {
		for (int i = 0; i < figure.getVertices().size(); i++) {
			createBorder(figure.getVertices().get(i),
					figure.getVertices().get((i + 1) % (figure.getVertices().size())));
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
				landMap.getLandPoint(MapHelper.formKey(initialXY[0], w)).setType(Constants.GARDEN_BORDER);
			}
		}

		double gradient = (finalXY[1] - initialXY[1]) * 1.0 / underscore;
		
		int lower = initialXY[0] < finalXY[0] ? initialXY[0] : finalXY[0];
		int upper = initialXY[0] > finalXY[0] ? initialXY[0] : finalXY[0];
		if (gradient == 0) {
			for (int w = lower; w < upper; w++) {
				landMap.getLandPoint(MapHelper.formKey(w, initialXY[1])).setType(Constants.GARDEN_BORDER);
			}
		}

		double b = finalXY[1] - gradient * finalXY[0];
		// 3nd the gradient is positive/negative.
		for (int w = lower; w <= upper; w++) {
			float y = MapHelper.round(gradient * w + b);
			if (y == (int) y) // quick and dirty convertion check
			{
				landMap.getLandPoint(MapHelper.formKey(w, (int) y)).setType(Constants.GARDEN_BORDER);
			}
		}
	}

}