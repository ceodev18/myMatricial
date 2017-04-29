package helpers.spineVariation;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import algorithm.spineVariation.LSystemSpineAlgorithm;
import algorithm.spineVariation.SpineAlgorithm;
import helpers.spineVariation.SpineDirectionHelper;
import interfaces.spineVariation.SpineConfiguration;
import models.spineVariation.SpineLandPoint;
import models.spineVariation.SpineLandMap;

public class SpineTester {
	public static void main(String[] argv){
		long startTime = System.nanoTime();
		//MAPA 1
		Runtime runtime = Runtime.getRuntime();
		long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocation" + usedMemoryBefore / 1000000 + " in MB");
		int large=1145 +1,width=804 +1;
		SpineLandMap spineLandMap = new SpineLandMap(large, width);
		

		List<SpineLandPoint> polygon = new ArrayList<>();
		SpineLandPoint landPoint = new SpineLandPoint(0, 13);
		polygon.add(landPoint);
		landPoint = new SpineLandPoint(996, 0);
		polygon.add(landPoint);
		landPoint = new SpineLandPoint(1145, 804);
		polygon.add(landPoint);
		landPoint = new SpineLandPoint(202,770);
		polygon.add(landPoint);
		landPoint = new SpineLandPoint(0,13);
		polygon.add(landPoint);
		spineLandMap.createBorderFromPolygon(polygon);
		//spineLandMap.compress();
		//entry point 353 31
		List<SpineLandPoint> entryPoints = new ArrayList<>();	
		//200 representa la distancia del borde del poligono al inicio
		//landPoint = new SpineLandPoint(200,571);
		landPoint = new SpineLandPoint(6,557);
		entryPoints.add(landPoint);
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocated" + usedMemoryBefore / 1000000 + " in MB");
		
		// replace this LSYSTEM  by For loop
		//spineLandMap.clearDottedLimits();
		
		SpineAlgorithm spineAlgorithm = new SpineAlgorithm();
		spineAlgorithm.setLandMap(spineLandMap);
		spineAlgorithm.setWidth(width);
		spineAlgorithm.setEntryX(6);
		spineAlgorithm.setEntryY(557);
		spineAlgorithm.setLarge(large);
		
		for (SpineLandPoint entryPoint : entryPoints) {
			int direction =SpineDirectionHelper.orthogonalDirectionFromPointToPoint(entryPoint,
					spineLandMap.getCentroid());
			spineAlgorithm.createRouteVariation(entryPoint.getId(), direction,SpineConfiguration.ARTERIAL_BRANCH);
			break;
		}
		
		// 4. We clusterize the points
		spineAlgorithm.spineizeV2();
		System.out.println("spineAlgorithm.getLandMap().getLandRoutes(); in TESTER");
		System.out.println("size is "+ spineAlgorithm.getLandMap().getLandRoutes().size());
		String strVertex = "";
		for (int i = 0; i < spineAlgorithm.getLandMap().getLandRoutes().size(); i++) {
			System.out.println(spineAlgorithm.getLandMap().getLandRoutes().get(i).stringify());
			strVertex += spineAlgorithm.getLandMap().getLandRoutes().get(i).stringify();
			if (i + 1 != spineAlgorithm.getLandMap().getLandRoutes().size()) {
				strVertex += ",";
			}
		}
		
		//spineAlgorithm.getLandMap().getLandRoutes();
		
		//spineAlgorithm.spineize();
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map after completed routes" + usedMemoryBefore / 1000000 + " in MB");
			
		
		// 5. Zonification
		spineAlgorithm.zonify();
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map after zonification" + usedMemoryBefore / 1000000 + " in MB");

		//spineAlgorithm.clearDotsSpine();

		//LSystemSpineAlgorithm.landMap.printMapToFile();
		
		//changeLbyE();
		
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/(1000000*1000);  //divide by  to get milliseconds.
		System.out.println("Algorithm finished in " + duration + "s");
		//ClusterLotizationAlgorithm.landMap.printMapToFile();
		//String compressedString = ClusterLotizationAlgorithm.landMap.stringify();
		//System.out.println("Compressed String lenght: " + compressedString.length());
		
		
		
		spineAlgorithm.getLandMap().printMapToFileNew();
		
		endTime = System.nanoTime();
		duration = (endTime - startTime)/(1000000*1000);  //divide by  to get milliseconds.
		System.out.println("Response build finished in " + duration + "s");
		
		//2 variants
		SpineTestPane spineTestPane = new SpineTestPane(true,
						spineAlgorithm.getLandMap().stringify(), large, width);		
				//clusterTestPane.clusterLandMap = clusterAlgorithm.getLandMap();
				//clusterTestPane.large = large;
				//clusterTestPane.width = width;
		System.out.println("Start canvas");
		
		
		
		
		
	
		/*int large1=1023 +1,width1=800 +1;
		SpineLandMap spineLandMap1 = new SpineLandMap(large1, width1);
		

		List<SpineLandPoint> polygon1 = new ArrayList<>();
		SpineLandPoint landPoint1 = new SpineLandPoint(0, 800);
		polygon1.add(landPoint1);
		landPoint1 = new SpineLandPoint(1023, 800);
		polygon1.add(landPoint1);
		landPoint1 = new SpineLandPoint(1023, 0);
		polygon1.add(landPoint1);
		landPoint1 = new SpineLandPoint(1, 0);
		polygon1.add(landPoint1);
		landPoint1 = new SpineLandPoint(0,800);
		polygon1.add(landPoint1);
		spineLandMap1.createBorderFromPolygon(polygon1);
		
		List<SpineLandPoint> entryPoints1 = new ArrayList<>();	
		landPoint1 = new SpineLandPoint(0,400);
		entryPoints1.add(landPoint1);

		SpineAlgorithm spineAlgorithm1 = new SpineAlgorithm();
		spineAlgorithm1.setLandMap(spineLandMap1);
		spineAlgorithm1.setWidth(width1);
		spineAlgorithm1.setEntryX(1); //entry point never can be 0
		spineAlgorithm1.setEntryY(400);
		spineAlgorithm1.setLarge(large1);
		
		for (SpineLandPoint entryPoint1 : entryPoints1) {
			int direction1 =SpineDirectionHelper.orthogonalDirectionFromPointToPoint(entryPoint1,
					spineLandMap1.getCentroid());
			spineAlgorithm1.createRouteVariation(entryPoint1.getId(), direction1,SpineConfiguration.ARTERIAL_BRANCH);
			break;
		}
		
		spineAlgorithm1.spineizeV2();
		spineAlgorithm1.zonify();
		spineAlgorithm1.getLandMap().printMapToFileNew();
		SpineTestPane spineTestPane1 = new SpineTestPane(true,
								spineAlgorithm1.getLandMap().stringify(), large1, width1);	
		

		System.out.println("Algorithm finished");*/
		
		new SpineTester(spineTestPane);
		//new SpineTester(spineTestPane1);
		
		
	}
	public SpineTester(SpineTestPane spineTestPane) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException ex) {
				}

				JFrame frame = new JFrame("Testing");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setLayout(new BorderLayout());
				JScrollPane scrPane = new JScrollPane(spineTestPane);
				frame.add(scrPane); // similar to getContentPane().add(scrPane);

				// frame.add(new TestPane());
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}
}
