package helpers.clusterVariation;

import java.util.ArrayList;
import java.util.List;

import algorithm.clusterVariation.LSystemClusterAlgorithm;
import interfaces.Constants;
import models.LandMap;
import models.LandPoint;

public class ClusterTester {
	public static void main(String[] args) {
		int large = 1834, width = 1623;
		// 1. We create the map
		LandMap landMap = new LandMap(large, width);

		List<LandPoint> polygon = new ArrayList<>();
		LandPoint landPoint = new LandPoint(1833, 0, false, false, true, false, false);
		polygon.add(landPoint);
		landPoint = new LandPoint(1594, 1434, false, false, true, false, false);
		polygon.add(landPoint);
		landPoint = new LandPoint(862, 1622, false, false, true, false, false);
		polygon.add(landPoint);
		landPoint = new LandPoint(0, 588, false, false, true, false, false);
		polygon.add(landPoint);
		landPoint = new LandPoint(1013, 183, false, false, true, false, false);
		polygon.add(landPoint);
		// we must reuse the first one as the last
		landPoint = new LandPoint(1833, 0, false, false, true, false, false);
		polygon.add(landPoint);

		// 2. we create the border from the polygon
		landMap.createBorderFromPolygon(polygon);

		// 3, We create the entry points for the main routes
		// Entry points = [327,981][1710,742]
		List<LandPoint> entryPoints = new ArrayList<>();
		landPoint = new LandPoint(327, 981, false, false, true, false, false);
		entryPoints.add(landPoint);
		landPoint = new LandPoint(1710, 742, false, false, true, false, false);
		entryPoints.add(landPoint);

		LSystemClusterAlgorithm.landMap = landMap;
		for (LandPoint entryPoint : entryPoints) {
			if (large > 1000 || width > 1000) {
				LSystemClusterAlgorithm.createMainRoute(entryPoint.getId(), Constants.ORTHOGONAL,
						Constants.ARTERIAL_BRANCH);
			} else {
				LSystemClusterAlgorithm.createMainRoute(entryPoint.getId(), Constants.ORTHOGONAL,
						Constants.COLLECTOR_BRANCH);
			}
		}

		// landMap.printMapToFile();

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