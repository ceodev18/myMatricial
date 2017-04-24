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
	public void paintAportes(){
		int tope,ejeX,limite,divisionResultBlock;
		String val1,val2,aux;
		divisionResultBlock= large/(SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE*2+SpineConfiguration.COLLECTOR_BRANCH_SIZE);
		tope=1;ejeX=0;
		for(int i=0;i<divisionResultBlock;i++)ejeX+=SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE*2+SpineConfiguration.COLLECTOR_BRANCH_SIZE;
		limite =large-ejeX;
		int newlongHouse=limite/2;
		String comparableValue,comparableValue2;
		
		while(true){
			comparableValue=landMap.findPoint(SpineMapHelper.formKey(ejeX,tope)).getType();
			if(comparableValue.equals(SpineConfiguration.ARTERIAL_MARK)){
					comparableValue=landMap.findPoint(SpineMapHelper.formKey(ejeX,tope+SpineConfiguration.LOCAL_BRANCH_SIZE)).getType();
					int printBackIndex=tope-1;
					tope+=SpineConfiguration.LOCAL_BRANCH_SIZE;
					int beginCounter=0;
					boolean stateOut=false;
					val1="1";
					val2="2";
					while(true){
						for(int i=0;i<SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE;i++){
							System.out.println(printBackIndex);
							comparableValue=landMap.findPoint(SpineMapHelper.formKey(ejeX,printBackIndex)).getType();
							if(comparableValue.equals(".")|| comparableValue.equals(" ") ||printBackIndex<1 || comparableValue.equals("a")){
								stateOut=true;
								break;
							}
							for(int j=0;j<newlongHouse;j++){
								comparableValue=landMap.findPoint(SpineMapHelper.formKey(ejeX+j,printBackIndex)).getType();
								comparableValue2=landMap.findPoint(SpineMapHelper.formKey(ejeX+newlongHouse+j,printBackIndex)).getType();
								if(!comparableValue.equals(" ") && !comparableValue.equals("."))
									landMap.findPoint(SpineMapHelper.formKey(ejeX+j,printBackIndex)).setType(val1);
								if(!comparableValue2.equals(" ") && !comparableValue2.equals("."))
									landMap.findPoint(SpineMapHelper.formKey(ejeX+newlongHouse+j,printBackIndex)).setType(val2);
							}
							printBackIndex--;
							beginCounter++;
						}
						aux=val1;
						val1=val2;
						val2=aux;
						if(stateOut)break;
						if(beginCounter>SpineConfiguration.BASE_CLUSTER_SIZE)break;
					}
					
					//prin blocks
			}else if(comparableValue.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)||comparableValue.equals(SpineConfiguration.BORDER_MARK))break;
			tope++;
			if(tope>yy)break;
		}
		comparableValue=landMap.findPoint(SpineMapHelper.formKey(ejeX,yy+SpineConfiguration.ARTERIAL_BRANCH_SIZE)).getType();
		tope=yy+SpineConfiguration.ARTERIAL_BRANCH_SIZE;
		
		if(!comparableValue.equals(" ") && !comparableValue.equals("a")&& !comparableValue.equals(".")){
			//landMap.findPoint(SpineMapHelper.formKey(ejeX,tope)).setType("9");//
			//auxiliar print block
			int idCont=0;
			int yyy=tope;
			val1="1";
			val2="2";
			boolean stateOut=false;
			while(true){
				for(int i=0;i<SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE;i++){
					comparableValue=landMap.findPoint(SpineMapHelper.formKey(ejeX,yyy)).getType();
					if(comparableValue.equals(SpineConfiguration.WALK_MARK)||
							comparableValue.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK) ||
							comparableValue.equals(SpineConfiguration.POLYGON_BORDER)){
						stateOut=true;
						break;
					}
					for(int j=0;j<newlongHouse;j++){
						landMap.findPoint(SpineMapHelper.formKey(ejeX+j,yyy)).setType(val1);
						landMap.findPoint(SpineMapHelper.formKey(ejeX+newlongHouse+j,yyy)).setType(val2);
					}
					idCont++;
					yyy++;
				}
				if(stateOut)break;
				aux=val1;
				val1=val2;
				val2=aux;
				if(idCont>(SpineConfiguration.BASE_CLUSTER_SIZE-SpineConfiguration.ARTERIAL_BRANCH_SIZE-SpineConfiguration.LOCAL_BRANCH_SIZE))break;
			}
			
			while(true){
				tope++;
				if(tope>width)break;
				comparableValue=landMap.findPoint(SpineMapHelper.formKey(ejeX,tope)).getType();
				if(comparableValue.equals(" ") || comparableValue.equals("."))break;
				if(comparableValue.equals(SpineConfiguration.ARTERIAL_MARK)){
					//int printBackIndex=tope-1;
					tope+=SpineConfiguration.LOCAL_BRANCH_SIZE;
					if(tope>width)break;
					val1="1";
					val2="2";
					stateOut=false;
					idCont=0;
					yyy=tope;
					while(true){
						for(int i=0;i<SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE;i++){
							comparableValue=landMap.findPoint(SpineMapHelper.formKey(ejeX,yyy)).getType();
							if(comparableValue.equals(SpineConfiguration.WALK_MARK)||
									comparableValue.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK) ||
									comparableValue.equals(SpineConfiguration.POLYGON_BORDER)){
								stateOut=true;
								break;
							}
							for(int j=0;j<newlongHouse;j++){
								landMap.findPoint(SpineMapHelper.formKey(ejeX+j,yyy)).setType(val1);
								landMap.findPoint(SpineMapHelper.formKey(ejeX+newlongHouse+j,yyy)).setType(val2);
							}
							idCont++;
							yyy++;
						}
						if(stateOut)break;
						aux=val1;
						val1=val2;
						val2=aux;
						if(idCont>SpineConfiguration.BASE_CLUSTER_SIZE)break;
					}
					
					//landMap.findPoint(SpineMapHelper.formKey(ejeX,tope)).setType("9");
				}
				if(tope>width)break;
				
			}	
		}
	}
	public void spineizeV2(){
		int divisionResultBlock = large/(SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE*2+SpineConfiguration.COLLECTOR_BRANCH_SIZE);
		int ejeX=SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE*2;//rev
		// COLLECTOR_BRANCH_SIZE
		int dos=0;
		for(int i=0;i<divisionResultBlock;i++){
				for(int j=0;j<SpineConfiguration.COLLECTOR_BRANCH_SIZE;j++){
					int index=1;
					while(true){
						if(landMap.findPoint(SpineMapHelper.formKey(ejeX+j,index)).getType().equals(SpineConfiguration.POLYGON_BORDER))break;
						if(landMap.findPoint(SpineMapHelper.formKey(ejeX+j,index)).getType().equals(SpineConfiguration.EMPTY_MARK)||
								landMap.findPoint(SpineMapHelper.formKey(ejeX+j,index)).getType().equals(SpineConfiguration.NODE_MARK))
							landMap.findPoint(SpineMapHelper.formKey(ejeX+j,index)).setType(SpineConfiguration.ARTERIAL_MARK);
						index++;
						if(index>width)break;
					}
					
				}
			ejeX+= (SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE*2+SpineConfiguration.COLLECTOR_BRANCH_SIZE);
		}
		
		
		//LOCAL_BRANCH_SIZE
		
		int inc=0;
		while(true){
			if(landMap.findPoint(SpineMapHelper.formKey(xx,yy-1+inc)).getType().equals(SpineConfiguration.POLYGON_BORDER)){
				break;
			}
			inc++;
			if(yy-1+inc>width)break;
		}
		System.out.println("inc");
		System.out.println(inc);
		int ejeY=yy-1+SpineConfiguration.BASE_CLUSTER_SIZE;
		//System.out.println(inc/SpineConfiguration.BASE_CLUSTER_SIZE);
		
		System.out.println("inc/SpineConfiguration.BASE_CLUSTER_SIZE");
		System.out.println(inc/SpineConfiguration.BASE_CLUSTER_SIZE);
		
		for(int i=0;i<(inc/SpineConfiguration.BASE_CLUSTER_SIZE);i++){
			for(int j=0;j<SpineConfiguration.LOCAL_BRANCH_SIZE;j++){
				int eje=0;
				while(true){
					if(!landMap.findPoint(SpineMapHelper.formKey(eje,ejeY-j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
					landMap.findPoint(SpineMapHelper.formKey(eje,ejeY-j)).setType(SpineConfiguration.ARTERIAL_MARK);
					eje++;
					if(eje>large)
					break;
				}
				
			}
			ejeY+=10+SpineConfiguration.BASE_CLUSTER_SIZE;
		}
		
		ejeY=yy-1-SpineConfiguration.BASE_CLUSTER_SIZE;
		System.out.println("ejeY");
		System.out.println(ejeY);
		for(int i=0;i<((yy-1)/SpineConfiguration.BASE_CLUSTER_SIZE);i++){
			for(int j=0;j<SpineConfiguration.LOCAL_BRANCH_SIZE;j++){
				int eje=0;
				while(true){
					if(!landMap.findPoint(SpineMapHelper.formKey(eje,ejeY-j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
						landMap.findPoint(SpineMapHelper.formKey(eje,ejeY-j)).setType(SpineConfiguration.ARTERIAL_MARK);
					eje++;
					if(eje>large)break;
				}
									
			}
			ejeY-=(SpineConfiguration.LOCAL_BRANCH_SIZE+SpineConfiguration.BASE_CLUSTER_SIZE);
			
		}
	//new methods
		//zonify();
	}
	public void zonify(){
		int divisionResultBlock= large/(SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE*2+SpineConfiguration.COLLECTOR_BRANCH_SIZE);
		setLongPark();
		int ejeY=yy-1;
		int ejeX=0;
		String val1,val2,aux;
		//bloques con parques arriba
		int dos=0;
		for(int i=0;i<divisionResultBlock;i++){
			if(dos<=2){
				int inc_Block=0;
				for(int j=0;j<((yy)/SpineConfiguration.BASE_CLUSTER_SIZE);j++){
					int tope=SpineConfiguration.BASE_CLUSTER_SIZE-5;
					//print blocks
					int inc=0;
					val1="1";
					val2="2";
					aux="";
					while(true){
						for(int k=0;k<SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE;k++){
							for(int l=0;l<SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE;l++){
								if(!landMap.findPoint(SpineMapHelper.formKey(ejeX+l,ejeY-inc_Block-inc)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
									landMap.findPoint(SpineMapHelper.formKey(ejeX+l,ejeY-inc_Block-inc)).setType(val1);
								if(!landMap.findPoint(SpineMapHelper.formKey(ejeX+l+SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE,ejeY-inc_Block-inc)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
									landMap.findPoint(SpineMapHelper.formKey(ejeX+l+SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE,ejeY-inc_Block-inc)).setType(val2);	
							}
							
							inc++;
						}
						//swap
						aux=val1;
						val1=val2;
						val2=aux;
						if(inc>tope)break;		
						
						}
					//pinto calle
					
					
					inc_Block+=SpineConfiguration.BASE_CLUSTER_SIZE+SpineConfiguration.LOCAL_BRANCH_SIZE;
				}

			}else{
				dos=0;
				int inc_Block=0;
				for(int j=0;j<((yy)/SpineConfiguration.BASE_CLUSTER_SIZE);j++){
					int tope=SpineConfiguration.BASE_CLUSTER_SIZE-SpineConfiguration.LOCAL_BRANCH_SIZE-nmbrParksLong;
					//print blocks
					int inc=0;
					val1="1";
					val2="2";
					aux="";
					while(true){
						for(int k=0;k<SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE;k++){
							for(int l=0;l<SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE;l++){
								if(!landMap.findPoint(SpineMapHelper.formKey(ejeX+l,ejeY-inc_Block-inc)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
									landMap.findPoint(SpineMapHelper.formKey(ejeX+l,ejeY-inc_Block-inc)).setType(val1);
								if(!landMap.findPoint(SpineMapHelper.formKey(ejeX+l+SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE,ejeY-inc_Block-inc)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
									landMap.findPoint(SpineMapHelper.formKey(ejeX+l+SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE,ejeY-inc_Block-inc)).setType(val2);	
							}
							
							inc++;
						}
						//swap
						aux=val1;
						val1=val2;
						val2=aux;
						if(inc>tope)break;		
						}
					//pinto calle
					for(int k=0;k<SpineConfiguration.LOCAL_BRANCH_SIZE;k++){
						for(int l=0;l<SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE*2;l++){
							if(!landMap.findPoint(SpineMapHelper.formKey(ejeX+l,ejeY-inc_Block-inc)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
								landMap.findPoint(SpineMapHelper.formKey(ejeX+l,ejeY-inc_Block-inc)).setType(SpineConfiguration.ARTERIAL_MARK);
						}
						inc++;
					}
					for(int k=0;k<nmbrParksLong;k++){
						for(int l=0;l<SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE*2;l++){
							if(!landMap.findPoint(SpineMapHelper.formKey(ejeX+l,ejeY-inc_Block-inc)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
								landMap.findPoint(SpineMapHelper.formKey(ejeX+l,ejeY-inc_Block-inc)).setType(SpineConfiguration.PARK_MARK);
						}
						inc++;
					}
					
					inc_Block+=SpineConfiguration.BASE_CLUSTER_SIZE+SpineConfiguration.LOCAL_BRANCH_SIZE;
				}
			}
			
			
			ejeX+=SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE*2+SpineConfiguration.COLLECTOR_BRANCH_SIZE;	
			dos++;
		}

		//bloques con parques abajo
		
		int ejeXX=0;
		int inc=0;
		while(true){
			if(landMap.findPoint(SpineMapHelper.formKey(xx,yy-1+inc)).getType().equals(SpineConfiguration.POLYGON_BORDER)){
				break;
			}
			inc++;
			if(yy-1+inc>width)break;
		}
		System.out.println("inc");
		System.out.println(inc);
		System.out.println("inc/SpineConfiguration.BASE_CLUSTER_SIZE");
		System.out.println(inc/SpineConfiguration.BASE_CLUSTER_SIZE);
		dos=0;
		for(int i=0;i<divisionResultBlock;i++){
			if(dos<=2){
				int ejeYY=yy-11+SpineConfiguration.BASE_CLUSTER_SIZE;	
				for(int j=0;j<(inc/SpineConfiguration.BASE_CLUSTER_SIZE);j++){
					int tope;
					if(j==0){
						//nro de lineas para el bloque
						tope=SpineConfiguration.BASE_CLUSTER_SIZE-26-10;
					}else{
						tope=SpineConfiguration.BASE_CLUSTER_SIZE;
					}

					
					int a=ejeYY-SpineConfiguration.LOCAL_BRANCH_SIZE+10;
					int counter=0;
					val1="1";
					val2="2";
					while(true){
						for(int iii=0;iii<SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE;iii++){
							for(int jjj=0;jjj<SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE;jjj++){
								//System.out.println(a);
								if(!landMap.findPoint(SpineMapHelper.formKey(ejeXX+jjj,a)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
									landMap.findPoint(SpineMapHelper.formKey(ejeXX+jjj,a)).setType(val1);
								if(!landMap.findPoint(SpineMapHelper.formKey(ejeXX+jjj+SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE,a)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
									landMap.findPoint(SpineMapHelper.formKey(ejeXX+jjj+SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE,a)).setType(val2);
							}
							a--;
							counter++;
						}
						aux=val1;
						val1=val2;
						val2=aux;
						
						if(counter>=tope)break;
					}
					
					ejeYY+=SpineConfiguration.BASE_CLUSTER_SIZE+SpineConfiguration.LOCAL_BRANCH_SIZE;
				}
			}else{
				dos=0;
				int ejeYY=yy-11+SpineConfiguration.BASE_CLUSTER_SIZE;	
				for(int j=0;j<(inc/SpineConfiguration.BASE_CLUSTER_SIZE);j++){
					int tope;
					if(j==0){
						//nro de lineas para el bloque
						tope=SpineConfiguration.BASE_CLUSTER_SIZE-SpineConfiguration.ARTERIAL_BRANCH_SIZE-nmbrParksLong-SpineConfiguration.LOCAL_BRANCH_SIZE*2;
					}else{
						tope=SpineConfiguration.BASE_CLUSTER_SIZE-nmbrParksLong-SpineConfiguration.LOCAL_BRANCH_SIZE;
					}

					for(int ii=0;ii<nmbrParksLong;ii++){
						for(int jj=0;jj<SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE*2;jj++){
							if(!landMap.findPoint(SpineMapHelper.formKey(ejeXX+jj,ejeYY-ii)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
								landMap.findPoint(SpineMapHelper.formKey(ejeXX+jj,ejeYY-ii)).setType(SpineConfiguration.PARK_MARK);
						}
					}
					
					for(int ii=0;ii<SpineConfiguration.LOCAL_BRANCH_SIZE;ii++){
						for(int jj=0;jj<SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE*2;jj++){
							if(!landMap.findPoint(SpineMapHelper.formKey(ejeXX+jj,ejeYY-ii-nmbrParksLong)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
								landMap.findPoint(SpineMapHelper.formKey(ejeXX+jj,ejeYY-ii-nmbrParksLong)).setType(SpineConfiguration.ARTERIAL_MARK);
						}
					}
					int a=ejeYY-nmbrParksLong-SpineConfiguration.LOCAL_BRANCH_SIZE;
					int counter=0;

					val1="1";
					val2="2";
					while(true){
						for(int iii=0;iii<SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE;iii++){
							for(int jjj=0;jjj<SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE;jjj++){
								//System.out.println(a);
								if(!landMap.findPoint(SpineMapHelper.formKey(ejeXX+jjj,a)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
									landMap.findPoint(SpineMapHelper.formKey(ejeXX+jjj,a)).setType(val1);
								if(!landMap.findPoint(SpineMapHelper.formKey(ejeXX+jjj+SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE,a)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
									landMap.findPoint(SpineMapHelper.formKey(ejeXX+jjj+SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE,a)).setType(val2);
							}
							a--;
							counter++;
						}
						aux=val1;
						val1=val2;
						val2=aux;
						
						if(counter>=tope)break;
					}
					
					ejeYY+=SpineConfiguration.BASE_CLUSTER_SIZE+SpineConfiguration.LOCAL_BRANCH_SIZE;
				}
			}
			dos++;
			ejeXX+=SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE*2+SpineConfiguration.COLLECTOR_BRANCH_SIZE;	
		}
		//patch //two sides

		//side down
		int indexUp=0;
		int a=(yy-1)- ((yy-1)/(SpineConfiguration.BASE_CLUSTER_SIZE+SpineConfiguration.LOCAL_BRANCH_SIZE)*SpineConfiguration.BASE_CLUSTER_SIZE+SpineConfiguration.LOCAL_BRANCH_SIZE);
		int topeDescount=a-SpineConfiguration.LOCAL_BRANCH_SIZE;
		val1="1";
		val2="2";
		for(int index=0;index<divisionResultBlock;index++){
			int counter=0;
			while(true){
				for(int i=0;i<SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE;i++){
					for(int j=0;j<SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE;j++){
						if(!landMap.findPoint(SpineMapHelper.formKey(indexUp+j,a-SpineConfiguration.LOCAL_BRANCH_SIZE-counter)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)&&
								!landMap.findPoint(SpineMapHelper.formKey(indexUp+j,a-SpineConfiguration.LOCAL_BRANCH_SIZE-counter)).getType().equals(SpineConfiguration.POLYGON_BORDER))
							landMap.findPoint(SpineMapHelper.formKey(indexUp+j,a-SpineConfiguration.LOCAL_BRANCH_SIZE-counter)).setType(val1);
						if(!landMap.findPoint(SpineMapHelper.formKey(indexUp+j+SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE,a-SpineConfiguration.LOCAL_BRANCH_SIZE-counter)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)&&
								!landMap.findPoint(SpineMapHelper.formKey(indexUp+j+SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE,a-SpineConfiguration.LOCAL_BRANCH_SIZE-counter)).getType().equals(SpineConfiguration.POLYGON_BORDER))
							landMap.findPoint(SpineMapHelper.formKey(indexUp+j+SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE,a-SpineConfiguration.LOCAL_BRANCH_SIZE-counter)).setType(val2);
					}
					counter++;
					if(counter>topeDescount)break;
				}
				aux=val1;
				val1=val2;
				val2=aux;
				if(counter>topeDescount)break;
			}
			indexUp+=46;
		}
		//side up	
		int indexYDown=(yy-1)/SpineConfiguration.BASE_CLUSTER_SIZE;
		int yBegin= yy+indexYDown*SpineConfiguration.BASE_CLUSTER_SIZE+SpineConfiguration.LOCAL_BRANCH_SIZE;//begin printting
		int nmbrLines=width-yBegin;
		System.out.println();
		int indexXLoop=0;
		boolean state=false;
		for(int index=0;index<divisionResultBlock;index++){
			int counter=0;
			val1="1";
			val2="2";
			while(true){
				//SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE
				for(int i=0;i<SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE;i++){
					for(int j=0;j<SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE;j++){
						if(!landMap.findPoint(SpineMapHelper.formKey(indexXLoop+j,yBegin+counter)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)&&
								!landMap.findPoint(SpineMapHelper.formKey(indexXLoop+j,yBegin+counter)).getType().equals(SpineConfiguration.POLYGON_BORDER))
							landMap.findPoint(SpineMapHelper.formKey(indexXLoop+j,yBegin+counter)).setType(val1);
						if(!landMap.findPoint(SpineMapHelper.formKey(indexXLoop+j+SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE,yBegin+counter)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)&&
								!landMap.findPoint(SpineMapHelper.formKey(indexXLoop+j+SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE,yBegin+counter)).getType().equals(SpineConfiguration.POLYGON_BORDER))
							landMap.findPoint(SpineMapHelper.formKey(indexXLoop+j+SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE,yBegin+counter)).setType(val2);
					}
					counter++;
					if(counter>nmbrLines)break;
				}
				aux=val1;
				val1=val2;
				val2=aux;
				if(counter>nmbrLines)break;
			}
			
			indexXLoop+=SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE*2+SpineConfiguration.COLLECTOR_BRANCH_SIZE;
		}
	paintAportes();
		
	}
	/*public  void spineize() {

	}*/
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

	private void setLongPark(){
		int total =(SpineConfiguration.COLLECTOR_BRANCH_SIZE*3)+3*(2*SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE)*SpineConfiguration.BASE_CLUSTER_SIZE;
		int eightPorc=(total/100)*10;//value 10%
		int result =eightPorc/(2*SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE);
		nmbrParksLong =result;
		System.out.println("nmbrParksLong");
		System.out.println(nmbrParksLong);
		
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



}
