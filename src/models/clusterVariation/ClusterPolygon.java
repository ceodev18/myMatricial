package models.clusterVariation;

import java.util.ArrayList;
import java.util.List;

import helpers.base.MapHelper;

public class ClusterPolygon {
	private List<Integer> points;
	private int[] centroid;
	private int[] squareLimits;
	private boolean complete;

	public ClusterPolygon() {
		points = new ArrayList<>();
	}

	public int[] getCentroid() {
		return centroid;
	}

	public void setCentroid(int[] centroid) {
		this.centroid = centroid;
	}

	public int[] getSquareLimits() {
		return squareLimits;
	}

	public void setSquareLimits(int[] squareLimits) {
		this.squareLimits = squareLimits;
	}

	public List<Integer> getPoints() {
		return points;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
		if (complete) {
			findCentroid();
		}
	}

	public boolean isComplete() {
		return complete;
	}

	public List<Integer> centroidShrinking(int size) {
		List<Integer> shrinkedList = new ArrayList<>();
		for (int i = 0; i < points.size(); i++) {
			int[] xy = MapHelper.breakKey(points.get(i));

			if (centroid[0] > xy[0])
				xy[0] = xy[0] + size > centroid[0] ? centroid[0] : xy[0] + size;
			else
				xy[0] = xy[0] - size < centroid[0] ? centroid[0] : xy[0] - size;

			if (centroid[1] > xy[1])
				xy[1] = xy[1] + size > centroid[1] ? centroid[1] : xy[1] + size;
			else
				xy[1] = xy[1] - size < centroid[1] ? centroid[1] : xy[1] - size;

			shrinkedList.add(i, MapHelper.formKey(xy[0], xy[1]));
		}
		return shrinkedList;
	}

	public List<Integer> vectorShrinking(int size) {
		List<Integer> shrinkedList = new ArrayList<>();
		List<Double> gradients = new ArrayList<>();
		List<Double> offsets = new ArrayList<>();
		List<double[]> variations = new ArrayList<>();

		for (int i = 0; i < points.size(); i++) {
			int[] xyInitial = MapHelper.breakKey(points.get(i));
			int[] xyFinal = MapHelper.breakKey(points.get((i + 1) % points.size()));

			// we find the unit vector
			double[] unitVector = new double[2];
			unitVector[0] = (xyFinal[0] - xyInitial[0])
					/ Math.sqrt(Math.pow(xyFinal[0] - xyInitial[0], 2) + Math.pow(xyFinal[1] - xyInitial[1], 2));
			unitVector[1] = (xyFinal[1] - xyInitial[1])
					/ Math.sqrt(Math.pow(xyFinal[0] - xyInitial[0], 2) + Math.pow(xyFinal[1] - xyInitial[1], 2));

			double gradient = (xyFinal[1] - xyInitial[1]) * 1.0 / (xyFinal[0] - xyInitial[0]);
			gradients.add(gradient);
			// then the perpendicular
			double[] perpendicularUnitVector = new double[2];
			perpendicularUnitVector[0] = unitVector[1];
			perpendicularUnitVector[1] = -unitVector[0];

			double[] variationA = new double[2];
			double[] variationB = new double[2];

			variationA[0] = xyInitial[0] + size * perpendicularUnitVector[0];
			variationA[1] = xyInitial[1] + size * perpendicularUnitVector[1];

			variationB[0] = xyInitial[0] - size * perpendicularUnitVector[0];
			variationB[1] = xyInitial[1] - size * perpendicularUnitVector[1];

			double distanceToCentroidA = distanceToCentroid(variationA);
			double distanceToCentroid = distanceToCentroid(xyInitial);

			double b;
			if (distanceToCentroid > distanceToCentroidA) {
				b = variationA[1] - gradient * variationA[0];
				variations.add(variationA);
			} else {
				b = variationB[1] - gradient * variationB[0];
				variations.add(variationB);
			}
			offsets.add(b);
		}

		// Special cases. When infinity and when 0 if infinity y=has a
		// number and the other is perpendicular. Such xy
		for (int i = 0; i < offsets.size(); i++) {
			int xy[] = new int[2];
			int previous = i - 1;
			if (i == 0) {
				previous = offsets.size() - 1;
			}

			int infinite = 0, zero = 0;
			if (gradients.get(i).isInfinite()) {
				infinite = 1;
				xy[0] = (int) variations.get(i)[0];
			}

			if (gradients.get(previous).isInfinite()) {
				infinite = 2;
				xy[0] = (int) variations.get(previous)[0];
			}

			if (gradients.get(i) == 0.0) {
				zero = 1;
				xy[1] = (int) variations.get(i)[1];
			}

			if (gradients.get(previous) == 0.0) {
				zero = 2;
				xy[1] = (int) variations.get(previous)[1];
			}

			// squared box
			if (infinite > 0 && zero > 0) {
				shrinkedList.add(MapHelper.formKey(xy[0], xy[1]));
				continue;
			}

			// square and non linear up and side
			if (infinite > 0) {
				if (infinite == 1) {
					xy[1] = (int) (gradients.get(previous) * xy[0] + offsets.get(previous));
				} else {
					xy[1] = (int) (gradients.get(i) * xy[0] + offsets.get(i));
				}
				shrinkedList.add(MapHelper.formKey(xy[0], xy[1]));
				continue;
			}

			// square and non linear down and side
			if (zero > 0) {
				if (zero == 1) {
					xy[0] = (int) ((xy[1] - offsets.get(previous)) / gradients.get(previous));
				} else {
					xy[0] = (int) ((xy[1] - offsets.get(i)) / gradients.get(i));
				}
				shrinkedList.add(MapHelper.formKey(xy[0], xy[1]));
				continue;
			}

			xy[1] = (int) (gradients.get(previous) * xy[0] + offsets.get(previous));
			xy[0] = (int) ((offsets.get(i) - offsets.get(previous)) / (gradients.get(previous) - gradients.get(i)));
			shrinkedList.add(MapHelper.formKey(xy[0], xy[1]));
		}

		// validity check
		for (int i = 0; i < shrinkedList.size(); i++) {
			if (!isInsidePolygon(shrinkedList.get(i))) {
				shrinkedList = new ArrayList<>();
			}
		}

		return shrinkedList;
	}

	private boolean isInsidePolygon(Integer vertexId) {
		boolean c = false;
		int[] xy = MapHelper.breakKey(vertexId);
		for (int i = 0, j = getPoints().size() - 1; i < getPoints().size(); j = i++) {
			if (((MapHelper.breakKey(getPoints().get(i))[1] > xy[1]) != (MapHelper
					.breakKey(getPoints().get(j))[1] > xy[1]))
					&& (xy[0] < (MapHelper.breakKey(getPoints().get(j))[0] - MapHelper.breakKey(getPoints().get(i))[0])
							* (xy[1] - MapHelper.breakKey(getPoints().get(i))[1])
							/ (MapHelper.breakKey(getPoints().get(j))[1] - MapHelper.breakKey(getPoints().get(i))[1])
							+ MapHelper.breakKey(getPoints().get(i))[0]))
				c = !c;
		}
		return c;
	}

	private double distanceToCentroid(int[] initial) {
		return Math.sqrt(Math.pow(centroid[0] - initial[0], 2) + Math.pow(centroid[1] - initial[1], 2));
	}

	private double distanceToCentroid(double[] variation) {
		return Math.sqrt(Math.pow(centroid[0] - variation[0], 2) + Math.pow(centroid[1] - variation[1], 2));
	}

	public int[] findCentroid() {
		int xLimits[] = new int[2];
		xLimits[1] = -1000;
		xLimits[0] = -1000;

		int yLimits[] = new int[2];
		yLimits[1] = -1000;
		yLimits[0] = -1000;

		for (int i = 0; i < points.size(); i++) {
			int[] xy = MapHelper.breakKey(points.get(i));
			if (i == 0) {
				xLimits[1] = xy[0];
				xLimits[0] = xy[0];
				yLimits[1] = xy[1];
				yLimits[0] = xy[1];
			} else {
				if (xLimits[1] < xy[0])
					xLimits[1] = xy[0];
				if (xLimits[0] > xy[0])
					xLimits[0] = xy[0];
				if (yLimits[1] < xy[1])
					yLimits[1] = xy[1];
				if (yLimits[0] > xy[1])
					yLimits[0] = xy[1];
			}
		}

		// inferiorleft, inferiorright, upright
		squareLimits = new int[4];
		squareLimits[0] = xLimits[0];
		squareLimits[1] = xLimits[1];
		squareLimits[2] = yLimits[0];
		squareLimits[3] = yLimits[1];

		return new int[] { ((xLimits[0] + xLimits[1]) / 2), ((yLimits[0] + yLimits[1]) / 2) };
	}

	public void printPolygon() {

		if (isComplete()) {
			System.out.println("Polygon is complete");
			for (int i = 0; i < points.size(); i++) {
				System.out.print(i + "=" + points.get(i) + ", ");
			}
		} else {
			System.out.println("Polygon is not complete");
			for (int i = 0; i < points.size(); i++) {
				System.out.print(i + "=" + points.get(i) + ", ");
			}
		}
		System.out.println();
	}

	public List<List<Integer>> routeZone(int initialShrink, int size) {
		List<List<Integer>> areas = new ArrayList<>();
		List<Integer> area = vectorShrinking(initialShrink);
		if (area.size() != 0) {
			areas.add(area);
			for (int i = initialShrink + 1; i < initialShrink + size; i++) {
				area = vectorShrinking(i);
				if (area.size() != 0) {
					areas.add(area);
				} else {
					break;
				}
			}
		}
		return areas;
	}

	public List<List<Integer>> parkZone(int initialDepth) {
		List<List<Integer>> areas = new ArrayList<>();
		List<Integer> area = vectorShrinking(initialDepth);
		int goDeeper = initialDepth;
		if (area.size() != 0) {
			areas.add(area);
			while (minimunDistanceBetweenVertex(area) > 1) {
				goDeeper++;
				area = vectorShrinking(goDeeper);
				if (area.size() != 0) {
					areas.add(area);
				}
			}
		}
		return areas;
	}

	private double minimunDistanceBetweenVertex(List<Integer> area) {
		double minimunDistance = 30000000;
		for (int i = 0; i < area.size(); i++) {
			int[] xyInitial = MapHelper.breakKey(area.get(i));
			int[] xyFinal = MapHelper.breakKey(area.get((i + 1) % area.size()));
			double distance = Math
					.sqrt(Math.pow(xyInitial[0] - xyFinal[0], 2) + Math.pow(xyInitial[1] - xyFinal[1], 2));
			if (minimunDistance > distance) {
				minimunDistance = distance;
			}
		}
		return minimunDistance;
	}


}