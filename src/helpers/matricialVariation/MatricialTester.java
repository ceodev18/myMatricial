package helpers.matricialVariation;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import algorithm.matricialVariation.MatricialAlgorithm;
import algorithm.spineVariation.SpineAlgorithm;
import helpers.spineVariation.SpineDirectionHelper;
import helpers.spineVariation.SpineTestPane;
import helpers.spineVariation.SpineTester;
import interfaces.spineVariation.SpineConfiguration;
import models.base.LandMap;
import models.base.LandPoint;
import models.matricialVariation.MatricialLandMap;
import models.matricialVariation.MatricialLandPoint;
import models.spineVariation.SpineLandMap;
import models.spineVariation.SpineLandPoint;

public class MatricialTester {
	public static void main(String[] args) {
		long startTime = System.nanoTime();
		//MAPA 1
		Runtime runtime = Runtime.getRuntime();
		long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocation" + usedMemoryBefore / 1000000 + " in MB");
		int large=969 +1,width=899	 +1;
		MatricialLandMap matricialLandMap = new MatricialLandMap(large, width);
		

		List<MatricialLandPoint> polygon = new ArrayList<>();
		MatricialLandPoint landPoint = new MatricialLandPoint(150, 67);
		polygon.add(landPoint);
		landPoint = new MatricialLandPoint(633, 0);
		polygon.add(landPoint);
		landPoint = new MatricialLandPoint(969, 338);
		polygon.add(landPoint);
		landPoint = new MatricialLandPoint(829,802);
		polygon.add(landPoint);
		landPoint = new MatricialLandPoint(376,899);
		polygon.add(landPoint);
		landPoint = new MatricialLandPoint(0,669);
		polygon.add(landPoint);
		matricialLandMap.createBorderFromPolygon(polygon);
		//spineLandMap.compress();
		//entry point 353 31
		List<MatricialLandPoint> entryPoints = new ArrayList<>();	
		//200 representa la distancia del borde del poligono al inicio
		//landPoint = new SpineLandPoint(200,571);
		//landPoint = new MatricialLandPoint(6,557);
		//entryPoints.add(landPoint);
		
		landPoint = new MatricialLandPoint(100,400);
		entryPoints.add(landPoint);
		entryPoints.add(new MatricialLandPoint(557,6));
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocated" + usedMemoryBefore / 1000000 + " in MB");
		
		// replace this LSYSTEM  by For loop
		//spineLandMap.clearDottedLimits();
		
		MatricialAlgorithm matricialAlgorithm = new MatricialAlgorithm();
		matricialAlgorithm.setLandMap(matricialLandMap);
		/*matricialAlgorithm.setWidth(width);
		matricialAlgorithm.setEntryX(6);
		matricialAlgorithm.setEntryY(557);
		matricialAlgorithm.setLarge(large);*/
		
		for (MatricialLandPoint entryPoint : entryPoints) {
			int direction =MatricialDirectionHelper.orthogonalDirectionFromPointToPoint(entryPoint,
					matricialLandMap.getCentroid());
			matricialAlgorithm.createRouteVariation(entryPoint.getId(), direction,SpineConfiguration.ARTERIAL_BRANCH);
			
		}
		
		// 4. We clusterize the points
		//matricialAlgorithm.spineizeV2();
		System.out.println("spineAlgorithm.getLandMap().getLandRoutes(); in TESTER");
		System.out.println("size is "+ matricialAlgorithm.getLandMap().getLandRoutes().size());
		String strVertex = "";
		for (int i = 0; i < matricialAlgorithm.getLandMap().getLandRoutes().size(); i++) {
			System.out.println(matricialAlgorithm.getLandMap().getLandRoutes().get(i).stringify());
			strVertex += matricialAlgorithm.getLandMap().getLandRoutes().get(i).stringify();
			if (i + 1 != matricialAlgorithm.getLandMap().getLandRoutes().size()) {
				strVertex += ",";
			}
		}
		
		//spineAlgorithm.getLandMap().getLandRoutes();
		
		//spineAlgorithm.spineize();
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map after completed routes" + usedMemoryBefore / 1000000 + " in MB");
			
		
		// 5. Zonification
		//matricialAlgorithm.zonify();
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map after zonification" + usedMemoryBefore / 1000000 + " in MB");

		
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/(1000000*1000);  //divide by  to get milliseconds.
		System.out.println("Algorithm finished in " + duration + "s");
		//ClusterLotizationAlgorithm.landMap.printMapToFile();
		//String compressedString = ClusterLotizationAlgorithm.landMap.stringify();
		//System.out.println("Compressed String lenght: " + compressedString.length());
		
		
		
		matricialAlgorithm.getLandMap().printMapToFileNew();
		
		endTime = System.nanoTime();
		duration = (endTime - startTime)/(1000000*1000);  //divide by  to get milliseconds.
		System.out.println("Response build finished in " + duration + "s");
		
		//2 variants
		SpineTestPane spineTestPane = new SpineTestPane(true,
				matricialAlgorithm.getLandMap().stringify(), large, width);		
				//clusterTestPane.clusterLandMap = clusterAlgorithm.getLandMap();
				//clusterTestPane.large = large;
				//clusterTestPane.width = width;
		System.out.println("Start canvas");
		
		new SpineTester(spineTestPane);

	}
	public MatricialTester(MatricialTestPane matricialTestPane) {
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
				JScrollPane scrPane = new JScrollPane(matricialTestPane);
				frame.add(scrPane); // similar to getContentPane().add(scrPane);

				// frame.add(new TestPane());
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}
}