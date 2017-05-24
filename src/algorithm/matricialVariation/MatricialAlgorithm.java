package algorithm.matricialVariation;



import helpers.matricialVariation.MatricialMapHelper;
import helpers.spineVariation.SpineDirectionHelper;
import helpers.spineVariation.SpineMapHelper;
import interfaces.matricialVariation.MatricialConfiguration;
import interfaces.spineVariation.SpineConfiguration;
import interfaces.spineVariation.SpineConstants;
import models.matricialVariation.MatricialLandMap;
import models.matricialVariation.MatricialLandPoint;
import models.matricialVariation.MatricialLandRoute;
import models.matricialVariation.MatricialPolygon;
import java.util.ArrayList;
import java.util.List;

public class MatricialAlgorithm {
	public double constantGradient;
	public double gradient; 
	public MatricialLandMap landMap;
	public static int indice;
	public static int width;
	public static int large;
	public static int pointStart;
	public static int nmbrParksSpine;
	public static int nmbrParksLong;
	public MatricialLandPoint entrypoint;
	public List<Integer> axisLongSide;
	public  int factorAporte;
	
	public MatricialLandMap getLandMap() {
		return landMap;
	}
	
	public void setLandMap(MatricialLandMap landMap) {
		this.landMap = landMap;
	}
	public void setLarge(int large) {
		this.large = large;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public void cleanArterialBranch(){
		
	}
	public MatricialLandPoint getEntryPoint(){
		return entrypoint;
	}
	public void setEntryPoint(MatricialLandPoint  point){
		entrypoint = point;
	}
	
	public  void createRouteVariation(int axisPoint, int direction, int branchType) {
		int extension = 0;
		String markType = "";
		int growDirection = -1;
		MatricialLandRoute matricialLandRoute = null;
		int trueAxis = axisPoint;
		while (landMap.landPointisOnMap(trueAxis) && landMap.getLandPoint(trueAxis).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)) {
			trueAxis = SpineMapHelper.moveKeyByOffsetAndDirection(trueAxis, 1, direction);
		}
		switch (branchType) {
		case SpineConfiguration.ARTERIAL_BRANCH:
			extension = SpineConfiguration.ARTERIAL_BRANCH_SIZE;
			markType = SpineConfiguration.ARTERIAL_MARK;
			matricialLandRoute = new MatricialLandRoute(trueAxis, direction, "a");
			break;
		case SpineConfiguration.COLLECTOR_BRANCH:
			extension = SpineConfiguration.COLLECTOR_BRANCH_SIZE;
			markType = SpineConfiguration.COLLECTOR_MARK;
			matricialLandRoute = new MatricialLandRoute(trueAxis, direction, "b");
			break;
		case SpineConfiguration.LOCAL_BRANCH:
			extension = SpineConfiguration.LOCAL_BRANCH_SIZE;
			markType = SpineConfiguration.LOCAL_MARK;
			matricialLandRoute = new MatricialLandRoute(trueAxis, direction, "c");
			break;
		case SpineConfiguration.WALK_BRANCH:
			extension = SpineConfiguration.WALK_BRANCH_SIZE;
			markType = SpineConfiguration.WALK_MARK;
			break;
		}

		switch (direction) {
		case SpineConstants.EAST:
			growDirection = SpineConstants.NORTH;
			break;
		case SpineConstants.NORTH:
			growDirection = SpineConstants.WEST;
			break;
		case SpineConstants.WEST:
			growDirection = SpineConstants.SOUTH;
			break;
		case SpineConstants.SOUTH:
			growDirection = SpineConstants.EAST;
			break;
		}
		
		createLine(
				SpineMapHelper.moveKeyByOffsetAndDirection(axisPoint, 1,
						SpineDirectionHelper.oppositeDirection(growDirection)),
				direction, SpineConfiguration.NODE_MARK);
		createLine(SpineMapHelper.moveKeyByOffsetAndDirection(axisPoint, extension, growDirection), direction,
				SpineConfiguration.NODE_MARK);

		for (int i = 0; i < extension; i++) {
			if ((branchType == SpineConfiguration.ARTERIAL_BRANCH) && (i == 0)) {
				int finalPointid = createLine(SpineMapHelper.moveKeyByOffsetAndDirection(axisPoint, i, growDirection),
						direction, markType);
				matricialLandRoute.setFinalPointId(finalPointid);
				//System.out.println("landMap.setLandRoute(clusterLandRoute)");
				landMap.getLandRoutes().add(matricialLandRoute);
				System.out.println("matricialLandRoute.stringify() in creaRouteVartiation");
				System.out.println(matricialLandRoute.stringify());
				//landMap.setLandRoute(clusterLandRoute);
			} else {
				createLine(MatricialMapHelper.moveKeyByOffsetAndDirection(axisPoint, i, growDirection), direction,
						markType);
			}
		}
	}
	private  int createLine(int givenXY, int direction, String markType) {
		int[] xy = SpineMapHelper.breakKey(givenXY);
		int[] newXY = new int[2];
		Boolean in = false, out = false, changed = false;
		if ((direction == SpineConstants.NORTH) || (direction == SpineConstants.SOUTH)) {
			if (xy[0] > landMap.getPointsx())
				return -1;
			for (int i = 0; i < landMap.getPointsy(); i++) {
				newXY[0] = xy[0];
				newXY[1] = i;
				changed = markPoint(newXY, markType, in, out);
				if (changed && !in) {
					in = true;
					changed = false;
				}
				if (changed && in) {
					out = true;
				}
				if (out.booleanValue()) {
					newXY[0] = xy[0];
					newXY[1] = i - 1;
					return SpineMapHelper.formKey(newXY[0], newXY[1]);
				}
			}
		} else {
			if (xy[1] > landMap.getPointsy())
				return -1;
			for (int i = 0; i < landMap.getPointsx(); i++) {
				newXY[0] = i;
				newXY[1] = xy[1];
				changed = markPoint(newXY, markType, in, out);
				if (changed && !in) {
					in = true;
					changed = false;
				}
				if (changed && in) {
					out = true;
				}
				if (out.booleanValue()) {
					newXY[0] = i - 1;
					newXY[1] = xy[1];
					return SpineMapHelper.formKey(newXY[0], newXY[1]);
				}
			}
		}
		return -1;
	}
	private  boolean markPoint(int[] newXY, String markType, Boolean in, Boolean out) {
		boolean changed = false;
		if(!verificablePoint(newXY[0],newXY[1]))return false;
		MatricialLandPoint matricialLandPoint = landMap.findPoint(MatricialMapHelper.formKey(newXY[0], newXY[1]));
	
		if (!in.booleanValue() && !matricialLandPoint.isMapLimit()) {
			changed = true;
			in = true;
		}

		if (in.booleanValue() && matricialLandPoint.isMapLimit()) {
			changed = true;
		}

		if (in.booleanValue()) {
			if (markType.equals(SpineConfiguration.NODE_MARK)) {
				if (matricialLandPoint.getType().equals(SpineConfiguration.EMPTY_MARK)) {
					matricialLandPoint.setType(markType);
				}
			} else {
				if (matricialLandPoint.getType().equals(SpineConfiguration.EMPTY_MARK)
						|| matricialLandPoint.getType().equals(SpineConfiguration.NODE_MARK)) {
					matricialLandPoint.setType(markType);
				}
			}
		}
		return changed;
	}
	private boolean verificablePoint(int pointX,int pointY){
		if(pointX<0 ||pointX>large)return false;
		if(pointY<0 ||pointY>width)return false;
		return true;
	}
	public void matricialZonification(){
		int middleX,middleY,axisX,axisY;
		double factorX,factorY;
		this.axisLongSide=this.landMap.mostLargeSide();
		factorX=(axisLongSide.get(2)-axisLongSide.get(0))*1.0;
		factorY=(axisLongSide.get(3)-axisLongSide.get(1))*1.0;
		axisX=Math.abs((axisLongSide.get(2)-axisLongSide.get(0))/2);
		axisY=Math.abs((axisLongSide.get(3)-axisLongSide.get(1))/2);
		//gradient=factorY/factorX;
		//constantGradient = axisLongSide.get(1)-(gradient*axisLongSide.get(0));
		//int valX = Math.abs(axisLongSide.get(2)-axisLongSide.get(0))/2;
	
		secondPrincipalStreet(axisX,axisY);
		firstPrincipalStreet();
			///find widht of polygon
			//find altura polygon
			//
	}
	private int[] secondPrincipalStreet(int axisX,int axisY ){
		List<Integer> localLayer;
		localLayer = new ArrayList<>();
		for(int i=0; i<landMap.getPolygonNodes().size()-1; i++){
			localLayer.add(landMap.getPolygonNodes().get(i).getId());
		}
		MatricialPolygon polygonAux = new MatricialPolygon();
		polygonAux.setPoints(localLayer);
		polygonAux.setComplete(true);
		int pointRef = MatricialMapHelper.formKey(axisX,axisY);
		int centroid =    MatricialMapHelper.formKey( polygonAux.getCentroid()[0],polygonAux.getCentroid()[1]);
		int distance = (int)(2*(landMap.distanceOfPointToPoint(pointRef, centroid)/3));
		List<Integer> auxLayer = polygonAux.vectorShrinking(distance);
		//find correct side of auxLayer
		double distanceMin1 =10000;
		int ind=-1; 
		for(int j= 0;j < auxLayer.size();j++){
			int valXY1[] =  MatricialMapHelper.breakKey(auxLayer.get(j));
			int valXY2[] =	MatricialMapHelper.breakKey(auxLayer.get((j+1)%auxLayer.size()));
			int midX = (valXY1[0]+ valXY2[0])/2;
			int midY = (valXY1[1]+ valXY2[1])/2;
			double distAux = landMap.distanceOfPointToPoint(pointRef , MatricialMapHelper.formKey(midX, midY));
			if(distAux < distanceMin1){
				distanceMin1 = distAux;
				ind = j;
			}
		}
		//
		int pointAux2 = landMap.findProyectionPointIntoParalelStraights(auxLayer.get(ind), auxLayer.get((ind+1)%auxLayer.size()), pointRef, true);
		int[] pointsInters = landMap.createMainRoute(pointRef,pointAux2,localLayer);
		landMap.createACustomRoute
			(pointsInters[0],pointsInters[1], MatricialConfiguration.ARTERIAL_BRANCH_SIZE , MatricialConfiguration.ARTERIAL_MARK);
		return pointsInters;
	}
	
	private int[] firstPrincipalStreet(){
		List<Integer> localLayer;
		localLayer = new ArrayList<>();
		for(int i=0; i<landMap.getPolygonNodes().size()-1; i++){
			localLayer.add(landMap.getPolygonNodes().get(i).getId());
		}
		MatricialPolygon polygonAux = new MatricialPolygon();
		polygonAux.setPoints(localLayer);
		polygonAux.setComplete(true);
		
		
		
		int referencePoint =  MatricialMapHelper.formKey( getEntryPoint().getX(),getEntryPoint().getY());
		int centroid =    MatricialMapHelper.formKey( polygonAux.getCentroid()[0],polygonAux.getCentroid()[1]);
		int[] pointsInters = landMap.createMainRoute(referencePoint,centroid,localLayer);
		landMap.createACustomRoute
			(pointsInters[0],pointsInters[1], MatricialConfiguration.ARTERIAL_BRANCH_SIZE , MatricialConfiguration.ARTERIAL_MARK);
		return pointsInters;
	}

}
