package algorithm.matricial2035Variation;

import models.matricial2035Variation.Matricial2035LandMap;
import models.matricial2035Variation.Matricial2035LandPoint;

public class Matricial2035Algorithm {
	private Matricial2035LandMap landMap;
	private double directionAngle; //the original direction angle before turning
	

	public void generateMatrix(Matricial2035LandPoint entryPoint) {

		
	}
	
	public void zonify() {
		// TODO Auto-generated method stub
		
	}
	
	public void setLandMap(Matricial2035LandMap landMap) {
		this.landMap = landMap;	
	}
	
	public Matricial2035LandMap getLandMap() {
		return landMap;
	}

	public double getDirectionAngle() {
		return directionAngle;
	}

	public void setDirectionAngle(double directionAngle) {
		this.directionAngle = directionAngle;
	}
}