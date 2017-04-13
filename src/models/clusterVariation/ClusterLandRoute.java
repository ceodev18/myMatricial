package models.clusterVariation;

public class ClusterLandRoute {
	private int initialPointId = -1;
	private int direction = -1;
	private String type = "";

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

	@Override
	public String toString() {
		return type + " " + initialPointId;
	}
}