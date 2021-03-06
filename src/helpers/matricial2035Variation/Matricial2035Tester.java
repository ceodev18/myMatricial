package helpers.matricial2035Variation;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import algorithm.matricial2035Variation.Matricial2035Algorithm;
import models.configuration.ConfigurationMatrix;
import models.matricial2035Variation.Matricial2035LandMap;
import models.matricial2035Variation.Matricial2035LandPoint;
import models.matricial2035Variation.Matricial2035RotationPoint;
import models.view.AlgorithmView;

public class Matricial2035Tester {
	public static void main(String[] args) {
		/**
		 * La ret�cula para pendientes del 20 al 35% tendr� tambi�n manzanas de
		 * 64x76m con dos pasajes peatonales, sin embargo s�lo tendr� un tipo de
		 * lote de 6x15m, orientado seg�n la direcci�n del pasaje.
		 */

		Runtime runtime = Runtime.getRuntime();
		long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocation" + usedMemoryBefore / 1000000 + " in MB");

		AlgorithmView algorithmView = new AlgorithmView("cluster-error.txt");
		ConfigurationMatrix configurationMatrix = new ConfigurationMatrix(algorithmView);

		int large = algorithmView.getxSize(), width = algorithmView.getySize();

		// 1. To be able to make rotationary changes we need to work with the
		// points before the map creation
		// 1.1. We create the map and set its intrinsec variables
		List<Matricial2035LandPoint> polygon = new ArrayList<>();
		List<Integer> intVertex = algorithmView.getCartVertexgeocoords();
		for (int i = 0; i < algorithmView.getVertexgeocoords().size(); i += 2) {
			Matricial2035LandPoint landPoint = new Matricial2035LandPoint(intVertex.get(i), intVertex.get(i + 1));
			polygon.add(landPoint);
		}
		Matricial2035LandPoint landPoint = new Matricial2035LandPoint(intVertex.get(0), intVertex.get(1));
		polygon.add(landPoint);

		List<Matricial2035LandPoint> entryPoints = new ArrayList<>();
		landPoint = new Matricial2035LandPoint(algorithmView.getCartEntrygeocoords().get(0),
				algorithmView.getCartEntrygeocoords().get(1));
		entryPoints.add(landPoint);

		//1.2. We get the direction angle to create the main route
		Matricial2035RotationPoint matricial2035RotationPoint = Matricial2035DirectionHelper.getRotationPoint(entryPoints.get(0), polygon);
		
		//1.3. The entry point and vertex points are changed
		List<Matricial2035LandPoint> rotatedPolygon = new ArrayList<>();
		for (int i = 0; i < polygon.size(); i ++) {
			landPoint = matricial2035RotationPoint.rotatePoint(polygon.get((i)));
			rotatedPolygon.add(landPoint);
		}
		
		
		
		Matricial2035LandPoint entry = new Matricial2035LandPoint(algorithmView.getCartEntrygeocoords().get(0),
				algorithmView.getCartEntrygeocoords().get(1));
		entryPoints = new ArrayList<>();
		landPoint = matricial2035RotationPoint.rotatePoint(entry);
		entryPoints.add(landPoint);

		int [] translationUnit = new int[2];
		Matricial2035DirectionHelper.translate(polygon, entryPoints, translationUnit);
		
		
		//1.4. Even the large and width
		int [] newSizes = new int[2];
		newSizes[0]=0;
		newSizes[1]=0;
		Matricial2035DirectionHelper.resize(polygon, newSizes);
		
		// TRUE BEGINNING OF THE ALGORITHM
		Matricial2035LandMap landMap = new Matricial2035LandMap(newSizes[0], newSizes[1]);
		// 2. we create the border from the polygon
		landMap.createMapBorder(polygon);
		landMap.setConfiguration(configurationMatrix.getConfiguration().get(0));

		// 3. We create the entry points for the main routes
		Matricial2035Algorithm matricial2035Algorithm = new Matricial2035Algorithm();
		matricial2035Algorithm.setLandMap(landMap);

		// 4. We make the matrix
		matricial2035Algorithm.clusterize(entryPoints.get(0));

		// 5. Zonification
		matricial2035Algorithm.zonify();

		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Final Memory" + usedMemoryBefore / 1000000 + " in MB");

		Matricial2035TestPane clusterTestPane = new Matricial2035TestPane(2,
				matricial2035Algorithm.getLandMap().getNodes(), matricial2035Algorithm.getLandMap().getGrammar(),
				matricial2035Algorithm.getLandMap().getLandRoutes(), newSizes[0], newSizes[1]);
		
		/*Matricial2035TestPane clusterTestPane = new Matricial2035TestPane(2,
				matricial2035Algorithm.getLandMap().getNodes(), matricial2035Algorithm.getLandMap().getGrammar(),
				matricial2035Algorithm.getLandMap().getLandRoutes(), large, width);*/
		new Matricial2035Tester(clusterTestPane);
	}

	public Matricial2035Tester(Matricial2035TestPane clusterTestPane) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException ex) {
				}

				JFrame frame = new JFrame("Matricial Testing");
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

	public Matricial2035Tester(Matricial2035PolygonTester clusterPolygonTester) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException ex) {
				}

				JFrame frame = new JFrame("Matricial Testing");
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