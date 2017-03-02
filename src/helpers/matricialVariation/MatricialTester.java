package helpers.matricialVariation;

import java.util.ArrayList;
import java.util.List;

import models.base.LandMap;
import models.base.LandPoint;

public class MatricialTester {
	public static void main(String[] args) {
		// int large, width
		// int slope
		// poligonal vertices
		// entering points
		// minLateralLot maxLateralLot
		// minAreaLot maxAreaLot
		// minGeographicDensity maxGeographicDensity
		
		//Limiting points = [1834,0][1594,1434][862,1623][0,588][1013,183]
	    //Entry points = [327,981][1710,742]
		//XDistance: 1834
		//YDistance: 1623
		
		int large = 1834, width = 1623;
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
		//we must reuse the first one as the last
		landPoint = new LandPoint(1833, 0, false, false, true, false, false);
		polygon.add(landPoint);
		landMap.createBorderFromPolygon(polygon);
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