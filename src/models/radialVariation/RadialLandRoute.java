package models.radialVariation;

import interfaces.radialVariation.RadialConfiguration;
import interfaces.radialVariation.RadialConstants;
import java.util.ArrayList;
import java.util.List;

import helpers.radialVariation.RadialMapHelper;


public class RadialLandRoute {
	private int initialPointId = -1;
	private int finalPointId = -1;
	private int direction = RadialConstants.ORTHOGONAL;
	private String type = "";
	
	public RadialLandRoute(int ini,int fin,String typ){
		setInitialPointId(ini);
		setFinalPointId(fin);
		int direct= findDirect(ini,fin);
		if(direct ==3)
		setDirection(RadialConstants.ORTHOGONAL);
		if(direct == 2)
		setDirection(RadialConstants.VERTICAL);
		if(direct ==1)
		setDirection(RadialConstants.HORIZONTAL);
		
		setType(typ);
	}
	
	public RadialLandRoute() {
		// TODO Auto-generated constructor stub
	}

	public int getInitialPointId() {
		return initialPointId;
	}
	public void setInitialPointId(int initialPointId) {
		this.initialPointId = initialPointId;
	}
	public int getDirection() {
		return direction;
	}
	public void setDirection(int direction) {
		this.direction = direction;
	}
	public int getFinalPointId() {
		return finalPointId;
	}
	public void setFinalPointId(int finalPointId) {
		this.finalPointId = finalPointId;
	}
	
	public String getType(){
		return type;
	}
	public void setType(String val){
		this.type = val;
	}
	
	private int findDirect(int ini, int end){
		int value=3;
		int xyInitial[] = RadialMapHelper.breakKey(ini);
		int xyFinal[] = RadialMapHelper.breakKey(end);
		if(xyInitial[0] == xyFinal[0]) value = 2;
		if(xyInitial[1] == xyFinal[1]) value = 1;
		return value;
	}
	
	public List<List<RadialLandRoute>> setRadialRoutes(List<List<Integer>> layers, List<Integer> vertix,int MainRouteIni,int MainRouteEnd){
		List<List<RadialLandRoute>> auxArr = new ArrayList<>();
		
		///for any layer will be n-routes, the layers of routes are 2,5,8... and n could be change then we need to verify but we save in vertix this points
		//
		for(int i = 2;i < layers.size(); i=i+3){
			List<RadialLandRoute> routesLayer = new ArrayList<>();
			for(int j = 0; j < layers.get(i).size(); j++){
				int ini= (layers.get(i).get(j%(layers.get(i).size())));
				int end= (layers.get(i).get((j+1)%(layers.get(i).size())));
				RadialLandRoute routes = new RadialLandRoute(ini,end,RadialConfiguration.LOCAL_MARK);
				routesLayer.add(routes);
			}
			auxArr.add(routesLayer);
		}
		//here we have the layers routes now we need the vertix layers
		List<RadialLandRoute> routesLayer = new ArrayList<>();
		for(int k=0; k < vertix.size(); k=k+2){
			RadialLandRoute routes = new RadialLandRoute(vertix.get(k),vertix.get(k+1),RadialConfiguration.COLLECTOR_MARK);
			routesLayer.add(routes);
		}
		//and finally ad the main route
		RadialLandRoute routes = new RadialLandRoute(MainRouteIni,MainRouteEnd,RadialConfiguration.ARTERIAL_MARK);
		routesLayer.add(routes);
		auxArr.add(routesLayer);
		
	return auxArr;	
	}
	
}
