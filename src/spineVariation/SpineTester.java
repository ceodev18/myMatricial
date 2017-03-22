package spineVariation;

import java.util.ArrayList;
import java.util.List;

import models.clusterVariation.ClusterLandMap;
import models.clusterVariation.ClusterLandPoint;

public class SpineTester {
	public static void main(String[] argv){
		int large=900;
		int width=900;
		ClusterLandMap landMap = new ClusterLandMap(large, width);
		
		List<ClusterLandPoint> polygon = new ArrayList<>();
		ClusterLandPoint landPoint = new ClusterLandPoint(0, 700);
		polygon.add(landPoint);
		landPoint = new ClusterLandPoint(300, 900);
		polygon.add(landPoint);
		landPoint = new ClusterLandPoint(800, 900);
		polygon.add(landPoint);
		landPoint = new ClusterLandPoint(900, 100);
		polygon.add(landPoint);
		// we must reuse the first one as the last
		landPoint = new ClusterLandPoint(100, 0);
		polygon.add(landPoint);
		landMap.createBorderFromPolygon(polygon);
		landMap.printMapToFile();
	
		
	}

}
