package models.radialVariation;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import helpers.radialVariation.RadialDirectionHelper;
import helpers.radialVariation.RadialMapHelper;
import interfaces.radialVariation.RadialConfiguration;
import interfaces.radialVariation.RadialConstants;
import models.configuration.ConfigurationEntry;


public class RadialLandMap {
	private ConfigurationEntry configuration;

	public void setConfiguration(ConfigurationEntry configurationEntry) {
		this.configuration = configurationEntry;
	}

	public ConfigurationEntry getConfiguration() {
		return configuration;
	}
	private int pointsx = -1;
	private int pointsy = -1;

	private RadialLandPoint centroid;

	private Map<Integer, RadialLandPoint> map;

	private List<RadialLandRoute> landRoutes = new ArrayList<>();
	private List<Integer> nodes = new ArrayList<>();
	List<List<Integer>> fullPolygon;
	private List<RadialLandPoint> polygonNodes;
	private double polygonalArea;

	public RadialLandMap(int pointsx, int pointsy) {
		this.setPointsx(++pointsx);
		this.setPointsy(++pointsy);

		map = new HashMap<>();
		for (int i = 0; i < pointsx; i++) {
			for (int j = 0; j < pointsy; j++) {
				map.put(RadialMapHelper.formKey(i, j), new RadialLandPoint(i, j));
			}
		}
	}

	public int getPointsx() {
		return pointsx;
	}

	public void setPointsx(int pointsx) {
		this.pointsx = pointsx;
	}

	public int getPointsy() {
		return pointsy;
	}

	public void setPointsy(int pointsy) {
		this.pointsy = pointsy;
	}

	public RadialLandPoint getLandPoint(int pointId) {
		return map.get(pointId);
	}

	public RadialLandPoint getCentroid() {
		return centroid;
	}

	public void setCentroid(RadialLandPoint centroid) {
		this.centroid = centroid;
	}

	public RadialLandPoint findPoint(int entryPointId) {
		return map.get(entryPointId);
	}

	public List<RadialLandRoute> getLandRoutes() {
		return landRoutes;
	}
	
	public void setLandRoutes(List<RadialLandRoute> landRoutes) {
		this.landRoutes = landRoutes;
	}
	
	/**
	 * This method marks all points that are not inside the polygon border as
	 * restricted area. This must be an ordered set of consecutive points (after
	 * all the input from android looks like that.
	 */
	public void createBorderFromPolygon(List<RadialLandPoint> polygon) {

		///evade sizes errors, when is possible to out of range
		for(int j =0 ;j < polygon.size();j++ ){
			int valX = polygon.get(j).getX();
			int valY = polygon.get(j).getY();
			int aux = (int)(RadialConfiguration.ARTERIAL_BRANCH_SIZE/2) ;
			if(valX < aux){
				polygon.get(j).setX(aux);
			}
			if(valX > (getPointsx() - aux)){
				polygon.get(j).setX(getPointsx() - aux);
			}
			if(valY < aux){
				polygon.get(j).setY(aux);
			}
			if(valY > (getPointsy() - aux)){
				polygon.get(j).setY(getPointsy() - aux);
			}
		}
		
		setPolygonNodes(polygon);
		fullPolygon = new ArrayList<>();
		// first we create the border
		for (int i = 0, j = 1; j < polygon.size(); i++, j++) {
			List<Integer> truePolygon = new ArrayList<>();
			int underscore = (polygon.get(j).getX() - polygon.get(i).getX());
			// there are three gradient cases.
			// 1st UNDEFINED = (get(j).getX()-get(i).getX()); straight Y axis
			if (underscore == 0) {
				int lower = polygon.get(i).getY() < polygon.get(j).getY() ? polygon.get(i).getY()
						: polygon.get(j).getY();
				int upper = polygon.get(i).getY() > polygon.get(j).getY() ? polygon.get(i).getY()
						: polygon.get(j).getY();

				for (int w = lower; w <= upper; w++) {
					getLandPoint(RadialMapHelper.formKey(polygon.get(i).getX(), w))
							.setType(RadialConstants.POLYGON_LIMIT);
					truePolygon.add(RadialMapHelper.formKey(polygon.get(i).getX(), w));
				}
				continue;
			}

			double gradient = (polygon.get(j).getY() - polygon.get(i).getY()) * 1.0 / underscore;
			// 2nd, gradient=0; straight in the X axis
			int lower = polygon.get(i).getX() < polygon.get(j).getX() ? polygon.get(i).getX() : polygon.get(j).getX();
			int upper = polygon.get(i).getX() > polygon.get(j).getX() ? polygon.get(i).getX() : polygon.get(j).getX();
			if (gradient == 0) {
				for (int w = lower; w <= upper; w++) {
					getLandPoint(RadialMapHelper.formKey(w, polygon.get(i).getY()))
							.setType(RadialConstants.POLYGON_LIMIT);
					truePolygon.add(RadialMapHelper.formKey(w, polygon.get(i).getY()));
				}
				continue;
			}

			double b = polygon.get(j).getY() - gradient * polygon.get(j).getX();
			// 3nd the gradient is positive/negative.
			for (int w = lower; w <= upper; w++) {
				float y = RadialMapHelper.round(gradient * w + b);
				if (y == (int) y) // quick and dirty convertion check
				{
					getLandPoint(RadialMapHelper.formKey(w, (int) y)).setType(RadialConstants.POLYGON_LIMIT);
					truePolygon.add(RadialMapHelper.formKey(w, (int) y));
				}
			}
			fullPolygon.add(truePolygon);
		}

		// we fill everything outside of it with Xs
		//fillPolygonalArea();
		findPolygonalArea(polygon);
		findCentroid(polygon);
		clearDottedLimits();
		updatePolygonLimits();
	}

	private void updatePolygonLimits() {
		for (int x = 0; x < fullPolygon.size(); x++) {
			List<Integer> polygonRow = new ArrayList<>();
			for (int y = 0; y < fullPolygon.get(x).size() - 1; y++) {
				RadialLandPoint initialLandPoint = getLandPoint(fullPolygon.get(x).get(y));
				polygonRow.add(fullPolygon.get(x).get(y));
				RadialLandPoint finalLandPoint = getLandPoint(fullPolygon.get(x).get(y + 1));

				int wi = initialLandPoint.getX();
				int wf = finalLandPoint.getX();

				if (initialLandPoint.getY() > finalLandPoint.getY()) {
					for (int z = initialLandPoint.getY() - 1; z >= finalLandPoint.getY() + 1; z--) {
						if (wi != wf) {
							if (isPolygonBorder(wi, z)) {
								polygonRow.add(RadialMapHelper.formKey(wi, z));
							}
						}

						if (isPolygonBorder(wf, z)) {
							polygonRow.add(RadialMapHelper.formKey(wf, z));
						}
					}
				} else {
					for (int z = initialLandPoint.getY() + 1; z <= finalLandPoint.getY() - 1; z++) {
						if (wi != wf) {
							if (isPolygonBorder(wi, z)) {
								polygonRow.add(RadialMapHelper.formKey(wi, z));
							}
						}
						if (isPolygonBorder(wf, z)) {
							polygonRow.add(RadialMapHelper.formKey(wf, z));
						}
					}
				}
			}
			polygonRow.add(fullPolygon.get(x).get(fullPolygon.get(x).size() - 1));
			fullPolygon.set(x, polygonRow);
		}
		fullPolygon = null;
	}

	private boolean isPolygonBorder(int x, int y) {
		int easternLimit = x + 1;
		int westernLimit = x - 1;
		int southLimit = y - 1;
		int northLimit = y + 1;

		if (westernLimit == -1 || easternLimit == pointsx || southLimit == -1 || northLimit == pointsy)
			return false;
		return (findPoint(RadialMapHelper.formKey(x - 1, y)).getType().equals(RadialConfiguration.OUTSIDE_POLYGON_MARK)
				|| findPoint(RadialMapHelper.formKey(x + 1, y)).getType().equals(RadialConfiguration.OUTSIDE_POLYGON_MARK))
				&& !findPoint(RadialMapHelper.formKey(x, y)).getType().equals(RadialConfiguration.OUTSIDE_POLYGON_MARK);
	}

	public void fillPolygonalArea() {
		for (int x = 0; x < pointsx; x++) {
			int count = 0;
			int pInitialLimit = -1;
			boolean reversed = false;

			for (int y = 0; y < pointsy; y++) {
				if (getLandPoint(RadialMapHelper.formKey(x, y)).getType() == RadialConstants.POLYGON_LIMIT) {
					count++;
					pInitialLimit = pInitialLimit == -1 ? y : pInitialLimit;
				}

				switch (count) {
				case 0:
					getLandPoint(RadialMapHelper.formKey(x, y)).setType(RadialConfiguration.OUTSIDE_POLYGON_MARK);
					break;
				case 2:
					if (!reversed) {
						for (int w = pInitialLimit + 1; w < y; w++) {
							getLandPoint(RadialMapHelper.formKey(x, w)).setType(RadialConfiguration.EMPTY_MARK);
						}
						reversed = true;
					} else {
						getLandPoint(RadialMapHelper.formKey(x, y)).setType(RadialConfiguration.OUTSIDE_POLYGON_MARK);
					}
					break;
				}
			}
		}
	}

	// Variation for the creation of zones
	public void createBorderFromPolygon(List<Integer> polygon, String markType) {
		int variation=0;
		for(int j =0 ;j < polygon.size();j++ ){
			int xyPoint[] = RadialMapHelper.breakKey(polygon.get(j));
			int aux = (int)(RadialConfiguration.ARTERIAL_BRANCH_SIZE/2);
			if(xyPoint[0] < aux){
				xyPoint[0]= aux;
				variation=1;
			}
			if(xyPoint[0] > (getPointsx() - aux)){
				xyPoint[0]= getPointsx() - aux;
				variation=1;
			}
			if(xyPoint[1] < aux){
				xyPoint[1]= aux;
				variation=1;
			}
			if(xyPoint[1] > (getPointsy() - aux)){
				xyPoint[1] = getPointsy() - aux;
				variation=1;
			}
			
			
			if(variation==1){
				List<Integer> listAux = new ArrayList<>();
				for(int k=0;k<polygon.size();k++){
					if(k==j){
						int auxValue= RadialMapHelper.formKey(xyPoint[0],xyPoint[1]);
						listAux.add(auxValue);
					}else{
						listAux.add(polygon.get(j));
					}
				}
				variation=0;
				polygon=listAux;
			}
		}
		
		/////////////
		for (int i = 0; i < polygon.size(); i++) {
			int xyInitial[] = RadialMapHelper.breakKey(polygon.get(i));
			int xyFinal[] = RadialMapHelper.breakKey(polygon.get((i + 1) % polygon.size()));

			int underscore = (xyFinal[0] - xyInitial[0]);
			if (underscore == 0) {
				int lower = xyInitial[1] < xyFinal[1] ? xyInitial[1] : xyFinal[1];
				int upper = xyInitial[1] > xyFinal[1] ? xyInitial[1] : xyFinal[1];

				for (int w = lower; w <= upper; w++) {
					getLandPoint(RadialMapHelper.formKey(xyInitial[0], w)).setType(markType);
				}
				continue;
			}

			double gradient = (xyFinal[1] - xyInitial[1]) * 1.0 / underscore;
			// 2nd, gradient=0; straight in the X axis
			int lowerx = xyInitial[0] < xyFinal[0] ? xyInitial[0] : xyFinal[0];
			int upperx = xyInitial[0] > xyFinal[0] ? xyInitial[0] : xyFinal[0];
			if (gradient == 0) {
				for (int w = lowerx; w <= upperx; w++) {
					getLandPoint(RadialMapHelper.formKey(w, xyInitial[1])).setType(markType);
				}
				continue;
			}
			int lowery = xyInitial[1] < xyFinal[1] ? xyInitial[1] : xyFinal[1];
			int uppery = xyInitial[1] > xyFinal[1] ? xyInitial[1] : xyFinal[1];
			double b = xyFinal[1] - gradient * xyFinal[0];
			// 3nd the gradient is positive/negative.
			for (int w = lowerx; w <= upperx; w++) {
				float y = RadialMapHelper.round(gradient * w + b);
					getLandPoint(RadialMapHelper.formKey(w, (int) y)).setType(markType);
			}
			for (int w = lowery; w <= uppery; w++) {
				float x = RadialMapHelper.round( (w - b)/gradient);
				if (x == (int) x) // quick and dirty convertion check
				{
					getLandPoint(RadialMapHelper.formKey((int) x, w)).setType(markType);
				}
			}
		}
	}
	
	public void createALine(int inicialPoint, int finalPoint, String markType) {
			int xyInitial[] = RadialMapHelper.breakKey(inicialPoint);
			int xyFinal[] = RadialMapHelper.breakKey(finalPoint);

			int underscore = (xyFinal[0] - xyInitial[0]);
			if (underscore == 0) {
				int lower = xyInitial[1] < xyFinal[1] ? xyInitial[1] : xyFinal[1];
				int upper = xyInitial[1] > xyFinal[1] ? xyInitial[1] : xyFinal[1];

				for (int w = lower; w <= upper; w++) {
					getLandPoint(RadialMapHelper.formKey(xyInitial[0], w)).setType(markType);
				}
				return;
			}

			double gradient = (xyFinal[1] - xyInitial[1]) * 1.0 / underscore;
			// 2nd, gradient=0; straight in the X axis
			int lowerx = xyInitial[0] < xyFinal[0] ? xyInitial[0] : xyFinal[0];
			int upperx = xyInitial[0] > xyFinal[0] ? xyInitial[0] : xyFinal[0];
			if (gradient == 0) {
				for (int w = lowerx; w <= upperx; w++) {
					if (landPointisOnMap(RadialMapHelper.formKey(xyInitial[0], w))) {
						getLandPoint(RadialMapHelper.formKey(w, xyInitial[1])).setType(markType);
					}
				}
				return;
			}
			int lowery = xyInitial[1] < xyFinal[1] ? xyInitial[1] : xyFinal[1];
			int uppery = xyInitial[1] > xyFinal[1] ? xyInitial[1] : xyFinal[1];

			double b = xyFinal[1] - gradient * xyFinal[0];
			// 3nd the gradient is positive/negative.
			for (int w = lowerx; w <= upperx; w++) {
				float y = RadialMapHelper.round(gradient * w + b);
					if (landPointisOnMap(RadialMapHelper.formKey(xyInitial[0], w))) {
						getLandPoint(RadialMapHelper.formKey(w, (int) y)).setType(markType);
					}
			}
			for (int w = lowery; w <= uppery; w++) {
				float x = RadialMapHelper.round( (w - b)/gradient);
				if (x == (int) x) // quick and dirty convertion check
				{
					if (landPointisOnMap(RadialMapHelper.formKey(xyInitial[0], w))) {
						getLandPoint(RadialMapHelper.formKey((int) x, w)).setType(markType);
					}
				}
			}
			
			
	}
	public void  printTriangular(int pnt1,int pnt2, int pnt3,String mask){
		int xyInitial[] = RadialMapHelper.breakKey(pnt1);
		int xyFinal[] = RadialMapHelper.breakKey(pnt2);

		int underscore = (xyFinal[0] - xyInitial[0]);
		if (underscore == 0) {
			int lower = xyInitial[1] < xyFinal[1] ? xyInitial[1] : xyFinal[1];
			int upper = xyInitial[1] > xyFinal[1] ? xyInitial[1] : xyFinal[1];

			for (int w = lower; w <= upper; w++) {
				createALine(RadialMapHelper.formKey(xyInitial[0], w),pnt3,mask);
			}
			return;
		}

		double gradient = (xyFinal[1] - xyInitial[1]) * 1.0 / underscore;
		// 2nd, gradient=0; straight in the X axis
		int lowerx = xyInitial[0] < xyFinal[0] ? xyInitial[0] : xyFinal[0];
		int upperx = xyInitial[0] > xyFinal[0] ? xyInitial[0] : xyFinal[0];
		if (gradient == 0) {
			for (int w = lowerx; w <= upperx; w++) {
				
				createALine(RadialMapHelper.formKey(w, xyInitial[1]),pnt3,mask);
				
			}
			return;
		}
		int lowery = xyInitial[1] < xyFinal[1] ? xyInitial[1] : xyFinal[1];
		int uppery = xyInitial[1] > xyFinal[1] ? xyInitial[1] : xyFinal[1];

		double b = xyFinal[1] - gradient * xyFinal[0];
		// 3nd the gradient is positive/negative.
		for (int w = lowerx; w <= upperx; w++) {
			float y = RadialMapHelper.round(gradient * w + b);
				
				createALine(RadialMapHelper.formKey(w, (int) y),pnt3,mask);
				
		}
		for (int w = lowery; w <= uppery; w++) {
			float x = RadialMapHelper.round( (w - b)/gradient);
			if (x == (int) x) // quick and dirty convertion check
			{
			
				createALine(RadialMapHelper.formKey((int) x, w),pnt3,mask);
				
			}
		}
	}
	
	
	public void createACustomRoute(int inicialPoint, int finalPoint,int size, String markType) {
		
		int xyInitial[] = RadialMapHelper.breakKey(inicialPoint);
		int xyFinal[] = RadialMapHelper.breakKey(finalPoint);
		int cont= 1;
		int sign = 1; 
		int aux = 0;
		int underscore = (xyFinal[0] - xyInitial[0]);
		if (underscore == 0) {
			
			createALine(inicialPoint,finalPoint,markType);
			for (int w = 0; w <= size-1; w++) {
				int auxInitPoint = RadialMapHelper.formKey(xyInitial[0] + cont*sign, xyInitial[1]) ;
				int auxFinPoint = RadialMapHelper.formKey(xyFinal[0] + cont*sign , xyFinal[1]);
				if(!landPointisOnMap(auxInitPoint) || !landPointisOnMap(auxFinPoint)){
					//System.out.println("point out of range");
					return;
				}
				createALine(auxInitPoint,auxFinPoint,markType);
				sign=sign*(-1);
				if(aux == 1){
					cont++;
					aux=0;
					continue;
				}
				aux++;
			}
			return;
		}

		double gradient = (xyFinal[1] - xyInitial[1]) * 1.0 / underscore;
		// 2nd, gradient=0; straight in the X axis
		if (gradient == 0) {
			createALine(inicialPoint,finalPoint,markType);
			for (int w = 0; w < size; w++) {
				int auxInitPoint = RadialMapHelper.formKey(xyInitial[0] , xyInitial[1] + cont*sign) ;
				int auxFinPoint = RadialMapHelper.formKey(xyFinal[0]   , xyFinal[1] + cont*sign);	
				if(!landPointisOnMap(auxInitPoint) || !landPointisOnMap(auxFinPoint)){
					//System.out.println("point out of range");
					return;
				}
				createALine(auxInitPoint,auxFinPoint,markType);
				sign=sign*(-1);
				if(aux == 1){
					cont++;
					aux=0;
					continue;
				}
				aux++;
			}
			return;
		}
		// 3nd the gradient is positive/negative.
		double contGradient = (-1)*(1.0/gradient);
		double b1 = xyInitial[1] - contGradient * xyInitial[0];
		double b2 = xyFinal[1] - contGradient * xyFinal[0];
		int x1,x2,y1,y2;
		createALine(inicialPoint,finalPoint,markType);
		for (int w = 0; w <= size-1; w++) {
			if(Math.abs(xyFinal[0]-xyInitial[0]) <  Math.abs(xyFinal[1]-xyInitial[1])){
				 x1 = xyInitial[0] + cont*sign;
				 x2 = xyFinal[0] + cont*sign;
				 y1 =  (int)RadialMapHelper.round(contGradient * x1 + b1);
				 y2 =  (int)RadialMapHelper.round(contGradient * x2 + b2);
			}else{
				 y1 = xyInitial[1] + cont*sign;
				 y2 = xyFinal[1] + cont*sign;
				 x1 =  (int)RadialMapHelper.round((y1 - b1)/contGradient);
				 x2 =  (int)RadialMapHelper.round((y2 - b2)/contGradient);
				
			}
	
			int auxInitPoint = RadialMapHelper.formKey(x1, y1) ;
			int auxFinPoint = RadialMapHelper.formKey( x2, y2);	
			if(!landPointisOnMap(auxInitPoint) || !landPointisOnMap(auxFinPoint)){
				//System.out.println("point out of range");
				return;
			}
			if((size/2) <= distanceOfPointToPoint(auxInitPoint,inicialPoint)){
				createALine(auxInitPoint,auxFinPoint,markType);
				break;
			}
			createALine(auxInitPoint,auxFinPoint,markType);
					
			sign=sign*(-1);
			if(aux == 1){
				cont++;
				aux=0;
				continue;
			}
			aux++;
		}
		return;
		
		
	}
	
	public int findProyectionPointIntoParalelStraights(int rect1Ini, int rect1End, int pointRef, boolean belong){ ////WARNING////////
		int pointSolution = -1;
		int xyRec1Ini[] = RadialMapHelper.breakKey(rect1Ini);
		int xyRec1End[] = RadialMapHelper.breakKey(rect1End);
		int xyPointRef[] = RadialMapHelper.breakKey(pointRef);
		int underscore = (xyRec1End[0] - xyRec1Ini[0]);
		double gradient = 0;
		double b = 0;
		if (underscore == 0) {
			int lower = xyRec1Ini[1] < xyRec1End[1] ? xyRec1Ini[1] : xyRec1End[1];
			int upper = xyRec1Ini[1] > xyRec1End[1] ? xyRec1Ini[1] : xyRec1End[1];	
				if((lower <= xyPointRef[1] && xyPointRef[1] <= upper)|| !belong){ //verify if the point belong to the straight
					pointSolution =  RadialMapHelper.formKey(xyRec1Ini[0],xyPointRef[1] );		
				}
				return pointSolution;
		}else{
			gradient = (xyRec1End[1] - xyRec1Ini[1]) * 1.0 / underscore;
			b = (xyRec1Ini[0]*xyRec1End[1] -(xyRec1End[0]*xyRec1Ini[1]))/(xyRec1Ini[0] -xyRec1End[0]);
			if(gradient == 0){
				int lower = xyRec1Ini[0] < xyRec1End[0] ? xyRec1Ini[0] : xyRec1End[0];
				int upper = xyRec1Ini[0] > xyRec1End[0] ? xyRec1Ini[0] : xyRec1End[0];
				if((lower <= xyPointRef[0] && xyPointRef[0] <= upper)|| !belong){ //verify if the point belong to the straight
					pointSolution =  RadialMapHelper.formKey( xyPointRef[0],xyRec1Ini[1] );		
				}
				return pointSolution;
			}
			double contGrad = -1*(1/gradient);
			double b2 = xyPointRef[1] - contGrad*xyPointRef[0];
			int lower = xyRec1Ini[0] < xyRec1End[0] ? xyRec1Ini[0] : xyRec1End[0];
			int upper = xyRec1Ini[0] > xyRec1End[0] ? xyRec1Ini[0] : xyRec1End[0];
			double x = (b2-b)/(gradient-contGrad);
			double y = contGrad*x + b2;
			RadialMapHelper.round(x);
			RadialMapHelper.round(y);
			if((lower <= x && x <= upper)|| !belong){
				pointSolution =  RadialMapHelper.formKey( (int)x,(int)y );
			}
		}
		
		return pointSolution;
	}
	
	//just works when the point belongs to both Straight
	//added a bool parameter to allow find a point that no belongs to any straigt
	public int findIntersectionPointIntoTwoStraight(int rect1Ini, int rect1End, int rect2Ini, int rect2End, boolean belong){
		int pointSolution =-1;
		int xyRec1Ini[] = RadialMapHelper.breakKey(rect1Ini);
		int xyRec1End[] = RadialMapHelper.breakKey(rect1End);
		int xyRec2Ini[] = RadialMapHelper.breakKey(rect2Ini);
		int xyRec2End[] = RadialMapHelper.breakKey(rect2End);
		
		int underscore1 = (xyRec1End[0] - xyRec1Ini[0]);
		int underscore2 = (xyRec2End[0] - xyRec2Ini[0]);
		double gradient1 = 0;
		double gradient2 = 0;
		double b1 = 0;
		double b2 = 0;
		
		if(underscore1 == 0 && underscore2 == 0) return pointSolution; // would be the same straight or paralell
		if(underscore1 != 0){
			gradient1 = (xyRec1End[1] - xyRec1Ini[1]) * 1.0 / underscore1;
			b1 = (xyRec1Ini[0]*xyRec1End[1] -(xyRec1End[0]*xyRec1Ini[1]))/(xyRec1Ini[0] -xyRec1End[0]);
		}
		if(underscore2 != 0){
			gradient2 = (xyRec2End[1] - xyRec2Ini[1]) * 1.0 / underscore2;
			b2 = (xyRec2Ini[0]*xyRec2End[1] -(xyRec2End[0]*xyRec2Ini[1]))/(xyRec2Ini[0] -xyRec2End[0]);
		}
		if((gradient1 == 0 && gradient2 == 0)|| (gradient1 == gradient2)) return pointSolution;  //would be the same straight or paralell
		
		
		if (underscore1 == 0 && underscore2 != 0) {
			int lower = xyRec1Ini[1] < xyRec1End[1] ? xyRec1Ini[1] : xyRec1End[1];
			int upper = xyRec1Ini[1] > xyRec1End[1] ? xyRec1Ini[1] : xyRec1End[1];	
			double yAux = xyRec1End[0]*gradient2 + b2;
				if((lower <= yAux && yAux <= upper)|| !belong){
					
					RadialMapHelper.round(yAux);
					pointSolution =  RadialMapHelper.formKey( xyRec1End[0],(int)yAux);
					
					return pointSolution;
				}

				
		
		}
		if (underscore2 == 0 && underscore1 != 0) {
			int lower = xyRec2Ini[1] < xyRec2End[1] ? xyRec2Ini[1] : xyRec2End[1];
			int upper = xyRec2Ini[1] > xyRec2End[1] ? xyRec2Ini[1] : xyRec2End[1];
				double yAux = xyRec2End[0]*gradient1 + b1;
				
				if((lower <= yAux && yAux <= upper)|| !belong){ //verify if the point belong to the straight
					RadialMapHelper.round(yAux);
					pointSolution =  RadialMapHelper.formKey(xyRec2End[0],(int)yAux);
					return pointSolution;
				}
				
		}
		if (underscore1 != 0 && underscore2 != 0) {
				if(gradient1 == 0){
					int lower = xyRec1Ini[0] < xyRec1End[0] ? xyRec1Ini[0] : xyRec1End[0];
					int upper = xyRec1Ini[0] > xyRec1End[0] ? xyRec1Ini[0] : xyRec1End[0];
					double xAux = (xyRec1End[1]- b2)/gradient2;
					if((lower <= xAux && xAux <= upper)|| !belong){
						
						RadialMapHelper.round(xAux);
						pointSolution =  RadialMapHelper.formKey( (int)xAux, xyRec1End[1]);
						
						return pointSolution;
					}
					
				}
				if(gradient2 == 0){
					int lower = xyRec2Ini[0] < xyRec2End[0] ? xyRec2Ini[0] : xyRec2End[0];
					int upper = xyRec2Ini[0] > xyRec2End[0] ? xyRec2Ini[0] : xyRec2End[0];
					double xAux = (xyRec2End[1]- b1)/gradient1;
					if((lower <= xAux && xAux <= upper)|| !belong){
						
						RadialMapHelper.round(xAux);
						pointSolution =  RadialMapHelper.formKey((int)xAux,xyRec2End[1]);
						
						return pointSolution;
					}
				}
			
			int lowerx = xyRec2Ini[0] < xyRec2End[0] ? xyRec2Ini[0] : xyRec2End[0];
			int upperx = xyRec2Ini[0] > xyRec2End[0] ? xyRec2Ini[0] : xyRec2End[0];
			int lowery = xyRec2Ini[1] < xyRec2End[1] ? xyRec2Ini[1] : xyRec2End[1];
			int uppery = xyRec2Ini[1] > xyRec2End[1] ? xyRec2Ini[1] : xyRec2End[1];
			double xAux = (b2 -b1)/(gradient1- gradient2);
			double yAux = gradient1*(xAux) + b1;		
			
			
				if((lowerx <= xAux && xAux <= upperx && lowery <= yAux && yAux <= uppery) || !belong){//verify if the point belong to the straight because it wouldn't be include in both
					RadialMapHelper.round(xAux);
					RadialMapHelper.round(yAux);
					pointSolution =  RadialMapHelper.formKey((int)xAux, (int)yAux);	
				
					return pointSolution;
				}
				
		
		}
		
		return pointSolution; //if dosnt fint, or dont exist, return -1
	}
	
	public double findGradient(int inicialPoint, int finalPoint){
		int xyInitial[] = RadialMapHelper.breakKey(inicialPoint);
		int xyFinal[] = RadialMapHelper.breakKey(finalPoint);
		double gradient;
		int underscore = (xyFinal[0] - xyInitial[0]);
		if(underscore == 0) return 0; //es infinito, mejorar
		
		gradient = (xyFinal[1] - xyInitial[1]) * 1.0 / underscore;
		return gradient;
	}
	public double distanceOfPointToPoint(int pointi, int pointf) {
		int xyPointF[] = RadialMapHelper.breakKey(pointf);
		int xyPointI[] = RadialMapHelper.breakKey(pointi);
		return Math.sqrt(Math.pow(xyPointF[0] - xyPointI[0], 2) + Math.pow(xyPointF[1] - xyPointI[1], 2));
	}

	public int findPointOnStreightToDistance(int pointIni,int pointEnd,double distance){
		int xyInitial[] = RadialMapHelper.breakKey(pointIni);
		int xyFinal[] = RadialMapHelper.breakKey(pointEnd);
		int underscore = (xyFinal[0] - xyInitial[0]);
		double gradient = 0;
		double b = 0;
		int pointSolution = -1;
		
		if (underscore == 0) {	
				if(xyInitial[1] < xyFinal[1] ){ 
					pointSolution =  RadialMapHelper.formKey(xyFinal[0],(xyInitial[1] + (int)distance));		
				}else{
					pointSolution =  RadialMapHelper.formKey(xyFinal[0],(xyInitial[1] - (int)distance));		
				}
				if(!landPointisOnMap(pointSolution)){
					//System.out.println("point out of range");
					return -1;
				}
				return pointSolution;
		}else{
			gradient = (xyFinal[1] - xyInitial[1]) * 1.0 / underscore;
			b = (xyInitial[0]*xyFinal[1] -(xyFinal[0]*xyInitial[1]))/(xyInitial[0] -xyFinal[0]);
			if(gradient == 0){
				if(xyInitial[0] < xyFinal[0]){ //verify if the point belong to the straight
					pointSolution =  RadialMapHelper.formKey( (xyInitial[0] + (int)distance),xyFinal[1] );		
				}else{
					pointSolution =  RadialMapHelper.formKey( (xyInitial[0] - (int)distance),xyFinal[1] );	
				}
				if(!landPointisOnMap(pointSolution)){
					//System.out.println("point out of range");
					return -1;
				}
				return pointSolution;
			}
			int sign,init,end;
			boolean onX = false;
			if(Math.abs(xyFinal[0]-xyInitial[0]) <  Math.abs(xyFinal[1]-xyInitial[1])){
			//do on Y
				init = xyInitial[1];
				end = xyFinal[1];
				if(xyInitial[1] < xyFinal[1]) sign = 1;
				else{
					sign = -1;
				}
			}else{
			//do on X
				init = xyInitial[0];
				end = xyFinal[0];
				if(xyInitial[0] < xyFinal[0] ) sign = 1;
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
					double auxDist = distanceOfPointToPoint(pointIni,pointSolution);
					if(auxDist >= distance){
						if(!landPointisOnMap(pointSolution)){
							//System.out.println("point out of range");
							return -1;
						}
						return pointSolution;
					}
				}
			}else if(sign == -1){
				for(int i = init; i >= end; i--){
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
					double auxDist = distanceOfPointToPoint(pointIni,pointSolution);
					if(auxDist >= distance){
						if(!landPointisOnMap(pointSolution)){
							//System.out.println("point out of range");
							return -1;
						}
						return pointSolution;
					}
				}
			}
			if(!landPointisOnMap(pointSolution)){
				//System.out.println("point out of range");
				return -1;
			}
			return pointSolution;
		}	
	}

	private void clearDottedLimits() {
		for (int x = 0; x < fullPolygon.size(); x++) {
			for (int i = 0; i < fullPolygon.get(x).size(); i++) {
				getLandPoint(fullPolygon.get(x).get(i)).setType(RadialConfiguration.EMPTY_MARK);
			}
		}
	}

	public void findPolygonalArea(List<RadialLandPoint> polygon) {
		int absoluteArea = 0;
		for (int i = 0; i < polygon.size(); i++) {
			absoluteArea += (polygon.get(i).getX() * polygon.get((i + 1) % polygon.size()).getY())
					- (polygon.get(i).getY() * polygon.get((i + 1) % polygon.size()).getX());
		}
		setPolygonalArea(Math.abs(absoluteArea) / 2);
	}

	private void findCentroid(List<RadialLandPoint> polygon) {
		this.setCentroid(new RadialLandPoint(pointsx / 2, pointsy / 2));
	}

	public void printMapToFile() {
		try {
			PrintWriter writer = new PrintWriter("printed-map.txt", "UTF-8");
			for (int j = pointsy - 1; j >= 0; j--) {
				for (int i = 0; i < pointsx; i++) {
					writer.print(getLandPoint(RadialMapHelper.formKey(i, j)).getType());
				}
				writer.println();
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void markVariation(int entryPointId, int branchType, int nodeType) {
		String variation = "-";
		switch (branchType) {
		case RadialConfiguration.ARTERIAL_BRANCH:
			variation = RadialConfiguration.ARTERIAL_MARK;
			break;
		case RadialConfiguration.COLLECTOR_BRANCH:
			variation = RadialConfiguration.COLLECTOR_MARK;
			break;
		case RadialConfiguration.LOCAL_BRANCH:
			variation = RadialConfiguration.LOCAL_MARK;
			break;
		case RadialConfiguration.NODE:
			variation = RadialConfiguration.NODE_MARK;
			break;
		}
		map.get(entryPointId).setType(variation);
		map.get(entryPointId).setNodeType(nodeType);
	}

	public boolean landPointisOnMap(int pointId) {
		int[] xy = RadialMapHelper.breakKey(pointId);
		return xy[0] < pointsx && xy[0] > 0 && xy[1] < pointsy && xy[1] > 0;
	}

	public List<Integer> getNodes() {
		return nodes;
	}

	public void setNodes(List<Integer> nodes) {
		this.nodes = nodes;
	}

	public List<RadialLandPoint> getPolygonNodes() {
		return polygonNodes;
	}

	public void setPolygonNodes(List<RadialLandPoint> polygonNodes) {
		this.polygonNodes = polygonNodes;
	}

	public double getPolygonalArea() {
		return polygonalArea;
	}

	public void setPolygonalArea(double polygonalArea) {
		this.polygonalArea = polygonalArea;
	}

	public boolean isSpecialNode(int x, int y) {
		// up,down,left,right
		if (!findPoint(RadialMapHelper.formKey(x, y)).getType().equals(RadialConfiguration.NODE_MARK)) {
			return false;
		}

		if ((y + 1 != pointsy) && (y - 1 != -1) && (x - 1 != -1) && (x + 1 != pointsx)) {
			if (findPoint(RadialMapHelper.formKey(x, y + 1)).getType().equals(RadialConfiguration.NODE_MARK)
					&& findPoint(RadialMapHelper.formKey(x, y - 1)).getType()
							.equals(RadialConfiguration.OUTSIDE_POLYGON_MARK)
					&& findPoint(RadialMapHelper.formKey(x + 1, y)).getType().equals(RadialConfiguration.EMPTY_MARK)
					&& (!findPoint(RadialMapHelper.formKey(x - 1, y)).getType().equals(RadialConfiguration.EMPTY_MARK)
							|| !findPoint(RadialMapHelper.formKey(x + 1, y)).getType()
									.equals(RadialConfiguration.NODE_MARK))) {
				return true;
			}
		}

		return false;
	}

	public boolean isNormalNode(int x, int y) {
		// up,down,left,right
		if (!findPoint(RadialMapHelper.formKey(x, y)).getType().equals(RadialConfiguration.NODE_MARK)) {
			return false;
		}
		int node = 0;
		boolean[] nodeInBorder = new boolean[] { false, false, false, false };
		int outside = 0;
		if (y + 1 != pointsy) {
			if (findPoint(RadialMapHelper.formKey(x, y + 1)).getType().equals(RadialConfiguration.NODE_MARK)) {
				node++;
				nodeInBorder[0] = true;
			}

			if (findPoint(RadialMapHelper.formKey(x, y + 1)).getType()
					.equals(RadialConfiguration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if (y - 1 != -1) {
			if (findPoint(RadialMapHelper.formKey(x, y - 1)).getType().equals(RadialConfiguration.NODE_MARK)) {
				node++;
				nodeInBorder[1] = true;
			}

			if (findPoint(RadialMapHelper.formKey(x, y - 1)).getType()
					.equals(RadialConfiguration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if (x - 1 != -1) {
			if (findPoint(RadialMapHelper.formKey(x - 1, y)).getType().equals(RadialConfiguration.NODE_MARK)) {
				node++;
				nodeInBorder[2] = true;
			}

			if (findPoint(RadialMapHelper.formKey(x - 1, y)).getType()
					.equals(RadialConfiguration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if (x + 1 != pointsx) {
			if (findPoint(RadialMapHelper.formKey(x + 1, y)).getType().equals(RadialConfiguration.NODE_MARK)) {
				node++;
				nodeInBorder[3] = true;
			}

			if (findPoint(RadialMapHelper.formKey(x + 1, y)).getType()
					.equals(RadialConfiguration.OUTSIDE_POLYGON_MARK))
				outside++;
		}

		if ((y + 1 != pointsy) && (y - 1 != -1) && (x - 1 != -1) && (x + 1 != pointsx)) {
			if ((findPoint(RadialMapHelper.formKey(x, y - 1)).getType().equals(RadialConfiguration.ARTERIAL_MARK)
					|| findPoint(RadialMapHelper.formKey(x, y - 1)).getType().equals(RadialConfiguration.LOCAL_MARK))
					&& findPoint(RadialMapHelper.formKey(x - 1, y)).getType()
							.equals(RadialConfiguration.COLLECTOR_MARK)
					&& findPoint(RadialMapHelper.formKey(x + 1, y)).getType().equals(RadialConfiguration.NODE_MARK)
					&& findPoint(RadialMapHelper.formKey(x, y + 1)).getType().equals(RadialConfiguration.NODE_MARK)) {
				return true;
			}
		}

		if (node == 2 && (!(nodeInBorder[0] && nodeInBorder[1]) || !(nodeInBorder[2] && nodeInBorder[3]))) {
			return false;
		}

		if (node + outside > 1) {
			return true;
		}
		return false;
	}

	public RadialPolygon joinWithPolygonalBorder(RadialPolygon RadialPolygon) {
		int initialVertex = RadialPolygon.getPoints().get(0);
		int finalVertex = RadialPolygon.getPoints().get(RadialPolygon.getPoints().size() - 1);
		int initialVertexSide = -1;
		int finalVertexSide = -1;

		for (int x = 0; x < fullPolygon.size(); x++) {
			for (int i = 0; i < fullPolygon.get(x).size(); i++) {
				// System.out.print(fullPolygon.get(x).get(i) + ",");

				if ((fullPolygon.get(x).get(i) == initialVertex) && (initialVertexSide == -1)) {
					initialVertexSide = x;
				}

				if ((fullPolygon.get(x).get(i) == finalVertex) && (finalVertexSide == -1)) {
					finalVertexSide = x;
				}

				if ((initialVertexSide != -1) && (finalVertexSide != -1))
					break;
			}
			if ((initialVertexSide != -1) && (finalVertexSide != -1))
				break;
		}
		// they both are on the same line. Meaning it is a triangle
		if (initialVertexSide == finalVertexSide) {
			RadialPolygon.setComplete(true);
			return RadialPolygon;
		}
		// System.out.println("Vertexes " + initialVertexSide + "||" +
		// finalVertexSide);

		if (initialVertexSide != finalVertexSide) {
			// simple complex figure
			// This polygons should be treated as if another procedure is
			// missing (going up from initial point
			// before trying again this strategy
			if ((initialVertexSide == -1 && finalVertexSide != -1)
					|| (initialVertexSide != -1 && finalVertexSide == -1)) {
				// System.out.println("Incomplete data polygon");
				RadialPolygon.printPolygon();
				return RadialPolygon;
			}

			int initialVertex0 = fullPolygon.get(initialVertexSide).get(0);
			int initialVertexFinal = fullPolygon.get(initialVertexSide)
					.get(fullPolygon.get(initialVertexSide).size() - 1);
			int finalVertex0 = fullPolygon.get(finalVertexSide).get(0);
			int finalVertexFinal = fullPolygon.get(finalVertexSide).get(fullPolygon.get(finalVertexSide).size() - 1);

			if (initialVertex0 == finalVertex0) {
				RadialPolygon.getPoints().add(initialVertex0);
				RadialPolygon.setComplete(true);
				// System.out.println("Complete data polygon");
				RadialPolygon.printPolygon();
				return RadialPolygon;
			} else if (initialVertex0 == finalVertexFinal) {
				RadialPolygon.getPoints().add(initialVertex0);
				RadialPolygon.setComplete(true);
				// System.out.println("Complete data polygon");
				RadialPolygon.printPolygon();
				return RadialPolygon;
			} else if (initialVertexFinal == finalVertex0) {
				RadialPolygon.getPoints().add(initialVertexFinal);
				RadialPolygon.setComplete(true);
				// System.out.println("Complete data polygon");
				RadialPolygon.printPolygon();
				return RadialPolygon;
			} else if (initialVertexFinal == finalVertexFinal) {
				RadialPolygon.getPoints().add(initialVertexFinal);
				RadialPolygon.setComplete(true);
				// System.out.println("Complete data polygon");
				RadialPolygon.printPolygon();
				return RadialPolygon;
			}
			// System.out.println("Polygon with more than 4 sides");
		}
		return RadialPolygon;
	}

	public Object lotize(List<Integer> list, int direction, int beginning) {
		if (beginning >= list.size()) {
			return 0;
		}

		int seed = 0;
		boolean lotizable = true, notUniform = false;
		int[] currentXY = RadialMapHelper.breakKey(list.get(beginning));
		int[] finalXY = RadialMapHelper.breakKey(list.get((beginning + 1) % list.size()));
		Double gradient = (currentXY[1] - finalXY[1]) * 1.0 / (currentXY[0] - finalXY[0]);
		double offset = finalXY[1] - gradient * finalXY[0];
		if (direction == RadialConstants.EAST || direction == RadialConstants.WEST) {
			if (gradient.doubleValue() == 0.0) {// perfect case
				currentXY[0] = direction == RadialConstants.EAST ? currentXY[0] + 1 : currentXY[0];
				RadialBuilding RadialBuilding = createWalkRoute(currentXY, false, direction, beginning);
				if (RadialBuilding != null) {
					currentXY = RadialMapHelper.moveKeyByOffsetAndDirection(currentXY,
							RadialConfiguration.WALK_BRANCH_SIZE, direction);
				}

				finalXY[0] = direction == RadialConstants.EAST ? finalXY[0] : finalXY[0] + 1;
				RadialBuilding = createWalkRoute(finalXY, true, direction, beginning);
				if (RadialBuilding != null) {
					finalXY = RadialMapHelper.moveKeyByOffsetAndDirection(finalXY,
							RadialConfiguration.WALK_BRANCH_SIZE, RadialDirectionHelper.oppositeDirection(direction));
				}
			} else {// imperfect case
				notUniform = true;
				currentXY = createNonOrthogonalWalkRoute(list.get(beginning), list.get((beginning + 1) % list.size()),
						finalXY, false, gradient);

				finalXY = createNonOrthogonalWalkRoute(list.get(beginning), list.get((beginning + 1) % list.size()),
						finalXY, true, gradient);
			}
		} else if (direction == RadialConstants.SOUTH || direction == RadialConstants.NORTH) {
			if (gradient.isInfinite()) {
				// means it is a route connection and a perfect one at it in
				// this initial case is neccesary to find the intermediate point
				// - 6 to make
				createRadialEntrance(currentXY, finalXY, direction);
				finalXY = RadialMapHelper.moveKeyByOffsetAndDirection(finalXY,
						configuration.getLotConfiguration().getDepthSize() * 2, direction);
				currentXY = RadialMapHelper.moveKeyByOffsetAndDirection(currentXY,
						configuration.getLotConfiguration().getDepthSize() * 2,
						RadialDirectionHelper.oppositeDirection(direction));
			} else {
				notUniform = true;
				createNonOrthogonalRadialEntrance(currentXY, finalXY, direction, gradient);
				// TODO currentXY and finalXY move to occupy all side
			}
		}

		int maxNumberofRepetitions =0, repetitions = 0;
		if(notUniform){
			maxNumberofRepetitions = (int) Math.sqrt(Math.pow(currentXY[0]-finalXY[0], 2)+Math.pow(currentXY[1]-finalXY[1], 2));
		}
		while (true) {
			boolean done = false;
			if ((direction == RadialConstants.EAST) || (direction == RadialConstants.NORTH))
				done = currentXY[0] >= finalXY[0] && currentXY[1] >= finalXY[1];
			else
				done = currentXY[0] <= finalXY[0] && currentXY[1] <= finalXY[1];

			// TODO eliminate this. There was a problem with the detection of
			// problems lol when there is a view like this.
			if (notUniform){
				if(maxNumberofRepetitions == repetitions){
					done = true;
				}
			}

			if (done) {
				if (notUniform) {
					int newDirection = RadialDirectionHelper.orthogonalDirectionFromPointToPoint(
							list.get((beginning + 1) % list.size()), list.get((beginning + 2) % list.size()));
					return lotize(list, newDirection, ++beginning);
				}
				switch (direction) {
				case RadialConstants.EAST:
					return lotize(list, RadialConstants.NORTH, ++beginning);
				case RadialConstants.NORTH:
					return lotize(list, RadialConstants.WEST, ++beginning);
				case RadialConstants.WEST:
					return lotize(list, RadialConstants.SOUTH, ++beginning);
				case RadialConstants.SOUTH:
					return 0;
				}
			}

			if (gradient.doubleValue() == 0.0 || gradient.isInfinite()) {
				lotizable = canBeLotized(currentXY, configuration.getLotConfiguration().getSideSize(),
						configuration.getLotConfiguration().getDepthSize() * 2, direction);
				if (lotizable) {
					createDoubleLot(currentXY, configuration.getLotConfiguration().getSideSize(),
							configuration.getLotConfiguration().getDepthSize(), direction, seed % 4);
					currentXY = RadialMapHelper.moveKeyByOffsetAndDirection(currentXY,
							configuration.getLotConfiguration().getSideSize(), direction);
					seed += 2;
				} else {
					currentXY = RadialMapHelper.moveKeyByOffsetAndDirection(currentXY, 1, direction);
				}
			} else {
				lotizable = canBeNonOrthogonallyLotized(currentXY, finalXY,
						configuration.getLotConfiguration().getSideSize(), configuration.getLotConfiguration().getDepthSize(),
						direction, gradient);
				if (lotizable) {
					createNonOrthogonalLot(currentXY, finalXY, configuration.getLotConfiguration().getSideSize(),
							configuration.getLotConfiguration().getDepthSize(), direction, gradient, seed % 4);
					currentXY = RadialMapHelper.moveKeyByGradientAndOffset(currentXY, finalXY,
							configuration.getLotConfiguration().getSideSize(), gradient, offset, direction);
					seed += 2;
				} else {
					currentXY = RadialMapHelper.moveKeyByGradientAndOffset(currentXY, finalXY, 1, gradient, offset,
							direction);
				}
			}
			repetitions++;
		}
	}

	private boolean canBeNonOrthogonallyLotized(int[] beginXY, int[] finalXY, int houseSideSize, int houseDepthSize,
			int direction, Double gradient) {
		int[] tbXY = new int[2];
		int[] tfXY = new int[2];
		double offset = -gradient * beginXY[0] + beginXY[1];
		double distance = Math.sqrt(0 + Math.pow(beginXY[1] - finalXY[1], 2));
		boolean inverse = false;
		boolean isUpDown = false;

		if (distance < houseSideSize) {
			return false;
		}

		if (beginXY[1] < finalXY[1]) {
			tbXY[1] = (int) beginXY[1];
			tbXY[0] = (int) ((tbXY[1] - offset) / gradient);

			tfXY[1] = (int) finalXY[1];
			tfXY[0] = (int) ((tfXY[1] - offset) / gradient);
		} else {
			isUpDown = true;
			tbXY[1] = (int) finalXY[1];
			tbXY[0] = (int) ((tbXY[1] - offset) / gradient);

			tfXY[1] = (int) beginXY[1];
			tfXY[0] = (int) ((tfXY[1] - offset) / gradient);
		}

		// THIS IS FOR FINDING WHERE DOES X POINT ALL THE TIME GIVEN POINTS
		// COORDINATES. DO NOT MOVE.
		if (finalXY[0] > beginXY[0]) {
			if (finalXY[1] > beginXY[1]) {
				inverse = false;
			} else {
				inverse = true;
			}
		} else {
			if (finalXY[1] > beginXY[1]) {
				inverse = false;
			} else {
				inverse = true;
			}
		}

		if (tbXY[1] > tfXY[1])
			return false;

		// double orthogonalGradient = -1 / gradient;
		// double orthogonalOffset = -orthogonalGradient * tbXY[0] + tbXY[1];
		double variation[] = new double[2];
		int[] currentXY = new int[2];
		int countYFactor = 0;
		int oldVariation = -1;

		for (int j = 0; j < houseSideSize; j++) {
			currentXY[0] = tbXY[0] + j;
			currentXY[1] = (int) (gradient * currentXY[0] + offset);
			// orthogonalOffset = -orthogonalGradient * currentXY[0] +
			// currentXY[1];

			if (isUpDown && (oldVariation != -1) && ((oldVariation + 1) < currentXY[1])) {
				// we take the reminder y that are needed for an exact answer
				for (int w = oldVariation + 1; w < currentXY[1]; w++) {
					for (int i = 0; i < houseDepthSize; i++) {
						variation[0] = currentXY[0] + (!inverse ? i : -i);
						variation[1] = w;
						// orthogonalGradient * variation[0] + orthogonalOffset;
						if (landPointisOnMap(RadialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
							String type = findPoint(RadialMapHelper.formKey((int) variation[0], (int) variation[1]))
									.getType();
							if (!type.equals(RadialConfiguration.EMPTY_MARK))
								return false;
						}
					}
					countYFactor++;
					if (countYFactor == houseSideSize) {
						break;
					}
				}
			} else if ((oldVariation != -1) && ((oldVariation - 1) > currentXY[1])) {
				// we take the reminder y that are needed for an exact answer
				for (int w = currentXY[1] + 1; w < oldVariation; w++) {
					for (int i = 0; i < houseDepthSize; i++) {
						variation[0] = currentXY[0] + (!inverse ? i : -i);
						variation[1] = w;
						// orthogonalGradient * variation[0] + orthogonalOffset;
						if (landPointisOnMap(RadialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
							String type = findPoint(RadialMapHelper.formKey((int) variation[0], (int) variation[1]))
									.getType();
							if (!type.equals(RadialConfiguration.EMPTY_MARK))
								return false;
						}
					}
					countYFactor++;
					if (countYFactor == houseSideSize) {
						break;
					}
				}
			}

			if (countYFactor == houseSideSize) {
				break;
			}

			// We need to find the furthest
			for (int i = 0; i < houseDepthSize; i++) {
				variation[0] = currentXY[0] + (!inverse ? i : -i);
				variation[1] = currentXY[1];
				// orthogonalGradient * variation[0] + orthogonalOffset;
				if (landPointisOnMap(RadialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
					String type = findPoint(RadialMapHelper.formKey((int) variation[0], (int) variation[1])).getType();
					if (!type.equals(RadialConfiguration.EMPTY_MARK))
						return false;
				}
			}
			countYFactor++;
			if (countYFactor == houseSideSize) {
				break;
			}
			oldVariation = (int) currentXY[1];
		}
		return true;
	}

	private void createNonOrthogonalLot(int[] beginXY, int[] finalXY, int houseSideSize, int houseDepthSize,
			int direction, Double gradient, int seed) {
		int[] tbXY = new int[2];
		int[] tfXY = new int[2];
		double offset = -gradient * beginXY[0] + beginXY[1];
		double distance = Math.sqrt(0 + Math.pow(beginXY[1] - finalXY[1], 2));
		boolean inverse = false;
		boolean isUpDown = false;

		if (distance < houseSideSize) {
			return;
		}

		if (beginXY[1] < finalXY[1]) {
			tbXY[1] = (int) beginXY[1];
			tbXY[0] = (int) ((tbXY[1] - offset) / gradient);

			tfXY[1] = (int) finalXY[1];
			tfXY[0] = (int) ((tfXY[1] - offset) / gradient);
		} else {
			isUpDown = true;
			tbXY[1] = (int) finalXY[1];
			tbXY[0] = (int) ((tbXY[1] - offset) / gradient);

			tfXY[1] = (int) beginXY[1];
			tfXY[0] = (int) ((tfXY[1] - offset) / gradient);
		}

		// THIS IS FOR FINDING WHERE DOES X POINT ALL THE TIME GIVEN POINTS
		// COORDINATES. DO NOT MOVE.
		if (finalXY[0] > beginXY[0]) {
			if (finalXY[1] > beginXY[1]) {
				inverse = false;
			} else {
				inverse = true;
			}
		} else {
			if (finalXY[1] > beginXY[1]) {
				inverse = false;
			} else {
				inverse = true;
			}
		}

		if (tbXY[1] > tfXY[1])
			return;

		// double orthogonalGradient = -1 / gradient;
		// double orthogonalOffset = -orthogonalGradient * tbXY[0] + tbXY[1];
		double variation[] = new double[2];
		int[] currentXY = new int[2];
		int countYFactor = 0;
		int oldVariation = -1;

		for (int j = 0; j < houseSideSize; j++) {
			currentXY[0] = tbXY[0] + j;
			currentXY[1] = (int) (gradient * currentXY[0] + offset);
			// orthogonalOffset = -orthogonalGradient * currentXY[0] +
			// currentXY[1];

			if (isUpDown && (oldVariation != -1) && ((oldVariation + 1) < currentXY[1])) {
				// we take the reminder y that are needed for an exact answer
				for (int w = oldVariation + 1; w < currentXY[1]; w++) {
					for (int i = 0; i < houseDepthSize; i++) {
						variation[0] = currentXY[0] + (!inverse ? i : -i);
						variation[1] = w;
						// orthogonalGradient * variation[0] + orthogonalOffset;
						if (landPointisOnMap(RadialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
							findPoint(RadialMapHelper.formKey((int) variation[0], (int) variation[1])).setType("" + seed);
						}
					}
					countYFactor++;
					if (countYFactor == houseSideSize) {
						break;
					}
				}
			} else if ((oldVariation != -1) && ((oldVariation - 1) > currentXY[1])) {
				// we take the reminder y that are needed for an exact answer
				for (int w = currentXY[1] + 1; w < oldVariation; w++) {
					for (int i = 0; i < houseDepthSize; i++) {
						variation[0] = currentXY[0] + (!inverse ? i : -i);
						variation[1] = w;
						// orthogonalGradient * variation[0] + orthogonalOffset;
						if (landPointisOnMap(RadialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
							findPoint(RadialMapHelper.formKey((int) variation[0], (int) variation[1])).setType("" + seed);
						}
					}
					countYFactor++;
					if (countYFactor == houseSideSize) {
						break;
					}
				}
			}

			if (countYFactor == houseSideSize) {
				break;
			}

			// We need to find the furthest
			for (int i = 0; i < houseDepthSize; i++) {
				variation[0] = currentXY[0] + (!inverse ? i : -i);
				variation[1] = currentXY[1];
				// orthogonalGradient * variation[0] + orthogonalOffset;
				if (landPointisOnMap(RadialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
					findPoint(RadialMapHelper.formKey((int) variation[0], (int) variation[1])).setType("" + seed);
				}
			}
			countYFactor++;
			if (countYFactor == houseSideSize) {
				break;
			}
			oldVariation = (int) currentXY[1];
		}
	}

	private void createNonOrthogonalRadialEntrance(int[] beginXY, int[] finalXY, int direction, Double gradient) {
		int[] tbXY = new int[2];
		int[] tfXY = new int[2];
		double offset = -gradient * beginXY[0] + beginXY[1];
		double distance = Math.sqrt(0 + Math.pow(beginXY[1] - finalXY[1], 2));
		boolean inverse = false;
		boolean isUpDown = false;

		if (distance < RadialConfiguration.RADIAL_ENTRANCE_SIZE) {
			return;
		}

		if (beginXY[1] < finalXY[1]) {
			tbXY[1] = (int) (beginXY[1] + (distance) / 2);
			tbXY[0] = (int) ((tbXY[1] - offset) / gradient);

			tfXY[1] = (int) (finalXY[1] - (distance / 2 - RadialConfiguration.RADIAL_ENTRANCE_SIZE));
			tfXY[0] = (int) ((tfXY[1] - offset) / gradient);
		} else {
			isUpDown = true;
			tbXY[1] = (int) (finalXY[1] + (distance) / 2);
			tbXY[0] = (int) ((tbXY[1] - offset) / gradient);

			tfXY[1] = (int) (beginXY[1] - (distance / 2 - RadialConfiguration.RADIAL_ENTRANCE_SIZE));
			tfXY[0] = (int) ((tfXY[1] - offset) / gradient);
		}

		// THIS IS FOR FINDING WHERE DOES X POINT ALL THE TIME GIVEN POINTS
		// COORDINATES. DO NOT MOVE.
		if (finalXY[0] > beginXY[0]) {
			if (finalXY[1] > beginXY[1]) {
				inverse = false;
			} else {
				inverse = true;
			}
		} else {
			if (finalXY[1] > beginXY[1]) {
				inverse = false;
			} else {
				inverse = true;
			}
		}

		if (tbXY[1] > tfXY[1])
			return;

		// double orthogonalGradient = -1 / gradient;
		// double orthogonalOffset = -orthogonalGradient * tbXY[0] + tbXY[1];
		double variation[] = new double[2];
		int[] currentXY = new int[2];
		int countYFactor = 0;
		int oldVariation = -1;

		for (int j = 0; j < RadialConfiguration.RADIAL_ENTRANCE_SIZE; j++) {
			currentXY[0] = tbXY[0] + j;
			currentXY[1] = (int) (gradient * currentXY[0] + offset);
			// orthogonalOffset = -orthogonalGradient * currentXY[0] +
			// currentXY[1];

			if (isUpDown && (oldVariation != -1) && ((oldVariation + 1) < currentXY[1])) {
				// we take the reminder y that are needed for an exact answer
				for (int w = oldVariation + 1; w < currentXY[1]; w++) {
					for (int i = 0; i < 2 * configuration.getLotConfiguration().getDepthSize(); i++) {
						variation[0] = currentXY[0] + (!inverse ? i : -i);
						variation[1] = w;
						// orthogonalGradient * variation[0] + orthogonalOffset;
						if (landPointisOnMap(RadialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
							findPoint(RadialMapHelper.formKey((int) variation[0], (int) variation[1]))
									.setType(RadialConfiguration.RADIAL_ENTRANCE_MARK);
						}
					}
					countYFactor++;
					if (countYFactor == RadialConfiguration.RADIAL_ENTRANCE_SIZE) {
						break;
					}
				}
			} else if ((oldVariation != -1) && ((oldVariation - 1) > currentXY[1])) {
				// we take the reminder y that are needed for an exact answer
				for (int w = currentXY[1] + 1; w < oldVariation; w++) {
					for (int i = 0; i < 2 * configuration.getLotConfiguration().getDepthSize(); i++) {
						variation[0] = currentXY[0] + (!inverse ? i : -i);
						variation[1] = w;
						// orthogonalGradient * variation[0] + orthogonalOffset;
						if (landPointisOnMap(RadialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
							findPoint(RadialMapHelper.formKey((int) variation[0], (int) variation[1]))
									.setType(RadialConfiguration.RADIAL_ENTRANCE_MARK);
						}
					}
					countYFactor++;
					if (countYFactor == RadialConfiguration.RADIAL_ENTRANCE_SIZE) {
						break;
					}
				}
			}

			if (countYFactor == RadialConfiguration.RADIAL_ENTRANCE_SIZE) {
				break;
			}

			// We need to find the furthest
			for (int i = 0; i < 2 * configuration.getLotConfiguration().getDepthSize(); i++) {
				variation[0] = currentXY[0] + (!inverse ? i : -i);
				variation[1] = currentXY[1];
				// orthogonalGradient * variation[0] + orthogonalOffset;
				if (landPointisOnMap(RadialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
					findPoint(RadialMapHelper.formKey((int) variation[0], (int) variation[1]))
							.setType(RadialConfiguration.RADIAL_ENTRANCE_MARK);
				}
			}
			countYFactor++;
			if (countYFactor == RadialConfiguration.RADIAL_ENTRANCE_SIZE) {
				break;
			}
			oldVariation = (int) currentXY[1];
		}
	}

	private int[] createNonOrthogonalWalkRoute(Integer initialPoint, Integer finalPoint, int[] beginXY, boolean inverse,
			Double gradient) {
		// f lines are perpendicular, M1ﾃ� M2 = 竏� 1
		double offset = -gradient * beginXY[0] + beginXY[1];

		double orthogonalGradient = -1 / gradient;
		double orthogonalOffset = -orthogonalGradient * beginXY[0] + beginXY[1];

		int[] initialXY = RadialMapHelper.breakKey(initialPoint);
		int[] finalXY = RadialMapHelper.breakKey(finalPoint);
		boolean down = false;
		if (inverse) {
			if (finalXY[0] - initialXY[0] > 0) {
				// EAST
				down = true;
				beginXY[0] = finalXY[0] - RadialConfiguration.WALK_BRANCH_SIZE;
				beginXY[1] = (int) (gradient * beginXY[0] + offset);
			} else {
				// WEST
				down = false;
				beginXY[0] = initialXY[0] - RadialConfiguration.WALK_BRANCH_SIZE;
				beginXY[1] = (int) (gradient * beginXY[0] + offset);
			}
		} else {
			if (finalXY[0] - initialXY[0] > 0) {
				// EAST
				down = true;
				beginXY[0] = initialXY[0];
				beginXY[1] = (int) (gradient * beginXY[0] + offset);
			} else {
				// WEST
				down = false;
				beginXY[0] = finalXY[0];
				beginXY[1] = (int) (gradient * beginXY[0] + offset);
			}
		}

		double variation[] = new double[2];

		int[] currentXY = new int[2];
		for (int j = 0; j < RadialConfiguration.WALK_BRANCH_SIZE; j++) {
			currentXY[0] = beginXY[0] + j;
			currentXY[1] = (int) (gradient * currentXY[0] + offset);
			orthogonalOffset = -orthogonalGradient * currentXY[0] + currentXY[1];

			for (int i = 0; i < 2 * configuration.getLotConfiguration().getDepthSize(); i++) {
				variation[1] = currentXY[1] + (!down ? i : -i);
				variation[0] = (variation[1] - orthogonalOffset) / orthogonalGradient;
				if (landPointisOnMap(RadialMapHelper.formKey((int) variation[0], (int) variation[1]))) {
					findPoint(RadialMapHelper.formKey((int) variation[0], (int) variation[1]))
							.setType(RadialConfiguration.WALK_MARK);
				}
			}
		}

		// We have the full
		currentXY[0] = currentXY[0] + (!down ? 1 : -1);
		currentXY[1] = (int) (gradient * currentXY[0] + offset);
		return currentXY;
	}

	private void createRadialEntrance(int[] currentXY, int[] finalXY, int direction) {
		// NORTH then it goes toward x+
		// SOUTH toward x-
		// given that x is the same, y is our indicator for the middle
		int upperMiddle[] = new int[2];
		upperMiddle[0] = currentXY[0];
		upperMiddle[1] = ((currentXY[1] + finalXY[1]) / 2) + (RadialConfiguration.RADIAL_ENTRANCE_SIZE) / 2;

		int lowerMiddle[] = new int[2];
		lowerMiddle[0] = currentXY[0];
		lowerMiddle[1] = ((currentXY[1] + finalXY[1]) / 2) - (RadialConfiguration.RADIAL_ENTRANCE_SIZE) / 2;

		if ((direction == RadialConstants.NORTH) && (lowerMiddle[1] < currentXY[1] || upperMiddle[1] > finalXY[1]))
			return;
		else if ((direction == RadialConstants.SOUTH)
				&& (lowerMiddle[1] > currentXY[1] || upperMiddle[1] < finalXY[1]))
			return;
		createInsideRadialRoute(upperMiddle, RadialMapHelper.formKey(lowerMiddle[0], lowerMiddle[1]), direction,
				RadialConfiguration.WALK_BRANCH, configuration.getLotConfiguration().getDepthSize() * 2,
				RadialConfiguration.RADIAL_ENTRANCE_MARK);
	}

	private RadialBuilding createWalkRoute(int[] currentXY, boolean isInverse, int direction, int rotation) {
		if (isInverse) {
			return createInsideRadialRoute(currentXY,
					RadialMapHelper.moveKeyByOffsetAndDirection(RadialMapHelper.formKey(currentXY[0], currentXY[1]),
							RadialConfiguration.WALK_BRANCH_SIZE, RadialDirectionHelper.oppositeDirection(direction)),
					direction, RadialConfiguration.WALK_BRANCH, configuration.getLotConfiguration().getDepthSize() * 2,
					RadialConfiguration.WALK_MARK);
		} else {
			return createInsideRadialRoute(currentXY,
					RadialMapHelper.moveKeyByOffsetAndDirection(RadialMapHelper.formKey(currentXY[0], currentXY[1]),
							RadialConfiguration.WALK_BRANCH_SIZE, direction),
					direction, RadialConfiguration.WALK_BRANCH, configuration.getLotConfiguration().getDepthSize() * 2,
					RadialConfiguration.WALK_MARK);
		}
	}

	private boolean canBeLotized(int[] currentXY, int houseSideSize, int doublehouseDepthSize, int direction) {
		switch (direction) {
		case RadialConstants.EAST:
			for (int i = currentXY[0]; i < currentXY[0] + houseSideSize; i++) {
				for (int j = currentXY[1]; j > currentXY[1] - doublehouseDepthSize; j--) {
					if (landPointisOnMap(RadialMapHelper.formKey(i, j))) {
						String type = findPoint(RadialMapHelper.formKey(i, j)).getType();
						if (type.equals(RadialConfiguration.RADIAL_ENTRANCE_MARK)||type.equals(RadialConfiguration.OUTSIDE_POLYGON_MARK)) {
							return false;
						}
					} else {
						return false;
					}
				}
			}
			break;
		case RadialConstants.NORTH:
			for (int i = currentXY[1]; i < currentXY[1] + houseSideSize; i++) {
				for (int j = currentXY[0]; j < currentXY[0] + doublehouseDepthSize; j++) {
					if (landPointisOnMap(RadialMapHelper.formKey(j, i))) {
						String type = findPoint(RadialMapHelper.formKey(j, i)).getType();
						if (type.equals(RadialConfiguration.RADIAL_ENTRANCE_MARK)||type.equals(RadialConfiguration.OUTSIDE_POLYGON_MARK)) {
							return false;
						}
					} else {
						return false;
					}
				}
			}
			break;
		case RadialConstants.WEST:
			for (int i = currentXY[0]; i >= currentXY[0] - houseSideSize; i--) {
				for (int j = currentXY[1]; j < currentXY[1] + doublehouseDepthSize; j++) {
					if (landPointisOnMap(RadialMapHelper.formKey(i, j))) {
						String type = findPoint(RadialMapHelper.formKey(i, j)).getType();
						if (type.equals(RadialConfiguration.RADIAL_ENTRANCE_MARK)||type.equals(RadialConfiguration.OUTSIDE_POLYGON_MARK)) {
							return false;
						}
					} else {
						return false;
					}
				}
			}
			break;
		case RadialConstants.SOUTH:
			for (int i = currentXY[1]; i >= currentXY[1] - houseSideSize; i--) {
				for (int j = currentXY[0]; j > currentXY[0] - doublehouseDepthSize; j--) {
					if (landPointisOnMap(RadialMapHelper.formKey(j, i))) {
						String type = findPoint(RadialMapHelper.formKey(j, i)).getType();
						if (type.equals(RadialConfiguration.RADIAL_ENTRANCE_MARK)||type.equals(RadialConfiguration.OUTSIDE_POLYGON_MARK)) {
							return false;
						}
					} else {
						return false;
					}
				}
			}
			break;
		}
		return true;
	}

	private void createDoubleLot(int[] currentXY, int houseSideMinimunSize, int houseDepthMinimunSize, int direction,
			int serialNumber) {
		switch (direction) {
		case RadialConstants.EAST:
			for (int i = currentXY[0]; i < currentXY[0] + houseSideMinimunSize; i++) {
				for (int j = currentXY[1]; j > currentXY[1] - houseDepthMinimunSize; j--) {
					findPoint(RadialMapHelper.formKey(i, j)).setType("" + serialNumber);
				}
				for (int j = currentXY[1] - (houseDepthMinimunSize + 1); j > currentXY[1]
						- 2 * houseDepthMinimunSize; j--) {
					findPoint(RadialMapHelper.formKey(i, j)).setType("" + (serialNumber + 1));
				}
			}
			break;
		case RadialConstants.NORTH:
			for (int i = currentXY[1]; i < currentXY[1] + houseSideMinimunSize; i++) {
				for (int j = currentXY[0]; j < currentXY[0] + houseDepthMinimunSize; j++) {
					findPoint(RadialMapHelper.formKey(j, i)).setType("" + serialNumber);
				}
				for (int j = currentXY[0] + houseDepthMinimunSize + 1; j < currentXY[0]
						+ 2 * houseDepthMinimunSize; j++) {
					findPoint(RadialMapHelper.formKey(j, i)).setType("" + (serialNumber + 1));
				}
			}
			break;
		case RadialConstants.WEST:
			for (int i = currentXY[0]; i >= currentXY[0] - houseSideMinimunSize; i--) {
				for (int j = currentXY[1]; j < currentXY[1] + houseDepthMinimunSize; j++) {
					findPoint(RadialMapHelper.formKey(i, j)).setType("" + serialNumber);
				}
				for (int j = currentXY[1] + houseDepthMinimunSize + 1; j < currentXY[1]
						+ 2 * houseDepthMinimunSize; j++) {
					findPoint(RadialMapHelper.formKey(i, j)).setType("" + (serialNumber + 1));
				}
			}
			break;
		case RadialConstants.SOUTH:
			for (int i = currentXY[1]; i >= currentXY[1] - houseSideMinimunSize; i--) {
				for (int j = currentXY[0]; j > currentXY[0] - houseDepthMinimunSize; j--) {
					findPoint(RadialMapHelper.formKey(j, i)).setType("" + serialNumber);
				}
				for (int j = currentXY[0] - (houseDepthMinimunSize + 1); j > currentXY[0]
						- 2 * houseDepthMinimunSize; j--) {
					findPoint(RadialMapHelper.formKey(j, i)).setType("" + (serialNumber + 1));
				}
			}
			break;
		}
	}

	private RadialBuilding createInsideRadialRoute(int[] currentXY, int finalKey, int direction, int type, int depth,
			String markType) {
		int[] finalXY = RadialMapHelper.breakKey(finalKey);
		int lower, upper;
		RadialBuilding RadialBuilding = new RadialBuilding();

		if ((direction == RadialConstants.NORTH) || (direction == RadialConstants.SOUTH)) {
			if (currentXY[1] > finalXY[1]) {
				lower = finalXY[1];
				upper = currentXY[1];
			} else {
				lower = currentXY[1];
				upper = finalXY[1];
			}

			if (direction == RadialConstants.SOUTH) {
				for (int i = lower; i < upper; i++) {
					for (int j = currentXY[0]; j > currentXY[0] - depth; j--) {
						findPoint(RadialMapHelper.formKey(j, i)).setType(markType);
					}
				}
			} else {
				for (int i = lower; i < upper; i++) {
					for (int j = currentXY[0]; j < currentXY[0] + depth; j++) {
						findPoint(RadialMapHelper.formKey(j, i)).setType(markType);
					}
				}
			}
		} else if ((direction == RadialConstants.EAST) || (direction == RadialConstants.WEST)) {
			if (currentXY[0] > finalXY[0]) {
				lower = finalXY[0];
				upper = currentXY[0];
			} else {
				lower = currentXY[0];
				upper = finalXY[0];
			}

			if (direction == RadialConstants.EAST) {
				for (int i = lower; i < upper; i++) {
					for (int j = currentXY[1]; j > currentXY[1] - depth; j--) {
						findPoint(RadialMapHelper.formKey(i, j)).setType(markType);
					}
				}
			} else {
				for (int i = lower; i < upper; i++) {
					for (int j = currentXY[1]; j < currentXY[1] + depth; j++) {
						findPoint(RadialMapHelper.formKey(i, j)).setType(markType);
					}
				}
			}
		}

		RadialBuilding.setType(markType);
		RadialBuilding.setNumber(0);
		return RadialBuilding;
	}

	public String stringify() {
		String mapString = "";
		for (int j = 0; j < pointsy; j++) {
			String type = getLandPoint(RadialMapHelper.formKey(0, j)).getType();
			int repetitions = 1;
			for (int i = 0; i < pointsx; i++) {
				if (type.equals(getLandPoint(RadialMapHelper.formKey(i, j)).getType())) {
					repetitions++;
				} else {
					mapString += type + "" + repetitions + ",";
					repetitions = 1;
					type = getLandPoint(RadialMapHelper.formKey(i, j)).getType();
				}
			}
			mapString += type + "" + repetitions + ",";
			mapString += ".";
		}
		return mapString;
	}
	
	//TODO new Grammar, testing resulting vectors
	public String getGrammar() {
		String mapString = "";
		for (int j = 0; j < pointsy; j++) {
			for (int i = 0; i < pointsx; i++) {
				String type = getLandPoint(RadialMapHelper.formKey(i, j)).getGramaticalType();
				if (type != null) {
					mapString += type + ",";
				}
			}
		}
		return mapString;// remove last ,
	}
	
	public void fillCentralPark() {
		for (int x = 0; x < pointsx; x++) {
			int count = 0;
			int pInitialLimit = -1;
			boolean reversed = false;

			for (int y = 0; y < pointsy; y++) {
				if (getLandPoint(RadialMapHelper.formKey(x, y)).getType() == RadialConstants.POLYGON_LIMIT) {
					count++;
					pInitialLimit = pInitialLimit == -1 ? y : pInitialLimit;
				}

				if (count==2) {
					if (!reversed) {
						for (int w = pInitialLimit + 1; w < y; w++) {
							getLandPoint(RadialMapHelper.formKey(x, w)).setType(RadialConfiguration.PARK_MARK);
						}
						reversed = true;
					} 
					break;
				}
			}
		}
	}

	
}
