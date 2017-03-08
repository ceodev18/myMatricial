package models.clusterVariation;

import java.util.ArrayList;
import java.util.List;

import helpers.base.MapHelper;

public class ClusterPolygon {
	private List<Integer> vertices;
	private int type;

	public ClusterPolygon(int[] polygonVertex, int type) {
		vertices = new ArrayList<>();
		for (int i = 0; i < type; i++) {
			vertices.add(polygonVertex[i]);
		}

		this.setType(type);
		enshrink();
	}

	private void enshrink() {
		int[] centroid = findCentroid();
		for (int i = 0; i < type; i++) {
			int[] xy = MapHelper.breakKey(vertices.get(i));

			if (centroid[0] - xy[0] > 0)
				xy[0]++;
			else
				xy[0]--;

			if (centroid[1] - xy[1] > 0)
				xy[1]++;
			else
				xy[1]--;

			vertices.set(i, MapHelper.formKey(xy[0], xy[1]));
		}
	}

	private int[] findCentroid() {
		int xLimits[] = new int[2];
		xLimits[1] = -1000;
		xLimits[0] = -1000;

		int yLimits[] = new int[2];
		yLimits[1] = -1000;
		yLimits[0] = -1000;

		for (int i = 0; i < type; i++) {
			int[] xy = MapHelper.breakKey(vertices.get(i));
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
		return new int[] { ((xLimits[0] + xLimits[1]) / 2), ((yLimits[0] + yLimits[1]) / 2) };
	}

	public boolean same(ClusterPolygon friend) {
		if (friend.getType() != type) {
			return false;
		}
		int equality = 0;
		for (int i = 0; i < type; i++) {
			for (int j = 0; j < type; j++) {
				if (vertices.get(i).intValue() == friend.vertices.get(j).intValue()) {
					equality++;
					break;
				}
			}
		}
		return equality == type;
	}

	public List<Integer> getVertices() {
		return vertices;
	}

	public void setVertices(List<Integer> vertices) {
		this.vertices = vertices;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void print() {
		String vertexChain = "";
		for (Integer in : getVertices()) {
			vertexChain += in + ", ";
		}
		System.out.println(vertexChain);
	}
}
