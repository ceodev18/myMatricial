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
	public static int nmbrParksLong;
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
		setLongPark();
		organicZonification();
	}
	private void setLongPark(){
		int total =48+3*(2*SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE)*SpineConfiguration.BASE_CLUSTER_SIZE;
		int eightPorc=(total/100)*10;
		int result =eightPorc/(2*SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE);
		nmbrParksLong =result;
		System.out.println("nmbrParksLong");
		System.out.println(nmbrParksLong);
		
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
		setPointStart();
		printParksSideArterial();
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
	private void setPointStart(){
		nmbrParksSpine=(int)(large-xx)/SpineConfiguration.SEPARATION_PARK_SIZE;
		System.out.println(nmbrParksSpine);
		int value=0;
		while(true){
			if(landMap.findPoint(SpineMapHelper.formKey(xx+value,yy+27)).getType().equals(SpineConfiguration.COLLECTOR_MARK))break;
			value++;
		}
		pointStart=xx+value+16;//park start
		
		
	}
	private void printParksSideArterial(){
		//start print parks
		int j=pointStart;
		boolean state=false;
		boolean state2=false;
		boolean stateOutPark=false;
		int counter=0;	
		while(true){
			//changing way to print 
			System.out.println("New set of parks");
			int breakPrintingPark=SpineConfiguration.BASE_CLUSTER_SIZE;
			stateOutPark=false;
			while(true){
				String comparable=landMap.findPoint(SpineMapHelper.formKey(j,yy+breakPrintingPark)).getType();
				if(comparable.equals(" ")||comparable.equals("."))break;
				for(int k=SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE*2;k>0;k--){
					for(int i=0;i<(nmbrParksLong + SpineConfiguration.LOCAL_BRANCH_SIZE);i++){
						String insideCompare=landMap.findPoint(SpineMapHelper.formKey(j+k-1,yy+breakPrintingPark-i-1)).getType();
						if(insideCompare.equals(SpineConfiguration.POLYGON_BORDER)){
							state=true;
							break;
						}else {
							if(i<nmbrParksLong)
								landMap.getLandPoint(SpineMapHelper.formKey(j+k-1,yy+breakPrintingPark-i-1)).setType(SpineConfiguration.PARK_MARK);
							else
								landMap.getLandPoint(SpineMapHelper.formKey(j+k-1,yy+breakPrintingPark-i-1)).setType(SpineConfiguration.LOCAL_MARK);
						}
						
					}
				}
				
				//it works good
				//DO NOT change
				for(int i=0;i<SpineConfiguration.BASE_CLUSTER_SIZE;i++){
					breakPrintingPark++;
					String comparableInside=landMap.findPoint(SpineMapHelper.formKey(j,yy+breakPrintingPark)).getType();
					if(comparableInside.equals(" ")||comparableInside.equals(".")){
						stateOutPark=true;
						break;
					}
				}
				if(stateOutPark)break;
			}
		//	landMap.getLandPoint(SpineMapHelper.formKey(j,yy+26)).setType("X");
			int verticalStop=yy-1;
			boolean outNowbyPark=false;
			//landMap.getLandPoint(SpineMapHelper.formKey(j,verticalStop)).setType("X");
			while(true){
				int checker=0;
				for(int k=0;k<SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE*2;k++){
					for(int i=0;i<(nmbrParksLong + SpineConfiguration.LOCAL_BRANCH_SIZE);i++){
						String insideCompare=landMap.findPoint(SpineMapHelper.formKey(j+k,verticalStop-i)).getType();
						if(insideCompare.equals(SpineConfiguration.POLYGON_BORDER)){
							state=true;
							break;
						}else {
							if(i<nmbrParksLong)
								landMap.getLandPoint(SpineMapHelper.formKey(j+k,verticalStop-i)).setType(SpineConfiguration.PARK_MARK);
							else
								landMap.getLandPoint(SpineMapHelper.formKey(j+k,verticalStop-i)).setType(SpineConfiguration.LOCAL_MARK);
						}
					}
					checker++;
				}
				if(checker==SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE*2)verticalStop-=nmbrParksLong+10;
				while(true){
					verticalStop--;
					String compareValue=landMap.getLandPoint(SpineMapHelper.formKey(j,verticalStop)).getType();
					if(compareValue.equals("a")){
						//analizar+
						verticalStop-=10;
						break;
					}else if(compareValue.equals(".")||(compareValue.equals(" "))){
						outNowbyPark=true;
						break;
					}
				}
				//big while
				if(outNowbyPark)break;
			}
			counter++;
			if(counter>nmbrParksSpine)break;
			System.out.println("EVALUATION AFTER PRINT NEXT PARK");
			//DO NOT TOUCH
			System.out.println("before value for j");
			//System.out.println(j);
			for(int i=0;i<SpineConfiguration.SEPARATION_PARK_SIZE;i++){
				
				if(landMap.findPoint(SpineMapHelper.formKey(j+i,yy+26)).getType().equals(SpineConfiguration.POLYGON_BORDER)){
					state2=true;
					break;
				}else{
					//System.out.println(landMap.findPoint(SpineMapHelper.formKey(j+i,yy+26)).getType());
					j++;
					if(landMap.findPoint(SpineMapHelper.formKey(j+i,yy+26)).getType().equals(SpineConfiguration.POLYGON_BORDER)){
						System.out.println("We broke by if inside for");
						state2=true;
						break;
					}
				}
				//System.out.println(landMap.findPoint(SpineMapHelper.formKey(j+i,yy-1)).getType());
				
			}
			
			System.out.println("new value for j");
			System.out.println(j);
			if(state2)break;
			else if(landMap.findPoint(SpineMapHelper.formKey(j,yy+26)).getType().equals(SpineConfiguration.NODE_MARK)){
				//System.out.println("last caracter was n");
				//comeback to find the first 'n'
				int x=0;
				while(true){
					if(landMap.findPoint(SpineMapHelper.formKey(j-x,yy+26)).getType().equals(SpineConfiguration.ARTERIAL_MARK)){
						System.out.println("j-=x");
						j-=x;
						System.out.println(j);
						System.out.println("j");
						j++;
						System.out.println(j);
						break;
					}
					x++;
				}
			}else if(landMap.findPoint(SpineMapHelper.formKey(j,yy+26)).getType().equals(SpineConfiguration.ARTERIAL_MARK)){
				
				//System.out.println("last caracter was a");
				int x=0;				
				while(true){
					if(landMap.findPoint(SpineMapHelper.formKey(j-x,yy+26)).getType().equals(SpineConfiguration.NODE_MARK)){
						//System.out.println(j);
						j-=x;
						j++;
						break;
					}
					x++;
				}
				
			}
			
		}
		printAlmostCompleteBlocks();
	}
	private void printAlmostCompleteBlocks(){
		int valueX=pointStart;
		int valueY=yy-1;
		int contadorLateral=0;
		String value1,value2,aux;
		value1=value2=value2="";
		String dotValue="";
		String dotValue2="";
		boolean stateOut=false;
		//down
		while(true){
			valueY=yy-1;
			dotValue=landMap.findPoint(SpineMapHelper.formKey(valueX,valueY)).getType();
			value1="1";
			value2="2";
			if(dotValue.equals("p")){
				int saveValue=valueY-(nmbrParksLong+SpineConfiguration.LOCAL_BRANCH_SIZE);
				boolean exit=false;
				int counter=0;
				String valueComp;
				while(true){
					 valueComp=landMap.findPoint(SpineMapHelper.formKey(valueX,saveValue)).getType();
					for(int iii=0;iii<6;iii++){
						valueComp=landMap.findPoint(SpineMapHelper.formKey(valueX,saveValue)).getType();
						if(valueComp.equals("a")){
							saveValue-=nmbrParksLong+SpineConfiguration.LOCAL_BRANCH_SIZE*2;
							iii=0;
						}else if(valueComp.equals(" ")||valueComp.equals(".")){
							exit=true;
							break;
						}
						for(int jj=0;jj<15;jj++){
							valueComp=landMap.findPoint(SpineMapHelper.formKey(valueX,saveValue)).getType();
							if(!valueComp.equals(" ") && !valueComp.equals(".")){
								landMap.findPoint(SpineMapHelper.formKey(valueX+jj,saveValue)).setType(value2);
								landMap.findPoint(SpineMapHelper.formKey(valueX+jj+SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE,saveValue)).setType(value1);
							}
							
						}
						saveValue--;
					}
					if(exit)break;
					aux=value1;
					value1=value2;
					value2=aux;
					//conter++;
				}
				
			}else{
				//case "a"
				while(true){
					dotValue=landMap.findPoint(SpineMapHelper.formKey(valueX,valueY)).getType();
					if(dotValue.equals("a")){
						//aplying looback;
						valueY-=10;
						//System.out.println("decreasing valueY"+valueY);
						//landMap.findPoint(SpineMapHelper.formKey(valueX,valueY-1)).setType("8");
					}else if(dotValue.equals(".")||dotValue.equals(" ")){
						break;
					}
					for(int j=0;j<6;j++){
						dotValue=landMap.findPoint(SpineMapHelper.formKey(valueX,valueY)).getType();
						if(dotValue.equals("a")||dotValue.equals(".")||dotValue.equals(" "))break;
						for(int k=0;k<15;k++){
							dotValue=landMap.findPoint(SpineMapHelper.formKey(valueX+k,valueY)).getType();
							dotValue2=landMap.findPoint(SpineMapHelper.formKey(valueX+k+SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE,valueY)).getType();
							if(dotValue.equals(".")||dotValue.equals(" ")||dotValue2.equals(" ")||dotValue2.equals(".")){
								stateOut=true;
								break;
							}
							landMap.findPoint(SpineMapHelper.formKey(valueX+k,valueY)).setType(value1);
							landMap.findPoint(SpineMapHelper.formKey(valueX+k+SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE,valueY)).setType(value2);
						}
						valueY--;
					}
					aux=value1;
					value1=value2;
					value2=aux;
					
				}
				
			}
		
			stateOut=false;
			for(int i=0;i<46;i++){
				if(landMap.findPoint(SpineMapHelper.formKey(valueX,yy)).getType().equals(".")){
					stateOut=true;
					break;
				}
				valueX++;
			}
			if(stateOut)break;
		}
		
		//up
		/*valueY=yy-1;
		contadorLateral=0;
		String value1,value2,aux;
		value1=value2=value2="";
		String dotValue="";*/
		//down
		valueX=pointStart;
		contadorLateral=0;
		boolean endBlock=false;
		boolean state=false;
		while(true){
			valueY=yy+26;
			endBlock=false;
			value1="1";
			value2="2";
			while(true){
				for(int i=0;i<6;i++){
					String valueComparable=landMap.findPoint(SpineMapHelper.formKey(valueX,valueY)).getType();
					if(valueComparable.equals(" ") ||valueComparable.equals(".")){
						endBlock=true;
						break;
					}else if(valueComparable.equals("a")){
						String aux1;
						aux1=landMap.findPoint(SpineMapHelper.formKey(valueX,valueY+10)).getType();
						if(aux1.equals("p")){
							valueY+=nmbrParksLong+ SpineConfiguration.LOCAL_BRANCH_SIZE*2;
							i=0;
							//we made a loopback
							int loopback=valueY-(nmbrParksLong+ SpineConfiguration.LOCAL_BRANCH_SIZE*2);
							loopback--;
							int saveValue=loopback;
							String value=landMap.findPoint(SpineMapHelper.formKey(valueX,loopback)).getType();
							int counter=0;
							while(value.equals(landMap.findPoint(SpineMapHelper.formKey(valueX,loopback)).getType())){
								loopback--;
								counter++;
							}
							//this paint 1 data down 
							System.out.println(counter);
							for(int ii=0;ii<counter;ii++){
								for(int jj=0;jj<15;jj++){
									landMap.findPoint(SpineMapHelper.formKey(valueX+jj,saveValue)).setType(value2);
									landMap.findPoint(SpineMapHelper.formKey(valueX+jj+SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE,saveValue)).setType(value1);
								}
								saveValue--;
							}
						}
						else{
							valueY+=SpineConfiguration.LOCAL_BRANCH_SIZE;
							i=0;
							//we made a loopback
							int loopback=valueY-SpineConfiguration.LOCAL_BRANCH_SIZE;
							loopback--;
							int saveValue=loopback;
							String value=landMap.findPoint(SpineMapHelper.formKey(valueX,loopback)).getType();
							int counter=0;
							while(value.equals(landMap.findPoint(SpineMapHelper.formKey(valueX,loopback)).getType())){
								loopback--;
								counter++;
							}
							//this paint 1 data down 
							//System.out.println(counter);
							for(int ii=0;ii<counter;ii++){
								for(int jj=0;jj<15;jj++){
									landMap.findPoint(SpineMapHelper.formKey(valueX+jj,saveValue)).setType(value2);
									landMap.findPoint(SpineMapHelper.formKey(valueX+jj+SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE,saveValue)).setType(value1);
								}
								saveValue--;
							}
							
						}
					}
					for(int j=0;j<15;j++){
						String val1,val2;
						val1=landMap.findPoint(SpineMapHelper.formKey(valueX+j,valueY)).getType();
						val2=landMap.findPoint(SpineMapHelper.formKey(valueX+j+SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE,valueY)).getType();
						if(val1.equals(".")||val1.equals(" ")||val2.equals(" ")||val2.equals(".")){
							state=true;
							break;
						}
						landMap.findPoint(SpineMapHelper.formKey(valueX+j,valueY)).setType(value1);
						landMap.findPoint(SpineMapHelper.formKey(valueX+j+SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE,valueY)).setType(value2);
					}
					if(state)break;
					valueY++;
				}
				if(state)break;
				aux=value1;
				value1=value2;
				value2=aux;
				if(endBlock)break;
				
			}
			
			state=false;
			//System.out.println("Value of valueX"+ valueX);
			//System.out.println(landMap.findPoint(SpineMapHelper.formKey(1485,yy)).getType());
			
			int counter=0;
			for(int i=0;i<(SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE*2 +SpineConfiguration.COLLECTOR_BRANCH_SIZE);i++){
				if(landMap.findPoint(SpineMapHelper.formKey(valueX,yy-1)).getType().equals(".")){
					state=true;
					if(counter>=15 && counter<=30){
						int topeX=valueX-counter;
						int topeY=yy-1;
						boolean emergency=false;
						String val1,val3,val2;
						val1="1";
						val2="2";
						val3="";
						//int ccc=0;
						while(true){
							System.out.println(topeX);
							for(int ii=0;ii<6;ii++){
								if(landMap.findPoint(SpineMapHelper.formKey(topeX,topeY)).getType().equals("a")){
									topeY-=10;
									//emergency=true;
									//break;
								}else if(landMap.findPoint(SpineMapHelper.formKey(topeX,topeY)).getType().equals(".")){
									emergency=true;
									break;
								}
								for(int jj=0;jj<15;jj++){
									landMap.findPoint(SpineMapHelper.formKey(topeX+jj,topeY)).setType(val1);
								}
								topeY-=1;	
							}
							
							val3=val1;
							val1=val2;
							val2=val3;
							if(emergency)break;
							//if(ccc>3)break;
						}
						/*for(int ii=0;ii<6;ii++){
							for(int jj=0;jj<15;jj++){
								landMap.findPoint(SpineMapHelper.formKey(topeX+jj,topeY)).setType("1");
							}
							topeY-=1;	
						}*/
					}
					break;
					//it break with a value less than 46
					//if this values is greater than 16 + 15
					//it means we can lotizer more lotes
				}
				valueX++;
				counter++;
			}
			if(state)break;
			
		}
	
	}
}
