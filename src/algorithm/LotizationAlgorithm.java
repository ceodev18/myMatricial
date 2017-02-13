package algorithm;

import helpers.MapHelper;
import models.LandLot;
import models.LandMap;

public class LotizationAlgorithm {

	public static LandMap landMap;
	private static boolean[][] searchMap;

	public static void generateLots(int lotSize) {
		LandLot lotLand = new LandLot(lotSize, "A");
		searchMap = new boolean[landMap.getPointsx()][landMap.getPointsy()];

		for (int j = 0; j < landMap.getPointsy(); j++) {
			for (int i = 0; i < landMap.getPointsx(); i++) {
				if (!searchMap[i][j]) { // Not yet searched
					searchMap[i][j] = true;
					if (landMap.getLandPoint(MapHelper.formKey(i, j)).getType().equals("L")) {
						if (makeLot(lotLand, i, j)) {
							lotLand.updateEnum();
						}
					}
				}

			}
		}
	}

	private static boolean makeLot(LandLot lotLand, int x, int y) {
		if (((x + lotLand.getPointSize()) >= landMap.getPointsx())
				|| ((y + lotLand.getPointSize()) > landMap.getPointsy()))
			return false;
		for (int i = x; i < x + lotLand.getPointSize(); i++) {
			for (int j = y; j < y + lotLand.getPointSize(); j++) {
				if (!landMap.getLandPoint(MapHelper.formKey(i, j)).getType().equals("L")) {
					return false;
				}
			}
		}

		for (int i = x; i < x + lotLand.getPointSize(); i++) {
			for (int j = y; j < y + lotLand.getPointSize(); j++) {
				landMap.getLandPoint(MapHelper.formKey(i, j)).setType(lotLand.getId());
				searchMap[i][j] = true;
			}
		}

		return true;
	}
}