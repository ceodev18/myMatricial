package models.base;

public class LandLot {
	private String id = "Z";
	private int pointSize;
	
	public LandLot(int lateralSize, String id){
		//120 = 2
		this.setPointSize(lateralSize / 120);
		this.setId(id);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getPointSize() {
		return pointSize;
	}

	public void setPointSize(int pointSize) {
		this.pointSize = pointSize;
	}
	
	public void updateEnum(){
		switch(id){
		case "A":
			this.id = "B";
			break;
		case "B":
			this.id = "C";
			break;
		case "C":
			this.id = "D";
			break;
		case "D":
			this.id = "A";
			break;
		}
	}
}
