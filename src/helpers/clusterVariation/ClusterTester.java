package helpers.clusterVariation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import algorithm.clusterVariation.ClusterAlgorithm;
import helpers.base.MapHelper;
import interfaces.clusterVariation.ClusterConfiguration;
import models.clusterVariation.ClusterLandMap;
import models.clusterVariation.ClusterLandPoint;

public class ClusterTester {
	public static void main(String[] args) {
		long startTime = System.nanoTime();

		Runtime runtime = Runtime.getRuntime();
		long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocation" + usedMemoryBefore/1000000 + " in MB");

		int large = 925, width = 1130;
		// 1. We create the map and set its intrinsec variables
		ClusterLandMap landMap = new ClusterLandMap(large, width);
		List<ClusterLandPoint> polygon = new ArrayList<>();
		ClusterLandPoint landPoint = new ClusterLandPoint(726, 0);
		polygon.add(landPoint);
		landPoint = new ClusterLandPoint(924, 762);
		polygon.add(landPoint);
		landPoint = new ClusterLandPoint(273, 1129);
		polygon.add(landPoint);
		landPoint = new ClusterLandPoint(0, 528);
		polygon.add(landPoint);
		// we must reuse the first one as the last
		landPoint = new ClusterLandPoint(726, 0);
		polygon.add(landPoint);
		// 2. we create the border from the polygon
		landMap.createBorderFromPolygon(polygon);
		// 3, We create the entry points for the main routes
		// Entry points = [327,981][1710,742]
		List<ClusterLandPoint> entryPoints = new ArrayList<>();
		landPoint = new ClusterLandPoint(487, 174);
		entryPoints.add(landPoint);

		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map allocated" + usedMemoryBefore/1000000 + " in MB");
		
		ClusterAlgorithm clusterAlgorithm = new ClusterAlgorithm();
		clusterAlgorithm.setLandMap(landMap);
		
		//LSystemClusterAlgorithm.landMap = landMap;
		for (ClusterLandPoint entryPoint : entryPoints) {
			int direction = ClusterDirectionHelper.orthogonalDirectionFromPointToPoint(entryPoint,
					landMap.getCentroid());
			clusterAlgorithm.createRouteVariation(entryPoint.getId(), direction,
					ClusterConfiguration.ARTERIAL_BRANCH);
			break;
		}

		// 4. We clusterize the points
		clusterAlgorithm.clusterize();

		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map after completed routes" + usedMemoryBefore/1000000 + " in MB");
		
		// 5. Zonification
		clusterAlgorithm.zonify();

		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory map after zonification" + usedMemoryBefore/1000000 + " in MB");
		
		long endTime = System.nanoTime();
		long duration = (endTime - startTime) / (1000000 * 1000); // divide by
																	// to get
																	// milliseconds.
		System.out.println("Algorithm finished in " + duration + "s");
		// ClusterLotizationAlgorithm.landMap.printMapToFile();
		
		clusterAlgorithm.getLandMap().printMapToFile();
		//String compressedString = clusterAlgorithm.getLandMap().stringify();
		//System.out.println("Compressed String lenght: " + compressedString.length());

		endTime = System.nanoTime();
		duration = (endTime - startTime) / (1000000 * 1000); // divide by to get
																// milliseconds.
		System.out.println("Response build finished in " + duration + "s");

		usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Final Memory" + usedMemoryBefore/1000000 + " in MB");
		
		TestPane.clusterLandMap = clusterAlgorithm.getLandMap();
		TestPane.large = large;
		TestPane.width = width;
		new ClusterTester();
		//
		/*
		 * System.out.print(ClusterLotizationAlgorithm.landMap.stringify());
		 */
	}

	public ClusterTester() {
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
				JScrollPane scrPane = new JScrollPane(new TestPane());
				frame.add(scrPane); // similar to getContentPane().add(scrPane);

				// frame.add(new TestPane());
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

	public static class TestPane extends JPanel {
		private static final long serialVersionUID = 1L;
		public static ClusterLandMap clusterLandMap;
		private int growtXY = 1;
		public static int large = 35;
		public static int width = 35;

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(large, width);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			int growthX = 0, growthY = 0;
			for (int y = clusterLandMap.getPointsy() - 1; y >= 0; y--) {
				for (int x = 0; x < clusterLandMap.getPointsx(); x++) {
					String type = clusterLandMap.findPoint(MapHelper.formKey(x, y)).getType();
					switch (type) {/* 39 + 40*0 | 0+ 40*1 */
					case "a":
					case "b":
					case "c":
					case "w":
					case "t":
						g.setColor(Color.GRAY);
						break;
					case "p":
						g.setColor(Color.GREEN);
						break;
					case "l":
						g.setColor(Color.RED);
						break;
					case "e":
					case "n":
						g.setColor(Color.MAGENTA);
						break;
					case " ":
						g.setColor(Color.WHITE);
						break;
					default: // is an avenue or a street
						int houseLot = Integer.valueOf(type);
						switch (houseLot % 9) {
						case 1:
							g.setColor(Color.BLACK);
							break;
						case 2:
							g.setColor(Color.LIGHT_GRAY);
							break;
						case 3:
							g.setColor(Color.CYAN);
							break;
						case 4:
							g.setColor(Color.WHITE);
							break;
						case 5:
							g.setColor(Color.YELLOW);
							break;
						case 6:
							g.setColor(Color.decode("#472544"));
							break;
						case 7:
							g.setColor(Color.ORANGE);
							break;
						case 8:
							g.setColor(Color.BLUE);
							break;
						default:
							g.setColor(Color.PINK);
							break;
						}

						break;
					}

					g.fillRect(growthX, growthY, (growthX + growtXY), (growthY + growtXY));
					growthX += growtXY;

				}
				growthX = 0;
				growthY += growtXY;
			}
		}
	}
}