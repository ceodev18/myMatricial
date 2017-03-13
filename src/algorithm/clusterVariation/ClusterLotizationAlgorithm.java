package algorithm.clusterVariation;

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
		for (int y = landMap.getPointsy()-1; y >= 0; y--) {
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
				
				if (landMap.isNode(x, y) && landMap.findPoint(MapHelper.formKey((x + 1), y)).getType()
						.equals(ClusterConfiguration.NODE_MARK) &&
						landMap.findPoint(MapHelper.formKey((x + 1), (y+1))).getType()
						.equals(ClusterConfiguration.EMPTY_MARK)) {
					ClusterPolygon clusterPolygon = new ClusterPolygon();
					System.out.println("x:" + x + " y:" + y);
					System.out.println("Inside Right");
					int nextPoint = createOrganicCoverture(MapHelper.formKey(x, y), Constants.EAST, clusterPolygon);
					//if (nextPoint != -1) {
					//	x = nextPoint - 1;// this is the next point where we
						// should find what we want
					//	System.out.println("Next x:" + x + " y:" + y);
					//}
					clusterPolygon.printPolygon();
					//return;// just for testing my hypothesis
				}

				if (landMap.isNode(x, y) && landMap.findPoint(MapHelper.formKey(x, y + 1)).getType()
						.equals(ClusterConfiguration.NODE_MARK)&&
						landMap.findPoint(MapHelper.formKey((x - 1), (y+1))).getType()
						.equals(ClusterConfiguration.EMPTY_MARK)) {
					ClusterPolygon clusterPolygon = new ClusterPolygon();
					System.out.println("x:" + x + " y:" + y);
					System.out.println("Inside Up");
					createOrganicCoverture(MapHelper.formKey(x, y), Constants.NORTH, clusterPolygon);
					clusterPolygon.printPolygon();
					//return;// just for testing my hypothesis
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
					createOrganicCoverture(initialKey, Constants.NORTH, clusterPolygon);
					borderFound = true;
				}
				break;
			case Constants.NORTH:
				if (landMap.findPoint(MapHelper.formKey(firstNode[0] - 1, firstNode[1])).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					createOrganicCoverture(initialKey, Constants.WEST, clusterPolygon);
					borderFound = true;
				}
				break;
			case Constants.WEST:
				if (landMap.findPoint(MapHelper.formKey(firstNode[0], firstNode[1] - 1)).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					createOrganicCoverture(initialKey, Constants.SOUTH, clusterPolygon);
					borderFound = true;
				}
				break;
			case Constants.SOUTH:
				if (landMap.findPoint(MapHelper.formKey(firstNode[0] + 1, firstNode[1])).getType()
						.equals(ClusterConfiguration.NODE_MARK)) {
					borderFound = true;
					clusterPolygon.getPoints().add(initialKey);
					clusterPolygon.setComplete(true);
				}
				break;
			}

			System.out.println("x:" + firstNode[0] + " y:" + firstNode[1] + " type: "
					+ landMap.findPoint(MapHelper.formKey(firstNode[0], firstNode[1])).getType());

			
			if (landMap.findPoint(MapHelper.formKey(firstNode[0], firstNode[1])).getType()
					.equals(ClusterConfiguration.OUTSIDE_POLYGON_MARK)) {
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

// private static void perfectZonification() {
// TODO this method is only used on figures where the park couldnt be
// big enough.
// it is the default zonification done by the architects. View page of
// the default zonification
// }