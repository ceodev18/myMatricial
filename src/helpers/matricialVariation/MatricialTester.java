package helpers.matricialVariation;

import java.util.ArrayList;
import java.util.List;

import algorithm.base.LSystemRouteAlgorithm;
import algorithm.base.LotizationAlgorithm;
import interfaces.Constants;
import models.LandMap;
import models.LandPoint;

public class MatricialTester {
	public static void main(String[] args) {
		LandMap landMap = new LandMap(40, 40);
		
		List<LandPoint> polygon = new ArrayList<>();
		LandPoint landPoint = new LandPoint(0, 20, false, false, true, false, false);
		polygon.add(landPoint);
		landPoint = new LandPoint(20, 39, false, false, true, false, false);
		polygon.add(landPoint);
		landPoint = new LandPoint(39, 15, false, false, true, false, false);
		polygon.add(landPoint);
		landPoint = new LandPoint(15, 0, false, false, true, false, false);
		polygon.add(landPoint);
		landPoint = new LandPoint(0, 20, false, false, true, false, false);
		polygon.add(landPoint);
		landMap.createBorderFromPolygon(polygon);
		landMap.printMap();
		
		/*
		landPoint = new LandPoint(20, 0, false, false, true, false, false);
		
		LSystemRouteAlgorithm.landMap = landMap;
		LSystemRouteAlgorithm.createRoute(landPoint.getId(), Constants.NORTH, Constants.AVENUE_BRANCH, 120, 0);
		//LSystemRouteAlgorithm.landMap.printMap();
		
		LotizationAlgorithm.landMap = LSystemRouteAlgorithm.landMap;
		int lotSize = 240;
		LotizationAlgorithm.generateLots(lotSize);
		LotizationAlgorithm.landMap.printMap();
		*/
	}
}