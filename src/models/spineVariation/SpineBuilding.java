package models.spineVariation;

import java.util.ArrayList;
import java.util.List;

public class SpineBuilding {
	private List<Integer> points = new ArrayList<>();
	private String type; //d is street route, numeric in case of house
	private int number; //if -1 street

	public List<Integer> getPoints() {
		return points;
	}
	public void setPoints(List<Integer> points) {
		this.points = points;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}

}
