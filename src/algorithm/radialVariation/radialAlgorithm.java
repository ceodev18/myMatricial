package algorithm.radialVariation;

import java.util.ArrayList;
import java.util.List;

import helpers.radialVariation.RadialDirectionHelper;
import helpers.radialVariation.RadialMapHelper;
import interfaces.radialVariation.RadialConfiguration;
import interfaces.radialVariation.RadialConstants;
import models.radialVariation.RadialLandMap;
import models.radialVariation.RadialLandPoint;
import models.radialVariation.RadialLandRoute;
import models.radialVariation.RadialPolygon;

public class radialAlgorithm {
	private RadialLandMap landMap;

	public RadialLandMap getLandMap() {
		return landMap;
	}

	public void setLandMap(RadialLandMap landMap) {
		this.landMap = landMap;
	}

	public void CreateRadialWeb( RadialLandPoint entryPoint ) {
		List<List<Integer>> layersPolygon = new ArrayList<>();
		List<Integer> localLayer;
		RadialPolygon polygon = new RadialPolygon();

		// select the first layer
		localLayer = new ArrayList<>();
		for(int i=0; i<landMap.getPolygonNodes().size(); i++){
			localLayer.add(landMap.getPolygonNodes().get(i).getId());
		}

		polygon.setMapPoints(localLayer);
		polygon.setComplete(true);
		layersPolygon.add(localLayer); 
		/// calculate the aprox of layers on the polygon
		int valueSeparation = RadialConfiguration.HOUSE_SIDE_MAXIMUN_SIZE * 2 + RadialConfiguration.LOCAL_BRANCH_SIZE;
		double areaTotal = landMap.getPolygonalArea();
		double areaPark = (areaTotal*2.4)/100;
		
		int valuePlus=1;
		while(true){
			int aux = valueSeparation*valuePlus;
			double possibleArea = polygon.areaShrinking(aux);
			if(possibleArea <= areaPark)break;
			if(valuePlus!= 1){
				localLayer = polygon.vectorShrinking((valuePlus-1)*valueSeparation);
				layersPolygon.add(localLayer);
			}
			localLayer = polygon.vectorShrinking(valueSeparation*(valuePlus-1) + RadialConfiguration.HOUSE_SIDE_MAXIMUN_SIZE );
			layersPolygon.add(localLayer);
			localLayer = polygon.vectorShrinking(valueSeparation*(valuePlus-1) + RadialConfiguration.HOUSE_SIDE_MAXIMUN_SIZE * 2 );
			layersPolygon.add(localLayer);
			/*
				for (int j = 0; j < layersPolygon.size(); j++) {
				landMap.createBorderFromPolygon(layersPolygon.get(j), RadialConfiguration.LOCAL_MARK);
			}
			 */

			// create the routes
			List<List<Integer>> routes = polygon.routeZone(valuePlus*RadialConfiguration.HOUSE_SIDE_MAXIMUN_SIZE * 2 + RadialConfiguration.LOCAL_BRANCH_SIZE*(valuePlus-1),
					RadialConfiguration.LOCAL_BRANCH_SIZE);
			if (routes.size() > 6) {
				for (int j = 0; j < routes.size(); j++) {
					landMap.createBorderFromPolygon(routes.get(j), RadialConfiguration.LOCAL_MARK);
				}
			} else {
					routes = new ArrayList<>();
			}
			valuePlus++;
		
		}
		
		//complete a arterial branch size
		List<List<Integer>> routesAux = polygon.routeZone((valuePlus-1)*valueSeparation, 16);
		if (routesAux.size() > 6) {
			for (int j = 0; j < routesAux.size(); j++) {
				landMap.createBorderFromPolygon(routesAux.get(j), RadialConfiguration.LOCAL_MARK);
			} 
		}else {
			routesAux = new ArrayList<>();
		}
		
		/////	
		
		localLayer = polygon.vectorShrinking(((valuePlus-1)*valueSeparation)+16);
		layersPolygon.add(localLayer);
		
		////create Principal Route
		int referencePoint =  RadialMapHelper.formKey( entryPoint.getX(),entryPoint.getY());
		int centroid =    RadialMapHelper.formKey( landMap.getCentroid().getX(),landMap.getCentroid().getY());
		int[] pointsInters = polygon.createMainRoute(referencePoint,centroid,layersPolygon.get(0));
		landMap.createACustomRoute
			(pointsInters[0],pointsInters[1],RadialConfiguration.ARTERIAL_BRANCH_SIZE , RadialConfiguration.ARTERIAL_MARK);
		//do vertix branch
		List<Integer> auxList = createVertixBranch(layersPolygon);
	
		// create the park //incomplete
		int auxvalue = ((valuePlus - 1)*3);
		landMap.createBorderFromPolygon(layersPolygon.get(auxvalue), RadialConstants.POLYGON_LIMIT);
		
				List<List<Integer>> grass = polygon.parkZone(
					(valuePlus-1)*valueSeparation+16);
				for (int j = 0; j < grass.size(); j++) {
					landMap.createBorderFromPolygon(grass.get(j), RadialConfiguration.PARK_MARK);
				}
		
		
		//polygon.parkArea((valuePlus-1)*valueSeparation + 16); //reparar
				
		//radialize  		
		Radialize(layersPolygon,pointsInters[0],pointsInters[1]);
		//RadializePrube(layersPolygon,pointsInters[0],pointsInters[1]);	
		///////index of routes
		List<List<RadialLandRoute>> ListRoutes = new ArrayList<>();
		RadialLandRoute aux = new RadialLandRoute();
		ListRoutes = aux.setRadialRoutes(layersPolygon,auxList,pointsInters[0],pointsInters[1]);
		
	
	}

	
	public List<Integer> createVertixBranch(List<List<Integer>> layersPolygon){ //que devuelva los puntos de los vertices para las rutas en puntos pares
		
		List<Integer> extLayer = layersPolygon.get(0);
		List<Integer> auxLayer = new ArrayList<>();
		List<Integer> intLayer = layersPolygon.get(layersPolygon.size()-1);
		List<Integer> auxListVertx = new ArrayList<>();
		int k =0;
		//check change of number of sizes
		int numSize = extLayer.size();
		for(int n = 0;n < layersPolygon.size();n=n+3){
			auxLayer = layersPolygon.get(n);
			if(numSize != auxLayer.size()){
				auxLayer = layersPolygon.get(n-3);
				break;
			}
		}
		
		for (int i = 0; i < intLayer.size(); i++) {
			double grad1 = landMap.findGradient(extLayer.get(k), auxLayer.get(k));
			double grad2 = landMap.findGradient(extLayer.get(k), intLayer.get(i));
			
			
			if(Math.abs(grad1 - grad2) < 0.35){// normal case
				landMap.createACustomRoute(extLayer.get(k), intLayer.get(i),RadialConfiguration.LOCAL_BRANCH_SIZE,RadialConfiguration.LOCAL_MARK);
				auxListVertx.add(extLayer.get(k));
				auxListVertx.add(intLayer.get(i));
				k++;
			}else{//find the problematic layer
				if(i == 0){ //special case
					 grad1 = landMap.findGradient(extLayer.get(i+1), auxLayer.get(i+1));
					 grad2 = landMap.findGradient(extLayer.get(i+1), intLayer.get(i+1));
					 
					 if(Math.abs(grad1 - grad2) < 0.35){ //if are equals  is the special case
						 int pointIntersection = landMap.findIntersectionPointIntoTwoStraight
								 (extLayer.get((extLayer.size()-1)), auxLayer.get((extLayer.size()-1)), extLayer.get(i), auxLayer.get(i), false);
						 landMap.createACustomRoute(extLayer.get(extLayer.size()-1),pointIntersection,RadialConfiguration.LOCAL_BRANCH_SIZE , RadialConfiguration.LOCAL_MARK);
						 landMap.createACustomRoute(extLayer.get(i),pointIntersection,RadialConfiguration.LOCAL_BRANCH_SIZE , RadialConfiguration.LOCAL_MARK);
						 landMap.createACustomRoute(pointIntersection,intLayer.get(i),RadialConfiguration.LOCAL_BRANCH_SIZE , RadialConfiguration.LOCAL_MARK);
						 k++;
						 auxListVertx.add(extLayer.get(extLayer.size()-1));
						 auxListVertx.add(pointIntersection);
						 auxListVertx.add(extLayer.get(i));
						 auxListVertx.add(pointIntersection);
						 auxListVertx.add(pointIntersection);
						 auxListVertx.add(intLayer.get(i));
						 continue;
					 }
				}
				
				int pointIntersection = landMap.findIntersectionPointIntoTwoStraight
						(extLayer.get(k), auxLayer.get(k), extLayer.get(k+1), auxLayer.get(k+1), false);
				//draw special case
				landMap.createACustomRoute(extLayer.get(k),pointIntersection,RadialConfiguration.LOCAL_BRANCH_SIZE , RadialConfiguration.LOCAL_MARK);
				landMap.createACustomRoute(extLayer.get(k+1),pointIntersection,RadialConfiguration.LOCAL_BRANCH_SIZE , RadialConfiguration.LOCAL_MARK);
				landMap.createACustomRoute(pointIntersection,intLayer.get(i),RadialConfiguration.LOCAL_BRANCH_SIZE , RadialConfiguration.LOCAL_MARK);
				auxListVertx.add(extLayer.get(k));
				auxListVertx.add(pointIntersection);
				auxListVertx.add(extLayer.get(k+1));
				auxListVertx.add(pointIntersection);
				auxListVertx.add(pointIntersection);
				auxListVertx.add(intLayer.get(i));
				k=k+2;
			}
			
		}
		return auxListVertx;
	}
	
	private void RadializePrube(List<List<Integer>>  layersPolygon,int pointIni, int pointEnd){
		int i= 9;
		int j= 2;
		
		
		int point1Top = layersPolygon.get(i).get(j % (layersPolygon.get(i).size()));
		
		int point2Top = layersPolygon.get(i).get((j+1)%(layersPolygon.get(i).size()));
		
		int point1Mid = layersPolygon.get(i + 1).get(j % (layersPolygon.get(i+1).size()));
		int point2Mid = layersPolygon.get(i + 1).get((j+1)%(layersPolygon.get(i+1).size()));
		int point1Down = layersPolygon.get(i + 2).get(j % (layersPolygon.get(i+2).size()));
		int point2Down = layersPolygon.get(i + 2).get((j+1)%(layersPolygon.get(i+2).size()));
		
		double  distanceTop = landMap.distanceOfPointToPoint(point1Top,point2Top);
		double  distanceDown = landMap.distanceOfPointToPoint(point1Down,point2Down); 	
		
		//verify minimun area
		double area = (RadialConfiguration.HOUSE_SIDE_MAXIMUN_SIZE*2*((distanceTop-RadialConfiguration.LOCAL_BRANCH_SIZE )+ (distanceDown-RadialConfiguration.LOCAL_BRANCH_SIZE )))/2;
		if(area < 90 || distanceDown<(6+ RadialConfiguration.LOCAL_BRANCH_SIZE) || distanceTop<(6+ RadialConfiguration.LOCAL_BRANCH_SIZE )){
			//caso contrario volver area libre 
			return;
		}
		
		
		int aux1Point = landMap.findIntersectionPointIntoTwoStraight(pointIni,pointEnd,point1Top,point2Top,true) ;
		int aux2Point = landMap.findIntersectionPointIntoTwoStraight(pointIni,pointEnd,point1Down,point2Down,true);
		int aux3Point = landMap.findIntersectionPointIntoTwoStraight(pointIni,pointEnd,point1Mid,point2Mid,true);
		
		int fixPoint1Top = landMap.findPointOnStreightToDistance( point1Top, point2Top,(RadialConfiguration.LOCAL_BRANCH_SIZE/2)+4);
		int fixPoint2Top = landMap.findPointOnStreightToDistance(point2Top, point1Top, (RadialConfiguration.LOCAL_BRANCH_SIZE/2)+4);
		int fixPoint1Mid = landMap.findPointOnStreightToDistance( point1Mid, point2Mid,(RadialConfiguration.LOCAL_BRANCH_SIZE/2)+4);
		int fixPoint2Mid = landMap.findPointOnStreightToDistance(point2Mid, point1Mid, (RadialConfiguration.LOCAL_BRANCH_SIZE/2)+4);
		int fixPoint1Down = landMap.findPointOnStreightToDistance( point1Down, point2Down,(RadialConfiguration.LOCAL_BRANCH_SIZE/2)+4);
		int fixPoint2Down = landMap.findPointOnStreightToDistance(point2Down, point1Down, (RadialConfiguration.LOCAL_BRANCH_SIZE/2)+4);
		
		if((aux1Point != -1 && aux2Point != -1) || (aux2Point != -1 && i==0)  ){	
			if(aux2Point != -1 && i==0){
				aux1Point = landMap.findIntersectionPointIntoTwoStraight(pointIni,pointEnd,point1Top,point2Top,false) ;
				aux3Point = landMap.findIntersectionPointIntoTwoStraight(pointIni,pointEnd,point1Mid,point2Mid,false);
			}
			int fixAux1Left = landMap.findPointOnStreightToDistance( aux1Point, point1Top,(RadialConfiguration.ARTERIAL_BRANCH_SIZE/2)+4);
			int fixAux2Left = landMap.findPointOnStreightToDistance(aux2Point, point1Down, (RadialConfiguration.ARTERIAL_BRANCH_SIZE/2)+4);
			int fixAux3Left = landMap.findPointOnStreightToDistance(aux3Point, point1Mid, (RadialConfiguration.ARTERIAL_BRANCH_SIZE/2)+4);
			int fixAux1Right = landMap.findPointOnStreightToDistance( aux1Point, point2Top,(RadialConfiguration.ARTERIAL_BRANCH_SIZE/2)+4);
			int fixAux2Right = landMap.findPointOnStreightToDistance(aux2Point, point2Down, (RadialConfiguration.ARTERIAL_BRANCH_SIZE/2)+4);
			int fixAux3Right = landMap.findPointOnStreightToDistance(aux3Point, point2Mid, (RadialConfiguration.ARTERIAL_BRANCH_SIZE/2)+4);
			
			
			sectorize(fixPoint1Top,fixAux1Left,fixPoint1Mid,fixAux3Left,fixPoint1Down,fixAux2Left);
			sectorize(fixAux1Right,fixPoint2Top,fixAux3Right,fixPoint2Mid,fixAux2Right,fixPoint2Down);
			return;
		}
		// do zonification
		sectorize(fixPoint1Top,fixPoint2Top,fixPoint1Mid,fixPoint2Mid,fixPoint1Down,fixPoint2Down);
		
		
	}
	
	private void Radialize(List<List<Integer>> layersPolygon,int pointIni, int pointEnd) {
		
		boolean specialCase = false;
		for(int i = 0;i < (layersPolygon.size() - 3); i= i+3){
			
			for(int j = 0;j < layersPolygon.get(i).size();j++){
				
				if(specialCase == true && (j == (layersPolygon.get(i+2).size()))){
					specialCase = false;
					continue;
				}
				
				int point1Top = layersPolygon.get(i).get(j % (layersPolygon.get(i).size()));
				int point2Top = layersPolygon.get(i).get((j+1)%(layersPolygon.get(i).size()));
				
				int point1Mid = layersPolygon.get(i + 1).get(j % (layersPolygon.get(i+1).size()));
				int point2Mid = layersPolygon.get(i + 1).get((j+1)%(layersPolygon.get(i+1).size()));
				int point1Down = layersPolygon.get(i + 2).get(j % (layersPolygon.get(i+2).size()));
				int point2Down = layersPolygon.get(i + 2).get((j+1)%(layersPolygon.get(i+2).size()));
			
				// find distances
				double  distanceTop = landMap.distanceOfPointToPoint(point1Top,point2Top);
				double  distanceDown = landMap.distanceOfPointToPoint(point1Down,point2Down); 	
				double  distanceMid = landMap.distanceOfPointToPoint(point1Mid,point2Mid);
			
				if(specialCase == true){
					point1Top = point2Top;
					point2Top = layersPolygon.get(i).get((j+2)%(layersPolygon.get(i).size())); 
					distanceTop = landMap.distanceOfPointToPoint(point1Top,point2Top);
					
					point1Mid = point2Mid;
					point2Mid = layersPolygon.get(i + 1).get((j+2)%(layersPolygon.get(i+1).size()));
				
				}
				//verify 5-4 points
				if(distanceTop < RadialConfiguration.LOCAL_BRANCH_SIZE || distanceTop < (distanceDown/2) ){
					//special case
					point1Top = point2Top;
					point2Top = layersPolygon.get(i).get((j+2)%(layersPolygon.get(i).size())); 
					distanceTop = landMap.distanceOfPointToPoint(point1Top,point2Top);
					specialCase = true;
					if(distanceDown > distanceMid){
					    point1Mid = point2Mid;
						point2Mid = layersPolygon.get(i + 1).get((j+2)%(layersPolygon.get(i+1).size()));

					}
				}
				
				//verify minimun area
				double area = (RadialConfiguration.HOUSE_SIDE_MAXIMUN_SIZE*2*((distanceTop-RadialConfiguration.LOCAL_BRANCH_SIZE )+ (distanceDown-RadialConfiguration.LOCAL_BRANCH_SIZE )))/2;
				if(area < 90 || distanceDown<(6+ RadialConfiguration.LOCAL_BRANCH_SIZE) || distanceTop<(6+ RadialConfiguration.LOCAL_BRANCH_SIZE )){
					//caso contrario volver area libre 
					continue;
				}
				//verify main route across
				int aux1Point = landMap.findIntersectionPointIntoTwoStraight(pointIni,pointEnd,point1Top,point2Top,true) ;
				int aux2Point = landMap.findIntersectionPointIntoTwoStraight(pointIni,pointEnd,point1Down,point2Down,true);
				int aux3Point = landMap.findIntersectionPointIntoTwoStraight(pointIni,pointEnd,point1Mid,point2Mid,true);
				
				int fixPoint1Top = landMap.findPointOnStreightToDistance( point1Top, point2Top,(RadialConfiguration.LOCAL_BRANCH_SIZE/2));
				int fixPoint2Top = landMap.findPointOnStreightToDistance(point2Top, point1Top, (RadialConfiguration.LOCAL_BRANCH_SIZE/2));
				int fixPoint1Mid = landMap.findPointOnStreightToDistance( point1Mid, point2Mid,(RadialConfiguration.LOCAL_BRANCH_SIZE/2));
				int fixPoint2Mid = landMap.findPointOnStreightToDistance(point2Mid, point1Mid, (RadialConfiguration.LOCAL_BRANCH_SIZE/2));
				
				int fixPoint1Down = landMap.findPointOnStreightToDistance( point1Down, point2Down,(RadialConfiguration.LOCAL_BRANCH_SIZE/2));
				int fixPoint2Down = landMap.findPointOnStreightToDistance(point2Down, point1Down, (RadialConfiguration.LOCAL_BRANCH_SIZE/2));
				
				if((aux1Point != -1 && aux2Point != -1) || (aux2Point != -1 && i==0)  ){	///would be crossing just one size
					if(aux2Point != -1 && i==0){
						aux1Point = landMap.findIntersectionPointIntoTwoStraight(pointIni,pointEnd,point1Top,point2Top,false) ;
						aux3Point = landMap.findIntersectionPointIntoTwoStraight(pointIni,pointEnd,point1Mid,point2Mid,false);
					}	
					int fixAux1Left = landMap.findPointOnStreightToDistance( aux1Point, point1Top,(RadialConfiguration.ARTERIAL_BRANCH_SIZE/2));
					int fixAux2Left = landMap.findPointOnStreightToDistance(aux2Point, point1Down, (RadialConfiguration.ARTERIAL_BRANCH_SIZE/2));
					int fixAux3Left = landMap.findPointOnStreightToDistance(aux3Point, point1Mid, (RadialConfiguration.ARTERIAL_BRANCH_SIZE/2));
					int fixAux1Right = landMap.findPointOnStreightToDistance( aux1Point, point2Top,(RadialConfiguration.ARTERIAL_BRANCH_SIZE/2));
					int fixAux2Right = landMap.findPointOnStreightToDistance(aux2Point, point2Down, (RadialConfiguration.ARTERIAL_BRANCH_SIZE/2));
					int fixAux3Right = landMap.findPointOnStreightToDistance(aux3Point, point2Mid, (RadialConfiguration.ARTERIAL_BRANCH_SIZE/2));
					
					
					sectorize(fixPoint1Top,fixAux1Left,fixPoint1Mid,fixAux3Left,fixPoint1Down,fixAux2Left);
					sectorize(fixAux1Right,fixPoint2Top,fixAux3Right,fixPoint2Mid,fixAux2Right,fixPoint2Down);
					continue;
				}
				// do zonification
				sectorize(fixPoint1Top,fixPoint2Top,fixPoint1Mid,fixPoint2Mid,fixPoint1Down,fixPoint2Down);
			}
		}
		
	}

	private void sectorize(int point1Top,int point2Top,int point1Mid,int point2Mid,int point1Down,int point2Down){
		
		//verify total area again //find distances again
		double  distanceTop = landMap.distanceOfPointToPoint(point1Top,point2Top);
		double  distanceDown = landMap.distanceOfPointToPoint(point1Down,point2Down); 	
		double area = (RadialConfiguration.HOUSE_SIDE_MAXIMUN_SIZE*2*((distanceTop-RadialConfiguration.LOCAL_BRANCH_SIZE )+ (distanceDown-RadialConfiguration.LOCAL_BRANCH_SIZE )))/2;
		if(area < 90 || distanceDown < 6 || distanceTop < 6){
			//caso contrario volver area libre 
			return;
		}
		
		
		//do algorithm with the major  like reference
		//find points to know the polygon form
		int pntLeftUp = landMap.findProyectionPointIntoParalelStraights(point1Top,point2Top,point1Down,true);
		int pntRightUp = landMap.findProyectionPointIntoParalelStraights(point1Top,point2Top,point2Down,true);
		int pntLeftDown = landMap.findProyectionPointIntoParalelStraights(point1Down,point2Down,point1Top,true);
		int pntRightDown = landMap.findProyectionPointIntoParalelStraights(point1Down,point2Down,point2Top,true);
		if((pntLeftUp == -1 && pntLeftDown == -1) || (pntRightUp == -1 && pntRightDown == -1))return;
		
		if(pntLeftUp == -1)pntLeftUp = point1Top;
		if(pntRightUp == -1) pntRightUp = point2Top;
		if(pntLeftDown == -1) pntLeftDown = point1Down;
		if(pntRightDown == -1) pntRightDown = point2Down;
		//find number of division of houses 
		double distanceLarge = landMap.distanceOfPointToPoint(pntLeftUp,pntRightUp);
		int nDivision = (int)(distanceLarge/(RadialConfiguration.HOUSE_SIDE_MINIMUN_SIZE));
		int  extraDistance= (int)(distanceLarge % (RadialConfiguration.HOUSE_SIDE_MINIMUN_SIZE));
		//print houses
		int initPoint1 = pntLeftUp;
		int aux1Point = landMap.findProyectionPointIntoParalelStraights(point1Mid,point2Mid,initPoint1,true);
		int initPoint2 = aux1Point;
		int aux2Point = landMap.findProyectionPointIntoParalelStraights(point1Mid,point2Mid,pntRightUp,true);
		for(int i = 0 ;i <= nDivision; i++){
			if((i%2) == 0){
				int endPoint1 = generateHouse(initPoint1,pntRightUp,aux1Point,aux2Point,RadialConfiguration.HOUSE_SIDE_MINIMUN_SIZE,RadialConfiguration.MARK_LOT1);
				int aux = landMap.findPointOnStreightToDistance( pntLeftUp, pntRightUp, RadialConfiguration.HOUSE_SIDE_MINIMUN_SIZE*(i+1));
				initPoint1 = aux;
			
				int endPoint2 = generateHouse(initPoint2,aux2Point,pntLeftDown,pntRightDown,RadialConfiguration.HOUSE_SIDE_MINIMUN_SIZE,RadialConfiguration.MARK_LOT2);
				int aux2 = landMap.findPointOnStreightToDistance( aux1Point, aux2Point, RadialConfiguration.HOUSE_SIDE_MINIMUN_SIZE*(i+1));
				initPoint2 = aux2;
			}else
			if((i%2) == 1){
				int endPoint1 = generateHouse(initPoint1,pntRightUp,aux1Point,aux2Point,RadialConfiguration.HOUSE_SIDE_MINIMUN_SIZE,RadialConfiguration.MARK_LOT2);
				int aux = landMap.findPointOnStreightToDistance( pntLeftUp, pntRightUp, RadialConfiguration.HOUSE_SIDE_MINIMUN_SIZE*(i+1));
				initPoint1 = aux;
			
				int endPoint2 = generateHouse(initPoint2,aux2Point,pntLeftDown,pntRightDown,RadialConfiguration.HOUSE_SIDE_MINIMUN_SIZE,RadialConfiguration.MARK_LOT1);
				int aux2 = landMap.findPointOnStreightToDistance( aux1Point, aux2Point, RadialConfiguration.HOUSE_SIDE_MINIMUN_SIZE*(i+1));
				initPoint2 = aux2;
			}
		}
		
		//analize special cases
	}
	
	
	
	private int generateHouse(int pnt1Up,int pnt2Up, int pnt1Dwn, int pnt2Dwn,int dist,String mask ){
		int pointSolution =-1;
		int xyRec1Ini[] = RadialMapHelper.breakKey(pnt1Up);
		int xyRec1End[] = RadialMapHelper.breakKey(pnt2Up);
		
		int underscore = (xyRec1End[0] - xyRec1Ini[0]);
		double gradient = 0;
		double b = 0;
		
		if (underscore == 0) {
			if(xyRec1Ini[1] < xyRec1End[1] ){ 
				for(int i=0;i< dist;i++){
					int auxPoint1 = RadialMapHelper.formKey(xyRec1End[0],(xyRec1Ini[1] + i));
					int auxPoint2 = landMap.findProyectionPointIntoParalelStraights(pnt1Dwn,pnt2Dwn,auxPoint1,false);
					landMap.createALine( auxPoint1,auxPoint2,mask);
				}
				pointSolution =  RadialMapHelper.formKey(xyRec1End[0],(xyRec1Ini[1] + (int)dist));		
			}else{
				for(int i=0;i< dist;i++){
					int auxPoint1 = RadialMapHelper.formKey(xyRec1End[0],(xyRec1Ini[1] - i));
					int auxPoint2 = landMap.findProyectionPointIntoParalelStraights(pnt1Dwn,pnt2Dwn,auxPoint1,false);
					landMap.createALine( auxPoint1,auxPoint2,mask);
				}
				pointSolution =  RadialMapHelper.formKey(xyRec1End[0],(xyRec1Ini[1] - (int)dist));		
			}
			return pointSolution;
		}else{
			gradient = (xyRec1End[1] - xyRec1Ini[1]) * 1.0 / underscore;
			b = (xyRec1Ini[0]*xyRec1End[1] -(xyRec1End[0]*xyRec1Ini[1]))/(xyRec1Ini[0] -xyRec1End[0]);
			if(gradient == 0){
				if(xyRec1Ini[0] < xyRec1End[0]){ 
					for(int i=0;i< dist;i++){
						int auxPoint1 = RadialMapHelper.formKey(xyRec1End[0] + i,(xyRec1End[1]));
						int auxPoint2 = landMap.findProyectionPointIntoParalelStraights(pnt1Dwn,pnt2Dwn,auxPoint1,false);
						landMap.createALine( auxPoint1,auxPoint2,mask);
					}
					pointSolution =  RadialMapHelper.formKey( (xyRec1Ini[0] + (int)dist),xyRec1End[1] );		
				}else{
					for(int i=0;i< dist;i++){
						int auxPoint1 = RadialMapHelper.formKey(xyRec1End[0] - i,(xyRec1End[1]));
						int auxPoint2 = landMap.findProyectionPointIntoParalelStraights(pnt1Dwn,pnt2Dwn,auxPoint1,false);
						landMap.createALine( auxPoint1,auxPoint2,mask);
					}	
					pointSolution =  RadialMapHelper.formKey( (xyRec1Ini[0] - (int)dist),xyRec1End[1] );	
				}
				return pointSolution;
			}
			int sign,init,end;
			boolean onX = false;
			if(Math.abs(xyRec1End[0]-xyRec1Ini[0]) <  Math.abs(xyRec1End[1]-xyRec1Ini[1])){
			//do on Y
				init = xyRec1Ini[1];
				end = xyRec1End[1];
				if(xyRec1Ini[1] < xyRec1End[1]) sign = 1;
				else{
					sign = -1;
				}
			}else{
			//do on X
				init = xyRec1Ini[0];
				end = xyRec1End[0];
				if(xyRec1Ini[0] < xyRec1End[0] ) sign = 1;
				else{ 
					sign = -1;
				}
				onX = true;
			}
			if(sign == 1){
				for(int i = init; i <= end; i++){
					double x,y;
					if(onX){
						x = i;
						y= gradient*x + b;
					}else{
						y= i;
						x = (y-b)/gradient; 
					}
					RadialMapHelper.round(x);
					RadialMapHelper.round(y);
					pointSolution =  RadialMapHelper.formKey( (int)x,(int)y );
					int auxPoint = landMap.findProyectionPointIntoParalelStraights(pnt1Dwn,pnt2Dwn,pointSolution,false);
					landMap.createALine( pointSolution,auxPoint,mask);
					double auxDist = landMap.distanceOfPointToPoint(pnt1Up,pointSolution);
					if(auxDist >= dist){	
						return pointSolution;
					}
				}
			}else if(sign == -1){
				for(int i = init; i >= end;  i--){
					double x,y;
					if(onX){
						x = i;
						y= gradient*x + b;
					}else{
						y= i;
						x = (y-b)/gradient; 
					}
					RadialMapHelper.round(x);
					RadialMapHelper.round(y);
					pointSolution =  RadialMapHelper.formKey( (int)x,(int)y );
					int auxPoint = landMap.findProyectionPointIntoParalelStraights(pnt1Dwn,pnt2Dwn,pointSolution,false);
					landMap.createALine( pointSolution,auxPoint,mask);
					double auxDist = landMap.distanceOfPointToPoint(pnt1Up,pointSolution);
					if(auxDist >= dist){	
						return pointSolution;
					}
				}
			}	
		}
		return pointSolution;
	}
	
	public void zonify() {
		// findZonificationAreas();
		organicZonification();
	}

	// Organic zonification: This zonification searchs the figure and
	// reduces it in a reason configured by the user. if it can be done, it
	// becomes a new Radial. If not, it becomes simply defaults into a
	// perfect zonification
	private void organicZonification() {
		for (int y = landMap.getPointsy() - 1; y >= 0; y--) {
			boolean insidePolygon = false;
			for (int x = 0; x < landMap.getPointsx(); x++) {
				if (insidePolygon && landMap.findPoint(RadialMapHelper.formKey(x, y)).getType()
						.equals(RadialConfiguration.OUTSIDE_POLYGON_MARK)) {
					break;
				}
				if (!insidePolygon && !landMap.findPoint(RadialMapHelper.formKey(x, y)).getType()
						.equals(RadialConfiguration.OUTSIDE_POLYGON_MARK)) {
					insidePolygon = true;
				}
				boolean identifiedNormalNode = false;
				if (landMap.isNormalNode(x, y)
						&& landMap.findPoint(RadialMapHelper.formKey((x + 1), y)).getType()
								.equals(RadialConfiguration.NODE_MARK)
						&& landMap.findPoint(RadialMapHelper.formKey((x + 1), (y + 1))).getType()
								.equals(RadialConfiguration.EMPTY_MARK)) {
					identifiedNormalNode = true;
					RadialPolygon RadialPolygon = new RadialPolygon();
					/* int nextPoint= */
					createOrganicCoverture(RadialMapHelper.formKey(x, y), RadialConstants.EAST, RadialPolygon);
					boolean passedThough = false;
					if (RadialPolygon.getPoints().size() == 2) {
						// finish the polygon and detect incompleteness
						completeOrganicCoverture(RadialMapHelper.formKey(x, y), RadialConstants.NORTH, RadialPolygon);
						RadialPolygon.rehashPolygon(RadialConfiguration.TYPE_COMPLETE);
						passedThough = true;
					}

					if (!RadialPolygon.isComplete() && RadialPolygon.getPoints().size() > 2) {
						System.out.println("Pre polygon join with border ");
						RadialPolygon.printPolygon();
						// RadialPolygon =
						// joinWithPolygonalBorder(RadialPolygon);
					}

					if (!passedThough && !RadialPolygon.isComplete() && RadialPolygon.getPoints().size() == 3) {
						completeOrganicCoverture(RadialMapHelper.formKey(x, y), RadialConstants.NORTH, RadialPolygon);
						RadialPolygon.rehashPolygon(RadialConfiguration.TYPE_COMPLETE);
						// RadialPolygon =
						// joinWithPolygonalBorder(RadialPolygon);
					}
					RadialPolygon.setCentroid(RadialPolygon.findCentroid());

					// we create the park
					List<List<Integer>> grass = RadialPolygon.parkZone(
							RadialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2 + RadialConfiguration.LOCAL_BRANCH_SIZE);
					for (int j = 0; j < grass.size(); j++) {
						landMap.createBorderFromPolygon(grass.get(j), RadialConfiguration.PARK_MARK);
					}

					// we create the routes
					List<List<Integer>> routes = RadialPolygon.routeZone(
							RadialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2, RadialConfiguration.LOCAL_BRANCH_SIZE);
					if (routes.size() > 6) {
						for (int j = 0; j < routes.size(); j++) {
							landMap.createBorderFromPolygon(routes.get(j), RadialConfiguration.LOCAL_MARK);
						}
					} else {
						routes = new ArrayList<>();
					}

					// we create the houses
					List<List<Integer>> lowerBorder = RadialPolygon
							.routeZone(RadialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2 - 1, 1);
					if (lowerBorder.size() > 0) {
						landMap.lotize(lowerBorder.get(0), RadialConstants.EAST, 0);
					}

					if ((routes.size() == 0)) {
						List<List<Integer>> contribution = RadialPolygon.contributionZone();
						for (int j = 0; j < contribution.size(); j++) {
							landMap.createBorderFromPolygon(contribution.get(j), RadialConfiguration.CONTRIBUTION_MARK);
						}
					}
				}

				// In case of special node (non regular spaced node
				if (!identifiedNormalNode && landMap.isSpecialNode(x, y)) {
					RadialPolygon RadialPolygon = new RadialPolygon();
					createSpecialCoverture(RadialMapHelper.formKey(x, y), RadialConstants.NORTH, RadialPolygon);
					System.out.println("Special polygon");
					RadialPolygon.printPolygon();
					RadialPolygon.rehashPolygon(RadialConfiguration.TYPE_SPECIAL);
					RadialPolygon.setCentroid(RadialPolygon.findCentroid());

					// we create the park
					List<List<Integer>> grass = RadialPolygon.parkZone(
							RadialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2 + RadialConfiguration.LOCAL_BRANCH_SIZE);
					for (int j = 0; j < grass.size(); j++) {
						landMap.createBorderFromPolygon(grass.get(j), RadialConfiguration.PARK_MARK);
					}

					// we create the routes
					List<List<Integer>> routes = RadialPolygon.routeZone(
							RadialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2, RadialConfiguration.LOCAL_BRANCH_SIZE);
					if (routes.size() > 6) {
						for (int j = 0; j < routes.size(); j++) {
							landMap.createBorderFromPolygon(routes.get(j), RadialConfiguration.LOCAL_MARK);
						}
					} else {
						routes = new ArrayList<>();
					}

					// we create the houses
					List<List<Integer>> lowerBorder = RadialPolygon
							.routeZone(RadialConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2 - 1, 1);
					if (lowerBorder.size() > 0) {
						int direction = RadialDirectionHelper.orthogonalDirectionFromPointToPoint(
								lowerBorder.get(0).get(0), lowerBorder.get(0).get(1));
						landMap.lotize(lowerBorder.get(0), direction, 0);
					}

					if ((routes.size() == 0)) {
						List<List<Integer>> contribution = RadialPolygon.contributionZone();
						for (int j = 0; j < contribution.size(); j++) {
							landMap.createBorderFromPolygon(contribution.get(j), RadialConfiguration.CONTRIBUTION_MARK);
						}
					}
				}
			}
		}
		// perfectZonification();
	}

	private Object createSpecialCoverture(int initialKey, int direction, RadialPolygon RadialPolygon) {
		RadialPolygon.getPoints().add(initialKey);
		boolean borderFound = false;
		while (!borderFound) {
			initialKey = RadialMapHelper.moveKeyByOffsetAndDirection(initialKey, 1, direction);
			int[] firstNode = RadialMapHelper.breakKey(initialKey);
			switch (direction) {
			case RadialConstants.NORTH:
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0] + 1, firstNode[1])).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					return createSpecialCoverture(initialKey, RadialConstants.EAST, RadialPolygon);
				}
				break;
			case RadialConstants.EAST:
				// east, north, west, south
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1] - 1)).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					return createSpecialCoverture(initialKey, RadialConstants.SOUTH, RadialPolygon);
				}
				break;
			case RadialConstants.SOUTH:
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0] - 1, firstNode[1])).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					return createSpecialCoverture(initialKey, RadialConstants.WEST, RadialPolygon);

				}
				break;
			case RadialConstants.WEST:
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1] + 1)).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					borderFound = true;
					RadialPolygon.setComplete(true);
				}
				break;
			}

			if (!RadialPolygon.isComplete() && landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1]))
					.getType().equals(RadialConfiguration.OUTSIDE_POLYGON_MARK)) {
				switch (direction) {
				case RadialConstants.EAST:
					RadialPolygon.getPoints().add(RadialMapHelper.formKey(firstNode[0] - 1, firstNode[1]));
					break;
				case RadialConstants.NORTH:
					RadialPolygon.getPoints().add(RadialMapHelper.formKey(firstNode[0], firstNode[1] - 1));
					break;
				case RadialConstants.WEST:
					RadialPolygon.getPoints().add(RadialMapHelper.formKey(firstNode[0] + 1, firstNode[1]));
					break;
				case RadialConstants.SOUTH:
					RadialPolygon.getPoints().add(RadialMapHelper.formKey(firstNode[0], firstNode[1] + 1));
					break;
				}
				return -1;
			}
			landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1]))
					.setType(RadialConfiguration.BORDER_MARK);
		}
		return initialKey;
	}

	/**
	 * In case there is not a direct square or a lateral east, north west
	 * polygon, we seize the opposite side to complete the polygon
	 */
	private Object completeOrganicCoverture(int initialKey, int direction, RadialPolygon RadialPolygon) {
		if (direction != RadialConstants.NORTH) {
			int numExpansions = RadialPolygon.getExpansions() + 1;
			RadialPolygon.setExpansions(numExpansions);
			RadialPolygon.getPoints().add(0, initialKey);
		}

		boolean borderFound = false;
		while (!borderFound) {
			initialKey = RadialMapHelper.moveKeyByOffsetAndDirection(initialKey, 1, direction);
			int[] firstNode = RadialMapHelper.breakKey(initialKey);
			switch (direction) {
			case RadialConstants.EAST:
				// east, north, west, south
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1] - 1)).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					return completeOrganicCoverture(initialKey, RadialConstants.SOUTH, RadialPolygon);
				}
				break;
			case RadialConstants.NORTH:
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0] + 1, firstNode[1])).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					return completeOrganicCoverture(initialKey, RadialConstants.EAST, RadialPolygon);
				}
				break;
			case RadialConstants.WEST:
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1] + 1)).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					borderFound = true;
					RadialPolygon.setComplete(true);
				}
				break;
			case RadialConstants.SOUTH:
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0] - 1, firstNode[1])).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					return completeOrganicCoverture(initialKey, RadialConstants.WEST, RadialPolygon);

				}
				break;
			}

			if (!RadialPolygon.isComplete() && landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1]))
					.getType().equals(RadialConfiguration.OUTSIDE_POLYGON_MARK)) {
				switch (direction) {
				case RadialConstants.EAST:
					RadialPolygon.getPoints().add(0, RadialMapHelper.formKey(firstNode[0] - 1, firstNode[1]));
					break;
				case RadialConstants.NORTH:
					RadialPolygon.getPoints().add(0, RadialMapHelper.formKey(firstNode[0], firstNode[1] - 1));
					break;
				case RadialConstants.WEST:
					RadialPolygon.getPoints().add(0, RadialMapHelper.formKey(firstNode[0] + 1, firstNode[1]));
					break;
				case RadialConstants.SOUTH:
					RadialPolygon.getPoints().add(0, RadialMapHelper.formKey(firstNode[0], firstNode[1] + 1));
					break;
				}
				return -1;
			}
			landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1]))
					.setType(RadialConfiguration.BORDER_MARK);
		}
		return initialKey;
	}

	private int createOrganicCoverture(int initialKey, int direction, RadialPolygon RadialPolygon) {
		RadialPolygon.getPoints().add(initialKey);
		boolean borderFound = false;
		while (!borderFound) {
			initialKey = RadialMapHelper.moveKeyByOffsetAndDirection(initialKey, 1, direction);
			int[] firstNode = RadialMapHelper.breakKey(initialKey);
			switch (direction) {
			case RadialConstants.EAST:
				// east, north, west, south
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1] + 1)).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					return createOrganicCoverture(initialKey, RadialConstants.NORTH, RadialPolygon);
				}
				break;
			case RadialConstants.NORTH:
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0] - 1, firstNode[1])).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					return createOrganicCoverture(initialKey, RadialConstants.WEST, RadialPolygon);
				}
				break;
			case RadialConstants.WEST:
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1] - 1)).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					return createOrganicCoverture(initialKey, RadialConstants.SOUTH, RadialPolygon);
				}
				break;
			case RadialConstants.SOUTH:
				if (landMap.findPoint(RadialMapHelper.formKey(firstNode[0] + 1, firstNode[1])).getType()
						.equals(RadialConfiguration.NODE_MARK)) {
					borderFound = true;
					RadialPolygon.setComplete(true);
				}
				break;
			}

			if (!RadialPolygon.isComplete() && landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1]))
					.getType().equals(RadialConfiguration.OUTSIDE_POLYGON_MARK)) {
				switch (direction) {
				case RadialConstants.EAST:
					RadialPolygon.getPoints().add(RadialMapHelper.formKey(firstNode[0] - 1, firstNode[1]));
					break;
				case RadialConstants.NORTH:
					RadialPolygon.getPoints().add(RadialMapHelper.formKey(firstNode[0], firstNode[1] - 1));
					break;
				case RadialConstants.WEST:
					RadialPolygon.getPoints().add(RadialMapHelper.formKey(firstNode[0] + 1, firstNode[1]));
					break;
				case RadialConstants.SOUTH:
					RadialPolygon.getPoints().add(RadialMapHelper.formKey(firstNode[0], firstNode[1] + 1));
					break;
				}
				return -1;
			}
			landMap.findPoint(RadialMapHelper.formKey(firstNode[0], firstNode[1]))
					.setType(RadialConfiguration.BORDER_MARK);
		}
		return initialKey;
	}

}
