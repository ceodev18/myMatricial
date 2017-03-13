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

	public List<Integer> shrink(int size) {
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

	public List<List<Integer>> shrinkZone(int initialShrink, int size) {
		List<List<Integer>> areas = new ArrayList<>();
		List<Integer> area =shrink(initialShrink);
		areas.add(area);
		int xy[] = MapHelper.breakKey(area.get(0));
		int distance = (int) Math.sqrt(Math.pow(xy[0] - centroid[0], 2) + Math.pow(xy[1] - centroid[1], 2));

		if (distance > initialShrink + size) {
			for (int i = initialShrink + 1; i < initialShrink + size; i++){
				area = shrink(i);
				areas.add(area);
			}
		} else {
			areas = new ArrayList<>();
		}

		return areas;
	}

	public List<List<Integer>> parkZone(int initialDepth) {
		List<List<Integer>> areas = new ArrayList<>();
		List<Integer> area =shrink(initialDepth);
		int minorDistance = 9999;
		for (int i = 0; i < area.size(); i++) {
			int xy[] = MapHelper.breakKey(area.get(i));
			int distance = (int) Math.sqrt(Math.pow(xy[0] - centroid[0], 2) + Math.pow(xy[1] - centroid[1], 2));
			if (distance < minorDistance) {
				minorDistance = distance;
			}
		}

		if (minorDistance > 0) {
			for (int i = initialDepth + 1; i < initialDepth + minorDistance; i++) {
				area = shrink(i);
				areas.add(area);
			}
		} else {
			areas = new ArrayList<>();
		}
		return areas;
	}
}