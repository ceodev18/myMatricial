package algorithm.clusterVariation;

import java.util.ArrayList;
import java.util.List;
import models.clusterVariation.CLandMap;
import models.clusterVariation.CLandPoint;

public class LSystemClusterAlgorithm {
	public static CLandMap landMap;

	public static void createMainRoute(int entryPointId, int direction, int branchType) {
		List<Integer> points = new ArrayList<>();
		CLandPoint currentPoint = landMap.findPoint(entryPointId);
		points.add(entryPointId);
		landMap.markVariation(entryPointId, branchType);
		while (!currentPoint.isMapLimit(direction)) {
			int nextPointId = currentPoint.findNeighbour(direction);
			points.add(nextPointId);
			currentPoint = landMap.findPoint(nextPointId);
			landMap.markVariation(entryPointId, branchType);
		}
	}
}