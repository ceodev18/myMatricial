package helpers;

import algorithm.LSystemRouteAlgorithm;
import interfaces.Constants;
import models.LandMap;
import models.LandPoint;

public class Tester {
	public static void main(String[] args) {
		LandMap landMap = new LandMap(60, 60);
		LandPoint landPoint = new LandPoint(30, 0, false, false, true, false, false);
		//List<LandRoute> routes = new ArrayList<>();
		
		//TODO Failed, Remember to erase
		//GreedyRouteAlgorithm.landMap = landMap;
		//GreedyRouteAlgorithm.createRoute(landPoint.getId(), Constants.NORTH, Constants.AVENUE_BRANCH, 3, 1);
		//GreedyRouteAlgorithm.landMap.printMap();
		
		LSystemRouteAlgorithm.landMap = landMap;
		/*routes = */LSystemRouteAlgorithm.createRoute(landPoint.getId(), Constants.NORTH, Constants.AVENUE_BRANCH, 10, 0);
		LSystemRouteAlgorithm.landMap.printMap();

	}
}