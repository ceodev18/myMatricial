package models.matricial2035Variation;

import java.util.ArrayList;
import java.util.List;

import helpers.matricial2035Variation.Matricial2035MapHelper;
import interfaces.matricial2035Variation.Matricial2035Configuration;

public class Matricial2035Polygon {
	private List<Integer> points;
	private int[] centroid;
	private int[] squareLimits;
	private boolean complete;

	public Matricial2035Polygon() {
		points = new ArrayList<>();
	}

	public Matricial2035Polygon(List<Integer> nodes) {
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
			int[] xy = Matricial2035MapHelper.breakKey(points.get(i));

			if (centroid[0] > xy[0])
				xy[0] = xy[0] + size > centroid[0] ? centroid[0] : xy[0] + size;
			else
				xy[0] = xy[0] - size < centroid[0] ? centroid[0] : xy[0] - size;

			if (centroid[1] > xy[1])
				xy[1] = xy[1] + size > centroid[1] ? centroid[1] : xy[1] + size;
			else
				xy[1] = xy[1] - size < centroid[1] ? centroid[1] : xy[1] - size;

			shrinkedList.add(i, Matricial2035MapHelper.formKey(xy[0], xy[1]));
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

		if (!canBeReducedBySize(size)) {
			return new ArrayList<>();
		}

		for (int i = 0; i < points.size(); i++) {
			int[] xyInitial = Matricial2035MapHelper.breakKey(points.get(i));
			int[] xyFinal = Matricial2035MapHelper.breakKey(points.get((i + 1) % points.size()));

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
				shrinkedList.add(Matricial2035MapHelper.formKey(xy[0], xy[1]));
				continue;
			}

			// square and non linear up and side
			if (infinite > 0) {
				if (infinite == 1) {
					xy[1] = (int) (gradients.get(previous) * xy[0] + offsets.get(previous));
				} else {
					xy[1] = (int) (gradients.get(i) * xy[0] + offsets.get(i));
				}
				shrinkedList.add(Matricial2035MapHelper.formKey(xy[0], xy[1]));
				continue;
			}

			// square and non linear down and side
			if (zero > 0) {
				if (zero == 1) {
					xy[0] = (int) ((xy[1] - offsets.get(previous)) / gradients.get(previous));
				} else {
					xy[0] = (int) ((xy[1] - offsets.get(i)) / gradients.get(i));
				}
				shrinkedList.add(Matricial2035MapHelper.formKey(xy[0], xy[1]));
				continue;
			}

			xy[0] = (int) ((offsets.get(i) - offsets.get(previous)) / (gradients.get(previous) - gradients.get(i)));
			xy[1] = (int) (gradients.get(previous) * xy[0] + offsets.get(previous));
			shrinkedList.add(Matricial2035MapHelper.formKey(xy[0], xy[1]));
		}

		shrinkedList = verifyRedutionPolygonVersion2(shrinkedList);
		shrinkedList = verifyReduceCrossingsByBounds(shrinkedList);

		// validity check
		if (insidePolygon(shrinkedList)) {
			return shrinkedList;
		} else {
			return new ArrayList<>();
		}
	}

	private boolean canBeReducedBySize(int size) {
		for (int i = 0; i < points.size(); i++) {
			int[] xyInitial = Matricial2035MapHelper.breakKey(points.get(i));
			int[] xyFinal = Matricial2035MapHelper.breakKey(points.get((i + 1) % points.size()));
			int distance = 0;
			int denominator = Math.abs(xyInitial[0] - xyFinal[0]);
			if (denominator == 0) {
				distance = Math.abs(centroid[0] - xyInitial[0]);
				if (distance < size) {
					return false;
				} else {
					continue;
				}
			}

			int numerator = Math.abs(xyInitial[1] - xyFinal[1]);
			if (numerator == 0) {
				distance = Math.abs(centroid[1] - xyInitial[1]);
				if (distance < size) {
					return false;
				} else {
					continue;
				}
			}

			double m = (xyInitial[1] - xyFinal[1]) * 1.0 / (xyInitial[0] - xyFinal[0]);
			double b = xyInitial[1] - m * xyInitial[0];

			double pm = -1 / m;
			double pb = centroid[1] - pm * centroid[0];
			// xm + b = xpm + pb
			// x = (pb-b)/(m-pm)
			double px = (pb - b) / (m - pm);
			double py = px * pm + pb;

			distance = (int) Math.sqrt(Math.pow(px - centroid[0], 2) + Math.pow(py - centroid[1], 2));
			if (distance < size) {
				return false;
			}
		}
		return true;
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
		int[] xy = Matricial2035MapHelper.breakKey(vertexId);
		for (int i = 0, j = getPoints().size() - 1; i < getPoints().size(); j = i++) {
			if (((Matricial2035MapHelper.breakKey(getPoints().get(i))[1] > xy[1]) != (Matricial2035MapHelper
					.breakKey(getPoints().get(j))[1] > xy[1]))
					&& (xy[0] < (Matricial2035MapHelper.breakKey(getPoints().get(j))[0]
							- Matricial2035MapHelper.breakKey(getPoints().get(i))[0])
							* (xy[1] - Matricial2035MapHelper.breakKey(getPoints().get(i))[1])
							/ (Matricial2035MapHelper.breakKey(getPoints().get(j))[1]
									- Matricial2035MapHelper.breakKey(getPoints().get(i))[1])
							+ Matricial2035MapHelper.breakKey(getPoints().get(i))[0]))
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
			int[] xy = Matricial2035MapHelper.breakKey(points.get(i));
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
			int[] xyInitial = Matricial2035MapHelper.breakKey(area.get(i));
			int[] xyFinal = Matricial2035MapHelper.breakKey(area.get((i + 1) % area.size()));
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
			int[] xyInitial = Matricial2035MapHelper.breakKey(points.get(i));
			int[] xyFinal = Matricial2035MapHelper.breakKey(points.get((i + 1) % points.size()));

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
				shrinkedList.add(Matricial2035MapHelper.formKey(xy[0], xy[1]));
				continue;
			}

			// square and non linear up and side
			if (infinite > 0) {
				if (infinite == 1) {
					xy[1] = (int) (gradients.get(previous) * xy[0] + offsets.get(previous));
				} else {
					xy[1] = (int) (gradients.get(i) * xy[0] + offsets.get(i));
				}
				shrinkedList.add(Matricial2035MapHelper.formKey(xy[0], xy[1]));
				continue;
			}

			// square and non linear down and side
			if (zero > 0) {
				if (zero == 1) {
					xy[0] = (int) ((xy[1] - offsets.get(previous)) / gradients.get(previous));
				} else {
					xy[0] = (int) ((xy[1] - offsets.get(i)) / gradients.get(i));
				}
				shrinkedList.add(Matricial2035MapHelper.formKey(xy[0], xy[1]));
				continue;
			}

			xy[0] = (int) ((offsets.get(i) - offsets.get(previous)) / (gradients.get(previous) - gradients.get(i)));
			xy[1] = (int) (gradients.get(previous) * xy[0] + offsets.get(previous));
			shrinkedList.add(Matricial2035MapHelper.formKey(xy[0], xy[1]));
		}
		return shrinkedList;
	}

	public int getArea() {
		int area = 0;
		for (int i = 0; i < points.size(); i++) {
			int[] xyInitial = Matricial2035MapHelper.breakKey(points.get(i));
			int[] xyFinal = Matricial2035MapHelper.breakKey(points.get((i + 1) % points.size()));
			area += xyInitial[0] * xyFinal[1] - xyInitial[1] * xyFinal[0];
		}
		return area / 2;
	}

	public boolean canBelotized(int houseDepth) {
		for (int i = 0; i < points.size(); i++) {
			int[] xyInitial = Matricial2035MapHelper.breakKey(points.get(i));
			int[] xyFinal = Matricial2035MapHelper.breakKey(points.get((i + 1) % points.size()));
			int distance = (int) Math
					.sqrt(Math.pow(xyInitial[0] - xyFinal[0], 2) + Math.pow(xyInitial[1] - xyFinal[1], 2));
			if (distance < (houseDepth + Matricial2035Configuration.LOCAL_BRANCH)) {
				return false;
			}
		}
		return true;
	}

	private List<Integer> verifyReduceCrossingsByBounds(List<Integer> shrinkedList) {
		int maxX = -11111, minX = 999999, maxY = -11111, minY = 999999;
		// once bounded, its now possible to detect non regular intersections
		if (shrinkedList.size() <= 3) {
			return shrinkedList;
		}

		for (int i = 0; i < shrinkedList.size(); i++) {
			int[] xy = Matricial2035MapHelper.breakKey(shrinkedList.get(i));
			if (maxX < xy[0]) {
				maxX = xy[0];
			}
			if (minX > xy[0]) {
				minX = xy[0];
			}

			if (maxY < xy[1]) {
				maxY = xy[1];
			}
			if (minY > xy[1]) {
				minY = xy[1];
			}
		}

		for (int i = 0; i < shrinkedList.size(); i++) {
			int[] xyInitialA = Matricial2035MapHelper.breakKey(shrinkedList.get(i));
			int[] xyFinalA = Matricial2035MapHelper.breakKey(shrinkedList.get((i + 1) % shrinkedList.size()));

			if (xyInitialA[0] == 138) {
				// TODO erase
				int x = 1;
				x++;
			}

			for (int j = 0; j < shrinkedList.size(); j++) {
				int[] xyInitialB = Matricial2035MapHelper.breakKey(shrinkedList.get(j));
				int[] xyFinalB = Matricial2035MapHelper.breakKey(shrinkedList.get((j + 1) % shrinkedList.size()));

				if (sharesPointMember(xyInitialA, xyFinalA) || sharesPointMember(xyInitialA, xyInitialB)
						|| sharesPointMember(xyInitialA, xyFinalB) || sharesPointMember(xyFinalA, xyInitialB)
						|| sharesPointMember(xyFinalA, xyFinalB) || sharesPointMember(xyInitialB, xyFinalB)) {
					continue;
				}

				if ((xyInitialA[0] - xyFinalA[0]) == 0 || (xyInitialB[0] - xyFinalB[0]) == 0) {
					continue;
				}

				double mA = (double) ((xyInitialA[1] - xyFinalA[1]) * 1.0 / (xyInitialA[0] - xyFinalA[0]));
				double mB = (double) ((xyInitialB[1] - xyFinalB[1]) * 1.0 / (xyInitialB[0] - xyFinalB[0]));
				if (mA == mB) {// parallels
					continue;
				}

				double bA = xyInitialA[1] - xyInitialA[0] * mA;
				double bB = xyInitialB[1] - xyInitialB[0] * mB;
				int[] intersection = new int[2];
				// xma + ba = xmb + bb
				intersection[0] = (int) ((bA - bB) / (mB - mA));
				intersection[1] = (int) (intersection[0] * mB + bB);
				if (intersection[0] < maxX && intersection[0] > minX && intersection[1] < maxY
						&& intersection[1] > minY) {
					return new ArrayList<>();
				}

			}
		}

		return shrinkedList;
	}

	private boolean sharesPointMember(int[] xyA, int[] xyB) {
		return (xyA[0] == xyB[0]) && (xyA[1] == xyB[1]);
	}

	// TODO Neil s methods
	public List<Integer> verifyRedutionPolygonVersion2(List<Integer> shrinkedList) {
		List<Integer> auxList = new ArrayList<>();
		int k = 0;
		while (shrinkedList.size() != k) {
			if (shrinkedList.size() > 4) {
				int interscPoint = findIntersectionPointIntoTwoStraight(shrinkedList.get(k % shrinkedList.size()),
						shrinkedList.get((k + 1) % shrinkedList.size()),
						shrinkedList.get((k + 2) % shrinkedList.size()),
						shrinkedList.get((k + 3) % shrinkedList.size()), true);
				if (interscPoint != -1) {
					auxList = new ArrayList<>();
					for (int i = 0; i < shrinkedList.size(); i++) {
						if (i == ((k + 1) % shrinkedList.size())) {
							auxList.add(interscPoint);
							i++;
						} else {
							auxList.add(shrinkedList.get(i));
						}
					}
					k = 0;
					shrinkedList = auxList;
					continue;
				} else {
					auxList.add(shrinkedList.get(k));
				}
				k++;
				continue;
			}
			if (shrinkedList.size() == 4) {
				int interscPoint = findIntersectionPointIntoTwoStraight(shrinkedList.get(0), shrinkedList.get(1),
						shrinkedList.get(2), shrinkedList.get(3), true);
				double dis[] = new double[4];
				dis[0] = distanceOfPointToPoint(shrinkedList.get(0), shrinkedList.get(1));
				dis[1] = distanceOfPointToPoint(shrinkedList.get(1), shrinkedList.get(2));
				dis[2] = distanceOfPointToPoint(shrinkedList.get(2), shrinkedList.get(3));
				dis[3] = distanceOfPointToPoint(shrinkedList.get(3), shrinkedList.get(0));
				if (interscPoint != -1) {
					double min = dis[0];
					int caso = 0;
					for (int j = 1; j < 4; j++) {
						if (min > dis[j]) {
							min = dis[j];
							caso = j;
						}
					}
					for (int j = 0; j < 4; j++) {
						if (caso == 3 && j == 0) {
							continue;
						}
						if (caso == j) {
							auxList.add(interscPoint);
							j++;
						} else {
							auxList.add(shrinkedList.get(j));
						}
					}
					k = 0;
					shrinkedList = auxList;
					continue;
				} else {
					auxList = shrinkedList;
					k = 4;
					return auxList;
				}

			}
			if (shrinkedList.size() == 3) {
				auxList = shrinkedList;
				return auxList;
			}
		}
		return auxList;
	}

	public double distanceOfPointToPoint(int pointi, int pointf) {
		int xyPointF[] = Matricial2035MapHelper.breakKey(pointf);
		int xyPointI[] = Matricial2035MapHelper.breakKey(pointi);
		return Math.sqrt(Math.pow(xyPointF[0] - xyPointI[0], 2) + Math.pow(xyPointF[1] - xyPointI[1], 2));
	}

	public int findIntersectionPointIntoTwoStraight(int rect1Ini, int rect1End, int rect2Ini, int rect2End,
			boolean belong) {
		int pointSolution = -1;
		int xyRec1Ini[] = Matricial2035MapHelper.breakKey(rect1Ini);
		int xyRec1End[] = Matricial2035MapHelper.breakKey(rect1End);
		int xyRec2Ini[] = Matricial2035MapHelper.breakKey(rect2Ini);
		int xyRec2End[] = Matricial2035MapHelper.breakKey(rect2End);

		int underscore1 = (xyRec1End[0] - xyRec1Ini[0]);
		int underscore2 = (xyRec2End[0] - xyRec2Ini[0]);
		double gradient1 = 0;
		double gradient2 = 0;
		double b1 = 0;
		double b2 = 0;

		if (underscore1 == 0 && underscore2 == 0)
			return pointSolution; // would be the same straight or paralell
		if (underscore1 != 0) {
			gradient1 = (xyRec1End[1] - xyRec1Ini[1]) * 1.0 / underscore1;
			b1 = (xyRec1Ini[0] * xyRec1End[1] - (xyRec1End[0] * xyRec1Ini[1])) / (xyRec1Ini[0] - xyRec1End[0]);
		}
		if (underscore2 != 0) {
			gradient2 = (xyRec2End[1] - xyRec2Ini[1]) * 1.0 / underscore2;
			b2 = (xyRec2Ini[0] * xyRec2End[1] - (xyRec2End[0] * xyRec2Ini[1])) / (xyRec2Ini[0] - xyRec2End[0]);
		}
		if ((gradient1 == 0 && gradient2 == 0) || (gradient1 == gradient2))
			return pointSolution; // would be the same straight or paralell

		if (underscore1 == 0 && underscore2 != 0) {
			int lower = xyRec2Ini[0] < xyRec2End[0] ? xyRec2Ini[0] : xyRec2End[0];
			int upper = xyRec2Ini[0] > xyRec2End[0] ? xyRec2Ini[0] : xyRec2End[0];
			if ((lower <= xyRec1End[0] && xyRec1End[0] <= upper) || !belong) {
				double yAux = xyRec1End[0] * gradient2 + b2;
				Matricial2035MapHelper.round(yAux);
				pointSolution = Matricial2035MapHelper.formKey(xyRec1End[0], (int) yAux);

				return pointSolution;
			}

		}
		if (underscore2 == 0 && underscore1 != 0) {
			int lower = xyRec1Ini[0] < xyRec1End[0] ? xyRec1Ini[0] : xyRec1End[0];
			int upper = xyRec1Ini[0] > xyRec1End[0] ? xyRec1Ini[0] : xyRec1End[0];
			if ((lower <= xyRec2End[0] && xyRec2End[0] <= upper) || !belong) { // verify
																				// if
																				// the
																				// point
																				// belong
																				// to
																				// the
																				// straight
				double yAux = xyRec2End[0] * gradient1 + b1;
				Matricial2035MapHelper.round(yAux);
				pointSolution = Matricial2035MapHelper.formKey(xyRec2End[0], (int) yAux);

				return pointSolution;
			}

		}
		if (underscore1 != 0 && underscore2 != 0) {
			int lowerx = xyRec2Ini[0] < xyRec2End[0] ? xyRec2Ini[0] : xyRec2End[0];
			int upperx = xyRec2Ini[0] > xyRec2End[0] ? xyRec2Ini[0] : xyRec2End[0];
			int lowery = xyRec2Ini[1] < xyRec2End[1] ? xyRec2Ini[1] : xyRec2End[1];
			int uppery = xyRec2Ini[1] > xyRec2End[1] ? xyRec2Ini[1] : xyRec2End[1];
			double xAux = (b2 - b1) / (gradient1 - gradient2);
			double yAux = gradient1 * (xAux) + b1;

			if ((lowerx <= xAux && xAux <= upperx && lowery <= yAux && yAux <= uppery) || !belong) {
				// verify if the point belong to the straight because
				// it wouldn't be include in both
				Matricial2035MapHelper.round(xAux);
				Matricial2035MapHelper.round(yAux);
				pointSolution = Matricial2035MapHelper.formKey((int) xAux, (int) yAux);

				return pointSolution;
			}

		}

		return pointSolution; // if dosnt fint, or dont exist, return -1
	}
}