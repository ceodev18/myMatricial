package algorithm.spineVariation;

import helpers.base.MapHelper;
import helpers.spineVariation.SpineMapHelper;
import models.spineVariation.SpineLandMap;
import interfaces.spineVariation.SpineConfiguration;

public class SpineLotizationAlgorithm {
	public static SpineLandMap spineLandMap;
	public static int xx;
	public static int yy;
	public static int width;
	public static int large;
	public static void zonify(int entryX,int entryY,int largeX) {
		xx=entryX;
		yy=entryY;
		large=largeX;
		organicZonification();
		changeValue(xx,yy);
		
		
	}
	private static void organicZonification(){
		//change n by l
		for (int j = spineLandMap.getPointsy() - 1; j >= 0; j--) {
			for (int i = 0; i < spineLandMap.getPointsx(); i++) {				
				if (spineLandMap.findPoint(SpineMapHelper.formKey(i, j)).getType()
						.equals(SpineConfiguration.NODE_MARK)) 
					spineLandMap.getLandPoint(SpineMapHelper.formKey(i,j)).setType(SpineConfiguration.EMPTY_MARK);
				//if(i== xx && j==yy)spineLandMap.getLandPoint(SpineMapHelper.formKey(i,j)).setType(SpineConfiguration.NODE_MARK);
			}
		}
	}
	private static void changeValue(int xx,int yy){
		int indice=0;
		for(int i=0;i<large;i++){
			//caso 1 
			if(spineLandMap.findPoint(MapHelper.formKey(i,yy)).getType().equals(SpineConfiguration.COLLECTOR_MARK)){
				i+=16;
				indice=i;
				break;//we find the first 'a' in the arterial branch
			}
		}
		
		//spineLandMap.getLandPoint(SpineMapHelper.formKey(indice,yy)).setType("K");
		int tp1,tp2,tp3,tp4;
		tp3=tp4=0;
		String etq1,etq2;
		etq1=SpineConfiguration.MARK_LOT1+"";
		etq2=SpineConfiguration.MARK_LOT2+"";
		int cambio=0;
		boolean key=true;
		for(int i=indice;i<large;i++){
			tp1=0;
			 tp2=0;
			 int counterFive=0;
			 cambio++;
			 key=true;
			 tp3=0;
			 tp4=0;
			 int j=0;
			 while(true){
				 if(spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
						 spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
				 if(spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.EMPTY_MARK)){
					 int div =(j)/Integer.parseInt(SpineConfiguration.BASE_CLUSTER_SIZE+"");
					 if(div>=1 && j>=Integer.parseInt(SpineConfiguration.BASE_CLUSTER_SIZE+"") && (j%Integer.parseInt(SpineConfiguration.BASE_CLUSTER_SIZE+""))==0 ){
						 System.out.print("valo j: ");
						 System.out.println(j);
						 System.out.print("valo div: ");
						 System.out.println(div);
						 if(spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.POLYGON_BORDER)||
								 spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
						 //if(spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
								// spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
						 for(int k=0;k<10;k++){
							 if(spineLandMap.findPoint(MapHelper.formKey(i,yy-j-k)).getType().equals(SpineConfiguration.POLYGON_BORDER)||
									 spineLandMap.findPoint(MapHelper.formKey(i,yy-j-k)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
							 spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy-j-k)).setType("a");	
							 //j++;
						 }
						 j+=10;
						 if(spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
								 spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
						 for(int l=0;l<27;l++){
							 if(spineLandMap.findPoint(MapHelper.formKey(i,yy-j-l)).getType().equals(SpineConfiguration.POLYGON_BORDER)||
									 spineLandMap.findPoint(MapHelper.formKey(i,yy-j-l)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
							 //if(spineLandMap.findPoint(MapHelper.formKey(i,yy-j-l)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
								//	 spineLandMap.findPoint(MapHelper.formKey(i,yy-j-l)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
							 spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy-j-l)).setType("p");
							 //j++;
						 }
						 j+=27;
						 if(spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
								 spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
						 
						// spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy-j)).setType("p");
						 //continue;
					 }
					 if(tp3<27){
						 //este caso es obligatorio si o si
							spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy-j)).setType("p");	
							tp3++;
							//if(tp3==6)tp2=0;
						}else if(tp4<10){
							spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy-j)).setType("a");	
							tp4++;
						}
					 	else if(tp1<6){
							spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy-j)).setType(etq1);	
							tp1++;
							if(tp2==6)tp2=0;
						}else {
							spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy-j)).setType(etq2);	
							tp2++;
							if(tp2==6)tp1=0;
						}
					 
				 }
				 j++;
				 if(spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
						 spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
				 
			 }
			
		}
		tp3=tp4=0;

		for(int i=100;i<large;i++){
			 tp1=0;
			 tp2=0;
			 int counterFive=0;
			 cambio++;
			 key=true;
			 tp3=0;
			 tp4=0;
			 int j=0;
			 while(true){
				 if(spineLandMap.findPoint(MapHelper.formKey(i,yy+j)).getType().equals(SpineConfiguration.POLYGON_BORDER) 
						 ||spineLandMap.findPoint(MapHelper.formKey(i,yy+j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
				 
				 if(spineLandMap.findPoint(MapHelper.formKey(i,yy+j)).getType().equals(SpineConfiguration.EMPTY_MARK)){
				/*	 int div =(j)/180;
					 if(div>=1 && j>=180 && (j%180)==0 ){
						 System.out.print("valo j: ");
						 System.out.println(j);
						 System.out.print("valo div: ");
						 System.out.println(div);
						 if(spineLandMap.findPoint(MapHelper.formKey(i,yy+j)).getType().equals(SpineConfiguration.POLYGON_BORDER)||
								 spineLandMap.findPoint(MapHelper.formKey(i,yy+j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
						 //if(spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
							//	 spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
						 for(int k=0;k<10;k++){
							 if(spineLandMap.findPoint(MapHelper.formKey(i,yy+j+k)).getType().equals(SpineConfiguration.POLYGON_BORDER)||
									 spineLandMap.findPoint(MapHelper.formKey(i,yy+j+k)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
							 spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy+j+k)).setType("a");	
							 //j++;
						 }
						 j+=10;
						 if(spineLandMap.findPoint(MapHelper.formKey(i,yy+j)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
								 spineLandMap.findPoint(MapHelper.formKey(i,yy+j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
						 for(int l=0;l<27;l++){
							 if(spineLandMap.findPoint(MapHelper.formKey(i,yy+j+l)).getType().equals(SpineConfiguration.POLYGON_BORDER)||
									 spineLandMap.findPoint(MapHelper.formKey(i,yy+j+l)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
							 //if(spineLandMap.findPoint(MapHelper.formKey(i,yy-j-l)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
								//	 spineLandMap.findPoint(MapHelper.formKey(i,yy-j-l)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
							 spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy+j+l)).setType("p");
							 //j++;
						 }
						 j+=27;
						 if(spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
								 spineLandMap.findPoint(MapHelper.formKey(i,yy-j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
						 r
						// spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy-j)).setType("p");
						 //continue;
					 }*/
					 	if(tp3<27){
					 		 //este caso es obligatorio si o si
							spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy+j)).setType(SpineConfiguration.PARK_MARK);	
					 		//spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy+j)).setType(valuePrint);	
							tp3++;
							//if(tp3==27)valuePrint="a";
						}else if(tp4<10){
							spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy+j)).setType("a");
							
							//spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy+j)).setType(valuePrint);
							tp4++;
						}else if(tp1<6){
							spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy+j)).setType(etq1);
							tp1++;
							if(tp2==6){
								tp2=0;
							}
						}else {
							spineLandMap.getLandPoint(SpineMapHelper.formKey(i,yy+j)).setType(etq2);	
							tp2++;
							if(tp2==6){
								tp1=0;
							}
						}
						
					}
				 j++;
				 if(spineLandMap.findPoint(MapHelper.formKey(i,yy+j)).getType().equals(SpineConfiguration.POLYGON_BORDER) ||
						 spineLandMap.findPoint(MapHelper.formKey(i,yy+j)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))break;
				 
				
			 }
			
		}
		
		
	}

}
