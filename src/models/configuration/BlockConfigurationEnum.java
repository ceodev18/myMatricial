package models.configuration;

public enum BlockConfigurationEnum {
	S_BLOCK(132, 2), XS_BLOCK(144, 1.8), SM_BLOCK(156, 1.6), M_BLOCK(168, 1.4), L_BLOCK(180, 1.2), XL_BLOCK(196, 1);

	private int sideSize;
	private double mathReason;

	BlockConfigurationEnum(int sideSize, double mathReason) {
		this.sideSize = sideSize;
		this.mathReason = mathReason;
	}

	public int getSideSize() {
		return sideSize;
	}

	public double getDepthSize() {
		return mathReason;
	}
}