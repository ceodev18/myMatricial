package models.RadialVariation;

import interfaces.Constants;

public class RadialLandRoute {
	private int type = Constants.ARTERIAL_BRANCH;
	private int initialPointId = -1;
	private int finalPointId = -1;
	private int direction = Constants.ORTHOGONAL;
	private int extension = 0;
	
	public int getType() {
		return type;
	}
	public void setType(int branchType) {
		this.type = branchType;
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
	public int getExtension() {
		return extension;
	}
	public void setExtension(int extension) {
		this.extension = extension;
	}
	public int getFinalPointId() {
		return finalPointId;
	}
	public void setFinalPointId(int finalPointId) {
		this.finalPointId = finalPointId;
	}
}