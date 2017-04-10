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

	public ClusterPolygon() {
		points = new ArrayList<>();
	}

	public ClusterPolygon(List<Integer> nodes) {
		points = new ArrayList<>();
		for (int i = 0; i < nodes.size(); i++) {
			points.add(nodes.get(i));
		}
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
			centroid = findCentroid();
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

		// TODO here, if the polygon has a non tolerable lateral distance
		// between point and point, then we should apply reduction formula

		for (int i = 0; i < points.size(); i++) {
			int[] xyInitial = ClusterMapHelper.breakKey(points.get(i));
			int[] xyFinal = ClusterMapHelper.breakKey(points.get((i + 1) % points.size()));

			// we find the unit vector
			double[] unitVector = new double[2];
			unitVector[0] = (xyFinal[0] - xyInitial[0])
					/ Math.sqrt(Math.pow(xyFinal[0] - xyInitial[0], 2) + Math.pow(xyFinal[1] - xyInitial[1], 2));
			unitVector[1] = (xyFinal[1] - xyInitial[1])
					/ Math.sqrt(Math.pow(xyFinal[0] - xyInitial[0], 2) + Math.pow(xyFinal[1] - xyInitial[1], 2));
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

			double gradient = (xyFinal[1] - xyInitial[1]) * 1.0 / (xyFinal[0] - xyInitial[0]);
			gradients.add(gradient);

			double bA;
			bA = variationA[1] - gradient * variationA[0];
			double distanceToCentroidA = distancefromProjectedPointToCentroid(variationA, gradient, bA);

			double bB;
			bB = variationB[1] - gradient * variationB[0];
			double distanceToCentroidB = distancefromProjectedPointToCentroid(variationB, gradient, bB);

			if (distanceToCentroidB < distanceToCentroidA) {
				offsets.add(bB);
				variations.add(variationB);
			} else {
				offsets.add(bA);
				variations.add(variationA);
			}
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

			xy[0] = (int) ((offsets.get(i) - offsets.get(previous)) / (gradients.get(previous) - gradients.get(i)));
			xy[1] = (int) (gradients.get(previous) * xy[0] + offsets.get(previous));
			shrinkedList.add(ClusterMapHelper.formKey(xy[0], xy[1]));
		}

		// validity check
		if (insidePolygon(shrinkedList)) {
			return shrinkedList;
		} else {
			return new ArrayList<>();
		}
	}

	private double distancefromProjectedPointToCentroid(double[] variation, Double gradient, double b) {
		if (gradient == 0) {
			return Math.abs(centroid[1] - variation[1]);
		} else if (gradient.isInfinite()) {
			return Math.abs(centroid[0] - variation[0]);
		} else {
			double orthogonalGradient = -1 / gradient;
			double orthogonalB = centroid[1] - centroid[0] * orthogonalGradient;
			double[] variationP = new double[2];

			variationP[0] = (orthogonalB - b) / (gradient - orthogonalGradient);
			variationP[1] = orthogonalGradient * variationP[1] + orthogonalB;
			return Math.sqrt(Math.pow(centroid[0] - variationP[0], 2) + Math.pow(centroid[1] - variationP[1], 2));
		}
	}

	private boolean insidePolygon(List<Integer> shrinkedList) {
		for (int i = 0; i < shrinkedList.size(); i++) {
			if (!isInsidePolygon(shrinkedList.get(i))) {
				return false;
			}
		}
		return true;
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
		List<Integer> area = vectorShrinking(1);
		int goDeeper = 1;
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

	public void setPoints(List<Integer> points) {
		this.points = points;
	}

	public List<Integer> translateTowardCenter(int size) {
		List<Integer> shrinkedList = new ArrayList<>();
		List<Double> gradients = new ArrayList<>();
		List<Double> offsets = new ArrayList<>();
		List<double[]> variations = new ArrayList<>();

		// TODO here, if the polygon has a non tolerable lateral distance
		// between point and point, then we should apply reduction formula
		for (int i = 0; i < points.size(); i++) {
			int[] xyInitial = ClusterMapHelper.breakKey(points.get(i));
			int[] xyFinal = ClusterMapHelper.breakKey(points.get((i + 1) % points.size()));

			// we find the unit vector
			double[] unitVector = new double[2];
			unitVector[0] = (xyFinal[0] - xyInitial[0])
					/ Math.sqrt(Math.pow(xyFinal[0] - xyInitial[0], 2) + Math.pow(xyFinal[1] - xyInitial[1], 2));
			unitVector[1] = (xyFinal[1] - xyInitial[1])
					/ Math.sqrt(Math.pow(xyFinal[0] - xyInitial[0], 2) + Math.pow(xyFinal[1] - xyInitial[1], 2));
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

			Double gradient = (xyFinal[1] - xyInitial[1]) * 1.0 / (xyFinal[0] - xyInitial[0]);
			gradients.add(gradient);

			if (gradient == 0 || gradient.isInfinite()) {
				double bA;
				bA = variationA[1] - gradient * variationA[0];
				double distanceToCentroidA = distancefromProjectedPointToCentroid(variationA, gradient, bA);

				double bB;
				bB = variationB[1] - gradient * variationB[0];
				double distanceToCentroidB = distancefromProjectedPointToCentroid(variationB, gradient, bB);

				if (distanceToCentroidB < distanceToCentroidA) {
					offsets.add(bB);
					variations.add(variationB);
				} else {
					offsets.add(bA);
					variations.add(variationA);
				}
			} else {
				double b = -xyInitial[0] * gradient + xyInitial[1];
				variations.add(new double[2]);
				offsets.add(b);
			}

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

			xy[0] = (int) ((offsets.get(i) - offsets.get(previous)) / (gradients.get(previous) - gradients.get(i)));
			xy[1] = (int) (gradients.get(previous) * xy[0] + offsets.get(previous));
			shrinkedList.add(ClusterMapHelper.formKey(xy[0], xy[1]));
		}
		return shrinkedList;
	}

	public int getArea() {
		int area = 0;
		for (int i = 0; i < points.size(); i++) {
			int[] xyInitial = ClusterMapHelper.breakKey(points.get(i));
			int[] xyFinal = ClusterMapHelper.breakKey(points.get((i + 1) % points.size()));
			area += xyInitial[0] * xyFinal[1] - xyInitial[1] * xyFinal[0];
		}
		return area / 2;
	}

	public boolean canBelotized() {
		for (int i = 0; i < points.size(); i++) {
			int[] xyInitial = ClusterMapHelper.breakKey(points.get(i));
			int[] xyFinal = ClusterMapHelper.breakKey(points.get((i + 1) % points.size()));
			int distance = (int) Math
					.sqrt(Math.pow(xyInitial[0] - xyFinal[0], 2) + Math.pow(xyInitial[1] - xyFinal[1], 2));
			if (distance < (ClusterConfiguration.HOUSE_DEPTH_MINIMUN_SIZE + ClusterConfiguration.LOCAL_BRANCH)) {
				return false;
			}
		}
		return true;
	}

}