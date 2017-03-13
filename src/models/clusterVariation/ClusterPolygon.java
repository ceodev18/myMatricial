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
		if(complete){
			findCentroid();
		}
	}

	public boolean isComplete() {
		return complete;
	}

	public void shrink() {
		for (int i = 0; i < points.size(); i++) {
			int[] xy = MapHelper.breakKey(points.get(i));

			if (centroid[0] - xy[0] > 0)
				xy[0]++;
			else
				xy[0]--;

			if (centroid[1] - xy[1] > 0)
				xy[1]++;
			else
				xy[1]--;

			points.set(i, MapHelper.formKey(xy[0], xy[1]));
		}
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
			for(int i = 0; i<points.size();i++){
				System.out.print(i+"="+points.get(i)+", ");
			}
		} else {			
			System.out.println("Polygon is not complete");
			for(int i = 0; i<points.size();i++){
				System.out.print(i+"="+points.get(i)+", ");
			}
		}
		System.out.println();
	}
}