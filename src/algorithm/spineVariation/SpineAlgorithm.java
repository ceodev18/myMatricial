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
				/ (SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE);
		tope = 1;
		ejeX = 0;
		for (int i = 0; i < divisionResultBlock; i++)
			ejeX += SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE;
		limite = large - ejeX;
		int newlongHouse = limite / 2;
		String comparableValue, comparableValue2;

		if (newlongHouse >= SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE) {
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
					while (true) {
						if(!landMap.findPoint(SpineMapHelper.formKey(ejeX,printBackIndex)).getType().equals(" ") &&counterGram<3 &&
								!landMap.findPoint(SpineMapHelper.formKey(ejeX+sideDepth,printBackIndex)).getType().equals(" ") && bol){
							
							landMap.findPoint(SpineMapHelper.formKey(ejeX, printBackIndex)).setGramaticalType("l-"
									+ejeX+"-"+(printBackIndex+6)+"-1-2-"+sideSize+"-"+newlongHouse);
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
							if (idCont > SpineConfiguration.BASE_CLUSTER_SIZE)
								break;
						}

						// landMap.findPoint(SpineMapHelper.formKey(ejeX,tope)).setType("9");
					}
					if (tope > width)
						break;

				}
			}

		} else if (limite > 10 && limite < 15) {
			int topAux = (SpineConfiguration.HOUSE_DEPTH_MINIMUN_SIZE * SpineConfiguration.HOUSE_SIDE_MINIMUN_SIZE)
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
						if (beginCounter > SpineConfiguration.BASE_CLUSTER_SIZE)
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

	public void spineizeV2() {
		int directionBranch = SpineDirectionHelper.orthogonalDirectionFromPointToPoint(new SpineLandPoint(xx, yy),
				landMap.getCentroid());
		if(directionBranch==2 || directionBranch==0){
			int divisionResultBlock = large
					/ (SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE);
			int ejeX = SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE * 2;// rev
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

				ejeX += (SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE);
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

			int ejeY = yy - 1 + SpineConfiguration.BASE_CLUSTER_SIZE;
			// System.out.println(inc/SpineConfiguration.BASE_CLUSTER_SIZE);

			landMap.findPoint(SpineMapHelper.formKey(1, ejeY)).setType("9");
			
			for (int i = 0; i < (inc / SpineConfiguration.BASE_CLUSTER_SIZE); i++) {
				boolean firstPaint = true;
				for (int j = 0; j < SpineConfiguration.LOCAL_BRANCH_SIZE; j++) {
					int eje = 0;
					while (true) {
						if (!landMap.findPoint(SpineMapHelper.formKey(eje, ejeY - j)).getType()
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
				ejeY += 10 + SpineConfiguration.BASE_CLUSTER_SIZE;
			}

			ejeY = yy - 1 - SpineConfiguration.BASE_CLUSTER_SIZE;
			int cccc = 0;
			for (int i = 0; i < ((yy - 1) / SpineConfiguration.BASE_CLUSTER_SIZE); i++) {
				boolean firstPaint = true;
				for (int j = 0; j < SpineConfiguration.LOCAL_BRANCH_SIZE; j++) {
					int eje = 0;
					while (true) {
						if (!landMap.findPoint(SpineMapHelper.formKey(eje, ejeY - j)).getType()
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
				ejeY -= (SpineConfiguration.LOCAL_BRANCH_SIZE + SpineConfiguration.BASE_CLUSTER_SIZE);

			}
			zonify();
		}else{
			int divisionResultBlock = width
					/ (SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE);
			int ejeX = SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE * 2;// rev
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

				ejeX += (SpineConfiguration.HOUSE_SIDE_MAXIMUN_SIZE * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE);
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

			int ejeY = xx - 1 + SpineConfiguration.BASE_CLUSTER_SIZE;
			
			for (int i = 0; i < (inc / SpineConfiguration.BASE_CLUSTER_SIZE); i++) {
				boolean firstPaint = true;
				for (int j = 0; j < SpineConfiguration.LOCAL_BRANCH_SIZE; j++) {
					int eje = 0;
					while (true) {
						if (!landMap.findPoint(SpineMapHelper.formKey(ejeY - j,eje)).getType()
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
				ejeY += 10 + SpineConfiguration.BASE_CLUSTER_SIZE;
			}
			ejeY = xx - 1 - SpineConfiguration.BASE_CLUSTER_SIZE;
			int cccc = 0;
			for (int i = 0; i < ((xx-1) / SpineConfiguration.BASE_CLUSTER_SIZE); i++) {
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
				ejeY -= (SpineConfiguration.LOCAL_BRANCH_SIZE + SpineConfiguration.BASE_CLUSTER_SIZE);
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

	private void clearBorderByEmptyMark() {
		for (int j = landMap.getPointsy() - 1; j >= 0; j--) {
			for (int i = 0; i < landMap.getPointsx(); i++) {
				if (landMap.findPoint(SpineMapHelper.formKey(i, j)).getType().equals(SpineConfiguration.POLYGON_BORDER))
					landMap.getLandPoint(SpineMapHelper.formKey(i, j)).setType(SpineConfiguration.EMPTY_MARK);
				// if(i== xx &&
				// j==yy)spineLandMap.getLandPoint(SpineMapHelper.formKey(i,j)).setType(SpineConfiguration.NODE_MARK);
			}
		}

	}

	private void clearNodeByEmptyMark() {
		for (int j = landMap.getPointsy() - 1; j >= 0; j--) {
			for (int i = 0; i < landMap.getPointsx(); i++) {
				if (landMap.findPoint(SpineMapHelper.formKey(i, j)).getType().equals(SpineConfiguration.NODE_MARK))
					landMap.getLandPoint(SpineMapHelper.formKey(i, j)).setType(SpineConfiguration.EMPTY_MARK);
			}
		}

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
									if(!landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX)).getType().equals(" "))
										landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX)).setGramaticalType("l-"
											+(ejeY - inc_Block - inc)+"-"+ejeX+"-2-1-"+sideSize+"-"+sideDepth);
									if(!landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,(ejeX+ landMap.getConfiguration().getLotConfiguration().getDepthSize()))).getType().equals(" "))
										landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,(ejeX+ landMap.getConfiguration().getLotConfiguration().getDepthSize()))).setGramaticalType("l-"
											+(ejeY - inc_Block - inc)+"-"+(ejeX+ landMap.getConfiguration().getLotConfiguration().getDepthSize())+"-2-1-"+sideSize+"-"+sideDepth);
								}
								/*for (int l = 0; l < landMap.getConfiguration().getLotConfiguration().getDepthSize(); l++) {
									if ((ejeY - inc_Block - inc)>=0 && !landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX + l)).getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
										landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX + l))
												.setType(val1);
									if ((ejeY - inc_Block - inc)>=0&&!landMap
											.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,
													ejeX + l + landMap.getConfiguration().getLotConfiguration().getDepthSize()
													))
											.getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
										landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,
												ejeX + l + landMap.getConfiguration().getLotConfiguration().getDepthSize()
												)).setType(val2);
								}*/

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
								//blocks  next to parks
								if(k==0){
									if(!landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX)).getType().equals(" "))
										landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX)).setGramaticalType("l-"
											+(ejeY - inc_Block - inc)+"-"+ejeX+"-2-1-"+sideSize+"-"+sideDepth);
									
									if(!landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc, ejeX + landMap.getConfiguration().getLotConfiguration().getDepthSize())).getType().equals(" "))
										landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX+ landMap.getConfiguration().getLotConfiguration().getDepthSize())).setGramaticalType("l-"
											+(ejeY - inc_Block - inc)+"-"+(ejeX+ landMap.getConfiguration().getLotConfiguration().getDepthSize())+"-2-1-"+sideSize+"-"+sideDepth);
								}
								
								/*for (int l = 0; l < landMap.getConfiguration().getLotConfiguration().getDepthSize(); l++) {
									if (!landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc, ejeX+l))
											.getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
										landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc, ejeX+l))
												.setType(val1);
									if (!landMap
											.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,
													ejeX + l + landMap.getConfiguration().getLotConfiguration().getDepthSize()))
											.getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
										landMap.findPoint(SpineMapHelper.formKey(
												ejeY - inc_Block - inc,
												ejeX + l + landMap.getConfiguration().getLotConfiguration().getDepthSize())).setType(val2);
								}*/

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
								if (!landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX + l))
										.getType().equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
									landMap.findPoint(SpineMapHelper.formKey(ejeY - inc_Block - inc,ejeX + l))
											.setType(SpineConfiguration.ARTERIAL_MARK);
							}
							inc++;
						}
						
						for (int k = 0; k < nmbrParksLong; k++) {
							if(k==0){
								//nmbrParksLong
								if(!landMap.findPoint(SpineMapHelper.formKey((ejeY - inc_Block - inc),ejeX)).getType().equals(" "))
								landMap.findPoint(SpineMapHelper.formKey((ejeY - inc_Block - inc),ejeX)).setGramaticalType("l-"
										+( ejeY - inc_Block - inc-36)+"-"+(ejeX+15)+"-2-1-"+nmbrParksLong+"-"+(landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2));
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
			int a = (xx - 1) - ((xx - 1) / (landMap.getConfiguration().getBlockConfiguration().getSideSize() + SpineConfiguration.LOCAL_BRANCH_SIZE)
					* landMap.getConfiguration().getBlockConfiguration().getSideSize() + SpineConfiguration.LOCAL_BRANCH_SIZE);
			int topeDescount = a - SpineConfiguration.LOCAL_BRANCH_SIZE;
			String v1,v2;
			for(int index = 0; index < divisionResultBlock; index++) {
				v1= landMap.findPoint(SpineMapHelper.formKey(topeDescount+1,indexUp)).getType();
				if(v1.equals("p")||v1.equals("1")||v1.equals("2")){
					topeDescount-=SpineConfiguration.LOCAL_BRANCH_SIZE;
					break;
				}
				indexUp+=46;
			}
			indexUp=0;
			for(int index = 0; index < divisionResultBlock; index++) {
				int base=topeDescount;
				val1="1";
				val2="2";
				while(true){
					for (int i = 0; i < landMap.getConfiguration().getLotConfiguration().getSideSize(); i++) {
						if(i==0 ){
							if(!landMap.findPoint(SpineMapHelper.formKey(base ,indexUp)).getType().equals(" "))
								landMap.findPoint(SpineMapHelper.formKey(base , indexUp)).setGramaticalType("l-"
									+base+"-"+indexUp+"-2-1-"+sideSize+"-"+sideDepth);
							if(!landMap.findPoint(SpineMapHelper.formKey(base,(indexUp+landMap.getConfiguration().getLotConfiguration().getDepthSize()))).getType().equals(" "))
								landMap.findPoint(SpineMapHelper.formKey(base,(indexUp+landMap.getConfiguration().getLotConfiguration().getDepthSize()))).setGramaticalType("l-"
									+base+"-"+(indexUp+landMap.getConfiguration().getLotConfiguration().getDepthSize())+"-2-1-"+sideSize+"-"+sideDepth);
						}
						
						base--;
						if(base<0)break;
					}
					aux = val1;
					val1 = val2;
					val2 = aux;
					
					if(base<0)break;
				}
				indexUp+=46;
			}
		} else {
			/// parche
			// SI EL MAPA A ESTE LADO MIDE MENOS PARA UN BLOQUE Y CALLE LADO IZQ MAPA
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
						/*if(indAux==0){
							if(!landMap.findPoint(SpineMapHelper.formKey(topY, xxx)).getType().equals(" "))
								landMap.findPoint(SpineMapHelper.formKey(topY, xxx)).setGramaticalType("l-"
										+topY+"-"+xxx+"-2-1-"+sideSize+"-"+sideDepth);
							if(!landMap.findPoint(SpineMapHelper.formKey(topY, xxx+landMap.getConfiguration().getLotConfiguration().getDepthSize())).getType().equals(" "))
								landMap.findPoint(SpineMapHelper.formKey(topY, xxx+landMap.getConfiguration().getLotConfiguration().getDepthSize())).setGramaticalType("l-"
										+topY+"-"+(xxx+landMap.getConfiguration().getLotConfiguration().getDepthSize())+"-2-1-"+sideSize+"-"+sideDepth);
						}
						*/
						for (int jndAux = 0; jndAux < landMap.getConfiguration().getLotConfiguration().getDepthSize(); jndAux++) {
							/*if (!landMap.findPoint(SpineMapHelper.formKey(topY, xxx + jndAux)).getType().equals(" ")
									&& !landMap.findPoint(SpineMapHelper.formKey(xxx + jndAux, topY)).getType()
											.equals("."))
								landMap.findPoint(SpineMapHelper.formKey(topY, xxx + jndAux)).setType(val1);
							if (!landMap
									.findPoint(SpineMapHelper
											.formKey(xxx + jndAux + landMap.getConfiguration().getLotConfiguration().getDepthSize(), topY))
									.getType().equals(" ")
									&& !landMap
											.findPoint(SpineMapHelper.formKey(
													xxx + jndAux + landMap.getConfiguration().getLotConfiguration().getDepthSize(), topY))
											.getType().equals("."))
								landMap.findPoint(SpineMapHelper
										.formKey(xxx + jndAux + landMap.getConfiguration().getLotConfiguration().getDepthSize(), topY))
										.setType(val2);*/
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
			System.out.println("divisionResultBlock");
			System.out.println(divisionResultBlock);
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
									if(a<=large && !landMap.findPoint(SpineMapHelper.formKey(a ,ejeXX)).getType().equals(" "))
										landMap.findPoint(SpineMapHelper.formKey(a,ejeXX)).setGramaticalType("l-"
												+a+"-"+ejeXX+"-2-1-"+sideSize+"-"+sideDepth);
									if(a<=large &&!landMap.findPoint(SpineMapHelper.formKey(a,(ejeXX+ landMap.getConfiguration().getLotConfiguration().getDepthSize()) )).getType().equals(" "))
										landMap.findPoint(SpineMapHelper.formKey(a,(ejeXX+landMap.getConfiguration().getLotConfiguration().getDepthSize()))).setGramaticalType("l-"
												+a+"-"+(ejeXX+ landMap.getConfiguration().getLotConfiguration().getDepthSize())+"-2-1-"+sideSize+"-"+sideDepth);
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
						landMap.findPoint(SpineMapHelper.formKey(xx,yy)).setType("9");
						landMap.findPoint(SpineMapHelper.formKey(xx+1,yy)).setType("9");
						landMap.findPoint(SpineMapHelper.formKey(xx+2,yy)).setType("9");
						// no esta imprimiendo
						int a = ejeYY  - SpineConfiguration.LOCAL_BRANCH_SIZE;
						for (int ii = 0; ii < nmbrParksLong; ii++) {
							if(ii==0){
								/*if((ejeYY-ii)<=width)
								landMap.findPoint(SpineMapHelper.formKey(ejeYY - ii,ejeXX)).setGramaticalType("p-"
										+(ejeYY - ii)+"-"+ejeXX+"-2-1-"+sideSize+"-"+nmbrParksLong);*/
								if (a<= large && a>=0 &&!landMap.findPoint(SpineMapHelper.formKey(a,ejeXX)).getType()
										.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
								landMap.findPoint(SpineMapHelper.formKey(a,ejeXX)).setGramaticalType("l-"
										+(a-29)+"-"+(ejeXX+landMap.getConfiguration().getLotConfiguration().getDepthSize())+"-2-1-"+nmbrParksLong+"-"+(landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2));
							}
							for (int jj = 0; jj < landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2; jj++) {
								if ((ejeYY-ii)<=large && !landMap.findPoint(SpineMapHelper.formKey(ejeYY - ii,ejeXX + jj)).getType()
										.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)){
									landMap.findPoint(SpineMapHelper.formKey(ejeYY - ii,ejeXX + jj))
									.setType(cambiable);
									
									
									/*landMap.findPoint(SpineMapHelper.formKey(ejeYY - ii,ejeXX + jj)).setGramaticalType("p-"
											+(ejeYY - ii)+"-"+(ejeXX + jj)+"-2-1-"+sideSize+"-"+nmbrParksLong);*/
									
								}
									
								
								
							}
						}
						//print streets
						for (int ii = 0; ii < SpineConfiguration.LOCAL_BRANCH_SIZE; ii++) {
							for (int jj = 0; jj < landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2; jj++) {
								if ((ejeYY - ii - nmbrParksLong)<width&&!landMap.findPoint(SpineMapHelper.formKey(ejeYY - ii - nmbrParksLong,ejeXX + jj))
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
									if (a<= large && a>=0 &&!landMap.findPoint(SpineMapHelper.formKey(a,ejeXX)).getType()
											.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
									landMap.findPoint(SpineMapHelper.formKey(a,ejeXX)).setGramaticalType("l-"
											+a+"-"+ejeXX+"-2-1-"+sideSize+"-"+sideDepth);
									if (a<= large && a>=0 &&!landMap.findPoint(SpineMapHelper.formKey(a,ejeXX+ landMap.getConfiguration().getLotConfiguration().getDepthSize())).getType()
											.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK))
									landMap.findPoint(SpineMapHelper.formKey(a,ejeXX+ landMap.getConfiguration().getLotConfiguration().getDepthSize())).setGramaticalType("l-"
											+a+"-"+(ejeXX+ landMap.getConfiguration().getLotConfiguration().getDepthSize())+"-2-1-"+sideSize+"-"+sideDepth);
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
				// System.out.println();
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
	}
	
	
	public void makeBorder(List<Integer> listData){
		String borderGrammar="g-";
		for(int i=0;i<listData.size();i+=2){
			borderGrammar+=listData.get(i)+"-"+listData.get(i+1)+"-";

		}
		landMap.findPoint(SpineMapHelper.formKey(listData.get(0),listData.get(1))).setGramaticalType(borderGrammar);
		
	}
	public void zonify() {
		System.out.println("landMap.getConfiguration().getBlockConfiguration().getSideSize()");
		System.out.println(landMap.getConfiguration().getBlockConfiguration().getSideSize());
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
									if(!landMap.findPoint(SpineMapHelper.formKey(ejeX , ejeY - inc_Block - inc)).getType().equals(" ")&&
											!landMap.findPoint(SpineMapHelper.formKey(ejeX +15, ejeY - inc_Block - inc)).getType().equals(" "))
										landMap.findPoint(SpineMapHelper.formKey(ejeX , ejeY - inc_Block - inc)).setGramaticalType("l-"
											+ejeX+"-"+(ejeY - inc_Block - inc)+"-1-2-"+sideSize+"-"+sideDepth);
									if(!landMap.findPoint(SpineMapHelper.formKey((ejeX+ landMap.getConfiguration().getLotConfiguration().getDepthSize()) , ejeY - inc_Block - inc)).getType().equals(" ")&&
											!landMap.findPoint(SpineMapHelper.formKey((ejeX+ landMap.getConfiguration().getLotConfiguration().getDepthSize()+15) , ejeY - inc_Block - inc)).getType().equals(" "))
										landMap.findPoint(SpineMapHelper.formKey((ejeX+ landMap.getConfiguration().getLotConfiguration().getDepthSize()) , ejeY - inc_Block - inc)).setGramaticalType("l-"
											+(ejeX+ landMap.getConfiguration().getLotConfiguration().getDepthSize())+"-"+(ejeY - inc_Block - inc)+"-1-2-"+sideSize+"-"+sideDepth);
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
								if(k==0){
									if(!landMap.findPoint(SpineMapHelper.formKey(ejeX , ejeY - inc_Block - inc)).getType().equals(" ")&&
											!landMap.findPoint(SpineMapHelper.formKey(ejeX+15 , ejeY - inc_Block - inc)).getType().equals(" "))
										landMap.findPoint(SpineMapHelper.formKey(ejeX , ejeY - inc_Block - inc)).setGramaticalType("l-"
											+ejeX+"-"+(ejeY - inc_Block - inc)+"-1-2-"+sideSize+"-"+sideDepth);
									if(!landMap.findPoint(SpineMapHelper.formKey(ejeX + landMap.getConfiguration().getLotConfiguration().getDepthSize(), ejeY - inc_Block - inc)).getType().equals(" ")&&
											!landMap.findPoint(SpineMapHelper.formKey(ejeX + landMap.getConfiguration().getLotConfiguration().getDepthSize()+15, ejeY - inc_Block - inc)).getType().equals(" "))
										landMap.findPoint(SpineMapHelper.formKey((ejeX+ landMap.getConfiguration().getLotConfiguration().getDepthSize()) , ejeY - inc_Block - inc)).setGramaticalType("l-"
											+(ejeX+ landMap.getConfiguration().getLotConfiguration().getDepthSize())+"-"+(ejeY - inc_Block - inc)+"-1-2-"+sideSize+"-"+sideDepth);
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
							if(k==0){
								if(!landMap.findPoint(SpineMapHelper.formKey((ejeX), (ejeY - inc_Block - inc))).getType().equals(" "))
								landMap.findPoint(SpineMapHelper.formKey((ejeX), (ejeY - inc_Block - inc))).setGramaticalType("l-"
										+(ejeX)+"-"+( ejeY - inc_Block - inc)+"-1-2-"+nmbrParksLong+"-"+(landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2));
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
				indexUp+=46;
			}
			indexUp=0;
			for(int index = 0; index < divisionResultBlock; index++) {
				int base=topeDescount;
				val1="1";
				val2="2";
				while(true){
					for (int i = 0; i < landMap.getConfiguration().getLotConfiguration().getSideSize(); i++) {
						if(i==0 ){
							if(!landMap.findPoint(SpineMapHelper.formKey(indexUp ,base)).getType().equals(" ")&&
									!landMap.findPoint(SpineMapHelper.formKey(indexUp+15,base)).getType().equals(" "))
								landMap.findPoint(SpineMapHelper.formKey(indexUp , base)).setGramaticalType("l-"
									+indexUp+"-"+base+"-1-2-"+sideSize+"-"+sideDepth);
							if(!landMap.findPoint(SpineMapHelper.formKey((indexUp+landMap.getConfiguration().getLotConfiguration().getDepthSize()) ,base)).getType().equals(" ")&&
									!landMap.findPoint(SpineMapHelper.formKey((indexUp+landMap.getConfiguration().getLotConfiguration().getDepthSize()+15),base)).getType().equals(" "))
								landMap.findPoint(SpineMapHelper.formKey((indexUp+landMap.getConfiguration().getLotConfiguration().getDepthSize()) , base)).setGramaticalType("l-"
									+(indexUp+landMap.getConfiguration().getLotConfiguration().getDepthSize())+"-"+base+"-1-2-"+sideSize+"-"+sideDepth);
						}
						base--;
						if(base<0)break;
					}
					aux = val1;
					val1 = val2;
					val2 = aux;
					
					if(base<0)break;
				}
				indexUp+=46;
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
							if(!landMap.findPoint(SpineMapHelper.formKey(xxx, topY)).getType().equals(" "))
								landMap.findPoint(SpineMapHelper.formKey(xxx, topY)).setGramaticalType("l-"
										+xxx+"-"+topY+"-1-2-"+sideSize+"-"+sideDepth);
							if(!landMap.findPoint(SpineMapHelper.formKey(xxx+landMap.getConfiguration().getLotConfiguration().getDepthSize(), topY)).getType().equals(" "))
								landMap.findPoint(SpineMapHelper.formKey(xxx+landMap.getConfiguration().getLotConfiguration().getDepthSize(), topY)).setGramaticalType("l-"
										+(xxx+landMap.getConfiguration().getLotConfiguration().getDepthSize())+"-"+topY+"-1-2-"+sideSize+"-"+sideDepth);
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
									if(!landMap.findPoint(SpineMapHelper.formKey(ejeXX ,a)).getType().equals(" "))
										landMap.findPoint(SpineMapHelper.formKey(ejeXX , a)).setGramaticalType("l-"
												+ejeXX+"-"+a+"-1-2-"+sideSize+"-"+sideDepth);
									if(!landMap.findPoint(SpineMapHelper.formKey((ejeXX+ landMap.getConfiguration().getLotConfiguration().getDepthSize()) ,a)).getType().equals(" "))
										landMap.findPoint(SpineMapHelper.formKey((ejeXX+landMap.getConfiguration().getLotConfiguration().getDepthSize()) , a)).setGramaticalType("l-"
												+(ejeXX+ landMap.getConfiguration().getLotConfiguration().getDepthSize())+"-"+a+"-1-2-"+sideSize+"-"+sideDepth);
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
								if(!landMap.findPoint(SpineMapHelper.formKey(ejeXX, ejeYY - ii)).getType().equals(" "))
									landMap.findPoint(SpineMapHelper.formKey(ejeXX, ejeYY - ii)).setGramaticalType("l-"
										+ejeXX+"-"+( ejeYY - ii)+"-1-2-"+nmbrParksLong+"-"+(landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2));
							}
							
							
							for (int jj = 0; jj < landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2; jj++) {
								
								if (!landMap.findPoint(SpineMapHelper.formKey(ejeXX + jj, ejeYY - ii)).getType()
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
									landMap.findPoint(SpineMapHelper.formKey(ejeXX , a)).setGramaticalType("l-"
											+ejeXX+"-"+a+"-1-2-"+sideSize+"-"+sideDepth);
									landMap.findPoint(SpineMapHelper.formKey((ejeXX +landMap.getConfiguration().getLotConfiguration().getDepthSize()), a)).setGramaticalType("l-"
											+(ejeXX+landMap.getConfiguration().getLotConfiguration().getDepthSize())+"-"+a+"-1-2-"+sideSize+"-"+sideDepth);
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
								landMap.findPoint(SpineMapHelper.formKey(indexXLoop , yBegin + counter)).setGramaticalType("l-"
										+indexXLoop+"-"+(yBegin + counter)+"-1-2-"+sideSize+"-"+sideDepth);
								landMap.findPoint(SpineMapHelper.formKey((indexXLoop+landMap.getConfiguration().getLotConfiguration().getDepthSize()) , yBegin + counter)).setGramaticalType("l-"
										+(indexXLoop+landMap.getConfiguration().getLotConfiguration().getDepthSize())+"-"+(yBegin + counter)+"-1-2-"+sideSize+"-"+sideDepth);
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
					for (int ii = 0; ii < landMap.getConfiguration().getLotConfiguration().getSideSize(); ii++) {
						
						String cmp = landMap.findPoint(SpineMapHelper.formKey(ejeXX, topY)).getType();
						if (cmp.equals(SpineConfiguration.OUTSIDE_POLYGON_MARK)
								|| cmp.equals(SpineConfiguration.POLYGON_BORDER) || topY > width) {
							stateOut = true;
							break;
						}
						if(ii==0){
							if(!landMap.findPoint(SpineMapHelper.formKey(ejeXX, topY)).getType().equals(" "))
								landMap.findPoint(SpineMapHelper.formKey(ejeXX, topY)).setGramaticalType("l-"
										+ejeXX+"-"+topY+"-1-2-"+sideSize+"-"+sideDepth);
							if(!landMap.findPoint(SpineMapHelper.formKey(ejeXX + landMap.getConfiguration().getLotConfiguration().getDepthSize(), topY)).getType().equals(" "))
								landMap.findPoint(SpineMapHelper.formKey(ejeXX + landMap.getConfiguration().getLotConfiguration().getDepthSize(), topY)).setGramaticalType("l-"
										+(ejeXX+ landMap.getConfiguration().getLotConfiguration().getDepthSize())+"-"+topY+"-1-2-"+sideSize+"-"+sideDepth);
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
					// topY++;
					// break;
				}

				ejeXX += landMap.getConfiguration().getLotConfiguration().getDepthSize() * 2 + SpineConfiguration.COLLECTOR_BRANCH_SIZE;
			}
		}

		paintAportes();

	}
}
