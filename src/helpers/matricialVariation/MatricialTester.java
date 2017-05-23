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
import interfaces.matricialVariation.MatricialConfiguration;
import models.configuration.ConfigurationMatrix;
import models.matricialVariation.MatricialLandMap;
import models.matricialVariation.MatricialLandPoint;
import models.view.AlgorithmView;

public class MatricialTester {
	public static void main(String[] args) {
		/** TODO segun el informe
		 manzana de 64x76m, con 36 lotes y pasajes
         peatonales al interior. Tendr� adem疽 dos tipos
         de lotes, un lote de 7.5x12m, orientado
         hacia la calle principal y un tipo de lote de
         6x15m orientado hacia el pasaje peatonal.*/		
		
		long startTime = System.nanoTime();
		//MAPA 1
		Runtime runtime = Runtime.getRuntime();
		long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocation" + usedMemoryBefore / 1000000 + " in MB");
		
		AlgorithmView algorithmView = new AlgorithmView("matricial-error.txt");
		ConfigurationMatrix configurationMatrix = new ConfigurationMatrix(algorithmView);
		int large = algorithmView.getxSize(), width = algorithmView.getySize();
		
		MatricialLandMap matricialLandMap = new MatricialLandMap(large, width);
		List<MatricialLandPoint> polygon = new ArrayList<>();
		List<Integer> intVertex = algorithmView.getCartVertexgeocoords();
		List<Integer> coordinates= new ArrayList<>();
		for (int i = 0; i < algorithmView.getVertexgeocoords().size(); i += 2) {
			coordinates.add(intVertex.get(i));
			coordinates.add(intVertex.get(i+1));
			MatricialLandPoint landPoint = new MatricialLandPoint(intVertex.get(i), intVertex.get(i + 1));
			polygon.add(landPoint);
		}
		
		matricialLandMap.setCoordinates(coordinates);
		MatricialLandPoint landPoint = new MatricialLandPoint(intVertex.get(0), intVertex.get(1));
		polygon.add(landPoint);
		
		matricialLandMap.createBorderFromPolygon(polygon);
		List<MatricialLandPoint> entryPoints = new ArrayList<>();
		landPoint = new MatricialLandPoint(algorithmView.getCartEntrygeocoords().get(0),
				algorithmView.getCartEntrygeocoords().get(1));
		entryPoints.add(landPoint);
		
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocated" + usedMemoryBefore / 1000000 + " in MB");
		
		// TRUE BEGINNING OF THE ALGORITHM
		//matricialLandMap.createMapBorder(polygon);
		matricialLandMap.setConfiguration(configurationMatrix.getConfiguration().get(0));
		MatricialAlgorithm matricialAlgorithm = new MatricialAlgorithm();
		matricialAlgorithm.setLandMap(matricialLandMap);
		matricialAlgorithm.setWidth(width);
		matricialAlgorithm.setLarge(large);
		matricialAlgorithm.setEntryPoint(entryPoints.get(0));
		
		matricialAlgorithm.matricialZonification();
		
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map after completed routes" + usedMemoryBefore / 1000000 + " in MB");
		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map after zonification" + usedMemoryBefore / 1000000 + " in MB");
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/(1000000*1000);  //divide by  to get milliseconds.
		endTime = System.nanoTime();
		duration = (endTime - startTime)/(1000000*1000);  //divide by  to get milliseconds.
		System.out.println("Response build finished in " + duration + "s");
		System.out.println("Start canvas");
		MatricialTestPane spineTestPane1 = new MatricialTestPane(1, matricialAlgorithm.getLandMap().stringify(), matricialAlgorithm.getLandMap().getLandRoutes(), large, width);
		new MatricialTester(spineTestPane1); 
		
		
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