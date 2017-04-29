package models.spineVariation;

import interfaces.spineVariation.SpineConstants;

public class SpineLandRoute {
	private int initialPointId = -1;
	private int finalPointId = -1;
	private int direction = SpineConstants.ORTHOGONAL;
	private String type = "";
	public SpineLandRoute(){}
	public SpineLandRoute(int initialPointId, int direction, String type){
		this.initialPointId = initialPointId;
		this.direction = direction; 
		this.type = type;
	}
	
	public int getInitialPointId() {
		return initialPointId;
	}
	public void setInitialPointId(int initialPointId) {
		this.initialPointId = initialPointId;
	}
	public int getDirection() {
		return direction;
	}
	public void setDirection(int direction) {
		this.direction = direction;
	}
	public int getFinalPointId() {
		return finalPointId;
	}
	public void setFinalPointId(int finalPointId) {
		this.finalPointId = finalPointId;
	}
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String stringify() {
		return direction + "" + type + "" + initialPointId;
	}

}
