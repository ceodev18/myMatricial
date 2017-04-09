package helpers.radialVariation;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


import algorithm.radialVariation.radialAlgorithm;
import helpers.radialVariation.RadialDirectionHelper;
import helpers.radialVariation.RadialTestPane;
import helpers.radialVariation.RadialTester;
import interfaces.radialVariation.RadialConfiguration;
import models.radialVariation.RadialLandMap;
import models.radialVariation.RadialLandPoint;

public class RadialTester {
	public static void main(String[] args) {
		long startTime = System.nanoTime();

		Runtime runtime = Runtime.getRuntime();
		long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocation" + usedMemoryBefore / 1000000 + " in MB");

		int large = 925, width = 1130;
		//int large = 950, width = 750;
		// 1. We create the map and set its intrinsec variables
		
		RadialLandMap landMap = new RadialLandMap(large, width);
		List<RadialLandPoint> polygon = new ArrayList<>();
		
		/*
		RadialLandPoint landPoint = new RadialLandPoint(450,48);
		polygon.add(landPoint);
		landPoint = new RadialLandPoint(908, 286);
		polygon.add(landPoint);
		landPoint = new RadialLandPoint(673, 678);
		polygon.add(landPoint);
		landPoint = new RadialLandPoint(126, 532);
		polygon.add(landPoint);
		landPoint = new RadialLandPoint(65, 326);
		polygon.add(landPoint);
		// we must reuse the first one as the last
		landPoint = new RadialLandPoint(450, 48);
		polygon.add(landPoint);
		*/
		
		RadialLandPoint landPoint = new RadialLandPoint(726, 20);
		polygon.add(landPoint);
		landPoint = new RadialLandPoint(924, 762);
		polygon.add(landPoint);
		landPoint = new RadialLandPoint(273, 1129);
		polygon.add(landPoint);
		landPoint = new RadialLandPoint(0, 528);
		polygon.add(landPoint);
		// we must reuse the first one as the last
		landPoint = new RadialLandPoint(726,20);
		polygon.add(landPoint);

		
		
		// 2. we create the border from the polygon
		landMap.createBorderFromPolygon(polygon);
		
		List<RadialLandPoint> entryPoints = new ArrayList<>();
		landPoint = new RadialLandPoint(468, 88);
		entryPoints.add(landPoint);
		
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocated" + usedMemoryBefore / 1000000 + " in MB");
		
		
		radialAlgorithm radialAlgorithm = new radialAlgorithm();
		radialAlgorithm.setLandMap(landMap);
		
		radialAlgorithm.CreateRadialWeb();
		/*
		 * 
		 * 
		for (RadialLandPoint entryPoint : entryPoints) {
			int direction = RadialDirectionHelper.orthogonalDirectionFromPointToPoint(entryPoint,
					landMap.getCentroid());
			radialAlgorithm.createRouteVariation(entryPoint.getId(), direction, RadialConfiguration.ARTERIAL_BRANCH);
			break;
		}
		*/
		
		
		
		landMap.printMapToFile();

		
		///////////////////////////////////
	
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map after zonification" + usedMemoryBefore / 1000000 + " in MB");

		long endTime = System.nanoTime();
		long duration = (endTime - startTime) / (1000000 * 1000); // divide by
																	// to get
																	// milliseconds.
		System.out.println("Algorithm finished in " + duration + "s");
		// ClusterLotizationAlgorithm.landMap.printMapToFile();

		radialAlgorithm.getLandMap().printMapToFile();
		
		// System.out.println("Compressed String lenght: " +
		// compressedString.length());

		endTime = System.nanoTime();
		duration = (endTime - startTime) / (1000000 * 1000); // divide by to get
																// milliseconds.
		System.out.println("Response build finished in " + duration + "s");

		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Final Memory" + usedMemoryBefore / 1000000 + " in MB");


		//2 variants
		RadialTestPane radialTestPane = new RadialTestPane(true,
				radialAlgorithm.getLandMap().stringify(), large, width);		
		//clusterTestPane.clusterLandMap = clusterAlgorithm.getLandMap();
		//clusterTestPane.large = large;
		//clusterTestPane.width = width;
		new RadialTester(radialTestPane);


	
	}
	public RadialTester(RadialTestPane radialTestPane) {
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
				JScrollPane scrPane = new JScrollPane(radialTestPane);
				frame.add(scrPane); // similar to getContentPane().add(scrPane);

				// frame.add(new TestPane());
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

}
