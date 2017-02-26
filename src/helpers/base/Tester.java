package helpers.base;

import algorithm.base.LSystemRouteAlgorithm;
import algorithm.base.LotizationAlgorithm;
import interfaces.Constants;
import models.LandMap;
import models.LandPoint;

public class Tester {
	public static void main(String[] args) {
		LandMap landMap = new LandMap(40, 40);
		LandPoint landPoint = new LandPoint(20, 0, false, false, true, false, false);
		LSystemRouteAlgorithm.landMap = landMap;
		LSystemRouteAlgorithm.createRoute(landPoint.getId(), Constants.NORTH, Constants.AVENUE_BRANCH, 120, 0);
		//LSystemRouteAlgorithm.landMap.printMap();
		
		LotizationAlgorithm.landMap = LSystemRouteAlgorithm.landMap;
		int lotSize = 240;
		LotizationAlgorithm.generateLots(lotSize);
		LotizationAlgorithm.landMap.printMap();
		
	}
}