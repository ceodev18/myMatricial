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
import models.configuration.ConfigurationMatrix;
import models.view.AlgorithmView;

public class ClusterTester {
	public static void main(String[] args) {
//TODO 300 M case (Its entering the optimization code. Meaning it cannot render a normal lotization because the shit is too small
		Runtime runtime = Runtime.getRuntime();
		long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocation" + usedMemoryBefore / 1000000 + " in MB");

		AlgorithmView algorithmView = new AlgorithmView("cluster-error.txt");
		ConfigurationMatrix configurationMatrix = new ConfigurationMatrix(algorithmView);

		int large = algorithmView.getxSize(), width = algorithmView.getySize();
		// 1. We create the map and set its intrinsec variables
		List<ClusterLandPoint> polygon = new ArrayList<>();
		List<Integer> intVertex = algorithmView.getCartVertexgeocoords();
		for (int i = 0; i < algorithmView.getVertexgeocoords().size(); i += 2) {
			ClusterLandPoint landPoint = new ClusterLandPoint(intVertex.get(i), intVertex.get(i + 1));
			polygon.add(landPoint);
		}
		ClusterLandPoint landPoint = new ClusterLandPoint(intVertex.get(0), intVertex.get(1));
		polygon.add(landPoint);

		List<ClusterLandPoint> entryPoints = new ArrayList<>();
		landPoint = new ClusterLandPoint(algorithmView.getCartEntrygeocoords().get(0),
				algorithmView.getCartEntrygeocoords().get(1));
		entryPoints.add(landPoint);

		// TRUE BEGINNING OF THE ALGORITHM
		ClusterLandMap landMap = new ClusterLandMap(large, width);
		// 2. we create the border from the polygon
		landMap.createMapBorder(polygon);
		landMap.setConfiguration(configurationMatrix.getConfiguration().get(0));

		// 3. We create the entry points for the main routes
		ClusterAlgorithm clusterAlgorithm = new ClusterAlgorithm();
		clusterAlgorithm.setLandMap(landMap);

		// 4. We clusterize the points
		clusterAlgorithm.clusterize(entryPoints.get(0));

		// 5. Zonification
		clusterAlgorithm.zonify();

		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Final Memory" + usedMemoryBefore / 1000000 + " in MB");

		// First variant to test directly on the map. Second for the ws response
		// ClusterTestPane clusterTestPane = new
		// ClusterTestPane(clusterAlgorithm.getLandMap(),
		// clusterAlgorithm.getLandMap().getLandRoutes(), large, width);
		//ClusterTestPane clusterTestPane = new ClusterTestPane(1, clusterAlgorithm.getLandMap().stringify(), clusterAlgorithm.getLandMap().getLandRoutes(), large, width);
		//new ClusterTester(clusterTestPane); 
		ClusterTestPane clusterTestPane = new ClusterTestPane(2, clusterAlgorithm.getLandMap().getNodes(), clusterAlgorithm.getLandMap().getGrammar(), clusterAlgorithm.getLandMap().getLandRoutes(), large, width);
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