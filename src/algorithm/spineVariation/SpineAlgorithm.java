package algorithm.spineVariation;

import java.util.List;
import helpers.spineVariation.SpineDirectionHelper;
import helpers.spineVariation.SpineMapHelper;
import interfaces.spineVariation.SpineConfiguration;
import interfaces.spineVariation.SpineConstants;
import models.spineVariation.SpineLandMap;
import models.spineVariation.SpineLandPoint;
import models.spineVariation.SpineLandRoute;

public class SpineAlgorithm {
	public SpineLandMap landMap;
	public static int xx;
	public static int yy;
	public static int width;
	public static int large;
	
	public SpineLandMap getLandMap() {
		return landMap;
	}

	public void setLandMap(SpineLandMap landMap) {
		this.landMap = landMap;
	}
	public void setEntryX(int x) {
		this.xx = x;
	}
	public void setEntryY(int y) {
		this.yy = y;
	}
	public void setLarge(int large) {
		this.large = large;
	}
	
	public  void clusterize() {
		// 1. we need to now the main route size
		SpineLandRoute mainRoute = landMap.getLandRoute();
		int entryPointId = mainRoute.getInitialPointId();
		// Once the collector branches are created we need to create the non
		// collector running orthogonal to the main
		List<Integer> orthogonalDirections = SpineDirectionHelper.orthogonalDirections(mainRoute.getDirection());
		while (true) {
			//
			entryPointId = SpineMapHelper.moveKeyByOffsetAndDirection(entryPointId,
					2*SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE + SpineConfiguration.COLLECTOR_BRANCH_SIZE,
					mainRoute.getDirection());
			if (landMap.landPointisOnMap(entryPointId) && landMap.intersectMainRoute(entryPointId)) {
				createRouteVariation(entryPointId, orthogonalDirections.get(0), SpineConfiguration.COLLECTOR_BRANCH);
			} else
				break;
		}
		
		//Principal routes
		int upperParallelId = mainRoute.getInitialPointId();
		int[] key = SpineMapHelper.breakKey(upperParallelId);
		key[0] = 1;
		upperParallelId = SpineMapHelper.formKey(key[0], key[1]);
		while (true) {
			upperParallelId = SpineMapHelper.moveKeyByOffsetAndDirection(upperParallelId,
					SpineConfiguration.BASE_CLUSTER_SIZE, orthogonalDirections.get(0));
			if (!landMap.landPointisOnMap(upperParallelId))
				break;
			createRouteVariation(upperParallelId, mainRoute.getDirection(), SpineConfiguration.LOCAL_BRANCH);
		}

		int lowerParallelId = mainRoute.getInitialPointId();
		key = SpineMapHelper.breakKey(lowerParallelId);
		key[0] = 1;
		lowerParallelId = SpineMapHelper.formKey(key[0], key[1]);
		while (true) {
			lowerParallelId = SpineMapHelper.moveKeyByOffsetAndDirection(lowerParallelId,
					SpineConfiguration.BASE_CLUSTER_SIZE, orthogonalDirections.get(1));
			if (!landMap.landPointisOnMap(lowerParallelId))
				break;
			createRouteVariation(lowerParallelId, mainRoute.getDirection(), SpineConfiguration.LOCAL_BRANCH);
		}
	}
	
	public  void createRouteVariation(int axisPoint, int direction, int branchType) {
		int extension = 0;
		String markType = "";
		int growDirection = -1;
		SpineLandRoute clusterLandRoute = null;
		switch (branchType) {
		case SpineConfiguration.ARTERIAL_BRANCH:
			clusterLandRoute = new SpineLandRoute();
			clusterLandRoute.setInitialPointId(axisPoint);
			clusterLandRoute.setDirection(direction);
			extension = SpineConfiguration.ARTERIAL_BRANCH_SIZE;
			markType = SpineConfiguration.ARTERIAL_MARK;
			break;
		case SpineConfiguration.COLLECTOR_BRANCH:
			extension = SpineConfiguration.COLLECTOR_BRANCH_SIZE;
			markType = SpineConfiguration.COLLECTOR_MARK;
			break;
		case SpineConfiguration.LOCAL_BRANCH:
			extension = SpineConfiguration.LOCAL_BRANCH_SIZE;
			markType = SpineConfiguration.LOCAL_MARK;
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
				clusterLandRoute.setFinalPointId(finalPointid);
				landMap.setLandRoute(clusterLandRoute);
			} else {
				createLine(SpineMapHelper.moveKeyByOffsetAndDirection(axisPoint, i, growDirection), direction,
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
		SpineLandPoint clusterLandPoint = landMap.findPoint(SpineMapHelper.formKey(newXY[0], newXY[1]));
		if (!in.booleanValue() && !clusterLandPoint.isMapLimit()) {
			changed = true;
			in = true;
		}

		if (in.booleanValue() && clusterLandPoint.isMapLimit()) {
			changed = true;
		}

		if (in.booleanValue()) {
			if (markType.equals(SpineConfiguration.NODE_MARK)) {
				if (clusterLandPoint.getType().equals(SpineConfiguration.EMPTY_MARK)) {
					clusterLandPoint.setType(markType);
				}
			} else {
				if (clusterLandPoint.getType().equals(SpineConfiguration.EMPTY_MARK)
						|| clusterLandPoint.getType().equals(SpineConfiguration.NODE_MARK)) {
					clusterLandPoint.setType(markType);
				}
			}
		}
		return changed;
	}
	/**TODO here begins the lotization part of the algorithm
	 * */
	public void zonify() {
		// findZonificationAreas();
		organicZonification();
		
	}
	private void organicZonification() {
		
		for (int j = landMap.getPointsy() - 1; j >= 0; j--) {
			for (int i = 0; i < landMap.getPointsx(); i++) {				
				if (landMap.findPoint(SpineMapHelper.formKey(i, j)).getType()
						.equals(SpineConfiguration.NODE_MARK)) 
					landMap.getLandPoint(SpineMapHelper.formKey(i,j)).setType(SpineConfiguration.EMPTY_MARK);
				//if(i== xx && j==yy)spineLandMap.getLandPoint(SpineMapHelper.formKey(i,j)).setType(SpineConfiguration.NODE_MARK);
			}
		}
		//getting first index
		int indice=0;
		
		for(int i=0;i<large;i++){
			//caso 1 
			if(landMap.findPoint(SpineMapHelper.formKey(i,yy)).getType().equals(SpineConfiguration.COLLECTOR_MARK)){
				i+=16;
				indice=i;
				break;//we find the first 'a' in the arterial branch
			}
		}
		int tp1,tp2,tp3,tp4;
		tp3=tp4=0;
		String etq1,etq2;
		etq1=SpineConfiguration.MARK_LOT1+"";
		etq2=SpineConfiguration.MARK_LOT2+"";
		int cambio=0;
		boolean key=true;
		
		//parte inferior 
		for(int i=0;i<large;i++){
		tp1=0;
		 tp2=0;
		 int counterFive=0;
		 cambio++;
		 key=true;
		 tp3=0;
		 tp4=0;
		 int j=0;
		 while(true){
			 if(landMap.findPoint(SpineMapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
					 landMap.findPoint(SpineMapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
			 if(landMap.findPoint(SpineMapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.EMPTY_MARK)){
				 int div =(j)/Integer.parseInt(SpineConfiguration.BASE_CLUSTER_SIZE+"");
				 if(div>=1 && j>=Integer.parseInt(SpineConfiguration.BASE_CLUSTER_SIZE+"") && (j%Integer.parseInt(SpineConfiguration.BASE_CLUSTER_SIZE+""))==0 ){
					 System.out.print("valo j: ");
					 System.out.println(j);
					 System.out.print("valo div: ");
					 System.out.println(div);
					 if(landMap.findPoint(SpineMapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.POLYGON_BORDER)||
							 landMap.findPoint(SpineMapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
					 //if(spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
							// spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
					 for(int k=0;k<10;k++){
						 if(landMap.findPoint(SpineMapHelper.formKey(i,yy-j-k)).getType().equals(SpineConfiguration.POLYGON_BORDER)||
								 landMap.findPoint(SpineMapHelper.formKey(i,yy-j-k)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
						 landMap.getLandPoint(SpineMapHelper.formKey(i,yy-j-k)).setType("a");	
						 //j++;
					 }
					 j+=10;
					 if(landMap.findPoint(SpineMapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
							 landMap.findPoint(SpineMapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
					 for(int l=0;l<27;l++){
						 if(landMap.findPoint(SpineMapHelper.formKey(i,yy-j-l)).getType().equals(SpineConfiguration.POLYGON_BORDER)||
								 landMap.findPoint(SpineMapHelper.formKey(i,yy-j-l)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
						 //if(spineLandMap.findPoint(MapHelper.formKey(i,yy-j-l)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
							//	 spineLandMap.findPoint(MapHelper.formKey(i,yy-j-l)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
						 landMap.getLandPoint(SpineMapHelper.formKey(i,yy-j-l)).setType("p");
						 //j++;
					 }
					 j+=27;
					 if(landMap.findPoint(SpineMapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
							 landMap.findPoint(SpineMapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
					 
					// spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy-j)).setType("p");
					 //continue;
				 }
				 if(tp3<27){
					 //este caso es obligatorio si o si
						landMap.getLandPoint(SpineMapHelper.formKey(i,yy-j)).setType("p");	
						tp3++;
						//if(tp3==6)tp2=0;
					}else if(tp4<10){
						landMap.getLandPoint(SpineMapHelper.formKey(i,yy-j)).setType("a");	
						tp4++;
					}
				 	else if(tp1<6){
				 		landMap.getLandPoint(SpineMapHelper.formKey(i,yy-j)).setType(etq1);	
						tp1++;
						if(tp2==6)tp2=0;
					}else {
						landMap.getLandPoint(SpineMapHelper.formKey(i,yy-j)).setType(etq2);	
						tp2++;
						if(tp2==6)tp1=0;
					}
				 
			 }
			 j++;
			 if(landMap.findPoint(SpineMapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
					 landMap.findPoint(SpineMapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
			 
		 }
		
	}
		//parte superior
		tp3=tp4=0;

		for(int i=0;i<large;i++){
			 tp1=0;
			 tp2=0;
			 int counterFive=0;
			 cambio++;
			 key=true;
			 tp3=0;
			 tp4=0;
			 int j=0;
			 while(true){
				 if(landMap.findPoint(SpineMapHelper.formKey(i,yy+j)).getType().equals(SpineConfiguration.POLYGON_BORDER) 
						 ||landMap.findPoint(SpineMapHelper.formKey(i,yy+j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
				 
				 if(landMap.findPoint(SpineMapHelper.formKey(i,yy+j)).getType().equals(SpineConfiguration.EMPTY_MARK)){
					 int div =(j)/180;
					 if(div>=1 && j>=180 && (j%180)==0 ){
						 System.out.print("valo j: ");
						 System.out.println(j);
						 System.out.print("valo div: ");
						 System.out.println(div);
						 if(landMap.findPoint(SpineMapHelper.formKey(i,yy+j)).getType().equals(SpineConfiguration.POLYGON_BORDER)||
								 landMap.findPoint(SpineMapHelper.formKey(i,yy+j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
						 //if(spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
							//	 spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
						 for(int k=0;k<10;k++){
							 if(landMap.findPoint(SpineMapHelper.formKey(i,yy+j+k)).getType().equals(SpineConfiguration.POLYGON_BORDER)||
									 landMap.findPoint(SpineMapHelper.formKey(i,yy+j+k)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
							 landMap.getLandPoint(SpineMapHelper.formKey(i,yy+j+k)).setType("a");	
							 //j++;
						 }
						 j+=10;
						 if(landMap.findPoint(SpineMapHelper.formKey(i,yy+j)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
								 landMap.findPoint(SpineMapHelper.formKey(i,yy+j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
						 for(int l=0;l<27;l++){
							 if(landMap.findPoint(SpineMapHelper.formKey(i,yy+j+l)).getType().equals(SpineConfiguration.POLYGON_BORDER)||
									 landMap.findPoint(SpineMapHelper.formKey(i,yy+j+l)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
							 //if(spineLandMap.findPoint(MapHelper.formKey(i,yy-j-l)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
								//	 spineLandMap.findPoint(MapHelper.formKey(i,yy-j-l)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
							 landMap.getLandPoint(SpineMapHelper.formKey(i,yy+j+l)).setType("p");
							 //j++;
						 }
						 j+=27;
						 if(landMap.findPoint(SpineMapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
								 landMap.findPoint(SpineMapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
						// r
						// spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy-j)).setType("p");
						 //continue;
					 }
					 	if(tp3<27){
					 		 //este caso es obligatorio si o si
					 		landMap.getLandPoint(SpineMapHelper.formKey(i,yy+j)).setType(SpineConfiguration.PARK_MARK);	
					 		//spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy+j)).setType(valuePrint);	
							tp3++;
							//if(tp3==27)valuePrint="a";
						}else if(tp4<10){
							landMap.getLandPoint(SpineMapHelper.formKey(i,yy+j)).setType("a");
							
							//spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy+j)).setType(valuePrint);
							tp4++;
						}else if(tp1<6){
							landMap.getLandPoint(SpineMapHelper.formKey(i,yy+j)).setType(etq1);
							tp1++;
							if(tp2==6){
								tp2=0;
							}
						}else {
							landMap.getLandPoint(SpineMapHelper.formKey(i,yy+j)).setType(etq2);	
							tp2++;
							if(tp2==6){
								tp1=0;
							}
						}
						
					}
				 j++;
				 if(landMap.findPoint(SpineMapHelper.formKey(i,yy+j)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
						 landMap.findPoint(SpineMapHelper.formKey(i,yy+j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
				 
				
			 }
			
		}
		
		
	}
}
