package algorithm.clusterVariation;

import java.util.ArrayList;
import java.util.List;

import helpers.base.MapHelper;
import helpers.clusterVariation.CDirectionHelper;
import interfaces.ClusterConfiguration;
import models.clusterVariation.ClusterLandMap;
import models.clusterVariation.ClusterLandPoint;
import models.clusterVariation.ClusterLandRoute;
import models.clusterVariation.ClusterPolygon;

public class LSystemClusterAlgorithm {
	public static ClusterLandMap landMap;

	public static void createRoute(int entryPointId, int direction, int branchType) {
		ClusterLandPoint currentPoint = landMap.findPoint(entryPointId);
		// due to the vastness of the maps there is a error margin to cover
		ClusterLandRoute clusterLandRoute = new ClusterLandRoute();
		clusterLandRoute.setDirection(direction);
		clusterLandRoute.setInitialPointId(entryPointId);

		int extension = 0;
		while (currentPoint.isMapLimit(direction)) {
			int nextPointId = currentPoint.findNeighbour(direction);
			currentPoint = landMap.findPoint(nextPointId);
			extension++;
		}

		landMap.getNodes().add(currentPoint.getId());
		boolean first = true;
		while (!currentPoint.isMapLimit(direction)) {
			if (first) {
				first = false;
				landMap.markVariation(currentPoint.getId(), ClusterConfiguration.NODE,
						ClusterConfiguration.TYPE_OUTER_NODE);
			} else {
				if (!currentPoint.getType().equals(ClusterConfiguration.EMPTY)) {
					landMap.getNodes().add(currentPoint.getId());
					landMap.markVariation(currentPoint.getId(), ClusterConfiguration.NODE,
							ClusterConfiguration.TYPE_INNER_NODE);
				} else {
					landMap.markVariation(currentPoint.getId(), branchType, ClusterConfiguration.TYPE_NO_NODE);
				}
			}
			int nextPointId = currentPoint.findNeighbour(direction);
			currentPoint = landMap.findPoint(nextPointId);
			extension++;
		}
		landMap.getNodes().add(currentPoint.getId());
		landMap.markVariation(currentPoint.getId(), ClusterConfiguration.NODE, ClusterConfiguration.TYPE_OUTER_NODE);

		clusterLandRoute.setExtension(extension);
		clusterLandRoute.setFinalPointId(currentPoint.getId());
		clusterLandRoute.setType(branchType);
		landMap.getLandRoutes().add(clusterLandRoute);
	}

	public static void clusterize() {
		// 1. we need to now the main route size
		ClusterLandRoute mainRoute = landMap.getLandRoutes().get(0);
		int numClusters = mainRoute.getExtension() / ClusterConfiguration.BASE_CLUSTER_SIZE;
		int entryPointId = mainRoute.getInitialPointId();

		// Once the collector branches are created we need to create the non
		// collector running orthogonal to the main
		List<Integer> orthogonalDirections = CDirectionHelper.randomOrthogonalDirection(mainRoute.getDirection());
		for (int i = 0; i < numClusters; i++) {
			entryPointId = MapHelper.moveKeyByOffsetAndDirection(entryPointId, ClusterConfiguration.BASE_CLUSTER_SIZE,
					mainRoute.getDirection());
			createRoute(entryPointId, orthogonalDirections.get(0), ClusterConfiguration.COLLECTOR_BRANCH);
			createRoute(entryPointId, orthogonalDirections.get(1), ClusterConfiguration.COLLECTOR_BRANCH);
		}

		int upperParallelId = mainRoute.getInitialPointId();
		int current = 0;
		while (true) {
			if (current % 2 == 0) {
				// the parallel should be houseLength (as 8 is already used
				// should be somewhere between 12 and more) and then road
				upperParallelId = MapHelper.moveKeyByOffsetAndDirection(upperParallelId,
						2 * ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE, orthogonalDirections.get(0));
			} else {
				// then BASE_CLUSTER_SIZE
				upperParallelId = MapHelper.moveKeyByOffsetAndDirection(upperParallelId,
						ClusterConfiguration.BASE_CLUSTER_SIZE, orthogonalDirections.get(0));
			}
			if (!landMap.landPointisOnMap(upperParallelId))
				break;
			createRoute(upperParallelId, mainRoute.getDirection(), ClusterConfiguration.LOCAL_BRANCH);
			current++;
		}

		int lowerParallelId = mainRoute.getInitialPointId();
		current = 0;
		while (true) {
			if (current % 2 == 0) {
				lowerParallelId = MapHelper.moveKeyByOffsetAndDirection(lowerParallelId,
						2 * ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE, orthogonalDirections.get(1));
			} else {
				lowerParallelId = MapHelper.moveKeyByOffsetAndDirection(lowerParallelId,
						ClusterConfiguration.BASE_CLUSTER_SIZE, orthogonalDirections.get(1));
			}
			if (!landMap.landPointisOnMap(upperParallelId))
				break;
			createRoute(lowerParallelId, mainRoute.getDirection(), ClusterConfiguration.LOCAL_BRANCH);
			current++;
		}
		// We define the cluster areas.
		List<ClusterPolygon> figures = defineFigures();

		// Finally we create the lots given their points to lotize themss
		ClusterLotizationAlgorithm.landMap = landMap;
		for (int i = 0; i < figures.size(); i++) {
			ClusterLotizationAlgorithm.zonify(figures);
		}
	}

	private static List<ClusterPolygon> defineFigures() {
		MapHelper.orderNodes(landMap.getNodes());
		List<ClusterPolygon> polygons = new ArrayList<>();
		int[] positions = new int[landMap.getNodes().size()];
		for (int i = 0; i < landMap.getNodes().size(); i++) {
			int lastW = 0;
			boolean first = true;
			for (int j = 0; j < 4; j++) {
				ClusterPolygon clusterPolygon = new ClusterPolygon();
				boolean out = false;
				for (int w = lastW; w < i + 1; w++) {
					if (positions[i] < 4) {
						if(){//rule 1
							if(first)lastW = w; 
						}else if(){//rule 2
							
						}else if(){//rule 3
							
						}
						
					}
				}
				polygons.add(clusterPolygon);
			}
		}

		return polygons;
	}
}