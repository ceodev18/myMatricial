package models.configuration;

import java.util.ArrayList;
import java.util.List;

import models.view.AlgorithmView;

public class ConfigurationMatrix {
	private List<ConfigurationEntry> configuration;

	int maxSize = LotConfigurationEnum.LARGE_DEPTH_SIZE.getSideSize();
	int minSize = LotConfigurationEnum.SMALL_WHOLE_SIZE.getSideSize();

	public ConfigurationMatrix() {

	}

	public ConfigurationMatrix(AlgorithmView algorithmView) {
		setConfiguration(gradualConfiguration(algorithmView));
	}

	private List<ConfigurationEntry> gradualConfiguration(AlgorithmView algorithmView) {
		// delimit possible combinations
		List<LotConfigurationEnum> lotConfigurations = new ArrayList<>();
		int minimunLotSize = algorithmView.getMinLotSize();
		int maximunLotSize = algorithmView.getMaxLotSize();

		if (minimunLotSize == LotConfigurationEnum.SMALL_WHOLE_SIZE.getSideSize())
			lotConfigurations.add(LotConfigurationEnum.SMALL_WHOLE_SIZE);

		if (minimunLotSize <= LotConfigurationEnum.MEDIUM_SIDE_SIZE.getSideSize()
				&& maximunLotSize >= LotConfigurationEnum.MEDIUM_SIDE_SIZE.getSideSize())
			lotConfigurations.add(LotConfigurationEnum.MEDIUM_SIDE_SIZE);

		if (minimunLotSize <= LotConfigurationEnum.MEDIUM_DEPTH_SIZE.getSideSize()
				&& maximunLotSize >= LotConfigurationEnum.MEDIUM_DEPTH_SIZE.getSideSize())
			lotConfigurations.add(LotConfigurationEnum.MEDIUM_DEPTH_SIZE);

		if (minimunLotSize <= LotConfigurationEnum.LARGE_SIDE_SIZE.getSideSize()
				&& maximunLotSize >= LotConfigurationEnum.LARGE_SIDE_SIZE.getSideSize())
			lotConfigurations.add(LotConfigurationEnum.LARGE_SIDE_SIZE);

		if (minimunLotSize <= LotConfigurationEnum.LARGE_DEPTH_SIZE.getSideSize()
				&& maximunLotSize >= LotConfigurationEnum.LARGE_DEPTH_SIZE.getSideSize())
			lotConfigurations.add(LotConfigurationEnum.LARGE_DEPTH_SIZE);

		List<BlockConfigurationEnum> blockConfigurations = new ArrayList<>();
		int minimunBlockSize = algorithmView.getMinBlockSize();
		int maximunBlockSize = algorithmView.getMaxBlockSize();

		if (minimunBlockSize == BlockConfigurationEnum.XS_BLOCK.getSideSize())
			blockConfigurations.add(BlockConfigurationEnum.XS_BLOCK);

		if (minimunBlockSize <= BlockConfigurationEnum.S_BLOCK.getSideSize()
				&& maximunBlockSize >= BlockConfigurationEnum.S_BLOCK.getSideSize())
			blockConfigurations.add(BlockConfigurationEnum.S_BLOCK);

		if (minimunBlockSize <= BlockConfigurationEnum.SM_BLOCK.getSideSize()
				&& maximunBlockSize >= BlockConfigurationEnum.SM_BLOCK.getSideSize())
			blockConfigurations.add(BlockConfigurationEnum.SM_BLOCK);

		if (minimunBlockSize <= BlockConfigurationEnum.M_BLOCK.getSideSize()
				&& maximunBlockSize >= BlockConfigurationEnum.M_BLOCK.getSideSize())
			blockConfigurations.add(BlockConfigurationEnum.M_BLOCK);

		if (minimunBlockSize <= BlockConfigurationEnum.L_BLOCK.getSideSize()
				&& maximunBlockSize >= BlockConfigurationEnum.L_BLOCK.getSideSize())
			blockConfigurations.add(BlockConfigurationEnum.L_BLOCK);

		if (minimunBlockSize <= BlockConfigurationEnum.XL_BLOCK.getSideSize()
				&& maximunBlockSize >= BlockConfigurationEnum.XL_BLOCK.getSideSize())
			blockConfigurations.add(BlockConfigurationEnum.XL_BLOCK);
		// Now we have all possible variants accepted
		// As the most important is to have a correct variance, we should always
		// aim to create the lowest ones
		List<ConfigurationEntry> configurations = new ArrayList<>();
		for (int j = 0; j < lotConfigurations.size(); j++) {
			for (int i = 0; i < blockConfigurations.size(); i++) {
				ConfigurationEntry configurationEntry = new ConfigurationEntry(lotConfigurations.get(j),
						blockConfigurations.get(i));
				configurations.add(configurationEntry);
				if (configurations.size() > 2) {
					break;
				}
			}
			if (configurations.size() > 2) {
				break;
			}
		}

		if (configurations.size() == 0) {
			return null;
		} else {
			return configurations;
		}
	}

	public List<ConfigurationEntry> getConfiguration() {
		return configuration;
	}

	public void setConfiguration(List<ConfigurationEntry> configuration) {
		this.configuration = configuration;
	}
}