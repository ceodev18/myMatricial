package models.clusterVariation;

import java.util.ArrayList;
import java.util.List;

import helpers.clusterVariation.ClusterMapHelper;
import interfaces.clusterVariation.ClusterConfiguration;

public class ClusterPolygon {
	private List<Integer> points;
	private int[] centroid;
	private int[] squareLimits;
	private boolean complete;
	private int expansions;

	public ClusterPolygon() {
		points = new ArrayList<>();
		setExpansions(0);
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
			int[] xy = ClusterMapHelper.breakKey(points.get(i));

			if (centroid[0] > xy[0])
				xy[0] = xy[0] + size > centroid[0] ? centroid[0] : xy[0] + size;
			else
				xy[0] = xy[0] - size < centroid[0] ? centroid[0] : xy[0] - size;

			if (centroid[1] > xy[1])
				xy[1] = xy[1] + size > centroid[1] ? centroid[1] : xy[1] + size;
			else
				xy[1] = xy[1] - size < centroid[1] ? centroid[1] : xy[1] - size;

			shrinkedList.add(i, ClusterMapHelper.formKey(xy[0], xy[1]));
		}
		return shrinkedList;
	}

	public List<Integer> vectorShrinking(int size) {
		List<Integer> shrinkedList = new ArrayList<>();
		List<Double> gradients = new ArrayList<>();
		List<Double> offsets = new ArrayList<>();
		List<double[]> variations = new ArrayList<>();

		for (int i = 0; i < points.size(); i++) {
			int[] xyInitial = ClusterMapHelper.breakKey(points.get(i));
			int[] xyFinal = ClusterMapHelper.breakKey(points.get((i + 1) % points.size()));

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
				shrinkedList.add(ClusterMapHelper.formKey(xy[0], xy[1]));
				continue;
			}

			// square and non linear up and side
			if (infinite > 0) {
				if (infinite == 1) {
					xy[1] = (int) (gradients.get(previous) * xy[0] + offsets.get(previous));
				} else {
					xy[1] = (int) (gradients.get(i) * xy[0] + offsets.get(i));
				}
				shrinkedList.add(ClusterMapHelper.formKey(xy[0], xy[1]));
				continue;
			}

			// square and non linear down and side
			if (zero > 0) {
				if (zero == 1) {
					xy[0] = (int) ((xy[1] - offsets.get(previous)) / gradients.get(previous));
				} else {
					xy[0] = (int) ((xy[1] - offsets.get(i)) / gradients.get(i));
				}
				shrinkedList.add(ClusterMapHelper.formKey(xy[0], xy[1]));
				continue;
			}

			xy[1] = (int) (gradients.get(previous) * xy[0] + offsets.get(previous));
			xy[0] = (int) ((offsets.get(i) - offsets.get(previous)) / (gradients.get(previous) - gradients.get(i)));
			shrinkedList.add(ClusterMapHelper.formKey(xy[0], xy[1]));
		}

		// validity check
		if (/*!crossCheck(shrinkedList) &&*/ insidePolygon(shrinkedList) && areaCheck(shrinkedList)) {
			return shrinkedList;
		} else {
			return new ArrayList<>();
		}
	}

	private boolean areaCheck(List<Integer> shrinkedList) {
		return true;
		/*int areaSum = 0;
		for (int i = 0; i < shrinkedList.size(); i++) {
			int[] initialXY = ClusterMapHelper.breakKey(shrinkedList.get(i));
			int[] finalXY = ClusterMapHelper.breakKey(shrinkedList.get((i + 1) % shrinkedList.size()));
			areaSum += initialXY[0] * finalXY[1] - initialXY[1] * finalXY[0];
		}

		areaSum = areaSum / 2;
		if (areaSum < 900) {
			return false;
		} else {
			return true;
		}*/
	}

	private boolean insidePolygon(List<Integer> shrinkedList) {
		for (int i = 0; i < shrinkedList.size(); i++) {
			if (!isInsidePolygon(shrinkedList.get(i))) {
				return false;
			}
		}
		return true;
	}

	private boolean crossCheck(List<Integer> shrinkedList) {
		for (int i = 0; i < shrinkedList.size(); i++) {
			int[] in1XY = ClusterMapHelper.breakKey(shrinkedList.get(i));
			int[] fi1XY = ClusterMapHelper.breakKey(shrinkedList.get((i + 1) % shrinkedList.size()));
			int j = 0;

			while ((i + 3 + j) % shrinkedList.size() != i) {
				int[] in2XY = ClusterMapHelper.breakKey(shrinkedList.get((i + 2 + j) % shrinkedList.size()));
				int[] fi2XY = ClusterMapHelper.breakKey(shrinkedList.get((i + 3 + j) % shrinkedList.size()));
				if (segmentsIntersect(in1XY, fi1XY, in2XY, fi2XY)) {
					return false;
				}
				j++;
			}
		}
		return true;
	}

	private boolean segmentsIntersect(int[] in1xy, int[] fi1xy, int[] in2xy, int[] fi2xy) {
		float s1_x, s1_y, s2_x, s2_y;
		s1_x = fi1xy[0] - in1xy[0];
		s1_y = fi1xy[1] - in1xy[1];
		s2_x = fi2xy[0] - in2xy[0];
		s2_y = fi2xy[1] - in2xy[1];
		float s, t;

		if ((-s2_x * s1_y + s1_x * s2_y) == 0 || ((-s2_x * s1_y + s1_x * s2_y) == 0)) {
			return false;
		}

		s = (-s1_y * (in1xy[0] - in2xy[0]) + s1_x * (in1xy[1] - in2xy[1])) / (-s2_x * s1_y + s1_x * s2_y);
		t = (s2_x * (in1xy[1] - in2xy[1]) - s2_y * (in1xy[0] - in2xy[0])) / (-s2_x * s1_y + s1_x * s2_y);
		if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
			return true;
		}

		return false; // No collision
	}

	private boolean isInsidePolygon(Integer vertexId) {
		boolean c = false;
		int[] xy = ClusterMapHelper.breakKey(vertexId);
		for (int i = 0, j = getPoints().size() - 1; i < getPoints().size(); j = i++) {
			if (((ClusterMapHelper.breakKey(getPoints().get(i))[1] > xy[1]) != (ClusterMapHelper
					.breakKey(getPoints().get(j))[1] > xy[1]))
					&& (xy[0] < (ClusterMapHelper.breakKey(getPoints().get(j))[0]
							- ClusterMapHelper.breakKey(getPoints().get(i))[0])
							* (xy[1] - ClusterMapHelper.breakKey(getPoints().get(i))[1])
							/ (ClusterMapHelper.breakKey(getPoints().get(j))[1]
									- ClusterMapHelper.breakKey(getPoints().get(i))[1])
							+ ClusterMapHelper.breakKey(getPoints().get(i))[0]))
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
			int[] xy = ClusterMapHelper.breakKey(points.get(i));
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
				} else {
					break;
				}
			}
		}
		return areas;
	}

	public List<List<Integer>> contributionZone() {
		List<List<Integer>> areas = new ArrayList<>();
		List<Integer> area = vectorShrinking(0);
		int goDeeper = 0;
		if (area.size() != 0) {
			areas.add(area);
			while (minimunDistanceBetweenVertex(area) > 1) {
				goDeeper++;
				area = vectorShrinking(goDeeper);
				if (area.size() != 0) {
					areas.add(area);
				} else {
					break;
				}
			}
		}
		return areas;
	}

	private double minimunDistanceBetweenVertex(List<Integer> area) {
		double minimunDistance = 30000000;
		for (int i = 0; i < area.size(); i++) {
			int[] xyInitial = ClusterMapHelper.breakKey(area.get(i));
			int[] xyFinal = ClusterMapHelper.breakKey(area.get((i + 1) % area.size()));
			double distance = Math
					.sqrt(Math.pow(xyInitial[0] - xyFinal[0], 2) + Math.pow(xyInitial[1] - xyFinal[1], 2));
			if (minimunDistance > distance) {
				minimunDistance = distance;
			}
		}
		return minimunDistance;
	}

	public int getExpansions() {
		return expansions;
	}

	public void setExpansions(int expansions) {
		this.expansions = expansions;
	}

	public void rehashPolygon(int type) {
		List<Integer> reorderedPoints = new ArrayList<>();
		if (type == ClusterConfiguration.TYPE_SPECIAL) {
			for (int i = points.size() - 1; i >= 0; i--) {
				reorderedPoints.add(points.get(i));
			}
			points = reorderedPoints;
		} else {
			if (expansions > 0) {
				for (int i = expansions + 1; i < points.size(); i++) {
					reorderedPoints.add(points.get(i));
				}

				for (int i = 0; i < expansions + 1; i++) {
					reorderedPoints.add(points.get(i));
				}
				points = reorderedPoints;
			}
		}
	}
}