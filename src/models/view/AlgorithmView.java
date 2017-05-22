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
	private List<Integer> vertexcartcoords;
	private List<Integer> entrycartcoords;
	
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
			Iterator<Double> doubleIterator = vertexgeocoordsList.iterator();
			while (doubleIterator.hasNext()) {
				vertexgeocoords.add(doubleIterator.next());
			}

			this.entrygeocoords = new ArrayList<>();
			JSONArray entrygeocoordsList = (JSONArray) jsonObject.get("entrygeocoords");
			doubleIterator = entrygeocoordsList.iterator();
			while (doubleIterator.hasNext()) {
				entrygeocoords.add(doubleIterator.next());
			}

			this.origincoords = new ArrayList<>();
			JSONArray origincoordsList = (JSONArray) jsonObject.get("origincoords");
			doubleIterator = origincoordsList.iterator();
			while (doubleIterator.hasNext()) {
				origincoords.add(doubleIterator.next());
			}

			this.coordLimits = new ArrayList<>();
			JSONArray coordLimitsList = (JSONArray) jsonObject.get("coordLimits");
			doubleIterator = coordLimitsList.iterator();
			while (doubleIterator.hasNext()) {
				coordLimits.add(doubleIterator.next());
			}
			
			this.setVertexcartcoords(new ArrayList<>());
			JSONArray vertexCartList = (JSONArray) jsonObject.get("vertexcartcoords");
			Iterator<Long> integerIterator = vertexCartList.iterator();
			while (integerIterator.hasNext()) {
				getVertexcartcoords().add(integerIterator.next().intValue());
			}
			
			this.setEntrycartcoords(new ArrayList<>());
			JSONArray entryCartList = (JSONArray) jsonObject.get("entrycartcoords");
			integerIterator = entryCartList.iterator();
			while (integerIterator.hasNext()) {
				getEntrycartcoords().add(integerIterator.next().intValue());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AlgorithmView(int xSize, int ySize, List<Double> vertexgeocoords, List<Double> entrygeocoords,
			List<Double> origincoords, List<Double> coordLimits, int minBlockSize, int maxBlockSize, int minLotSize,
			int maxLotSize, int userId, int intDate, String fcmToken, List<Integer> vertexcartcoords, List<Integer> entrycartcoords) {
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
		this.setVertexcartcoords(vertexcartcoords);
		this.setEntrycartcoords(entrycartcoords);
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

	// a b (1/f) WGS84
	// 6 378 137.0 | 6 356 752.314 245 | 298.257 223 563
	// latitude, longitude and height (h)
	// X = (N(latitude) + h)*cos(latitude) * cos(longitude)
	// Y = (N(latitude) + h)*cos(latitude) * sin(longitude)
	// Z = ((b2/a2) * N(latitude) + h)* sin(latitude)
	// N(latitude) = a2 / sqrt(a2 * cos2(latitude) + b2 * sin2(latitude))
	public List<Integer> getCartVertexgeocoordsWGS84() {
		List<Integer> coordinates = new ArrayList<>(vertexgeocoords.size());
		// 0 -> Latitud //1 -> Longitude
		for (int i = 0; i < vertexgeocoords.size(); i += 2) {
			double latitude = Math.toRadians(vertexgeocoords.get(i));
			double longitude = Math.toRadians(vertexgeocoords.get(i+1));
			
			double N = Math.pow(6378137.0, 2) / (Math.sqrt(Math.pow(6378137.0, 2) * Math.pow(Math.cos(latitude),2) + Math.pow(6356752.314245, 2) * Math.pow(Math.sin(latitude),2)));
			int X = round((N + 0) * Math.cos(latitude) * Math.cos(longitude));
			int Y = round((N + 0) * Math.cos(latitude) * Math.sin(longitude));
			coordinates.add(X);
			coordinates.add(Y);
		}

		// TEST INVERSE TRANSFORMATION
		coordinates = translatePoints(coordinates);
		readjustLimits(coordinates);
		return coordinates;
	}

	private void readjustLimits(List<Integer> coordinates) {
		for (int i = 0; i < coordinates.size(); i += 2) {
			if(coordinates.get(i)>xSize){
				xSize =  coordinates.get(i);
			}
			
			if(coordinates.get(i+1)>ySize){
				ySize = coordinates.get(i+1);
			}
		}
	}

	private List<Integer> translatePoints(List<Integer> coordinates) {
		// TODO will only work on southern hemisphere
		List<Integer> translatedCoordinates = new ArrayList<>(vertexgeocoords.size());
		int minLatitude = 9999999, minLongitude = 9999999;
		// 0 -> Latitud //1 -> Longitude
		for (int i = 0; i < vertexgeocoords.size(); i += 2) {
			if (minLatitude > coordinates.get(i)) {
				minLatitude = coordinates.get(i);
			}

			if (minLongitude > coordinates.get(i + 1)) {
				minLongitude = coordinates.get(i + 1);
			}
		}
		
		//translate and see if it is correct
		for (int i = 0; i < vertexgeocoords.size(); i += 2) {
			translatedCoordinates.add(coordinates.get(i)-minLatitude);
			translatedCoordinates.add(coordinates.get(i+1)-minLongitude);
		}
		
		return translatedCoordinates;
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

	public List<Integer> getVertexcartcoords() {
		return vertexcartcoords;
	}

	public void setVertexcartcoords(List<Integer> vertexcartcoords) {
		this.vertexcartcoords = vertexcartcoords;
	}

	public List<Integer> getEntrycartcoords() {
		return entrycartcoords;
	}

	public void setEntrycartcoords(List<Integer> entrycartcoords) {
		this.entrycartcoords = entrycartcoords;
	}
}