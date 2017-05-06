package models.configuration;

public class ConfigurationEntry {
	private LotConfigurationEnum lotConfiguration;
	private BlockConfigurationEnum blockConfiguration;

	public ConfigurationEntry(LotConfigurationEnum lotConfiguration, BlockConfigurationEnum blockConfiguration) {
		this.lotConfiguration = lotConfiguration;
		this.blockConfiguration = blockConfiguration;
	}

	public LotConfigurationEnum getLotConfiguration() {
		return lotConfiguration;
	}

	public void setLotConfiguration(LotConfigurationEnum lotConfiguration) {
		this.lotConfiguration = lotConfiguration;
	}

	public BlockConfigurationEnum getBlockConfiguration() {
		return blockConfiguration;
	}

	public void setBlockConfiguration(BlockConfigurationEnum blockConfiguration) {
		this.blockConfiguration = blockConfiguration;
	}
}