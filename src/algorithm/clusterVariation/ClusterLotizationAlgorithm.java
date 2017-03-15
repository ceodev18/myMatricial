package algorithm.clusterVariation;

import java.util.List;

import helpers.base.MapHelper;
import interfaces.ClusterConfiguration;
import interfaces.Constants;
import models.clusterVariation.ClusterLandMap;
import models.clusterVariation.ClusterPolygon;

public class ClusterLotizationAlgorithm {

	public static ClusterLandMap landMap;

	public static void zonify() {
		// findZonificationAreas();
		organicZonification();
	}

	private static void organicZonification() {
		// Organic zonification: This zonification searchs the figure and
		// reduces it in a reason configured by the user. if it can be done, it
		// becomes a new cluster. If not, it becomes simply defaults into a
		// perfect zonification
		System.out.println("Limit x:" + landMap.getPointsx() + "Limit y:" + landMap.getPointsy());
		// we should save the Ns used
		for (int y = landMap.getPointsy() - 1; y >= 0; y--) {
			boolean insidePolygon = false;
			for (int x = 0; x < landMap.getPointsx(); x++) {
				if (insidePolygon && landMap.findPoint(MapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
					break;
				}
				if (!insidePolygon && !landMap.findPoint(MapHelper.formKey(x, y)).getType()
						.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
					insidePolygon = true;
				}

				if (landMap.isNode(x, y)
						&& landMap.findPoint(MapHelper.formKey((x + 1), y)).getType()
								.equals(ClusterConfiguration.NODE_MARK)
						&& landMap.findPoint(MapHelper.formKey((x + 1), (y + 1))).getType()
								.equals(ClusterConfiguration.EMPTY_MARK)) {
					ClusterPolygon clusterPolygon = new ClusterPolygon();
					/* int nextPoint= */createOrganicCoverture(MapHelper.formKey(x, y), Constants.EAST, clusterPolygon);
					if (!clusterPolygon.isComplete()) {
						landMap.joinWithPolygonalBorder(clusterPolygon);
					}
					// clusterPolygon.printPolygon();
					// we create the routes surrounding the park
					clusterPolygon.setCentroid(clusterPolygon.findCentroid());
					List<List<Integer>> routes = clusterPolygon.routeZone(
							ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2, ClusterConfiguration.LOCAL_BRANCH_SIZE);
					for (int j = 0; j < routes.size(); j++) {
						landMap.createBorderFromPolygon(routes.get(j), ClusterConfiguration.LOCAL_MARK);
					}

					// we create the park
					List<List<Integer>> grass = clusterPolygon.parkZone(
							ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2 + ClusterConfiguration.LOCAL_BRANCH_SIZE);
					for (int j = 0; j < grass.size(); j++) {
						landMap.createBorderFromPolygon(grass.get(j), ClusterConfiguration.PARK_MARK);
					}

					// we create the houses
					List<List<Integer>> lowerBorder = clusterPolygon
							.routeZone(ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2 - 1, 1);
					if (lowerBorder.size() > 0) {
						landMap.lotize(lowerBorder.get(0), Constants.EAST, 0);
					}
					if ((routes.size() == 0) && (grass.size() == 0)) {
						// TODO lotize as full focus (library or other).
						clusterPolygon.printPolygon();
					}
				}
			}
		}
		// perfectZonification();
	}

	private static int createOrganicCoverture(int initialKey, int direction, ClusterPolygon clusterPolygon) {
		clusterPolygon.getPoints().add(initialKey);
		boolean borderFound = false;
		while (!borderFound) {
			initialKey = MapHelper.moveKeyByOffsetAndDirection(initialKey, 1, direction);
			int[] firstNode = MapHelper.breakKey(initialKey);
			switch (direction) {
			case Constants.EAST:
				// east, north, west, south
				if (landMap.findPoint(MapHelper.formKey(firstNode[0], firstNode[1] + 1)).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					return createOrganicCoverture(initialKey, Constants.NORTH, clusterPolygon);
				}
				break;
			case Constants.NORTH:
				if (landMap.findPoint(MapHelper.formKey(firstNode[0] - 1, firstNode[1])).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					return createOrganicCoverture(initialKey, Constants.WEST, clusterPolygon);
				}
				break;
			case Constants.WEST:
				if (landMap.findPoint(MapHelper.formKey(firstNode[0], firstNode[1] - 1)).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					return createOrganicCoverture(initialKey, Constants.SOUTH, clusterPolygon);
				}
				break;
			case Constants.SOUTH:
				if (landMap.findPoint(MapHelper.formKey(firstNode[0] + 1, firstNode[1])).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					borderFound = true;
					clusterPolygon.setComplete(true);
				}
				break;
			}

			if (!clusterPolygon.isComplete() && landMap.findPoint(MapHelper.formKey(firstNode[0], firstNode[1]))
					.getType().equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
				switch (direction) {
				case Constants.EAST:
					clusterPolygon.getPoints().add(MapHelper.formKey(firstNode[0] - 1, firstNode[1]));
					break;
				case Constants.NORTH:
					clusterPolygon.getPoints().add(MapHelper.formKey(firstNode[0], firstNode[1] - 1));
					break;
				}
				// TODO for the case of this 2 figures there are fixed answers
				// GOING from the initial point upwards
				// if up. Just going till finding a border will be enough
				// (Inverse createOrganicCoverture
				// that is the case for both cases.
				return -1;
			}
			landMap.findPoint(MapHelper.formKey(firstNode[0], firstNode[1])).setType("e");
		}
		return initialKey;
	}
}