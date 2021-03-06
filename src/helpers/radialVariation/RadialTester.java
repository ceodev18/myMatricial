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
import helpers.radialVariation.RadialTestPane;
import helpers.radialVariation.RadialPolygonTester;
import helpers.radialVariation.RadialTester;
import models.radialVariation.RadialLandPoint;
import models.configuration.ConfigurationMatrix;
import models.radialVariation.RadialLandMap;
import models.view.AlgorithmView;

public class RadialTester {
	public static void main(String[] args) {
		long startTime = System.nanoTime();

		Runtime runtime = Runtime.getRuntime();
		long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocation" + usedMemoryBefore / 1000000 + " in MB");

		//int large = 950, width = 1200;
		//int large = 950, width = 750;
		// 1. We create the map and set its intrinsec variables
		//int large =1215, width = 850 ;
		AlgorithmView algorithmView = new AlgorithmView("radial-error.txt");
		ConfigurationMatrix configurationMatrix = new ConfigurationMatrix(algorithmView);

		int large = algorithmView.getxSize(), width = algorithmView.getySize();
		// 1. We create the map and set its intrinsec variables
		List<RadialLandPoint> polygon = new ArrayList<>();
		List<Integer> intVertex = algorithmView.getCartVertexgeocoords();
		for (int i = 0; i < algorithmView.getVertexgeocoords().size(); i += 2) {
			RadialLandPoint landPoint = new RadialLandPoint(intVertex.get(i), intVertex.get(i + 1));
			polygon.add(landPoint);
		}
		
		RadialLandPoint landPoint = new RadialLandPoint(intVertex.get(0), intVertex.get(1));
		polygon.add(landPoint);

		RadialLandPoint entryPoints = new RadialLandPoint(algorithmView.getCartEntrygeocoords().get(0),
													      algorithmView.getCartEntrygeocoords().get(1));
	

		
		
		
		RadialLandMap landMap = new RadialLandMap(large, width);
		//List<RadialLandPoint> polygon = new ArrayList<>();
		
		/*
		RadialLandPoint landPoint = new RadialLandPoint(0,700);
		polygon.add(landPoint);
		landPoint = new RadialLandPoint(725, 850);
		polygon.add(landPoint);
		landPoint = new RadialLandPoint(1215, 600);
		polygon.add(landPoint);
		landPoint = new RadialLandPoint(850, 0);
		polygon.add(landPoint);
		// we must reuse the first one as the last
		landPoint = new RadialLandPoint(0,700);
		polygon.add(landPoint);
		
		*/
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
		
		/*
		RadialLandPoint landPoint = new RadialLandPoint(726,56);
		polygon.add(landPoint);
		landPoint = new RadialLandPoint(924, 762);
		polygon.add(landPoint);
		landPoint = new RadialLandPoint(273, 1129);
		polygon.add(landPoint);
		landPoint = new RadialLandPoint(50, 528);
		polygon.add(landPoint);
		// we must reuse the first one as the last
		landPoint = new RadialLandPoint(726,56);
		polygon.add(landPoint);

		*/
		
		// 2. we create the border from the polygon
		
		landMap.createBorderFromPolygon(polygon);
		landMap.setConfiguration(configurationMatrix.getConfiguration().get(0));
		
		//RadialLandPoint entryPoint = new RadialLandPoint(700,160 );
		//RadialLandPoint entryPoint = new RadialLandPoint(625,450 );
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocated" + usedMemoryBefore / 1000000 + " in MB");
		
		
		radialAlgorithm radialAlgorithm = new radialAlgorithm();
		radialAlgorithm.setLandMap(landMap);
		
		radialAlgorithm.CreateRadialWeb(entryPoints);
		
		
		
		landMap.printMapToFile();

		
		///////////////////////////////////
	
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map after zonification" + usedMemoryBefore / 1000000 + " in MB");

		long endTime = System.nanoTime();
		long duration = (endTime - startTime) / (1000000 * 1000); // divide by
																	// to get
																	// milliseconds.
		System.out.println("Algorithm finished in " + duration + "s");
		// RadialLotizationAlgorithm.landMap.printMapToFile();

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
		//RadialPolygonTester radialTestPane = new RadialPolygonTester(true,
		//		radialAlgorithm.getLandMap().stringify(), large, width);		
		//new RadialTester(radialTestPane);
	
		
	//  RadialTestPane radialTestPane = new RadialTestPane(2, radialAlgorithm.getLandMap().getGrammar(), radialAlgorithm.getLandMap().getLandRoutes(), large, width);
	//	new RadialTester(radialTestPane);
		RadialTestPane radialTestPane = new RadialTestPane(2, radialAlgorithm.getLandMap().getNodes(), radialAlgorithm.getLandMap().getGrammar(), radialAlgorithm.getLandMap().getLandRoutes(), large, width);
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
	
	public RadialTester(RadialPolygonTester clusterPolygonTester) {
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
				JScrollPane scrPane = new JScrollPane(clusterPolygonTester);
				frame.add(scrPane); // similar to getContentPane().add(scrPane);

				// frame.add(new TestPane());
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}


}
