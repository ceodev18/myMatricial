package models.matricial2035Variation;

import helpers.matricial2035Variation.Matricial2035MapHelper;

public class Matricial2035RotationPoint {
	private int rotationPointId;
	private int nodeIndex;
	private double angle;

	public int getRotationPointId() {
		return rotationPointId;
	}

	public void setRotationPointId(int rotationPointId) {
		this.rotationPointId = rotationPointId;
	}

	public int getNodeIndex() {
		return nodeIndex;
	}

	public void setNodeIndex(int nodeIndex) {
		this.nodeIndex = nodeIndex;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public Matricial2035LandPoint rotatePoint(Matricial2035LandPoint rotationPoint) {
		int[] axisXY = Matricial2035MapHelper.breakKey(rotationPointId);
		int[] toRotateXY = Matricial2035MapHelper.breakKey(rotationPoint.getId());

		int newX = (int) (axisXY[0] + (toRotateXY[0] - axisXY[0]) * Math.cos(Math.toRadians(angle))
				- (toRotateXY[1] - axisXY[1]) * Math.sin(Math.toRadians(angle)));
		int newY = (int) (axisXY[1] + (toRotateXY[0] - axisXY[0]) * Math.sin(Math.toRadians(angle))
				+ (toRotateXY[1] - axisXY[1]) * Math.cos(Math.toRadians(angle)));
		return new Matricial2035LandPoint(newX, newY);
	}
}