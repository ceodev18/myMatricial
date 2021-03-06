package algorithm.radialVariation;

import java.util.ArrayList;
import java.util.List;

import helpers.radialVariation.RadialMapHelper;
import interfaces.radialVariation.RadialConfiguration;
import models.radialVariation.RadialLandMap;
import models.radialVariation.RadialLandPoint;
import models.radialVariation.RadialLandRoute;
import models.radialVariation.RadialPolygon;

public class radialAlgorithm {
	private List<List<RadialLandRoute>> ListRoutes;
	
	private RadialLandMap landMap;

	public RadialLandMap getLandMap() {
		return landMap;
	}
	
	public List<List<RadialLandRoute>> getListRoutes(){
		return ListRoutes;
	}
	public void setListRoutes(List<List<RadialLandRoute>> auxList){
		this.ListRoutes = auxList;
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
		//border of polygon print new grammar
		/*String borderGrammar = "o-";
		for (int i = 0; i < localLayer.size(); i++) {
			int[] xy = RadialMapHelper.breakKey(localLayer.get(i));
			borderGrammar += xy[0] + "-" + xy[1];
			if(i+1<localLayer.size()){
				borderGrammar += "-";
			}
		}
		landMap.findPoint(localLayer.get(0)).setGramaticalType(borderGrammar);	
		*/
		/// calculate the aprox of layers on the polygon
		int valueSeparation = landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + RadialConfiguration.LOCAL_BRANCH_SIZE;
		double areaTotal = landMap.getPolygonalArea();
		double areaPark = (areaTotal*2.4)/100;
		
		int valuePlus=1;
		while(true){
			int aux = valueSeparation*valuePlus;
			///avoid error in big areas
			if(valuePlus==6)areaPark = (areaTotal*3)/100;
			if(valuePlus==7)areaPark = (areaTotal*3.5)/100;
			if(valuePlus==8)areaPark = (areaTotal*4)/100;
			if(valuePlus==9)areaPark = (areaTotal*5)/100;
			double possibleArea = polygon.areaShrinking(aux);
			double possibleAreaNext = polygon.areaShrinking(aux + valueSeparation);
			if(possibleAreaNext == 0)break;
			if(possibleArea <= (areaPark))break;
			if(valuePlus!= 1){
				localLayer = polygon.vectorShrinking((valuePlus-1)*valueSeparation);
				layersPolygon.add(localLayer);
			}
			localLayer = polygon.vectorShrinking(valueSeparation*(valuePlus-1) + landMap.getConfiguration().getLotConfiguration().getDepthSize() );
			layersPolygon.add(localLayer);
			localLayer = polygon.vectorShrinking(valueSeparation*(valuePlus-1) + landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 );
			layersPolygon.add(localLayer);
			/*
				for (int j = 0; j < layersPolygon.size(); j++) {
				landMap.createBorderFromPolygon(layersPolygon.get(j), RadialConfiguration.LOCAL_MARK);
			}
			 */
			/*
			// create the routes
			List<List<Integer>> routes = polygon.routeZone(valuePlus*landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + RadialConfiguration.LOCAL_BRANCH_SIZE*(valuePlus-1),
					RadialConfiguration.LOCAL_BRANCH_SIZE);
			if (routes.size() > 6) {
				for (int j = 0; j < routes.size(); j++) {
					landMap.createBorderFromPolygon(routes.get(j), RadialConfiguration.LOCAL_MARK);
				}
			} else {
					routes = new ArrayList<>();
			}
			*/
			
			valuePlus++;
		
		}
		/*
		//complete a arterial branch size
		List<List<Integer>> routesAux = polygon.routeZone((valuePlus-1)*valueSeparation, (RadialConfiguration.ARTERIAL_BRANCH_SIZE-RadialConfiguration.LOCAL_BRANCH_SIZE));
		if (routesAux.size() > 6) {
			for (int j = 0; j < routesAux.size(); j++) {
				landMap.createBorderFromPolygon(routesAux.get(j), RadialConfiguration.LOCAL_MARK);
			} 
		}else {
			routesAux = new ArrayList<>();
		}
		*/
		/////	
		
		localLayer = polygon.vectorShrinking(((valuePlus-1)*valueSeparation)+(RadialConfiguration.ARTERIAL_BRANCH_SIZE-RadialConfiguration.LOCAL_BRANCH_SIZE));
		layersPolygon.add(localLayer);
		//To create the park 
		int auxvalue = ((valuePlus-1)*3);
		//landMap.createBorderFromPolygon(layersPolygon.get(auxvalue), RadialConfiguration.PARK_MARK);
		//border of the park print new grammar
				String grassGrammar = "g-";
				for (int i = 0; i < layersPolygon.get(auxvalue).size(); i++) {
					int[] xy = RadialMapHelper.breakKey(layersPolygon.get(auxvalue).get(i));
					grassGrammar += xy[0] + "-" + xy[1];
					if(i+1<layersPolygon.get(auxvalue).size()){
						grassGrammar += "-";
					}
				}
				landMap.findPoint(layersPolygon.get(auxvalue).get(0)).setGramaticalType(grassGrammar);	
		
		////create Principal Route
		RadialPolygon polygonAux = new RadialPolygon(); //TO FIND CENTROID TO THE PARK
		polygonAux.setPoints(layersPolygon.get(auxvalue));
		polygonAux.setComplete(true);
		
		///
		int referencePoint =  RadialMapHelper.formKey( entryPoint.getX(),entryPoint.getY());
		int centroid =    RadialMapHelper.formKey( polygonAux.getCentroid()[0],polygonAux.getCentroid()[1]);
		int[] pointsInters = polygon.createMainRoute(referencePoint,centroid,layersPolygon.get(0));
		//landMap.createACustomRoute
		//	(pointsInters[0],pointsInters[1],RadialConfiguration.ARTERIAL_BRANCH_SIZE , RadialConfiguration.ARTERIAL_MARK);
		//do vertix branch
		List<Integer> auxList = createVertixBranch(layersPolygon);
	
		/*
		//// PRINT PARK
				List<List<Integer>> grass = polygon.parkZone(
					(valuePlus-1)*valueSeparation+
					(RadialConfiguration.ARTERIAL_BRANCH_SIZE-RadialConfiguration.LOCAL_BRANCH_SIZE));
				for (int j = 0; j < grass.size(); j++) {
					landMap.createBorderFromPolygon(grass.get(j), RadialConfiguration.PARK_MARK);
				}
		
		*/
		//polygon.parkArea((valuePlus-1)*valueSeparation + 16); //to fix
				
		//radialize  		
		Radialize(layersPolygon,pointsInters[0],pointsInters[1]);
		//RadializePrube(layersPolygon,pointsInters[0],pointsInters[1]);	
		///////index of routes
		List<List<RadialLandRoute>> listRoutes = new ArrayList<>();
		RadialLandRoute aux = new RadialLandRoute();
		
		listRoutes = aux.setRadialRoutes(layersPolygon,auxList,pointsInters[0],pointsInters[1]);
		setListRoutes(ListRoutes);
		List<RadialLandRoute> auxRoutes = new ArrayList<>();
		for(int s= 0;s <  listRoutes.size();s++){
			for(int r= 0;r < listRoutes.get(s).size();r++){
				auxRoutes.add(listRoutes.get(s).get(r));
			}
		}
		landMap.setLandRoutes(auxRoutes);

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
				//landMap.createACustomRoute(extLayer.get(k), intLayer.get(i),RadialConfiguration.COLLECTOR_BRANCH_SIZE,RadialConfiguration.COLLECTOR_MARK);
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
						 //landMap.createACustomRoute(extLayer.get(extLayer.size()-1),pointIntersection,RadialConfiguration.COLLECTOR_BRANCH_SIZE , RadialConfiguration.COLLECTOR_MARK);
						 //landMap.createACustomRoute(extLayer.get(i),pointIntersection,RadialConfiguration.COLLECTOR_BRANCH_SIZE , RadialConfiguration.COLLECTOR_MARK);
						 //landMap.createACustomRoute(pointIntersection,intLayer.get(i),RadialConfiguration.COLLECTOR_BRANCH_SIZE , RadialConfiguration.COLLECTOR_MARK);
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
				//landMap.createACustomRoute(extLayer.get(k),pointIntersection,RadialConfiguration.COLLECTOR_BRANCH_SIZE , RadialConfiguration.COLLECTOR_MARK);
				//landMap.createACustomRoute(extLayer.get(k+1),pointIntersection,RadialConfiguration.COLLECTOR_BRANCH_SIZE , RadialConfiguration.COLLECTOR_MARK);
				//landMap.createACustomRoute(pointIntersection,intLayer.get(i),RadialConfiguration.COLLECTOR_BRANCH_SIZE , RadialConfiguration.COLLECTOR_MARK);
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
	
	
	
	private void Radialize(List<List<Integer>> layersPolygon,int pointIni, int pointEnd) {
		
		boolean specialCase = false;
		double areaPolygon = landMap.getPolygonalArea();
		double areaExtra = 5.6*areaPolygon/100;
		int numMaxParks = 0;
		int numMaxZones = 0;
		int numLayersAble = 0;
		int aux =0;
		//do calculus
		for(int k=3;k < (layersPolygon.size() - 3);k=k+3){
			if(k == 3 || k == 12 || k==21) numMaxParks = numMaxParks + layersPolygon.get(k).size();
			else numMaxZones = numMaxZones + layersPolygon.get(k).size();
			numLayersAble++;
		}
		int numTotalMax = numMaxParks + numMaxZones ;
		int spaceHouse = (int)(800/(2*landMap.getConfiguration().getLotConfiguration().getDepthSize())/landMap.getConfiguration().getLotConfiguration().getSideSize());
		spaceHouse++;
		int numAproxZones=0;
		while(numAproxZones==0){
			double zoneArea = spaceHouse*landMap.getConfiguration().getLotConfiguration().getSideSize()*2*landMap.getConfiguration().getLotConfiguration().getDepthSize();
			
			double actualAreaZone = areaExtra;
			for(int k=1;k <= numTotalMax;k++){
				actualAreaZone = actualAreaZone - (zoneArea);
				numAproxZones = k;
				if(actualAreaZone <= 0) break;
			}
			if(actualAreaZone > 0) {
				spaceHouse++ ;
				numAproxZones = 0;
			}
		}
		if(numMaxParks > numAproxZones){
			numMaxParks = (int)(numAproxZones/2);
			numMaxZones	= 	numAproxZones - numMaxParks;
		}else{
			numMaxZones = numAproxZones - numMaxParks;
		}
		
		///begin sectorize
			
		for(int i = 0;i < (layersPolygon.size() - 3); i= i+3){
			
			for(int j = 0;j < layersPolygon.get(i).size();j++){
				
				
				if(specialCase == true && (j == (layersPolygon.get(i+2).size()))){
					specialCase = false;
					continue;
				}
				if(numMaxZones <= 0 && (numMaxParks > 0) && (i!=3 || i!=12 || i!=21)){
					numMaxZones++;
					numMaxParks--;
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
				if(distanceTop < RadialConfiguration.COLLECTOR_BRANCH_SIZE || distanceTop < (distanceDown/2) ){
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
				double area = (landMap.getConfiguration().getLotConfiguration().getDepthSize()*2*((distanceTop-RadialConfiguration.COLLECTOR_BRANCH_SIZE )+ (distanceDown-RadialConfiguration.COLLECTOR_BRANCH_SIZE )))/2;
				if(area < 90 || distanceDown<(6+ RadialConfiguration.COLLECTOR_BRANCH_SIZE) || distanceTop<(6+ RadialConfiguration.COLLECTOR_BRANCH_SIZE )){
					//caso contrario volver area libre 
					cuadrangular(point1Top,point2Top,point2Down,point1Down,RadialConfiguration.CONTRIBUTION_MARK);
					continue;
				}
				//verify main route across
				int aux1Point = landMap.findIntersectionPointIntoTwoStraight(pointIni,pointEnd,point1Top,point2Top,true) ;
				int aux2Point = landMap.findIntersectionPointIntoTwoStraight(pointIni,pointEnd,point1Down,point2Down,true);
				int aux3Point = landMap.findIntersectionPointIntoTwoStraight(pointIni,pointEnd,point1Mid,point2Mid,true);
				
				int fixPoint1Top = landMap.findPointOnStreightToDistance( point1Top, point2Top,(RadialConfiguration.COLLECTOR_BRANCH_SIZE/2)+2);
				int fixPoint2Top = landMap.findPointOnStreightToDistance(point2Top, point1Top, (RadialConfiguration.COLLECTOR_BRANCH_SIZE/2)+2);
				int fixPoint1Mid = landMap.findPointOnStreightToDistance( point1Mid, point2Mid,(RadialConfiguration.COLLECTOR_BRANCH_SIZE/2)+2);
				int fixPoint2Mid = landMap.findPointOnStreightToDistance(point2Mid, point1Mid, (RadialConfiguration.COLLECTOR_BRANCH_SIZE/2)+2);
				
				int fixPoint1Down = landMap.findPointOnStreightToDistance( point1Down, point2Down,(RadialConfiguration.COLLECTOR_BRANCH_SIZE/2)+2);
				int fixPoint2Down = landMap.findPointOnStreightToDistance(point2Down, point1Down, (RadialConfiguration.COLLECTOR_BRANCH_SIZE/2)+2);
				
				if((aux1Point != -1 && aux2Point != -1) || (aux2Point != -1 && i==0)  ){	///would be crossing just one size
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
					
					if(i !=0 && (i==3 || i == 12 || i == 21) && numMaxParks>= 0){
						aux = sectorize(fixPoint1Top,fixAux1Left,fixPoint1Mid,fixAux3Left,fixPoint1Down,fixAux2Left , spaceHouse,RadialConfiguration.PARK_MARK );
						numMaxParks = numMaxParks + aux;
						aux =sectorize(fixAux1Right,fixPoint2Top,fixAux3Right,fixPoint2Mid,fixAux2Right,fixPoint2Down, spaceHouse,RadialConfiguration.PARK_MARK);
						numMaxParks = numMaxParks + aux;
						continue;
					}
					if( i !=0 && numMaxZones >= 0){
						aux = sectorize(fixPoint1Top,fixAux1Left,fixPoint1Mid,fixAux3Left,fixPoint1Down,fixAux2Left , spaceHouse,RadialConfiguration.CONTRIBUTION_MARK );
						numMaxZones = numMaxZones + aux;
						aux =sectorize(fixAux1Right,fixPoint2Top,fixAux3Right,fixPoint2Mid,fixAux2Right,fixPoint2Down, spaceHouse,RadialConfiguration.CONTRIBUTION_MARK);
						numMaxZones = numMaxZones + aux;
						continue;
					}else{
						sectorize(fixPoint1Top,fixAux1Left,fixPoint1Mid,fixAux3Left,fixPoint1Down,fixAux2Left , 0,RadialConfiguration.CONTRIBUTION_MARK );
						sectorize(fixAux1Right,fixPoint2Top,fixAux3Right,fixPoint2Mid,fixAux2Right,fixPoint2Down, 0,RadialConfiguration.CONTRIBUTION_MARK);
						continue;
					}
				}
				
				// do zonification
				if(i !=0 && (i==3 || i == 12 || i == 21) && numMaxParks>= 0){
					aux = sectorize(fixPoint1Top,fixPoint2Top,fixPoint1Mid,fixPoint2Mid,fixPoint1Down,fixPoint2Down,spaceHouse,RadialConfiguration.PARK_MARK);
					numMaxParks = numMaxParks + aux;
					continue;
				}
				if( i !=0 && numMaxZones >= 0){
					aux = sectorize(fixPoint1Top,fixPoint2Top,fixPoint1Mid,fixPoint2Mid,fixPoint1Down,fixPoint2Down,spaceHouse,RadialConfiguration.CONTRIBUTION_MARK);
					numMaxZones = numMaxZones + aux;
					continue;
				}else{
					sectorize(fixPoint1Top,fixPoint2Top,fixPoint1Mid,fixPoint2Mid,fixPoint1Down,fixPoint2Down,0,RadialConfiguration.CONTRIBUTION_MARK);
					continue;
				}
				
				
				
			}
		}
		
	}
	
	

	private int sectorize(int point1Top,int point2Top,int point1Mid,int point2Mid,int point1Down,int point2Down,int spaceHouse, String mask){
		
		int verif = 0; 
		//verify total area again //find distances again
		double  distanceTop = landMap.distanceOfPointToPoint(point1Top,point2Top);
		double  distanceDown = landMap.distanceOfPointToPoint(point1Down,point2Down); 	
		double area = (landMap.getConfiguration().getLotConfiguration().getDepthSize()*2*((distanceTop-RadialConfiguration.COLLECTOR_BRANCH_SIZE )+ (distanceDown-RadialConfiguration.COLLECTOR_BRANCH_SIZE )))/2;
		if(area < 90 || distanceDown < 6 || distanceTop < 6){
			//caso contrario volver area libre 
			//cuadrangular(point1Top,point2Top,point2Down,point1Down,RadialConfiguration.CONTRIBUTION_MARK);
			///print with new graamar
			landMap.findPoint(point1Top).setGramaticalType(
					"z" + "-" + RadialMapHelper.breakKey(point1Top)[0] + "-" + RadialMapHelper.breakKey(point1Top)[1] + "-" +
							RadialMapHelper.breakKey(point2Top)[0]+ "-" + RadialMapHelper.breakKey(point2Top)[1] + "-"
							+ RadialMapHelper.breakKey(point2Down)[0] + "-" + RadialMapHelper.breakKey(point2Down)[1] + "-" 
							+ RadialMapHelper.breakKey(point1Down)[0] + "-" + RadialMapHelper.breakKey(point1Down)[1]);
			
			return verif;
		}
		int modeI =0;
		int modeD =0;
		///////do covert of cuadras
		///print with new graamar
		//cuadrangular(point1Top,point2Top,point2Down,point1Down,RadialConfiguration.ARTERIAL_MARK);
		landMap.findPoint(point1Top).setGramaticalType(
				"o" + "-"  + RadialMapHelper.breakKey(point1Top)[0] + "-" + RadialMapHelper.breakKey(point1Top)[1] + "-" +
						RadialMapHelper.breakKey(point2Top)[0]+ "-" + RadialMapHelper.breakKey(point2Top)[1] + "-"
						+ RadialMapHelper.breakKey(point2Down)[0] + "-" + RadialMapHelper.breakKey(point2Down)[1] + "-" 
						+ RadialMapHelper.breakKey(point1Down)[0] + "-" + RadialMapHelper.breakKey(point1Down)[1]);
		
		//do algorithm with the major  like reference
		//find points to know the polygon form
		int pntLeftUp = landMap.findProyectionPointIntoParalelStraights(point1Top,point2Top,point1Down,true);
		int pntRightUp = landMap.findProyectionPointIntoParalelStraights(point1Top,point2Top,point2Down,true);
		int pntLeftDown = landMap.findProyectionPointIntoParalelStraights(point1Down,point2Down,point1Top,true);
		int pntRightDown = landMap.findProyectionPointIntoParalelStraights(point1Down,point2Down,point2Top,true);
		if((pntLeftUp == -1 && pntLeftDown == -1) || (pntRightUp == -1 && pntRightDown == -1)) return verif;
		
		if(pntLeftUp != -1 &&  pntLeftDown == -1)modeI =1;
		if(pntLeftUp == -1 &&  pntLeftDown != -1)modeI =2;
		if(pntRightUp != -1 &&  pntRightDown == -1)modeD =3;
		if(pntRightUp == -1 &&  pntRightDown != -1)modeD =5;
		
		if(pntLeftUp == -1)pntLeftUp = point1Top;
		if(pntRightUp == -1) pntRightUp = point2Top;
		if(pntLeftDown == -1) pntLeftDown = point1Down;
		if(pntRightDown == -1) pntRightDown = point2Down;
		//find number of division of houses 
		double distanceLarge = landMap.distanceOfPointToPoint(pntLeftUp,pntRightUp);
		int nDivision = (int)(distanceLarge/(landMap.getConfiguration().getLotConfiguration().getSideSize()));
		int  extraDistance= (int)(distanceLarge % (landMap.getConfiguration().getLotConfiguration().getSideSize()));
		int medidor = (int)(nDivision/3)*2 ;
		int part = 0;
		boolean spec = false;
		int ind = 0;
		if(spaceHouse!=0 && (spaceHouse < medidor)){
			spec = true;
			nDivision = nDivision - spaceHouse;
			part = (int)nDivision/2;
			verif = -1;		
		}
		if(modeD ==3 && nDivision%2 == 0) modeD =4;
		if(modeD ==5 && nDivision%2 == 0) modeD =6;
		pulirEsquinas(modeI,modeD,point1Top,point2Top,point1Mid,point2Mid,point1Down,point2Down,extraDistance);
			//print houses
			int initPoint1 = pntLeftUp;
			int aux1Point = landMap.findProyectionPointIntoParalelStraights(point1Mid,point2Mid,initPoint1,true);
			int initPoint2 = aux1Point;
			int aux2Point = landMap.findProyectionPointIntoParalelStraights(point1Mid,point2Mid,pntRightUp,true);
			/////////////////line case
			if(spec == false){
				//landMap.createALine(point1Mid, point2Mid, RadialConfiguration.ARTERIAL_MARK);
				//print grammar
				landMap.findPoint(point1Mid).setGramaticalType(
						"o" + "-"  + RadialMapHelper.breakKey(point1Mid)[0] + "-" + RadialMapHelper.breakKey(point1Mid)[1] + "-" +
								RadialMapHelper.breakKey(point2Mid)[0]+ "-" + RadialMapHelper.breakKey(point2Mid)[1] + "-"
								+ RadialMapHelper.breakKey(point2Mid)[0] + "-" + RadialMapHelper.breakKey(point2Mid)[1] + "-" 
								+ RadialMapHelper.breakKey(point1Mid)[0] + "-" + RadialMapHelper.breakKey(point1Mid)[1]);
			}
			
			for(int i = 0 ;i < nDivision; i++){
				if(spec == true && i == part){
					ind =1;
					//print area
					int pntDwn1 =   landMap.findPointOnStreightToDistance( pntLeftDown, pntRightDown, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i)));
					int pntDwn2 =   landMap.findPointOnStreightToDistance( pntLeftDown, pntRightDown, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i)+(ind*landMap.getConfiguration().getLotConfiguration().getSideSize()*spaceHouse)));
					int aux = 		landMap.findPointOnStreightToDistance( pntLeftUp, pntRightUp, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i) + (ind*landMap.getConfiguration().getLotConfiguration().getSideSize()*spaceHouse)));
					/*
					
					List<Integer> localList;
					RadialPolygon auxPolygon = new RadialPolygon();
					localList = new ArrayList<>();
					localList.add(initPoint1);
					localList.add(aux);
					localList.add(pntDwn2);
					localList.add(pntDwn1);
					localList.add(initPoint1);
					auxPolygon.setMapPoints(localList);
					auxPolygon.setComplete(true);					
					
					landMap.createBorderFromPolygon(localList,mask);
					List<List<Integer>> auxList = auxPolygon.createAreaContribution();
					for (int j = 0; j < auxList.size(); j++) {
						landMap.createBorderFromPolygon(auxList.get(j), mask);
					}
					*/
				////case line
					//cuadrangular(initPoint1,aux,pntDwn2,pntDwn1,mask);
					if(mask == "p"){
						landMap.findPoint(initPoint1).setGramaticalType(
								"g" + "-"  + RadialMapHelper.breakKey(initPoint1)[0] + "-" + RadialMapHelper.breakKey(initPoint1)[1] + "-" +
										RadialMapHelper.breakKey(aux)[0]+ "-" + RadialMapHelper.breakKey(aux)[1] + "-"
										+ RadialMapHelper.breakKey(pntDwn2)[0] + "-" + RadialMapHelper.breakKey(pntDwn2)[1] + "-" 
										+ RadialMapHelper.breakKey(pntDwn1)[0] + "-" + RadialMapHelper.breakKey(pntDwn1)[1]);
					}else{
						landMap.findPoint(initPoint1).setGramaticalType(
								"z" + "-" + RadialMapHelper.breakKey(initPoint1)[0] + "-" + RadialMapHelper.breakKey(initPoint1)[1] + "-" +
										RadialMapHelper.breakKey(aux)[0]+ "-" + RadialMapHelper.breakKey(aux)[1] + "-"
										+ RadialMapHelper.breakKey(pntDwn2)[0] + "-" + RadialMapHelper.breakKey(pntDwn2)[1] + "-" 
										+ RadialMapHelper.breakKey(pntDwn1)[0] + "-" + RadialMapHelper.breakKey(pntDwn1)[1]);
					}
					
					int aux7 = landMap.findProyectionPointIntoParalelStraights(point1Mid,point2Mid,initPoint1,true);
					int aux8 = landMap.findProyectionPointIntoParalelStraights(point1Mid,point2Mid,aux,true);
					//landMap.createALine(point1Mid, aux7, RadialConfiguration.ARTERIAL_MARK);
					//landMap.createALine(aux8, point2Mid, RadialConfiguration.ARTERIAL_MARK);
					/////////print grammar

					landMap.findPoint(point1Mid).setGramaticalType(
							"o" + "-"  + RadialMapHelper.breakKey(point1Mid)[0] + "-" + RadialMapHelper.breakKey(point1Mid)[1] + "-" +
									RadialMapHelper.breakKey(aux7)[0]+ "-" + RadialMapHelper.breakKey(aux7)[1] + "-"
									+ RadialMapHelper.breakKey(aux7)[0] + "-" + RadialMapHelper.breakKey(aux7)[1] + "-" 
									+ RadialMapHelper.breakKey(point1Mid)[0] + "-" + RadialMapHelper.breakKey(point1Mid)[1]);
					landMap.findPoint(aux8).setGramaticalType(
							"o" + "-"  + RadialMapHelper.breakKey(aux8)[0] + "-" + RadialMapHelper.breakKey(aux8)[1] + "-" +
									RadialMapHelper.breakKey(point2Mid)[0]+ "-" + RadialMapHelper.breakKey(point2Mid)[1] + "-"
									+ RadialMapHelper.breakKey(point2Mid)[0] + "-" + RadialMapHelper.breakKey(point2Mid)[1] + "-" 
									+ RadialMapHelper.breakKey(aux8)[0] + "-" + RadialMapHelper.breakKey(aux8)[1]);
			
					////
					///////////otra manera d generar
					//int aux3 = landMap.findPointOnStreightToDistance( pntLeftUp, pntRightUp, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i) + (int)(ind*landMap.getConfiguration().getLotConfiguration().getSideSize()*spaceHouse/2)));
					//int aux4 = landMap.findPointOnStreightToDistance( pntLeftDown, pntRightDown, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i)+(int)(ind*landMap.getConfiguration().getLotConfiguration().getSideSize()*spaceHouse/2)));
					//landMap.createACustomRoute(aux3,aux4,landMap.getConfiguration().getLotConfiguration().getSideSize()*spaceHouse,mask);
					////
					initPoint1 = aux;
					int aux2 = landMap.findPointOnStreightToDistance( aux1Point, aux2Point, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i) + (ind*landMap.getConfiguration().getLotConfiguration().getSideSize()*spaceHouse)));
					initPoint2 = aux2;
					
				}
				///////////////line case
				generateLineHouse(initPoint1,pntRightUp,pntLeftDown,pntRightDown,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.ARTERIAL_MARK);
				int aux = landMap.findPointOnStreightToDistance( pntLeftUp, pntRightUp, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i+1) + (ind*landMap.getConfiguration().getLotConfiguration().getSideSize()*spaceHouse)));
				initPoint1 = aux;
				int aux2 = landMap.findPointOnStreightToDistance( aux1Point, aux2Point, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i+1) + (ind*landMap.getConfiguration().getLotConfiguration().getSideSize()*spaceHouse)));
				initPoint2 = aux2;
				
				//////////////////////
				/*	
				if((i%2) == 0){
					generateHouse(initPoint1,pntRightUp,aux1Point,aux2Point,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.MARK_LOT1);
					int aux = landMap.findPointOnStreightToDistance( pntLeftUp, pntRightUp, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i+1) + (ind*landMap.getConfiguration().getLotConfiguration().getSideSize()*spaceHouse)));
					initPoint1 = aux;
				
					generateHouse(initPoint2,aux2Point,pntLeftDown,pntRightDown,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.MARK_LOT2);
					int aux2 = landMap.findPointOnStreightToDistance( aux1Point, aux2Point, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i+1) + (ind*landMap.getConfiguration().getLotConfiguration().getSideSize()*spaceHouse)));
					initPoint2 = aux2;
				
		
				}else
				if((i%2) == 1){
					generateHouse(initPoint1,pntRightUp,aux1Point,aux2Point,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.MARK_LOT2);
					int aux = landMap.findPointOnStreightToDistance( pntLeftUp, pntRightUp, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i+1) + (ind*landMap.getConfiguration().getLotConfiguration().getSideSize()*spaceHouse)));
					initPoint1 = aux;
			
					generateHouse(initPoint2,aux2Point,pntLeftDown,pntRightDown,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.MARK_LOT1);
					int aux2 = landMap.findPointOnStreightToDistance( aux1Point, aux2Point, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i+1) + (ind*landMap.getConfiguration().getLotConfiguration().getSideSize()*spaceHouse)));
					initPoint2 = aux2;				
				}
				*/
			}
			return verif ;
			//analize special cases
	}
	
	private int generateLineHouse(int pnt1Up,int pnt2Up, int pnt1Dwn, int pnt2Dwn,int dist,String mask ){
		int pointSolution =-1; 
		
		int aux = landMap.findPointOnStreightToDistance( pnt1Up, pnt2Up, dist);
		int opuestAux = landMap.findProyectionPointIntoParalelStraights( pnt1Dwn, pnt2Dwn,aux,false);
		//landMap.createALine(aux, opuestAux, mask);
		landMap.findPoint(aux).setGramaticalType(
				"l" + "-"  + RadialMapHelper.breakKey(aux)[0] + "-" + RadialMapHelper.breakKey(aux)[1] + "-" +
						RadialMapHelper.breakKey(opuestAux)[0]+ "-" + RadialMapHelper.breakKey(opuestAux)[1] + "-"
						+ RadialMapHelper.breakKey(opuestAux)[0] + "-" + RadialMapHelper.breakKey(opuestAux)[1] + "-" 
						+ RadialMapHelper.breakKey(aux)[0] + "-" + RadialMapHelper.breakKey(aux)[1]);
		
		pointSolution=aux;
		return pointSolution;
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
				/////
				pointSolution =  RadialMapHelper.formKey(xyRec1End[0],(xyRec1Ini[1] + (int)dist));		
			}else{
				for(int i=0;i< dist;i++){
					int auxPoint1 = RadialMapHelper.formKey(xyRec1End[0],(xyRec1Ini[1] - i));
					int auxPoint2 = landMap.findProyectionPointIntoParalelStraights(pnt1Dwn,pnt2Dwn,auxPoint1,false);
					landMap.createALine( auxPoint1,auxPoint2,mask);
				}
				/////
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
						landMap.createALine( pointSolution,auxPoint,mask);
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
						landMap.createALine( pointSolution,auxPoint,mask);
						return pointSolution;
					}
				}
			}	
		}
		return pointSolution;
	}
	
	private void pulirEsquinas(int modeI,int modeD,int point1Top,int point2Top,int point1Mid,int point2Mid,int point1Down,int point2Down,int exced){
		if(modeI==1){
			int pntTopRight = landMap.findProyectionPointIntoParalelStraights(point1Top,point2Top,point1Down,true);
			int pntMidAux = landMap.findProyectionPointIntoParalelStraights(point1Down,pntTopRight,point1Mid,true);
			double auxDist = landMap.distanceOfPointToPoint(pntMidAux,point1Mid);
			int numHouses = (int)(auxDist/landMap.getConfiguration().getLotConfiguration().getSideSize());
			int  extraDistance= (int)(auxDist % (landMap.getConfiguration().getLotConfiguration().getSideSize()));
			if(numHouses != 0){
				int initPoint1 = pntTopRight;
				/// case line
				initPoint1 = landMap.findPointOnStreightToDistance( pntTopRight, point2Top, (landMap.getConfiguration().getLotConfiguration().getSideSize()));
				int aux1Point = landMap.findProyectionPointIntoParalelStraights(point2Mid,point1Mid,initPoint1,true);
				///
				
				for(int i = 0 ;i < numHouses; i++){
					//////////case line
					
					generateLineHouse(initPoint1,point1Top,aux1Point,point1Mid,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.ARTERIAL_MARK);
					int aux = landMap.findPointOnStreightToDistance( pntTopRight, point1Top, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i)));
					initPoint1 = aux;
					/*///
					int aux1Point = landMap.findProyectionPointIntoParalelStraights(point1Mid,point2Mid,initPoint1,true);
					if((i%2) == 0){
						generateHouse(initPoint1,point1Top,aux1Point,point1Mid,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.MARK_LOT2);
						int aux = landMap.findPointOnStreightToDistance( pntTopRight, point1Top, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i)));
						initPoint1 = aux;					
					}else
					if((i%2) == 1){
						generateHouse(initPoint1,point1Top,aux1Point,point1Mid,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.MARK_LOT1);
						int aux = landMap.findPointOnStreightToDistance( pntTopRight, point1Top, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i)));
						initPoint1 = aux;					
					}*/
				}
				/*
				if(extraDistance!=0){
					 int aux5 = landMap.findProyectionPointIntoParalelStraights(point1Mid,point2Mid,initPoint1,false);
		 			  if(numHouses%2 == 0){
					    	cuadrangular(initPoint1,aux5,point1Mid,point1Top,RadialConfiguration.MARK_LOT1);
					  } else  if(numHouses%2 == 1){
					    	cuadrangular(initPoint1,aux5,point1Mid,point1Top,RadialConfiguration.MARK_LOT2);
					  }
				}else{
						if(numHouses%2 == 0){
							 triangular(initPoint1,point1Mid,point1Top,RadialConfiguration.MARK_LOT1);
						} else  if(numHouses%2 == 1){
						    triangular(initPoint1,point1Mid,point1Top,RadialConfiguration.MARK_LOT2);
						}	
					}
				*/
			}else{//all black
				//cuadrangular(pntTopRight,pntMidAux,point1Mid,point1Top,RadialConfiguration.MARK_LOT1);
			}
			//triangular(point1Down,point1Mid,pntMidAux,RadialConfiguration.MARK_LOT2);
		}
		if(modeI==2){
			int pntDownRight = landMap.findProyectionPointIntoParalelStraights(point1Down,point2Down,point1Top,true);
			int pntMidAux = landMap.findProyectionPointIntoParalelStraights(pntDownRight,point1Top,point1Mid,true);
			double auxDist = landMap.distanceOfPointToPoint(pntMidAux,point1Mid);
			int numHouses = (int)(auxDist/landMap.getConfiguration().getLotConfiguration().getSideSize());
			int  extraDistance= (int)(auxDist % (landMap.getConfiguration().getLotConfiguration().getSideSize()));
			if(numHouses != 0){
				int initPoint1 = pntMidAux;
				/////case line
				initPoint1 = landMap.findPointOnStreightToDistance( pntMidAux, point2Mid, (landMap.getConfiguration().getLotConfiguration().getSideSize()));
				////
				for(int i = 0 ;i < numHouses; i++){
					////////case line
					generateLineHouse(initPoint1,point1Mid,pntDownRight,point1Down,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.ARTERIAL_MARK);
					int aux = landMap.findPointOnStreightToDistance( pntMidAux, point1Mid, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i)));
					initPoint1 = aux;
					/*
					//int aux1Point = landMap.findProyectionPointIntoParalelStraights(point1Mid,point2Mid,initPoint1,true);
					if((i%2) == 0){
						generateHouse(initPoint1,point1Mid,pntDownRight,point1Down,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.MARK_LOT1);
						int aux = landMap.findPointOnStreightToDistance( pntMidAux, point1Mid, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i)));
						initPoint1 = aux;					
					}else
					if((i%2) == 1){
						generateHouse(initPoint1,point1Mid,pntDownRight,point1Down,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.MARK_LOT2);
						int aux = landMap.findPointOnStreightToDistance( pntMidAux, point1Mid, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i)));
						initPoint1 = aux;		
					}
					*/
				}
				/*
				if(extraDistance!=0){
					int aux5 = landMap.findProyectionPointIntoParalelStraights(point1Down,point2Down,initPoint1,false);
		 			if(numHouses%2 == 0){
					    cuadrangular(initPoint1,aux5,point1Down,point1Mid,RadialConfiguration.MARK_LOT2);
					} else  if(numHouses%2 == 1){
					    cuadrangular(initPoint1,aux5,point1Down,point1Mid,RadialConfiguration.MARK_LOT1);
					}
				}else{
					int aux5 = landMap.findProyectionPointIntoParalelStraights(point1Down,point2Down,point1Mid,false);
					if(numHouses%2 == 0){
						triangular(point1Mid,aux5,point1Down,RadialConfiguration.MARK_LOT2);
					} else  if(numHouses%2 == 1){
						triangular(point1Mid,aux5,point1Down,RadialConfiguration.MARK_LOT1);
					}	
					
				}
				*/
			}else{//all white
				//cuadrangular(pntDownRight,pntMidAux,point1Mid,point1Down,RadialConfiguration.MARK_LOT2);
			}
			//triangular(point1Top,point1Mid,pntMidAux,RadialConfiguration.MARK_LOT1);
			
		}
		if(modeD==3 || modeD==4){
			
			int pntUpRef = landMap.findProyectionPointIntoParalelStraights(point2Top,point1Top,point2Down,true);
			int pntUpLeft = pntUpRef;
			if(exced != 0)
				pntUpLeft = landMap.findPointOnStreightToDistance( pntUpRef, point1Top, exced);
			int pntDownLeft = landMap.findProyectionPointIntoParalelStraights(point2Down,point1Down,pntUpLeft,false);
			int pntMidAux = landMap.findProyectionPointIntoParalelStraights(pntUpLeft,pntDownLeft,point2Mid,true);
			double auxDist = landMap.distanceOfPointToPoint(pntMidAux,point2Mid);
			int numHouses = (int)(auxDist/landMap.getConfiguration().getLotConfiguration().getSideSize());
			int  extraDistance= (int)(auxDist % (landMap.getConfiguration().getLotConfiguration().getSideSize()));
			if(numHouses != 0 ){
				int initPoint1 = pntUpLeft;
				for(int i = 0 ;i < numHouses; i++){
					////////case line
					generateLineHouse(initPoint1,point2Top,pntMidAux,point2Mid,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.ARTERIAL_MARK);
					int aux = landMap.findPointOnStreightToDistance( pntUpLeft, point2Top, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i)));
					initPoint1 = aux;
					/*
					if((i%2) == 0){
						if(modeD==3)
							generateHouse(initPoint1,point2Top,pntMidAux,point2Mid,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.MARK_LOT2);
						if(modeD==4)
							generateHouse(initPoint1,point2Top,pntMidAux,point2Mid,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.MARK_LOT1);
						int aux = landMap.findPointOnStreightToDistance( pntUpLeft, point2Top, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i)));
						initPoint1 = aux;					
					}else
					if((i%2) == 1){
						if(modeD==3)
							generateHouse(initPoint1,point2Top,pntMidAux,point2Mid,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.MARK_LOT1);
						if(modeD==4)
							generateHouse(initPoint1,point2Top,pntMidAux,point2Mid,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.MARK_LOT2);
						int aux = landMap.findPointOnStreightToDistance( pntUpLeft, point2Top, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i)));
						initPoint1 = aux;					
					}
					*/
					
				}
				/*
					if(extraDistance!=0){
					    int aux5 = landMap.findProyectionPointIntoParalelStraights(point2Mid,point1Mid,initPoint1,false);
					    int aux6 = landMap.findProyectionPointIntoParalelStraights(point2Top,point1Top,point2Mid,false);
		 			    if(numHouses%2 == 0){
		 			    	if(modeD==3)
		 			    		cuadrangular(initPoint1,aux5,point2Mid,aux6,RadialConfiguration.MARK_LOT1);
		 			    	if(modeD==4)
		 			    		cuadrangular(initPoint1,aux5,point2Mid,aux6,RadialConfiguration.MARK_LOT2);
					    } else if(numHouses%2 == 1){
					    	if(modeD==3)
					    		cuadrangular(initPoint1,aux5,point2Mid,aux6,RadialConfiguration.MARK_LOT2);
					    	if(modeD==4)
					    		cuadrangular(initPoint1,aux5,point2Mid,aux6,RadialConfiguration.MARK_LOT1);
					    }
					}
					{
						 int aux5 = landMap.findProyectionPointIntoParalelStraights(point2Top,point1Top,point2Mid,false);
						 if(numHouses%2 == 0){
							  if(modeD==3)
								  triangular(point2Mid,aux5,point2Top,RadialConfiguration.MARK_LOT1);
							  if(modeD==4)
								  triangular(point2Mid,aux5,point2Top,RadialConfiguration.MARK_LOT2);
						  }else  if(numHouses%2 == 1){
							  if(modeD==3)
								  triangular(point2Mid,aux5,point2Top,RadialConfiguration.MARK_LOT2);
							  if(modeD==4)
								  triangular(point2Mid,aux5,point2Top,RadialConfiguration.MARK_LOT1);
						  }	
					}
					*/
			}
			/*
			else{
				if(exced!=0){
					if(modeD==3)
						cuadrangular(pntUpLeft,pntMidAux,point2Mid,point2Top,RadialConfiguration.MARK_LOT2);
					if(modeD==4)
						cuadrangular(pntUpLeft,pntMidAux,point2Mid,point2Top,RadialConfiguration.MARK_LOT1);
					
				}else
				if(exced==0){
					int aux6 = landMap.findProyectionPointIntoParalelStraights(point2Top,point1Top,point2Mid,false);
					if(modeD==3)
						triangular(point2Mid,aux6,point2Top,RadialConfiguration.MARK_LOT2);
					if(modeD==4)
						triangular(point2Mid,aux6,point2Top,RadialConfiguration.MARK_LOT1);
					
					
				}
			}
			/*
			if(exced!=0){
				if(modeD==3)
					cuadrangular(pntDownLeft,pntMidAux,point2Mid,point2Down,RadialConfiguration.MARK_LOT2);
				if(modeD==4)
					cuadrangular(pntDownLeft,pntMidAux,point2Mid,point2Down,RadialConfiguration.MARK_LOT1);
				
			}else
			if(exced==0){				
				int aux6 = landMap.findProyectionPointIntoParalelStraights(point2Mid,point1Mid,point2Down,false);
				if(modeD==3)
					triangular(aux6,point2Mid,point2Down,RadialConfiguration.MARK_LOT2);
				if(modeD==4)
					triangular(aux6,point2Mid,point2Down,RadialConfiguration.MARK_LOT1);	
			}
			*/
			
		}
		if(modeD==5|| modeD==6){
			int pntDownRef = landMap.findProyectionPointIntoParalelStraights(point2Down,point1Down,point2Top,true);
			int pntDownLeft = pntDownRef;
			if(exced != 0)
				pntDownLeft = landMap.findPointOnStreightToDistance( pntDownRef, point1Down, exced);
			int pntUpLeft = landMap.findProyectionPointIntoParalelStraights(point2Top,point1Top,pntDownLeft,false);
			int pntMidAux = landMap.findProyectionPointIntoParalelStraights(pntUpLeft,pntDownLeft,point2Mid,true);
			double auxDist = landMap.distanceOfPointToPoint(pntMidAux,point2Mid);
			int numHouses = (int)(auxDist/landMap.getConfiguration().getLotConfiguration().getSideSize());
			int  extraDistance= (int)(auxDist % (landMap.getConfiguration().getLotConfiguration().getSideSize()));
			if(numHouses != 0 ){
				int initPoint1 = pntMidAux;
				for(int i = 0 ;i < numHouses; i++){
					generateLineHouse(initPoint1,point2Mid,pntDownLeft,point2Down,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.MARK_LOT1);
					int aux = landMap.findPointOnStreightToDistance( pntMidAux, point2Mid, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i)));
					initPoint1 = aux;
					
					/*
					if((i%2) == 0){
						if(modeD==5)
							generateHouse(initPoint1,point2Mid,pntDownLeft,point2Down,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.MARK_LOT2);
						if(modeD==6)
							generateHouse(initPoint1,point2Mid,pntDownLeft,point2Down,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.MARK_LOT1);
						int aux = landMap.findPointOnStreightToDistance( pntMidAux, point2Mid, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i)));
						initPoint1 = aux;					
					}else
					if((i%2) == 1){
						if(modeD==5)
							generateHouse(initPoint1,point2Mid,pntDownLeft,point2Down,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.MARK_LOT1);
						if(modeD==6)
							generateHouse(initPoint1,point2Mid,pntDownLeft,point2Down,landMap.getConfiguration().getLotConfiguration().getSideSize(),RadialConfiguration.MARK_LOT2);
						int aux = landMap.findPointOnStreightToDistance( pntMidAux, point2Mid, (landMap.getConfiguration().getLotConfiguration().getSideSize()*(i)));
						initPoint1 = aux;					
					}
					*/
				}
				/*
					if(extraDistance!=0){
					    int aux5 = landMap.findProyectionPointIntoParalelStraights(point2Down,point1Down,initPoint1,false);
					    int aux6 = landMap.findProyectionPointIntoParalelStraights(point2Down,point1Down,point2Mid,false);
		 			    if(numHouses%2 == 0){
		 			    	if(modeD==5)
		 			    		cuadrangular(initPoint1,aux5,aux6,point2Mid,RadialConfiguration.MARK_LOT1);
		 			    	if(modeD==6)
		 			    		cuadrangular(initPoint1,aux5,aux6,point2Mid,RadialConfiguration.MARK_LOT2);
					    } else if(numHouses%2 == 1){
					    	if(modeD==5)
					    		cuadrangular(initPoint1,aux5,aux6,point2Mid,RadialConfiguration.MARK_LOT2);
					    	if(modeD==6)
					    		cuadrangular(initPoint1,aux5,aux6,point2Mid,RadialConfiguration.MARK_LOT1);
					    }
					}
					{
						 int aux5 = landMap.findProyectionPointIntoParalelStraights(point2Down,point1Down,point2Mid,false);
						 if(numHouses%2 == 0){
							  if(modeD==5)
								  triangular(point2Mid,aux5,point2Down,RadialConfiguration.MARK_LOT1);
							  if(modeD==6)
								  triangular(point2Mid,aux5,point2Down,RadialConfiguration.MARK_LOT2);
						  }else  if(numHouses%2 == 1){
							  if(modeD==5)
								  triangular(point2Mid,aux5,point2Down,RadialConfiguration.MARK_LOT2);
							  if(modeD==6)
								  triangular(point2Mid,aux5,point2Down,RadialConfiguration.MARK_LOT1);
						  }	
					}
					*/
			}
			/*
			  else{
			 
				if(exced!=0){
					if(modeD==5)
						cuadrangular(pntMidAux,pntDownLeft,point2Down,point2Mid,RadialConfiguration.MARK_LOT2);
					if(modeD==6)
						cuadrangular(pntMidAux,pntDownLeft,point2Down,point2Mid,RadialConfiguration.MARK_LOT1);
		
					
				}else
				if(exced==0){
					int aux6 = landMap.findProyectionPointIntoParalelStraights(point2Down,point1Down,point2Mid,false);
					if(modeD==5)
						triangular(point2Mid,aux6,point2Down,RadialConfiguration.MARK_LOT2);
					if(modeD==6)
						triangular(point2Mid,aux6,point2Down,RadialConfiguration.MARK_LOT1);
					
					
				}
			}
			if(exced!=0){
				if(modeD==5)
					cuadrangular(pntUpLeft,pntMidAux,point2Mid,point2Top,RadialConfiguration.MARK_LOT2);
				if(modeD==6)
					cuadrangular(pntUpLeft,pntMidAux,point2Mid,point2Top,RadialConfiguration.MARK_LOT2);
				
			}else
			if(exced==0){				
				int aux6 = landMap.findProyectionPointIntoParalelStraights(point2Mid,point1Mid,point2Top,false);
				if(modeD==5)
					triangular(aux6,point2Mid,point2Top,RadialConfiguration.MARK_LOT2);
				if(modeD==6)
					triangular(aux6,point2Mid,point2Top,RadialConfiguration.MARK_LOT1);	
			}
			
		*/		
		}

	}
	
	public void cuadrangular(int pnt1,int pnt2,int pnt3,int pnt4,String mask){
	
		List<Integer> localList;
		RadialPolygon auxPolygon = new RadialPolygon();
		localList = new ArrayList<>();
		localList.add(pnt1);
		localList.add(pnt2);
		localList.add(pnt3);
		localList.add(pnt4);
		localList.add(pnt1);
		auxPolygon.setMapPoints(localList);
		auxPolygon.setComplete(true);	
		//verif if line donnt across
		
		int aux1 = landMap.findIntersectionPointIntoTwoStraight(pnt1,pnt2,pnt4,pnt3,true) ;
		double dis1 = landMap.distanceOfPointToPoint(pnt1,pnt2);
		double dis2 = landMap.distanceOfPointToPoint(pnt2,pnt3);
		double dis3 = landMap.distanceOfPointToPoint(pnt3,pnt4);
		double dis4 = landMap.distanceOfPointToPoint(pnt4,pnt1);
		
		if(aux1 !=-1)return;
		if(dis1 <= 1 || dis2 <=1 || dis3 <= 1 || dis4 <=1 ){
			if(dis1 <= 1 ) triangular(pnt1,pnt3,pnt4,mask);
			if(dis2 <= 1 ) triangular(pnt1,pnt2,pnt4,mask);
			if(dis3 <= 1 ) triangular(pnt1,pnt2,pnt4,mask);
			if(dis4 <= 1 ) triangular(pnt1,pnt2,pnt3,mask);
			return;
		}
		landMap.createBorderFromPolygon(localList,mask);
		//List<List<Integer>> auxList = auxPolygon.createAreaContribution();
		//for (int j = 0; j < auxList.size(); j++) {
		//	landMap.createBorderFromPolygon(auxList.get(j), mask);
		//}
	}
	public void triangular(int pnt1,int pnt2, int pnt3,String mask){
		//landMap.printTriangular(pnt1, pnt2, pnt3, mask);
	}

}
