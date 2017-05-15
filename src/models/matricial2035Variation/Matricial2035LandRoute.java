package models.matricial2035Variation;

public class Matricial2035LandRoute {
	private int initialPointId = -1;
	private int direction = -1;
	private String type = "";
	
	public Matricial2035LandRoute(int initialPointId, int direction, String type){
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