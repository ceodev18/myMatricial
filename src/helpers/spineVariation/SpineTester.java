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
		int large=1000 +1,width=700 +1;
		SpineLandMap spineLandMap = new SpineLandMap(large, width);
		

		List<SpineLandPoint> polygon = new ArrayList<>();
		SpineLandPoint landPoint = new SpineLandPoint(0, 700);
		polygon.add(landPoint);
		landPoint = new SpineLandPoint(400, 700);
		polygon.add(landPoint);
		landPoint = new SpineLandPoint(1000, 0);
		polygon.add(landPoint);
		landPoint = new SpineLandPoint(300, 0);
		polygon.add(landPoint);
		landPoint = new SpineLandPoint(0,700);
		polygon.add(landPoint);
		spineLandMap.createBorderFromPolygon(polygon);
		//spineLandMap.compress();
		//entry point 353 31
		List<SpineLandPoint> entryPoints = new ArrayList<>();	
		//200 representa la distancia del borde del poligono al inicio
		//landPoint = new SpineLandPoint(200,571);
		landPoint = new SpineLandPoint(150,350);
		entryPoints.add(landPoint);
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocated" + usedMemoryBefore / 1000000 + " in MB");
		
		// replace this LSYSTEM  by For loop
		//spineLandMap.clearDottedLimits();
		
		SpineAlgorithm spineAlgorithm = new SpineAlgorithm();
		spineAlgorithm.setLandMap(spineLandMap);
		spineAlgorithm.setWidth(width);
		spineAlgorithm.setEntryX(150);
		spineAlgorithm.setEntryY(350);
		spineAlgorithm.setLarge(large);
		
		for (SpineLandPoint entryPoint : entryPoints) {
			int direction =SpineDirectionHelper.orthogonalDirectionFromPointToPoint(entryPoint,
					spineLandMap.getCentroid());
			spineAlgorithm.createRouteVariation(entryPoint.getId(), direction,SpineConfiguration.ARTERIAL_BRANCH);
			break;
		}
		
		// 4. We clusterize the points
		//spineAlgorithm.spineizeV2();
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
		new SpineTester(spineTestPane);
		
		
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
