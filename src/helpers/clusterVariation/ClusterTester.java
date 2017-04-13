package helpers.clusterVariation;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import algorithm.clusterVariation.ClusterAlgorithm;
import models.clusterVariation.ClusterLandMap;
import models.clusterVariation.ClusterLandPoint;

public class ClusterTester {
	public static void main(String[] args) {
		long startTime = System.nanoTime();

		Runtime runtime = Runtime.getRuntime();
		long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocation" + usedMemoryBefore / 1000000 + " in MB");

		int large = 925, width = 1130;
		// 1. We create the map and set its intrinsec variables
		ClusterLandMap landMap = new ClusterLandMap(large, width);
		List<ClusterLandPoint> polygon = new ArrayList<>();
		ClusterLandPoint landPoint = new ClusterLandPoint(726, 20);
		polygon.add(landPoint);
		landPoint = new ClusterLandPoint(924, 762);
		polygon.add(landPoint);
		landPoint = new ClusterLandPoint(273, 1129);
		polygon.add(landPoint);
		landPoint = new ClusterLandPoint(0, 528);
		polygon.add(landPoint);
		// we must reuse the first one as the last
		landPoint = new ClusterLandPoint(726,20);
		polygon.add(landPoint);

		// 2. we create the border from the polygon
		landMap.createMapBorder(polygon);
		
		// 3, We create the entry points for the main routes
		List<ClusterLandPoint> entryPoints = new ArrayList<>();
		landPoint = new ClusterLandPoint(487, 174);
		entryPoints.add(landPoint);

		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocated" + usedMemoryBefore / 1000000 + " in MB");

		ClusterAlgorithm clusterAlgorithm = new ClusterAlgorithm();
		clusterAlgorithm.setLandMap(landMap);

		// 4. We clusterize the points
		clusterAlgorithm.clusterize(entryPoints.get(0));

		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map after completed routes" + usedMemoryBefore / 1000000 + " in MB");

		// 5. Zonification
		/*List<ClusterPolygon> orcs= */clusterAlgorithm.zonify();

		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map after zonification" + usedMemoryBefore / 1000000 + " in MB");

		long endTime = System.nanoTime();
		long duration = (endTime - startTime) / (1000000 * 1000); // divide by
																	// to get
																	// milliseconds.
		System.out.println("Algorithm finished in " + duration + "s");

		clusterAlgorithm.getLandMap().printMapToFile();
		
		endTime = System.nanoTime();
		duration = (endTime - startTime) / (1000000 * 1000); // divide by to get
																// milliseconds.
		System.out.println("Response build finished in " + duration + "s");

		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Final Memory" + usedMemoryBefore / 1000000 + " in MB");

		//ClusterPolygonTester clusterPolygonTester = new ClusterPolygonTester(orcs);
		//new ClusterTester(clusterPolygonTester);

		//2 variants
		ClusterTestPane clusterTestPane = new ClusterTestPane(clusterAlgorithm.getLandMap(), clusterAlgorithm.getLandMap().getLandRoutes(), large, width);
		//ClusterTestPane clusterTestPane = new ClusterTestPane(true, clusterAlgorithm.getLandMap().stringify(), clusterAlgorithm.getLandMap().getLandRoutes(), large, width);		
		new ClusterTester(clusterTestPane);
	}

	public ClusterTester(ClusterTestPane clusterTestPane) {
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

	public ClusterTester(ClusterPolygonTester clusterPolygonTester) {
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