package models.radialVariation;

import interfaces.radialVariation.RadialConstants;


public class RadialLandRoute {
	private int initialPointId = -1;
	private int finalPointId = -1;
	private int direction = RadialConstants.ORTHOGONAL;
	
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
}
