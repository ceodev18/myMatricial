package models;

import java.util.ArrayList;
import java.util.List;

public class LandRoute {
	private List<Integer> points = new ArrayList<>();//the ids of the nodes that conform the route
	private int type;
	public List<Integer> getPoints() {
		return points;
	}
	public void setPoints(List<Integer> points) {
		this.points = points;
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	

}