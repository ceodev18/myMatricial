package helpers.clusterVariation;

import java.util.ArrayList;
import java.util.List;

import algorithm.clusterVariation.LSystemClusterAlgorithm;
import interfaces.Constants;
import models.clusterVariation.CLandMap;
import models.clusterVariation.CLandPoint;

public class ClusterTester {
	public static void main(String[] args) {
		int large = 1834, width = 1623;
		// 1. We create the map
		CLandMap landMap = new CLandMap(large, width);

		List<CLandPoint> polygon = new ArrayList<>();
		CLandPoint landPoint = new CLandPoint(1833, 0);
		polygon.add(landPoint);
		landPoint = new CLandPoint(1594, 1434);
		polygon.add(landPoint);
		landPoint = new CLandPoint(862, 1622);
		polygon.add(landPoint);
		landPoint = new CLandPoint(0, 588);
		polygon.add(landPoint);
		landPoint = new CLandPoint(1013, 183);
		polygon.add(landPoint);
		// we must reuse the first one as the last
		landPoint = new CLandPoint(1833, 0);
		polygon.add(landPoint);

		// 2. we create the border from the polygon
		landMap.createBorderFromPolygon(polygon);

		// 3, We create the entry points for the main routes
		// Entry points = [327,981][1710,742]
		List<CLandPoint> entryPoints = new ArrayList<>();
		landPoint = new CLandPoint(327, 981);
		entryPoints.add(landPoint);
		landPoint = new CLandPoint(1710, 742);
		entryPoints.add(landPoint);

		LSystemClusterAlgorithm.landMap = landMap;
		for (CLandPoint entryPoint : entryPoints) {
			int direction = CDirectionHelper.orthogonalDirectionFromPointToPoint(entryPoint, landMap.getCentroid());
			if (large > 1000 || width > 1000) {
				LSystemClusterAlgorithm.createMainRoute(entryPoint.getId(), direction, Constants.ARTERIAL_BRANCH);
			} else {
				LSystemClusterAlgorithm.createMainRoute(entryPoint.getId(), direction, Constants.COLLECTOR_BRANCH);
			}
		}

		landMap.printMapToFile();

		/*
		 * landPoint = new LandPoint(20, 0, false, false, true, false, false);
		 * 
		 * LSystemRouteAlgorithm.landMap = landMap;
		 * LSystemRouteAlgorithm.createRoute(landPoint.getId(), Constants.NORTH,
		 * Constants.AVENUE_BRANCH, 120, 0);
		 * //LSystemRouteAlgorithm.landMap.printMap();
		 * 
		 * LotizationAlgorithm.landMap = LSystemRouteAlgorithm.landMap; int
		 * lotSize = 240; LotizationAlgorithm.generateLots(lotSize);
		 * LotizationAlgorithm.landMap.printMap();
		 */
	}
}