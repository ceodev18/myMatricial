package helpers;

import algorithm.LSystemRouteAlgorithm;
import interfaces.Constants;
import models.LandMap;
import models.LandPoint;

public class Tester {
	public static void main(String[] args) {
		LandMap landMap = new LandMap(40, 40);
		LandPoint landPoint = new LandPoint(30, 0, false, false, true, false, false);
		LSystemRouteAlgorithm.landMap = landMap;
		/*routes = */LSystemRouteAlgorithm.createRoute(landPoint.getId(), Constants.NORTH, 
				Constants.AVENUE_BRANCH, 10, 0);
		LSystemRouteAlgorithm.landMap.printMap();
	}
}