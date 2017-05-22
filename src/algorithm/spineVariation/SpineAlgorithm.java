package algorithm.spineVariation;

import java.util.List;

import helpers.clusterVariation.ClusterMapHelper;
import helpers.spineVariation.SpineDirectionHelper;
import helpers.spineVariation.SpineMapHelper;
import interfaces.clusterVariation.ClusterConfiguration;
import interfaces.spineVariation.SpineConfiguration;
import interfaces.spineVariation.SpineConstants;
import models.configuration.ConfigurationEntry;
import models.spineVariation.SpineLandMap;
import models.spineVariation.SpineLandPoint;
import models.spineVariation.SpineLandRoute;
import models.spineVariation.SpinePolygon;

import java.util.concurrent.ThreadLocalRandom;

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
	public int sideSize;
	public int sideDepth;
	public int factorAporte;
	public String out1, out2, out11, out22, aux, aux2;
	
	public SpineLandMap getLandMap() {
		return landMap;
	}
	public void setLandMap(SpineLandMap landMap) {
		this.landMap = landMap;
		this.sideDepth=landMap.getConfiguration().getLotConfiguration().getDepthSize();
		this.sideSize=landMap.getConfiguration().getLotConfiguration().getSideSize();
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
	public void clearDotsSpine() {
		for (int j = landMap.getPointsy() - 1; j >= 0; j--) {
			for (int i = 0; i < landMap.getPointsx(); i++) {
				if (landMap.findPoint(SpineMapHelper.formKey(i, j)).getType().equals(SpineConfiguration.POLYGON_BORDER))
					landMap.getLandPoint(SpineMapHelper.formKey(i, j)).setType(SpineConfiguration.EMPTY_MARK);
				// if(i== xx &&
				// j==yy)spineLandMap.getLandPoint(SpineMapHelper.formKey(i,j)).setType(SpineConfiguration.NODE_MARK);
			}
		}

	}

	public void paintAportes() {
		int tope, ejeX, limite, divisionResultBlock;
		String val1, val2, aux;
		divisionResultBlock = large
				/ (landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE);
		tope = 1;
		ejeX = 0;
		for (int i = 0; i < divisionResultBlock; i++)
			ejeX += landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE;
		limite = large - ejeX;
		int newlongHouse = limite / 2;
		String comparableValue, comparableValue2;

		if (newlongHouse >= landMap.getConfiguration().getLotConfiguration().getDepthSize()) {
			while (true) {
				comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, tope)).getType();
				if (!comparableValue.equals(SpineConfiguration.ARTERIAL_MARK)) {
					comparableValue = landMap
							.findPoint(SpineMapHelper.formKey(ejeX, tope + SpineConfiguration.LOCAL_BRANCH_SIZE))
							.getType();
					int printBackIndex = tope - 1;
					tope += SpineConfiguration.LOCAL_BRANCH_SIZE;
					int beginCounter = 0;
					boolean stateOut = false;
					val1 = "1";
					val2 = "2";
					int counterGram=0;
					boolean bol=true;
					int cccc=6;	
					while (true) {
						if(!landMap.findPoint(SpineMapHelper.formKey(ejeX,printBackIndex)).getType().equals(" ") &&counterGram<3 &&
								!landMap.findPoint(SpineMapHelper.formKey(ejeX+sideDepth,printBackIndex)).getType().equals(" ") && bol){
							String dato1="l-"+ejeX+"-"+(printBackIndex)+"-"+(ejeX+newlongHouse)+"-"+(printBackIndex)+"-"+(ejeX+newlongHouse)+"-"+(printBackIndex+6)+"-"+ejeX+"-"+(printBackIndex+6);
							if(cccc>=6 && (printBackIndex+20)<=width){
								landMap.findPoint(SpineMapHelper.formKey(ejeX, (printBackIndex+20))).setGramaticalType(dato1);
							}
						}
						cccc=0;
						for (int i = 0; i < landMap.getConfiguration().getLotConfiguration().getSideSize(); i++) {
							comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, printBackIndex)).getType();
							if (comparableValue.equals(".") || comparableValue.equals(" ") || printBackIndex < 1
									|| comparableValue.equals("a")) {
								stateOut = true;
								break;
							}
							printBackIndex--;
							beginCounter++;
							cccc++;
						}
						aux = val1;
						val1 = val2;
						val2 = aux;
						if (stateOut)
							break;
						if (beginCounter > landMap.getConfiguration().getBlockConfiguration().getSideSize())
							break;
						counterGram++;
					}

					// prin blocks
				} else if (comparableValue.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
						|| comparableValue.equals(SpineConfiguration.BORDER_MARK))
					break;
				tope++;
				if (tope > yy)
					break;
			}
			if((yy + SpineConfiguration.ARTERIAL_BRANCH_SIZE)>width)return;
			comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, yy + SpineConfiguration.ARTERIAL_BRANCH_SIZE)).getType();
			tope = yy + SpineConfiguration.ARTERIAL_BRANCH_SIZE;

			if (!comparableValue.equals(" ") && !comparableValue.equals("a") && !comparableValue.equals(".")) {
				// landMap.findPoint(SpineMapHelper.formKey(ejeX,tope)).setType("9");//
				// auxiliar print block
				int idCont = 0;
				int yyy = tope;
				val1 = "1";
				val2 = "2";
				boolean stateOut = false;
				while (true) {
					for (int i = 0; i < landMap.getConfiguration().getLotConfiguration().getSideSize(); i++) {
						comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, yyy)).getType();
						if (comparableValue.equals(SpineConfiguration.WALK_MARK)
								|| comparableValue.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
								|| comparableValue.equals(SpineConfiguration.POLYGON_BORDER)) {
							stateOut = true;
							break;
						}
						idCont++;
						yyy++;
						if(yyy>width){
							stateOut = true;
							break;
						}
					}
					if (stateOut)break;
					aux = val1;
					val1 = val2;
					val2 = aux;
					if (idCont > (landMap.getConfiguration().getBlockConfiguration().getSideSize() - SpineConfiguration.ARTERIAL_BRANCH_SIZE
							- SpineConfiguration.LOCAL_BRANCH_SIZE))
						break;
				}

				while (true) {
					tope++;
					if (tope > width)
						break;
					comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, tope)).getType();
					if (comparableValue.equals(" ") || comparableValue.equals("."))
						break;
					if (comparableValue.equals(SpineConfiguration.ARTERIAL_MARK)) {
						// int printBackIndex=tope-1;
						tope += SpineConfiguration.LOCAL_BRANCH_SIZE;
						if (tope > width)
							break;
						val1 = "1";
						val2 = "2";
						stateOut = false;
						idCont = 0;
						yyy = tope;
						while (true) {
							for (int i = 0; i < landMap.getConfiguration().getLotConfiguration().getSideSize(); i++) {
								comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, yyy)).getType();
								if (comparableValue.equals(SpineConfiguration.WALK_MARK)
										|| comparableValue.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
										|| comparableValue.equals(SpineConfiguration.POLYGON_BORDER)) {
									stateOut = true;
									break;
								}
								idCont++;
								yyy++;
							}
							if (stateOut)
								break;
							aux = val1;
							val1 = val2;
							val2 = aux;
							if (idCont > landMap.getConfiguration().getBlockConfiguration().getSideSize())
								break;
						}

						// landMap.findPoint(SpineMapHelper.formKey(ejeX,tope)).setType("9");
					}
					if (tope > width)
						break;

				}
			}

		} else if (limite >= 15 && limite < 30) {

			// cuadras de 1 casa
			while (true) {
				comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, tope)).getType();
				if (comparableValue.equals(SpineConfiguration.ARTERIAL_MARK)) {
					comparableValue = landMap
							.findPoint(SpineMapHelper.formKey(ejeX, tope + SpineConfiguration.LOCAL_BRANCH_SIZE))
							.getType();
					int printBackIndex = tope - 1;
					tope += SpineConfiguration.LOCAL_BRANCH_SIZE;
					int beginCounter = 0;
					boolean stateOut = false;
					val1 = "1";
					val2 = "2";
					while (true) {
						for (int i = 0; i < landMap.getConfiguration().getLotConfiguration().getSideSize(); i++) {

							comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, printBackIndex)).getType();
							if (comparableValue.equals(".") || comparableValue.equals(" ") || printBackIndex < 1
									|| comparableValue.equals("a")) {
								stateOut = true;
								break;
							}
							printBackIndex--;
							beginCounter++;
						}
						aux = val1;
						val1 = val2;
						val2 = aux;
						if (stateOut)
							break;
						if (beginCounter > landMap.getConfiguration().getBlockConfiguration().getSideSize())
							break;
					}

					// prin blocks
				} else if (comparableValue.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
						|| comparableValue.equals(SpineConfiguration.BORDER_MARK))
					break;
				tope++;
				if (tope > yy)
					break;
			}
			comparableValue = landMap
					.findPoint(SpineMapHelper.formKey(ejeX, yy + SpineConfiguration.ARTERIAL_BRANCH_SIZE)).getType();
			tope = yy + SpineConfiguration.ARTERIAL_BRANCH_SIZE;

			if (!comparableValue.equals(" ") && !comparableValue.equals("a") && !comparableValue.equals(".")) {
				// landMap.findPoint(SpineMapHelper.formKey(ejeX,tope)).setType("9");//
				// auxiliar print block
				int idCont = 0;
				int yyy = tope;
				val1 = "1";
				val2 = "2";
				boolean stateOut = false;
				while (true) {
					for (int i = 0; i < landMap.getConfiguration().getLotConfiguration().getSideSize(); i++) {
						comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, yyy)).getType();
						if (comparableValue.equals(SpineConfiguration.WALK_MARK)
								|| comparableValue.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
								|| comparableValue.equals(SpineConfiguration.POLYGON_BORDER)) {
							stateOut = true;
							break;
						}
						for (int j = 0; j < limite; j++) {
							landMap.findPoint(SpineMapHelper.formKey(ejeX + j, yyy)).setType(val1);
							// landMap.findPoint(SpineMapHelper.formKey(ejeX+newlongHouse+j,yyy)).setType(val2);
						}
						idCont++;
						yyy++;
					}
					if (stateOut)
						break;
					aux = val1;
					val1 = val2;
					val2 = aux;
					if (idCont > (landMap.getConfiguration().getBlockConfiguration().getSideSize() - SpineConfiguration.ARTERIAL_BRANCH_SIZE
							- SpineConfiguration.LOCAL_BRANCH_SIZE))
						break;
				}

				while (true) {
					tope++;
					if (tope > width)
						break;
					comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, tope)).getType();
					if (comparableValue.equals(" ") || comparableValue.equals("."))
						break;
					if (comparableValue.equals(SpineConfiguration.ARTERIAL_MARK)) {
						// int printBackIndex=tope-1;
						tope += SpineConfiguration.LOCAL_BRANCH_SIZE;
						if (tope > width)
							break;
						val1 = "1";
						val2 = "2";
						stateOut = false;
						idCont = 0;
						yyy = tope;
						while (true) {
							for (int i = 0; i < landMap.getConfiguration().getLotConfiguration().getSideSize(); i++) {
								comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, yyy)).getType();
								if (comparableValue.equals(SpineConfiguration.WALK_MARK)
										|| comparableValue.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
										|| comparableValue.equals(SpineConfiguration.POLYGON_BORDER)) {
									stateOut = true;
									break;
								}
								for (int j = 0; j < limite; j++) {
									landMap.findPoint(SpineMapHelper.formKey(ejeX + j, yyy)).setType(val1);
									// landMap.findPoint(SpineMapHelper.formKey(ejeX+newlongHouse+j,yyy)).setType(val2);
								}
								idCont++;
								yyy++;
								if(yyy>width){
									stateOut = true;
									break;
									
								}
							}
							if (stateOut)
								break;
							aux = val1;
							val1 = val2;
							val2 = aux;
							if (idCont > landMap.getConfiguration().getBlockConfiguration().getSideSize())
								break;
						}

						// landMap.findPoint(SpineMapHelper.formKey(ejeX,tope)).setType("9");
					}
					if (tope > width)
						break;

				}
			}

		} else if (limite > 10 && limite < 15) {
			int topAux = (landMap.getConfiguration().getLotConfiguration().getDepthSize() * landMap.getConfiguration().getLotConfiguration().getSideSize())
					/ limite;
			// topAux++;
			// cuadras de 1 casa
			while (true) {
				comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, tope)).getType();
				if (comparableValue.equals(SpineConfiguration.ARTERIAL_MARK)) {
					comparableValue = landMap
							.findPoint(SpineMapHelper.formKey(ejeX, tope + SpineConfiguration.LOCAL_BRANCH_SIZE))
							.getType();
					int printBackIndex = tope - 1;
					tope += SpineConfiguration.LOCAL_BRANCH_SIZE;
					int beginCounter = 0;
					boolean stateOut = false;
					val1 = "1";
					val2 = "2";
					while (true) {
						for (int i = 0; i < topAux; i++) {
							comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, printBackIndex)).getType();
							if (comparableValue.equals(".") || comparableValue.equals(" ") || printBackIndex < 1
									|| comparableValue.equals("a")) {
								stateOut = true;
								break;
							}
							for (int j = 0; j < limite; j++) {
								comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX + j, printBackIndex))
										.getType();
								// comparableValue2=landMap.findPoint(SpineMapHelper.formKey(ejeX+newlongHouse+j,printBackIndex)).getType();
								if (!comparableValue.equals(" ") && !comparableValue.equals("."))
									landMap.findPoint(SpineMapHelper.formKey(ejeX + j, printBackIndex)).setType(val1);
								// if(!comparableValue2.equals(" ") &&
								// !comparableValue2.equals("."))
								// landMap.findPoint(SpineMapHelper.formKey(ejeX+newlongHouse+j,printBackIndex)).setType(val2);
							}
							printBackIndex--;
							beginCounter++;
						}
						aux = val1;
						val1 = val2;
						val2 = aux;
						if (stateOut)
							break;
						if (beginCounter > landMap.getConfiguration().getBlockConfiguration().getSideSize())
							break;
					}

					// prin blocks
				} else if (comparableValue.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
						|| comparableValue.equals(SpineConfiguration.BORDER_MARK))
					break;
				tope++;
				if (tope > yy)
					break;
			}
			comparableValue = landMap
					.findPoint(SpineMapHelper.formKey(ejeX, yy + SpineConfiguration.ARTERIAL_BRANCH_SIZE)).getType();
			tope = yy + SpineConfiguration.ARTERIAL_BRANCH_SIZE;

			if (!comparableValue.equals(" ") && !comparableValue.equals("a") && !comparableValue.equals(".")) {
				// landMap.findPoint(SpineMapHelper.formKey(ejeX,tope)).setType("9");//
				// auxiliar print block
				int idCont = 0;
				int yyy = tope;
				val1 = "1";
				val2 = "2";
				boolean stateOut = false;
				while (true) {
					for (int i = 0; i < topAux; i++) {
						comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, yyy)).getType();
						if (comparableValue.equals(SpineConfiguration.WALK_MARK)
								|| comparableValue.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
								|| comparableValue.equals(SpineConfiguration.POLYGON_BORDER)) {
							stateOut = true;
							break;
						}
						for (int j = 0; j < limite; j++) {
							landMap.findPoint(SpineMapHelper.formKey(ejeX + j, yyy)).setType(val1);
							// landMap.findPoint(SpineMapHelper.formKey(ejeX+newlongHouse+j,yyy)).setType(val2);
						}
						idCont++;
						yyy++;
					}
					if (stateOut)
						break;
					aux = val1;
					val1 = val2;
					val2 = aux;
					if (idCont > (landMap.getConfiguration().getBlockConfiguration().getSideSize() - SpineConfiguration.ARTERIAL_BRANCH_SIZE
							- SpineConfiguration.LOCAL_BRANCH_SIZE))
						break;
				}

				while (true) {
					tope++;
					if (tope > width)
						break;
					comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, tope)).getType();
					if (comparableValue.equals(" ") || comparableValue.equals("."))
						break;
					if (comparableValue.equals(SpineConfiguration.ARTERIAL_MARK)) {
						// int printBackIndex=tope-1;
						tope += SpineConfiguration.LOCAL_BRANCH_SIZE;
						if (tope > width)
							break;
						val1 = "1";
						val2 = "2";
						stateOut = false;
						idCont = 0;
						yyy = tope;
						while (true) {
							for (int i = 0; i < topAux; i++) {
								comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, yyy)).getType();
								if (comparableValue.equals(SpineConfiguration.WALK_MARK)
										|| comparableValue.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
										|| comparableValue.equals(SpineConfiguration.POLYGON_BORDER)) {
									stateOut = true;
									break;
								}
								for (int j = 0; j < limite; j++) {
									landMap.findPoint(SpineMapHelper.formKey(ejeX + j, yyy)).setType(val1);
									// landMap.findPoint(SpineMapHelper.formKey(ejeX+newlongHouse+j,yyy)).setType(val2);
								}
								idCont++;
								yyy++;
							}
							if (stateOut)
								break;
							aux = val1;
							val1 = val2;
							val2 = aux;
							if (idCont > landMap.getConfiguration().getBlockConfiguration().getSideSize())
								break;
						}

						// landMap.findPoint(SpineMapHelper.formKey(ejeX,tope)).setType("9");
					}
					if (tope > width)
						break;

				}
			}

		}

	}

	public void spineizeV2(int directionBranch) {
		/*int directionBranch = SpineDirectionHelper.orthogonalDirectionFromPointToPoint(new SpineLandPoint(xx, yy),
				landMap.getCentroid());*/
		if(directionBranch==2 || directionBranch==0){
			int divisionResultBlock = large
					/ (landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE);
			int ejeX = landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2;// rev
			// COLLECTOR_BRANCH_SIZE
			int dos = 0;
			for (int i = 0; i < divisionResultBlock; i++) {
				boolean firstPaint = true;
				for (int j = 0; j < SpineConfiguration.COLLECTOR_BRANCH_SIZE; j++) {
					int index = 1;
					while (true) {
						if (landMap.findPoint(SpineMapHelper.formKey(ejeX + j, index)).getType()
								.equals(SpineConfiguration.POLYGON_BORDER))
							break;
						if (landMap.findPoint(SpineMapHelper.formKey(ejeX + j, index)).getType()
								.equals(SpineConfiguration.EMPTY_MARK)
								|| landMap.findPoint(SpineMapHelper.formKey(ejeX + j, index)).getType()
										.equals(SpineConfiguration.NODE_MARK)) {
							if (firstPaint) {
								landMap.findPoint(SpineMapHelper.formKey(ejeX + j, index))
										.setType(SpineConfiguration.COLLECTOR_MARK);
								// sent to LandRoute
								SpineLandPoint collectorPoint = new SpineLandPoint(ejeX, index);
								int direction = SpineDirectionHelper.orthogonalDirectionFromPointToPoint(collectorPoint,
										landMap.getCentroid());
								addingLandRoute(collectorPoint.getId(), direction, SpineConfiguration.COLLECTOR_BRANCH);
								firstPaint = false;
							} else
								landMap.findPoint(SpineMapHelper.formKey(ejeX + j, index))
										.setType(SpineConfiguration.COLLECTOR_MARK);

						}

						index++;
						if (index > width)
							break;
					}

				}

				ejeX += (landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE);
			}

			// LOCAL_BRANCH_SIZE

			int inc = 0;
			while (true) {
				if (landMap.findPoint(SpineMapHelper.formKey(xx, yy - 1 + inc)).getType()
						.equals(SpineConfiguration.POLYGON_BORDER)) {
					break;
				}
				inc++;
				if (yy - 1 + inc > width)
					break;
			}

			int ejeY = yy - 1 + landMap.getConfiguration().getBlockConfiguration().getSideSize();
			for (int i = 0; i < (inc / landMap.getConfiguration().getBlockConfiguration().getSideSize()); i++) {
				boolean firstPaint = true;
				for (int j = 0; j < SpineConfiguration.LOCAL_BRANCH_SIZE; j++) {
					int eje = 0;
					while (true) {
						if ( verificablePoint(eje,ejeY - j) && !landMap.findPoint(SpineMapHelper.formKey(eje, ejeY - j)).getType()
								.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)) {
							if (firstPaint) {
								firstPaint = false;
								SpineLandPoint collectorPoint = new SpineLandPoint(ejeX, ejeY - j);
								int direction = SpineDirectionHelper.orthogonalDirectionFromPointToPoint(collectorPoint,
										landMap.getCentroid());
								addingLandRoute(collectorPoint.getId(), direction, SpineConfiguration.LOCAL_BRANCH);
								landMap.findPoint(SpineMapHelper.formKey(eje, ejeY - j))
										.setType(SpineConfiguration.LOCAL_MARK);
							} else {
								landMap.findPoint(SpineMapHelper.formKey(eje, ejeY - j))
										.setType(SpineConfiguration.LOCAL_MARK);
							}
						}

						eje++;
						if (eje > large)
							break;
					}

				}
				ejeY += 10 + landMap.getConfiguration().getBlockConfiguration().getSideSize();
			}

			ejeY = yy - 1 - landMap.getConfiguration().getBlockConfiguration().getSideSize();
			int cccc = 0;
			for (int i = 0; i < ((yy - 1) / landMap.getConfiguration().getBlockConfiguration().getSideSize()); i++) {
				boolean firstPaint = true;
				for (int j = 0; j < SpineConfiguration.LOCAL_BRANCH_SIZE; j++) {
					int eje = 0;
					while (true) {
						if ( verificablePoint(eje,ejeY - j) &&!landMap.findPoint(SpineMapHelper.formKey(eje, ejeY - j)).getType()
								.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)) {
							if (firstPaint) {
								SpineLandPoint collectorPoint = new SpineLandPoint(eje, ejeY - j);
								int direction = SpineDirectionHelper.orthogonalDirectionFromPointToPoint(collectorPoint,
										landMap.getCentroid());
								addingLandRoute(collectorPoint.getId(), direction, SpineConfiguration.LOCAL_BRANCH);
								firstPaint = false;
								landMap.findPoint(SpineMapHelper.formKey(eje, ejeY - j))
										.setType(SpineConfiguration.LOCAL_MARK);
							} else {
								landMap.findPoint(SpineMapHelper.formKey(eje, ejeY - j))
										.setType(SpineConfiguration.LOCAL_MARK);
							}

						}

						eje++;
						if (eje > large)
							break;
					}

				}
				ejeY -= (SpineConfiguration.LOCAL_BRANCH_SIZE + landMap.getConfiguration().getBlockConfiguration().getSideSize());

			}
			zonify();
		}else{
			int divisionResultBlock = width
					/ (landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE);
			int ejeX = landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2;// rev
			// COLLECTOR_BRANCH_SIZE
			int dos = 0;
			for (int i = 0; i < divisionResultBlock; i++) {
				boolean firstPaint = true;
				for (int j = 0; j < SpineConfiguration.COLLECTOR_BRANCH_SIZE; j++) {
					int index = 1;
					while (true) {
						if (landMap.findPoint(SpineMapHelper.formKey(index,ejeX + j)).getType()
								.equals(SpineConfiguration.POLYGON_BORDER))
							break;
						if (landMap.findPoint(SpineMapHelper.formKey(index, ejeX + j)).getType()
								.equals(SpineConfiguration.EMPTY_MARK)
								|| landMap.findPoint(SpineMapHelper.formKey(index, ejeX + j)).getType()
										.equals(SpineConfiguration.NODE_MARK)) {
							if (firstPaint) {
								landMap.findPoint(SpineMapHelper.formKey(index,ejeX + j))
										.setType(SpineConfiguration.COLLECTOR_MARK);
								// sent to LandRoute
								SpineLandPoint collectorPoint = new SpineLandPoint(index, ejeX);
								int direction = SpineDirectionHelper.orthogonalDirectionFromPointToPoint(collectorPoint,
										landMap.getCentroid());
								addingLandRoute(collectorPoint.getId(), direction, SpineConfiguration.COLLECTOR_BRANCH);
								firstPaint = false;
							} else
								landMap.findPoint(SpineMapHelper.formKey(index,ejeX + j))
										.setType(SpineConfiguration.COLLECTOR_MARK);

						}

						index++;
						if (index > large)
							break;
					}

				}

				ejeX += (landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE);
			}
			int inc = 0;
			while (true) {
				if (landMap.findPoint(SpineMapHelper.formKey(xx - 1 + inc, yy)).getType()
						.equals(SpineConfiguration.POLYGON_BORDER)) {
					break;
				}
				inc++;
				if (xx - 1 + inc > large)
					break;
			}

			int ejeY = xx - 1 + landMap.getConfiguration().getBlockConfiguration().getSideSize();
			
			for (int i = 0; i < (inc / landMap.getConfiguration().getBlockConfiguration().getSideSize()); i++) {
				boolean firstPaint = true;
				for (int j = 0; j < SpineConfiguration.LOCAL_BRANCH_SIZE; j++) {
					int eje = 0;
					while (true) {
						if ( verificablePoint(ejeY - j,eje)&& !landMap.findPoint(SpineMapHelper.formKey(ejeY - j,eje)).getType()
								.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)) {
							if (firstPaint) {
								firstPaint = false;
								SpineLandPoint collectorPoint = new SpineLandPoint(ejeX, ejeY - j);
								int direction = SpineDirectionHelper.orthogonalDirectionFromPointToPoint(collectorPoint,
										landMap.getCentroid());
								addingLandRoute(collectorPoint.getId(), direction, SpineConfiguration.LOCAL_BRANCH);
								landMap.findPoint(SpineMapHelper.formKey(ejeY - j,eje))
										.setType(SpineConfiguration.LOCAL_MARK);
							} else {
								landMap.findPoint(SpineMapHelper.formKey(ejeY - j,eje))
										.setType(SpineConfiguration.LOCAL_MARK);
							}
						}

						eje++;
						if (eje > width)
							break;
					}

				}
				ejeY += 10 + landMap.getConfiguration().getBlockConfiguration().getSideSize();
			}
			ejeY = xx - 1 - landMap.getConfiguration().getBlockConfiguration().getSideSize();
			int cccc = 0;
			for (int i = 0; i < ((xx-1) / landMap.getConfiguration().getBlockConfiguration().getSideSize()); i++) {
				boolean firstPaint = true;
				for (int j = 0; j < SpineConfiguration.LOCAL_BRANCH_SIZE; j++) {
					int eje = 0;
					while (true) {
						if (!landMap.findPoint(SpineMapHelper.formKey(ejeY - j,eje)).getType()
								.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)) {
							if (firstPaint) {
								SpineLandPoint collectorPoint = new SpineLandPoint(ejeY - j,eje);
								int direction = SpineDirectionHelper.orthogonalDirectionFromPointToPoint(collectorPoint,
										landMap.getCentroid());
								addingLandRoute(collectorPoint.getId(), direction, SpineConfiguration.LOCAL_BRANCH);
								firstPaint = false;
								landMap.findPoint(SpineMapHelper.formKey(ejeY - j,eje))
										.setType(SpineConfiguration.LOCAL_MARK);
							} else {
								landMap.findPoint(SpineMapHelper.formKey(ejeY - j,eje))
										.setType(SpineConfiguration.LOCAL_MARK);
							}

						}

						eje++;
						if (eje > width)
							break;
					}

				}
				ejeY -= (SpineConfiguration.LOCAL_BRANCH_SIZE + landMap.getConfiguration().getBlockConfiguration().getSideSize());
				if(ejeY<0)break;

			}
			zonifyVertical();	
		}

		
	}




	public void createRouteVariation(int axisPoint, int direction, int branchType) {
		int extension = 0;
		String markType = "";
		int growDirection = -1;
		SpineLandRoute spineLandRoute = null;
		int trueAxis = axisPoint;
		while (landMap.landPointisOnMap(trueAxis)
				&& landMap.getLandPoint(trueAxis).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)) {
			trueAxis = SpineMapHelper.moveKeyByOffsetAndDirection(trueAxis, 1, direction);
		}
		switch (branchType) {
		case SpineConfiguration.ARTERIAL_BRANCH:
			extension = SpineConfiguration.ARTERIAL_BRANCH_SIZE;
			markType = SpineConfiguration.ARTERIAL_MARK;
			spineLandRoute = new SpineLandRoute(trueAxis, direction, "a");
			break;
		case SpineConfiguration.COLLECTOR_BRANCH:
			extension = SpineConfiguration.COLLECTOR_BRANCH_SIZE;
			markType = SpineConfiguration.COLLECTOR_MARK;
			spineLandRoute = new SpineLandRoute(trueAxis, direction, "b");
			break;
		case SpineConfiguration.LOCAL_BRANCH:
			extension = SpineConfiguration.LOCAL_BRANCH_SIZE;
			markType = SpineConfiguration.LOCAL_MARK;
			spineLandRoute = new SpineLandRoute(trueAxis, direction, "c");
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
				spineLandRoute.setFinalPointId(finalPointid);
				landMap.getLandRoutes().add(spineLandRoute);
			} else {
				createLine(SpineMapHelper.moveKeyByOffsetAndDirection(axisPoint, i, growDirection), direction,
						markType);
			}
		}
	}

	private int createLine(int givenXY, int direction, String markType) {
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

	private boolean markPoint(int[] newXY, String markType, Boolean in, Boolean out) {
		boolean changed = false;
		if(!verificablePoint(newXY[0],newXY[1]))return false;
		SpineLandPoint clusterLandPoint = landMap.findPoint(SpineMapHelper.formKey(newXY[0], newXY[1]));
		if(clusterLandPoint==null){
			System.out.println("null");
		}
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

	/**
	 * TODO here begins the lotization part of the algorithm
	 */

	private void setLongPark() {
		int total = (SpineConfiguration.COLLECTOR_BRANCH_SIZE * 3)
				+ 3 * (2 * SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE) * SpineConfiguration.BASE_CLUSTER_SIZE;

		int eightPorc = (total / 100) * 10;// value 10%
		int result = eightPorc / (2 * SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE);
		nmbrParksLong = result;

	}




	// recieve two arguments determines where put the 1st file of parks

	private void addingLandRoute(int axisPoint, int direction, int branchType) {
		int extension = 0;
		String markType = "";
		int growDirection = -1;
		SpineLandRoute spineLandRoute = null;
		int trueAxis = axisPoint;
		while (landMap.landPointisOnMap(trueAxis)
				&& landMap.getLandPoint(trueAxis).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)) {
			trueAxis = SpineMapHelper.moveKeyByOffsetAndDirection(trueAxis, 1, direction);
		}
		switch (branchType) {
		case SpineConfiguration.ARTERIAL_BRANCH:
			extension = SpineConfiguration.ARTERIAL_BRANCH_SIZE;
			markType = SpineConfiguration.ARTERIAL_MARK;
			spineLandRoute = new SpineLandRoute(trueAxis, direction, "a");
			break;
		case SpineConfiguration.COLLECTOR_BRANCH:
			extension = SpineConfiguration.COLLECTOR_BRANCH_SIZE;
			markType = SpineConfiguration.COLLECTOR_MARK;
			spineLandRoute = new SpineLandRoute(trueAxis, direction, "b");
			break;
		case SpineConfiguration.LOCAL_BRANCH:
			extension = SpineConfiguration.LOCAL_BRANCH_SIZE;
			markType = SpineConfiguration.LOCAL_MARK;
			spineLandRoute = new SpineLandRoute(trueAxis, direction, "c");
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

		/*
		 * for (int i = 0; i < extension; i++) {
		 * createLine(SpineMapHelper.moveKeyByOffsetAndDirection(axisPoint, i,
		 * growDirection), direction, markType); }
		 */
		landMap.getLandRoutes().add(spineLandRoute);

	}
	
	public void zonifyVertical(){
		int divisionResultBlock = width
				/ (landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE);
		setLongPark();
		factorAporte = divisionResultBlock / 9;
		int ejeY = xx - 1;//revisar
		int ejeX = 0;
		String val1, val2, aux;
		int dos = 0;
		if (xx >= landMap.getConfiguration().getBlockConfiguration().getSideSize()) {
			for (int i = 0; i < divisionResultBlock; i++) {
				int div = i / 9;
				int rem = i % 9;
				// if(i>10 && (i/10)>)
				if (dos <= 2) {
					int inc_Block = 0;
						
					for (int j = 0; j < ((xx) / landMap.getConfiguration().getBlockConfiguration().getSideSize()); j++) {
						int tope = landMap.getConfiguration().getBlockConfiguration().getSideSize() - 5;
						// print blocks
						int inc = 0;
						val1 = "1";
						val2 = "2";
						aux = "";
						while (true) {
							for (int k = 0; k < landMap.getConfiguration().getLotConfiguration().getSideSize(); k++) {
								if(k==0 && (ejeY - inc_Block - inc)>=0){
									if(verificablePoint(ejeY - inc_Block - inc,ejeX) &&!landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX)).getType().equals(" ")){
										if(insideMApVertical((ejeY - inc_Block - inc),ejeX,2,1)){
											String data1="l-"+(ejeY - inc_Block - inc)+"-"+ejeX+"-"+((ejeY - inc_Block - inc)+sideSize)+"-"+ejeX+"-"+
													((ejeY - inc_Block - inc)+sideSize)+"-"+(ejeX-sideDepth)+"-"+(ejeY - inc_Block - inc)+"-"+(ejeX-sideDepth);
											landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX)).setGramaticalType(data1);
										}
										if(insideMApVertical((ejeY - inc_Block - inc),(ejeX-sideDepth),2,1)){
											String data2="l-"+(ejeY - inc_Block - inc)+"-"+(ejeX-sideDepth)+"-"+((ejeY - inc_Block - inc)+sideSize)+"-"+(ejeX-sideDepth)+"-"+
													((ejeY - inc_Block - inc)+sideSize)+"-"+(ejeX-(sideDepth*2))+"-"+(ejeY - inc_Block - inc)+"-"+(ejeX-(sideDepth*2));
											landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,(ejeX-sideDepth))).setGramaticalType(data2);
										}
										
									}
									
								}
								inc++;
							}
							// swap
							aux = val1;
							val1 = val2;
							val2 = aux;
							if (inc > tope)
								break;

						}
						// pinto calle

						inc_Block += landMap.getConfiguration().getBlockConfiguration().getSideSize() + SpineConfiguration.LOCAL_BRANCH_SIZE;
					}

				} else {
					//here print parks
					dos = 0;

					int inc_Block = 0;

					for (int j = 0; j < ((xx) / landMap.getConfiguration().getBlockConfiguration().getSideSize()); j++) {
						String cambiable = SpineConfiguration.PARK_MARK;
						int randomNum = ThreadLocalRandom.current().nextInt(0,
								(xx) / landMap.getConfiguration().getBlockConfiguration().getSideSize());

						if (cambiable.equals(SpineConfiguration.ADDINGS_MARK))
							cambiable = SpineConfiguration.PARK_MARK;
						if (div > 0 && rem == 0 && randomNum == j) {
							cambiable = SpineConfiguration.ADDINGS_MARK;
						}

						int tope = landMap.getConfiguration().getBlockConfiguration().getSideSize() - SpineConfiguration.LOCAL_BRANCH_SIZE
								- nmbrParksLong;
						// print blocks
						int inc = 0;
						val1 = "1";
						val2 = "2";
						aux = "";
						while (true) {
							
							
							for (int k = 0; k < landMap.getConfiguration().getLotConfiguration().getSideSize(); k++) {
								if(k==0 && (ejeY - inc_Block - inc)>=0){
									if(verificablePoint(ejeY - inc_Block - inc,ejeX) &&!landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX)).getType().equals(" ")){
										if(insideMApVertical((ejeY - inc_Block - inc),ejeX,2,1)){
											String data1="l-"+(ejeY - inc_Block - inc)+"-"+ejeX+"-"+((ejeY - inc_Block - inc)+sideSize)+"-"+ejeX+"-"+
													((ejeY - inc_Block - inc)+sideSize)+"-"+(ejeX-sideDepth)+"-"+(ejeY - inc_Block - inc)+"-"+(ejeX-sideDepth);
											landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX)).setGramaticalType(data1);
										}
										if(insideMApVertical((ejeY - inc_Block - inc),(ejeX-sideDepth),2,1)){
											String data2="l-"+(ejeY - inc_Block - inc)+"-"+(ejeX-sideDepth)+"-"+((ejeY - inc_Block - inc)+sideSize)+"-"+(ejeX-sideDepth)+"-"+
													((ejeY - inc_Block - inc)+sideSize)+"-"+(ejeX-(sideDepth*2))+"-"+(ejeY - inc_Block - inc)+"-"+(ejeX-(sideDepth*2));
											landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,(ejeX-sideDepth))).setGramaticalType(data2);
										}
										
									}
									
								}
								
								inc++;
							}
							// swap
							aux = val1;
							val1 = val2;
							val2 = aux;
							if (inc > tope)
								break;
						}
						// pinto calle
						for (int k = 0; k < SpineConfiguration.LOCAL_BRANCH_SIZE; k++) {
							for (int l = 0; l < landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2; l++) {
								if ((ejeY - inc_Block - inc)>=0 &&!landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX + l)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
									landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX + l))
											.setType(SpineConfiguration.ARTERIAL_MARK);
							}
							inc++;
						}
						//revisar
						for (int k = 0; k < nmbrParksLong; k++) {
								if(k==0 && (ejeY - inc_Block - inc)>=0 &&  (ejeY - inc_Block - inc)>=0){
									if(verificablePoint(ejeY - inc_Block - inc,ejeX) &&!landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX)).getType().equals(" ")){
											if(verifyPArk(ejeY - inc_Block - inc,ejeX,2,1)){
												String data1="g-"+(ejeY - inc_Block - inc-36)+"-"+(ejeX)+"-"+(ejeY - inc_Block - inc-36+nmbrParksLong)+"-"+(ejeX)+"-"+
														(ejeY - inc_Block - inc-36+nmbrParksLong)+"-"+(ejeX-(sideDepth*2))+"-"+(ejeY - inc_Block - inc-36)+"-"+(ejeX-(sideDepth*2));;
														landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX)).setGramaticalType(data1);
											}
											
									}
									
								}
							for (int l = 0; l < landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2; l++) {
								if ((ejeY - inc_Block - inc)>=0 && !landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX + l))
										.getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)){
									landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX+l ))
									.setType(cambiable);
									
									
								}
									
							}
							inc++;
						}

						inc_Block += landMap.getConfiguration().getBlockConfiguration().getSideSize() + SpineConfiguration.LOCAL_BRANCH_SIZE;
					}

				}

				ejeX += landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE;
				dos++;

			}
			
			// PATCH LEFT OF ARTERIAL BRANCH 
			int indexUp = 0;
			int blockSize=landMap.getConfiguration().getBlockConfiguration().getSideSize();
			//if()
			int a = (xx-1) - ((xx-1) / (blockSize + SpineConfiguration.LOCAL_BRANCH_SIZE)
					* blockSize + SpineConfiguration.LOCAL_BRANCH_SIZE);
			
			
			int topeDescount = a - SpineConfiguration.LOCAL_BRANCH_SIZE;
			double divisionOutByShortBlock=((xx-1) % (blockSize + SpineConfiguration.LOCAL_BRANCH_SIZE))%(blockSize + SpineConfiguration.LOCAL_BRANCH_SIZE);
			divisionOutByShortBlock/=blockSize + SpineConfiguration.LOCAL_BRANCH_SIZE;
			if(divisionOutByShortBlock<0.5){

				//condicion de impresion
				String v1,v2;
				for(int index = 0; index < divisionResultBlock; index++) {
					v1= landMap.findPoint(SpineMapHelper.formKey(topeDescount+1,indexUp)).getType();
					if(v1.equals("p")||v1.equals("1")||v1.equals("2")){
						topeDescount-=SpineConfiguration.LOCAL_BRANCH_SIZE;
						break;
					}
					indexUp+=landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE;
					if(indexUp>width)break;
				}
				indexUp=0;
				//getDescount 
				for(int index = 0; index < divisionResultBlock; index++) {
					int base=topeDescount;
					val1="1";
					val2="2";
					while(true){
						for (int i = 0; i < landMap.getConfiguration().getLotConfiguration().getSideSize(); i++) {
							if(i==0 ){
								if(verificablePoint(base,indexUp) &&!landMap.findPoint(SpineMapHelper.formKey(base,indexUp)).getType().equals(" ")){
									if(insideMApVertical(base-36,indexUp,2,1)){
										String data1="l-"+(base-36)+"-"+indexUp+"-"+((base-36)+sideSize)+"-"+indexUp+"-"+
												((base-36)+sideSize)+"-"+(indexUp-sideDepth)+"-"+(base-36)+"-"+(indexUp-sideDepth);
										landMap.findPoint(SpineMapHelper.formKey((base-36),indexUp)).setGramaticalType(data1);
										
									}
									
									if(insideMApVertical((base-36),indexUp-sideDepth,2,1)){
										String data2="l-"+(base-36)+"-"+(indexUp-sideDepth)+"-"+((base-36)+sideSize)+"-"+(indexUp-sideDepth)+"-"+
												((base-36)+sideSize)+"-"+(indexUp -(sideDepth*2))+"-"+(base-36)+"-"+(indexUp -(sideDepth*2));
										landMap.findPoint(SpineMapHelper.formKey((base-36),indexUp-sideDepth)).setGramaticalType(data2);
									}
									
								}
								
							}
							
							base--;
							if(base<0)break;
						}
						aux = val1;
						val1 = val2;
						val2 = aux;
						
						if(base<0)break;
					}
					indexUp+=landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE;;
				}
			}

		} else {
			/// parche
			// ESTAS LINEAS SE EJECUTAN SI UN LADO DEL ARTERIAL BRANCH ES MENOR A UN BLOQUE CON CALLE AGREGADA
			int xxx = 0;
			for (int i = 0; i < divisionResultBlock; i++) {
				int topY = xx - 1;
				boolean stateOut = false;
				val1 = "1";
				val2 = "2";
				while (true) {
					for (int indAux = 0; indAux < landMap.getConfiguration().getLotConfiguration().getSideSize(); indAux++) {
						String cmp = landMap.findPoint(SpineMapHelper.formKey(topY, xxx)).getType();
						if (cmp.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
								|| cmp.equals(SpineConfiguration.POLYGON_BORDER) || topY <= 0) {
							stateOut = true;
							break;
						}
						if(indAux==0){
							if(verificablePoint(topY,xxx) &&!landMap.findPoint(SpineMapHelper.formKey(topY, xxx)).getType().equals(" ")){
								if(insideMApVertical(topY,ejeX,2,1)){
									String data1="l-"+topY+"-"+xxx+"-"+(topY+sideSize)+"-"+xxx+"-"+
											(topY+sideSize)+"-"+(xxx-sideDepth)+"-"+topY+"-"+(xxx-sideDepth);
									landMap.findPoint(SpineMapHelper.formKey(topY,ejeX)).setGramaticalType(data1);
								}
								if(insideMApVertical(topY,xxx+sideDepth,2,1)){
									String data1="l-"+topY+"-"+(xxx+sideDepth)+"-"+(topY+sideSize)+"-"+(xxx+sideDepth)+"-"+
											(topY+sideSize)+"-"+(xxx-(sideDepth*2))+"-"+topY+"-"+(xxx-(sideDepth*2));
									
								}
								
							}
						}
						topY--;
					}
					aux = val1;
					val1 = val2;
					val2 = aux;
					if (topY <= 0)
						break;
					if (stateOut)
						break;
				}
				xxx += landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE;
				
			}
		}
		
		//SIDE RIGHT OF ARTERIAL BRANCH
		int ejeXX =0;
		int inc = 0;
		while (true) {
			if (landMap.findPoint(SpineMapHelper.formKey(yy - 1 + inc,2)).getType()
					.equals(SpineConfiguration.POLYGON_BORDER)) {
				break;
			}
			inc++;
			if (yy - 1 + inc > large)
				break;
		}
		dos = 0;
		
		if (inc >= landMap.getConfiguration().getBlockConfiguration().getSideSize()) {
			for (int i = 0; i < divisionResultBlock; i++) {
				int div = i / 9;
				int rem = i % 9;
				if (dos <= 2) {
					int ejeYY = xx - 11 + landMap.getConfiguration().getBlockConfiguration().getSideSize();

					for (int j = 0; j < (inc / landMap.getConfiguration().getBlockConfiguration().getSideSize()); j++) {
						int tope;
						if (j == 0) {
							// nro de lineas para el bloque
							tope = landMap.getConfiguration().getBlockConfiguration().getSideSize() - 26 - 10;
						} else {
							tope = landMap.getConfiguration().getBlockConfiguration().getSideSize();
						}

						int a = ejeYY - SpineConfiguration.LOCAL_BRANCH_SIZE + 10;
						int counter = 0;
						val1 = "1";
						val2 = "2";
						while (true) {
							for (int iii = 0; iii < landMap.getConfiguration().getLotConfiguration().getSideSize(); iii++) {
								if(iii==0){
									if( verificablePoint(a,ejeXX) && a>=0&&a<=large&& !landMap.findPoint(SpineMapHelper.formKey(a, ejeXX)).getType().equals(" ")){
										if(insideMApVertical(a,ejeXX,2,1)){
											String data1="l-"+a+"-"+ejeXX+"-"+(a+sideSize)+"-"+ejeXX+"-"+
													(a+sideSize)+"-"+(ejeXX-sideDepth)+"-"+a+"-"+(ejeXX-sideDepth);
											landMap.findPoint(SpineMapHelper.formKey(a,ejeXX)).setGramaticalType(data1);
										}
										if(insideMApVertical(a,ejeXX-sideDepth,2,1)){
											String data2="l-"+a+"-"+(ejeXX-sideDepth)+"-"+(a+sideSize)+"-"+(ejeXX-sideDepth)+"-"+
													(a+sideSize)+"-"+(ejeXX-(sideDepth*2))+"-"+a+"-"+(ejeXX-(sideDepth*2));
											landMap.findPoint(SpineMapHelper.formKey(a,ejeXX+sideDepth)).setGramaticalType(data2);
										}
									}
								}
								a--;
								counter++;
								
							}
							aux = val1;
							val1 = val2;
							val2 = aux;

							if (counter >= tope)
								break;
						}

						ejeYY += landMap.getConfiguration().getBlockConfiguration().getSideSize() + SpineConfiguration.LOCAL_BRANCH_SIZE;
					}
				} else {
					//PRINTING PARK RIGHT SIDE OF ARTERIAL BRANCH
					dos = 0;
					int ejeYY = xx - 11 + landMap.getConfiguration().getBlockConfiguration().getSideSize();
					for (int j = 0; j < (inc / landMap.getConfiguration().getBlockConfiguration().getSideSize()); j++) {
						String cambiable = SpineConfiguration.PARK_MARK;
						int randomNum;
						if (((xx) / landMap.getConfiguration().getBlockConfiguration().getSideSize()) == 0) {
							randomNum = 0;
						} else
							randomNum = ThreadLocalRandom.current().nextInt(0,
									(xx) / landMap.getConfiguration().getBlockConfiguration().getSideSize());

						if (cambiable.equals(SpineConfiguration.ADDINGS_MARK))
							cambiable = SpineConfiguration.PARK_MARK;
						if (div > 0 && rem == 0 && randomNum == j) {
							cambiable = SpineConfiguration.ADDINGS_MARK;
						}

						int tope;
						if (j == 0) {
							// nro de lineas para el bloque
							tope = landMap.getConfiguration().getBlockConfiguration().getSideSize() - SpineConfiguration.ARTERIAL_BRANCH_SIZE
									- nmbrParksLong - SpineConfiguration.LOCAL_BRANCH_SIZE * 2;
						} else {
							tope = landMap.getConfiguration().getBlockConfiguration().getSideSize() - nmbrParksLong
									- SpineConfiguration.LOCAL_BRANCH_SIZE;
						}
						// no esta imprimiendo
						int a = ejeYY  - SpineConfiguration.LOCAL_BRANCH_SIZE;
						for (int ii = 0; ii < nmbrParksLong; ii++) {
							if(ii==0){
								if (verificablePoint(a,ejeXX) &&  a<= large && a>=0 &&!landMap.findPoint(SpineMapHelper.formKey(a,ejeXX)).getType()
										.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)){
									if(verifyPArk(a-29,ejeXX,2,1)){
										String data1="g-"+(a-29)+"-"+ejeXX+"-"+((a-29)+nmbrParksLong)+"-"+ejeXX+"-"+((a-29)+nmbrParksLong)+"-"+(ejeXX-(sideDepth*2))+"-"+(a-29)+"-"+(ejeXX-(sideDepth*2));
										landMap.findPoint(SpineMapHelper.formKey(a-29,ejeXX)).setGramaticalType(data1);	
									}
									
								}
								
							}
							for (int jj = 0; jj < landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2; jj++) {
								if ((ejeYY-ii)<=large && !landMap.findPoint(SpineMapHelper.formKey(ejeYY - ii,ejeXX + jj)).getType()
										.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)){
									landMap.findPoint(SpineMapHelper.formKey(ejeYY - ii,ejeXX + jj))
									.setType(cambiable);
								}
									
								
								
							}
						}
						//print streets
						for (int ii = 0; ii < SpineConfiguration.LOCAL_BRANCH_SIZE; ii++) {
							for (int jj = 0; jj < landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2; jj++) {
								
								if ( verificablePoint(ejeYY - ii - nmbrParksLong,ejeXX + jj)&& (ejeYY - ii - nmbrParksLong)<width&&!landMap.findPoint(SpineMapHelper.formKey(ejeYY - ii - nmbrParksLong,ejeXX + jj))
										.getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
									landMap.findPoint(SpineMapHelper.formKey(ejeYY - ii - nmbrParksLong,ejeXX + jj))
											.setType(SpineConfiguration.ARTERIAL_MARK);
							}
						}
						a = ejeYY - nmbrParksLong - SpineConfiguration.LOCAL_BRANCH_SIZE;
						int counter = 0;

						val1 = "1";
						val2 = "2";
						while (true) {
							
							for (int iii = 0; iii < landMap.getConfiguration().getLotConfiguration().getSideSize(); iii++) {
								if(iii==0){
									if (verificablePoint(a,ejeXX) &&  a<= large && a>=0 &&!landMap.findPoint(SpineMapHelper.formKey(a,ejeXX)).getType()
											.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)){
										if(insideMApVertical(a,ejeXX,2,1)){
											String data1="l-"+a+"-"+ejeXX+"-"+(a+sideSize)+"-"+ejeXX+"-"+
													(a+sideSize)+"-"+(ejeXX-sideDepth)+"-"+a+"-"+(ejeXX-sideDepth);
											landMap.findPoint(SpineMapHelper.formKey(a,ejeXX)).setGramaticalType(data1);
										}
										if(insideMApVertical(a,ejeXX-sideDepth,2,1)){
											String data2="l-"+a+"-"+(ejeXX-sideDepth)+"-"+(a+sideSize)+"-"+(ejeXX-sideDepth)+"-"+
													(a+sideSize)+"-"+((ejeXX-sideDepth)-sideDepth)+"-"+a+"-"+((ejeXX-sideDepth)-sideDepth);
											landMap.findPoint(SpineMapHelper.formKey(a,ejeXX+sideDepth)).setGramaticalType(data2);
										}
										
									}
								}
								
								a--;
								counter++;
							}
							aux = val1;
							val1 = val2;
							val2 = aux;

							if (counter >= tope)
								break;
						}

						ejeYY += landMap.getConfiguration().getBlockConfiguration().getSideSize() + SpineConfiguration.LOCAL_BRANCH_SIZE;
					}
				}
				dos++;
				ejeXX += landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE;
			}

			// parche
			/*if (!((yy + 26 + landMap.getConfiguration().getBlockConfiguration().getSideSize()) > width)) {
				// int indexYDown=(yy-1)/landMap.getConfiguration().getBlockConfiguration().getSideSize();
				int indexYDown = (width - yy)
						/ (landMap.getConfiguration().getBlockConfiguration().getSideSize() + SpineConfiguration.LOCAL_BRANCH_SIZE);
				int yBegin = yy
						+ indexYDown * (landMap.getConfiguration().getBlockConfiguration().getSideSize() + SpineConfiguration.LOCAL_BRANCH_SIZE)
						- SpineConfiguration.LOCAL_BRANCH_SIZE;// begin
																// printting
				int nmbrLines = width - yBegin;
				int indexXLoop = 0;
				boolean state = false;
				for (int index = 0; index < divisionResultBlock; index++) {
					int counter = 0;
					val1 = "1";
					val2 = "2";
					while (true) {
						for (int i = 0; i < landMap.getConfiguration().getLotConfiguration().getSideSize(); i++) {
							for (int j = 0; j < landMap.getConfiguration().getLotConfiguration().getDepthSize(); j++) {
								if (!landMap.findPoint(SpineMapHelper.formKey(indexXLoop + j, yBegin + counter))
										.getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
										&& !landMap.findPoint(SpineMapHelper.formKey(indexXLoop + j, yBegin + counter))
												.getType().equals(SpineConfiguration.POLYGON_BORDER))
									landMap.findPoint(SpineMapHelper.formKey(indexXLoop + j, yBegin + counter))
											.setType(val1);
								if (!landMap
										.findPoint(SpineMapHelper.formKey(
												indexXLoop + j + landMap.getConfiguration().getLotConfiguration().getDepthSize(),
												yBegin + counter))
										.getType().equals(
												SpineConfiguration.OUTSIDE_POLYGON_MARK)
										&& !landMap
												.findPoint(SpineMapHelper.formKey(
														indexXLoop + j + landMap.getConfiguration().getLotConfiguration().getDepthSize(),
														yBegin + counter))
												.getType().equals(SpineConfiguration.POLYGON_BORDER))
									landMap.findPoint(SpineMapHelper.formKey(
											indexXLoop + j + landMap.getConfiguration().getLotConfiguration().getDepthSize(),
											yBegin + counter)).setType(val2);
							}
							counter++;
							if (counter > nmbrLines)
								break;
						}
						aux = val1;
						val1 = val2;
						val2 = aux;
						if (counter > nmbrLines)
							break;
					}

					indexXLoop += landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2
							+ SpineConfiguration.COLLECTOR_BRANCH_SIZE;
				}
			}*/

		}
		paintAportesVertical();
	}
	
	
	public void makeBorder(List<Integer> listData){
		String borderGrammar="g-";
		for(int i=0;i<listData.size();i+=2){
			borderGrammar+=listData.get(i)+"-"+listData.get(i+1)+"-";

		}
		landMap.findPoint(SpineMapHelper.formKey(listData.get(0),listData.get(1))).setGramaticalType(borderGrammar);
		
	}
	public void zonify() {
		//landMap.getConfiguration().getBlockConfiguration().getSideSize();
		
		int divisionResultBlock = large
				/ (SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE);
		setLongPark();
		factorAporte = divisionResultBlock / 9;
		int ejeY = yy - 1;
		int ejeX = 0;
		String val1, val2, aux;
		// bloques con parques arriba
		int dos = 0;
		if (yy >= landMap.getConfiguration().getBlockConfiguration().getSideSize()) {
			
			for (int i = 0; i < divisionResultBlock; i++) {
				int div = i / 9;
				int rem = i % 9;
				// if(i>10 && (i/10)>)
				if (dos <= 2) {
					int inc_Block = 0;
						
					for (int j = 0; j < ((yy) / landMap.getConfiguration().getBlockConfiguration().getSideSize()); j++) {
						int tope = landMap.getConfiguration().getBlockConfiguration().getSideSize() - 5;
						// print blocks
						int inc = 0;
						val1 = "1";
						val2 = "2";
						aux = "";
						while (true) {
							for (int k = 0; k < landMap.getConfiguration().getLotConfiguration().getSideSize(); k++) {
								if(k==0 && (ejeY - inc_Block - inc)>=0){
									if(verificablePoint(ejeX,ejeY - inc_Block - inc) &&  !landMap.findPoint(SpineMapHelper.formKey(ejeX , ejeY - inc_Block - inc)).getType().equals(" ")&&
											!landMap.findPoint(SpineMapHelper.formKey(ejeX +sideDepth, ejeY - inc_Block - inc)).getType().equals(" ")
											&& insideMApVertical(ejeX,( ejeY - inc_Block - inc),2,1)){
										String dato1="l-"+ejeX+"-"+( ejeY - inc_Block - inc)+"-"+(ejeX+sideDepth)+"-"+( ejeY - inc_Block - inc)+
												"-"+(ejeX+sideDepth)+"-"+( ejeY - inc_Block - inc+sideSize)+"-"+ejeX+"-"+( ejeY - inc_Block - inc+sideSize);
										landMap.findPoint(SpineMapHelper.formKey(ejeX,ejeY - inc_Block - inc)).setGramaticalType(dato1);
									}
									if(verificablePoint(ejeX+sideDepth,ejeY - inc_Block - inc) && !landMap.findPoint(SpineMapHelper.formKey((ejeX+ landMap.getConfiguration().getLotConfiguration().getDepthSize()) , ejeY - inc_Block - inc)).getType().equals(" ")&&
											!landMap.findPoint(SpineMapHelper.formKey((ejeX+ landMap.getConfiguration().getLotConfiguration().getDepthSize()+sideDepth) , ejeY - inc_Block - inc)).getType().equals(" ")
											&& insideMApVertical(ejeX+sideDepth,( ejeY - inc_Block - inc),2,1)){
										String dato2="l-"+(ejeX+sideDepth)+"-"+( ejeY - inc_Block - inc)+"-"+(ejeX+(sideDepth*2))+"-"+( ejeY - inc_Block - inc)+
												"-"+(ejeX+(sideDepth*2))+"-"+( ejeY - inc_Block - inc+sideSize)+"-"+(ejeX+sideDepth)+"-"+( ejeY - inc_Block - inc+sideSize);
										landMap.findPoint(SpineMapHelper.formKey(ejeX+15,ejeY - inc_Block - inc)).setGramaticalType(dato2);
									}
								}
								inc++;
							}
							// swap
							aux = val1;
							val1 = val2;
							val2 = aux;
							if (inc > tope)
								break;

						}
						// pinto calle

						inc_Block += landMap.getConfiguration().getBlockConfiguration().getSideSize() + SpineConfiguration.LOCAL_BRANCH_SIZE;
					}

				} else {
					dos = 0;

					int inc_Block = 0;

					for (int j = 0; j < ((yy) / landMap.getConfiguration().getBlockConfiguration().getSideSize()); j++) {
						String cambiable = SpineConfiguration.PARK_MARK;
						int randomNum = ThreadLocalRandom.current().nextInt(0,
								(yy) / landMap.getConfiguration().getBlockConfiguration().getSideSize());

						if (cambiable.equals(SpineConfiguration.ADDINGS_MARK))
							cambiable = SpineConfiguration.PARK_MARK;
						if (div > 0 && rem == 0 && randomNum == j) {
							cambiable = SpineConfiguration.ADDINGS_MARK;
						}

						int tope = landMap.getConfiguration().getBlockConfiguration().getSideSize() - SpineConfiguration.LOCAL_BRANCH_SIZE
								- nmbrParksLong;
						// print blocks
						int inc = 0;
						val1 = "1";
						val2 = "2";
						aux = "";
						while (true) {
							for (int k = 0; k < landMap.getConfiguration().getLotConfiguration().getSideSize(); k++) {
								if(k==0 && (ejeY - inc_Block - inc)>=0){
									if(verificablePoint(ejeX,ejeY - inc_Block - inc) &&!landMap.findPoint(SpineMapHelper.formKey(ejeX , ejeY - inc_Block - inc)).getType().equals(" ")&&
											!landMap.findPoint(SpineMapHelper.formKey(ejeX +sideDepth, ejeY - inc_Block - inc)).getType().equals(" ")
											&& insideMApVertical(ejeX,( ejeY - inc_Block - inc),2,1)){
										String dato1="l-"+ejeX+"-"+( ejeY - inc_Block - inc)+"-"+(ejeX+sideDepth)+"-"+( ejeY - inc_Block - inc)+
												"-"+(ejeX+sideDepth)+"-"+( ejeY - inc_Block - inc+sideSize)+"-"+ejeX+"-"+( ejeY - inc_Block - inc+sideSize);
										landMap.findPoint(SpineMapHelper.formKey(ejeX,ejeY - inc_Block - inc)).setGramaticalType(dato1);
									}
									if(verificablePoint(ejeX+sideDepth,ejeY - inc_Block - inc) &&!landMap.findPoint(SpineMapHelper.formKey((ejeX+ landMap.getConfiguration().getLotConfiguration().getDepthSize()) , ejeY - inc_Block - inc)).getType().equals(" ")&&
											!landMap.findPoint(SpineMapHelper.formKey((ejeX+ landMap.getConfiguration().getLotConfiguration().getDepthSize()+sideDepth) , ejeY - inc_Block - inc)).getType().equals(" ")
											&& insideMApVertical(ejeX+sideDepth,( ejeY - inc_Block - inc),2,1)){
										String dato2="l-"+(ejeX+sideDepth)+"-"+( ejeY - inc_Block - inc)+"-"+(ejeX+(sideDepth*2))+"-"+( ejeY - inc_Block - inc)+
												"-"+(ejeX+(sideDepth*2))+"-"+( ejeY - inc_Block - inc+sideSize)+"-"+(ejeX+sideDepth)+"-"+( ejeY - inc_Block - inc+sideSize);
										landMap.findPoint(SpineMapHelper.formKey(ejeX+sideDepth,ejeY - inc_Block - inc)).setGramaticalType(dato2);
									}
								}
								
								inc++;
							}
							// swap
							aux = val1;
							val1 = val2;
							val2 = aux;
							if (inc > tope)
								break;
						}
						// pinto calle
						for (int k = 0; k < SpineConfiguration.LOCAL_BRANCH_SIZE; k++) {
							
							
							for (int l = 0; l < landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2; l++) {
								if (!landMap.findPoint(SpineMapHelper.formKey(ejeX + l, ejeY - inc_Block - inc))
										.getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
									landMap.findPoint(SpineMapHelper.formKey(ejeX + l, ejeY - inc_Block - inc))
											.setType(SpineConfiguration.ARTERIAL_MARK);
							}
							inc++;
						}
						
						for (int k = 0; k < nmbrParksLong; k++) {
							if(k==0 && (ejeY - inc_Block - inc)>=0){
								if(verificablePoint(ejeX,ejeY - inc_Block - inc) &&!landMap.findPoint(SpineMapHelper.formKey(ejeX,ejeY - inc_Block - inc)).getType().equals(" ")){
										String data1="g-"+ejeX+"-"+(ejeY - inc_Block - inc+10)+"-"+(ejeX+2*15)+"-"+(ejeY - inc_Block - inc+10)+"-"+
										(ejeX+2*15)+"-"+(ejeY - inc_Block - inc-nmbrParksLong+10	)+"-"+ejeX+"-"+(ejeY - inc_Block - inc-nmbrParksLong+10);
										landMap.findPoint(SpineMapHelper.formKey(ejeX,ejeY - inc_Block - inc)).setGramaticalType(data1);
								}
								
							}
							for (int l = 0; l < landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2; l++) {
								
								if ((ejeY - inc_Block - inc)>=0 && !landMap.findPoint(SpineMapHelper.formKey(ejeX + l, ejeY - inc_Block - inc))
										.getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)){
									landMap.findPoint(SpineMapHelper.formKey(ejeX + l, ejeY - inc_Block - inc))
									.setType(cambiable);
									
									
								}
									
							}
							inc++;
						}

						inc_Block += landMap.getConfiguration().getBlockConfiguration().getSideSize() + SpineConfiguration.LOCAL_BRANCH_SIZE;
					}

				}

				ejeX += landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE;
				dos++;

			}
			// side down
			int indexUp = 0;
			int a = (yy - 1) - ((yy - 1) / (landMap.getConfiguration().getBlockConfiguration().getSideSize() + SpineConfiguration.LOCAL_BRANCH_SIZE)
					* landMap.getConfiguration().getBlockConfiguration().getSideSize() + SpineConfiguration.LOCAL_BRANCH_SIZE);
			int topeDescount = a - SpineConfiguration.LOCAL_BRANCH_SIZE;
			String v1,v2;
			for(int index = 0; index < divisionResultBlock; index++) {
				v1= landMap.findPoint(SpineMapHelper.formKey(indexUp,topeDescount+1)).getType();
				if(v1.equals("p")||v1.equals("1")||v1.equals("2")){
					topeDescount-=SpineConfiguration.LOCAL_BRANCH_SIZE;
					break;
				}
				indexUp+=landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE;;
			}
			indexUp=0;
			for(int index = 0; index < divisionResultBlock; index++) {
				int base=topeDescount;
				val1="1";
				val2="2";
				while(true){
					for (int i = 0; i < landMap.getConfiguration().getLotConfiguration().getSideSize(); i++) {
						if(i==0){
							if(verificablePoint(indexUp,base) && !landMap.findPoint(SpineMapHelper.formKey(indexUp , base)).getType().equals(" ")&&
									!landMap.findPoint(SpineMapHelper.formKey(indexUp +sideDepth, base)).getType().equals(" ")
									&& insideMApVertical(ejeX,base,1,2)){
								String dato1="l-"+indexUp+"-"+base+"-"+(indexUp+sideDepth)+"-"+base+
										"-"+(indexUp+sideDepth)+"-"+( base+sideSize)+"-"+indexUp+"-"+( base+sideSize);
								landMap.findPoint(SpineMapHelper.formKey(indexUp,base)).setGramaticalType(dato1);
							}
							if(verificablePoint(indexUp+sideDepth,base) &&!landMap.findPoint(SpineMapHelper.formKey((indexUp+ landMap.getConfiguration().getLotConfiguration().getDepthSize()) , base)).getType().equals(" ")&&
									!landMap.findPoint(SpineMapHelper.formKey((indexUp+ landMap.getConfiguration().getLotConfiguration().getDepthSize()+sideDepth) , base)).getType().equals(" ")
									&& insideMApVertical(indexUp+sideDepth,base,1,2)){
								String dato2="l-"+(indexUp+sideDepth)+"-"+base+"-"+(indexUp+(sideDepth*2))+"-"+( base)+
										"-"+(indexUp+(sideDepth*2))+"-"+( base+sideSize)+"-"+(indexUp+sideDepth)+"-"+( base+sideSize);
								landMap.findPoint(SpineMapHelper.formKey(indexUp+sideDepth,base)).setGramaticalType(dato2);
							}
						}
						base--;
						if(base<0)break;
					}
					aux = val1;
					val1 = val2;
					val2 = aux;
					
					if(base<0)break;
				}
				indexUp+=landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE;;
			}
				} else {
			/// parche
			// aqui imprimimos si esta parte no mide ni si quiera un bloque
			int xxx = 0;
			for (int i = 0; i < divisionResultBlock; i++) {
				int topY = yy - 1;
				boolean stateOut = false;
				val1 = "1";
				val2 = "2";
				while (true) {
					for (int indAux = 0; indAux < landMap.getConfiguration().getLotConfiguration().getSideSize(); indAux++) {
						
						
						String cmp = landMap.findPoint(SpineMapHelper.formKey(xxx, topY)).getType();
						if (cmp.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
								|| cmp.equals(SpineConfiguration.POLYGON_BORDER) || topY <= 0) {
							stateOut = true;
							break;
						}
						if(indAux==0){
							if(verificablePoint(xxx,topY) &&!landMap.findPoint(SpineMapHelper.formKey(xxx, topY)).getType().equals(" ")){
								if(insideMApVertical(xxx,topY,1,2)){
									String dato1="l-"+xxx+"-"+topY+"-"+(xxx+sideDepth)+"-"+topY+
											"-"+(xxx+sideDepth)+"-"+( topY+sideSize)+"-"+xxx+"-"+( topY+sideSize);
								}
							}
							if(verificablePoint(xxx+sideDepth,topY) &&!landMap.findPoint(SpineMapHelper.formKey(xxx+sideDepth, topY)).getType().equals(" ")){
								if(insideMApVertical(xxx+sideDepth,topY,1,2)){
									String dato2="l-"+(xxx+sideDepth)+"-"+topY+"-"+(xxx+(sideDepth*2))+"-"+topY+
											"-"+(xxx+(sideDepth*2))+"-"+( topY+sideSize)+"-"+(xxx+sideDepth)+"-"+( topY+sideSize);
									
								}
							}
							
						}
						topY--;
					}
					aux = val1;
					val1 = val2;
					val2 = aux;
					if (topY <= 0)
						break;
					if (stateOut)
						break;
				}
				xxx += landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE;
				;
			}
		}
		// landMap.findPoint(SpineMapHelper.formKey(xx,yy-1)).setType("9");
		// landMap.findPoint(SpineMapHelper.formKey(xx,yy-2)).setType("9");

		// BLOCKS UNDER THE ARTERIAL BRANCH(COLOUR MAP)

		int ejeXX = 0;
		int inc = 0;
		while (true) {
			if (landMap.findPoint(SpineMapHelper.formKey(xx, yy - 1 + inc)).getType()
					.equals(SpineConfiguration.POLYGON_BORDER)) {
				break;
			}
			inc++;
			if (yy - 1 + inc > width)
				break;
		}

		dos = 0;
		
		if (inc >= landMap.getConfiguration().getBlockConfiguration().getSideSize()) {
			for (int i = 0; i < divisionResultBlock; i++) {
				int div = i / 9;
				int rem = i % 9;
				if (dos <= 2) {
					int ejeYY = yy - 11 + landMap.getConfiguration().getBlockConfiguration().getSideSize();

					for (int j = 0; j < (inc / landMap.getConfiguration().getBlockConfiguration().getSideSize()); j++) {
						int tope;
						if (j == 0) {
							// nro de lineas para el bloque
							tope = landMap.getConfiguration().getBlockConfiguration().getSideSize() - 26 - 10;
						} else {
							tope = landMap.getConfiguration().getBlockConfiguration().getSideSize();
						}

						int a = ejeYY - SpineConfiguration.LOCAL_BRANCH_SIZE + 10;
						int counter = 0;
						val1 = "1";
						val2 = "2";
						while (true) {
							for (int iii = 0; iii < landMap.getConfiguration().getLotConfiguration().getSideSize(); iii++) {
								if(iii==0){
									if(verificablePoint(ejeXX,a) &&!landMap.findPoint(SpineMapHelper.formKey(ejeXX ,a)).getType().equals(" ")){
										if(insideMApVertical(ejeXX,a,1,2)){
											String dato1="l-"+ejeXX+"-"+a+"-"+(ejeXX+sideDepth)+"-"+a+"-"+(ejeXX+sideDepth)+"-"+(a+sideSize)+"-"+ejeXX+"-"+(a+sideSize);
											landMap.findPoint(SpineMapHelper.formKey(ejeXX , a)).setGramaticalType(dato1);
										}
										if(insideMApVertical(ejeXX+sideDepth,a,1,2)){
											String dato2="l-"+(ejeXX+sideDepth)+"-"+a+"-"+(ejeXX+sideDepth*2)+"-"+a+"-"+(ejeXX+sideDepth*2)+"-"+(a+sideSize)+"-"+(ejeXX+sideDepth)+"-"+(a+sideSize);
											landMap.findPoint(SpineMapHelper.formKey(ejeXX+sideDepth , a)).setGramaticalType(dato2);
										}
									}
									
								}
								a--;
								counter++;
								
							}
							aux = val1;
							val1 = val2;
							val2 = aux;

							if (counter >= tope)
								break;
						}

						ejeYY += landMap.getConfiguration().getBlockConfiguration().getSideSize() + SpineConfiguration.LOCAL_BRANCH_SIZE;
					}
				} else {
					//here print parks
					dos = 0;
					int ejeYY = yy - 11 + landMap.getConfiguration().getBlockConfiguration().getSideSize();
					for (int j = 0; j < (inc / landMap.getConfiguration().getBlockConfiguration().getSideSize()); j++) {
						String cambiable = SpineConfiguration.PARK_MARK;
						int randomNum;
						if (((yy) / landMap.getConfiguration().getBlockConfiguration().getSideSize()) == 0) {
							randomNum = 0;
						} else
							randomNum = ThreadLocalRandom.current().nextInt(0,
									(yy) / landMap.getConfiguration().getBlockConfiguration().getSideSize());

						if (cambiable.equals(SpineConfiguration.ADDINGS_MARK))
							cambiable = SpineConfiguration.PARK_MARK;
						if (div > 0 && rem == 0 && randomNum == j) {
							cambiable = SpineConfiguration.ADDINGS_MARK;
						}

						int tope;
						if (j == 0) {
							// nro de lineas para el bloque
							tope = landMap.getConfiguration().getBlockConfiguration().getSideSize() - SpineConfiguration.ARTERIAL_BRANCH_SIZE
									- nmbrParksLong - SpineConfiguration.LOCAL_BRANCH_SIZE * 2;
						} else {
							tope = landMap.getConfiguration().getBlockConfiguration().getSideSize() - nmbrParksLong
									- SpineConfiguration.LOCAL_BRANCH_SIZE;
						}

						for (int ii = 0; ii < nmbrParksLong; ii++) {
							if(ii==0 ){
								if(verificablePoint(ejeXX,ejeYY - ii) && !landMap.findPoint(SpineMapHelper.formKey(ejeXX, ejeYY - ii)).getType().equals(" ")){
									if(verificablePoint(ejeXX, ejeYY - ii) && verifyPArk(ejeXX, ejeYY - ii,1,2)){
										String grass="g-"+ejeXX+"-"+(ejeYY - ii-40)+"-"+(ejeXX+sideDepth*2)+"-"+(ejeYY - ii-40)+"-"+(ejeXX+sideDepth*2)+"-"+(ejeYY - ii+nmbrParksLong-40)+"-"+ejeXX+"-"+(ejeYY - ii+nmbrParksLong-40);
										landMap.findPoint(SpineMapHelper.formKey(ejeXX ,  ejeYY - ii)).setGramaticalType(grass);
									}
									
								}
									
							}
							
							
							for (int jj = 0; jj < landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2; jj++) {
								
								if (verificablePoint(ejeXX + jj,ejeYY - ii) && !landMap.findPoint(SpineMapHelper.formKey(ejeXX + jj, ejeYY - ii)).getType()
										.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)){
									landMap.findPoint(SpineMapHelper.formKey(ejeXX + jj, ejeYY - ii))
									.setType(cambiable);
									/*landMap.findPoint(SpineMapHelper.formKey(ejeXX + jj, ejeYY - ii)).setGramaticalType("l-"
											+(ejeXX + jj)+"-"+( ejeYY - ii)+"-1-2-"+1+"-"+1);*/
								}
									
								
								
							}
						}

						for (int ii = 0; ii < SpineConfiguration.LOCAL_BRANCH_SIZE; ii++) {
							for (int jj = 0; jj < landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2; jj++) {
								if (!landMap.findPoint(SpineMapHelper.formKey(ejeXX + jj, ejeYY - ii - nmbrParksLong))
										.getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
									landMap.findPoint(SpineMapHelper.formKey(ejeXX + jj, ejeYY - ii - nmbrParksLong))
											.setType(SpineConfiguration.ARTERIAL_MARK);
							}
						}
						int a = ejeYY - nmbrParksLong - SpineConfiguration.LOCAL_BRANCH_SIZE;
						int counter = 0;

						val1 = "1";
						val2 = "2";
						while (true) {
							
							for (int iii = 0; iii < landMap.getConfiguration().getLotConfiguration().getSideSize(); iii++) {
								if(iii==0 && !landMap.findPoint(SpineMapHelper.formKey(ejeXX ,a)).getType().equals(" ")){
									if(verificablePoint(ejeXX, a) && !landMap.findPoint(SpineMapHelper.formKey(ejeXX ,a)).getType().equals(" ")){
										if(insideMApVertical(ejeXX,a,1,2)){
											String dato1="l-"+ejeXX+"-"+a+"-"+(ejeXX+sideDepth)+"-"+a+"-"+(ejeXX+sideDepth)+"-"+(a+sideSize)+"-"+ejeXX+"-"+(a+sideSize);
											landMap.findPoint(SpineMapHelper.formKey(ejeXX , a)).setGramaticalType(dato1);
										}
										if(insideMApVertical(ejeXX+sideDepth,a,1,2)){
											String dato2="l-"+(ejeXX+sideDepth)+"-"+a+"-"+(ejeXX+sideDepth*2)+"-"+a+"-"+(ejeXX+sideDepth*2)+"-"+(a+sideSize)+"-"+(ejeXX+sideDepth)+"-"+(a+sideSize);
											landMap.findPoint(SpineMapHelper.formKey(ejeXX+sideDepth , a)).setGramaticalType(dato2);
										}
									}
									
								}
								
								a--;
								counter++;
							}
							aux = val1;
							val1 = val2;
							val2 = aux;

							if (counter >= tope)
								break;
						}

						ejeYY += landMap.getConfiguration().getBlockConfiguration().getSideSize() + SpineConfiguration.LOCAL_BRANCH_SIZE;
					}
				}
				dos++;
				ejeXX += landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE;
			}

			// parche
			if (!((yy + 26 + landMap.getConfiguration().getBlockConfiguration().getSideSize()) > width)) {
				// int indexYDown=(yy-1)/landMap.getConfiguration().getBlockConfiguration().getSideSize();
				int indexYDown = (width - yy)
						/ (landMap.getConfiguration().getBlockConfiguration().getSideSize() + SpineConfiguration.LOCAL_BRANCH_SIZE);
				int yBegin = yy
						+ indexYDown * (landMap.getConfiguration().getBlockConfiguration().getSideSize() + SpineConfiguration.LOCAL_BRANCH_SIZE)
						- SpineConfiguration.LOCAL_BRANCH_SIZE;// begin
																// printting
				int nmbrLines = width - yBegin;
				int indexXLoop = 0;
				boolean state = false;
				for (int index = 0; index < divisionResultBlock; index++) {
					int counter = 0;
					val1 = "1";
					val2 = "2";
					while (true) {
						for (int i = 0; i < landMap.getConfiguration().getLotConfiguration().getSideSize(); i++) {
							if(i==0 && !landMap.findPoint(SpineMapHelper.formKey(indexXLoop , yBegin + counter)).getType().equals(" ")){
								if(verificablePoint(indexXLoop, (yBegin + counter)) && !landMap.findPoint(SpineMapHelper.formKey(indexXLoop ,(yBegin + counter))).getType().equals(" ")){
									if(insideMApVertical(indexXLoop,yBegin + counter,1,2)){
										String dato1="l-"+indexXLoop+"-"+(yBegin + counter)+"-"+(indexXLoop+sideDepth)+"-"+(yBegin + counter)+"-"+(indexXLoop+sideDepth)+"-"+((yBegin + counter)+sideSize)+"-"+indexXLoop+"-"+((yBegin + counter)+sideSize);
										landMap.findPoint(SpineMapHelper.formKey(ejeXX , (yBegin + counter))).setGramaticalType(dato1);
									}
									if(insideMApVertical(indexXLoop+sideDepth,(yBegin + counter),1,2)){
										String dato2="l-"+(indexXLoop+sideDepth)+"-"+(yBegin + counter)+"-"+(indexXLoop+sideDepth*2)+"-"+(yBegin + counter)+"-"+(indexXLoop+sideDepth*2)+"-"+((yBegin + counter)+sideSize)+"-"+(indexXLoop+sideDepth)+"-"+((yBegin + counter)+sideSize);
										landMap.findPoint(SpineMapHelper.formKey(indexXLoop+sideDepth , (yBegin + counter))).setGramaticalType(dato2);
									}
								}
								
							}
							
							counter++;
							if (counter > nmbrLines)
								break;
						}
						aux = val1;
						val1 = val2;
						val2 = aux;
						if (counter > nmbrLines)
							break;
					}

					indexXLoop += landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2
							+ SpineConfiguration.COLLECTOR_BRANCH_SIZE;
				}
			}

		} else {
			//IF THE SECTOR UNDER THE ARTERIAL BRANCH ON VISIBLE MAP SIZE LESS THAN A BLOCK
			// EN CASO DE LA ARTERIAL BRANCH AL EXTREMO HAYA MENOS DE UN BLOQUE
			// TRABAJAMOS CASO ESPECIAL
			ejeXX = 0;
			for (int i = 0; i < divisionResultBlock; i++) {
				int topY = yy + 26;
				boolean stateOut = false;
				val1 = "1";
				val2 = "2";
				while (true) {
					if(topY>width)break;
					for (int ii = 0; ii < landMap.getConfiguration().getLotConfiguration().getSideSize(); ii++) {
						String cmp = landMap.findPoint(SpineMapHelper.formKey(ejeXX, topY)).getType();
						if (cmp.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
								|| cmp.equals(SpineConfiguration.POLYGON_BORDER) || topY > width) {
							stateOut = true;
							break;
						}
						if(ii==0){
							if(verificablePoint(ejeXX, topY) && insideMApVertical(ejeXX,topY,1,2)){
								String dato1="l-"+ejeXX+"-"+topY+"-"+(ejeXX+sideDepth)+"-"+topY+"-"+(ejeXX+sideDepth)+"-"+(topY+sideSize)+"-"+ejeXX+"-"+(topY+sideSize);
								landMap.findPoint(SpineMapHelper.formKey(ejeXX , topY)).setGramaticalType(dato1);
							}
							if(verificablePoint(ejeXX+sideDepth, topY) &&insideMApVertical(ejeXX+sideDepth,topY,1,2)){
								String dato2="l-"+(ejeXX+sideDepth)+"-"+topY+"-"+(ejeXX+sideDepth*2)+"-"+topY+"-"+(ejeXX+sideDepth*2)+"-"+(topY+sideSize)+"-"+(ejeXX+sideDepth)+"-"+(topY+sideSize);
								landMap.findPoint(SpineMapHelper.formKey(ejeXX+sideDepth ,topY)).setGramaticalType(dato2);
							}
							
							
						}
						topY++;
					}
					aux = val1;
					val1 = val2;
					val2 = aux;
					if (topY > width)
						break;
					if (stateOut)
						break;
				}

				ejeXX += landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE;
			}
		}

		paintAportes();

	}
	private void paintAportesVertical(){
		int tope, ejeX, limite, divisionResultBlock;
		String val1, val2, aux;
		divisionResultBlock = width
				/ (landMap.getConfiguration().getLotConfiguration().getSideSize()  * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE);
		tope = 1;
		ejeX = 0;
		for (int i = 0; i < divisionResultBlock; i++)
			ejeX += landMap.getConfiguration().getLotConfiguration().getSideSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE;
		limite = width - ejeX;
		int newlongHouse = limite / 2;
		String comparableValue, comparableValue2;
		if((ejeX<0 ||ejeX>large) )return;
		if (newlongHouse >= landMap.getConfiguration().getLotConfiguration().getSideSize()) {
			while (true) {
				comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, tope)).getType();
				if (!comparableValue.equals(SpineConfiguration.ARTERIAL_MARK)) {
					comparableValue = landMap
							.findPoint(SpineMapHelper.formKey(tope + SpineConfiguration.LOCAL_BRANCH_SIZE,ejeX ))
							.getType();
					int printBackIndex = tope - 1;
					tope += SpineConfiguration.LOCAL_BRANCH_SIZE;
					int beginCounter = 0;
					boolean stateOut = false;
					val1 = "1";
					val2 = "2";
					int counterGram=0;
					boolean bol=true;
					while (true) {
						if(verificablePoint(printBackIndex,ejeX) && verificablePoint(printBackIndex,ejeX +sideDepth) && !landMap.findPoint(SpineMapHelper.formKey(printBackIndex,ejeX)).getType().equals(" ") &&counterGram<3 &&
								!landMap.findPoint(SpineMapHelper.formKey(printBackIndex,ejeX+sideDepth)).getType().equals(" ") && bol){
							if(insideMApVertical(printBackIndex,ejeX,1,2)){
								
								String data1="l-"+printBackIndex+"-"+ejeX+"-"+(printBackIndex+sideSize)+"-"+ejeX+"-"+
										(printBackIndex+sideSize)+"-"+(ejeX-sideDepth)+"-"+printBackIndex+"-"+(ejeX-sideDepth);
								landMap.findPoint(SpineMapHelper.formKey(printBackIndex,ejeX)).setGramaticalType(data1);
								
							}
						}
						//if(bol!=true)bol=true;
							
						for (int i = 0; i < SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE; i++) {
							comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, printBackIndex)).getType();
							if (comparableValue.equals(".") || comparableValue.equals(" ") || printBackIndex < 1
									|| comparableValue.equals("a")) {
								stateOut = true;
								break;
							}
							printBackIndex--;
							beginCounter++;
						}
						aux = val1;
						val1 = val2;
						val2 = aux;
						if (stateOut)
							break;
						if (beginCounter > SpineConfiguration.BASE_CLUSTER_SIZE)
							break;
						counterGram++;
					}

					// prin blocks
				} else if (comparableValue.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
						|| comparableValue.equals(SpineConfiguration.BORDER_MARK))
					break;
				tope++;
				if (tope > yy)
					break;
			}
			comparableValue = landMap
					.findPoint(SpineMapHelper.formKey(ejeX, yy + SpineConfiguration.ARTERIAL_BRANCH_SIZE)).getType();
			tope = yy + SpineConfiguration.ARTERIAL_BRANCH_SIZE;

			if (!comparableValue.equals(" ") && !comparableValue.equals("a") && !comparableValue.equals(".")) {
				// landMap.findPoint(SpineMapHelper.formKey(ejeX,tope)).setType("9");//
				// auxiliar print block
				int idCont = 0;
				int yyy = tope;
				val1 = "1";
				val2 = "2";
				boolean stateOut = false;
				while (true) {
					for (int i = 0; i < SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE; i++) {
						comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, yyy)).getType();
						if (comparableValue.equals(SpineConfiguration.WALK_MARK)
								|| comparableValue.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
								|| comparableValue.equals(SpineConfiguration.POLYGON_BORDER)) {
							stateOut = true;
							break;
						}
						idCont++;
						yyy++;
					}
					if (stateOut)
						break;
					aux = val1;
					val1 = val2;
					val2 = aux;
					if (idCont > (SpineConfiguration.BASE_CLUSTER_SIZE - SpineConfiguration.ARTERIAL_BRANCH_SIZE
							- SpineConfiguration.LOCAL_BRANCH_SIZE))
						break;
				}

				while (true) {
					tope++;
					if (tope > width)
						break;
					comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, tope)).getType();
					if (comparableValue.equals(" ") || comparableValue.equals("."))
						break;
					if (comparableValue.equals(SpineConfiguration.ARTERIAL_MARK)) {
						// int printBackIndex=tope-1;
						tope += SpineConfiguration.LOCAL_BRANCH_SIZE;
						if (tope > width)
							break;
						val1 = "1";
						val2 = "2";
						stateOut = false;
						idCont = 0;
						yyy = tope;
						while (true) {
							for (int i = 0; i < SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE; i++) {
								comparableValue = landMap.findPoint(SpineMapHelper.formKey(ejeX, yyy)).getType();
								if (comparableValue.equals(SpineConfiguration.WALK_MARK)
										|| comparableValue.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
										|| comparableValue.equals(SpineConfiguration.POLYGON_BORDER)) {
									stateOut = true;
									break;
								}
								idCont++;
								yyy++;
							}
							if (stateOut)
								break;
							aux = val1;
							val1 = val2;
							val2 = aux;
							if (idCont > SpineConfiguration.BASE_CLUSTER_SIZE)
								break;
						}

						// landMap.findPoint(SpineMapHelper.formKey(ejeX,tope)).setType("9");
					}
					if (tope > width)
						break;

				}
			}

		}
		
	}
	private boolean insideMApVertical(int x,int y, int a, int b){
		//verify sense
		if(a==2 && b==1){
			if( (x-sideSize)<0 ||(x+sideSize)>large ||(y-this.sideDepth)<0 ||(y+sideDepth)>width )return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x,y)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x,y)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x+sideSize,y)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x+sideSize,y)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x-sideSize,y)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x-sideSize,y)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x,y+sideDepth)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x,y+sideDepth)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x,y-sideDepth)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x,y-sideDepth)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x+sideSize,y+sideDepth)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x+sideSize,y+sideDepth)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x-sideSize,y-sideDepth)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x-sideSize,y-sideDepth)).getType().equals("."))return false;
		}
		if(a==1 && b==2){
			if( (x-sideDepth)<0 ||(x+sideDepth)>large ||(y-this.sideSize)<0 ||(y+sideSize)>width )return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x,y)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x,y)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x,y+sideSize)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x,y+sideSize)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x,y-sideSize)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x,y-sideSize)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x+sideDepth,y)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x+sideDepth,y)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x-sideDepth,y)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x-sideDepth,y)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x+sideDepth,y+sideSize)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x+sideDepth,y+sideSize)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x-sideDepth,y-sideSize)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x-sideDepth,y-sideSize)).getType().equals("."))return false;
		}
		
		return true;
	}
	private boolean verifyPArk(int x,int y, int a, int b){
		//verify sense
		if(a==2 && b==1){
			if( (x-nmbrParksLong)<0 ||(x+nmbrParksLong)>large ||(y-(sideDepth*2))<0 ||(y+(sideDepth*2))>width )return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x,y)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x,y)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x+nmbrParksLong,y)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x+nmbrParksLong,y)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x-nmbrParksLong,y)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x-nmbrParksLong,y)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x,y+(sideDepth*2))).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x,y+(sideDepth*2))).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x,y-(sideDepth*2))).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x,y-(sideDepth*2))).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x+nmbrParksLong,y+(sideDepth*2))).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x+nmbrParksLong,y+(sideDepth*2))).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x-nmbrParksLong,y-(sideDepth*2))).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x-nmbrParksLong,y-(sideDepth*2))).getType().equals("."))return false;
		}
		
		if(a==1 && b==2){
			if( (x-nmbrParksLong)<0 ||(x+nmbrParksLong)>large ||(y-(sideDepth*2))<0 ||(y+(sideDepth*2))>width )return false;
			/*if( (x-sideDepth*2)<0 ||(x+sideDepth*2)>large ||(y-nmbrParksLong)<0 ||(y+nmbrParksLong)>width )return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x,y)).getType().equals(" ")||
					landMap.findPoint(SpineMapHelper.formKey(x+sideDepth*2,y)).getType().equals(".")||
					landMap.findPoint(SpineMapHelper.formKey(x,y+nmbrParksLong)).getType().equals(".")||
					landMap.findPoint(SpineMapHelper.formKey(x+sideDepth*2,y+nmbrParksLong)).getType().equals(".")
					)return false;*/
			//if(!landMap.landPointisOnMap(x)||!landMap.landPointisOnMap(x+sideDepth*2))return false;
			/*if(landMap.findPoint(SpineMapHelper.formKey(x,y)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x,y)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x,y+nmbrParksLong)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x,y+nmbrParksLong)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x,y-nmbrParksLong)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x,y-nmbrParksLong)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x+(sideDepth*2),y)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x+(sideDepth*2),y)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x-(sideDepth*2),y)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x-(sideDepth*2),y)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x+(sideDepth*2),y+nmbrParksLong)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x+sideDepth*2,y+nmbrParksLong)).getType().equals("."))return false;
			if(landMap.findPoint(SpineMapHelper.formKey(x-(sideDepth*2),y+nmbrParksLong)).getType().equals(" ")||landMap.findPoint(SpineMapHelper.formKey(x-sideDepth*2,y+nmbrParksLong)).getType().equals("."))return false;*/
		}
		return true;
	}
	private boolean verificablePoint(int pointX,int pointY){
		if(pointX<0 ||pointX>large)return false;
		if(pointY<0 ||pointY>width)return false;
		return true;
	}
}
