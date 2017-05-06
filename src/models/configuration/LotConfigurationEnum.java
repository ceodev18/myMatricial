package models.configuration;

public enum LotConfigurationEnum {
	SMALL_WHOLE_SIZE(6, 15), 
	MEDIUM_DEPTH_SIZE(8, 12), 
	MEDIUM_SIDE_SIZE(12, 8), 
	LARGE_SIDE_SIZE(10, 16), 
	LARGE_DEPTH_SIZE(8, 20);

	private int sideSize;
	private int depthSize;

	LotConfigurationEnum(int sideSize, int depthSize) {
		this.sideSize = sideSize;
		this.depthSize = depthSize;
	}

	public int getSideSize() {
		return sideSize;
	}

	public int getDepthSize() {
		return depthSize;
	}
}