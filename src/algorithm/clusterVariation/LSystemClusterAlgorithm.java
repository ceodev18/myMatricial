package algorithm.clusterVariation;

import models.clusterVariation.ClusterLandMap;
import models.clusterVariation.ClusterLandPoint;

public class LSystemClusterAlgorithm {
	public static ClusterLandMap landMap;

	public static void createMainRoute(int entryPointId, int direction, int branchType) {
		ClusterLandPoint currentPoint = landMap.findPoint(entryPointId);
		// due to the vastness of the maps there is a error margin that we must
		// cover
		while (currentPoint.isMapLimit(direction)) {
			landMap.markVariation(currentPoint.getId(), branchType);
			int nextPointId = currentPoint.findNeighbour(direction);
			currentPoint = landMap.findPoint(nextPointId);
		}
		// then the true end
		while (!currentPoint.isMapLimit(direction)) {
			landMap.markVariation(currentPoint.getId(), branchType);
			int nextPointId = currentPoint.findNeighbour(direction);
			currentPoint = landMap.findPoint(nextPointId);
		}
	}

	public static void clusterize() {
		int numberofparks = landMap.getNumberOfParks();
		numberofparks++;
		for(int i = 0 ; i < numberofparks; i++){
			//bisect along one of the main routes
		}

	}

}