package helpers.spineVariation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import algorithm.spineVariation.SpineLotizationAlgorithm;
import algorithm.clusterVariation.ClusterAlgorithm;
import algorithm.spineVariation.LSystemSpineAlgorithm;
import algorithm.spineVariation.SpineAlgorithm;
import algorithm.spineVariation.LSystemSpineAlgorithm;
import helpers.base.MapHelper;
import helpers.clusterVariation.ClusterDirectionHelper;
import helpers.clusterVariation.ClusterTestPane;
import helpers.clusterVariation.ClusterTester;
import helpers.spineVariation.SpineDirectionHelper;
import interfaces.clusterVariation.ClusterConfiguration;
import interfaces.spineVariation.SpineConfiguration;
import models.spineVariation.SpineLandPoint;
import models.clusterVariation.ClusterLandPoint;
import models.spineVariation.SpineLandMap;
import models.spineVariation.SpineLandPoint;
import models.spineVariation.SpineLandMap;

public class SpineTester {
	public static void main(String[] argv){
		long startTime = System.nanoTime();
		//MAPA 1
		Runtime runtime = Runtime.getRuntime();
		long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		
		int large=1600+1,width=	700+1;
		SpineLandMap spineLandMap = new SpineLandMap(large, width);
		

		List<SpineLandPoint> polygon = new ArrayList<>();
		SpineLandPoint landPoint = new SpineLandPoint(1, 50);
		polygon.add(landPoint);
		landPoint = new SpineLandPoint(400, 600);
		polygon.add(landPoint);
		landPoint = new SpineLandPoint(1600, 600);
		polygon.add(landPoint);
		landPoint = new SpineLandPoint( 1600, 50);
		polygon.add(landPoint);
		landPoint = new SpineLandPoint(1,50);
		polygon.add(landPoint);
		spineLandMap.createBorderFromPolygon(polygon);
		//entry point 353 31
		List<SpineLandPoint> entryPoints = new ArrayList<>();	
		//200 representa la distancia del borde del poligono al inicio
		//landPoint = new SpineLandPoint(200,571);
		landPoint = new SpineLandPoint(150,256);
		entryPoints.add(landPoint);
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		
		// replace this LSYSTEM  by For loop
		LSystemSpineAlgorithm.landMap=spineLandMap;
		SpineAlgorithm spineAlgorithm = new SpineAlgorithm();
		spineAlgorithm.setLandMap(spineLandMap);
		spineAlgorithm.setEntryX(150);
		spineAlgorithm.setEntryY(256);
		spineAlgorithm.setLarge(large);
		for (SpineLandPoint entryPoint : entryPoints) {
			int direction =SpineDirectionHelper.orthogonalDirectionFromPointToPoint(entryPoint,
					spineLandMap.getCentroid());
			spineAlgorithm.createRouteVariation(entryPoint.getId(), direction,SpineConfiguration.ARTERIAL_BRANCH);
			break;
		}
		
		//spineAlgorithm.clusterize();
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		
		//spineAlgorithm.zonify();
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		//LSystemSpineAlgorithm.landMap.printMapToFileNew();
		// 4. We clusterize the points
		//LSystemSpineAlgorithm.clusterize();
		//LSystemSpineAlgorithm.landMap.printMapToFileNew();
		
		// 5. Zonification
		//SpineLotizationAlgorithm.spineLandMap = LSystemSpineAlgorithm.landMap;
		//SpineLotizationAlgorithm.zonify(150,256-1,large);
		//SpineLotizationAlgorithm.zonify(31+1,353-1); //punto de inicio
		

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
