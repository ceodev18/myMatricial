package models.matricialVariation;
import interfaces.matricialVariation.MatricialConstants;

public class MatricialLandRoute {
	private int initialPointId = -1;
	private int finalPointId = -1;
	private int direction = MatricialConstants.ORTHOGONAL;
	private String type = "";
	//ORTHOGONAL
	public MatricialLandRoute(){}
	public MatricialLandRoute(int initialPointId, int direction, String type){
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
