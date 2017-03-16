package helpers.clusterVariation;

import java.util.ArrayList;
import java.util.List;

import algorithm.clusterVariation.ClusterLotizationAlgorithm;
import algorithm.clusterVariation.LSystemClusterAlgorithm;
import interfaces.clusterVariation.ClusterConfiguration;
import models.clusterVariation.ClusterLandMap;
import models.clusterVariation.ClusterLandPoint;

public class ClusterTester {
	public static void main(String[] args) {
		/*int large = 1834, width = 1623;
		// 1. We create the map and set its intrinsec variables
		ClusterLandMap landMap = new ClusterLandMap(large, width);
		List<ClusterLandPoint> polygon = new ArrayList<>();
		ClusterLandPoint landPoint = new ClusterLandPoint(1833, 0);
		polygon.add(landPoint);
		landPoint = new ClusterLandPoint(1594, 1434);
		polygon.add(landPoint);
		landPoint = new ClusterLandPoint(862, 1622);
		polygon.add(landPoint);
		landPoint = new ClusterLandPoint(0, 588);
		polygon.add(landPoint);
		landPoint = new ClusterLandPoint(1013, 183);
		polygon.add(landPoint);
		// we must reuse the first one as the last
		landPoint = new ClusterLandPoint(1833, 0);
		polygon.add(landPoint);
		// 2. we create the border from the polygon
		landMap.createBorderFromPolygon(polygon);
		// 3, We create the entry points for the main routes
		// Entry points = [327,981][1710,742]
		List<ClusterLandPoint> entryPoints = new ArrayList<>();
		landPoint = new ClusterLandPoint(327, 981);
		entryPoints.add(landPoint);*/

		int large = 421, width = 617 ;
		// 1. We create the map and set its intrinsec variables
		ClusterLandMap landMap = new ClusterLandMap(large, width);
		List<ClusterLandPoint> polygon = new ArrayList<>();
		ClusterLandPoint landPoint = new ClusterLandPoint(92, 0);
		polygon.add(landPoint);
		landPoint = new ClusterLandPoint(421, 336);
		polygon.add(landPoint);
		landPoint = new ClusterLandPoint(166, 617);
		polygon.add(landPoint);
		landPoint = new ClusterLandPoint(0, 362);
		polygon.add(landPoint);
		landPoint = new ClusterLandPoint(92, 0);
		polygon.add(landPoint);
		// 2. we create the border from the polygon
		landMap.createBorderFromPolygon(polygon);
		// 3, We create the entry points for the main routes
		// Entry points = [327,981][1710,742]
		List<ClusterLandPoint> entryPoints = new ArrayList<>();
		landPoint = new ClusterLandPoint(244, 155);
		entryPoints.add(landPoint);
		
		
		LSystemClusterAlgorithm.landMap = landMap;
		for (ClusterLandPoint entryPoint : entryPoints) {
			int direction = ClusterDirectionHelper.orthogonalDirectionFromPointToPoint(entryPoint,
					landMap.getCentroid());
			LSystemClusterAlgorithm.createRouteVariation(entryPoint.getId(), direction,
					ClusterConfiguration.ARTERIAL_BRANCH);
			break;
		}

		// 4. We clusterize the points
		LSystemClusterAlgorithm.clusterize();

		// 5. Zonification
		ClusterLotizationAlgorithm.landMap = LSystemClusterAlgorithm.landMap;
		ClusterLotizationAlgorithm.zonify();
		
		ClusterLotizationAlgorithm.landMap.printMapToFile();
		/*
		 * System.out.print(ClusterLotizationAlgorithm.landMap.stringify());
		 */
	}
}