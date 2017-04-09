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
	public static int indice;
	public static int width;
	public static int large;
	public static int pointStart;
	public static int nmbrParksSpine;
	public String out1,out2,out11,out22,aux,aux2;
	
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
	public void setWidth(int width) {
		this.width = width;
	}
	public  void clearDotsSpine() {
		for (int j = landMap.getPointsy() - 1; j >= 0; j--) {
			for (int i = 0; i < landMap.getPointsx(); i++) {				
				if (landMap.findPoint(SpineMapHelper.formKey(i, j)).getType()
						.equals(SpineConfiguration.POLYGON_BORDER)) 
					landMap.getLandPoint(SpineMapHelper.formKey(i,j)).setType(SpineConfiguration.EMPTY_MARK);
				//if(i== xx && j==yy)spineLandMap.getLandPoint(SpineMapHelper.formKey(i,j)).setType(SpineConfiguration.NODE_MARK);
			}
		}
		
	}
	public  void spineize() {
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
		//clearBorderByEmptyMark();
		//clearNodeByEmptyMark();
		organicZonification();
	}
	private void organicZonification() {
		 indice=0;		
		for(int i=0;i<large;i++){
			//caso 1 
			if(landMap.findPoint(SpineMapHelper.formKey(i,yy)).getType().equals(SpineConfiguration.COLLECTOR_MARK)){
				i+=16;
				indice=i;
				break;//we find the first 'a' in the arterial branch
			}
		}
		//inferior part
		//inferioSuperiorMark(indice,-1);
		//superior parte
		//inferioSuperiorMark(indice,1);
		System.out.print("yy = ");
		System.out.println(yy);
		
		System.out.print("landMap.getPointsy()/2 = ");
		System.out.println(landMap.getPointsy()/2);
		if(yy<(landMap.getPointsy()/2)){
			//park start up
			System.out.println("Working Up");
			printFirstLineParkUp();
		}else{
			//park start down
			System.out.println("Working Down");
			printFirstLineParkDown();
		}
	}
	private void clearBorderByEmptyMark(){
		for (int j = landMap.getPointsy() - 1; j >= 0; j--) {
			for (int i = 0; i < landMap.getPointsx(); i++) {				
				if (landMap.findPoint(SpineMapHelper.formKey(i, j)).getType()
						.equals(SpineConfiguration.POLYGON_BORDER)) 
					landMap.getLandPoint(SpineMapHelper.formKey(i,j)).setType(SpineConfiguration.EMPTY_MARK);
				//if(i== xx && j==yy)spineLandMap.getLandPoint(SpineMapHelper.formKey(i,j)).setType(SpineConfiguration.NODE_MARK);
			}
		}
		
	}
	private void clearNodeByEmptyMark(){
		for (int j = landMap.getPointsy() - 1; j >= 0; j--) {
			for (int i = 0; i < landMap.getPointsx(); i++) {				
				if (landMap.findPoint(SpineMapHelper.formKey(i, j)).getType()
						.equals(SpineConfiguration.NODE_MARK)) 
					landMap.getLandPoint(SpineMapHelper.formKey(i,j)).setType(SpineConfiguration.EMPTY_MARK);
			}
		}
		
	}
	//recieve two arguments determines where put the 1st file of parks
	
	private void printFirstLineParkDown(){
		System.out.println("Down");
		//find star position
		nmbrParksSpine=(int)(large-xx-16)/300;
		System.out.println(nmbrParksSpine);
		int value=0;
		while(true){
			if(landMap.findPoint(SpineMapHelper.formKey(xx+value,yy-1)).getType().equals(SpineConfiguration.COLLECTOR_MARK))break;
			value++;
		}
		pointStart=xx+value+16;//park start
		
		//start print parks
		int j=pointStart;
		boolean state=false;
		boolean state2=false;
		int counter=0;
		while(true){
			//print park
			for(int k=0;k<SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE*2;k++){
				for(int i=0;i<(SpineConfiguration.MIN_SIDE_PARK_BY_HOUSE + SpineConfiguration.LOCAL_BRANCH_SIZE);i++){
					if(landMap.findPoint(SpineMapHelper.formKey(j+k,yy-1-i)).getType().equals(SpineConfiguration.POLYGON_BORDER)){
						/*System.out.println("We out!");
						System.out.println("We out!");
						System.out.println("We out!");
						System.out.println("We out!");*/
						state=true;
						//break;
					}else {
						if(i<SpineConfiguration.MIN_SIDE_PARK_BY_HOUSE)
							landMap.getLandPoint(SpineMapHelper.formKey(j+k,yy-1-i)).setType(SpineConfiguration.PARK_MARK);
						else
							landMap.getLandPoint(SpineMapHelper.formKey(j+k,yy-1-i)).setType(SpineConfiguration.LOCAL_MARK);
					}
				}
				//if(state)break;
			}
			
			/*for(int aa=0;aa<6;aa++){
				for(int bb=0;bb<15;bb++){
					
				}
			}*/
			counter++;
			if(counter>nmbrParksSpine)break;
			System.out.println("EVALUATION AFTER PRINT NEXT PARK");
			
			for(int i=0;i<300;i++){
				if(landMap.findPoint(SpineMapHelper.formKey(j+i,yy-1)).getType().equals(SpineConfiguration.POLYGON_BORDER)){
					state2=true;
					break;
				}else j++;
				//System.out.println(landMap.findPoint(SpineMapHelper.formKey(j+i,yy-1)).getType());
				if(landMap.findPoint(SpineMapHelper.formKey(j+i,yy-1)).getType().equals(SpineConfiguration.POLYGON_BORDER))break;
			}
			if(state2)break;
			else if(landMap.findPoint(SpineMapHelper.formKey(j,yy-1)).getType().equals(SpineConfiguration.NODE_MARK)){
				System.out.println("last caracter was n");
				//comeback to find the first 'n'
				int x=0;
				while(true){
					if(landMap.findPoint(SpineMapHelper.formKey(j-x,yy-1)).getType().equals(SpineConfiguration.ARTERIAL_MARK)){
						j-=x;
						j++;
						break;
					}
					x++;
				}
			}else if(landMap.findPoint(SpineMapHelper.formKey(j,yy-1)).getType().equals(SpineConfiguration.ARTERIAL_MARK)){
				System.out.println("last caracter was a");
				int x=0;
				while(true){
					if(landMap.findPoint(SpineMapHelper.formKey(j-x,yy-1)).getType().equals(SpineConfiguration.NODE_MARK)){
						j-=x;
						j++;
						break;
					}
					x++;
				}
			}
		}
	
	}
	private void printFirstLineParkUp(){
		System.out.println("Up");
		//find star position
		nmbrParksSpine=(int)(large-xx-16)/300;
		System.out.println(nmbrParksSpine);
		int value=0;
		while(true){
			if(landMap.findPoint(SpineMapHelper.formKey(xx+value,yy+27)).getType().equals(SpineConfiguration.COLLECTOR_MARK))break;
			value++;
		}
		System.out.print("VALOR DE X ES: ");
		System.out.println(xx+value);
		System.out.print("VALOR DE Y ES: ");
		System.out.println(yy+26);
		pointStart=xx+value+16;//park start
		//landMap.getLandPoint(SpineMapHelper.formKey(xx+value,yy+26)).setType("x");
		
		//start print parks
		int j=pointStart;
		boolean state=false;
		boolean state2=false;
		int counter=0;
		
				
		while(true){
			//print park
			for(int k=0;k<SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE*2;k++){
				for(int i=0;i<(SpineConfiguration.MIN_SIDE_PARK_BY_HOUSE + SpineConfiguration.LOCAL_BRANCH_SIZE);i++){
					if(landMap.findPoint(SpineMapHelper.formKey(j+k,yy+26+i)).getType().equals(SpineConfiguration.POLYGON_BORDER)){
						System.out.println("We out!");
						state=true;
						break;
					}else {
						if(i<SpineConfiguration.MIN_SIDE_PARK_BY_HOUSE)
							landMap.getLandPoint(SpineMapHelper.formKey(j+k,yy+26+i)).setType(SpineConfiguration.PARK_MARK);
						else
							landMap.getLandPoint(SpineMapHelper.formKey(j+k,yy+26+i)).setType(SpineConfiguration.LOCAL_MARK);
					}
					
				}
				if(state)break;
			}
			//we print building on park
			int count=0;
			int six=0;
			aux2="";
			out11="1";
			out22="2";
			//comentar while
			while(true){
				if(landMap.findPoint(SpineMapHelper.formKey(j,37+yy+26+six)).getType().equals(SpineConfiguration.POLYGON_BORDER))break;
				for(int iii=0;iii<6;iii++){
					
					if(landMap.findPoint(SpineMapHelper.formKey(j,37+yy+26+six)).getType().equals(SpineConfiguration.ARTERIAL_MARK)){
						six+=10;
						break;
					}
					for(int jjj=0;jjj<15;jjj++){
							landMap.getLandPoint(SpineMapHelper.formKey(j+jjj,37+yy+26+six)).setType(out11);
							landMap.getLandPoint(SpineMapHelper.formKey(j+jjj+15,37+yy+26+six)).setType(out22);
						}
					six++;
					}
				if(landMap.findPoint(SpineMapHelper.formKey(j,37+yy+26+six)).getType().equals(SpineConfiguration.POLYGON_BORDER))break;
				aux2=out11;
					out11=out22;
				out22=aux2;
			}

			counter++;
			if(counter>nmbrParksSpine)break;
			System.out.println("EVALUATION AFTER PRINT NEXT PARK");
			//DO NOT TOUCH
			for(int i=0;i<300;i++){
				if(landMap.findPoint(SpineMapHelper.formKey(j+i,yy+26)).getType().equals(SpineConfiguration.POLYGON_BORDER)){
					state2=true;
					break;
				}else j++;
				//System.out.println(landMap.findPoint(SpineMapHelper.formKey(j+i,yy-1)).getType());
				if(landMap.findPoint(SpineMapHelper.formKey(j+i,yy+26)).getType().equals(SpineConfiguration.POLYGON_BORDER))break;
			}
			if(state2)break;
			
			else if(landMap.findPoint(SpineMapHelper.formKey(j,yy+26)).getType().equals(SpineConfiguration.NODE_MARK)){
				System.out.println("last caracter was n");
				//comeback to find the first 'n'
				int x=0;
				while(true){
					if(landMap.findPoint(SpineMapHelper.formKey(j-x,yy+26)).getType().equals(SpineConfiguration.ARTERIAL_MARK)){
						j-=x;
						j++;
						break;
					}
					x++;
				}
					
				
			}else if(landMap.findPoint(SpineMapHelper.formKey(j,yy+26)).getType().equals(SpineConfiguration.ARTERIAL_MARK)){
				System.out.println("last caracter was a");
				int x=0;
				while(true){
					if(landMap.findPoint(SpineMapHelper.formKey(j-x,yy+26)).getType().equals(SpineConfiguration.NODE_MARK)){
						j-=x;
						j++;
						break;
					}
					x++;
				}
					
				
			}
			
				
			
			
		}
		//print blocks
		
		int starBlockyAfterPark=yy+26;
		int counterBlock=46;
		boolean out=false;
		int xxx=0;
		out1="1";
		out2="2";
		aux="";
		while(true){
			//every loop for a extend block
			if(landMap.findPoint(SpineMapHelper.formKey(pointStart+counterBlock,starBlockyAfterPark)).getType().equals(SpineConfiguration.NODE_MARK)){
				System.out.print("X = ");
				System.out.println(pointStart+counterBlock);
				System.out.print("Y = ");
				System.out.println(starBlockyAfterPark);
				landMap.getLandPoint(SpineMapHelper.formKey(pointStart+counterBlock,starBlockyAfterPark)).setType("X");
				
				//replace node by X
				//landMap.getLandPoint(SpineMapHelper.formKey(pointStart+counterBlock,starBlockyAfterPark)).setType("X");
				int way_out=0;
				int plus6=0;
				boolean stateInside=false;
				while(true){
					if(landMap.findPoint(SpineMapHelper.formKey(pointStart+counterBlock,plus6 +starBlockyAfterPark)).getType().equals(SpineConfiguration.POLYGON_BORDER)  ||
									landMap.findPoint(SpineMapHelper.formKey(pointStart+counterBlock,plus6+starBlockyAfterPark)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
					else if(landMap.findPoint(SpineMapHelper.formKey(pointStart+counterBlock,plus6+starBlockyAfterPark)).getType().equals(SpineConfiguration.ARTERIAL_MARK)){
							
								for(int aa=0;aa<8;aa++){
									plus6++;
									if(landMap.findPoint(SpineMapHelper.formKey(pointStart+counterBlock,plus6+starBlockyAfterPark)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
								}
							
							
					}
					for(int ii=0;ii<6;ii++){
						if(landMap.findPoint(SpineMapHelper.formKey(pointStart+counterBlock,starBlockyAfterPark+ii+plus6)).getType().equals(SpineConfiguration.ARTERIAL_MARK))break;
						for(int jj=0;jj<15;jj++){						
							if(!landMap.findPoint(SpineMapHelper.formKey(pointStart+counterBlock+ jj,starBlockyAfterPark)).getType().equals(SpineConfiguration.POLYGON_BORDER) &&
									!landMap.findPoint(SpineMapHelper.formKey(pointStart+counterBlock+ jj,starBlockyAfterPark)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK ) && 
									!landMap.findPoint(SpineMapHelper.formKey(pointStart+counterBlock+ jj,starBlockyAfterPark)).getType().equals(SpineConfiguration.ARTERIAL_MARK ))
								landMap.getLandPoint(SpineMapHelper.formKey(pointStart+counterBlock+jj,starBlockyAfterPark+ii+plus6)).setType(out1); 
							if(!landMap.findPoint(SpineMapHelper.formKey(pointStart+counterBlock+jj+15,starBlockyAfterPark)).getType().equals(SpineConfiguration.POLYGON_BORDER) && 
									!landMap.findPoint(SpineMapHelper.formKey(pointStart+counterBlock+jj+15,starBlockyAfterPark)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK) && 
									!landMap.findPoint(SpineMapHelper.formKey(pointStart+counterBlock+ jj,starBlockyAfterPark)).getType().equals(SpineConfiguration.ARTERIAL_MARK ))						
								landMap.getLandPoint(SpineMapHelper.formKey(pointStart+counterBlock+jj+15,starBlockyAfterPark+ii+plus6)).setType(out2);
						}
					}
					//swap values printables
					aux=out1;
					out1=out2;
					out2=aux;
					plus6+=6;
					way_out++;
					//if(way_out>15)break;
				}
				
				//break;
			}
			//DONT TOUCH
			for(int jj=0;jj<46;jj++){
				counterBlock++;
				if(landMap.findPoint(SpineMapHelper.formKey(pointStart+counterBlock,starBlockyAfterPark)).getType().equals(SpineConfiguration.POLYGON_BORDER)){
					out=true;
					break;
				}
			}
			//verify 30 'l' for block
			//DONT TOUCH
			int c_30=1;
			while(!landMap.findPoint(SpineMapHelper.formKey(pointStart+counterBlock+c_30,starBlockyAfterPark)).getType().equals(SpineConfiguration.POLYGON_BORDER)){
				c_30++;
				if(c_30==30)break;
			}
			if(c_30<30)break;//IF THE FINAL BLOCK DONT SIZE 30 WIDTH
		}
			
	}


}
