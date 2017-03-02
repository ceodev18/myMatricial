package algorithm.clusterVariation;

import models.clusterVariation.CLandMap;
import models.clusterVariation.CLandPoint;

public class LSystemClusterAlgorithm {
	public static CLandMap landMap;

	public static void createMainRoute(int entryPointId, int direction, int branchType) {
		CLandPoint currentPoint = landMap.findPoint(entryPointId);
		//due to the vastness of the maps there is a error margin that we must cover
		while (currentPoint.isMapLimit(direction)) {
			landMap.markVariation(currentPoint.getId(), branchType);
			int nextPointId = currentPoint.findNeighbour(direction);
			currentPoint = landMap.findPoint(nextPointId);
		}
		//then the true end
		while (!currentPoint.isMapLimit(direction)) {
			landMap.markVariation(currentPoint.getId(), branchType);
			int nextPointId = currentPoint.findNeighbour(direction);
			currentPoint = landMap.findPoint(nextPointId);
		}
	}
	
	
}