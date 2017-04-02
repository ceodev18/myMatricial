package helpers.radialVariation;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

//import algorithm.radialVariation.RadialAlgorithm;
import helpers.radialVariation.RadialDirectionHelper;
import helpers.radialVariation.RadialTestPane;
import helpers.radialVariation.RadialTester;
import interfaces.radialVariation.RadialConfiguration;
import models.clusterVariation.ClusterLandPoint;
import models.radialVariation.RadialLandMap;
import models.radialVariation.RadialLandPoint;

public class RadialTester {
	public static void main(String[] args) {
		long startTime = System.nanoTime();

		Runtime runtime = Runtime.getRuntime();
		long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocation" + usedMemoryBefore / 1000000 + " in MB");

		int large = 950, width = 750;
		// 1. We create the map and set its intrinsec variables
		RadialLandMap landMap = new RadialLandMap(large, width);
		List<RadialLandPoint> polygon = new ArrayList<>();
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
		// 2. we create the border from the polygon
		landMap.createBorderFromPolygon(polygon);
		
		List<RadialLandPoint> entryPoints = new ArrayList<>();
		landPoint = new RadialLandPoint(208, 146);
		entryPoints.add(landPoint);
		
		landMap.printMapToFile();

	
	}
	public RadialTester(RadialTestPane clusterTestPane) {
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
				JScrollPane scrPane = new JScrollPane(clusterTestPane);
				frame.add(scrPane); // similar to getContentPane().add(scrPane);

				// frame.add(new TestPane());
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

}
