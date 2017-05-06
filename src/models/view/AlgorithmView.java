package models.view;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AlgorithmView {
	private int xSize;
	private int ySize;
	private List<Double> vertexgeocoords;
	private List<Double> entrygeocoords;
	private List<Double> origincoords;
	private List<Double> coordLimits;// missing getter and setter
	private int minBlockSize;
	private int maxBlockSize;
	private int minLotSize;
	private int maxLotSize;
	private int userId;
	private int intDate;
	private String fcmToken;

	protected AlgorithmView() {
	}

	public AlgorithmView(String fileName) {
		super();
		JSONParser parser = new JSONParser();

		try {
			Object obj = parser.parse(new FileReader(fileName));
			JSONObject jsonObject = (JSONObject) obj;
			this.xSize = ((Long) jsonObject.get("xSize")).intValue();
			this.ySize = ((Long) jsonObject.get("ySize")).intValue();
			this.minBlockSize = ((Long) jsonObject.get("minBlockSize")).intValue();
			this.maxBlockSize = ((Long) jsonObject.get("maxBlockSize")).intValue();
			this.minLotSize = ((Long) jsonObject.get("minLotSize")).intValue();
			this.maxLotSize = ((Long) jsonObject.get("maxLotSize")).intValue();

			this.vertexgeocoords = new ArrayList<>();
			JSONArray vertexgeocoordsList = (JSONArray) jsonObject.get("vertexgeocoords");
			Iterator<Double> iterator = vertexgeocoordsList.iterator();
			while (iterator.hasNext()) {
				vertexgeocoords.add(iterator.next());
			}

			this.entrygeocoords = new ArrayList<>();
			JSONArray entrygeocoordsList = (JSONArray) jsonObject.get("entrygeocoords");
			iterator = entrygeocoordsList.iterator();
			while (iterator.hasNext()) {
				entrygeocoords.add(iterator.next());
			}

			this.origincoords = new ArrayList<>();
			JSONArray origincoordsList = (JSONArray) jsonObject.get("origincoords");
			iterator = origincoordsList.iterator();
			while (iterator.hasNext()) {
				origincoords.add(iterator.next());
			}

			this.coordLimits = new ArrayList<>();
			JSONArray coordLimitsList = (JSONArray) jsonObject.get("coordLimits");
			iterator = coordLimitsList.iterator();
			while (iterator.hasNext()) {
				coordLimits.add(iterator.next());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AlgorithmView(int xSize, int ySize, List<Double> vertexgeocoords, List<Double> entrygeocoords,
			List<Double> origincoords, List<Double> coordLimits, int minBlockSize, int maxBlockSize, int minLotSize,
			int maxLotSize, int userId, int intDate, String fcmToken) {
		super();
		this.xSize = xSize;
		this.ySize = ySize;
		this.vertexgeocoords = vertexgeocoords;
		this.entrygeocoords = entrygeocoords;
		this.origincoords = origincoords;
		this.coordLimits = coordLimits;
		this.minBlockSize = minBlockSize;
		this.maxBlockSize = maxBlockSize;
		this.minLotSize = minLotSize;
		this.maxLotSize = maxLotSize;
		this.userId = userId;
		this.intDate = intDate;
		this.fcmToken = fcmToken;
	}

	public int getxSize() {
		return xSize;
	}

	public void setxSize(int xSize) {
		this.xSize = xSize;
	}

	public int getySize() {
		return ySize;
	}

	public void setySize(int ySize) {
		this.ySize = ySize;
	}

	public List<Double> getEntrygeocoords() {
		return entrygeocoords;
	}

	public List<Integer> getCartEntrygeocoords() {
		List<Integer> cartesianCoords = new ArrayList<>();
		// 0 -> Latitud //1 -> Longitude
		for (int i = 0; i < entrygeocoords.size(); i++) {
			if (i % 2 == 0) {
				cartesianCoords.add(i, round(xSize * (entrygeocoords.get(i) - coordLimits.get(0))
						/ (coordLimits.get(2) - coordLimits.get(0))));
			} else {
				cartesianCoords.add(i, round(ySize * (entrygeocoords.get(i) - coordLimits.get(1))
						/ (coordLimits.get(3) - coordLimits.get(1))));
			}
		}
		return cartesianCoords;
	}

	public void setEntrygeocoords(List<Double> entrygeocoords) {
		this.entrygeocoords = entrygeocoords;
	}

	public int getMinBlockSize() {
		return minBlockSize;
	}

	public void setMinBlockSize(int minBlockSize) {
		this.minBlockSize = minBlockSize;
	}

	public int getMaxBlockSize() {
		return maxBlockSize;
	}

	public void setMaxBlockSize(int maxBlockSize) {
		this.maxBlockSize = maxBlockSize;
	}

	public int getMinLotSize() {
		return minLotSize;
	}

	public void setMinLotSize(int minLotSize) {
		this.minLotSize = minLotSize;
	}

	public int getMaxLotSize() {
		return maxLotSize;
	}

	public List<Double> getOrigincoords() {
		return origincoords;
	}

	public void setOrigincoords(List<Double> origincoords) {
		this.origincoords = origincoords;
	}

	public void setMaxLotSize(int maxLotSize) {
		this.maxLotSize = maxLotSize;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getIntDate() {
		return intDate;
	}

	public void setIntDate(int intDate) {
		this.intDate = intDate;
	}

	public String getFcmToken() {
		return fcmToken;
	}

	public void setFcmToken(String fcmToken) {
		this.fcmToken = fcmToken;
	}

	public List<Double> getVertexgeocoords() {
		return vertexgeocoords;
	}

	public List<Double> getCoordLimits() {
		return coordLimits;
	}

	public void setCoordLimits(List<Double> coordLimits) {
		this.coordLimits = coordLimits;
	}

	public List<Integer> getCartVertexgeocoords() {
		List<Integer> coordinates = new ArrayList<>(vertexgeocoords.size());
		// 0 -> Latitud //1 -> Longitude
		for (int i = 0; i < vertexgeocoords.size(); i++) {
			if (i % 2 == 0) {
				coordinates.add(i, round(xSize * (vertexgeocoords.get(i) - coordLimits.get(0))
						/ (coordLimits.get(2) - coordLimits.get(0))));
			} else {
				coordinates.add(i, round(ySize * (vertexgeocoords.get(i) - coordLimits.get(1))
						/ (coordLimits.get(3) - coordLimits.get(1))));
			}
		}
		return coordinates;
	}

	private static int round(double d) {
		double dAbs = Math.abs(d);
		int i = (int) dAbs;
		double result = dAbs - (double) i;
		if (result < 0.5) {
			return d < 0 ? -i : i;
		} else {
			return d < 0 ? -(i + 1) : i + 1;
		}
	}

	public void setVertexgeocoords(List<Double> vertexgeocoords) {
		this.vertexgeocoords = vertexgeocoords;
	}
}